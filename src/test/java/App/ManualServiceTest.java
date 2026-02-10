package App;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ManualServiceTest {

	@Test
    void testUrlGeneration() {
        ManualService service = new ManualService();
        Car car = new Car("123", "Ford", "Focus", "2012", "1.6", "Benzina");

        String url = service.getManualSearchUrl(car);

        // Verificam daca URL-ul incepe corect
        assertTrue(url.startsWith("https://www.google.com/search?q="));
        
        // Verificam daca a inclus cuvintele cheie si a codat spatiile (Ford+Focus sau Ford%20Focus)
        // Nota: URLEncoder poate folosi + sau %20 pentru spatii, verificam daca contine partile esentiale
        assertTrue(url.contains("Ford"));
        assertTrue(url.contains("Focus"));
        assertTrue(url.contains("service"));
        assertTrue(url.contains("filetype%3Apdf") || url.contains("filetype:pdf"));
    }

    @Test
    void testNullCarReturnsGoogle() {
        ManualService service = new ManualService();
        String url = service.getManualSearchUrl(null);
        
        assertEquals("https://google.com", url);
    }

}
