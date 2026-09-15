module com.example.opolenetworkmap {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;

    exports com.example;
    exports com.example.backend;

    opens com.example to javafx.fxml;
    opens com.example.backend to javafx.fxml;
}
