package com.example.backend;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnector {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        Properties prop = new Properties();
        try (InputStream input = DatabaseConnector.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                prop.load(input);
                URL = prop.getProperty("db.url");
                USER = prop.getProperty("db.user");
                PASSWORD = prop.getProperty("db.password");
            }
        } catch (Exception ex) {
        }

        if (URL == null) URL = System.getenv("DB_URL");
        if (USER == null) USER = System.getenv("DB_USER");
        if (PASSWORD == null) PASSWORD = System.getenv("DB_PASSWORD");
    }

    public static Connection getConnection() throws SQLException {
        if (PASSWORD == null || PASSWORD.isEmpty()) {
            throw new SQLException("Brak konfiguracji bazy danych! Sprawdź plik config.properties.");
        }
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Nie znaleziono sterownika PostgreSQL!", e);
        }
    }
}
