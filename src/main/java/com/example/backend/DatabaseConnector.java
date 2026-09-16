package com.example.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    public static Connection getConnection() throws SQLException {
        // Jawnie zdefiniowany adres URL, login i hasło
        String url = "jdbc:postgresql://aws-1-eu-west-1.pooler.supabase.com:6543/postgres?sslmode=require";
        String user = "postgres.ewwfuazxazouidrpcvyk";
        String password = "Ypqf9Wn57oOyQY5k";


        try {
            // Wymuszenie załadowania sterownika dla środowiska modularnego JavaFX
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return DriverManager.getConnection(url, user, password);
    }
}
