package anaislyes.projetfinal;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.ClockBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Accueil extends StackPane {
	String RessourcePath = "/resources/";
	String StylePath = "Styles/";
	//String StylePath = "";
	//String RessourcePath ="/";
    public Accueil() {
    	 Clock clock = ClockBuilder.create()
                 .skinType(Clock.ClockSkinType.CLOCK) // Type d'horloge
                 .hourTickMarkColor(Color.WHITE)     // Couleur des ticks d'heure
                 .minuteTickMarkColor(Color.WHITE)  // Couleur des ticks de minute
                 .hourColor(Color.WHITE)            // Couleur des aiguilles des heures
                 .minuteColor(Color.WHITE)          // Couleur des aiguilles des minutes
                 .secondColor(Color.RED)            // Couleur de l'aiguille des secondes
                 .running(true)                     // Active la mise à jour automatique des secondes
                 .build();

         // Ajout de l'horloge au conteneur
    	 StackPane cloackContainer = new StackPane();
    	 cloackContainer.getChildren().add(clock);
    	 
    	 cloackContainer.setPadding(new Insets(300,90,0,0));
         this.getChildren().add(cloackContainer);
      
        // Chemin vers la vidéo
       
       

        // Ajouter un texte superposé
        Text overlayText = new Text("");
        overlayText.setFill(Color.WHITE); // Couleur du texte
        overlayText.setFont(Font.font("Roboto", 110)); // Police élégante et classique
        overlayText.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.3);" // Fond semi-transparent plus subtil
            + "-fx-padding: 20;" // Marge autour du texte pour plus d'espace
           ); // Ombre douce mais visible

  

        // Centrer le texte dans le StackPane
        StackPane.setAlignment(overlayText, Pos.TOP_LEFT); // Position en haut à gauche
        overlayText.setTranslateX(50); // Décalage horizontal
        overlayText.setTranslateY(50); // Décalage vertical
    }
}
