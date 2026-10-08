package DMS;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class LogicLogin {

    public static class User {
        public final String userId, username, password, fullName, phone, role, roomId;

        public User(String userId, String username, String password, String fullName,
                    String phone, String role, String roomId) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.phone = phone;
            this.role = role;
            this.roomId = roomId;
        }

        // คอนสตรัคเตอร์เดิม (6 พารามิเตอร์) เพื่อให้โค้ดเก่า เช่น TenantUI fallback ยังคอมไพล์ได้
        public User(String username, String password, String fullName,
                    String phone, String role, String roomId) {
            this("", username, password, fullName, phone, role, roomId);
        }
    }

    private static User currentUser;

    public static User getCurrentUser() { return currentUser; }
    public static void logout()         { currentUser = null; }

    private static final String HEADER = "userId,username,password,fullName,phone,role,roomId";

    private static final String[] CSV_PATHS = {
            "users.csv", "src/users.csv", "data/users.csv", "src/data/users.csv", "DMS/src/data/users.csv"
    };

    /** "-" หรือค่าว่าง ถือว่าไม่มีห้อง (คืนค่าเป็น "" เพื่อให้ UI เช็ค isEmpty() ได้) */
    private static String normalizeRoom(String room) {
        if (room == null) return "";
        room = room.trim();
        return room.equals("-") ? "" : room;
    }

    private static List<User> loadUsers() throws IOException {
        Path path = null;
        for (String p : CSV_PATHS) {
            if (Files.exists(Paths.get(p))) { path = Paths.get(p); break; }
        }
        if (path == null) {
            // หากยังไม่มีไฟล์ ให้สร้างไฟล์เปล่าพร้อม header
            Path defaultPath = Paths.get("users.csv");
            Files.write(defaultPath, (HEADER + "\n").getBytes(StandardCharsets.UTF_8));
            path = defaultPath;
        }

        List<User> users = new ArrayList<>();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) return users;

        // ตรวจรูปแบบไฟล์จาก header: ถ้าขึ้นต้นด้วย userId = รูปแบบใหม่ (มี userId คอลัมน์แรก)
        String headerLine = lines.get(0).replace("\uFEFF", "").trim().toLowerCase();
        int off = headerLine.startsWith("userid") ? 1 : 0;

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).replace("\uFEFF", "").trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] c = line.split(",", -1);
            if (c.length < off + 2) continue;

            String id    = (off == 1) ? c[0].trim() : "";
            String u     = c[off].trim();
            String p     = c[off + 1].trim();
            String name  = (c.length > off + 2) ? c[off + 2].trim() : "";
            String phone = (c.length > off + 3) ? c[off + 3].trim() : "";
            String role  = (c.length > off + 4 && !c[off + 4].trim().isEmpty())
                    ? c[off + 4].trim().toUpperCase() : "GUEST";
            String room  = (c.length > off + 5) ? normalizeRoom(c[off + 5]) : "";

            users.add(new User(id, u, p, name, phone, role, room));
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

            // ต้องตั้ง currentUser ก่อนสร้างหน้า UI เพราะ TenantUI/GuestUI ฯลฯ
            // เรียก LogicLogin.getCurrentUser() ภายใน constructor
            currentUser = user;

            JFrame next;
            try {
                next = createUIByRole(user.role);
            } catch (RuntimeException ex) {
                currentUser = null;
                ex.printStackTrace();
                JOptionPane.showMessageDialog(current, "Failed to open main screen: " + ex,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (next == null) {
                currentUser = null;
                JOptionPane.showMessageDialog(current, "Unknown role: " + user.role,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

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