package com.example.backend;

import com.example.model.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll(Role.values());
        roleComboBox.getSelectionModel().select(Role.OPERATOR);
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        Role selectedRole = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Wypełnij wszystkie pola!", Color.RED);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Hasła nie są identyczne!", Color.RED);
            return;
        }

        boolean success = UserService.registerUser(username, password, selectedRole);
        if (success) {
            showMessage("Rejestracja udana! Możesz się zalogować.", Color.GREEN);
        } else {
            showMessage("Użytkownik o takiej nazwie już istnieje!", Color.RED);
        }
    }

    @FXML
    public void handleBackToLogin() {
        try {
            URL fxmlLocation = getClass().getResource("/login_view.fxml");
            if (fxmlLocation == null) {
                System.err.println("BŁĄD: Nie odnaleziono login_view.fxml!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 300));
            stage.setTitle("Logowanie - Opole Network Map");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String text, Color color) {
        messageLabel.setText(text);
        messageLabel.setTextFill(color);
        messageLabel.setVisible(true);
    }
}
