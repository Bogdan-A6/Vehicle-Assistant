package App;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Serviciu simplu care construieste un URL de cautare pentru Google.
 * Folosim niste operatori de cautare avansata (filetype:pdf) ca sa ajutam 
 * utilizatorul sa gaseasca direct manualele de service nu doar pagini de vanzari.
 */
public class ManualService {

    public String getManualSearchUrl(Car car) {
        if (car == null || car.getMake() == null) return "https://google.com";

        // Cream o interogare specifica pentru manuale de service
        String query = String.format("%s %s %s service manual filetype:pdf",
                car.getMake(),
                car.getModel(),
                car.getYear());

        return "https://www.google.com/search?q=" + encodeValue(query);
    }
    /**
     * Trebuie sa encodam string-ul pentru a fi sigur pentru URL (ex: spatiile devin %20).
     */
    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}