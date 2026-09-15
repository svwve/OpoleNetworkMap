package com.example.backend;

import com.example.model.Project;
import com.example.model.Role;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapController {

    @FXML private WebView webView;
    @FXML private ListView<String> elementList;

    @FXML private Button btnBackToMenu;
    @FXML private Button btnAddCamera;
    @FXML private Button btnAddFiber;
    @FXML private Button btnFinishFiber;
    @FXML private Button btnEditFiber;
    @FXML private Button btnRename;
    @FXML private Button btnDelete;

    private WebEngine webEngine;
    private Role currentRole = Role.ADMIN; // Domyślna rola
    private Project currentProject;

    /**
     * Inicjalizacja danych po otwarciu widoku z menu
     */
    public void initData(Project project, Role role) {
        this.currentProject = project;
        if (role != null) {
            this.currentRole = role;
        }

        // Zawsze aktualizujemy uprawnienia na interfejsie użytkownika
        applyPermissions();

        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            loadProjectDataFromDatabase();
        }
    }

    public void setCurrentRole(Role role) {
        this.currentRole = role;
        applyPermissions();
    }

    public void setCurrentProject(Project project) {
        this.currentProject = project;
    }

    private void applyPermissions() {
        boolean isAdmin = (currentRole == Role.ADMIN);

        Platform.runLater(() -> {
            if (btnAddCamera != null) btnAddCamera.setDisable(!isAdmin);
            if (btnAddFiber != null) btnAddFiber.setDisable(!isAdmin);
            if (btnFinishFiber != null) btnFinishFiber.setDisable(!isAdmin);
            if (btnEditFiber != null) btnEditFiber.setDisable(!isAdmin);
            if (btnRename != null) btnRename.setDisable(!isAdmin);
            if (btnDelete != null) btnDelete.setDisable(!isAdmin);
        });
    }

    public class JavaBridge {
        public void syncElements(String data) {
            Platform.runLater(() -> {
                try {
                    elementList.getItems().clear();
                    if (data == null || data.trim().isEmpty()) return;

                    String[] items = data.split(";;");
                    for (String item : items) {
                        if (!item.trim().isEmpty()) {
                            elementList.getItems().add(item);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        public void showDetails(int id) {
            Platform.runLater(() -> openCameraDetailsDialog(id));
        }
    }

    @FXML
    public void initialize() {
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", new JavaBridge());

                    loadProjectDataFromDatabase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        URL mapUrl = getClass().getResource("/HTML/map.html");
        if (mapUrl == null) {
            mapUrl = getClass().getResource("/html/map.html");
        }

        if (mapUrl != null) {
            webEngine.load(mapUrl.toExternalForm());
        } else {
            System.err.println("BŁĄD: Nie znaleziono pliku map.html w katalogu resources!");
        }

        applyPermissions();
    }

    @FXML
    public void handleBackToMenu() {
        try {
            String[] possiblePaths = {
                    "/projects_view.fxml",
                    "/com/example/backend/projects_view.fxml",
                    "/fxml/projects_view.fxml",
                    "projects_view.fxml"
            };

            URL fxmlUrl = null;
            for (String path : possiblePaths) {
                fxmlUrl = getClass().getResource(path);
                if (fxmlUrl != null) break;
            }

            if (fxmlUrl == null) {
                throw new IllegalStateException("Nie odnaleziono pliku projects_view.fxml w katalogu resources.");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) webView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Menu Projektów");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Nie można otworzyć menu projektów");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadProjectDataFromDatabase() {
        if (currentProject == null) return;

        try (Connection conn = DatabaseConnector.getConnection()) {
            webEngine.executeScript("clearMap();");

            String camSql = "SELECT id, name, latitude AS lat, longitude AS lng, description FROM cameras WHERE project_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(camSql)) {
                stmt.setInt(1, currentProject.getId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name").replace("'", "\\'");
                    double lat = rs.getDouble("lat");
                    double lng = rs.getDouble("lng");
                    String desc = rs.getString("description");
                    desc = (desc == null) ? "" : desc.replace("'", "\\'").replace("\n", "\\n");

                    List<String> photos = new ArrayList<>();
                    try (PreparedStatement pStmt = conn.prepareStatement("SELECT photo_url FROM camera_photos WHERE camera_id = ?")) {
                        pStmt.setInt(1, id);
                        ResultSet pRs = pStmt.executeQuery();
                        while (pRs.next()) {
                            String url = pRs.getString("photo_url");
                            if (url != null) {
                                photos.add("\"" + url.replace("\\", "/") + "\"");
                            }
                        }
                    }
                    String photosJson = "[" + String.join(",", photos) + "]";

                    String script = String.format(Locale.US, "addCameraFromDb(%d, '%s', %.6f, %.6f, '%s', %s);",
                            id, name, lat, lng, desc, photosJson);
                    webEngine.executeScript(script);
                }
            }

            String fibSql = "SELECT id, name, color, weight FROM fibers WHERE project_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(fibSql)) {
                stmt.setInt(1, currentProject.getId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int fiberId = rs.getInt("id");
                    String name = rs.getString("name").replace("'", "\\'");
                    String color = rs.getString("color");
                    int weight = rs.getInt("weight");

                    StringBuilder pts = new StringBuilder("[");
                    try (PreparedStatement pStmt = conn.prepareStatement(
                            "SELECT lat, lng FROM fiber_points WHERE fiber_id = ? ORDER BY point_order ASC")) {
                        pStmt.setInt(1, fiberId);
                        ResultSet pRs = pStmt.executeQuery();
                        boolean first = true;
                        while (pRs.next()) {
                            if (!first) pts.append(",");
                            pts.append(String.format(Locale.US, "[%.6f, %.6f]", pRs.getDouble("lat"), pRs.getDouble("lng")));
                            first = false;
                        }
                    }
                    pts.append("]");

                    String script = String.format(Locale.US, "addFiberFromDb(%d, '%s', '%s', %d, %s);",
                            fiberId, name, color, weight, pts.toString());
                    webEngine.executeScript(script);
                }
            }

            refreshElementList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void handleSaveData() {
        if (currentProject == null || currentRole != Role.ADMIN) return;

        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement d1 = conn.prepareStatement("DELETE FROM cameras WHERE project_id = ?");
                 PreparedStatement d2 = conn.prepareStatement("DELETE FROM fibers WHERE project_id = ?")) {
                d1.setInt(1, currentProject.getId());
                d1.executeUpdate();
                d2.setInt(1, currentProject.getId());
                d2.executeUpdate();
            }

            Object rawCameras = webEngine.executeScript("getAllCamerasData();");
            if (rawCameras != null && !rawCameras.toString().equals("[]")) {
                String cameraSql = "INSERT INTO cameras (project_id, name, latitude, longitude, description) VALUES (?, ?, ?, ?, ?) RETURNING id";
                String photoSql = "INSERT INTO camera_photos (camera_id, photo_url) VALUES (?, ?)";

                Matcher m = Pattern.compile("\\{\"name\":\"(.*?)\",\"lat\":([\\d\\.-]+),\"lng\":([\\d\\.-]+),\"description\":\"(.*?)\",\"photos\":\\[(.*?)\\]\\}").matcher(rawCameras.toString());
                while (m.find()) {
                    int newCamId;
                    try (PreparedStatement stmt = conn.prepareStatement(cameraSql)) {
                        stmt.setInt(1, currentProject.getId());
                        stmt.setString(2, m.group(1));
                        stmt.setDouble(3, Double.parseDouble(m.group(2)));
                        stmt.setDouble(4, Double.parseDouble(m.group(3)));
                        stmt.setString(5, m.group(4).replace("\\n", "\n"));
                        ResultSet rs = stmt.executeQuery();
                        rs.next();
                        newCamId = rs.getInt(1);
                    }

                    String photosContent = m.group(5);
                    if (!photosContent.trim().isEmpty()) {
                        Matcher pMatcher = Pattern.compile("\"(.*?)\"").matcher(photosContent);
                        while (pMatcher.find()) {
                            try (PreparedStatement pStmt = conn.prepareStatement(photoSql)) {
                                pStmt.setInt(1, newCamId);
                                pStmt.setString(2, pMatcher.group(1).replace("\\\\", "/").replace("\\", "/"));
                                pStmt.executeUpdate();
                            }
                        }
                    }
                }
            }

            Object rawFibers = webEngine.executeScript("getAllFibersData();");
            if (rawFibers != null && !rawFibers.toString().equals("[]")) {
                String fiberSql = "INSERT INTO fibers (project_id, name, color, weight) VALUES (?, ?, ?, ?) RETURNING id";
                String pointSql = "INSERT INTO fiber_points (fiber_id, point_order, lat, lng) VALUES (?, ?, ?, ?)";

                Matcher m = Pattern.compile("\\{\"name\":\"(.*?)\",\"color\":\"(.*?)\",\"weight\":(\\d+),\"points\":\\[(.*?)\\]\\}").matcher(rawFibers.toString());
                while (m.find()) {
                    int newFiberId;
                    try (PreparedStatement stmt = conn.prepareStatement(fiberSql)) {
                        stmt.setInt(1, currentProject.getId());
                        stmt.setString(2, m.group(1));
                        stmt.setString(3, m.group(2));
                        stmt.setInt(4, Integer.parseInt(m.group(3)));
                        ResultSet rs = stmt.executeQuery();
                        rs.next();
                        newFiberId = rs.getInt(1);
                    }

                    String pointsContent = m.group(4);
                    Matcher ptMatcher = Pattern.compile("\\[([\\d\\.-]+),([\\d\\.-]+)\\]").matcher(pointsContent);
                    int order = 1;
                    while (ptMatcher.find()) {
                        try (PreparedStatement ptStmt = conn.prepareStatement(pointSql)) {
                            ptStmt.setInt(1, newFiberId);
                            ptStmt.setInt(2, order++);
                            ptStmt.setDouble(3, Double.parseDouble(ptMatcher.group(1)));
                            ptStmt.setDouble(4, Double.parseDouble(ptMatcher.group(2)));
                            ptStmt.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            loadProjectDataFromDatabase();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Zapis");
            alert.setHeaderText(null);
            alert.setContentText("Zapisano dane projektu w strukturze relacyjnej Supabase.");
            alert.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLoadData() {
        loadProjectDataFromDatabase();
    }

    private void openCameraDetailsDialog(int id) {
        Object rawData = webEngine.executeScript("getCameraData(" + id + ");");
        if (rawData == null) return;

        String name = "Kamera " + id;
        String currentDesc = "";
        List<String> photoPaths = new ArrayList<>();

        String json = rawData.toString();
        Matcher nameMatch = Pattern.compile("\"name\":\"(.*?)\"").matcher(json);
        if (nameMatch.find()) name = nameMatch.group(1);

        Matcher descMatch = Pattern.compile("\"description\":\"(.*?)\"").matcher(json);
        if (descMatch.find()) currentDesc = descMatch.group(1).replace("\\n", "\n");

        Matcher photosMatch = Pattern.compile("\"photos\":\\[(.*?)\\]").matcher(json);
        if (photosMatch.find()) {
            String pContent = photosMatch.group(1);
            Matcher pathMatcher = Pattern.compile("\"(.*?)\"").matcher(pContent);
            while (pathMatcher.find()) {
                photoPaths.add(pathMatcher.group(1).replace("\\\\", "/").replace("\\", "/"));
            }
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Szczegóły Kamery");
        dialog.setHeaderText("Kamera: " + name + " [ID: " + id + "]");
        dialog.setResizable(true);

        if (webView.getScene() != null) {
            dialog.initOwner(webView.getScene().getWindow());
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TextArea descArea = new TextArea(currentDesc);
        descArea.setPromptText("Wprowadź dodatkowe informacje / opis...");
        descArea.setPrefRowCount(3);
        descArea.setEditable(currentRole == Role.ADMIN);

        FlowPane photoPane = new FlowPane();
        photoPane.setHgap(10);
        photoPane.setVgap(10);

        for (String path : photoPaths) {
            addPhotoThumbnail(photoPane, path);
        }

        Button btnAddPhoto = new Button("📷 Dodaj zdjęcie");
        btnAddPhoto.setDisable(currentRole != Role.ADMIN);
        btnAddPhoto.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Wybierz zdjęcie kamery");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki graficzne", "*.png", "*.jpg", "*.jpeg"));
            File file = chooser.showOpenDialog(webView.getScene().getWindow());
            if (file != null) {
                String path = file.getAbsolutePath().replace("\\", "/");
                photoPaths.add(path);
                addPhotoThumbnail(photoPane, path);
            }
        });

        content.getChildren().addAll(
                new Label("Opis / Informacje dodatkowe:"),
                descArea,
                new Label("Załączone zdjęcia:"),
                photoPane,
                btnAddPhoto
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(500);

        if (currentRole == Role.ADMIN) {
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        } else {
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK && currentRole == Role.ADMIN) {
            String newDesc = descArea.getText().replace("\n", "\\n").replace("'", "\\'");

            StringBuilder jsonPhotos = new StringBuilder("[");
            for (int i = 0; i < photoPaths.size(); i++) {
                jsonPhotos.append("\"").append(photoPaths.get(i).replace("\\", "/")).append("\"");
                if (i < photoPaths.size() - 1) jsonPhotos.append(",");
            }
            jsonPhotos.append("]");

            webEngine.executeScript(String.format("updateCameraDetails(%d, '%s', %s);", id, newDesc, jsonPhotos.toString()));
        }
    }

    private void addPhotoThumbnail(FlowPane pane, String path) {
        try {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                ImageView imgView = new ImageView(new Image(imgFile.toURI().toString()));
                imgView.setFitWidth(100);
                imgView.setFitHeight(100);
                imgView.setPreserveRatio(true);
                pane.getChildren().add(imgView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshElementList() {
        Platform.runLater(() -> {
            try {
                Object result = webEngine.executeScript("getElementsListString();");
                if (result != null) {
                    elementList.getItems().clear();
                    String data = result.toString();
                    if (!data.trim().isEmpty()) {
                        String[] items = data.split(";;");
                        for (String item : items) {
                            if (!item.trim().isEmpty()) {
                                elementList.getItems().add(item);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void handleAddCamera() {
        if (currentRole != Role.ADMIN) return;
        TextInputDialog dialog = new TextInputDialog("Kamera 1");
        dialog.setTitle("Nowa Kamera");
        dialog.setHeaderText("Dodawanie punktu kamery");
        dialog.setContentText("Wprowadź nazwę kamery:");

        if (webView.getScene() != null) {
            dialog.initOwner(webView.getScene().getWindow());
        }

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(cameraName -> {
            String escapedName = cameraName.replace("'", "\\'");
            webEngine.executeScript(String.format("enableCameraPlacement('%s');", escapedName));
        });
    }

    @FXML
    public void handleAddFiber() {
        if (currentRole != Role.ADMIN) return;
        TextInputDialog dialog = new TextInputDialog("Światłowód Główny");
        dialog.setTitle("Nowy Światłowód");
        dialog.setHeaderText("Dodawanie trasy światłowodowej");
        dialog.setContentText("Wprowadź nazwę trasy:");

        if (webView.getScene() != null) {
            dialog.initOwner(webView.getScene().getWindow());
        }

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(fiberName -> {
            String escapedName = fiberName.replace("'", "\\'");
            webEngine.executeScript(String.format("enableFiberDrawing('%s');", escapedName));
        });
    }

    @FXML
    public void handleFinishFiber() {
        if (currentRole != Role.ADMIN) return;
        webEngine.executeScript("finishFiberDrawing();");
        refreshElementList();
    }

    @FXML
    public void handleDelete() {
        if (currentRole != Role.ADMIN) return;
        String selected = elementList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String id = extractId(selected);
            if (id != null) {
                webEngine.executeScript("deleteElement(" + id + ");");
                refreshElementList();
            }
        }
    }

    @FXML
    public void handleRename() {
        if (currentRole != Role.ADMIN) return;
        String selected = elementList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String id = extractId(selected);
            if (id != null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Zmień nazwę");
                dialog.setHeaderText("Edycja nazwy elementu");
                dialog.setContentText("Wprowadź nową nazwę:");

                if (webView.getScene() != null) {
                    dialog.initOwner(webView.getScene().getWindow());
                }

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(newName -> {
                    String escapedName = newName.replace("'", "\\'");
                    webEngine.executeScript("renameElement(" + id + ", '" + escapedName + "');");
                    refreshElementList();
                });
            }
        }
    }

    @FXML
    public void handleEditFiber() {
        if (currentRole != Role.ADMIN) return;
        String selected = elementList.getSelectionModel().getSelectedItem();
        if (selected != null && (selected.contains("Światłowód") || selected.contains("Swiatlowod"))) {
            String id = extractId(selected);
            if (id != null) {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edycja Światłowodu");
                dialog.setHeaderText("Ustaw parametry światłowodu");

                if (webView.getScene() != null) {
                    dialog.initOwner(webView.getScene().getWindow());
                }

                ButtonType confirmButtonType = new ButtonType("Zatwierdź", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);

                TextField colorField = new TextField("#e74c3c");
                TextField weightField = new TextField("6");

                grid.add(new Label("Kolor (HEX):"), 0, 0);
                grid.add(colorField, 1, 0);
                grid.add(new Label("Grubość (px):"), 0, 1);
                grid.add(weightField, 1, 1);

                dialog.getDialogPane().setContent(grid);

                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == confirmButtonType) {
                    String color = colorField.getText().trim();
                    String weight = weightField.getText().trim();
                    webEngine.executeScript(String.format("editFiberData(%s, '%s', %s);", id, color, weight));
                    refreshElementList();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informacja");
            alert.setHeaderText(null);
            alert.setContentText("Zaznacz światłowód z listy, aby go edytować.");
            alert.showAndWait();
        }
    }

    private String extractId(String item) {
        Pattern pattern = Pattern.compile("\\[(\\d+)\\]$");
        Matcher matcher = pattern.matcher(item);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
