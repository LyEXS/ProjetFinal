module anaislyes.projetfinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.swing;
    requires mysql.connector.j;
    requires java.desktop;
    
    requires javafx.media;
    requires com.dlsc.gemsfx; 
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
   requires org.kordamp.ikonli.fontawesome;
   requires com.jfoenix;
   requires itextpdf;
   requires eu.hansolo.medusa;
  

 
    
    

		opens Objects to javafx.base;
 // Permet l'accès par réflexion pour la classe Cinema
		
		



    exports anaislyes.projetfinal;
    	// Assurez-vous que cela exporte correctement les bons packages
}
