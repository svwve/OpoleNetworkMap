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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Label versionLabel;

    @FXML
    public void initialize() {
        checkVersionStatus();
    }

    private void checkVersionStatus() {
        try {
            Properties props = new Properties();
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                if (input != null) {
                    props.load(input);
                    String localVersion = props.getProperty("app.version", "1.0.0");

                    URI uri = URI.create("https://api.github.com/repos/svwve/OpoleNetworkMap/releases/latest");
                    HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/vnd.github+json");
                    connection.setRequestProperty("User-Agent", "OpoleNetworkMap-App");

                    // Sprawdźmy kod odpowiedzi HTTP
                    int responseCode = connection.getResponseCode();
                    if (responseCode != 200) {
                        System.err.println("GitHub API zwróciło błąd HTTP: " + responseCode);
                    }

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        String json = response.toString();
                        if (json.contains("tag_name")) {
                            int start = json.indexOf("tag_name") + 12;
                            int end = json.indexOf("\"", start);
                            String remoteVersion = json.substring(start, end).replace("v", "");

                            if (localVersion.equals(remoteVersion)) {
                                versionLabel.setText("Wersja aktualna (" + localVersion + ")");
                                versionLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 11px;");
                            } else {
                                versionLabel.setText("Dostępna nowa wersja: " + remoteVersion + " (Masz: " + localVersion + ")");
                                versionLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 11px;");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (versionLabel != null) {
                versionLabel.setText("Nie udało się sprawdzić aktualizacji");
                versionLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
            }
        }
    }


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
