module com.example.opolenetworkmap {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires jbcrypt;
    requires jdk.jsobject; // <-- DODAJ TĘ LINIE

    opens com.example.backend to javafx.fxml;
    exports com.example.backend;
    exports com.example.model;
}
