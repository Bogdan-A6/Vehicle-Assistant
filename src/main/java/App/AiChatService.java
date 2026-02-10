package App;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Serviciu care faciliteaza comunicarea cu API-ul GroqCloud.
 * Este optimizat pentru raspunsuri rapide si foloseste formatul JSON standard OpenAI.
 */
public class AiChatService {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = "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";
    private static final String MODEL_NAME = "llama-3.3-70b-versatile";

    /**
     * Trimite o intrebare catre AI intr-un mod non-blocant folosind CompletableFuture.
     * Acest lucru previne inghetarea interfetei Swing in timpul apelului de retea.
     * @param car Obiectul masina pentru a oferi context asistentului AI.
     * @param userQuestion Intrebarea primita de la utilizator.
     * @return Un CompletableFuture care va contine raspunsul generat de AI.
     */
    public CompletableFuture<String> askAi(Car car, String userQuestion) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String systemContext = "Esti un mecanic auto util. Masina: " + car.getYear() + " " + car.getMake();
                
                String jsonBody = "{"
                        + "\"model\": \"" + MODEL_NAME + "\","
                        + "\"messages\": ["
                        + "  {\"role\": \"system\", \"content\": \"" + systemContext + "\"},"
                        + "  {\"role\": \"user\", \"content\": \"" + userQuestion + "\"}"
                        + "],"
                        + "\"stream\": false"
                        + "}";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GROQ_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + API_KEY)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return (response.statusCode() == 200) ? extractResponseFromGroqJson(response.body()) : "Eroare Groq.";

            } catch (Exception e) {
                return "Nu am putut contacta GroqCloud.";
            }
        });
    }
    
    /** @return Mesajul de intampinare personalizat pentru modelul masinii. */
    public String getInitialGreeting(Car car) {
        return "Salut! Analizez datele pentru " + car.getMake() + " " + car.getModel() + ". Cu ce te ajut?";
    }

    /**
     * Metoda de parsare manuala a JSON-ului pentru a extrage campul 'content'.
     * @param jsonResponse Corpul raspunsului de la API.
     * @return Textul curat al raspunsului AI.
     */
    private String extractResponseFromGroqJson(String jsonResponse) {
        try {
            String key = "\"content\":\"";
            int startIndex = jsonResponse.indexOf(key) + key.length();
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            while (jsonResponse.charAt(endIndex - 1) == '\\') {
                endIndex = jsonResponse.indexOf("\"", endIndex + 1);
            }
            return jsonResponse.substring(startIndex, endIndex).replace("\\n", "\n").replace("\\\"", "\"");
        } catch (Exception e) {
            return "Eroare la procesarea raspunsului.";
        }
    }
}