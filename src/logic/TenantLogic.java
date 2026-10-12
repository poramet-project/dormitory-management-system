package logic;


import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Logic ของหน้าต่างหลักผู้เช่า (TenantUI) ไม่มีการสร้าง component Swing */
public class TenantLogic {

    /** ชื่อเมนูและรหัสหน้า เรียงตามลำดับเดียวกัน */
    public static final String[] MENU_NAMES = {
            "หน้าหลัก", "ห้องพักของฉัน", "สัญญาเช่า", "แจ้งซ่อม/ร้องเรียน", "ยื่นคำขอย้ายห้อง" };
    public static final String[] PAGE_KEYS = { "HOME", "ROOM", "CONTRACT", "REPORT", "TRANSFER" };

    private final LogicLogin.User user;

    public TenantLogic() {
        // หา user ปัจจุบัน (ห้ามให้ล้มเหลวจนหน้าต่างไม่เปิด)
        LogicLogin.User u = null;
        try {
            u = LogicLogin.getCurrentUser();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (u == null) {
            u = new LogicLogin.User("tenant", "1234", "กิตติพงษ์ รักสงบ", "0812345678", "TENANT", "R101");
        }
        this.user = u;
    }

    public LogicLogin.User user() {
        return user;
    }

    /** ชื่อที่แสดงบน top bar */
    public String displayName() {
        return orDefault(user.fullName, orDefault(user.username, "ผู้ใช้"));
    }

    /** ข้อความบทบาท/ห้องบน top bar */
    public String roleText() {
        return "ผู้อยู่อาศัย (ห้อง " + orDefault(user.roomId, "ไม่ระบุ") + ")";
    }

    public void logout() {
        try {
            LogicLogin.logout();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // ===================== ตัวช่วย =====================

    /** แปลงค่า null / ว่าง / "-" ให้เป็นข้อความที่แสดงผลได้ */
    public static String orDefault(String s, String fallback) {
        if (s == null)
            return fallback;
        String t = s.trim();
        return (t.isEmpty() || t.equals("-")) ? fallback : t;
    }

    public static String pickThaiFont() {
        String[] prefs = { "Sarabun", "Noto Sans Thai", "Leelawadee UI", "Leelawadee", "Thonburi", "Tahoma" };
        Set<String> avail = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String p : prefs)
            if (avail.contains(p))
                return p;
        return Font.DIALOG;
    }

    public static Image loadImage(String... names) {
        return ImageLoader.load(names);
    }

    public static String stackTraceOf(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
