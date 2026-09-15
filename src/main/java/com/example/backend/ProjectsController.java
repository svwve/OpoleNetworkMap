package com.example.backend;

import com.example.model.Project;
import com.example.model.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ProjectsController {

    @FXML private ListView<Project> projectsListView;
    @FXML private TextField newProjectNameField;

    private Role currentRole;

    public void initData(Role role) {
        this.currentRole = role;
        refreshList();
    }

    @FXML
    public void initialize() {
        refreshList();
    }

    private void refreshList() {
        if (projectsListView != null) {
            projectsListView.getItems().clear();
            projectsListView.getItems().addAll(ProjectService.getProjects());
        }
    }

    @FXML
    private void handleCreateProject() {
        if (newProjectNameField == null) return;

        String projectName = newProjectNameField.getText();
        if (projectName != null && !projectName.trim().isEmpty()) {
            Project created = ProjectService.createProject(projectName.trim());

            if (created != null) {
                newProjectNameField.clear();
                refreshList();
            }
        }
    }

    @FXML
    private void handleOpenProject() {
        Project selected = projectsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            URL fxmlLocation = getClass().getResource("/map_view.fxml");

            if (fxmlLocation == null) {
                System.err.println("Nie znaleziono pliku FXML! Sprawdź ścieżkę w getResource().");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            MapController mapController = loader.getController();
            if (mapController != null) {
                mapController.initData(selected, currentRole);
            }

            Stage stage = (Stage) projectsListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Błąd podczas ładowania widoku mapy: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
