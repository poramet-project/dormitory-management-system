package logic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * LoginLogic คือคลาสสำหรับจัดการระบบยืนยันตัวตนและการเข้าสู่ระบบ (Authentication) ของผู้ใช้
 * currentUser คือ บัญชีผู้ใช้งานที่กำลังเข้าสู่ระบบอยู่ในปัจจุบัน (Session)
 * HEADER คือ ส่วนหัวคอลัมน์ของไฟล์ users.csv
 * CSV_PATHS คือ รายการพาธไฟล์ users.csv ที่เป็นไปได้
 */
public class LoginLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF(currentUser) คือ ระบบจัดการ Session และยืนยันตัวตนของผู้ใช้กับไฟล์ users.csv
    // RI
    // 1. CSV_PATHS ต้องไม่เป็น null และมีขนาดอย่างน้อย 1 รายการ
    // 2. หาก currentUser ไม่เป็น null ต้องมี username และ role ระบุอยู่

    /**
     * User คือคลาสสำหรับเก็บข้อมูลของผู้ใช้งานในระบบ
     * userId คือ รหัสประจำตัวผู้ใช้ (เช่น U001)
     * username คือ ชื่อบัญชีผู้ใช้สำหรับเข้าสู่ระบบ
     * password คือ รหัสผ่าน
     * fullName คือ ชื่อและนามสกุลจริง
     * phone คือ หมายเลขโทรศัพท์
     * role คือ บทบาทหน้าที่ในระบบ ("ADMIN", "TENANT", "GUEST")
     * roomId คือ รหัสห้องพักที่ผูกกับผู้ใช้ (หรือ "-" หากไม่มีห้องพัก)
     */
    public static class User {

        // ===== representation =====
        // Abstraction Function:
        // AF(userId, username, password, fullName, phone, role, roomId) คือ บัญชีข้อมูลผู้ใช้งาน 1 คนในระบบ
        // RI
        // 1. username, password, fullName, role ต้องไม่เป็น null
        // 2. role ต้องเป็น ADMIN, TENANT, หรือ GUEST

        public final String userId, username, password, fullName, phone, role, roomId;

        /**
         * คอนสตรักเตอร์สำหรับสร้างข้อมูลผู้ใช้ (แบบระบุ userId)
         * 
         * @param userId รหัสประจำตัวผู้ใช้
         * @param username ชื่อบัญชีผู้ใช้
         * @param password รหัสผ่าน
         * @param fullName ชื่อและนามสกุลจริง
         * @param phone หมายเลขโทรศัพท์
         * @param role บทบาทในระบบ
         * @param roomId รหัสห้องพักที่ผูกกับผู้ใช้
         */
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

        /**
         * คอนสตรัคเตอร์สำหรับสร้างข้อมูลผู้ใช้ (แบบ 6 พารามิเตอร์ดั้งเดิม)
         * 
         * @param username ชื่อบัญชีผู้ใช้
         * @param password รหัสผ่าน
         * @param fullName ชื่อและนามสกุลจริง
         * @param phone หมายเลขโทรศัพท์
         * @param role บทบาทในระบบ
         * @param roomId รหัสห้องพักที่ผูกกับผู้ใช้
         */
        public User(String username, String password, String fullName,
                    String phone, String role, String roomId) {
            this("", username, password, fullName, phone, role, roomId);
        }
    }

    private static User currentUser;

    /**
     * ดึงข้อมูลผู้ใช้งานที่กำลังเข้าสู่ระบบอยู่ในปัจจุบัน (Session)
     * 
     * @return ออบเจ็กต์ User หรือ null หากยังไม่ได้เข้าสู่ระบบ
     */
    public static User getCurrentUser() { return currentUser; }

    /**
     * กำหนดข้อมูลผู้ใช้งานที่กำลังเข้าสู่ระบบในปัจจุบัน
     * 
     * @param user ออบเจ็กต์ User ที่เข้าสู่ระบบ
     */
    public static void setCurrentUser(User user) { currentUser = user; }

    /**
     * ดำเนินการออกจากระบบ โดยล้างข้อมูล Session ของผู้ใช้ปัจจุบัน
     */
    public static void logout()         { currentUser = null; }

    private static final String HEADER = "userId,username,password,fullName,phone,role,roomId";

    private static final String[] CSV_PATHS = {
            "users.csv", "src/users.csv", "data/users.csv", "src/data/users.csv", "DMS/src/data/users.csv"
    };

    /**
     * จัดการแปลงรหัสห้องพักให้เป็นรูปแบบมาตรฐาน (ค่าว่างหรือ "-" จะแปลงเป็น "")
     * 
     * @param room สตริงรหัสห้องพัก
     * @return รหัสห้องที่ผ่านการแปลง
     */
    private static String normalizeRoom(String room) {
        if (room == null) return "";
        room = room.trim();
        return room.equals("-") ? "" : room;
    }

    /**
     * โหลดรายชื่อผู้ใช้งานทั้งหมดจากไฟล์ users.csv
     * 
     * @return รายการออบเจ็กต์ User ทั้งหมด
     * @throws IOException หากเกิดข้อผิดพลาดในการอ่านไฟล์
     */
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

    /**
     * ตรวจสอบและยืนยันตัวตนของผู้ใช้งานด้วยชื่อบัญชีและรหัสผ่าน
     * 
     * @param username ชื่อบัญชีผู้ใช้
     * @param password รหัสผ่าน
     * @return ออบเจ็กต์ User หากข้อมูลถูกต้อง หรือ null หากไม่ถูกต้อง
     * @throws IOException หากเกิดข้อผิดพลาดในการอ่านไฟล์ข้อมูล
     */
    public static User authenticate(String username, String password) throws IOException {
        for (User u : loadUsers()) {
            if (u.username.equalsIgnoreCase(username) && u.password.equals(password)) {
                return u;
            }
        }
        return null;
    }

    /**
     * มอบหมายห้องพักให้ผู้ใช้ ปรับบทบาทเป็น TENANT และอัปเดตไฟล์ users.csv พร้อม Session
     * 
     * @param userId รหัสประจำตัวผู้ใช้
     * @param roomId รหัสห้องพักที่มอบหมาย
     * @return true หากบันทึกสำเร็จ
     */
    public static synchronized boolean assignRoom(String userId, String roomId) {
        try {
            Path path = null;
            for (String p : CSV_PATHS) {
                if (Files.exists(Paths.get(p))) { path = Paths.get(p); break; }
            }
            if (path == null) return false;

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (lines.isEmpty()) return false;

            String headerLine = lines.get(0).replace("\uFEFF", "").trim().toLowerCase();
            int off = headerLine.startsWith("userid") ? 1 : 0;

            boolean changed = false;
            List<String> out = new ArrayList<>();
            User updated = null;
            for (int i = 0; i < lines.size(); i++) {
                String raw = lines.get(i);
                String line = raw.replace("\uFEFF", "").trim();
                if (i == 0 || line.isEmpty() || line.startsWith("#")) { out.add(raw); continue; }

                String[] c = line.split(",", -1);
                boolean mine = (off == 1)
                        ? c[0].trim().equalsIgnoreCase(userId)
                        : c[0].trim().equalsIgnoreCase(userId);   // รูปแบบเก่า: คอลัมน์แรกคือ username
                if (!mine || c.length < off + 2) { out.add(raw); continue; }

                // ขยายให้ครบคอลัมน์ role/roomId
                String[] n = new String[Math.max(c.length, off + 6)];
                for (int k = 0; k < n.length; k++) n[k] = k < c.length ? c[k] : "";
                n[off + 4] = "TENANT";
                n[off + 5] = roomId;
                out.add(String.join(",", n));
                changed = true;
                updated = new User(n[0].trim().isEmpty() || off == 0 ? "" : n[0].trim(),
                        n[off].trim(), n[off + 1].trim(), n[off + 2].trim(),
                        n[off + 3].trim(), "TENANT", roomId);
            }
            if (!changed) return false;

            Files.write(path, (String.join(System.lineSeparator(), out) + System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8));
            // อัปเดตผู้ใช้ปัจจุบันเฉพาะเมื่อเป็นคนเดียวกัน (กรณี admin อนุมัติให้คนอื่นต้องไม่ทับ session ของ admin)
            if (updated != null && currentUser != null
                    && (userId.equalsIgnoreCase(currentUser.userId) || userId.equalsIgnoreCase(currentUser.username))) {
                currentUser = updated;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ค้นหาข้อมูลผู้ใช้งานจาก userId หรือ username
     * 
     * @param idOrUsername รหัสประจำตัวผู้ใช้หรือชื่อบัญชี
     * @return ออบเจ็กต์ User ที่พบ หรือ null หากไม่พบ
     */
    public static User findUser(String idOrUsername) {
        if (idOrUsername == null) return null;
        try {
            for (User u : loadUsers()) {
                if (idOrUsername.equalsIgnoreCase(u.userId) || idOrUsername.equalsIgnoreCase(u.username)) return u;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * แปลงบทบาท (role) เป็นชื่อหน้าจอหลักที่ต้องเปิดแสดงผล ("ADMIN", "TENANT" หรือ "GUEST")
     * 
     * @param role ชื่อบทบาท เช่น "ADMIN", "TENANT", "GUEST", "APPLICANT"
     * @return ชื่อหน้าจอเป้าหมาย
     */
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