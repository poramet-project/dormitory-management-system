package logic;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * TenantLogic คือคลาสสำหรับจัดการตรรกะภาพรวมและเมนูนำทางของหน้าต่างหลักผู้เช่า
 * (TenantUI)
 * MENU_NAMES คือ รายชื่อเมนูทั้งหมดในฝั่งผู้เช่า
 * PAGE_KEYS คือ คีย์ระบุหน้าสำหรับการสลับหน้าด้วย CardLayout
 * user คือ ข้อมูลบัญชีผู้เช่าปัจจุบัน
 */
public class TenantLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF(MENU_NAMES, PAGE_KEYS, user) คือ
    // ตรรกะโครงสร้างเมนูและการจัดการเซสชันของผู้เช่า
    // RI
    // 1. MENU_NAMES และ PAGE_KEYS ต้องมีจำนวนสมาชิกเท่ากัน
    // 2. user ต้องไม่เป็น null

    /** ชื่อเมนูและรหัสหน้า เรียงตามลำดับเดียวกัน */
    public static final String[] MENU_NAMES = {
            "หน้าหลัก", "ห้องพักของฉัน", "สัญญาเช่า", "แจ้งซ่อม/ร้องเรียน", "ยื่นคำขอย้ายห้อง" };
    public static final String[] PAGE_KEYS = { "HOME", "ROOM", "CONTRACT", "REPORT", "TRANSFER" };

    private final LoginLogic.User user;

    /**
     * คอนสตรักเตอร์สำหรับสร้างตรรกะหน้าต่างหลักผู้เช่า
     * โดยดึงข้อมูลผู้ใช้ปัจจุบันหรือข้อมูลจำลอง
     */
    public TenantLogic() {
        // หา user ปัจจุบัน (ห้ามให้ล้มเหลวจนหน้าต่างไม่เปิด)
        LoginLogic.User u = null;
        try {
            u = LoginLogic.getCurrentUser();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (u == null) {
            u = new LoginLogic.User("tenant", "1234", "กิตติพงษ์ รักสงบ", "0812345678", "TENANT", "R101");
        }
        this.user = u;
    }

    /**
     * ดึงข้อมูลบัญชีผู้เช่าปัจจุบัน
     * 
     * @return ออบเจ็กต์ User ของผู้เช่า
     */
    public LoginLogic.User user() {
        return user;
    }

    /**
     * ดึงชื่อสำหรับแสดงผลบนแถบด้านบน (Top bar)
     * 
     * @return ชื่อ-นามสกุล หรือชื่อผู้ใช้งาน
     */
    public String displayName() {
        return orDefault(user.fullName, orDefault(user.username, "ผู้ใช้"));
    }

    /**
     * ดึงข้อความระบุบทบาทและหมายเลขห้องพักสำหรับแสดงผลบนแถบด้านบน
     * 
     * @return ข้อความบทบาท เช่น "ผู้อยู่อาศัย (ห้อง 101)"
     */
    public String roleText() {
        String rId = (user != null) ? user.roomId : null;
        if (user != null && !DataStore.blank(user.username)) {
            LoginLogic.User latest = LoginLogic.findUser(user.username);
            if (latest != null && !DataStore.blank(latest.roomId)) {
                rId = latest.roomId;
            }
        }
        if (rId != null && !"-".equals(rId.trim())) {
            DataStore.Room r = DataStore.room(rId);
            if (r == null)
                r = DataStore.roomByNumber(rId);
            if (r != null)
                return "ผู้อยู่อาศัย (ห้อง " + r.number + ")";
        }
        return "ผู้อยู่อาศัย (ห้อง " + orDefault(rId, "ไม่ระบุ") + ")";
    }

    /**
     * ออกจากระบบ โดยล้างสถานะ Session ของผู้ใช้งาน
     */
    public void logout() {
        try {
            LoginLogic.logout();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // ===================== ตัวช่วย =====================

    /**
     * คืนค่าข้อความเดิม หรือคืนค่าข้อความสำรองหากข้อความเดิมเป็นค่าว่างหรือ "-"
     * 
     * @param s        ข้อความต้นทาง
     * @param fallback ข้อความสำรองกรณีเป็นค่าว่าง
     * @return ข้อความที่พร้อมแสดงผล
     */
    public static String orDefault(String s, String fallback) {
        if (s == null)
            return fallback;
        String t = s.trim();
        return (t.isEmpty() || t.equals("-")) ? fallback : t;
    }

    /**
     * เลือกฟอนต์ภาษาไทยที่ติดตั้งอยู่ในระบบตามลำดับความสวยงามที่ต้องการ
     * 
     * @return ชื่อฟอนต์ที่พบในระบบ หรือ Font.DIALOG
     */
    public static String pickThaiFont() {
        String[] prefs = { "Sarabun", "Noto Sans Thai", "Leelawadee UI", "Leelawadee", "Thonburi", "Tahoma" };
        Set<String> avail = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String p : prefs)
            if (avail.contains(p))
                return p;
        return Font.DIALOG;
    }

    /**
     * โหลดรูปภาพจากระบบไฟล์หรือ Classpath ผ่าน ImageLoader
     * 
     * @param names รายชื่อไฟล์หรือพาธของรูปภาพ
     * @return ออบเจ็กต์ Image ที่โหลดสำเร็จ หรือ null
     */
    public static Image loadImage(String... names) {
        return ImageLoader.load(names);
    }

    /**
     * แปลง StackTrace ของข้อยกเว้น (Throwable) ให้เป็นสตริงข้อความ
     * 
     * @param t ออบเจ็กต์ Throwable
     * @return ข้อความ StackTrace แบบเต็ม
     */
    public static String stackTraceOf(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
