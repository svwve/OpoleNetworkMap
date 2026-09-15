package com.example.backend;

import com.example.model.Role;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static final String FILE_PATH = "users.json";
    private static Map<String, User> userDatabase = new HashMap<>();

    static {
        loadUsers();
        // Domyślne konta na start, jeśli plik jeszcze nie istnieje
        if (userDatabase.isEmpty()) {
            registerUser("admin", "admin123", Role.ADMIN);
            registerUser("operator", "operator123", Role.OPERATOR);
        }
    }

    public static class User {
        private String username;
        private String password;
        private Role role;

        public User(String username, String password, Role role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public Role getRole() { return role; }
    }

    public static boolean registerUser(String username, String password, Role role) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        if (userDatabase.containsKey(username)) {
            return false; // Użytkownik już istnieje
        }

        userDatabase.put(username, new User(username, password, role));
        saveUsers();
        return true;
    }

    public static Role authenticate(String username, String password) {
        User user = userDatabase.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user.getRole();
        }
        return null;
    }

    private static void saveUsers() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH))) {
            StringBuilder json = new StringBuilder("[\n");
            int count = 0;
            for (User u : userDatabase.values()) {
                json.append(String.format("  {\"username\":\"%s\", \"password\":\"%s\", \"role\":\"%s\"}",
                        u.getUsername(), u.getPassword(), u.getRole()));
                if (++count < userDatabase.size()) json.append(",");
                json.append("\n");
            }
            json.append("]");
            writer.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadUsers() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try {
            String content = Files.readString(file.toPath());
            content = content.trim();
            if (content.startsWith("[") && content.endsWith("]")) {
                content = content.substring(1, content.length() - 1);
                String[] entries = content.split("},\\s*\\{");
                for (String entry : entries) {
                    entry = entry.replace("{", "").replace("}", "").replace("\"", "");
                    String[] pairs = entry.split(",");
                    String username = "", password = "";
                    Role role = Role.OPERATOR;

                    for (String pair : pairs) {
                        String[] kv = pair.split(":");
                        if (kv.length == 2) {
                            String key = kv[0].trim();
                            String value = kv[1].trim();
                            if (key.equals("username")) username = value;
                            if (key.equals("password")) password = value;
                            if (key.equals("role")) role = Role.valueOf(value);
                        }
                    }

                    if (!username.isEmpty()) {
                        userDatabase.put(username, new User(username, password, role));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

