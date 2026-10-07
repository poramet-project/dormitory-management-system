package DMS;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Logic การ Login: อ่านผู้ใช้จาก users.csv, ตรวจสอบรหัสผ่าน
 * แล้ว new หน้า UI ตามบทบาท (role) ของผู้ใช้
 *
 * รูปแบบไฟล์ users.csv:
 * username,password,fullName,phone,role,roomId
 */
public class LogicLogin {

    // ---------- ข้อมูลผู้ใช้ ----------
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

    // ผู้ใช้ที่ล็อกอินอยู่ (เรียกใช้จากหน้า UI อื่นได้ เช่น LogicLogin.getCurrentUser())
    private static User currentUser;

    public static User getCurrentUser() { return currentUser; }
    public static void logout()         { currentUser = null; }

    // ตำแหน่งที่จะลองหาไฟล์ users.csv ตามลำดับ
    private static final String[] CSV_PATHS = {
            "users.csv", "src/users.csv", "data/users.csv", "src/data/users.csv"
    };

    // ---------- อ่านไฟล์ CSV ----------
    private static List<User> loadUsers() throws IOException {
        Path path = null;
        for (String p : CSV_PATHS) {
            if (Files.exists(Paths.get(p))) { path = Paths.get(p); break; }
        }
        if (path == null) throw new FileNotFoundException("ไม่พบไฟล์ users.csv");

        List<User> users = new ArrayList<>();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        for (int i = 1; i < lines.size(); i++) {           // ข้ามบรรทัด header
            String line = lines.get(i).replace("\uFEFF", "").trim();
            if (line.isEmpty()) continue;

            String[] c = line.split(",", -1);
            if (c.length < 6) continue;                    // ข้ามบรรทัดที่ข้อมูลไม่ครบ

            users.add(new User(c[0].trim(), c[1].trim(), c[2].trim(),
                               c[3].trim(), c[4].trim().toUpperCase(), c[5].trim()));
        }
        return users;
    }

    // ---------- ตรวจสอบ username / password ----------
    public static User authenticate(String username, String password) throws IOException {
        for (User u : loadUsers()) {
            if (u.username.equals(username) && u.password.equals(password)) {
                return u;
            }
        }
        return null; // ไม่พบ หรือรหัสผ่านผิด
    }

    // ---------- new หน้าตามบทบาท ----------
    private static JFrame createUIByRole(String role) {
        switch (role) {
            case "ADMIN":     return new AdminUI();
            case "TENANT":    return new TenantUI();
            case "APPLICANT":                 // ผู้สมัครเข้าพัก ใช้หน้า Guest
            case "GUEST":     return new GuestUI();
            default:          return null;
        }
    }

    /**
     * เรียกจากปุ่ม Log in
     * @param current หน้า LoginUI ปัจจุบัน (จะถูกปิดเมื่อล็อกอินสำเร็จ)
     * @return true ถ้าล็อกอินสำเร็จ
     */
    public static boolean login(JFrame current, String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(current, "กรุณากรอก Username และ Password",
                    "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            User user = authenticate(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(current, "Username หรือ Password ไม่ถูกต้อง",
                        "เข้าสู่ระบบไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            JFrame next = createUIByRole(user.role);
            if (next == null) {
                JOptionPane.showMessageDialog(current, "ไม่รู้จักบทบาท: " + user.role,
                        "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            currentUser = user;
            current.dispose();          // ปิดหน้า Login
            next.setVisible(true);      // เปิดหน้าตามบทบาท
            return true;

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(current, "อ่านไฟล์ผู้ใช้ไม่ได้: " + ex.getMessage(),
                    "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}