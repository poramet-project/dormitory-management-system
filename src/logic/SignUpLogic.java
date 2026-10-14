package logic;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Logic ของหน้าสมัครสมาชิก: ตรวจข้อมูลและบันทึกลง users.csv (ไม่มี Swing) */
public class SignUpLogic {

    public static final String PH_USER = "Username";
    public static final String PH_PASS = "Password";
    public static final String PH_NAME = "Fullname";
    public static final String PH_PHONE = "Phone";

    // คอลัมน์: userId(0), username(1), password(2), fullName(3), phone(4), role(5), roomId(6)
    private static final String HEADER = "userId,username,password,fullName,phone,role,roomId";
    private static final String ID_PREFIX = "U";
    private static final int ID_DIGITS = 3;
    private static final String DEFAULT_ROLE = "GUEST";
    private static final String NO_ROOM = "-";

    private SignUpLogic() { }

    /** ตรวจสอบว่าข้อความมีตัวอักษรภาษาไทยปนอยู่หรือไม่ */
    private static boolean containsThai(String text) {
        return text != null && text.matches(".*[\\u0E00-\\u0E7F].*");
    }

    /**
     * ตรวจสอบข้อมูลและบันทึกผู้ใช้ใหม่
     *
     * @throws NumberFormatException    เบอร์โทรไม่ถูกต้อง
     * @throws IllegalArgumentException ข้อมูลไม่ครบ/ไม่ถูกต้อง
     * @throws IllegalStateException    username ซ้ำ
     * @throws IOException              บันทึกไฟล์ไม่สำเร็จ
     */
    public static void register(String u, String p, String n, String ph) throws IOException {
        // 1. ตรวจสอบค่าว่างหรือ Placeholder
        if (u.isEmpty() || u.equals(PH_USER)) {
            throw new IllegalArgumentException("Please enter a username");
        }
        if (p.isEmpty() || p.equals(PH_PASS)) {
            throw new IllegalArgumentException("Please enter a password");
        }
        if (p.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long");
        }
        if (n.isEmpty() || n.equals(PH_NAME)) {
            throw new IllegalArgumentException("Please enter your full name");
        }
        if (ph.isEmpty() || ph.equals(PH_PHONE)) {
            throw new IllegalArgumentException("Please enter your phone number");
        }

        // 2. ดักจับภาษาไทย (ไม่อนุญาตให้มีภาษาไทยในระบบ)
        if (containsThai(u)) {
            throw new IllegalArgumentException("Username cannot contain Thai characters (English only)");
        }
        if (containsThai(p)) {
            throw new IllegalArgumentException("Password cannot contain Thai characters");
        }
        if (containsThai(n)) {
            throw new IllegalArgumentException("Full name must be in English only (No Thai characters)");
        }

        // 2.1 ห้ามมีเครื่องหมายจุลภาค (,) เพราะ CSV ใช้ , คั่นคอลัมน์
        if (u.contains(",") || p.contains(",") || n.contains(",")) {
            throw new IllegalArgumentException("Username, password and full name cannot contain a comma (,)");
        }

        // 3. ตรวจสอบเบอร์โทรศัพท์ (ตัวเลข 9-10 หลัก เริ่มต้นด้วย 0)
        if (!ph.matches("^0[0-9]{8,9}$")) {
            throw new NumberFormatException("Invalid phone number (must be 9-10 digits and start with 0)");
        }

        // 4. เตรียมไฟล์ (แปลงไฟล์เก่าให้มีคอลัมน์ userId) แล้วตรวจ Username ซ้ำ
        File csvFile = resolveCsvFile();
        ensureUserIdColumn(csvFile);
        if (isUsernameTaken(csvFile, u)) {
            throw new IllegalStateException("This username is already taken. Please choose another one.");
        }

        // 5. สร้าง userId ถัดไป แล้วบันทึกทันที (7 คอลัมน์ ด้วย UTF-8)
        String userId = nextUserId(csvFile);
        saveUserToCsv(csvFile, userId, u, p, n, ph, DEFAULT_ROLE, NO_ROOM);
    }

    // ==================================================================
    //  ส่วนจัดการไฟล์ users.csv
    // ==================================================================

    private static File resolveCsvFile() {
        String[] paths = {
            "users.csv",
            "src/users.csv",
            "data/users.csv",
            "src/data/users.csv",
            "DMS/src/data/users.csv"
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) return f;
        }
        return new File("users.csv");
    }

    /**
     * ถ้าไฟล์เก่ายังไม่มีคอลัมน์ userId ให้เติมให้อัตโนมัติ (ทำครั้งเดียว)
     * - เพิ่ม userId (U001, U002, ...) ตามลำดับแถว
     * - roomId ที่ว่าง จะถูกแทนด้วย "-"
     * - ลบบรรทัดว่างทิ้ง
     */
    private static void ensureUserIdColumn(File file) throws IOException {
        if (!file.exists() || file.length() == 0) return;

        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        List<String> out = new ArrayList<>();
        boolean headerSeen = false;
        int n = 1;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (!headerSeen) {
                headerSeen = true;
                if (line.startsWith("userId,")) return; // เป็นไฟล์รูปแบบใหม่แล้ว
                out.add(HEADER);
                continue;
            }

            String[] c = line.split(",", -1);
            if (c.length > 0 && c[c.length - 1].trim().isEmpty()) {
                c[c.length - 1] = NO_ROOM;
            }
            out.add(String.format("%s%0" + ID_DIGITS + "d,%s", ID_PREFIX, n++, String.join(",", c)));
        }
        Files.write(file.toPath(), out, StandardCharsets.UTF_8);
    }

    /** สร้าง userId ถัดไป เช่น U001, U002, ... โดยหาเลขสูงสุดในไฟล์แล้วบวก 1 */
    private static String nextUserId(File file) throws IOException {
        int max = 0;
        if (file.exists()) {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String id = line.split(",", -1)[0].trim();
                if (id.matches(ID_PREFIX + "\\d+")) {
                    max = Math.max(max, Integer.parseInt(id.substring(ID_PREFIX.length())));
                }
            }
        }
        return String.format("%s%0" + ID_DIGITS + "d", ID_PREFIX, max + 1);
    }

    /** ตรวจ username ซ้ำ (username อยู่คอลัมน์ index 1 เพราะ index 0 คือ userId) */
    private static boolean isUsernameTaken(File file, String username) throws IOException {
        if (!file.exists()) return false;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("userId,")) continue;
                String[] cols = line.split(",", -1);
                if (cols.length > 1 && cols[1].trim().equalsIgnoreCase(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void saveUserToCsv(File file, String userId, String u, String p,
                                      String name, String phone, String role, String roomId) throws IOException {
        boolean hasContent = file.exists() && file.length() > 0;
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            if (!hasContent) {
                writer.write(HEADER);
                writer.newLine();
            } else if (!endsWithNewline(file)) {
                writer.newLine(); // กันแถวใหม่ไปต่อท้ายแถวเก่า
            }
            writer.write(String.join(",", userId, u, p, name, phone, role, roomId));
            writer.newLine();
        }
    }

    /** เช็คว่าตัวอักษรสุดท้ายของไฟล์เป็นขึ้นบรรทัดใหม่หรือไม่ */
    private static boolean endsWithNewline(File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            if (raf.length() == 0) return true;
            raf.seek(raf.length() - 1);
            int last = raf.read();
            return last == '\n' || last == '\r';
        }
    }
}
