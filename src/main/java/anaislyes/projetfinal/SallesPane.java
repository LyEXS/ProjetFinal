package anaislyes.projetfinal;

import Objects.Salle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.sql.*;

public class SallesPane extends BorderPane {
    private TableView<Salle> tableView; // Table pour afficher les salles
    private ObservableList<Salle> sallesList; // Données des salles
   
    private Button addButton, editButton, deleteButton; // Boutons d'action

    public SallesPane() throws SQLException {
        // Initialiser les données
        sallesList = FXCollections.observableArrayList();
        chargerDonneesDepuisBase();

        // Créer le ComboBox des cinémas
        

        // Créer la table pour afficher les salles
        tableView = new TableView<>();
        TableColumn<Salle, String> cinemaColumn = new TableColumn<>("Cinéma");
        cinemaColumn.setCellValueFactory(data -> data.getValue().cinemaProperty());

        TableColumn<Salle, String> numSalleColumn = new TableColumn<>("Numéro de Salle");
        numSalleColumn.setCellValueFactory(data -> data.getValue().numSalleProperty());

        TableColumn<Salle, Integer> capacityColumn = new TableColumn<>("Capacité");
        capacityColumn.setCellValueFactory(data -> data.getValue().capaciteProperty().asObject());

        tableView.getColumns().addAll(cinemaColumn, numSalleColumn, capacityColumn);
        tableView.setItems(sallesList);
        tableView.setMaxWidth(1000);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Formulaire pour ajouter/modifier des salles
        HBox form = new HBox(10);
        form.setPadding(new Insets(10));
        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacité");
        
        addButton = new Button("Ajouter");
        editButton = new Button("Modifier");
        deleteButton = new Button("Supprimer");
        
        form.getChildren().addAll(addButton, editButton, deleteButton);
        form.setAlignment(Pos.CENTER);
        setCenter(tableView);
        setBottom(form);

        // Actions des boutons
        addButton.setOnAction(e -> {
			try {
				afficherDialogSalle(null);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
        editButton.setOnAction(e -> {
            Salle selectedSalle = tableView.getSelectionModel().getSelectedItem();
            if (selectedSalle != null) {
                try {
					afficherDialogSalle(selectedSalle);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            } else {
                showAlert("Erreur", "Veuillez sélectionner une salle à modifier.");
            }
        });
        deleteButton.setOnAction(e -> supprimerSalle());
    }

    private void afficherDialogSalle(Salle salleExistant) throws SQLException {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(salleExistant == null ? "Ajouter une Salle" : "Modifier une Salle");
        dialog.setHeaderText(salleExistant == null ? "Ajoutez une nouvelle salle." : "Modifiez les informations de la salle.");

        // Boutons du Dialog
        ButtonType validerButtonType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(validerButtonType, ButtonType.CANCEL);

        // Champs de formulaire
        ComboBox<String> cinemaComboBoxDialog = new ComboBox<>(FXCollections.observableArrayList());
        cinemaComboBoxDialog.setPromptText("Choisir un Cinéma");
        remplirComboBox(cinemaComboBoxDialog.getItems());
        
        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacité");

        if (salleExistant != null) {
            cinemaComboBoxDialog.setValue(salleExistant.getCinema());
            capacityField.setText(String.valueOf(salleExistant.getCapacite()));
        }

        VBox content = new VBox(10, new Label("Cinéma :"), cinemaComboBoxDialog, new Label("Capacité :"), capacityField);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        // Gestion du résultat
        dialog.setResultConverter(button -> {
            if (button == validerButtonType) {
                String cinema = cinemaComboBoxDialog.getValue();
                String capaciteText = capacityField.getText();
                if (cinema == null || capaciteText.isEmpty()) {
                    showAlert("Erreur", "Tous les champs doivent être remplis !");
                    return null;
                }

                try {
                    int capacite = Integer.parseInt(capaciteText);
                    if (salleExistant == null) {
                        ajouterSalle(cinema, capacite);
                    } else {
                        modifierSalle(salleExistant, cinema, capacite);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "La capacité doit être un nombre !");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void ajouterSalle(String cinema, int capacite) {
        try {
        	Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
        	PreparedStatement ps1 = con.prepareStatement("SELECT NumCine FROM cinema WHERE NomCine = ?");
        	ps1.setString(1, cinema);
        	ResultSet rs1 = ps1.executeQuery();
        	rs1.next();
        	int numCine = rs1.getInt(1);

        	// Insérer une nouvelle salle
        	PreparedStatement ps3 = con.prepareStatement("INSERT INTO salle (Capacite, NumCine) VALUES (?, ?)", 
        	                                               Statement.RETURN_GENERATED_KEYS);  // Spécifiez qu'on veut récupérer la clé générée
        	ps3.setInt(1, capacite);
        	ps3.setInt(2, numCine);
        	ps3.executeUpdate();

        	// Récupérer le NumSalle généré
        	ResultSet generatedKeys = ps3.getGeneratedKeys();
        	if (generatedKeys.next()) {
        	    int numSalle = generatedKeys.getInt(1);  // Récupère le NumSalle généré
        	    Salle nouvelleSalle = new Salle(cinema, String.valueOf(numSalle), capacite);
        	    sallesList.add(nouvelleSalle);
        	}

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout de la salle.");
        }
    }

    private void modifierSalle(Salle salleExistant, String cinema, int capacite) {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
            PreparedStatement ps = con.prepareStatement("UPDATE salle SET Capacite = ? WHERE NumSalle = ?");
            ps.setInt(1, capacite);
            ps.setInt(2, Integer.parseInt(salleExistant.getNumSalle()));
            ps.executeUpdate();

            salleExistant.setCapacite(capacite);
            tableView.refresh();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification de la salle.");
        }
    }

    private void supprimerSalle() {
        Salle selectedSalle = tableView.getSelectionModel().getSelectedItem();
        if (selectedSalle != null) {
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
                PreparedStatement ps = con.prepareStatement("DELETE FROM salle WHERE NumSalle = ?");
                ps.setInt(1, Integer.parseInt(selectedSalle.getNumSalle()));
                ps.executeUpdate();
                
                sallesList.remove(selectedSalle);
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer la salle.");
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner une salle à supprimer !");
        }
    }

    private void remplirComboBox(ObservableList<String> list) throws SQLException {
        list.clear();
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
        PreparedStatement ps = con.prepareStatement("SELECT NomCine FROM cinema");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(rs.getString("NomCine"));
        }
    }

    private void chargerDonneesDepuisBase() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT c.NomCine, s.NumSalle, s.Capacite FROM salle s JOIN cinema c ON s.NumCine = c.NumCine");

            while (rs.next()) {
                String nomCinema = rs.getString("NomCine");
                int numSalle = rs.getInt("NumSalle");
                int capacite = rs.getInt("Capacite");

                sallesList.add(new Salle(nomCinema, String.valueOf(numSalle), capacite));
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des données.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
