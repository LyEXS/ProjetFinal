module anaislyes.projetfinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    requires org.controlsfx.controls;
    requires mysql.connector.j;
    opens Objects to javafx.base; // Allow reflection access for the Cinema class
    opens anaislyes.projetfinal to javafx.fxml;

    exports anaislyes.projetfinal;
    exports Controllers;
    opens Controllers to javafx.fxml;
}