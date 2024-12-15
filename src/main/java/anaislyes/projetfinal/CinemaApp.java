package anaislyes.projetfinal;



import java.io.FileNotFoundException;
import java.io.InputStream;

import org.kordamp.ikonli.fontawesome.FontAwesome;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import anaislyes.projetfinal.*;
import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.ClockBuilder;


public class CinemaApp extends Application {
	String RessourcePath = "/resources/";
	String StylePath = "Styles/";
	//String StylePath = "";
	//String RessourcePath ="/";


    private boolean toolBarVisible = true; // Pour suivre l'état de la barre d'outils
    private Rectangle bookmark; // Déclaration du petit carré à utiliser dans le toggleToolBar

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
    	
    	
    	Image cursorImage = new Image(getClass().getResource(RessourcePath+"images/Black-Angel.png").toExternalForm());
    	
    	
    	 Image backgroundImage = new Image(getClass().getResource(RessourcePath+"images/BackgroundAccueil.jpg").toExternalForm());
    	    BackgroundImage bgImage = new BackgroundImage(
    	        backgroundImage, 
    	        BackgroundRepeat.NO_REPEAT, 
    	        BackgroundRepeat.NO_REPEAT, 
    	        BackgroundPosition.CENTER, 
    	        new BackgroundSize(1920, 1080, false, false, true, true)
    	    );
    	
    	   
    	    // Conteneur principal avec arrière-plan
    	    BorderPane root = new BorderPane();
    	   
    	    root.setCursor(new ImageCursor(cursorImage));
    	    root.setPrefWidth(1920);
    	    root.setPrefHeight(1080);
    	    root.setBackground(new Background(bgImage));
    	    System.out.println(getClass().getResource(RessourcePath+"images/Background.jpg"));

        

        
        
        // Créer une barre d'outils verticale
        ToolBar toolBar = new ToolBar();
        toolBar.setPrefWidth(150);
        toolBar.setStyle("-fx-background-color: #141414;"); // Définir la couleur de fond de la ToolBar

        // Créer des boutons pour chaque option
        Clock clock = ClockBuilder.create()
                .skinType(Clock.ClockSkinType.DIGITAL) // Style d'horloge digitale
                .textColor(Color.WHITE)               // Couleur du texte (heure, minutes, secondes)
                .dateColor(Color.WHITE)               // Couleur de la date (si activée)
                .running(true)                        // Horloge active
                .build();
       
        
        StackPane clockContainer = new StackPane();
        clock.setMaxWidth(150);
        clockContainer.getChildren().add(clock);
        clockContainer.setAlignment(Pos.CENTER);
        clockContainer.setPadding(new Insets(460,0,0,0)); // Ajouter un espace autour de l'horloge
        clockContainer.setMaxWidth(150);
        clock.setStyle("-fx-background-color: transparent;");

        Button btnCinema = createButtonWithIcon("Cinéma", new FontIcon(FontAwesome.FILM));
        Button btnProjections = createButtonWithIcon("Projections", new FontIcon(FontAwesome.CALENDAR));
        Button btnSalles = createButtonWithIcon("Salles", new FontIcon(FontAwesome.BUILDING));
        Button btnFilms = createButtonWithIcon("Films", new FontIcon(FontAwesome.VIDEO_CAMERA));
        Button btnAccueil = createButtonWithIcon("Accueil", new FontIcon(FontAwesome.HOME));

        // Créer un petit carré (bookmark) à droite de la ToolBar
        bookmark = new Rectangle(30, 50);
        bookmark.setFill(Color.web("#370a1c")); // Utiliser la même couleur que la ToolBar
        bookmark.setOpacity(1); // Toujours visible

        // Créer un bouton toggle dans le rectangle
        Label toggleButton = new Label("≡");
        toggleButton.setStyle("-fx-font-size: 20px; -fx-text-fill: #F2F2F7;");
        toggleButton.setMinWidth(30);
        toggleButton.setMaxHeight(50);
        toggleButton.setAlignment(Pos.CENTER);

        toggleButton.setStyle("-fx-background-color: #370a1c;");
        toggleButton.setOnMouseClicked(e -> toggleToolBar(toolBar, bookmark, toggleButton));

        // Créer un conteneur StackPane pour combiner le bouton toggle avec le rectangle
        StackPane bookmarkContainer = new StackPane();
        bookmarkContainer.getChildren().addAll(bookmark, toggleButton);

        // Ajouter les boutons à la barre d'outils
        toolBar.getItems().addAll(btnAccueil,btnCinema, btnProjections, btnSalles, btnFilms,clockContainer);
        
        // Créer une HBox pour gérer la position de la ToolBar et du bookmark avec le bouton toggle
        HBox toolBarContainer = new HBox();
        toolBarContainer.getChildren().addAll(toolBar, bookmarkContainer);
        HBox.setMargin(btnAccueil, new Insets(30, 0, 0, 0));
        // Positionner la ToolBar à gauche et la rendre verticale
        toolBar.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // Uniformiser la taille des boutons
        btnCinema.setPrefWidth(140);
        btnCinema.setPrefHeight(50);
        btnProjections.setMinWidth(140);
        btnProjections.setPrefHeight(50);
        btnSalles.setMinWidth(140);
        btnSalles.setPrefHeight(50);
        btnFilms.setMinWidth(140);
        btnFilms.setPrefHeight(50);
        btnAccueil.setPrefWidth(140); // Set the width to match the other buttons
        btnAccueil.setPrefHeight(50);
        /*
        Image cinemaImage = new Image(getClass().getResource("/Images/cinema.png").toExternalForm());
        Image projectionsImage = new Image(getClass().getResource("/Images/projections.png").toExternalForm());
        Image sallesImage = new Image(getClass().getResource("/Images/salles.png").toExternalForm());
        Image filmsImage = new Image(getClass().getResource("/Images/films.png").toExternalForm());
        Image AccueilImage = new Image(getClass().getResource("/Images/accueil.png").toExternalForm());

        // Créer des ImageView avec des dimensions ajustées
        ImageView cinemaView = new ImageView(cinemaImage);
        cinemaView.setFitWidth(40); // Largeur ajustée
        cinemaView.setFitHeight(40); // Hauteur ajustée

        ImageView projectionsView = new ImageView(projectionsImage);
        projectionsView.setFitWidth(40);
        projectionsView.setFitHeight(40);

        ImageView sallesView = new ImageView(sallesImage);
        sallesView.setFitWidth(40);
        sallesView.setFitHeight(40);

        ImageView filmsView = new ImageView(filmsImage);
        filmsView.setFitWidth(40);
        filmsView.setFitHeight(40);
        
        ImageView AccueilView = new ImageView(AccueilImage);
        AccueilView.setFitWidth(40); // Adjust width
        AccueilView.setFitHeight(40); // Adjust height

        // Associer les ImageView aux boutons
        btnCinema.setGraphic(cinemaView);
        btnProjections.setGraphic(projectionsView);
        btnSalles.setGraphic(sallesView);
        btnFilms.setGraphic(filmsView);
        btnAccueil.setGraphic(AccueilView);

      
        
        */
        



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
       
        
        Button quitter = new Button("X");
        quitter.setStyle("-fx-background-color: #5c162e; -fx-text-fill: white;");
        quitter.setOnAction(e -> {
            // Créer une alerte de confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de sortie");
            alert.setHeaderText("Voulez-vous vraiment quitter ?");
            alert.setContentText("Toutes les modifications non sauvegardées seront perdues.");

            // Ajouter les boutons Oui et Non
            ButtonType buttonYes = new ButtonType("Oui");
            ButtonType buttonNo = new ButtonType("Non",ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonYes, buttonNo);

            // Afficher la boîte de dialogue et attendre une réponse
            alert.showAndWait().ifPresent(response -> {
                if (response == buttonYes) {
                    Platform.exit(); // Quitter l'application
                }
                // Sinon, rien ne se passe
            });
        });

        quitter.setLineSpacing(10);
        HBox.setMargin(quitter, new Insets(30, 10, 10, 100));
        // Positionner le bouton Quitter à droite et avec un espace de 30px
        root.setRight(quitter); // Placer le bouton Quitter à droite
        
        
       
        
        
        root.setLeft(toolBarContainer); // Placer la ToolBar et le bookmark avec le bouton toggle à gauche
        
        // Conteneur de contenu dynamique
        StackPane dynamicContent = new StackPane();
        dynamicContent.getChildren().add(new Accueil()); // Ajouter le texte overlay au conteneur dynamique
        StackPane.setAlignment(textOverlay, Pos.CENTER);
        root.setCenter(dynamicContent);
        
        btnAccueil.setOnAction(e -> {
            // Créer un ProgressIndicator
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(100, 100); // Taille de l'indicateur
            progressIndicator.setStyle("-fx-progress-color: #5c162e;"); // Couleur personnalisée (facultatif)

            // Afficher l'indicateur dans le conteneur dynamique
            dynamicContent.getChildren().setAll(progressIndicator);

            // Créer un Task pour charger les données
            Task<Accueil> loadCinemaPaneTask = new Task<>() {
                @Override
                protected Accueil call() throws Exception {
                    

               
                    return new Accueil(); // Remplacez par votre implémentation réelle
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

        btnProjections.setOnAction(e -> {
            // Créer un ProgressIndicator
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(100, 100); // Taille de l'indicateur
            progressIndicator.setStyle("-fx-progress-color: #5c162e;"); // Couleur personnalisée (facultatif)

            // Afficher l'indicateur dans le conteneur dynamique
            dynamicContent.getChildren().setAll(progressIndicator);

            // Créer un Task pour charger les données de projection
            Task<ProjectionsPane> loadProjectionPaneTask = new Task<>() {
                @Override
                protected ProjectionsPane call() throws Exception {
                    // Simuler un processus de chargement (par exemple, une requête à la base de données)
                  
                    return new ProjectionsPane(); // Remplacez par votre implémentation réelle de la projection
                }
            };

            // Lors de la fin du chargement, afficher le contenu de la projection
            loadProjectionPaneTask.setOnSucceeded(event -> {
                dynamicContent.getChildren().setAll(loadProjectionPaneTask.getValue());
            });

            // Lancer la tâche dans un thread d'arrière-plan
            new Thread(loadProjectionPaneTask).start();
        });
        btnSalles.setOnAction(e -> {
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(100, 100);
            progressIndicator.setStyle("-fx-progress-color: #5c162e;");

            dynamicContent.getChildren().setAll(progressIndicator);

            Task<SallesPane> loadSallesPaneTask = new Task<>() {
                @Override
                protected SallesPane call() throws Exception {
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
        mainScene.getStylesheets().add(getClass().getResource(RessourcePath+StylePath+"Styles.css").toExternalForm());
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
 // Configuration commune pour un bouton avec une icône alignée à gauche
    private Button createButtonWithIcon(String text, FontIcon icon) {
        // Créer une HBox pour l'alignement
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT); // Aligner le contenu à gauche
        content.setSpacing(10); // Espacement entre l'icône et le texte

        // Appliquer un style à l'icône
        icon.setIconSize(24);
       

        // Créer le texte du bouton
        Label label = new Label(text);
        label.setTextFill(Color.WHITE); // Texte blanc

        // Ajouter l'icône et le texte dans le HBox
        content.getChildren().addAll(icon, label);

        // Créer le bouton et définir le HBox comme contenu graphique
        Button button = new Button();
        button.setGraphic(content);
     

        return button;
    }

   

    public static void main(String[] args) {
    	
        launch(args);
    }
}
