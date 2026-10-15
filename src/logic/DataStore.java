package logic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DataStore คือคลาสศูนย์กลางสำหรับจัดการการอ่านและเขียนไฟล์ CSV ของระบบหอพัก
 * COMPLAINTS_FILE คือ ชื่อไฟล์บันทึกการแจ้งซ่อม (complaints.csv)
 * CONTRACTS_FILE คือ ชื่อไฟล์บันทึกสัญญาเช่า (contracts.csv)
 * BOOKINGS_FILE คือ ชื่อไฟล์บันทึกคำขอจองห้องพัก (bookings.csv)
 * currentLoginUser คือ ตัวแปรเก็บชื่อผู้ใช้ที่กำลังล็อกอินอยู่ในระบบ
 */
public final class DataStore {

    // ===== representation =====
    // Abstraction Function:
    // AF(currentLoginUser) คือ คลังข้อมูลหลักและกลไกเข้าถึงฐานข้อมูลไฟล์ CSV สำหรับระบบจัดการหอพัก
    // RI
    // 1. currentLoginUser ต้องไม่เป็น null
    // 2. ไฟล์ CSV ที่ระบุในระบบต้องเป็นรูปแบบ UTF-8 ที่มีหัวตาราง (Header) ถูกต้อง

    private DataStore() {
    }

    // ===================== ค่าคงที่ระบบแจ้งซ่อมและสัญญา =====================
    public static final String COMPLAINTS_FILE = "complaints.csv";
    public static final String COMPLAINTS_HEADER = "complaintId,userId,roomId,category,topic,description,requestDate,status";
    
    public static final String CONTRACTS_FILE = "contracts.csv";
    public static final String CONTRACTS_HEADER = "contractId,userId,roomId,startDate,endDate,monthlyRent,deposit,status";

    public static final String BOOKINGS_FILE = "bookings.csv";
    public static final String BOOKINGS_HEADER = "bookingId,roomId,fullName,phone,requestDate,status";

    // ===================== ตัวแปรเก็บ Session ผู้ใช้ปัจจุบัน =====================
    private static String currentLoginUser = "Guest";

    /**
     * ดึงชื่อผู้ใช้งานที่กำลังเข้าสู่ระบบอยู่ในปัจจุบัน
     * 
     * @return ชื่อบัญชีผู้ใช้งานปัจจุบัน
     */
    public static String getCurrentLoginUser() {
        return currentLoginUser;
    }

    /**
     * กำหนดชื่อผู้ใช้งานที่เข้าสู่ระบบ
     * 
     * @param username ชื่อบัญชีผู้ใช้งาน
     */
    public static void setCurrentLoginUser(String username) {
        currentLoginUser = username != null ? username : "Guest";
    }

    // ===================== หาไฟล์ =====================

    private static final String[] DIRS = { "src/data/", "data/", "DMS/src/data/", "src/", "" };

    /**
     * ค้นหาไฟล์ข้อมูลตามลำดับโฟลเดอร์ที่เป็นไปได้ในระบบ
     * 
     * @param name ชื่อไฟล์ที่ต้องการค้นหา
     * @return ออบเจ็กต์ File ของไฟล์ที่พบ หรือตำแหน่งเป้าหมาย
     */
    public static File file(String name) {
        for (String d : DIRS) {
            File f = new File(d + name);
            if (f.exists())
                return f;
        }
        return new File("data/" + name);
    }

    // ===================== อ่าน CSV =====================

    /**
     * แยกข้อความ 1 บรรทัดของ CSV ให้เป็นอาร์เรย์ของแต่ละคอลัมน์ โดยรองรับเครื่องหมายคำพูด (Quotes)
     * 
     * @param line ข้อความบรรทัดใน CSV
     * @return อาร์เรย์ของข้อความในแต่ละคอลัมน์
     */
    static String[] parseLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else
                        quoted = false;
                } else
                    sb.append(c);
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }

    /**
     * อ่านข้อมูลทั้งหมดจากไฟล์ CSV ในรูปแบบตาราง (List ของแถวข้อมูลแบบ Map)
     * 
     * @param name ชื่อไฟล์ CSV
     * @return รายการแถวข้อมูลทั้งหมดในรูปแบบ Map คีย์เป็นชื่อคอลัมน์
     */
    public static synchronized List<Map<String, String>> readTable(String name) {
        List<Map<String, String>> rows = new ArrayList<>();
        File f = file(name);
        if (!f.exists() || f.length() == 0)
            return rows;
        try {
            String[] header = null;
            for (String raw : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) {
                String line = raw.replace("\uFEFF", "").trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                String[] c = parseLine(line);
                if (header == null) {
                    header = c;
                    for (int i = 0; i < header.length; i++)
                        header[i] = header[i].trim();
                    continue;
                }
                Map<String, String> m = new LinkedHashMap<>();
                for (int i = 0; i < header.length; i++) {
                    m.put(header[i], i < c.length ? c[i].trim() : "");
                }
                rows.add(m);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    // ===================== เขียน CSV =====================

    /**
     * แปลงข้อความให้ปลอดภัยสำหรับนำไปเขียนลงในไฟล์ CSV (ครอบเครื่องหมายคำพูดหากจำเป็น)
     * 
     * @param v ข้อความที่ต้องการแปลง
     * @return ข้อความที่ปลอดภัยตามมาตรฐาน CSV
     */
    static String csvEscape(String v) {
        if (v == null)
            return "";
        v = v.replace("\r", " ").replace("\n", " ").trim();
        if (v.contains(",") || v.contains("\"")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    /**
     * เพิ่มแถวข้อมูลใหม่ต่อท้ายไฟล์ CSV
     * 
     * @param name ชื่อไฟล์ CSV
     * @param header ส่วนหัวตาราง (ใช้เมื่อไฟล์ยังไม่เคยมีอยู่)
     * @param values ค่าของแต่ละคอลัมน์ในแถวที่จะเพิ่ม
     * @throws IOException หากเกิดข้อผิดพลาดในการเขียนไฟล์
     */
    public static synchronized void appendRow(String name, String header, String... values) throws IOException {
        File f = file(name);
        if (f.getParentFile() != null)
            f.getParentFile().mkdirs();

        String existing = f.exists()
                ? new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8).replace("\uFEFF", "")
                : "";

        StringBuilder sb = new StringBuilder(existing.trim().isEmpty() ? header : existing.replaceAll("\\s+$", ""));
        String nl = System.lineSeparator();
        sb.append(nl);
        for (int i = 0; i < values.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(csvEscape(values[i]));
        }
        sb.append(nl);
        Files.write(f.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * สร้างรหัสถัดไปแบบอัตโนมัติ (Auto-increment ID) จากข้อมูลที่มีอยู่ในตาราง
     * 
     * @param name ชื่อไฟล์ CSV
     * @param idColumn ชื่อคอลัมน์ที่เป็นรหัส
     * @param prefix คำนำหน้ารหัส เช่น "BK", "CNT", "T"
     * @param digits จำนวนหลักของตัวเลข เช่น 3 หลัก ("001")
     * @return รหัสใหม่ถัดไป
     */
    public static synchronized String nextId(String name, String idColumn, String prefix, int digits) {
        int max = 0;
        for (Map<String, String> m : readTable(name)) {
            String id = m.getOrDefault(idColumn, "").trim();
            if (id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return prefix + String.format("%0" + digits + "d", max + 1);
    }

    // ===================== ตัวช่วยทั่วไป =====================

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
    static int toInt(String s) {
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

    private static final String[] TH_MONTHS = { "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
            "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค." };

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

        // แยกเฉพาะส่วนวันที่ในกรณีมีเวลาพ่วงท้าย (เช่น "16/10/2026 08:00" หรือ "2026-10-16T08:00:00")
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

    // ===================== การจัดการข้อมูล Bookings =====================

    /**
     * บันทึกคำขอจองห้องพักใหม่ลงในไฟล์ bookings.csv
     * 
     * @param roomId รหัสห้องพัก
     * @param fullName ชื่อ-นามสกุลของผู้ขอจอง
     * @param phone เบอร์โทรศัพท์ติดต่อ
     * @return true หากบันทึกสำเร็จ, false หากเกิดข้อผิดพลาด
     */
    public static synchronized boolean saveBooking(String roomId, String fullName, String phone) {
        try {
            String bookingId = nextId(BOOKINGS_FILE, "bookingId", "BK", 3);
            String requestDate = LocalDate.now().toString();
            appendRow(BOOKINGS_FILE, BOOKINGS_HEADER, bookingId, roomId, fullName, phone, requestDate, "PENDING");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ตรวจสอบว่าผู้ใช้งานชื่อนี้มีคำขอจองห้องที่ยังรอพิจารณา (PENDING) อยู่หรือไม่
     * 
     * @param fullName ชื่อ-นามสกุล
     * @return true หากมีคำขอจองสถานะ PENDING ค้างอยู่
     */
    public static boolean hasPendingBookingByName(String fullName) {
        if (blank(fullName)) return false;
        for (Map<String, String> m : readTable(BOOKINGS_FILE)) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", "")) &&
                m.getOrDefault("fullName", "").equalsIgnoreCase(fullName.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * ตรวจสอบว่าผู้ใช้งานรายนี้เป็นเจ้าของคำขอจองที่กำลังรอพิจารณา (PENDING) ของห้องนี้หรือไม่
     * 
     * @param roomId รหัสห้องพัก
     * @param fullName ชื่อ-นามสกุล
     * @return true หากเป็นเจ้าของคำขอจองที่รอพิจารณาอยู่สำหรับห้องนี้
     */
    public static boolean isRoomPendingByUser(String roomId, String fullName) {
        if (blank(roomId) || blank(fullName)) return false;
        for (Map<String, String> m : readTable(BOOKINGS_FILE)) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", "")) &&
                roomId.equalsIgnoreCase(m.getOrDefault("roomId", "")) &&
                fullName.trim().equalsIgnoreCase(m.getOrDefault("fullName", "").trim())) {
                return true;
            }
        }
        return false;
    }

    // ===================== ห้องพัก =====================

    /**
     * Room คือคลาสแทนข้อมูลห้องพัก 1 ห้องในหอพัก
     * id คือ รหัสห้องพัก (เช่น R101)
     * number คือ หมายเลขห้องพัก (เช่น 101)
     * type คือ ประเภทห้องพัก (เช่น Air, Fan)
     * status คือ สถานะห้องพัก (AVAILABLE, OCCUPIED, MAINTENANCE)
     * rent คือ อัตราค่าเช่ารายเดือน
     */
    public static final class Room {

        // ===== representation =====
        // Abstraction Function:
        // AF(id, number, type, status, rent) คือ ข้อมูลห้องพัก 1 ห้องในหอพัก
        // RI
        // 1. id, number, type, status ต้องไม่เป็น null
        // 2. rent ต้องมีค่า >= 0

        public final String id, number, type, status;
        public final int rent;

        Room(Map<String, String> m) {
            id = m.getOrDefault("roomId", "").trim();
            number = m.getOrDefault("roomNumber", "").trim();
            type = m.getOrDefault("roomType", "").trim();
            status = m.getOrDefault("status", "").trim().toUpperCase();
            rent = toInt(m.getOrDefault("baseRent", "0"));
        }

        /**
         * ตรวจสอบว่าห้องพักเป็นห้องปรับอากาศ (แอร์) หรือไม่
         * 
         * @return true หากเป็นห้องแอร์
         */
        public boolean isAir() {
            return type.toLowerCase().contains("air");
        }

        /**
         * คืนค่าประเภทห้องพักแบบย่อภาษาไทย
         * 
         * @return ชื่อประเภทห้อง เช่น "ห้องแอร์" หรือ "ห้องพัดลม"
         */
        public String typeShort() {
            if (isAir())
                return "ห้องแอร์";
            if (type.equalsIgnoreCase("Fan"))
                return "ห้องพัดลม";
            return type.isEmpty() ? "-" : type;
        }

        /**
         * คืนค่าประเภทห้องพักแบบเต็มพร้อมระบุคำภาษาอังกฤษในวงเล็บ
         * 
         * @return ชื่อประเภทห้องแบบเต็ม
         */
        public String typeFull() {
            return typeShort() + " (" + type + ")";
        }

        /**
         * แปลงสถานะของห้องพักภาษาอังกฤษเป็นข้อความภาษาไทย
         * 
         * @return สถานะห้องภาษาไทย เช่น "ห้องว่าง", "มีผู้เช่า", "ปิดปรับปรุง"
         */
        public String statusThai() {
            switch (status) {
                case "AVAILABLE":
                case "RESERVED":
                case "PENDING":
                    return "ห้องว่าง";
                case "OCCUPIED":
                    return "มีผู้เช่า";
                case "MAINTENANCE":
                case "UNDER_RENOVATION":
                    return "ปิดปรับปรุง";
                default:
                    return status.isEmpty() ? "-" : status;
            }
        }

        /**
         * ดึงหมายเลขชั้นของห้องพักจากตัวเลขห้อง
         * 
         * @return หมายเลขชั้น เช่น "1", "2" หรือ "-"
         */
        public String floor() {
            if (number.isEmpty() || !number.matches("\\d+"))
                return "-";
            return number.length() >= 3 ? number.substring(0, number.length() - 2) : number.substring(0, 1);
        }

        /**
         * แปลงข้อมูลห้องพักเป็นข้อความสรุปสำหรับแสดงผลในรายการ
         * 
         * @return ข้อความรายละเอียดห้องพัก
         */
        @Override
        public String toString() {
            return "ห้อง " + number + "  •  " + typeShort() + "  •  " + money(rent) + " บาท/เดือน";
        }
    }

    /**
     * เปรียบเทียบหมายเลขห้องพักสองห้องเพื่อใช้ในการเรียงลำดับห้อง
     * 
     * @param n1 หมายเลขห้องแรก
     * @param n2 หมายเลขห้องที่สอง
     * @return ค่าลบหาก n1 น้อยกว่า, 0 หากเท่ากัน, ค่าบวกหาก n1 มากกว่า
     */
    public static int compareRoomNumbers(String n1, String n2) {
        if (n1 == null) n1 = "";
        if (n2 == null) n2 = "";
        try {
            int num1 = Integer.parseInt(n1.replaceAll("\\D+", ""));
            int num2 = Integer.parseInt(n2.replaceAll("\\D+", ""));
            if (num1 != num2) return Integer.compare(num1, num2);
        } catch (Exception ignored) {}
        return n1.compareToIgnoreCase(n2);
    }

    /**
     * จัดเรียงข้อมูลห้องพักในไฟล์ rooms.csv ตามหมายเลขห้องและบันทึกกลับลงไฟล์
     */
    public static synchronized void sortRoomsFile() {
        File f = file("rooms.csv");
        if (!f.exists()) return;
        List<Map<String, String>> rows = readTable("rooms.csv");
        rows.sort((a, b) -> compareRoomNumbers(a.getOrDefault("roomNumber", ""), b.getOrDefault("roomNumber", "")));

        StringBuilder sb = new StringBuilder("roomId,roomNumber,roomType,baseRent,status");
        String nl = System.lineSeparator();
        sb.append(nl);
        for (Map<String, String> m : rows) {
            sb.append(csvEscape(m.getOrDefault("roomId", ""))).append(',')
              .append(csvEscape(m.getOrDefault("roomNumber", ""))).append(',')
              .append(csvEscape(m.getOrDefault("roomType", ""))).append(',')
              .append(csvEscape(m.getOrDefault("baseRent", ""))).append(',')
              .append(csvEscape(m.getOrDefault("status", ""))).append(nl);
        }
        try {
            Files.write(f.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ดึงรายการห้องพักทั้งหมดจาก rooms.csv พร้อมจัดเรียงตามหมายเลขห้อง
     * 
     * @return รายการออบเจ็กต์ Room ทั้งหมด
     */
    public static List<Room> rooms() {
        List<Room> list = new ArrayList<>();
        for (Map<String, String> m : readTable("rooms.csv")) {
            Room r = new Room(m);
            if (!r.id.isEmpty())
                list.add(r);
        }
        list.sort((a, b) -> compareRoomNumbers(a.number, b.number));
        return list;
    }

    /**
     * ค้นหาข้อมูลห้องพักตามรหัสห้อง (roomId)
     * 
     * @param roomId รหัสห้องพักที่ต้องการค้นหา
     * @return ออบเจ็กต์ Room หรือ null หากไม่พบ
     */
    public static Room room(String roomId) {
        if (blank(roomId))
            return null;
        for (Room r : rooms()) {
            if (r.id.equalsIgnoreCase(roomId.trim()))
                return r;
        }
        return null;
    }

    /**
     * ดึงรายชื่อห้องพักที่ว่างทั้งหมด โดยสามารถเลือกตัดห้องที่ไม่ต้องการออกได้
     * 
     * @param excludeRoomId รหัสห้องพักที่ต้องการยกเว้นไม่ให้นับ
     * @return รายการห้องพักที่มีสถานะว่าง
     */
    public static List<Room> availableRooms(String excludeRoomId) {
        List<Room> list = new ArrayList<>();
        for (Room r : rooms()) {
            if ("AVAILABLE".equalsIgnoreCase(r.status)
                    && !r.id.equalsIgnoreCase(excludeRoomId == null ? "" : excludeRoomId.trim())) {
                list.add(r);
            }
        }
        return list;
    }

    // ===================== สัญญาเช่า =====================

    /**
     * Contract คือคลาสแทนสัญญาเช่าห้องพัก 1 ฉบับ
     * id คือ รหัสสัญญาเช่า (เช่น CNT001)
     * userId คือ รหัสผู้เช่าที่ทำสัญญา
     * roomId คือ รหัสห้องพักที่ทำสัญญา
     * start คือ วันที่เริ่มต้นสัญญา
     * end คือ วันที่สิ้นสุดสัญญา
     * status คือ สถานะสัญญา (ACTIVE, PENDING, EXPIRED, TERMINATED, REJECTED)
     * rent คือ อัตราค่าเช่ารายเดือนตามสัญญา
     * deposit คือ เงินประกันสัญญา
     */
    public static final class Contract {

        // ===== representation =====
        // Abstraction Function:
        // AF(id, userId, roomId, start, end, status, rent, deposit) คือ สัญญาเช่าห้องพัก 1 ฉบับ
        // RI
        // 1. id, userId, roomId, status ต้องไม่เป็น null
        // 2. rent และ deposit ต้องมีค่า >= 0

        public final String id, userId, roomId, start, end, status;
        public final int rent, deposit;

        Contract(Map<String, String> m) {
            id = m.getOrDefault("contractId", "").trim();
            userId = m.getOrDefault("userId", "").trim();
            roomId = m.getOrDefault("roomId", "").trim();
            start = m.getOrDefault("startDate", "").trim();
            end = m.getOrDefault("endDate", "").trim();
            status = m.getOrDefault("status", "").trim().toUpperCase();
            rent = toInt(m.getOrDefault("monthlyRent", "0"));
            deposit = toInt(m.getOrDefault("deposit", "0"));
        }

        /**
         * ตรวจสอบว่าสัญญาเช่าฉบับนี้มีสถานะเปิดใช้งานอยู่ (ACTIVE) หรือไม่
         * 
         * @return true หากสัญญาเปิดใช้งานอยู่
         */
        public boolean isActive() {
            return status.equals("ACTIVE");
        }

        /**
         * แปลงสถานะของสัญญาเช่าเป็นข้อความภาษาไทยพร้อมคำอธิบาย
         * 
         * @return สถานะสัญญาภาษาไทย
         */
        public String statusThai() {
            switch (status) {
                case "ACTIVE":
                    return "ใช้งานอยู่ (Active)";
                case "PENDING":
                    return "รออนุมัติ (Pending)";
                case "REJECTED":
                    return "ไม่อนุมัติ (Rejected)";
                case "EXPIRED":
                    return "หมดอายุ (Expired)";
                case "TERMINATED":
                    return "ยกเลิกสัญญา (Terminated)";
                default:
                    return status.isEmpty() ? "-" : status;
            }
        }
    }

    /**
     * ดึงรายการสัญญาเช่าทั้งหมดจากไฟล์ contracts.csv
     * 
     * @return รายการสัญญาเช่าทั้งหมด
     */
    public static List<Contract> contracts() {
        List<Contract> list = new ArrayList<>();
        for (Map<String, String> m : readTable(CONTRACTS_FILE)) {
            Contract c = new Contract(m);
            if (!c.id.isEmpty())
                list.add(c);
        }
        return list;
    }

    /**
     * ค้นหาสัญญาเช่าของผู้เช่ารายที่ระบุ โดยให้ความสำคัญกับสัญญาที่สถานะ ACTIVE ก่อน
     * 
     * @param user ข้อมูลผู้เช่า
     * @return สัญญาเช่าที่ตรงกัน หรือ null หากไม่มี
     */
    public static Contract contractFor(LoginLogic.User user) {
        if (user == null)
            return null;
        String uId = blank(user.userId) ? "" : user.userId.trim();
        String uRoom = blank(user.roomId) ? "" : user.roomId.trim();

        // 1. ลองหาสัญญาที่ ACTIVE ของห้องปัจจุบันก่อน
        if (!blank(uRoom) && !"-".equals(uRoom)) {
            Room r = room(uRoom);
            if (r == null) r = roomByNumber(uRoom);
            String rId = r != null ? r.id : uRoom;
            String rNum = r != null ? r.number : uRoom;

            for (Contract c : contracts()) {
                boolean userOk = blank(uId) || c.userId.equalsIgnoreCase(uId);
                boolean roomOk = c.roomId.equalsIgnoreCase(rId) || c.roomId.equalsIgnoreCase(rNum);
                if (userOk && roomOk && c.isActive()) {
                    return c;
                }
            }
        }

        // 2. หาสัญญาที่ ACTIVE ของ user
        if (!blank(uId)) {
            for (Contract c : contracts()) {
                if (c.userId.equalsIgnoreCase(uId) && c.isActive()) {
                    return c;
                }
            }
        }

        // 3. Fallback: สัญญาฉบับล่าสุดของผู้เช่า
        Contract best = null;
        for (Contract c : contracts()) {
            boolean mine = !blank(uId)
                    ? c.userId.equalsIgnoreCase(uId)
                    : (!blank(uRoom) && c.roomId.equalsIgnoreCase(uRoom));
            if (mine) {
                if (best == null || c.isActive() || !best.isActive()) {
                    best = c;
                }
            }
        }
        return best;
    }

    /**
     * บันทึกข้อมูลสัญญาเช่าใหม่โดยกำหนดสถานะเริ่มต้นเป็น ACTIVE
     * 
     * @param userId รหัสผู้เช่า
     * @param roomId รหัสห้องพัก
     * @param monthlyRent ค่าเช่ารายเดือน
     * @return true หากบันทึกสำเร็จ
     */
    public static synchronized boolean saveContract(String userId, String roomId, int monthlyRent) {
        return saveContract(userId, roomId, monthlyRent, "ACTIVE");
    }

    /**
     * บันทึกข้อมูลสัญญาเช่าใหม่โดยกำหนดสถานะสัญญาได้ตามต้องการ
     * 
     * @param userId รหัสผู้เช่า
     * @param roomId รหัสห้องพัก
     * @param monthlyRent ค่าเช่ารายเดือน
     * @param status สถานะสัญญาเช่า เช่น "ACTIVE" หรือ "PENDING"
     * @return true หากบันทึกสำเร็จ
     */
    public static synchronized boolean saveContract(String userId, String roomId, int monthlyRent, String status) {
        try {
            String contractId = nextId(CONTRACTS_FILE, "contractId", "CNT", 3);
            String startDate = LocalDate.now().toString();
            String endDate = LocalDate.now().plusYears(1).toString();
            appendRow(CONTRACTS_FILE, CONTRACTS_HEADER, contractId, userId, roomId, startDate, endDate,
                    String.valueOf(monthlyRent), "0", status);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ตรวจสอบว่าผู้เช่ามีสัญญาเช่าที่รอการอนุมัติ (PENDING) หรือไม่
     * 
     * @param userId รหัสผู้เช่า
     * @return true หากมีสัญญาเช่าที่รออนุมัติค้างอยู่
     */
    public static boolean hasPendingBooking(String userId) {
        if (blank(userId))
            return false;
        for (Contract c : contracts()) {
            if (c.status.equals("PENDING") && c.userId.equalsIgnoreCase(userId.trim()))
                return true;
        }
        return false;
    }

    /**
     * ดึงรายการสัญญาเช่าทั้งหมดที่อยู่ในสถานะรอการอนุมัติ (PENDING)
     * 
     * @return รายการสัญญาเช่าที่รออนุมัติ
     */
    public static List<Contract> pendingContracts() {
        List<Contract> list = new ArrayList<>();
        for (Contract c : contracts()) {
            if (c.status.equals("PENDING"))
                list.add(c);
        }
        return list;
    }

    /**
     * อนุมัติสัญญาเช่า กำหนดวันที่เริ่ม-สิ้นสุดสัญญา ปรับสถานะห้องเป็น OCCUPIED และบันทึกห้องให้ผู้ใช้
     * 
     * @param contractId รหัสสัญญาเช่าที่ต้องการอนุมัติ
     * @return true หากอนุมัติสำเร็จ
     */
    public static synchronized boolean approveBooking(String contractId) {
        Contract c = null;
        for (Contract x : pendingContracts()) {
            if (x.id.equalsIgnoreCase(contractId))
                c = x;
        }
        if (c == null)
            return false;
        String start = LocalDate.now().toString();
        String end = LocalDate.now().plusYears(1).toString();
        boolean ok = updateCell(CONTRACTS_FILE, "contractId", c.id, "status", "ACTIVE")
                && updateCell(CONTRACTS_FILE, "contractId", c.id, "startDate", start)
                && updateCell(CONTRACTS_FILE, "contractId", c.id, "endDate", end);
        if (!ok)
            return false;
        setRoomStatus(c.roomId, "OCCUPIED");
        LoginLogic.assignRoom(c.userId, c.roomId);
        return true;
    }

    /**
     * ปฏิเสธคำขอทำสัญญาเช่า และคืนสถานะห้องพักกลับเป็น AVAILABLE
     * 
     * @param contractId รหัสสัญญาเช่าที่ต้องการปฏิเสธ
     * @return true หากปฏิเสธสำเร็จ
     */
    public static synchronized boolean rejectBooking(String contractId) {
        Contract c = null;
        for (Contract x : pendingContracts()) {
            if (x.id.equalsIgnoreCase(contractId))
                c = x;
        }
        if (c == null)
            return false;
        if (!updateCell(CONTRACTS_FILE, "contractId", c.id, "status", "REJECTED"))
            return false;
        Room r = room(c.roomId);
        if (r != null && r.status.equals("RESERVED"))
            setRoomStatus(c.roomId, "AVAILABLE");
        return true;
    }

    /**
     * ตรวจสอบว่าผู้เช่ามีสัญญาเช่าที่กำลังเปิดใช้งานอยู่ (ACTIVE) หรือไม่
     * 
     * @param userId รหัสผู้เช่า
     * @return true หากมีสัญญาเช่าที่ใช้งานอยู่
     */
    public static boolean hasActiveContract(String userId) {
        if (blank(userId))
            return false;
        for (Contract c : contracts()) {
            if (c.isActive() && c.userId.equalsIgnoreCase(userId.trim()))
                return true;
        }
        return false;
    }

    /**
     * ค้นหาข้อมูลห้องพักจากหมายเลขห้องพัก (roomNumber)
     * 
     * @param number หมายเลขห้องพัก เช่น "101"
     * @return ออบเจ็กต์ Room หรือ null หากไม่พบ
     */
    public static Room roomByNumber(String number) {
        if (blank(number))
            return null;
        for (Room r : rooms()) {
            if (r.number.equalsIgnoreCase(number.trim()))
                return r;
        }
        return null;
    }

    /**
     * ปรับปรุงสถานะของห้องพักในไฟล์ rooms.csv
     * 
     * @param roomId รหัสห้องพัก
     * @param newStatus สถานะใหม่ของห้องพัก เช่น "AVAILABLE", "OCCUPIED"
     * @return true หากปรับปรุงสถานะสำเร็จ
     */
    public static synchronized boolean setRoomStatus(String roomId, String newStatus) {
        return updateCell("rooms.csv", "roomId", roomId, "status", newStatus);
    }

    /**
     * ปรับปรุงค่าในคอลัมน์ของแถวข้อมูลที่ตรงกับค่าคีย์ที่กำหนดในไฟล์ CSV
     * 
     * @param name ชื่อไฟล์ CSV
     * @param keyCol ชื่อคอลัมน์คีย์สำหรับค้นหาแถว
     * @param keyVal ค่าคีย์ที่ต้องการจับคู่
     * @param col ชื่อคอลัมน์ที่ต้องการแก้ไขค่า
     * @param newVal ค่าใหม่ที่ต้องการเขียนลงไป
     * @return true หากพบแถวและปรับปรุงข้อมูลสำเร็จ
     */
    public static synchronized boolean updateCell(String name, String keyCol, String keyVal, String col, String newVal) {
        File f = file(name);
        if (!f.exists())
            return false;
        try {
            List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            int idCol = -1, stCol = -1;
            boolean changed = false;
            List<String> out = new ArrayList<>();
            for (String raw : lines) {
                String line = raw.replace("\uFEFF", "").trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    out.add(raw);
                    continue;
                }
                String[] c = parseLine(line);
                if (idCol < 0) { // บรรทัด header
                    for (int i = 0; i < c.length; i++) {
                        String h = c[i].trim();
                        if (h.equals(keyCol)) idCol = i;
                        if (h.equals(col)) stCol = i;
                    }
                    if (idCol < 0 || stCol < 0)
                        return false;
                    out.add(line);
                    continue;
                }
                if (idCol < c.length && stCol < c.length && c[idCol].trim().equalsIgnoreCase(keyVal.trim())) {
                    c[stCol] = newVal;
                    changed = true;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < c.length; i++) {
                    if (i > 0) sb.append(',');
                    sb.append(csvEscape(c[i]));
                }
                out.add(sb.toString());
            }
            if (!changed)
                return false;
            Files.write(f.toPath(), (String.join(System.lineSeparator(), out) + System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * อัปเดตรหัสห้องพัก (roomId) จากรหัสเดิมเป็นรหัสใหม่ในไฟล์ CSV ทั้งหมดที่เกี่ยวข้อง
     * เพื่อให้รหัสห้องพักเชื่อมโยงอย่างถูกต้องทั่วทั้งระบบ
     * 
     * @param oldRoomId รหัสห้องพักเดิม (เช่น R206)
     * @param newRoomId รหัสห้องพักใหม่ที่แปรผันตามเลขห้อง (เช่น R207)
     */
    public static synchronized void cascadeUpdateRoomId(String oldRoomId, String newRoomId) {
        if (oldRoomId == null || newRoomId == null || oldRoomId.trim().equalsIgnoreCase(newRoomId.trim())) {
            return;
        }
        String oldId = oldRoomId.trim();
        String newId = newRoomId.trim();

        // 1. contracts.csv (คอลัมน์ roomId)
        updateCell(CONTRACTS_FILE, "roomId", oldId, "roomId", newId);
        
        // 2. users.csv (คอลัมน์ roomId)
        updateCell("users.csv", "roomId", oldId, "roomId", newId);
        
        // 3. bookings.csv (คอลัมน์ roomId)
        updateCell(BOOKINGS_FILE, "roomId", oldId, "roomId", newId);
        
        // 4. complaints.csv (คอลัมน์ roomId)
        updateCell(COMPLAINTS_FILE, "roomId", oldId, "roomId", newId);
        
        // 5. room_transfers.csv (คอลัมน์ oldRoomId และ newRoomId)
        updateCell("room_transfers.csv", "oldRoomId", oldId, "oldRoomId", newId);
        updateCell("room_transfers.csv", "newRoomId", oldId, "newRoomId", newId);
    }

    /**
     * อัปเดตข้อมูลห้องพักทั้งหมดใน rooms.csv พร้อมทั้งเปลี่ยนรหัสห้อง (roomId) และกระจายไปยังไฟล์ CSV อื่น
     * 
     * @param oldRoomId รหัสห้องเดิมก่อนแก้ไข
     * @param newRoomId รหัสห้องใหม่ที่คำนวณตามหมายเลขห้อง
     * @param roomNumber หมายเลขห้องใหม่
     * @param roomType ประเภทห้อง (Air หรือ Fan)
     * @param rent อัตราค่าเช่า
     * @param status สถานะห้องพัก (AVAILABLE, OCCUPIED, MAINTENANCE)
     * @return true หากพบแถวและอัปเดตข้อมูลสำเร็จ
     */
    public static synchronized boolean updateRoomFull(String oldRoomId, String newRoomId, String roomNumber, String roomType, int rent, String status) {
        File f = file("rooms.csv");
        if (!f.exists())
            return false;
        try {
            List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            boolean changed = false;
            List<String> out = new ArrayList<>();
            int idCol = -1, numCol = -1, typeCol = -1, rentCol = -1, stCol = -1;
            for (String raw : lines) {
                String line = raw.replace("\uFEFF", "").trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    out.add(raw);
                    continue;
                }
                String[] c = parseLine(line);
                if (idCol < 0) { // บรรทัด header
                    for (int i = 0; i < c.length; i++) {
                        String h = c[i].trim();
                        if (h.equals("roomId")) idCol = i;
                        else if (h.equals("roomNumber")) numCol = i;
                        else if (h.equals("roomType")) typeCol = i;
                        else if (h.equals("baseRent")) rentCol = i;
                        else if (h.equals("status")) stCol = i;
                    }
                    out.add(line);
                    continue;
                }
                if (idCol >= 0 && idCol < c.length && c[idCol].trim().equalsIgnoreCase(oldRoomId.trim())) {
                    c[idCol] = newRoomId;
                    if (numCol >= 0 && numCol < c.length) c[numCol] = roomNumber;
                    if (typeCol >= 0 && typeCol < c.length) c[typeCol] = roomType;
                    if (rentCol >= 0 && rentCol < c.length) c[rentCol] = String.valueOf(rent);
                    if (stCol >= 0 && stCol < c.length) c[stCol] = status;
                    changed = true;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < c.length; i++) {
                    if (i > 0) sb.append(',');
                    sb.append(csvEscape(c[i]));
                }
                out.add(sb.toString());
            }
            if (!changed)
                return false;
            Files.write(f.toPath(), (String.join(System.lineSeparator(), out) + System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8));
            
            // กระจายรหัสห้องใหม่ไปยัง CSV อื่นๆ หากรหัสเปลี่ยน
            if (!oldRoomId.trim().equalsIgnoreCase(newRoomId.trim())) {
                cascadeUpdateRoomId(oldRoomId, newRoomId);
            }
            sortRoomsFile();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================== คำขอย้ายห้อง =====================

    /**
     * ตรวจสอบว่าผู้เช่ามีคำขอย้ายห้องที่ยังรอการพิจารณา (PENDING) อยู่หรือไม่
     * 
     * @param userId รหัสผู้เช่า
     * @return true หากมีคำขอย้ายห้องที่รอพิจารณาค้างอยู่
     */
    public static boolean hasPendingTransfer(String userId) {
        if (blank(userId))
            return false;
        for (Map<String, String> m : readTable("room_transfers.csv")) {
            if (m.getOrDefault("userId", "").trim().equalsIgnoreCase(userId.trim())
                    && m.getOrDefault("status", "").trim().equalsIgnoreCase("PENDING")) {
                return true;
            }
        }
        return false;
    }

    // ===================== แจ้งซ่อม / ร้องเรียน =====================

    /**
     * Complaint คือคลาสแทนข้อมูลรายการแจ้งซ่อมหรือร้องเรียน 1 รายการ
     */
    public static final class Complaint {

        // ===== representation =====
        // Abstraction Function:
        // AF(id, userId, roomId, category, topic, description, date, status) คือ ข้อมูลรายการแจ้งซ่อมหรือร้องเรียน 1 รายการ
        // RI
        // 1. id, userId, roomId, category, topic, status ต้องไม่เป็น null

        public final String id, userId, roomId, category, topic, description, date, status;

        Complaint(Map<String, String> m) {
            id = m.getOrDefault("complaintId", "").trim();
            userId = m.getOrDefault("userId", "").trim();
            roomId = m.getOrDefault("roomId", "").trim();
            category = m.getOrDefault("category", "").trim();
            topic = m.getOrDefault("topic", "").trim();
            description = m.getOrDefault("description", "").trim();
            date = m.getOrDefault("requestDate", "").trim();
            status = m.getOrDefault("status", "").trim().toUpperCase();
        }

        /**
         * แปลงสถานะรายการแจ้งซ่อม/ร้องเรียนเป็นข้อความภาษาไทย
         * 
         * @return สถานะภาษาไทย เช่น "กำลังดำเนินการ", "ช่างกำลังเข้าตรวจสอบ", "เสร็จสิ้น"
         */
        public String statusThai() {
            switch (status) {
                case "PENDING":
                    return "กำลังดำเนินการ";
                case "IN_PROGRESS":
                    return "ช่างกำลังเข้าตรวจสอบ";
                case "RESOLVED":
                    return "เสร็จสิ้น";
                default:
                    return status.isEmpty() ? "-" : status;
            }
        }
    }

    /**
     * ดึงรายการแจ้งซ่อมและข้อร้องเรียนของผู้เช่าเฉพาะห้องพักปัจจุบัน (ไม่รวมข้อมูลของห้องเดิมที่ย้ายออกแล้ว)
     * 
     * @param userId รหัสผู้เช่า
     * @return รายการข้อร้องเรียนของผู้เช่าในห้องปัจจุบัน
     */
    public static List<Complaint> complaintsForUser(String userId) {
        if (blank(userId))
            return new ArrayList<>();
        LoginLogic.User u = LoginLogic.findUser(userId);
        String rId = (u != null) ? u.roomId : null;
        if (blank(rId) || "-".equals(rId.trim())) {
            Contract c = contractFor(u);
            if (c != null && c.isActive()) {
                rId = c.roomId;
            }
        }
        return complaintsForUserAndRoom(userId, rId);
    }

    /**
     * ดึงรายการแจ้งซ่อมและข้อร้องเรียนของผู้เช่าเฉพาะห้องพักที่ระบุ
     * 
     * @param userId รหัสผู้เช่า
     * @param roomId รหัสหรือหมายเลขห้องพัก
     * @return รายการข้อร้องเรียนของผู้เช่าในห้องพักที่ระบุ
     */
    public static List<Complaint> complaintsForUserAndRoom(String userId, String roomId) {
        List<Complaint> list = new ArrayList<>();
        if (blank(userId) || blank(roomId) || "-".equals(roomId.trim()))
            return list;
        Room r = room(roomId);
        if (r == null) r = roomByNumber(roomId);
        String curId = r != null ? r.id : roomId.trim();
        String curNum = r != null ? r.number : roomId.trim();

        for (Map<String, String> m : readTable(COMPLAINTS_FILE)) {
            Complaint c = new Complaint(m);
            boolean userMatch = c.userId.equalsIgnoreCase(userId.trim());
            boolean roomMatch = c.roomId.equalsIgnoreCase(curId) || c.roomId.equalsIgnoreCase(curNum);
            if (userMatch && roomMatch) {
                list.add(c);
            }
        }
        return list;
    }
}