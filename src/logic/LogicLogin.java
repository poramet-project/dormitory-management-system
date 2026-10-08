package logic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Logic การล็อกอิน (ตรวจสอบผู้ใช้จาก users.csv และเก็บผู้ใช้ปัจจุบัน) ไม่มี Swing */
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
    public static void setCurrentUser(User user) { currentUser = user; }
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

    /** แปลง role เป็นหน้าจอที่ต้องเปิด: "ADMIN", "TENANT" หรือ "GUEST" (APPLICANT/GUEST/อื่น ๆ) */
    public static String screenFor(String role) {
        switch (role) {
            case "ADMIN":     return "ADMIN";
            case "TENANT":    return "TENANT";
            case "APPLICANT":
            case "GUEST":
            default:          return "GUEST";
        }
    }
}
