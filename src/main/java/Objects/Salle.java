package Objects;

import javafx.beans.property.*;

public class Salle {
    private final StringProperty cinema; // Nom du cinéma
    private final StringProperty numSalle; // Numéro de la salle
    private final IntegerProperty capacite; // Capacité de la salle

    public Salle(String cinema, String numSalle, int capacite) {
        this.cinema = new SimpleStringProperty(cinema);
        this.numSalle = new SimpleStringProperty(numSalle);
        this.capacite = new SimpleIntegerProperty(capacite);
    }

    // Getters et Setters pour le cinéma
    public String getCinema() {
        return cinema.get();
    }

    public void setCinema(String value) {
        this.cinema.set(value);
    }

    public StringProperty cinemaProperty() {
        return cinema;
    }

    // Getters et Setters pour le numéro de salle
    public String getNumSalle() {
        return numSalle.get();
    }

    public void setNumSalle(String value) {
        this.numSalle.set(value);
    }

    public StringProperty numSalleProperty() {
        return numSalle;
    }

    // Getters et Setters pour la capacité
    public int getCapacite() {
        return capacite.get();
    }

    public void setCapacite(int value) {
        this.capacite.set(value);
    }

    public IntegerProperty capaciteProperty() {
        return capacite;
    }
}
