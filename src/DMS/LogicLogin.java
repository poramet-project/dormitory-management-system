package DMS;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class LogicLogin {

    public static class User {
        public final String username, password, fullName, phone, role, roomId;

        public User(String username, String password, String fullName,
                    String phone, String role, String roomId) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.phone = phone;
            this.role = role;
            this.roomId = roomId;
        }
    }

    private static User currentUser;

    public static User getCurrentUser() { return currentUser; }
    public static void logout()         { currentUser = null; }

    private static final String[] CSV_PATHS = {
            "users.csv", "src/users.csv", "data/users.csv", "src/data/users.csv", "DMS/src/data/users.csv"
    };

    private static List<User> loadUsers() throws IOException {
        Path path = null;
        for (String p : CSV_PATHS) {
            if (Files.exists(Paths.get(p))) { path = Paths.get(p); break; }
        }
        if (path == null) {
            // หากยังไม่มีไฟล์ ให้สร้าง default ไว้
            Path defaultPath = Paths.get("users.csv");
            Files.write(defaultPath, "username,password,fullName,phone,role,roomId\n".getBytes(StandardCharsets.UTF_8));
            path = defaultPath;
        }

        List<User> users = new ArrayList<>();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).replace("\uFEFF", "").trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] c = line.split(",", -1);
            if (c.length < 2) continue;

            String u = c[0].trim();
            String p = c[1].trim();
            String name = (c.length > 2) ? c[2].trim() : "";
            String phone = (c.length > 3) ? c[3].trim() : "";
            String role = (c.length > 4 && !c[4].trim().isEmpty()) ? c[4].trim().toUpperCase() : "GUEST";
            String room = (c.length > 5) ? c[5].trim() : "";

            users.add(new User(u, p, name, phone, role, room));
        }
        return users;
    }

    public static User authenticate(String username, String password) throws IOException {
        for (User u : loadUsers()) {
            if (u.username.equalsIgnoreCase(username) && u.password.equals(password)) {
                return u;
            }
        }
        return null;
    }

    private static JFrame createUIByRole(String role) {
        switch (role) {
            case "ADMIN":     return new AdminUI();
            case "TENANT":    return new TenantUI();
            case "APPLICANT":
            case "GUEST":     return new GuestUI();
            default:          return new GuestUI(); // Fallback ถ้า role ไม่ตรง
        }
    }

    public static boolean login(JFrame current, String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(current, "Please enter Username and Password",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            User user = authenticate(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(current, "Username or Password is incorrect",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            JFrame next = createUIByRole(user.role);
            if (next == null) {
                JOptionPane.showMessageDialog(current, "Unknown role: " + user.role,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            currentUser = user;
            current.dispose();
            next.setVisible(true);
            return true;

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(current, "Failed to read user file: " + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}