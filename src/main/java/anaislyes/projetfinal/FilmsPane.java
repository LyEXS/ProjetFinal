package anaislyes.projetfinal;

import Objects.Film;
import com.dlsc.gemsfx.TimePicker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Paths;
import java.sql.*;

import com.jfoenix.controls.JFXButton;

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
        
        TableColumn<Film, String> Annee = new TableColumn<>("Année de sortie ");
        Annee.setCellValueFactory(new PropertyValueFactory<>("Annee"));

        tableView.getColumns().addAll(titreFilm, nomExp, genre, idFilm,Annee, dureeFilm);
        tableView.setMaxWidth(1000);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        chargerDonneesDepuisBase();
        tableView.setItems(data);

        // Buttons bar
        HBox buttonsBar = new HBox(10);
        buttonsBar.setStyle("-fx-padding: 10px;");

        JFXButton btnAjouter = new JFXButton("Ajouter");
        JFXButton btnModifier = new JFXButton("Modifier");
        JFXButton btnSupprimer = new JFXButton("Supprimer");

        btnAjouter.setOnAction(e -> ajouterFilm());
        btnModifier.setOnAction(e -> modifierFilm());
        btnSupprimer.setOnAction(e -> supprimerFilm());
        
        TextField nomfilmField = new TextField();
        nomfilmField.setPromptText("Titre du film");
        
        JFXButton buttonRechercher = new JFXButton("Rechercher");
        buttonRechercher.setPrefWidth(140);
        
        VBox vbox = new VBox(10, nomfilmField, buttonRechercher);
        
        buttonRechercher.setOnAction(e->{
        	String titre = nomfilmField.getText();
        	ObservableList<Film> list = FXCollections.observableArrayList();
        	list = RechercherFilmsDepuisBase(titre);
        	tableView.setItems(list);
        });
        
        nomfilmField.setOnKeyPressed(e->{
        	buttonRechercher.fire();
        });
        
        
        
        

        buttonsBar.getChildren().addAll(btnAjouter, btnModifier, btnSupprimer);
        buttonsBar.setAlignment(Pos.CENTER);

        // Add components to layout
        this.setCenter(tableView);
        this.setBottom(buttonsBar);
        this.setLeft(vbox);
    }

    private void chargerDonneesDepuisBase() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
            Statement stmt = con.createStatement();

            String query = "SELECT film.IdFilm,film.AnneeSortie, film.Titre, film.Duree, film.NomExp, " +
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
                
                String annee = rs.getString("AnneeSortie");
                if(annee!=null)
                	annee = annee.substring(0, 4);
                
                
                data.add(new Film(idFilm, titre, duree, nomExp, categories,annee, null));
                // Null for image
            }

            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données depuis la base.");
        }
    }
    
    private ObservableList<Film> RechercherFilmsDepuisBase(String Titre){
    	ObservableList<Film> list = FXCollections.observableArrayList();
    	
    
    	try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
            String query = "SELECT film.IdFilm,film.AnneeSortie, film.Titre, film.Duree, film.NomExp, " +
                    "COALESCE(GROUP_CONCAT(categorie.NomCategorie SEPARATOR ', '), 'Aucune catégorie') AS NomCategorie " +
                    "FROM film " +
                    "LEFT JOIN filmscategories ON film.IdFilm = filmscategories.IdFilm " +
                    "LEFT JOIN categorie ON filmscategories.IdCategorie = categorie.IdCategorie " +
                    "where film.titre like ?"+
                    "GROUP BY film.IdFilm, film.Titre, film.Duree, film.NomExp";
            PreparedStatement st = con.prepareStatement(query);
            st.setString(1, "%" + Titre + "%");
            ResultSet rs = st.executeQuery();
            
            while (rs.next()) {
            	int idFilm = rs.getInt("IdFilm");
                String titre = rs.getString("Titre");
                String duree = rs.getString("Duree");
                String nomExp = rs.getString("NomExp");
                String categories = rs.getString("NomCategorie");
                String annee = rs.getString("AnneeSortie");
                list.add(new Film(idFilm, titre, duree, nomExp, categories,annee, null)); // Null for image
            }
    	}catch (Exception e) {
    		System.out.println(e.getMessage());
		}
    	return list;
            
            
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
                String query = "INSERT INTO film (Titre, Duree, NomExp,image,AnneeSortie) VALUES (?, ?, ?,?,?)";
                PreparedStatement pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, film.getTitreFilm());
                pstmt.setString(2, dureeFilm);
                pstmt.setString(3, film.getNomExpo());
                pstmt.setString(5, film.getAnnee());
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
                    
                    // Récupérer les genres et les séparer
                    String[] genres = film.getGenre().split(",");
                    System.out.println(Arrays.toString(genres));
                    String genre1 = genres[0];
                    String genre2 = genres.length > 1? genres[1] : null;
                    System.out.println(genre1);
                    System.out.println(genre2);
                    for (String genre : genres) {
                        genre = genre.trim(); // Supprimer les espaces inutiles
                        if (!genre.isEmpty()) {
                            PreparedStatement pstmt2 = con.prepareStatement(
                                "INSERT INTO filmscategories(idcategorie, idfilm) VALUES ((SELECT idCategorie FROM Categorie WHERE NomCategorie = ?), ?)"
                            );
                            pstmt2.setString(1, genre);
                            pstmt2.setInt(2, film.getIdFilm());
                            pstmt2.execute();
                        }
                    }
                    
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
            film = new Film(0, "", "", "", "", "", null); // Initialisation de film vide
        }

        final Film finalFilm = film;  // Déclarez 'film' comme final

        // Création des champs
        TextField titreField = new TextField(finalFilm.getTitreFilm());
        titreField.setPrefWidth(200);
        
        TextField dureeField = new TextField(finalFilm.getDureeFilm());
        TextField expoField = new TextField(finalFilm.getNomExpo());
        TextField anneeField = new TextField(finalFilm.getAnnee());
        anneeField.selectedTextProperty();
        Label selectImage = new Label("Image non sélectionnée");
        selectImage.setStyle("-fx-text-fill: red");

        // Ajouter TimePicker pour choisir la durée
        TimePicker timePicker = new TimePicker();
        timePicker.setClockType(null); // Format 24 heures

        // Préremplir le TimePicker avec la valeur actuelle de la durée
        if (finalFilm.getDureeFilm() != null && finalFilm.getDureeFilm().contains(":")) {
            String[] timeParts = finalFilm.getDureeFilm().split(":");
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            timePicker.setTime(LocalTime.of(hours, minutes));
        }
        ComboBox<String> categorieBox = new ComboBox<>();
        RemplirCategories(categorieBox);

        ComboBox<String> categorieBox2 = new ComboBox<>();
        RemplirCategories(categorieBox2);

        // Si un objet film est passé pour modification
        if (film != null) {
            String[] genres = film.getGenre().split(";"); // Séparer les genres
            categorieBox.getSelectionModel().select(genres[0].trim()); // Premier genre
            if (genres.length > 1) {
                categorieBox2.getSelectionModel().select(genres[1].trim()); // Deuxième genre
            }
        }

        
        // Bouton de sélection d'image
        Button imageButton = new Button("Choisir une image");
        imageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg"));

            // Définir le répertoire initial sur le dossier "images" dans "resources"
            String pathToImages = Paths.get("src/main/resources/images").toAbsolutePath().toString();
            File initialDirectory = new File(pathToImages);

            // Vérifier si le répertoire existe avant de le définir comme répertoire initial
            if (initialDirectory.exists() && initialDirectory.isDirectory()) {
                fileChooser.setInitialDirectory(initialDirectory);
            }

            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                finalFilm.setImage(image);
                selectImage.setText("Image sélectionnée");
                selectImage.setStyle("-fx-text-fill: green");
            }
        });

        // ImageView pour afficher l'image actuelle
        ImageView imageView = new ImageView(finalFilm.getImage());
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);

        // Utiliser un GridPane pour organiser les composants
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setStyle("-fx-padding: 20px;");
        gridPane.add(new Label("Titre :"), 0, 0);
        gridPane.add(titreField, 1, 0);
        gridPane.add(new Label("Durée (HH:MM) :"), 0, 1);
        gridPane.add(timePicker, 1, 1);
        gridPane.add(new Label("Exposition :"), 0, 2);
        gridPane.add(expoField, 1, 2);
        gridPane.add(new Label("Genre :"), 0, 3);
        gridPane.add(categorieBox, 1, 3);
        gridPane.add(categorieBox2, 2, 3);
        gridPane.add(new Label("Année de sortie :"), 0, 4);
        gridPane.add(anneeField, 1, 4);
        gridPane.add(new Label("Image :"), 0, 5);
        gridPane.add(imageButton, 1, 5);
        gridPane.add(imageView, 2, 5);

        dialog.getDialogPane().setContent(gridPane);

        // Boutons de validation
        ButtonType saveButtonType = ButtonType.OK;
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                LocalTime selectedTime = timePicker.getTime();
                String dureeComposee = String.format("%02d:%02d", selectedTime.getHour(), selectedTime.getMinute());
                String categories = categorieBox.getValue()+","+categorieBox2.getValue();
                return new Film(
                        finalFilm.getIdFilm(),
                        titreField.getText(),
                        dureeComposee,
                        expoField.getText(),
                        categorieBox.getValue()+","+categorieBox2.getValue(),
                        anneeField.getText(),
                        finalFilm.getImage()
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
