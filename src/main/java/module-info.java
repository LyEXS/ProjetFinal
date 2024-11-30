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

    opens Objects to javafx.base; // Permet l'accès par réflexion pour la classe Cinema
    opens anaislyes.projetfinal to javafx.fxml;

    exports anaislyes.projetfinal;  // Assurez-vous que cela exporte correctement les bons packages
}
