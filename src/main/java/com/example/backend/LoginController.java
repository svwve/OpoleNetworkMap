package com.example.backend;

import com.example.model.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        Role loggedRole = UserService.authenticate(username, password);

        if (loggedRole != null) {
            openProjectsWindow(loggedRole);
        } else {
            errorLabel.setText("Błędna nazwa użytkownika lub hasło!");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleOpenRegister() {
        try {
            URL fxmlLocation = getClass().getResource("/register_view.fxml");
            if (fxmlLocation == null) {
                System.err.println("BŁĄD: Nie znaleziono register_view.fxml w folderze resources!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 420));
            stage.setTitle("Rejestracja - Opole Network Map");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openProjectsWindow(Role role) {
        try {
            URL fxmlLocation = getClass().getResource("/projects_view.fxml");
            if (fxmlLocation == null) {
                System.err.println("BŁĄD: Nie znaleziono projects_view.fxml!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            ProjectsController controller = loader.getController();
            if (controller != null) {
                controller.initData(role);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 480));
            stage.setTitle("Wybór Projektu - Opole Network Map");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
