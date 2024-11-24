package anaislyes.projetfinal;

import Objects.Cinema;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;

public class AjouterCinema extends Application {

    // Constructeur qui prend la référence de CinemaListView

    @Override
    public void start(Stage stage) {
        // Définir le layout principal
        BorderPane root = new BorderPane();


        // Titre
        Label titre = new Label("Ajouter un cinéma");
        titre.setFont(Font.font("Roboto", 30));
        HBox topBox = new HBox(titre);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(20, 0, 30, 0)); // Espace sous le titre

        // Création des Labels et TextFields
        Label EntrezLeNom = new Label("Entrez le nom du cinéma :");
        EntrezLeNom.setFont(Font.font("Roboto", 20));
        TextField NomCinema = new TextField();
        NomCinema.setPromptText("Nom du cinéma");
        NomCinema.setFont(Font.font("Roboto", 20));

        Label EntrezAdresse = new Label("Entrez l'adresse du cinéma :");
        EntrezAdresse.setFont(Font.font("Roboto", 20));
        TextField Adresse = new TextField();
        Adresse.setPromptText("Adresse du cinéma");
        Adresse.setFont(Font.font("Roboto", 20));

        Label EntrezCodePostal = new Label("Entrez le code postal :");
        EntrezCodePostal.setFont(Font.font("Roboto", 20));
        TextField CodePostal = new TextField();
        CodePostal.setPromptText("Code postal");
        CodePostal.setFont(Font.font("Roboto", 20));


        GridPane form = new GridPane();
        form.setPadding(new Insets(20, 20, 20, 20));
        form.setHgap(20);
        form.setVgap(20);


        form.add(EntrezLeNom, 0, 0);
        form.add(NomCinema, 1, 0);

        form.add(EntrezAdresse, 0, 1);
        form.add(Adresse, 1, 1);

        form.add(EntrezCodePostal, 0, 2);
        form.add(CodePostal, 1, 2);


        Button AjouterCinema = new Button("Ajouter");
        AjouterCinema.setFont(Font.font("Roboto", 20));
        AjouterCinema.setPrefHeight(50);
        AjouterCinema.setPrefWidth(100);

        AjouterCinema.setOnAction(f -> {
            try {
                Connection con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/cinemagestion", "root", "");
                PreparedStatement ps = con.prepareStatement("insert into cinema values(NULL,?,?,?)");
                ps.setString(1, NomCinema.getText());
                ps.setString(2, Adresse.getText());
                ps.setInt(3, Integer.parseInt(CodePostal.getText()));
                String Nom =NomCinema.getText();
                String Adr = Adresse.getText();
                int Code = Integer.parseInt(CodePostal.getText());
                ps.execute();
                Cinema cinema = new Cinema(Code,Nom,Adr);
                CinemaPane.data.add(cinema);
                CinemaPane.Actualiser();




                ps.close();
                con.close();
                root.getScene().getWindow().hide();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        HBox ButtonPane = new HBox(AjouterCinema);
        ButtonPane.setAlignment(Pos.CENTER);
        ButtonPane.setSpacing(20);
        ButtonPane.setPadding(new Insets(20, 20, 20, 20));




        root.setTop(topBox);
        root.setCenter(form);
        root.setBottom(ButtonPane);

        // Créer la scène
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/Styles/Styles.css").toExternalForm());
        stage.setTitle("Ajouter un Cinéma");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
