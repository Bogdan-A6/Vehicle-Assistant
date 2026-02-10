package App;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Fereastra principala a aplicatiei Asistent Auto Inteligent realizata in Swing.
 * Aceasta clasa implementeaza un CardLayout pentru gestionarea multiplelor
 * ecrane: cautare VIN si vizualizare detalii masina. Aplicatia integreaza diverse servicii
 * pentru obtinerea informatiei, gestionarea istoricului si comunicarea cu AI-ul.
 */
public class MainApp extends JFrame {

    private final CarInfoService carInfoService;
    private final ManualService manualService;
    private final AiChatService aiChatService;

    private final CardLayout cardLayout;
    private final JPanel mainContainer;
    
    private JTextField vinInput;
    private JButton searchButton;
    private JList<String> historyList;
    private DefaultListModel<String> historyListModel;
    private JLabel statusLabel;

    /**
     * Constructorul ferestrei principale care initializeaza toate componentele.
     * Configureaza dimensiunile, serviciile dependente si structura de layout-uri.
     * Aplicatia este centrata pe ecran si pregatita pentru operatii de cautare VIN.
     */
    public MainApp() {
        this.carInfoService = new CarInfoService();
        this.manualService = new ManualService();
        this.aiChatService = new AiChatService(); 

        setTitle("Asistent Reparare Vehicul"); 
        setSize(950, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Centreaza fereastra pe ecran
        setLocationRelativeTo(null); 

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        JPanel searchView = createSearchPanel();
        mainContainer.add(searchView, "SEARCH");

        add(mainContainer);
        refreshHistory();
        cardLayout.show(mainContainer, "SEARCH");
    }

    /**
     * Creeaza panoul principal de cautare VIN si afisare istoric.
     * Acest panou include un camp de introducere VIN, buton de cautare si o lista
     * interactiva cu istoricul cautarilor anterioare. Functionalitatea de double-click
     * pe elementele istoricului permite reincarcarea datelor.
     * @return JPanel configurat pentru operatiuni de cautare si navigare istoric.
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        vinInput = new JTextField("", 20);
        searchButton = new JButton("Cauta Vehicul");
        topPanel.add(new JLabel("Introdu VIN:"));
        topPanel.add(vinInput);
        topPanel.add(searchButton);
        panel.add(topPanel, BorderLayout.NORTH);

        historyListModel = new DefaultListModel<>();
        historyList = new JList<>(historyListModel);
        
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(new JLabel("Istoric cautari (Double-click pentru detalii):"), BorderLayout.NORTH);
        listPanel.add(new JScrollPane(historyList), BorderLayout.CENTER);
        panel.add(listPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Aplicatie pornita. Gata de scanare.");
        panel.add(statusLabel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> onSearch(vinInput.getText().trim().toUpperCase()));

        // Functionalitate pentru a reincarca rapid o masina din istoric prin double-click
        historyList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String selectedEntry = historyList.getSelectedValue();
                    if (selectedEntry != null) {
                        String vin = selectedEntry.split(" - ")[0];
                        vinInput.setText(vin);
                        onSearch(vin);
                    }
                }
            }
        });

        return panel;
    }

    /**
     * Creeaza panoul de detalii pentru o masina specifica identificata prin VIN.
     * Acest panou utilizeaza un JSplitPane pentru a separa detaliile tehnice de
     * chat-ul interactiv cu AI-ul. Includerea manualelor PDF si conversatia AI.
     * @param car Obiectul masina pentru care se construieste panoul de detalii.
     * @return JPanel structurat care afiseaza datele masinii si permite interactiunea cu AI.
     */
    private JPanel createDetailsPanel(Car car) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Zona Detalii Masina - partea stanga a splitPane-ului
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informatii Identificate"));
        
        JTextArea infoArea = new JTextArea(car.toString());
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);

        JPanel carButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBack = new JButton("Inapoi");
        JButton btnManual = new JButton("Cauta Manual PDF");
        
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "SEARCH"));
        btnManual.addActionListener(e -> openWebLink(manualService.getManualSearchUrl(car)));
        
        carButtons.add(btnBack);
        carButtons.add(btnManual);
        infoPanel.add(carButtons, BorderLayout.SOUTH);

        // Zona Chat AI - partea dreapta a splitPane-ului
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("Mecanic Virtual (Groq AI)"));
        chatPanel.setPreferredSize(new Dimension(400, 0));

        JTextArea chatLog = new JTextArea();
        chatLog.setEditable(false);
        chatLog.setLineWrap(true);
        chatLog.setWrapStyleWord(true);
        chatLog.append("AI: " + aiChatService.getInitialGreeting(car) + "\n\n");
        
        JScrollPane chatScroll = new JScrollPane(chatLog);
        chatPanel.add(chatScroll, BorderLayout.CENTER);

        JPanel chatInputPanel = new JPanel(new BorderLayout(5, 5));
        JTextField chatInputField = new JTextField();
        JButton btnSend = new JButton("Intreaba");
        
        chatInputPanel.add(chatInputField, BorderLayout.CENTER);
        chatInputPanel.add(btnSend, BorderLayout.EAST);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);

        // Actiunea de trimitere mesaj catre AI
        Runnable sendMessage = () -> {
            String text = chatInputField.getText().trim();
            if (text.isEmpty()) return;

            chatLog.append("Eu: " + text + "\n");
            chatInputField.setText("");
            chatInputField.setEnabled(false);

            // Apel catre serviciul AI
            aiChatService.askAi(car, text).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    chatLog.append("AI: " + response + "\n\n");
                    chatInputField.setEnabled(true);
                    chatInputField.requestFocus();
                    chatLog.setCaretPosition(chatLog.getDocument().getLength());
                });
            });
        };

        btnSend.addActionListener(e -> sendMessage.run());
        chatInputField.addActionListener(e -> sendMessage.run());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoPanel, chatPanel);
        splitPane.setDividerLocation(450); 
        
        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    /**
     * Executa procesul de cautare a unei masina dupa codul VIN.
     * Aceasta metoda se ocupa de intreg procesul de cautare, de la apasarea butonului
     * pana la afisarea rezultatelor. Pentru a nu ingheta interfata în timp ce se fac
     * apeluri catre site-uri externe si baza de date, cautarea ruleaza in fundal.
     * @param vin Codul VIN al vehiculului de cautat.
     */
    private void onSearch(String vin) {
        if (vin.isEmpty()) return;
        searchButton.setEnabled(false);
        statusLabel.setText("Se cauta in baza de date si pe internet...");

        SwingWorker<Car, Void> worker = new SwingWorker<Car, Void>() {
            @Override
            protected Car doInBackground() { return carInfoService.getCarInfo(vin); }

            @Override
            protected void done() {
                try {
                    Car car = get();
                    if (car != null) {
                        statusLabel.setText("Date incarcate cu succes pentru: " + vin);
                        refreshHistory();
                        mainContainer.add(createDetailsPanel(car), "DETAILS_" + vin);
                        cardLayout.show(mainContainer, "DETAILS_" + vin);
                    } else {
                        statusLabel.setText("Eroare: VIN-ul nu a fost gasit.");
                        JOptionPane.showMessageDialog(MainApp.this, "Nu s-au putut gasi date pentru acest VIN.");
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    statusLabel.setText("Eroare critica la cautare.");
                }
                searchButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    /**
     * Reincarca lista de istoric din baza de date si o afiseaza in JList.
     * Aceasta metoda aduce cele mai recente cautari efectuate si le formateaza
     * pentru afisarea compacta in interfața. 
     */
    private void refreshHistory() {
        historyListModel.clear();
        List<Car> cars = DatabaseManager.getAllHistory();
        for (Car c : cars) {
            String display = String.format("%s - %s %s (%s)",
                    c.getVin(),
                    (c.getMake() != null ? c.getMake() : "Necunoscut"),
                    (c.getModel() != null ? c.getModel() : ""),
                    (c.getYear() != null ? c.getYear() : "-"));
            historyListModel.addElement(display);
        }
    }

    /**
     * Deschide o adresa URL in browser-ul utilizatorului.
     * Utilizata pentru accesarea manualelor PDF si a altor resurse online.
     * Metoda include gestionarea exceptiilor pentru cazurile in care browser-ul
     * nu poate fi lansat sau adresa este invalida.
     * @param url Adresa web de deschis in browser.
     */
    private void openWebLink(String url) {
        try { Desktop.getDesktop().browse(new URI(url)); } catch (Exception e) {
            System.err.println("Nu am putut deschide browser-ul.");
        }
    }
}