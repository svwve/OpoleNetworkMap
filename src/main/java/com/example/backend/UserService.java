package com.example.backend;

import com.example.model.Role;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    public static Role authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT password_hash, role FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String roleStr = rs.getString("role");

                    // Weryfikacja hasła z hashem w bazie za pomocą BCrypt
                    if (BCrypt.checkpw(password, storedHash)) {
                        return Role.valueOf(roleStr.toUpperCase());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Błąd bazy danych podczas logowania: " + e.getMessage());
        }

        return null; // Błędny login, hasło lub użytkownik nie istnieje
    }

    // Metoda rejestrująca nowego użytkownika z bezpiecznym hashem
    public static boolean registerUser(String username, String password, Role role) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }

        String query = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";

        // Generowanie bezpiecznego hashu z podanego hasła
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username.trim());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role.name());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // Ewentualny błąd, np. gdy nazwa użytkownika już istnieje (UNIQUE constraint w bazie)
            e.printStackTrace();
            return false;
        }
    }

}
