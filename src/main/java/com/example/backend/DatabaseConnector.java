package com.example.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    private static final String URL = "jdbc:postgresql://db.ewwfuazxazouidrpcvyk.supabase.co:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "K@R0lPLlarczyk2oo8";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Nie znaleziono sterownika PostgreSQL!", e);
        }
    }
}
