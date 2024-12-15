package anaislyes.projetfinal;

import Objects.Projection;
import com.dlsc.gemsfx.TimePicker;
import com.dlsc.gemsfx.daterange.DateRange;
import com.dlsc.gemsfx.daterange.DateRangePicker;
import com.itextpdf.testutils.ITextTest;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.jfoenix.controls.JFXButton;
import com.mysql.cj.xdevapi.Table;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.imageio.ImageIO;

import javafx.scene.paint.Color;


public class ProjectionsPane extends BorderPane {
    private TableView<Projection> tableView;
    ObservableList<Projection> data;
    
	String RessourcePath = "/resources/";
	String StylePath = "Styles/";
	//String StylePath = "";
	//String RessourcePath ="/";
   

    public ProjectionsPane() {
        JFXButton buttonPlanifierProjection = new JFXButton("Planifier Projections");
        JFXButton buttonAnnulerProjection = new JFXButton("Annuler Projection");
        JFXButton buttonReplanifierProjection = new JFXButton("Replanifier Projection");
        JFXButton buttonArchive = new JFXButton("Archive des projections");
        
        
        
        
        TextField nomcinemaField = new TextField();
        nomcinemaField.setPromptText("Nom du Cinéma");
        TextField nomfilmField = new TextField();
        nomfilmField.setPromptText("Nom du Film");
        TextField NumSalleField = new TextField();
        NumSalleField.setPromptText("Numéro de Salle");
        DatePicker date = new DatePicker();
        date.setPromptText("Date ");
       
        JFXButton buttonRechercher = new JFXButton("Rechercher");
        buttonRechercher.setPrefWidth(150);
        buttonRechercher.setPrefHeight(30);
        buttonRechercher.setPadding(new Insets(10));
        VBox vbox = new VBox(10,nomcinemaField,nomfilmField,NumSalleField,date,buttonRechercher);
       

        
        buttonRechercher.setOnAction(e -> {
            ObservableList<Projection> list = FXCollections.observableArrayList();
            
            // Récupérer les valeurs des champs
            String cinemaName = nomcinemaField.getText();
            String filmName = nomfilmField.getText();
            String roomNumber = NumSalleField.getText();
            LocalDate selectedDate = date.getValue();

            // Créer la requête SQL avec des paramètres préparés
            StringBuilder query = new StringBuilder(
                "SELECT cinema.NomCine, film.Titre, projection.NumSalle, projection.heure,projection.Date " +
                "FROM projection " +
                "JOIN film ON film.idfilm = projection.idfilm " +
                
                "JOIN salle ON salle.numsalle = projection.numsalle " +
                "JOIN cinema ON cinema.numcine = salle.numcine " +
                "WHERE 1=1");

            // Ajouter les conditions en fonction des champs remplis
            if (!cinemaName.isEmpty()) {
                query.append(" AND cinema.NomCine LIKE ?");
            }
            if (!filmName.isEmpty()) {
                query.append(" AND film.Titre LIKE ?");
            }
            if (!roomNumber.isEmpty()) {
                query.append(" AND projection.NumSalle LIKE ?");
            }
            if (selectedDate != null) {
                query.append(" AND projection.Date = ?");
            }
            

            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "")) {
                PreparedStatement st = con.prepareStatement(query.toString());

                // Ajouter les paramètres aux requêtes préparées
                int index = 1;

                if (!cinemaName.isEmpty()) {
                    st.setString(index++, "%" + cinemaName + "%");
                }
                if (!filmName.isEmpty()) {
                    st.setString(index++, "%" + filmName + "%");
                }
                if (!roomNumber.isEmpty()) {
                    st.setString(index++, "%" + roomNumber + "%");
                }
                if (selectedDate != null) {
                    st.setDate(index++, java.sql.Date.valueOf(selectedDate));
                }

                ResultSet rs = st.executeQuery();
                
                while (rs.next()) {
                    Projection p = new Projection(
                        rs.getInt("NumSalle"),
                        rs.getString("NomCine"),
                        rs.getString("Titre"),
                        rs.getString("Heure"),
                        rs.getString("Date")
                    );
                    list.add(p);
                }

                // Fermer les ressources
                con.close();
                st.close();
            } catch (Exception e2) {
                e2.printStackTrace(); // Afficher l'exception pour le débogage
            }
            if(cinemaName.isEmpty()&&filmName.isEmpty()&&roomNumber.isEmpty()&&selectedDate == null)
            	tableView.setItems(data);

            // Mettre à jour la table avec les résultats
            tableView.setItems(list);
        });
        
        buttonArchive.setOnAction(e -> {
            Stage archiveStage = new Stage();
            VBox v = new VBox();
            archiveStage.setTitle("Projections archivées");

            TableView<Projection> table = new TableView<>();
            TableColumn<Projection, String> colCinema = new TableColumn<>("Cinéma");
            colCinema.setCellValueFactory(new PropertyValueFactory<>("nomCinema"));

            TableColumn<Projection, Integer> colSalle = new TableColumn<>("Salle");
            colSalle.setCellValueFactory(new PropertyValueFactory<>("idSalle"));

            TableColumn<Projection, LocalDate> colDate = new TableColumn<>("Date");
            colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

            TableColumn<Projection, LocalTime> colHeure = new TableColumn<>("Heure");
            colHeure.setCellValueFactory(new PropertyValueFactory<>("horaire"));

            TableColumn<Projection, String> colTitre = new TableColumn<>("Titre");
            colTitre.setCellValueFactory(new PropertyValueFactory<>("film"));

            table.getColumns().addAll(colCinema, colSalle, colTitre, colDate, colHeure);
            
            JFXButton imprimer = new JFXButton("Imprimer");
            imprimer.setOnAction(e2->{
            	ObservableList<Projection> anciennesProjections = FXCollections.observableArrayList();
            	anciennesProjections = getAnciennesProjections();
            	generatePDF(anciennesProjections);
            	archiveStage.close();
            	Alert alert = new Alert(AlertType.INFORMATION,"Veulliez consulter le dossier des archives pour y trouver le fichier.");
            	alert.showAndWait();
            	
            	
            	
            });
            
            

            // Charger les données dans la table
            table.setItems(getAnciennesProjections());
            

            // Affichage
            VBox vbox2 = new VBox(table,imprimer);
            Scene scene = new Scene(vbox2, 600, 400);
            archiveStage.setScene(scene);
            archiveStage.show();
        });



        
        
        
        buttonAnnulerProjection.setOnAction
        (e -> {
        	
            AnnulerProjection(tableView);
        });

        buttonPlanifierProjection.setOnAction(e -> {
        	PlanifierProjection();
        });
        buttonReplanifierProjection.setOnAction(e -> {
            ReplanifierProjection(tableView);
        });

        ObservableList<Projection>  projections = FXCollections.observableArrayList();
        tableView = new TableView<>();
        ContextMenu contextMenu = new ContextMenu();
        MenuItem printPosterItem = new MenuItem("Imprimer Affiche");
        contextMenu.getItems().add(printPosterItem);
        tableView.setContextMenu(contextMenu);
        
        
        printPosterItem.setOnAction(event -> {
            Projection selectedProjection = tableView.getSelectionModel().getSelectedItem();
           creerAffichePDF(selectedProjection);
        });

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
        projections = chargerProjectionsDepuisBase();
        tableView.getColumns().addAll(cinemaColumn, salleColumn, filmColumn, dateColumn, horaireColumn);
        tableView.setItems(projections);
        tableView.setMaxWidth(1000);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Charger les projections depuis la base de données
        

        // Ajouter les boutons d'action
        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(10));
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(buttonPlanifierProjection, buttonAnnulerProjection, buttonReplanifierProjection,buttonArchive);

        // Ajouter les composants au layout
        setCenter(tableView);
        setBottom(buttons);
        setLeft(vbox);
    }

    private ObservableList<Projection> chargerProjectionsDepuisBase( ) {
    	ObservableList<Projection> projection = FXCollections.observableArrayList();
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
             Statement stmt = con.createStatement();
        		ResultSet rs = stmt.executeQuery(
        			    "SELECT cinema.NomCine, projection.NumSalle, projection.Date, projection.Heure, film.Titre " +
        			    "FROM cinema, salle, film, projection " +
        			    "WHERE salle.NumSalle = projection.NumSalle " +
        			    "AND film.IdFilm = projection.IdFilm " +
        			    "AND salle.NumCine = cinema.NumCine " +
        			    "AND projection.Date >= CURDATE();"
        			))
 {

            while (rs.next()) {
                String nomCine = rs.getString("NomCine");
                String film = rs.getString("Titre");
                int salle = rs.getInt("NumSalle");
                String horaire = rs.getString("Heure");
                String date = rs.getString("Date");

                projection.add(new Projection(salle, nomCine, film, horaire, date));
            }
          

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les projections.");
            e.printStackTrace();
        }
		return projection;
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
        
        TimePicker timepicker = new TimePicker();

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
       

        // Calcul automatique de l'heure de fin
        dureeField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int duree = Integer.parseInt(newValue);
                if (true) {
                    LocalTime heureDebut = timepicker.getTime();
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
        grid.add(timepicker, 1, 4);               // Première occurrence
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
        
        timepicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println("Temps sélectionné : " + newValue);
            } else {
                System.out.println("Aucune valeur sélectionnée !");
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
        Tooltip tooltip = new Tooltip("Assurez-vous que l'heure de début ne chevauche pas les projections existantes.");

     // Ajouter le Tooltip au bouton "Ajouter"
     Tooltip.install(addButton, tooltip);
        addButton.setOnAction(event -> {
            // Récupérer les valeurs des champs
            String cinema = cinemaComboBox.getValue();
            String salle = salleComboBox.getValue();
            String film = filmComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String heureDebut = timepicker.getTime().toString();
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
            	String errorMessage = e.getMessage();
            	Alert alert = new Alert(Alert.AlertType.ERROR);
            	if(errorMessage.contains("chevauchement")) {
            		System.out.println("a"+errorMessage);
            		alert.setHeaderText("Présence de chevauchement entre les projections");}
            	else {
            		alert.setHeaderText("Une ou plusieurs données sont invalides");
            		System.out.println("b"+errorMessage);
            	}
            		
            	alert.setTitle("Planification impossible");
                
                alert.showAndWait();
                
                
            }catch(NumberFormatException e) {
            		Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Ajout impossible ");
                alert.setTitle("Une ou plusieurs données sont invalides");
                
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
    
    public void ReplanifierProjection(TableView<Projection> tableViewDialog) {
        Projection projectionSelectionnee = tableViewDialog.getSelectionModel().getSelectedItem();
        if (projectionSelectionnee == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune projection sélectionnée");
            alert.setHeaderText("Veuillez sélectionner une projection à replanifier.");
            alert.showAndWait();
            return;
        }

        // Créer un Stage pour modifier la projection
        Stage stage = new Stage();
        stage.setTitle("Replanifier une projection");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20, 150, 10, 10));

        // Formulaire pour la replanification
        ComboBox<String> cinemaComboBox = new ComboBox<>();
        chargerCinemas(cinemaComboBox);

        ComboBox<String> salleComboBox = new ComboBox<>();
        

        ComboBox<String> filmComboBox = new ComboBox<>();
        chargerFilms(filmComboBox);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(projectionSelectionnee.getDate(), formatter);

        DatePicker datePicker = new DatePicker(LocalDate.parse(projectionSelectionnee.getDate()));



        TimePicker timePicker = new TimePicker();
        TextField dureeField = new TextField();
        dureeField.setEditable(false);
        
        cinemaComboBox.setValue(projectionSelectionnee.getNomCinema());
        salleComboBox.setValue(String.valueOf(projectionSelectionnee.getIdSalle()));
        filmComboBox.setValue(projectionSelectionnee.getFilm());
        
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

        // Charger les salles en fonction du cinéma sélectionné
        cinemaComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                chargerSalles(newValue, salleComboBox);
            }
        });
        
        

        // Formulaire dans le VBox
        vbox.getChildren().addAll(
            new Label("Cinéma :"), cinemaComboBox,
            new Label("Salle :"), salleComboBox,
            new Label("Film :"), filmComboBox,
            new Label("Date :"), datePicker,
            new Label("Heure :"), timePicker,
            new Label("Durée :"), dureeField
        );

        // Bouton pour sauvegarder les modifications
        Button saveButton = new Button("Sauvegarder");
        saveButton.setOnAction(event -> {
            String salle = salleComboBox.getValue();
            String film = filmComboBox.getValue();
            LocalDate date = datePicker.getValue();
            LocalTime heure = timePicker.getTime();

            String updateQuery = "UPDATE projection SET NumSalle = ?, IdFilm = (SELECT IdFilm FROM film WHERE Titre = ?), Date = ?, Heure = ? " +
                                 "WHERE NumSalle = ? AND IdFilm = (SELECT IdFilm FROM film WHERE Titre = ?) AND Date = ? AND Heure = ?";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
                 PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

                // Nouveaux paramètres
                stmt.setInt(1, Integer.parseInt(salle));
                stmt.setString(2, film);
                stmt.setDate(3, Date.valueOf(date));
                stmt.setTime(4, Time.valueOf(heure));

                // Anciens paramètres (pour localiser la projection)
                stmt.setInt(5, projectionSelectionnee.getIdSalle());
                stmt.setString(6, projectionSelectionnee.getFilm());
                stmt.setDate(7, Date.valueOf(projectionSelectionnee.getDate()));
                stmt.setTime(8, Time.valueOf(projectionSelectionnee.getHoraire()));

                stmt.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Replanification réussie");
                alert.setHeaderText("La projection a été mise à jour avec succès.");
                alert.showAndWait();

                stage.close();
                tableViewDialog.refresh();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(e.toString());
                alert.showAndWait();
            }
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(event -> stage.close());

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        vbox.getChildren().add(buttonBox);

        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }

    public void AnnulerProjection(TableView<Projection> tableViewDialog) {
        Projection projectionSelectionnee = tableViewDialog.getSelectionModel().getSelectedItem();
        if (projectionSelectionnee == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune projection sélectionnée");
            alert.setHeaderText("Veuillez sélectionner une projection à annuler.");
            alert.showAndWait();
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation d'annulation");
        confirmationAlert.setHeaderText("Êtes-vous sûr de vouloir annuler cette projection ?");
        confirmationAlert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deleteQuery = "DELETE FROM projection WHERE NumSalle = ? AND IdFilm = (SELECT IdFilm FROM film WHERE Titre = ?) AND Date = ? AND Heure = ?";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
                 PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

                stmt.setInt(1, projectionSelectionnee.getIdSalle());
                stmt.setString(2, projectionSelectionnee.getFilm());
                stmt.setDate(3, Date.valueOf(projectionSelectionnee.getDate()));
                stmt.setTime(4, Time.valueOf(projectionSelectionnee.getHoraire()));

                stmt.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Projection annulée");
                alert.setHeaderText("La projection a été annulée avec succès.");
                alert.showAndWait();

                tableViewDialog.getItems().remove(projectionSelectionnee); // Met à jour la vue
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Une erreur est survenue lors de l'annulation.");
                alert.showAndWait();
            }
        }
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
    
    
    


    public static String formaterDate(String dateString) {
        // Définir le format d'entrée (yyyy-MM-dd)
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Parse la date
        LocalDate date = LocalDate.parse(dateString, inputFormatter);

        // Définir le format de sortie (d MMM yyyy)
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d MMM yyyy");

        // Retourner la date formatée
        return date.format(outputFormatter);
    }
    public static String formaterHeure(String heureString) {
        // Définir le format d'entrée (HH:mm:ss)
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Analyser la chaîne en une heure
        LocalTime heure = LocalTime.parse(heureString, inputFormatter);

        // Définir le format de sortie (HH'h'mm)
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("H'h'mm");

        // Retourner l'heure formatée
        return heure.format(outputFormatter);
    }



    
    private Image recupererImageFilm(String filmTitre) {
        Image image = null;
        String query = "SELECT image FROM film WHERE Titre = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, filmTitre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Récupérer le BLOB comme tableau de bytes
                Blob blob = rs.getBlob("image");
                if (blob != null) {
                    InputStream inputStream = blob.getBinaryStream();
                    image = new Image(inputStream);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return image;
    }
    
  
    
  

    private void enregistrerFenetreEnImage(Stage stage) {
        // Obtenir la scène
        Scene scene = stage.getScene();

        // Créer une image en mémoire à partir de la scène
        WritableImage image = new WritableImage((int)scene.getWidth(), (int)scene.getHeight());
        scene.snapshot(image); // Prendre une capture d'écran de la scène

        // Définir le chemin du répertoire de destination sur le bureau
        String desktopPath = "C:\\Users\\lyesb\\Desktop";
        File directory = new File(desktopPath);
        
        // Créer le fichier à partir du chemin
        if (!directory.exists()) {
            directory.mkdir();  // Crée le répertoire si nécessaire
        }
        
        // Spécifier le fichier image
        File file = new File(directory, "fenetre_projection.png");

        try {
            // Convertir l'image JavaFX en image Swing pour pouvoir l'enregistrer
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);
            System.out.println("L'image a été sauvegardée sous : " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ObservableList<Projection> getAnciennesProjections() {
        ObservableList<Projection> projections = FXCollections.observableArrayList();
        String query = "SELECT cinema.NomCine, projection.NumSalle, projection.Date, projection.Heure, film.Titre " +
                       "FROM cinema, salle, film, projection " +
                       "WHERE salle.NumSalle = projection.NumSalle " +
                       "AND film.IdFilm = projection.IdFilm " +
                       "AND salle.NumCine = cinema.NumCine " +
                       "AND projection.Date < CURDATE()";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "")){
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);

        	while (rs.next()) {
                String nomCine = rs.getString("NomCine");
                String film = rs.getString("Titre");
                int salle = rs.getInt("NumSalle");
                String horaire = rs.getString("Heure");
                String date = rs.getString("Date");

                projections.add(new Projection(salle, nomCine, film, horaire, date));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projections;
    }
    
    public void generatePDF(ObservableList<Projection> projections) {
    	
    	
    	String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    	String pdfPath = "C:\\Users\\lyesb\\Documents\\ArchivesCinema\\Archive-" + formattedDate + ".pdf";


        // Créer un document iText
        Document document = new Document();

        try {
            // Créer un PdfWriter pour écrire dans le fichier
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfPath));

            // Ouvrir le document pour y ajouter du contenu
            document.open();

            // Définir la police pour le titre
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Archives des Projections", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Créer une table avec 5 colonnes
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(20);

            // Définir les en-têtes de la table avec un fond coloré
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            PdfPCell headerCell;
            headerCell = new PdfPCell(new Phrase("Cinéma", headerFont));
            headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(headerCell);
            
            headerCell = new PdfPCell(new Phrase("Salle", headerFont));
            headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(headerCell);
            
            headerCell = new PdfPCell(new Phrase("Date", headerFont));
            headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(headerCell);
            
            headerCell = new PdfPCell(new Phrase("Heure", headerFont));
            headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(headerCell);
            
            headerCell = new PdfPCell(new Phrase("Titre", headerFont));
            headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(headerCell);

            // Ajouter les données des projections
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);
            for (Projection projection : projections) {
                table.addCell(new PdfPCell(new Phrase(projection.getNomCinema(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(projection.getIdSalle()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(projection.getDate().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(projection.getHoraire().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(projection.getFilm(), cellFont)));
            }

            // Ajouter la table au document
            document.add(table);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Fermer le document
            document.close();
        }
    }
    
    public void creerAffichePDF(Projection projection) {
        // Définir le chemin du fichier PDF
        String desktopPath = "C:\\Users\\lyesb\\Desktop\\Affiches";
        File directory = new File(desktopPath);

        // Créer le répertoire si nécessaire
        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(directory, "affiche_projection"+projection.getFilm()+".pdf");

        // Créer un document PDF
        com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(PageSize.A4);
        Document document = new Document(pageSize); // Orientation portrait par défaut
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Ajouter l'image de fond
            com.itextpdf.text.Image backgroundImage = com.itextpdf.text.Image.getInstance(getClass().getResource(RessourcePath+"images/backgroundAffiche3.jpg"));
            backgroundImage.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            backgroundImage.setAbsolutePosition(0, 0); // Placer l'image à la position (0,0)

            // Ajouter l'image de fond au document
            PdfContentByte canvas = writer.getDirectContentUnder();  // Ajout en arrière-plan
            canvas.addImage(backgroundImage);

            // Ajouter l'image du film en haut de la page
            Image img = recupererImageFilm(projection.getFilm());
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(img, null);

            ByteArrayOutputStream out  = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", out);
            com.itextpdf.text.Image pdfImage = com.itextpdf.text.Image.getInstance(out.toByteArray());
            
            // Vérifier si l'image est valide
            if (img != null) {
                // Redimensionner l'image pour qu'elle prenne une portion du haut de la page
                float width = PageSize.A4.getWidth() - 100;
                float height = PageSize.A4.getHeight() - 200;
                pdfImage.scaleAbsolute(width, height);
                
                float x = (PageSize.A4.getWidth() - (PageSize.A4.getWidth() - 100)) / 2;
                float y = 200 - x + 10; 
                pdfImage.setAbsolutePosition(x, y);

                // Ajouter l'image au document (en haut de la page)
                document.add(pdfImage);
            }

            // Créer le texte de la projection
            String texteProjection = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + String.format(
                " LE FILM %s SERA DIFFUSE AU CINEMA %s DANS LA SALLE %s, LE %s A %s.".toUpperCase(),
                projection.getFilm(),
                projection.getNomCinema(),
                projection.getIdSalle(),
                projection.getDate(),
                formaterHeure(projection.getHoraire())
            );

            // Ajouter du texte en bas de la page (sous l'image)
            Font infoFont = new Font(Font.FontFamily.HELVETICA, 23, Font.BOLD, BaseColor.WHITE); // Texte blanc
            Paragraph infoText = new Paragraph(texteProjection, infoFont);
            infoText.setAlignment(Element.ALIGN_CENTER); // Centrer le texte
            infoText.setSpacingBefore(20); // Espacement pour éviter le chevauchement avec l'image
            infoText.setSpacingAfter(20);  // Un peu d'espace après le texte

            // Ajouter la phrase au document (en bas)
            document.add(infoText);

            // Message de confirmation
            System.out.println("Le fichier PDF a été sauvegardé sous : " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }


   





}

