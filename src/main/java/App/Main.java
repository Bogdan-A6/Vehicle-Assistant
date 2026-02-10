package App;

import javax.swing.SwingUtilities;

/**
 * Aceasta clasa initializeaza infrastructura aplicatiei si porneste interfata grafica.
 * Gestioneaza initializarea bazei de date si interfața Swing.
 */
public class Main {
    
    /**
     * Metoda principala care asigura initializarea corecta a aplicatiei.
     * Porneste prin crearea tabelelor necesare in baza de date, apoi lanseaza interfata grafica.
     * @param args Argumentele liniei de comanda 
     */
    public static void main(String[] args) {
        // Ne asiguram ca tabelul exista in baza de date inainte sa pornim orice functionalitate.
        DatabaseManager.createNewTable();
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.setVisible(true);
        });
    }
}