package anaislyes.projetfinal;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.sql.SQLException;

public class CinemaApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Créer une barre d'outils
        ToolBar toolBar = new ToolBar();

        // Créer des boutons pour chaque option
        Button btnCinema = new Button("Cinéma");
        Button btnProjections = new Button("Projections");
        Button btnSalles = new Button("Salles");
        Button btnFilms = new Button("Films");

        // Ajouter les boutons à la barre d'outils
        toolBar.getItems().addAll(btnCinema, btnProjections, btnSalles, btnFilms);

        // Créer le layout principal
        BorderPane root = new BorderPane();
        root.setTop(toolBar);


        StackPane dynamicContent = new StackPane();
        root.setCenter(dynamicContent);


        btnCinema.setOnAction(e -> {
            try {
                dynamicContent.getChildren().setAll(new CinemaPane());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        btnProjections.setOnAction(e -> dynamicContent.getChildren().setAll(new ProjectionsPane()));
        btnSalles.setOnAction(e -> {
            try {
                dynamicContent.getChildren().setAll(new SallesPane());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        btnFilms.setOnAction(e -> dynamicContent.getChildren().setAll(new FilmsPane()));

        // Configurer la scène principale
        Scene mainScene = new Scene(root);
        mainScene.getStylesheets().add(getClass().getResource("/Styles/Styles.css").toExternalForm());
        primaryStage.setTitle("Gestion du Cinéma");
        primaryStage.setMaximized(true);
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
