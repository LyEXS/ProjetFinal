package anaislyes.projetfinal;

import javafx.animation.TranslateTransition;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.sql.SQLException;

public class CinemaApp extends Application {

    private boolean toolBarVisible = true; // Pour suivre l'état de la barre d'outils
    private Rectangle bookmark; // Déclaration du petit carré à utiliser dans le toggleToolBar

    @Override
    public void start(Stage primaryStage) {
        // Créer une barre d'outils verticale
        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: #2a2a2a;"); // Définir la couleur de fond de la ToolBar

        // Créer des boutons pour chaque option
        Button btnCinema = new Button("Cinéma");
        Button btnProjections = new Button("Projections");
        Button btnSalles = new Button("Salles");
        Button btnFilms = new Button("Films");

        // Créer un petit carré (bookmark) à droite de la ToolBar
        bookmark = new Rectangle(30, 50);
        bookmark.setFill(Color.web("#2a2a2a")); // Utiliser la même couleur que la ToolBar
        bookmark.setOpacity(1); // Toujours visible

        // Créer un bouton toggle dans le rectangle
        Label toggleButton = new Label("≡");
        toggleButton.setStyle("-fx-font-size: 20px; -fx-text-fill: #F2F2F7;");
        toggleButton.setMinWidth(30);
        toggleButton.setMaxHeight(50);
        toggleButton.setAlignment(Pos.CENTER);

        toggleButton.setStyle("-fx-background-color: #5c162e;");
        toggleButton.setOnMouseClicked(e -> toggleToolBar(toolBar, bookmark, toggleButton));

        // Créer un conteneur StackPane pour combiner le bouton toggle avec le rectangle
        StackPane bookmarkContainer = new StackPane();
        bookmarkContainer.getChildren().addAll(bookmark, toggleButton);

        // Ajouter les boutons à la barre d'outils
        toolBar.getItems().addAll(btnCinema, btnProjections, btnSalles, btnFilms);

        // Créer une HBox pour gérer la position de la ToolBar et du bookmark avec le bouton toggle
        HBox toolBarContainer = new HBox();
        toolBarContainer.getChildren().addAll(toolBar, bookmarkContainer);

        // Positionner la ToolBar à gauche et la rendre verticale
        toolBar.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // Uniformiser la taille des boutons
        btnCinema.setPrefWidth(93);
        btnCinema.setPrefHeight(50);
        btnProjections.setMinWidth(93);
        btnProjections.setPrefHeight(50);
        btnSalles.setMinWidth(93);
        btnSalles.setPrefHeight(50);
        btnFilms.setMinWidth(93);
        btnFilms.setPrefHeight(50);
        String videoPath = getClass().getResource("/Media/Video2.mp4").toExternalForm();        Media media = new Media(videoPath);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setPreserveRatio(true); // Maintenir les proportions de la vidéo
        mediaView.setFitWidth(2000); // Largeur maximum souhaitée (modifiez selon vos besoins)
        mediaView.setFitHeight(1080);


        // Créer un texte overlay bien visible
        // Créer un texte overlay très grand
        Label textOverlay = new Label("Bienvenue au Cinéma !");
        textOverlay.setTextFill(Color.WHITE); // Couleur du texte
        textOverlay.setFont(Font.font("Arial", 150)); // Taille de police très grande
        textOverlay.setStyle( // Optionnel : fond semi-transparent
                "-fx-text-fill: white; " + // Couleur du texte
                        "-fx-padding: 30px; " + // Espacement autour du texte
                        "-fx-alignment: center;"); // Centrer le texte

        // Créer le layout principal
        BorderPane root = new BorderPane();

        mediaPlayer.play();
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        root.setLeft(toolBarContainer); // Placer la ToolBar et le bookmark avec le bouton toggle à gauche
        root.setCenter(mediaView);
        // Conteneur de contenu dynamique
        StackPane dynamicContent = new StackPane();
        dynamicContent.getChildren().addAll(mediaView, textOverlay);
        StackPane.setAlignment(textOverlay, Pos.CENTER);
        root.setCenter(dynamicContent);



        // Gestion des actions des boutons
        btnCinema.setOnAction(e -> {
            // Créer un ProgressIndicator
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(100, 100); // Taille de l'indicateur
            progressIndicator.setStyle("-fx-progress-color: #5c162e;"); // Couleur personnalisée (facultatif)

            // Afficher l'indicateur dans le conteneur dynamique
            dynamicContent.getChildren().setAll(progressIndicator);

            // Créer un Task pour charger les données
            Task<CinemaPane> loadCinemaPaneTask = new Task<>() {
                @Override
                protected CinemaPane call() throws Exception {
                    
                    Thread.sleep(200);

               
                    return new CinemaPane(); // Remplacez par votre implémentation réelle
                }
            };

            // Une fois la tâche réussie
            loadCinemaPaneTask.setOnSucceeded(workerStateEvent -> {
                try {
                    dynamicContent.getChildren().setAll(loadCinemaPaneTask.getValue()); // Mettre à jour le contenu
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // En cas d'erreur
            loadCinemaPaneTask.setOnFailed(workerStateEvent -> {
                dynamicContent.getChildren().clear();
                Label errorLabel = new Label("Erreur de chargement !");
                errorLabel.setTextFill(Color.RED);
                dynamicContent.getChildren().add(errorLabel);
            });

            // Démarrer le chargement dans un nouveau thread
            new Thread(loadCinemaPaneTask).start();
        });

        btnProjections.setOnAction(e -> dynamicContent.getChildren().setAll(new ProjectionsPane()));
        btnSalles.setOnAction(e -> {
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(100, 100);
            progressIndicator.setStyle("-fx-progress-color: #5c162e;");

            dynamicContent.getChildren().setAll(progressIndicator);

            Task<SallesPane> loadSallesPaneTask = new Task<>() {
                @Override
                protected SallesPane call() throws Exception {
                    Thread.sleep(200); // Simulation de délai
                    return new SallesPane(); // Remplacez par l'implémentation réelle
                }
            };

            loadSallesPaneTask.setOnSucceeded(workerStateEvent -> {
                try {
                    dynamicContent.getChildren().setAll(loadSallesPaneTask.getValue());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            loadSallesPaneTask.setOnFailed(workerStateEvent -> {
                dynamicContent.getChildren().clear();
                Label errorLabel = new Label("Erreur de chargement !");
                errorLabel.setTextFill(Color.RED);
                dynamicContent.getChildren().add(errorLabel);
            });

            new Thread(loadSallesPaneTask).start();
        });
        btnFilms.setOnAction(e -> {
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(100, 100);
            progressIndicator.setStyle("-fx-progress-color: #5c162e;");

            dynamicContent.getChildren().setAll(progressIndicator);

            Task<FilmsPane> loadFilmsPaneTask = new Task<>() {
                @Override
                protected FilmsPane call() throws Exception {
                    Thread.sleep(200); // Simulation de délai
                    return new FilmsPane(); // Remplacez par l'implémentation réelle
                }
            };

            loadFilmsPaneTask.setOnSucceeded(workerStateEvent -> {
                try {
                    dynamicContent.getChildren().setAll(loadFilmsPaneTask.getValue());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            loadFilmsPaneTask.setOnFailed(workerStateEvent -> {
                dynamicContent.getChildren().clear();
                Label errorLabel = new Label("Erreur de chargement !");
                errorLabel.setTextFill(Color.RED);
                dynamicContent.getChildren().add(errorLabel);
            });

            new Thread(loadFilmsPaneTask).start();
        });
        



        // Configurer la scène principale
        Scene mainScene = new Scene(root);
        mainScene.getStylesheets().add(getClass().getResource("/Styles/Styles.css").toExternalForm());
        primaryStage.setTitle("Gestion du Cinéma");
        primaryStage.setMaximized(true);
        // Enlever la décoration native de la fenêtre et appliquer un style personnalisé
        primaryStage.initStyle(StageStyle.UNDECORATED); // Enlever la barre de titre et les bordures classiques
        primaryStage.setOpacity(1);
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    // Méthode pour masquer/afficher la ToolBar avec animation
    private void toggleToolBar(ToolBar toolBar, Rectangle bookmark, Label toggleButton) {
        TranslateTransition toolBarTransition = new TranslateTransition(Duration.seconds(0.5), toolBar);
        TranslateTransition bookmarkTransition = new TranslateTransition(Duration.seconds(0.5), bookmark);
        TranslateTransition toggleButtonTransition = new TranslateTransition(Duration.seconds(0.5), toggleButton);

        if (toolBarVisible) {
            // Faire glisser la ToolBar hors de l'écran
            toolBarTransition.setByX(-toolBar.getWidth());
            bookmarkTransition.setByX(-toolBar.getWidth()); // Faire déplacer le carré avec la ToolBar
            toggleButtonTransition.setByX(-toolBar.getWidth()); // Déplacer également le bouton toggle
        } else {
            // Faire glisser la ToolBar dans l'écran
            toolBarTransition.setByX(toolBar.getWidth());
            bookmarkTransition.setByX(toolBar.getWidth()); // Faire déplacer le carré avec la ToolBar
            toggleButtonTransition.setByX(toolBar.getWidth()); // Déplacer également le bouton toggle
        }

        toolBarTransition.play();
        bookmarkTransition.play();
        toggleButtonTransition.play();
        toolBarVisible = !toolBarVisible; // Inverser l'état de la ToolBar
    }
    
    

    public static void main(String[] args) {
        launch(args);
    }
}
