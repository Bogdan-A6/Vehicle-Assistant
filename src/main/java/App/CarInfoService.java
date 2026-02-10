package App;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviciul principal care gestioneaza fluxul de date.
 * Implementeaza o logica de tip "Local-First": verifica baza de date locala 
 * inainte de a face o cerere externa de scraping.
 */
class CarInfoService {

    /**
     * Incearca sa obtina datele unei masini pe baza VIN-ului.
     * Daca masina e gasita prin scraping, aceasta este salvata automat in baza de date.
     * @param vin Codul de sasiu cautat.
     * @return Un obiect Car cu date sau null daca nu s-a gasit nimic.
     */
    public Car getCarInfo(String vin) {
        System.out.println("caut VIN-ul: " + vin);
        Car car = DatabaseManager.getCarByVin(vin);

        if (car != null) {
            return car;
        }

        car = scrapeWebForCar(vin);
        if(car != null) {
            DatabaseManager.saveCar(car);
             return car;
        }
        return null;
    }

    /**
     * Realizeaza procesul de Web Scraping pe site-ul freevindecoder.eu.
     * Folosim HttpClient pentru request-ul asincron si Jsoup.
     * @param vinToLookup Codul VIN care trebuie adaugat in URL-ul de cautare.
     * @return Obiectul masina extras din tabelul HTML sau null in caz de eroare.
     */
    private Car scrapeWebForCar(String vinToLookup) {
        String apiUrl = "https://www.freevindecoder.eu/ro/" + vinToLookup;
        Map<String, String> date = new HashMap<>(); 
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build();

        try {
            HttpResponse<String> raspuns = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (raspuns.statusCode() == 200) {
                Document doc = Jsoup.parse(raspuns.body());
                Element infoTableDiv = doc.select("div.table-info").first();

                if (infoTableDiv != null) {
                    Elements rows = infoTableDiv.select("tr");
                    for(Element row : rows) {
                        Element keyCell = row.select("td.info-left").first();
                        Element valueCell = row.select("td.info-right").first();

                        if (keyCell != null && valueCell != null) {
                             date.put(keyCell.text(), valueCell.text());
                        }
                    }

                    if (!date.isEmpty()) {
                        return new Car(
                                vinToLookup,
                                date.getOrDefault("Face", null), 
                                date.getOrDefault("Model", null),
                                date.getOrDefault("Modelul anului", null),
                                date.getOrDefault("Tipul motorului", null),
                                date.getOrDefault("Tipul combustibilului", null)
                        );
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}