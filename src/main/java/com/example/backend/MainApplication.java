package com.example.backend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        checkforUpdates();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/login_view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 500);
        stage.setTitle("Logowanie - Opole Network Map");
        stage.setScene(scene);
        stage.show();
    }

    private void checkforUpdates() {
        try {
            java.util.Properties props = new java.util.Properties();
            try (java.io.InputStream input = MainApplication.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input != null) {
                    props.load(input);
                    String localVersion = props.getProperty("app.version", "1.0.0");

                     java.net.URI uri = java.net.URI.create("https://api.github.com/repos/svwve/OpoleNetworkMap/releases/latest");
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) uri.toURL().openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/vnd.github+json");

                    try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        String json = response.toString();
                        if (json.contains("tag_name")) {
                            int start = json.indexOf("tag_name") + 12;
                            int end = json.indexOf("\"", start);
                            String remoteVersion = json.substring(start, end).replace("v", ""); // usuwa 'v' jeśli dodajesz np. v1.1.0



                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Nie udało się sprawdzić aktualizacji: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        launch();
    }
}
