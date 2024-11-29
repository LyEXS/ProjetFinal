package anaislyes.projetfinal;

import Objects.Cinema;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.*;

public class CinemaPane extends BorderPane {
    private static TableView<Cinema> tableView;
    public static ObservableList<Cinema> data;
    private ComboBox<String> villeComboBox;

    public CinemaPane() throws SQLException {
        // Initialiser les données
        data = FXCollections.observableArrayList();
        tableView = new TableView<>();

        // Configurer les colonnes de la table
        TableColumn<Cinema, Integer> numCinemaColumn = new TableColumn<>("Numéro");
        numCinemaColumn.setCellValueFactory(new PropertyValueFactory<>("numCinema"));

        TableColumn<Cinema, String> nomCinemaColumn = new TableColumn<>("Nom");
        nomCinemaColumn.setCellValueFactory(new PropertyValueFactory<>("nomCinema"));

        TableColumn<Cinema, String> adresseCinemaColumn = new TableColumn<>("Adresse");
        adresseCinemaColumn.setCellValueFactory(new PropertyValueFactory<>("adresseCinema"));

        tableView.getColumns().addAll(numCinemaColumn, nomCinemaColumn, adresseCinemaColumn);
        tableView.setItems(data);
        tableView.setMaxWidth(1000);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);



        // Charger les données depuis la base de données
        chargerDonneesDepuisBase();

        // Ajouter les boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));

        Button addButton = new Button("Ajouter");
        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER);

        setCenter(tableView);
        setBottom(buttonBox);

        // Actions des boutons
        addButton.setOnAction(e -> {
            try {
                afficherDialogCinema(null);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        editButton.setOnAction(e -> {
            Cinema selectedCinema = tableView.getSelectionModel().getSelectedItem();
            if (selectedCinema == null) {
                showAlert("Erreur", "Veuillez sélectionner un cinéma à modifier.");
            } else {
                try {
                    afficherDialogCinema(selectedCinema);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        deleteButton.setOnAction(e -> supprimerCinema());
    }

    public static void Actualiser() {
        tableView.refresh();
    }

    // Charger les données depuis la base de données
    private void chargerDonneesDepuisBase() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT NumCine, NomCine, AdrsCine FROM Cinema");

            while (rs.next()) {
                int numCinema = rs.getInt("NumCine");
                String nomCinema = rs.getString("NomCine");
                String adresseCinema = rs.getString("AdrsCine");
                data.add(new Cinema(numCinema, nomCinema, adresseCinema));
            }

            con.close();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données.");
        }
    }

    // Afficher un DialogPane pour ajouter ou modifier un cinéma
    private void afficherDialogCinema(Cinema cinemaExistant) throws SQLException {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(cinemaExistant == null ? "Ajouter un cinéma" : "Modifier un cinéma");
        dialog.setHeaderText(cinemaExistant == null ? "Ajoutez un nouveau cinéma." : "Modifiez les informations du cinéma.");

        // Boutons du Dialog
        ButtonType validerButtonType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(validerButtonType, ButtonType.CANCEL);

        // Champs de formulaire
        TextField nameField = new TextField();
        nameField.setPromptText("Nom du cinéma");
        TextField addressField = new TextField();
        addressField.setPromptText("Adresse");
        villeComboBox = new ComboBox<>();
        villeComboBox.setPromptText("Sélectionnez une ville");
        remplirComboBox();

        if (cinemaExistant != null) {
            nameField.setText(cinemaExistant.getNomCinema());
            addressField.setText(cinemaExistant.getAdresseCinema());
            villeComboBox.setValue(obtenirNomVille(cinemaExistant.getNumCinema()));
        }

        VBox content = new VBox(10, new Label("Nom :"), nameField, new Label("Adresse :"), addressField, new Label("Ville :"), villeComboBox);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        // Gestion du résultat
        dialog.setResultConverter(button -> {
            if (button == validerButtonType) {
                String nom = nameField.getText().trim();
                String adresse = addressField.getText().trim();
                String ville = villeComboBox.getValue();

                if (nom.isEmpty() || adresse.isEmpty() || ville == null) {
                    showAlert("Erreur", "Tous les champs doivent être remplis !");
                    return null;
                }

                try {
                    int codePostal = obtenirCodePostal(ville);

                    if (cinemaExistant == null) {
                        ajouterCinemaDansBD(nom, adresse, codePostal);
                    } else {
                        modifierCinemaDansBD(cinemaExistant, nom, adresse, codePostal);
                    }

                    tableView.refresh();
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de l'enregistrement du cinéma.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    // Ajouter un cinéma dans la base de données
    private void ajouterCinemaDansBD(String nom, String adresse, int codePostal) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
        PreparedStatement pstmt = con.prepareStatement("INSERT INTO Cinema (NomCine, AdrsCine, CodePostal) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, nom);
        pstmt.setString(2, adresse);
        pstmt.setInt(3, codePostal);
        pstmt.executeUpdate();

        ResultSet rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            int numCinema = rs.getInt(1);
            data.add(new Cinema(numCinema, nom, adresse));
        }

        con.close();
    }

    // Modifier un cinéma dans la base de données
    private void modifierCinemaDansBD(Cinema cinema, String nom, String adresse, int codePostal) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
        PreparedStatement pstmt = con.prepareStatement("UPDATE Cinema SET NomCine = ?, AdrsCine = ?, CodePostal = ? WHERE NumCine = ?");
        pstmt.setString(1, nom);
        pstmt.setString(2, adresse);
        pstmt.setInt(3, codePostal);
        pstmt.setInt(4, cinema.getNumCinema());
        pstmt.executeUpdate();

        cinema.setNomCinema(nom);
        cinema.setAdresseCinema(adresse);

        con.close();
    }

    // Supprimer un cinéma
    private void supprimerCinema() {
        Cinema selectedCinema = tableView.getSelectionModel().getSelectedItem();
        if (selectedCinema == null) {
            showAlert("Erreur", "Veuillez sélectionner un cinéma à supprimer.");
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
            PreparedStatement pstmt = con.prepareStatement("DELETE FROM Cinema WHERE NumCine = ?");
            pstmt.setInt(1, selectedCinema.getNumCinema());
            pstmt.executeUpdate();

            data.remove(selectedCinema);

            con.close();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de supprimer le cinéma.");
        }
    }

    // Récupérer le code postal d'une ville
    private int obtenirCodePostal(String ville) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
        PreparedStatement pstmt = con.prepareStatement("SELECT CodePostal FROM ville WHERE NomVille = ?");
        pstmt.setString(1, ville);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("CodePostal");
        }
        throw new SQLException("Ville introuvable.");
    }

    // Remplir le ComboBox des villes
    private void remplirComboBox() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
            PreparedStatement pstmt = con.prepareStatement("SELECT NomVille FROM ville");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                villeComboBox.getItems().add(rs.getString("NomVille"));
            }

            con.close();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les villes.");
        }
    }

    // Récupérer le nom de la ville pour un cinéma donné
    private String obtenirNomVille(int numCinema) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion", "root", "");
        PreparedStatement pstmt = con.prepareStatement("SELECT NomVille FROM ville v JOIN Cinema c ON v.CodePostal = c.CodePostal WHERE c.NumCine = ?");
        pstmt.setInt(1, numCinema);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getString("NomVille");
        }
        throw new SQLException("Ville introuvable pour le cinéma donné.");
    }

    // Afficher une alerte
    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
