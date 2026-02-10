package App;

/**
 * Clasa de tip POJO (Plain Old Java Object) care serveste drept model de date pentru aplicatie.
 * O folosim pentru a stoca si transporta informatiile despre un vehicul intre baza de date, 
 * serviciul de scraping si interfata grafica.
 */
public class Car {

    private String vin;
    private String make;
    private String model;
    private String year;
    private String engineType;
    private String fuelType;

    /**
     * Constructor pentru initializarea completa a unui obiect de tip Car.
     * @param vin Codul unic de identificare al sasiului.
     * @param make Marca producatorului .
     * @param model Modelul specific al masinii.
     * @param year Anul de fabricatie extras din baza de date sau de pe net.
     * @param engineType Detalii despre motorizare.
     * @param fuelType Tipul de combustibil utilizat.
     */
    public Car(String vin, String make, String model, String year, String engineType, String fuelType) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
        this.engineType = engineType;
        this.fuelType = fuelType;
    }

    /** @return Returneaza codul VIN salvat in obiect. */
    public String getVin() { return vin; }

    /** @return Returneaza marca masinii. */
    public String getMake() { return make; }

    /** @return Returneaza modelul masinii. */
    public String getModel() { return model; }

    /** @return Returneaza anul de fabricatie sub forma de String. */
    public String getYear() { return year; }

    /** @return Returneaza tipul de motor gasit. */
    public String getEngineType() { return engineType; }

    /** @return Returneaza tipul de carburant (Diesel, Benzina, etc.). */
    public String getFuelType() { return fuelType; }

    /**
     * Metoda suprascrisa pentru a genera un raport text.
     * Daca nu sunt date punem o cratima.
     * * @return Un String formatat cu toate detaliile tehnice ale masinii.
     */
    @Override
    public String toString() {
        return "Detalii pentru " + vin + " \n\n" +
                "Marca: \t" + (make != null ? make : "-") + "\n" +
                "Model: \t" + (model != null ? model : "-") + "\n" +
                "An: \t" + (year != null ? year : "-") + "\n" +
                "Motor: \t" + (engineType != null ? engineType : "-") + "\n" +
                "Carburant: \t" + (fuelType != null ? fuelType : "-");
    }
}