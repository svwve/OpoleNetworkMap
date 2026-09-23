package com.example.backend;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class LoginController {

    @FXML
    private Label versionLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        String localVersion = UpdateService.getLocalVersion();
        if (versionLabel != null) {
            versionLabel.setText("Wersja: " + localVersion);
        }

        Thread updateThread = new Thread(() -> {
            try {
                if (UpdateService.isUpdateAvailable()) {
                    Platform.runLater(this::showUpdateDialog);
                }
            } catch (Exception e) {
                System.err.println("Nie udało się sprawdzić aktualizacji: " + e.getMessage());
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void showUpdateDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dostępna aktualizacja");
        alert.setHeaderText("Pojawiła się nowa wersja aplikacji!");
        alert.setContentText("Kliknij „Zaktualizuj”, aby pobrać najnowszą wersję z GitHub.");

        ButtonType updateButton = new ButtonType("Zaktualizuj");
        ButtonType cancelButton = new ButtonType("Później", ButtonType.CANCEL.getButtonData());

        alert.getButtonTypes().setAll(updateButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == updateButton) {
            Thread downloadThread = new Thread(() -> {
                UpdateService updateService = new UpdateService();
                updateService.downloadAndInstallUpdate();
            });
            downloadThread.setDaemon(true);
            downloadThread.start();
        }
        // Po kliknięciu "Później" okno dialogowe po prostu się zamyka
        // i użytkownik zostaje na ekranie logowania.
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Wypełnij wszystkie pola!");
            return;
        }

        if ("admin".equals(username) && "admin".equals(password)) {
            hideError();

            try {
                // Po udanym zalogowaniu otwieramy menu projektów
                // (Upewnij się, że ścieżka do pliku projektów jest poprawna, np. /com/example/backend/projects_view.fxml)
                URL resource = getClass().getResource("/projects_view.fxml");
                if (resource == null) {
                    showError("Błąd: Nie znaleziono pliku menu projektów.");
                    return;
                }

                Parent root = FXMLLoader.load(resource);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Opole Network Map - Menu Projektów");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showError("Błąd podczas ładowania menu projektów.");
            }
        } else {
            showError("Nieprawidłowy login lub hasło!");
        }
    }

    @FXML
    private void handleOpenRegister() {
        try {
            // Poprawna nazwa pliku: register_view.fxml
            URL resource = getClass().getResource("/register_view.fxml");
            if (resource == null) {
                showError("Błąd: Nie znaleziono pliku register_view.fxml.");
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Opole Network Map - Rejestracja");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Błąd wczytywania widoku rejestracji.");
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }
}
