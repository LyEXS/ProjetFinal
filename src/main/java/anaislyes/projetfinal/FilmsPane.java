package anaislyes.projetfinal;

import Objects.Film;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class FilmsPane extends BorderPane {
    public static TableView<Film> tableView;
    public static ObservableList<Film> data;

    public FilmsPane() {
        data = FXCollections.observableArrayList();
        tableView = new TableView<>();

        // Colonnes du tableau
        TableColumn<Film, String> titreFilm = new TableColumn<>("Titre du film");
        titreFilm.setCellValueFactory(new PropertyValueFactory<>("titreFilm"));

        TableColumn<Film, String> nomExp = new TableColumn<>("Nom d'exposition");
        nomExp.setCellValueFactory(new PropertyValueFactory<>("nomExpo"));

        TableColumn<Film, String> genre = new TableColumn<>("Catégorie du film");
        genre.setCellValueFactory(new PropertyValueFactory<>("genre"));

        TableColumn<Film, Integer> idFilm = new TableColumn<>("Id du film");
        idFilm.setCellValueFactory(new PropertyValueFactory<>("idFilm"));

        TableColumn<Film, String> dureeFilm = new TableColumn<>("Durée du film ");
        dureeFilm.setCellValueFactory(new PropertyValueFactory<>("dureeFilm"));

        tableView.getColumns().addAll(titreFilm, nomExp, genre, idFilm, dureeFilm);
       
        tableView.setMaxWidth(1000);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Charger les données depuis la base
        chargerDonneesDepuisBase();
        tableView.setItems(data);

        // Créer la barre de boutons pour ajouter, modifier, supprimer
        HBox buttonsBar = new HBox(10);
        buttonsBar.setStyle("-fx-padding: 10px;");

        Button btnAjouter = new Button("Ajouter");
        Button btnModifier = new Button("Modifier");
        Button btnSupprimer = new Button("Supprimer");

        btnAjouter.setOnAction(e -> ajouterFilm());
        btnModifier.setOnAction(e -> modifierFilm());
        btnSupprimer.setOnAction(e -> supprimerFilm());

        buttonsBar.getChildren().addAll(btnAjouter, btnModifier, btnSupprimer);
        buttonsBar.setAlignment(Pos.CENTER);

        // Ajouter les composants à l'interface
        this.setCenter(tableView);
        this.setBottom(buttonsBar);
    }

    private void chargerDonneesDepuisBase() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
            Statement stmt = con.createStatement();

            String query = "SELECT film.IdFilm, film.Titre, film.Duree, film.NomExp, " +
                    "GROUP_CONCAT(categorie.NomCategorie SEPARATOR ', ') AS NomCategorie " +
                    "FROM film " +
                    "JOIN filmscategories ON film.IdFilm = filmscategories.IdFilm " +
                    "JOIN categorie ON filmscategories.IdCategorie = categorie.IdCategorie " +
                    "GROUP BY film.IdFilm, film.Titre, film.Duree, film.NomExp";

            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int idFilm = rs.getInt("IdFilm");
                String titre = rs.getString("Titre");
                String duree = rs.getString("Duree");
                String nomExp = rs.getString("NomExp");
                String categories = rs.getString("NomCategorie");
                data.add(new Film(idFilm, titre, duree, nomExp, categories));
            }

            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données depuis la base.");
        }
    }

    private void ajouterFilm() {
        Dialog<Film> dialog = creerDialogueFilm(null);
        dialog.showAndWait().ifPresent(film -> {
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
                String query = "INSERT INTO film (Titre, Duree, NomExp) VALUES (?, ?, ?)";
                PreparedStatement pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, film.getTitreFilm());
                pstmt.setString(2, film.getDureeFilm());
                pstmt.setString(3, film.getNomExpo());
                pstmt.executeUpdate();

                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    film.setIdFilm(generatedKeys.getInt (1));
                }

                data.add(film);
                pstmt.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'ajouter le film.");
            }
        });
    }

    private void modifierFilm() {
        Film selectedFilm = tableView.getSelectionModel().getSelectedItem();
        if (selectedFilm != null) {
            Dialog<Film> dialog = creerDialogueFilm(selectedFilm);
            dialog.showAndWait().ifPresent(film -> {
                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
                    String query = "UPDATE film SET Titre = ?, Duree = ?, NomExp = ? WHERE IdFilm = ?";
                    PreparedStatement pstmt = con.prepareStatement(query);
                    pstmt.setString(1, film.getTitreFilm());
                    pstmt.setString(2, film.getDureeFilm());
                    pstmt.setString(3, film.getNomExpo());
                    pstmt.setInt(4, film.getIdFilm());
                    pstmt.executeUpdate();

                    data.set(data.indexOf(selectedFilm), film);
                    pstmt.close();
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Impossible de modifier le film.");
                }
            });
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un film à modifier.");
        }
    }

    private void supprimerFilm() {
        Film selectedFilm = tableView.getSelectionModel().getSelectedItem();
        if (selectedFilm != null) {
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
                String query = "DELETE FROM film WHERE IdFilm = ?";
                PreparedStatement pstmt = con.prepareStatement(query);
                pstmt.setInt(1, selectedFilm.getIdFilm());
                pstmt.executeUpdate();

                data.remove(selectedFilm);
                pstmt.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer le film.");
            }
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un film à supprimer.");
        }
    }

    private Dialog<Film> creerDialogueFilm(Film film) {
        Dialog<Film> dialog = new Dialog<>();
        dialog.setTitle(film == null ? "Ajouter un film" : "Modifier un film");

        TextField titreField = new TextField(film == null ? "" : film.getTitreFilm());
        TextField dureeField = new TextField(film == null ? "" : film.getDureeFilm());
        TextField expoField = new TextField(film == null ? "" : film.getNomExpo());

        Label validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: red;");

        // Ajouter un listener pour valider la durée au fur et à mesure de la saisie
        dureeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValidDuree(newValue)) {
                validationLabel.setText("Format de durée invalide (HH:MM).");
            } else {
                validationLabel.setText("");
            }
        });

        VBox vbox = new VBox(10,
                new Label("Titre:"), titreField,
                new Label("Durée (HH:MM):"), dureeField,
                new Label("Exposition:"), expoField,
                validationLabel
        );
        dialog.getDialogPane().setContent(vbox);

        ButtonType saveButtonType = ButtonType.OK;
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                if (!isValidDuree(dureeField.getText())) {
                    showAlert("Erreur de saisie", "Veuillez saisir une durée valide au format HH:MM.");
                    return null;
                }

                return new Film(
                        film == null ? 0 : film.getIdFilm(),
                        titreField.getText(),
                        dureeField.getText(),
                        expoField.getText(),
                        film == null ? "" : film.getGenre()
                );
            }
            return null;
        });

        return dialog;
    }

    private boolean isValidDuree(String duree) {
        // Vérifie que la durée suit le format HH:MM (par exemple, 02:30)
        return duree.matches("^([0-1]?\\d|2[0-3]):[0-5]\\d$");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
