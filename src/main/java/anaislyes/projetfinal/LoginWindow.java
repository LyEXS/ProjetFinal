package anaislyes.projetfinal;

import java.sql.Connection;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginWindow extends Application {
	
	String RessourcePath = "/resources/";
	String StylePath = "Styles/";
	//String StylePath = "";
	//String RessourcePath ="/";

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
        // Titre
        Text sceneTitle = new Text("CinéFlow - Connexion");
        sceneTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        sceneTitle.setFill(Color.WHITE);
        
        
        InputStream inputstream = getClass().getResourceAsStream(RessourcePath+"images/Logo.png");
        
        
        Image image = new Image(inputstream, 64, 64, true, true); // Largeur et hauteur spécifiées
        primaryStage.getIcons().add(image);
        
        
        
        Image backgroundImage = new Image(getClass().getResource(RessourcePath+"images/LoginBackground.jpg").toExternalForm());
	    BackgroundImage bgImage = new BackgroundImage(
	        backgroundImage, 
	        BackgroundRepeat.NO_REPEAT, 
	        BackgroundRepeat.NO_REPEAT, 
	        BackgroundPosition.CENTER, 
	        new BackgroundSize(1920, 1080, false, false, true, true)
	    );

        // Grille pour les champs utilisateur et mot de passe
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);


        // Nom d'utilisateur
        Label userNameLabel = new Label("Nom d'utilisateur:");
        userNameLabel.setFont(Font.font("Arial", 14));
        TextField userTextField = new TextField();
        userTextField.setPromptText("Entrez votre nom d'utilisateur");
        userTextField.setPrefWidth(250);

        // Mot de passe
        Label pwLabel = new Label("Mot de passe:");
        pwLabel.setFont(Font.font("Arial", 14));
        PasswordField pwBox = new PasswordField();
        pwBox.setPromptText("Entrez votre mot de passe");

        // Bouton Connexion
        Button loginButton = new Button("Se connecter");
        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        loginButton.setMaxWidth(Double.MAX_VALUE);
        
        loginButton.setOnAction(e->{
        	try {
				authentificate(primaryStage,userTextField, pwBox);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
        });

        // Ajouter les éléments à la grille
        grid.add(userNameLabel, 0, 0);
        grid.add(userTextField, 1, 0);
        grid.add(pwLabel, 0, 1);
        grid.add(pwBox, 1, 1);
  

        // Footer
        Text footer = new Text("© 2024 CinéFlow - Tous droits réservés");
        footer.setFont(Font.font("Verdana", 10));
        footer.setFill(Color.GRAY);

        // Ajouter tout dans un VBox
        VBox root = new VBox(50);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(30));
        root.getChildren().addAll(sceneTitle, grid,loginButton, footer);
	    root.setBackground(new Background(bgImage));


        // Appliquer le style
        Scene scene = new Scene(root, 500, 450);
        scene.getStylesheets().add(RessourcePath+StylePath+"Styles.css"); // Créez ce fichier pour plus de style

        // Configurer et afficher la fenêtre
        primaryStage.setTitle("Connexion - CinéSphere");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    
    private void authentificate(Stage stage ,TextField username, TextField password) throws FileNotFoundException {
    	try(Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cinemagestion","root","")){
    		PreparedStatement st = con.prepareStatement("select * from admin where username = ? and password = ? ");
    		st.setString(1, username.getText());
    		
            st.setString(2, password.getText());
    		ResultSet rs = st.executeQuery();
    		
    		
            if(rs.next()){
            	CinemaApp cinemaApp = new CinemaApp();
            	cinemaApp.start(new Stage());
            	stage.close();	
            }
            else {
            	Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur d'authentification");
                alert.setHeaderText("Nom d'utilisateur ou mot de passe incorrect");
                alert.setContentText("Veuillez réessayer");
                alert.showAndWait();
                username.clear();
                password.clear();
                
                return;
            }
            
    		
    		
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static void main(String[] args) {
        launch(args);
    }
}
