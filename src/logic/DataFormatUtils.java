package logic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DataFormatUtils คือคลาสยูทิลิตี้สำหรับจัดรูปแบบข้อมูล
 * แสดงผลวันที่และเวลาภาษาไทย และเปรียบเทียบข้อมูล
 */
public final class DataFormatUtils {

    // ===== representation =====
    // Abstraction Function:
    // AF() คือ ตัวช่วยจัดรูปแบบข้อความ วันที่ และตัวเลขในระบบ
    // RI
    // - ไม่มีตัวแปรสถานะภายใน

    private DataFormatUtils() {
    }

    private static final String[] TH_MONTHS = { "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
            "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค." };

    /**
     * ตรวจสอบว่าข้อความเป็นค่าว่าง null หรือมีเพียงเครื่องหมายขีด "-" หรือไม่
     * 
     * @param s ข้อความที่ต้องการตรวจสอบ
     * @return true หากเป็นค่าว่างหรือไม่มีข้อมูลจริง
     */
    public static boolean blank(String s) {
        if (s == null)
            return true;
        String t = s.trim();
        return t.isEmpty() || t.equals("-");
    }

    /**
     * แปลงสตริงตัวเลขเป็นค่าจำนวนเต็ม int โดยตัดเครื่องหมายจุลภาคออก
     * 
     * @param s ข้อความตัวเลข
     * @return ค่าตัวเลขจำนวนเต็ม หรือ 0 หากไม่สามารถแปลงได้
     */
    public static int toInt(String s) {
        try {
            return Integer.parseInt(s.trim().replaceAll("[,]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * จัดรูปแบบจำนวนเงินเป็นสตริงที่มีเครื่องหมายจุลภาคคั่นหลักพัน
     * 
     * @param v จำนวนเงิน
     * @return ข้อความจำนวนเงิน เช่น "4,500"
     */
    public static String money(int v) {
        return String.format("%,d", v);
    }

    /**
     * แปลงสตริงวันที่สากลหรือรูปแบบต่างๆ ให้เป็นวันที่ภาษาไทยพร้อมปี พ.ศ.
     * 
     * @param raw ข้อความวันที่ต้นทาง เช่น "2026-10-16"
     * @return วันที่ภาษาไทย เช่น "16 ต.ค. 2569" หรือ "-" หากไม่มีข้อมูล
     */
    public static String thaiDate(String raw) {
        if (raw == null) {
            return "-";
        }
        String s = raw.trim();
        if (s.isEmpty() || "-".equals(s)) {
            return "-";
        }

        // หากเป็นวันที่ไทยอยู่แล้ว เช่น "16 ต.ค. 2569"
        for (String m : TH_MONTHS) {
            if (s.contains(m)) {
                return s;
            }
        }

        // หากเป็นข้อความทั่วไปที่ไม่มีตัวเลข (เช่น "เร็วๆ นี้") ให้ส่งคืนข้อความเดิม
        boolean hasDigit = false;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) {
                hasDigit = true;
                break;
            }
        }
        if (!hasDigit) {
            return s;
        }

        // แยกเฉพาะส่วนวันที่ในกรณีมีเวลาพ่วงท้าย (เช่น "16/10/2026 08:00" หรือ
        // "2026-10-16T08:00:00")
        String datePart = s.split("[ T]")[0].trim();

        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("yyyy-M-d"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("d-M-yyyy"),
                DateTimeFormatter.ofPattern("yyyy/M/d")
        };

        LocalDate d = null;
        for (DateTimeFormatter fmt : formatters) {
            try {
                d = LocalDate.parse(datePart, fmt);
                break;
            } catch (Exception ignored) {
            }
        }

        if (d == null) {
            try {
                String[] parts = datePart.split("[-/.]");
                if (parts.length == 3) {
                    int p0 = Integer.parseInt(parts[0].trim());
                    int p1 = Integer.parseInt(parts[1].trim());
                    int p2 = Integer.parseInt(parts[2].trim());
                    int day, month, year;
                    if (p0 > 1000) {
                        year = p0;
                        month = p1;
                        day = p2;
                    } else {
                        day = p0;
                        month = p1;
                        year = p2;
                    }
                    if (year > 2400) {
                        year -= 543;
                    }
                    d = LocalDate.of(year, month, day);
                }
            } catch (Exception ignored) {
            }
        }

        if (d != null) {
            int year = d.getYear();
            int beYear = year < 2400 ? year + 543 : year;
            return d.getDayOfMonth() + " " + TH_MONTHS[d.getMonthValue() - 1] + " " + beYear;
        }

        return "-";
    }

    /**
     * ดึงและจัดรูปแบบเวลาภาษาไทย (เติมหน่วย น.)
     * 
     * @param raw ข้อความวันและเวลาต้นทาง
     * @return ข้อความเวลา เช่น "14:30 น." หรือ "-" หากไม่มีข้อมูล
     */
    public static String thaiTime(String raw) {
        if (raw == null) {
            return "-";
        }
        String s = raw.trim();
        if (s.isEmpty() || "-".equals(s)) {
            return "-";
        }
        String[] parts = s.split("[ T]");
        if (parts.length > 1) {
            String t = parts[1].trim();
            if (t.endsWith("น.")) {
                return t;
            }
            if (t.matches("\\d{1,2}:\\d{2}(:\\d{2})?")) {
                if (t.length() > 5) {
                    t = t.substring(0, 5);
                }
                return t + " น.";
            }
            return t;
        }
        return "-";
    }

    /**
     * เปรียบเทียบหมายเลขห้องพักสองห้องเพื่อใช้ในการเรียงลำดับห้อง
     * 
     * @param n1 หมายเลขห้องแรก
     * @param n2 หมายเลขห้องที่สอง
     * @return ค่าลบหาก n1 น้อยกว่า, 0 หากเท่ากัน, ค่าบวกหาก n1 มากกว่า
     */
    public static int compareRoomNumbers(String n1, String n2) {
        if (n1 == null)
            n1 = "";
        if (n2 == null)
            n2 = "";
        try {
            int num1 = Integer.parseInt(n1.replaceAll("\\D+", ""));
            int num2 = Integer.parseInt(n2.replaceAll("\\D+", ""));
            if (num1 != num2)
                return Integer.compare(num1, num2);
        } catch (Exception ignored) {
        }
        return n1.compareToIgnoreCase(n2);
    }
}
