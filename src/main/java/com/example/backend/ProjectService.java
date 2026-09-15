package com.example.backend;

import com.example.model.Project;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectService {

    public static List<Project> getProjects() {
        List<Project> projects = new ArrayList<>();
        // Zmiana 'name' na 'title'
        String query = "SELECT id, title FROM projects ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("title"); // Pobieramy wartość z kolumny title
                projects.add(new Project(id, name));
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania projektów: " + e.getMessage());
            e.printStackTrace();
        }

        return projects;
    }

    public static Project createProject(String name) {
        int defaultUserId = 1;
        return createProject(name, defaultUserId);
    }

    public static Project createProject(String name, int userId) {
        if (name == null || name.trim().isEmpty()) return null;

        // Zmiana kolumny 'name' na 'title'
        String query = "INSERT INTO projects (title, user_id) VALUES (?, ?) RETURNING id";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name.trim());
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int generatedId = rs.getInt("id");
                System.out.println("Utworzono projekt w Supabase o ID: " + generatedId);
                return new Project(generatedId, name.trim());
            }

        } catch (SQLException e) {
            System.err.println("Błąd podczas zapisywania projektu: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
