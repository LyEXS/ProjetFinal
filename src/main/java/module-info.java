module anaislyes.projetfinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

   
    
  
    requires mysql.connector.j;
    requires java.desktop;
    requires javafx.media;
	requires javafx.graphics;
    opens Objects to javafx.base; // Allow reflection access for the Cinema class
    opens anaislyes.projetfinal to javafx.fxml;

    exports anaislyes.projetfinal;
    exports Controllers;
    opens Controllers to javafx.fxml;
}