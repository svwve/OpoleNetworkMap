package com.example.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://db.ewwfuaxzaxouidrpcvyk.supabase.co:5432/postgres");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "TWOJE_HASLO");

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Nie znaleziono sterownika PostgreSQL!", e);
        }
    }
}
