package Objects;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Film {
    private final IntegerProperty idFilm;
    private final StringProperty titreFilm;
    private final StringProperty dureeFilm;
    private final StringProperty nomExpo;
    private final StringProperty genre;
    private final ObjectProperty<Image> image; // Add image property

    // Default constructor
    public Film() {
        this.idFilm = new SimpleIntegerProperty();
        this.titreFilm = new SimpleStringProperty();
        this.dureeFilm = new SimpleStringProperty();
        this.nomExpo = new SimpleStringProperty();
        this.genre = new SimpleStringProperty();
        this.image = new SimpleObjectProperty<>(); // Initialize image property
    }

    // Constructor with parameters
    public Film(int idFilm, String titreFilm, String dureeFilm, String nomExpo, String genre, Image image) {
        this.idFilm = new SimpleIntegerProperty(idFilm);
        this.titreFilm = new SimpleStringProperty(titreFilm);
        this.dureeFilm = new SimpleStringProperty(dureeFilm);
        this.nomExpo = new SimpleStringProperty(nomExpo);
        this.genre = new SimpleStringProperty(genre);
        this.image = new SimpleObjectProperty<>(image); // Initialize image property
    }

    // Getters and setters with properties
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

    // Getter and setter for image
    public Image getImage() {
        return image.get();
    }

    public void setImage(Image image) {
        this.image.set(image);
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public byte[] imageToByteArray() throws IOException {
        Image img = image.get();  // Obtenir l'image depuis ObjectProperty
        if (img != null) {
            // Convertir l'image en BufferedImage
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(img, null);
            // Utiliser ByteArrayOutputStream pour convertir en byte[]
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);  // Choisissez le format ici ("jpg", "png", etc.)
            return byteArrayOutputStream.toByteArray();
        }
        return null;  // Si l'image est null
    }

    @Override
    public String toString() {
        return "Film{" +
                "idFilm=" + getIdFilm() +
                ", titreFilm='" + getTitreFilm() + '\'' +
                ", dureeFilm='" + getDureeFilm() + '\'' +
                ", nomExpo='" + getNomExpo() + '\'' +
                ", genre='" + getGenre() + '\'' +
                ", image=" + getImage() + // Include image in the toString method
                '}';
    }
}
