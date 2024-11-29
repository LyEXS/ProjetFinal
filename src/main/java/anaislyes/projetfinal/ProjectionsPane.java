package anaislyes.projetfinal;

import Objects.Projection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ProjectionsPane extends BorderPane {
    private TableView<Projection> tableView;
    private ObservableList<Projection> projections, data;

    public ProjectionsPane() {
        Button buttonPlanifierProjection = new Button("Planifier Projections");
        Button buttonAnnulerProjection = new Button("Annuler Projection");
        Button buttonReplanifierProjection = new Button("Replanifier Projection");

        buttonPlanifierProjection.setOnAction(e -> {
            PlanifierProjection();
        });

        // Initialiser les données
        projections = FXCollections.observableArrayList();
        tableView = new TableView<>();

        // Colonnes de la table
        TableColumn<Projection, String> cinemaColumn = new TableColumn<>("Nom du cinema");
        cinemaColumn.setCellValueFactory(new PropertyValueFactory<>("NomCinema"));

        TableColumn<Projection, String> filmColumn = new TableColumn<>("Film");
        filmColumn.setCellValueFactory(new PropertyValueFactory<>("Film"));

        TableColumn<Projection, String> salleColumn = new TableColumn<>("Numéro Salle");
        salleColumn.setCellValueFactory(new PropertyValueFactory<>("idSalle"));

        TableColumn<Projection, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("Date"));

        TableColumn<Projection, String> horaireColumn = new TableColumn<>("Heure");
        horaireColumn.setCellValueFactory(new PropertyValueFactory<>("Horaire"));

        tableView.getColumns().addAll(cinemaColumn, salleColumn, filmColumn, dateColumn, horaireColumn);
        tableView.setItems(projections);
        tableView.setMaxWidth(1000);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Charger les projections depuis la base de données
        chargerProjectionsDepuisBase();

        // Ajouter les boutons d'action
        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(10));
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(buttonPlanifierProjection, buttonAnnulerProjection, buttonReplanifierProjection);

        // Ajouter les composants au layout
        setCenter(tableView);
        setBottom(buttons);
    }

    private void chargerProjectionsDepuisBase() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT cinema.NomCine, projection.NumSalle, projection.Date, projection.Heure, film.Titre " +
                     "FROM cinema, salle, film, projection " +
                     "WHERE salle.NumSalle = projection.NumSalle " +
                     "AND film.IdFilm = projection.IdFilm " +
                     "AND salle.NumCine = cinema.NumCine;")) {

            while (rs.next()) {
                String nomCine = rs.getString("NomCine");
                String film = rs.getString("Titre");
                int salle = rs.getInt("NumSalle");
                String horaire = rs.getString("Heure");
                String date = rs.getString("Date");

                projections.add(new Projection(salle, nomCine, film, horaire, date));
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les projections.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void PlanifierProjection() {
        data = FXCollections.observableArrayList();

        // Créer un Stage (fenêtre principale)
        Stage stage = new Stage();
        stage.setTitle("Ajouter une projection");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(20, 150, 10, 10));

        TableView<Projection> tableViewDialog = new TableView<>();

        // Colonnes de la table dans le Stage
        TableColumn<Projection, String> cinemaColumn = new TableColumn<>("Nom du cinema");
        cinemaColumn.setCellValueFactory(new PropertyValueFactory<>("NomCinema"));

        TableColumn<Projection, String> filmColumn = new TableColumn<>("Film");
        filmColumn.setCellValueFactory(new PropertyValueFactory<>("Film"));

        TableColumn<Projection, String> salleColumn = new TableColumn<>("Numéro Salle");
        salleColumn.setCellValueFactory(new PropertyValueFactory<>("idSalle"));

        TableColumn<Projection, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("Date"));

        TableColumn<Projection, String> horaireColumn = new TableColumn<>("Heure");
        horaireColumn.setCellValueFactory(new PropertyValueFactory<>("Horaire"));

        tableViewDialog.getColumns().addAll(cinemaColumn, salleColumn, filmColumn, dateColumn, horaireColumn);

        // Grille pour organiser les champs
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // ComboBox pour cinéma
        ComboBox<String> cinemaComboBox = new ComboBox<>();
        chargerCinemas(cinemaComboBox);

        // ComboBox pour salle
        ComboBox<String> salleComboBox = new ComboBox<>();
        salleComboBox.setDisable(true); // Les salles ne sont pas accessibles tant qu'un cinéma n'est pas sélectionné.

        // ComboBox pour film
        ComboBox<String> filmComboBox = new ComboBox<>();
        chargerFilms(filmComboBox);  // Charger les films au démarrage

        // DatePicker pour la date
        DatePicker datePicker = new DatePicker(LocalDate.now());

        // TextField pour l'heure de début
        TextField heureDebutField = new TextField();
        heureDebutField.setPromptText("HH:mm");

        // TextField pour la durée du film
        TextField dureeField = new TextField();
        dureeField.setPromptText("minutes");

        // Label pour l'heure de fin (calculée)
        Label heureFinLabel = new Label("Heure de fin : -");

        Label heureDebutErrorLabel = new Label();
        heureDebutErrorLabel.setStyle("-fx-text-fill: red;"); // Style pour les erreurs

        // Listener pour vérifier le format de l'heure en temps réel
        heureDebutField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{2}:\\d{2}")) { // Vérifie que le format est HH:mm
                heureDebutErrorLabel.setText("Format invalide (HH:mm attendu)"); // Affiche un message d'erreur
            } else {
                try {
                    LocalTime.parse(newValue, DateTimeFormatter.ofPattern("HH:mm")); // Vérifie si l'heure est valide
                    heureDebutErrorLabel.setText(""); // Pas d'erreur
                } catch (Exception e) {
                    heureDebutErrorLabel.setText("Heure invalide !"); // Par exemple : 25:00
                }
            }
        });

        // Calcul automatique de l'heure de fin
        dureeField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int duree = Integer.parseInt(newValue);
                if (!heureDebutField.getText().isEmpty()) {
                    LocalTime heureDebut = LocalTime.parse(heureDebutField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                    LocalTime heureFin = heureDebut.plusMinutes(duree);
                    heureFinLabel.setText("Heure de fin : " + heureFin.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            } catch (Exception e) {
                heureFinLabel.setText("Heure de fin : -");
            }
        });

        // Grille pour organiser les champs
        grid.add(new Label("Cinéma :"), 0, 0);
        grid.add(cinemaComboBox, 1, 0);
        grid.add(new Label("Salle :"), 0, 1);
        grid.add(salleComboBox, 1, 1);
        grid.add(new Label("Film :"), 0, 2);
        grid.add(filmComboBox, 1, 2);
        grid.add(new Label("Date :"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Heure de début :"), 0, 4);  // Première occurrence
        grid.add(heureDebutField, 1, 4);               // Première occurrence
        grid.add(heureDebutErrorLabel, 1, 5);          // Ajoutez l'étiquette d'erreur sous le champ
        grid.add(new Label("Durée du film :"), 0, 5);
        grid.add(dureeField, 1, 5);
        grid.add(heureFinLabel, 1, 6);

        // Ajouter un Listener pour charger les projections de la salle sélectionnée
        salleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                chargerProjectionsParSalle(newValue, tableViewDialog);
            }
        });

        // Ajouter un Listener pour charger les salles en fonction du cinéma sélectionné
        cinemaComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                chargerSalles(newValue, salleComboBox);
            }
        });

        filmComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String duree = null;
                try {
                    duree = recupererDuree(filmComboBox.getValue());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                dureeField.setText(duree);
            }
        });

        vbox.getChildren().addAll(grid, tableViewDialog);

        // Créer un bouton "Ajouter" et gérer son action
        Button addButton = new Button("Ajouter");
        addButton.setOnAction(event -> {
            // Récupérer les valeurs des champs
            String cinema = cinemaComboBox.getValue();
            String salle = salleComboBox.getValue();
            String film = filmComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String heureDebut = heureDebutField.getText();
            String duree = dureeField.getText();

            // Ajouter la projection à la base de données
            String insertQuery = "INSERT INTO projection (NumSalle, IdFilm, Date, Heure) "
                    + "VALUES (?, (SELECT IdFilm FROM film WHERE Titre = ?), ?, ?)";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
                 PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

                // Remplir les paramètres de l'insertion
                stmt.setInt(1, Integer.parseInt(salle)); // Numéro de la salle
                stmt.setString(2, film);  // Titre du film (on suppose que c'est unique)
                stmt.setDate(3, Date.valueOf(date)); // Date de la projection
                stmt.setString(4, heureDebut); // Heure de début

                // Exécuter l'insertion
                stmt.executeUpdate();

                // Afficher un message de succès ou mettre à jour l'interface
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Projection ajoutée");
                alert.setHeaderText("La projection a été ajoutée avec succès.");
                alert.showAndWait();
                stage.close();
                tableView.refresh();


            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Planification impossible");
                alert.setHeaderText("Présence de chevauchement entre les projections");
                alert.showAndWait();
            }
        });

        // Créer un bouton "Annuler"
        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(event -> stage.close()); // Fermer le stage

        // Ajouter les boutons au layout
        HBox buttonBox = new HBox(10, addButton, cancelButton);
        vbox.getChildren().add(buttonBox);

        // Mettre en place la scène et afficher le stage
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }





    // Fonction pour convertir la durée "HH:mm:ss" en minutes
    private int convertDureeToMinutes(String duree) {
        String[] parts = duree.split(":");
        int heures = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        // Les secondes peuvent être ignorées si vous n'en avez pas besoin
        return heures * 60 + minutes;
    }



    
    private void chargerFilms(ComboBox<String> filmComboBox) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Titre FROM film")) {

            while (rs.next()) {
                String film = rs.getString("Titre");
                filmComboBox.getItems().add(film);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les films.");
            e.printStackTrace();
        }
    }
    
    private boolean isHeureValide(String heure) {
        try {
            LocalTime.parse(heure, DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    
    private void chargerCinemas(ComboBox<String> cinemaComboBox) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT NomCine FROM cinema")) {

            while (rs.next()) {
                String cinema = rs.getString("NomCine");
                cinemaComboBox.getItems().add(cinema);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les cinémas.");
            e.printStackTrace();
        }
    }

    private void chargerSalles(String cinema, ComboBox<String> salleComboBox) {
        salleComboBox.getItems().clear();
        salleComboBox.setDisable(false);

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
             PreparedStatement stmt = con.prepareStatement("SELECT NumSalle FROM salle WHERE NumCine = (SELECT NumCine FROM cinema WHERE NomCine = ?)")) {

            stmt.setString(1, cinema);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int salleNum = rs.getInt("NumSalle");
                salleComboBox.getItems().add(String.valueOf(salleNum));
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les salles.");
            e.printStackTrace();
        }
    }
    
    private String recupererDuree(String titre) throws SQLException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = null;

        try {
            // Connexion à la base de données
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");

            // Préparation de la requête SQL
            String query = "SELECT Duree FROM film WHERE Titre = ?";
            stmt = con.prepareStatement(query);
            stmt.setString(1, titre);

            // Exécution de la requête
            rs = stmt.executeQuery();

            // Vérification si un résultat a été retourné
            if (rs.next()) {
                // Récupération de la durée
                return rs.getString("Duree");
            } else {
                // Si aucun film n'a été trouvé
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Affiche l'exception dans la console
            throw new SQLException("Erreur lors de la récupération de la durée", e);
        } finally {
            // Fermeture des ressources
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Affiche l'exception si une ressource ne peut pas être fermée
            }
        }
    }


    private void chargerProjectionsParSalle(String salle, TableView<Projection> tableViewDialog) {
        ObservableList<Projection> projectionsSalle = FXCollections.observableArrayList();

        // Charger les projections en fonction de la salle sélectionnée
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
             PreparedStatement stmt = con.prepareStatement("SELECT cinema.NomCine, projection.NumSalle, projection.Date, projection.Heure, film.Titre " +
                     "FROM cinema, salle, film, projection " +
                     "WHERE salle.NumSalle = projection.NumSalle " +
                     "AND film.IdFilm = projection.IdFilm " +
                     "AND salle.NumCine = cinema.NumCine " +
                     "AND projection.NumSalle = ?")) {

            stmt.setInt(1, Integer.parseInt(salle)); // Extract number from salle name

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String nomCine = rs.getString("NomCine");
                String film = rs.getString("Titre");
                int salleNum = rs.getInt("NumSalle");
                String horaire = rs.getString("Heure");
                String date = rs.getString("Date");

                projectionsSalle.add(new Projection(salleNum, nomCine, film, horaire, date));
            }

            
            tableViewDialog.setItems(projectionsSalle);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les projections de cette salle.");
            e.printStackTrace();
        }
    }
}

