package com.example.backend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class UpdateService {

    // Adres zawsze wskazujący na najnowsze wydanie (latest) obsługiwane przez GitHub Actions
    private static final String DOWNLOAD_URL = "https://github.com/svwve/OpoleNetworkMap/releases/latest/download/OpoleNetworkMap.jar";
    private static final String VERSION_FILE_URL = "https://raw.githubusercontent.com/svwve/OpoleNetworkMap/main/versions.txt";

    public boolean checkForUpdates(String currentVersion) {
        try {
            URL url = new URL(VERSION_FILE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                if (scanner.hasNextLine()) {
                    String remoteVersion = scanner.nextLine().trim();
                    return !remoteVersion.equals(currentVersion);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void downloadAndInstallUpdate() {
        Thread updateThread = new Thread(() -> {
            try {
                System.out.println("Pobieranie nowej wersji...");

                URL url = new URL(DOWNLOAD_URL.trim());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // GitHub często używa przekierowań, warto je jawnie włączyć
                connection.setInstanceFollowRedirects(true);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Path targetPath = Paths.get("OpoleNetworkMap-updated.jar");

                    try (InputStream in = connection.getInputStream();
                         FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {

                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    System.out.println("Aktualizacja została pomyślnie pobrana do: " + targetPath.toAbsolutePath());
                } else {
                    System.err.println("Błąd pobierania: Serwer zwrócił kod " + responseCode + ". Upewnij się, że plik OpoleNetworkMap.jar istnieje w zakładce Releases na GitHubie.");
                }

            } catch (Exception e) {
                System.err.println("Wystąpił błąd podczas pobierania aktualizacji: " + e.getMessage());
            }
        });
        // Oznacz jako demon, aby wątek nie blokował wyłączenia aplikacji
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public static String getLocalVersion() {
        return "1.0.0";
    }

    public static boolean isUpdateAvailable() {
        return true;
    }

}
