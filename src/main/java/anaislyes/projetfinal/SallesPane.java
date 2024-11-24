package anaislyes.projetfinal;

import Objects.Cinema;
import Objects.Salle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SallesPane extends BorderPane {
    private TableView<Salle> tableView; // Table pour afficher les salles
    private ObservableList<Salle> sallesList; // Données des salles
    private TextField capacityField;
    private ComboBox<String> Cinema; // Champs pour ajouter/modifier
    private Button addButton, editButton, deleteButton; // Boutons d'action
    private Map<String, Integer> cinemaCounters; // Compteur pour chaque cinéma

    public SallesPane() throws SQLException {
        // Initialiser les données
        sallesList = FXCollections.observableArrayList();
        chargerDonneesDepuisBase();
        cinemaCounters = new HashMap<>();

        ObservableList<String> Cinemas = FXCollections.observableArrayList();
        remplirComboBox(Cinemas);
        Cinema = new ComboBox<>(Cinemas);
        Cinema.setPromptText("Choisissez le Cinéma");

        // Créer la table
        tableView = new TableView<>();

        TableColumn<Salle, String> cinemaColumn = new TableColumn<>("Cinéma");
        cinemaColumn.setCellValueFactory(data -> data.getValue().cinemaProperty());

        TableColumn<Salle, String> numSalleColumn = new TableColumn<>("Numéro de Salle");
        numSalleColumn.setCellValueFactory(data -> data.getValue().numSalleProperty());

        TableColumn<Salle, Integer> capacityColumn = new TableColumn<>("Capacité");
        capacityColumn.setCellValueFactory(data -> data.getValue().capaciteProperty().asObject());

        tableView.getColumns().addAll(cinemaColumn, numSalleColumn, capacityColumn);
        tableView.setItems(sallesList);

        // Ajouter un formulaire pour ajouter/modifier des salles
        HBox form = new HBox(10);
        form.setPadding(new Insets(10));

        capacityField = new TextField();
        capacityField.setPromptText("Capacité");
        addButton = new Button("Ajouter");
        editButton = new Button("Modifier");
        deleteButton = new Button("Supprimer");

        form.getChildren().addAll(new Label("Cinéma :"), Cinema, new Label("Capacité :"), capacityField, addButton, editButton, deleteButton);

        // Positionner les éléments dans le BorderPane
        setTop(new Text("Gestion des Salles"));
        setCenter(tableView);
        setBottom(form);

        // Ajouter les événements
        addButton.setOnAction(e -> ajouterSalle());
        editButton.setOnAction(e -> modifierSalle());
        deleteButton.setOnAction(e -> supprimerSalle());
    }

    private void ajouterSalle() {
        String cinema = Cinema.getSelectionModel().getSelectedItem();
        String capaciteText = capacityField.getText();

        if (cinema == null || cinema.isEmpty() || capaciteText.isEmpty()) {
            showAlert("Erreur", "Tous les champs sont obligatoires !");
            return;
        }

        try {
            int capacite = Integer.parseInt(capaciteText);

          ;

            // Créer une nouvelle salle et l'ajouter à la liste


            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
            PreparedStatement ps1 = con.prepareStatement("select NumCine from cinema where NomCine = ?");
            ps1.setString(1, Cinema.getSelectionModel().getSelectedItem());

            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            int NumCine = rs1.getInt(1);

            PreparedStatement ps2 = con.prepareStatement("select count(*) from salle where NumCine = ?");
            ps2.setInt(1, NumCine);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            int NbrSalles = rs2.getInt(1);

            int NumSalle = Integer.parseInt(String.valueOf(NumCine)+NbrSalles)+1;

            PreparedStatement ps = con.prepareStatement("INSERT INTO salle VALUES (?,?,?)");
            ps.setInt(1, NumSalle);
            ps.setInt(2, capacite);
            ps.setInt(3, NumCine);

            Salle nouvelleSalle = new Salle(cinema, String.valueOf(NumSalle), capacite);
            sallesList.add(nouvelleSalle);
            ps.executeUpdate();
            ps.close();
            ps1.close();
            ps2.close();
            con.close();


            // Effacer les champs après l'ajout
            Cinema.getSelectionModel().clearSelection();
            capacityField.clear();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La capacité doit être un nombre !");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void modifierSalle() {
        Salle selectedSalle = tableView.getSelectionModel().getSelectedItem();
        if (selectedSalle == null) {
            showAlert("Erreur", "Veuillez sélectionner une salle à modifier !");
            return;
        }

        String capaciteText = capacityField.getText();

        if (capaciteText.isEmpty()) {
            showAlert("Erreur", "Le champ capacité est obligatoire !");
            return;
        }

        try {
            int capacite = Integer.parseInt(capaciteText);
            selectedSalle.setCapacite(capacite);
            tableView.refresh();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La capacité doit être un nombre !");
        }
    }

    private void supprimerSalle() {
        Salle selectedSalle = tableView.getSelectionModel().getSelectedItem();
        if (selectedSalle != null) {
            sallesList.remove(selectedSalle);
        } else {
            showAlert("Erreur", "Veuillez sélectionner une salle à supprimer !");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
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
            String query = "select cinema.NomCine,salle.NumSalle,salle.Capacite from salle,cinema where salle.NumCine = cinema.NumCine;";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String nomCinema = rs.getString("NomCine");
                int numSalle = rs.getInt("NumSalle");
                int capacite = rs.getInt("Capacite");
                Salle nouvelleSalle = new Salle(nomCinema, String.valueOf(numSalle), capacite);
                sallesList.add(nouvelleSalle);
            }

            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données depuis la base.");
        }
    }
}
