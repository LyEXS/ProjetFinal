package Objects;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

public class Ville {
    private final StringProperty nomVille;
    private final IntegerProperty codePostal;

    // Constructeur
    public Ville(String nomVille, int codePostal) {
        this.nomVille = new SimpleStringProperty(nomVille);
        this.codePostal = new SimpleIntegerProperty(codePostal);
    }

    // Getter et Setter pour nomVille
    public String getNomVille() {
        return nomVille.get();
    }

    public void setNomVille(String nomVille) {
        this.nomVille.set(nomVille);
    }

    public StringProperty nomVilleProperty() {
        return nomVille;
    }

    // Getter et Setter pour codePostal
    public int getCodePostal() {
        return codePostal.get();
    }

    public void setCodePostal(int codePostal) {
        this.codePostal.set(codePostal);
    }

    public IntegerProperty codePostalProperty() {
        return codePostal;
    }
}
