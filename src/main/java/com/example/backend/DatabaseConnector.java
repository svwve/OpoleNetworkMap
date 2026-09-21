package com.example.backend;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnector {

    public static Connection getConnection() throws SQLException {
        String url = "";
        String user = "";
        String password = "";

        // Wczytywanie danych z pliku config.properties
        try (InputStream input = DatabaseConnector.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                url = prop.getProperty("db.url");
                user = prop.getProperty("db.user");
                password = prop.getProperty("db.password");
            } else {
                throw new SQLException("Nie znaleziono pliku config.properties w zasobach!");
            }
        } catch (Exception e) {
            throw new SQLException("Nie udało się wczytać konfiguracji bazy danych: " + e.getMessage());
        }

        try {
            // Wymuszenie załadowania sterownika dla środowiska modularnego JavaFX
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return DriverManager.getConnection(url, user, password);
    }
}
