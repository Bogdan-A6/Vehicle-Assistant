package App;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clasa responsabila pentru comunicarea cu baza de date Apache Derby.
 * Gestioneaza tabelele si operatiile afarente.
 */
public class DatabaseManager {

    private static final String URL = "jdbc:derby:CarDB;create=true";
    private static final String USER = "";
    private static final String PASS = "";

    /**
     * Metoda de bootstrap care se asigura ca mediul de stocare este pregatit.
     * Daca tabelul 'CARS' nu exista acesta este creat automat.
     */
    public static void createNewTable() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                DatabaseMetaData dbm = conn.getMetaData();
                ResultSet tables = dbm.getTables(null, null, "CARS", null);

                if (!tables.next()) {
                    try (Statement stmt = conn.createStatement()) {
                        String sql = "CREATE TABLE CARS ("
                                + " vin VARCHAR(50) PRIMARY KEY,"
                                + " make VARCHAR(100),"
                                + " model VARCHAR(100),"
                                + " year_prod VARCHAR(20),"  
                                + " engine VARCHAR(100),"
                                + " fuel VARCHAR(50),"
                                + " search_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                                + ")";
                        stmt.execute(sql);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Interogheaza baza de date pentru a gasi o masina salvata anterior.
     * @param vin Cheia primara dupa care se face cautarea.
     * @return Obiectul Car daca exista altfel null.
     */
    public static Car getCarByVin(String vin) {
        String sql = "SELECT * FROM CARS WHERE vin = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement abc = conn.prepareStatement(sql)) {
            abc.setString(1, vin);
            ResultSet rs = abc.executeQuery();
            if (rs.next()) {
                return mapRowToCar(rs);
            }
        } catch (SQLException e) {
             e.printStackTrace();
        }
        return null;
    }

    /**
     * Recupereaza istoricul cautarilor pentru a fi afisat in lista principala.
     * @return O lista de obiecte Car sortate descrescator dupa data cautarii.
     */
    public static List<Car> getAllHistory() {
        List<Car> history = new ArrayList<>();
        String sql = "SELECT * FROM CARS ORDER BY search_date DESC";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                history.add(mapRowToCar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    /**
     * Salveaza datele masinii sau actualizeaza data ultimei cautari daca VIN-ul exista deja.
     * Aceasta abordare previne erorile de tip 'Duplicate Key'.
     * @param car Obiectul care trebuie persistat in baza de date.
     */
    public static void saveCar(Car car) {
        String insertSql = "INSERT INTO CARS(vin, make, model, year_prod, engine, fuel, search_date) VALUES(?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        String updateSql = "UPDATE CARS SET search_date=CURRENT_TIMESTAMP WHERE vin=?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, car.getVin());
                pstmt.setString(2, car.getMake());
                pstmt.setString(3, car.getModel());
                pstmt.setString(4, car.getYear());
                pstmt.setString(5, car.getEngineType());
                pstmt.setString(6, car.getFuelType());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                try (PreparedStatement upstmt = conn.prepareStatement(updateSql)) {
                    upstmt.setString(1, car.getVin());
                    upstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mapare interna de la ResultSet SQL la obiect Java.
     * @param rs Setul de rezultate de la baza de date.
     * @return Un obiect Car mapat corect.
     * @throws SQLException In cazul in care coloanele nu sunt gasite.
     */
    private static Car mapRowToCar(ResultSet rs) throws SQLException {
        return new Car(
                rs.getString("vin"),
                rs.getString("make"),
                rs.getString("model"),
                rs.getString("year_prod"), 
                rs.getString("engine"),
                rs.getString("fuel")
        );
    }
}