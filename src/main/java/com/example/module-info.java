module com.example.opolenetworkmap {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires jdk.jsobject; // <-- To rozwiązuje problem z netscape.javascript

    // Wskaż pakiety, które faktycznie istnieją w Twoim projekcie:
    opens com.example.backend to javafx.fxml;
    exports com.example.backend;
    exports com.example.model;
}
