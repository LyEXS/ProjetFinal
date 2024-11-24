package Objects;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class Film {
    private final IntegerProperty idFilm;
    private final StringProperty titreFilm;
    private final StringProperty dureeFilm;
    private final StringProperty nomExpo;
    private final StringProperty genre;

    // Constructeur par défaut
    public Film() {
        this.idFilm = new SimpleIntegerProperty();
        this.titreFilm = new SimpleStringProperty();
        this.dureeFilm = new SimpleStringProperty();
        this.nomExpo = new SimpleStringProperty();
        this.genre = new SimpleStringProperty();
    }

    // Constructeur avec paramètres
    public Film(int idFilm, String titreFilm, String dureeFilm, String nomExpo, String genre) {
        this.idFilm = new SimpleIntegerProperty(idFilm);
        this.titreFilm = new SimpleStringProperty(titreFilm);
        this.dureeFilm = new SimpleStringProperty(dureeFilm);
        this.nomExpo = new SimpleStringProperty(nomExpo);
        this.genre = new SimpleStringProperty(genre);
    }

    // Getters et setters avec propriétés
    public int getIdFilm() {
        return idFilm.get();
    }

    public void setIdFilm(int idFilm) {
        this.idFilm.set(idFilm);
    }

    public IntegerProperty idFilmProperty() {
        return idFilm;
    }

    public String getTitreFilm() {
        return titreFilm.get();
    }

    public void setTitreFilm(String titreFilm) {
        this.titreFilm.set(titreFilm);
    }

    public StringProperty titreFilmProperty() {
        return titreFilm;
    }

    public String getDureeFilm() {
        return dureeFilm.get();
    }

    public void setDureeFilm(String dureeFilm) {
        this.dureeFilm.set(dureeFilm);
    }

    public StringProperty dureeFilmProperty() {
        return dureeFilm;
    }

    public String getNomExpo() {
        return nomExpo.get();
    }

    public void setNomExpo(String nomExpo) {
        this.nomExpo.set(nomExpo);
    }

    public StringProperty nomExpoProperty() {
        return nomExpo;
    }

    public String getGenre() {
        return genre.get();
    }

    public void setGenre(String genre) {
        this.genre.set(genre);
    }

    public StringProperty genreProperty() {
        return genre;
    }

    @Override
    public String toString() {
        return "Film{" +
                "idFilm=" + getIdFilm() +
                ", titreFilm='" + getTitreFilm() + '\'' +
                ", dureeFilm='" + getDureeFilm() + '\'' +
                ", nomExpo='" + getNomExpo() + '\'' +
                ", genre='" + getGenre() + '\'' +
                '}';
    }
}
