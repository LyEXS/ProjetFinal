package anaislyes.projetfinal;

import Objects.Film;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Paths;
import java.sql.*;

public class FilmsPane extends BorderPane {
    public static TableView<Film> tableView;
    public static ObservableList<Film> data;

    public FilmsPane() {
        data = FXCollections.observableArrayList();
        tableView = new TableView<>();

        // Table columns
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

        chargerDonneesDepuisBase();
        tableView.setItems(data);

        // Buttons bar
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

        // Add components to layout
        this.setCenter(tableView);
        this.setBottom(buttonsBar);
    }

    private void chargerDonneesDepuisBase() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
            Statement stmt = con.createStatement();

            String query = "SELECT film.IdFilm, film.Titre, film.Duree, film.NomExp, " +
                    "COALESCE(GROUP_CONCAT(categorie.NomCategorie SEPARATOR ', '), 'Aucune catégorie') AS NomCategorie " +
                    "FROM film " +
                    "LEFT JOIN filmscategories ON film.IdFilm = filmscategories.IdFilm " +
                    "LEFT JOIN categorie ON filmscategories.IdCategorie = categorie.IdCategorie " +
                    "GROUP BY film.IdFilm, film.Titre, film.Duree, film.NomExp";

            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int idFilm = rs.getInt("IdFilm");
                String titre = rs.getString("Titre");
                String duree = rs.getString("Duree");
                String nomExp = rs.getString("NomExp");
                String categories = rs.getString("NomCategorie");
                data.add(new Film(idFilm, titre, duree, nomExp, categories, null)); // Null for image
            }

            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données depuis la base.");
        }
    }

    private boolean verifierFormatHeure(String duree) {
        // Nettoyage des espaces avant et après
        duree = duree.trim();

        // Vérifie si la durée est au format HH:MM
        String regex = "^([01]?[0-9]|2[0-3]):([0-5]?[0-9])$";
        return duree.matches(regex);
    }
    private void ajouterFilm() {
        Dialog<Film> dialog = creerDialogueFilm(null);
        dialog.showAndWait().ifPresent(film -> {
            String dureeFilm = film.getDureeFilm();
            if (!verifierFormatHeure(dureeFilm)) {
                showAlert("Erreur", "Le format de la durée est incorrect. Utilisez le format HH:MM (par exemple 02:30 pour 2 heures et 30 minutes).");
                return;
            }

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
                String query = "INSERT INTO film (Titre, Duree, NomExp,image) VALUES (?, ?, ?,?)";
                PreparedStatement pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, film.getTitreFilm());
                pstmt.setString(2, dureeFilm);
                pstmt.setString(3, film.getNomExpo());
                byte[] imageBytes = film.imageToByteArray();  // Utilise la méthode pour convertir l'image en bytes
                if (imageBytes != null) {
                    pstmt.setBytes(4, imageBytes);  // Insérer l'image sous forme de bytes dans la requête
                } else {
                    pstmt.setNull(4, java.sql.Types.BLOB);  // Si l'image est null, on insère NULL
                }

                pstmt.executeUpdate();

                ResultSet generatedKeys = pstmt.getGeneratedKeys();

                if (generatedKeys.next()) {
                    film.setIdFilm(generatedKeys.getInt(1));
                    PreparedStatement pstmt2 = con.prepareStatement("INSERT INTO filmscategories(idcategorie,idfilm) VALUES ((SELECT idCategorie FROM Categorie WHERE NomCategorie = ?), ?)");
                    pstmt2.setString(1, film.getGenre());
                    pstmt2.setInt(2, film.getIdFilm());
                    pstmt2.execute();
                    data.add(film);
                }

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
                String dureeFilm = film.getDureeFilm();
                if (!verifierFormatHeure(dureeFilm)) {
                    showAlert("Erreur", "Le format de la durée est incorrect. Utilisez le format HH:MM.");
                    return;
                }

                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
                    String query = "UPDATE film SET Titre = ?, Duree = ?, NomExp = ?, image = ? WHERE IdFilm = ?";
                    PreparedStatement pstmt = con.prepareStatement(query);
                    pstmt.setString(1, film.getTitreFilm());
                    pstmt.setString(2, dureeFilm);
                    pstmt.setString(3, film.getNomExpo());
                    
                    byte[] imageBytes = film.imageToByteArray();  // Utiliser la méthode pour convertir l'image en bytes
                    if (imageBytes != null) {
                        pstmt.setBytes(4, imageBytes);  // Insérer l'image sous forme de bytes dans la requête
                    } else {
                        pstmt.setNull(4, java.sql.Types.BLOB);  // Si l'image est null, on insère NULL
                    }

                    pstmt.setInt(5, film.getIdFilm()); // Ajouter l'ID pour mettre à jour le bon film
                    pstmt.executeUpdate();

                    data.set(data.indexOf(selectedFilm), film);  // Mettre à jour les données de la table
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

        // Si film est null, initialisez-le ici
        if (film == null) {
            film = new Film(0, "", "", "", "", null); // Initialisation de film vide
        }

        final Film finalFilm = film;  // Déclarez 'film' comme final

        TextField titreField = new TextField(finalFilm.getTitreFilm());
        TextField dureeField = new TextField(finalFilm.getDureeFilm());
        TextField expoField = new TextField(finalFilm.getNomExpo());
        ComboBox<String> categorieBox = new ComboBox<>();
        RemplirCategories(categorieBox);

        // Add image selection button
        Button imageButton = new Button("Choisir une image");
        imageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg"));
            
            // Définir le répertoire initial sur le dossier "images" dans le répertoire "resources"
            String pathToImages = Paths.get("src/main/resources/images").toAbsolutePath().toString();
            File initialDirectory = new File(pathToImages);
            
            if (initialDirectory.exists()) {
                fileChooser.setInitialDirectory(initialDirectory); // Définir le répertoire initial
            }
            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                finalFilm.setImage(image); // Utiliser finalFilm ici pour mettre à jour l'image
            }	
        });

        // Display current image if present
        ImageView imageView = new ImageView(finalFilm.getImage());
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        VBox imageBox = new VBox(10, imageButton, imageView);

        VBox vbox = new VBox(10,
                new Label("Titre:"), titreField,
                new Label("Durée (HH:MM):"), dureeField,
                new Label("Exposition:"), expoField,
                new Label("Genre:"), categorieBox,
                imageBox, // Ajouter la sélection de l'image
                new Separator()
        );

        dialog.getDialogPane().setContent(vbox);

        ButtonType saveButtonType = ButtonType.OK;
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                return new Film(
                        finalFilm.getIdFilm(), // Conserver l'ID du film existant
                        titreField.getText(),
                        dureeField.getText(),
                        expoField.getText(),
                        categorieBox.getValue(),
                        finalFilm.getImage() // Conserver l'image si elle a été choisie
                );
            }
            return null;
        });

        return dialog;
    }




    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void RemplirCategories(ComboBox<String> comboBox) {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
            String query = "SELECT NomCategorie from Categorie";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                comboBox.getItems().add(rs.getString("NomCategorie"));
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de remplir les catégories.");
        }
    }
}
