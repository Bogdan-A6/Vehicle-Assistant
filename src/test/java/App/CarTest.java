package App;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CarTest {

    private Car testCar;
    private final String TEST_VIN = "TEST1234567890XYZ";

    @BeforeEach
    void setUp() {
        // initializam un obiect Car pentru teste
        testCar = new Car(TEST_VIN, "BMW", "3 Series", "2020", "2.0 Diesel", "Diesel");
        // ne asiguram ca baza de date este pregatita
        DatabaseManager.createNewTable();
    }

    /**
     * testeaza daca obiectul Car stocheaza corect datele prin constructor si gettere.
     */
    @Test
    void testCarModelData() {
        assertEquals(TEST_VIN, testCar.getVin());
        assertEquals("BMW", testCar.getMake());
        assertEquals("2020", testCar.getYear());
        assertTrue(testCar.toString().contains(TEST_VIN));
    }

    /**
     * testeaza salvarea si recuperarea unei masini din baza de date locala.
     */
    @Test
    void testDatabaseSave() {
        // salvam masina
        DatabaseManager.saveCar(testCar);
        
        // o cautam dupa VIN
        Car retrievedCar = DatabaseManager.getCarByVin(TEST_VIN);
        
        assertNotNull(retrievedCar, "Masina ar fi trebuit sa fie gasita în DB.");
        assertEquals("BMW", retrievedCar.getMake());
        assertEquals("3 Series", retrievedCar.getModel());
    }


    /**
     * Testeaza comportamentul serviciului principal când VIN-ul este invalid/inexistent.
     */
    @Test
    void testCarInfoServiceInvalidVin() {
        CarInfoService service = new CarInfoService();
        // Folosim un VIN care cel mai probabil nu exista
        Car result = service.getCarInfo("INVALID_VIN_123");
        
        assertNull(result, "Pentru un VIN invalid, rezultatul ar trebui sa fie null.");
    }
}