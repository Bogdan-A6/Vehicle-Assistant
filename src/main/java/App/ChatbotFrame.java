package App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Fereastra separata pentru chat-ul cu AI-ul de service auto.
 * Aceasta se deschide cand utilizatorul apasa butonul albastru din aplicatia principala.
 * Fereastra ramane intotdeauna deasupra ferestrelor principale pentru accesibilitate.
 */
public class ChatbotFrame extends JFrame {

    private final Car car;
    private final AiChatService aiService;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    /**
     * Constructorul ferestrei de chat care initializeaza componentele si configurari.
     * Fereastra este configurata pentru a ramane deasupra aplicatiei principale.
     * @param car Obiectul masina pentru care se deschide consultatia AI.
     * @param aiService Serviciul care gestioneaza comunicarea cu API-ul AI.
     */
    public ChatbotFrame(Car car, AiChatService aiService) {
        this.car = car;
        this.aiService = aiService;

        setTitle("AI Mechanic - " + car.getMake() + " " + car.getModel());
        setSize(400, 500);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null);
        setAlwaysOnTop(true); 

        initUI();
        
        appendMessage("AI", aiService.getInitialGreeting(car));
    }

    /**
     * Initializarea componentelor grafice ale interfetei utilizator.
     * Aceasta metoda configureaza zona de conversatie, campul de introducere si butonul de trimitere.
     * Se adauga si listener-i pentru evenimentele de tastatura si mouse.
     */
    private void initUI() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        inputField = new JTextField();
        sendButton = new JButton("Trimite");

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
    }

    /**
     * Proceseaza si trimite mesajul utilizatorului catre serviciul AI.
     * Aceasta metoda afiseaza mesajul utilizatorului in chat, dezactiveaza temporar controalele
     * si apeleaza API-ul AI intr-un mod asincron pentru a preveni blocarea interfetei grafice.
     */
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        appendMessage("Eu", text);
        inputField.setText("");
        inputField.setEnabled(false);
        sendButton.setEnabled(false);

        aiService.askAi(car, text).thenAccept(response -> {
            SwingUtilities.invokeLater(() -> {
                appendMessage("AI", response);
                inputField.setEnabled(true);
                sendButton.setEnabled(true);
                inputField.requestFocus();
            });
        });
    }

    /**
     * Adauga un mesaj nou in zona de conversatie si face scroll automat la ultimul mesaj.
     * Aceasta metoda formateaza mesajul cu numele expeditorului si asigura vizibilitatea.
     * @param sender Numele expeditorului.
     * @param message Textul mesajului de adaugat in conversatie.
     */
    private void appendMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}