package com.example.backend;

import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

public class UpdateService {

    private static final String VERSION_URL = "https://raw.githubusercontent.com/svwve/https://github.com/svwve/OpoleNetworkMap/main/version.txt";

    public static boolean isUpdateAvailable() {
        try {

            Properties props = new Properties();
            try (InputStream input = UpdateService.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) return false;
                props.load(input);
            }
            String localVersion = props.getProperty("app.version", "1.0.0");


            URI uri = URI.create(VERSION_URL);
            String remoteVersion;
            try (InputStream in = uri.toURL().openStream()) {
                remoteVersion = new String(in.readAllBytes()).trim();
            }


            return !localVersion.equals(remoteVersion);

        } catch (Exception e) {

            return false;
        }
    }
}
