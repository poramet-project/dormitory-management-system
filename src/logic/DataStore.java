package logic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ศูนย์กลางอ่าน/เขียนไฟล์ CSV ของระบบ (rooms, contracts, room_transfers, complaints, bookings)
 * - อ่านตามชื่อ header ไม่ใช่ตำแหน่งคอลัมน์ จึงไม่พังเวลาเพิ่ม/สลับคอลัมน์
 * - รองรับค่าที่ครอบด้วย "..." และมีเครื่องหมายจุลภาคอยู่ข้างใน
 */
public final class DataStore {
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

    public static String getCurrentLoginUser() {
        return currentLoginUser;
    }

    public static void setCurrentLoginUser(String username) {
        currentLoginUser = username != null ? username : "Guest";
    }

    // ===================== หาไฟล์ =====================

    private static final String[] DIRS = { "src/data/", "data/", "DMS/src/data/", "src/", "" };

    /** หาไฟล์ตามชื่อ ถ้าไม่เจอที่ไหนเลยจะใช้ data/<name> */
    public static File file(String name) {
        for (String d : DIRS) {
            File f = new File(d + name);
            if (f.exists())
                return f;
        }
        return new File("data/" + name);
    }

    // ===================== อ่าน CSV =====================

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
     * อ่านทั้งตาราง คืนเป็นรายการของ Map(ชื่อคอลัมน์ -> ค่า)
     * ข้ามบรรทัดว่างและบรรทัดขึ้นต้นด้วย #
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

    static String csvEscape(String v) {
        if (v == null)
            return "";
        v = v.replace("\r", " ").replace("\n", " ").trim();
        if (v.contains(",") || v.contains("\"")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    /** เพิ่มหนึ่งแถวท้ายไฟล์ ถ้าไฟล์ว่างจะเขียน header ให้ก่อน */
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
     * สร้างรหัสถัดไป เช่น nextId("complaints.csv","complaintId","C",3) -> C001, C002 ...
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

    /** null / ว่าง / "-" ถือว่าไม่มีค่า */
    public static boolean blank(String s) {
        if (s == null)
            return true;
        String t = s.trim();
        return t.isEmpty() || t.equals("-");
    }

    static int toInt(String s) {
        try {
            return Integer.parseInt(s.trim().replaceAll("[,]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public static String money(int v) {
        return String.format("%,d", v);
    }

    private static final String[] TH_MONTHS = { "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
            "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค." };

    /** "2027-05-31" -> "31 พ.ค. 2570" */
    public static String thaiDate(String iso) {
        try {
            LocalDate d = LocalDate.parse(iso.trim());
            return d.getDayOfMonth() + " " + TH_MONTHS[d.getMonthValue() - 1] + " " + (d.getYear() + 543);
        } catch (Exception e) {
            return "-";
        }
    }

    // ===================== การจัดการข้อมูล Bookings =====================
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

    // ===================== ห้องพัก =====================

    public static final class Room {
        public final String id, number, type, status;
        public final int rent;

        Room(Map<String, String> m) {
            id = m.getOrDefault("roomId", "").trim();
            number = m.getOrDefault("roomNumber", "").trim();
            type = m.getOrDefault("roomType", "").trim();
            status = m.getOrDefault("status", "").trim().toUpperCase();
            rent = toInt(m.getOrDefault("baseRent", "0"));
        }

        public boolean isAir() {
            return type.toLowerCase().contains("air");
        }

        public String typeShort() {
            if (isAir())
                return "ห้องแอร์";
            if (type.equalsIgnoreCase("Fan"))
                return "ห้องพัดลม";
            return type.isEmpty() ? "-" : type;
        }

        public String typeFull() {
            return typeShort() + " (" + type + ")";
        }

        public String statusThai() {
            switch (status) {
                case "AVAILABLE":
                    return "ห้องว่าง";
                case "OCCUPIED":
                    return "มีผู้เช่า";
                case "RESERVED":
                    return "รออนุมัติการจอง";
                case "MAINTENANCE":
                case "UNDER_RENOVATION":
                    return "อยู่ระหว่างปรับปรุง";
                default:
                    return status.isEmpty() ? "-" : status;
            }
        }

        /** ชั้น: ตัดเลข 2 หลักท้ายของเลขห้องออก เช่น 101 -> 1, 205 -> 2, 1205 -> 12 */
        public String floor() {
            if (number.isEmpty() || !number.matches("\\d+"))
                return "-";
            return number.length() >= 3 ? number.substring(0, number.length() - 2) : number.substring(0, 1);
        }

        @Override
        public String toString() {
            return "ห้อง " + number + "  •  " + typeShort() + "  •  " + money(rent) + " บาท/เดือน";
        }
    }

    public static List<Room> rooms() {
        List<Room> list = new ArrayList<>();
        for (Map<String, String> m : readTable("rooms.csv")) {
            Room r = new Room(m);
            if (!r.id.isEmpty())
                list.add(r);
        }
        return list;
    }

    public static Room room(String roomId) {
        if (blank(roomId))
            return null;
        for (Room r : rooms()) {
            if (r.id.equalsIgnoreCase(roomId.trim()))
                return r;
        }
        return null;
    }

    /** ห้องที่ว่างและย้ายเข้าได้ (ตัดห้องของตัวเองออก) */
    public static List<Room> availableRooms(String excludeRoomId) {
        List<Room> list = new ArrayList<>();
        for (Room r : rooms()) {
            if (r.status.equals("AVAILABLE")
                    && !r.id.equalsIgnoreCase(excludeRoomId == null ? "" : excludeRoomId.trim())) {
                list.add(r);
            }
        }
        return list;
    }

    // ===================== สัญญาเช่า =====================

    public static final class Contract {
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

        public boolean isActive() {
            return status.equals("ACTIVE");
        }

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
     * สัญญาของผู้ใช้ (จับคู่ด้วย userId ถ้าไม่มี userId ใช้ roomId) เลือกฉบับ ACTIVE ก่อน
     */
    public static Contract contractFor(LogicLogin.User user) {
        if (user == null)
            return null;
        boolean byUser = !blank(user.userId);
        Contract best = null;
        for (Contract c : contracts()) {
            boolean mine = byUser
                    ? c.userId.equalsIgnoreCase(user.userId.trim())
                    : (!blank(user.roomId) && c.roomId.equalsIgnoreCase(user.roomId.trim()));
            if (!mine)
                continue;
            if (best == null || c.isActive() || !best.isActive())
                best = c;
        }
        return best;
    }

    /** บันทึกสัญญาแบบ ACTIVE ทันที (ใช้กรณี admin สร้างเอง) */
    public static synchronized boolean saveContract(String userId, String roomId, int monthlyRent) {
        return saveContract(userId, roomId, monthlyRent, "ACTIVE");
    }

    /** บันทึกสัญญา/คำขอจองลงใน contracts.csv ด้วยสถานะที่ระบุ (เช่น PENDING) คืน true ถ้าเขียนสำเร็จ */
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

    /** ผู้ใช้คนนี้มีคำขอจองที่รออนุมัติอยู่หรือไม่ */
    public static boolean hasPendingBooking(String userId) {
        if (blank(userId))
            return false;
        for (Contract c : contracts()) {
            if (c.status.equals("PENDING") && c.userId.equalsIgnoreCase(userId.trim()))
                return true;
        }
        return false;
    }

    /** คำขอจองทั้งหมดที่รอ admin อนุมัติ */
    public static List<Contract> pendingContracts() {
        List<Contract> list = new ArrayList<>();
        for (Contract c : contracts()) {
            if (c.status.equals("PENDING"))
                list.add(c);
        }
        return list;
    }

    /** admin อนุมัติ: สัญญา ACTIVE (เริ่มนับจากวันนี้), ห้อง OCCUPIED, ผู้ใช้เป็น TENANT */
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
        LogicLogin.assignRoom(c.userId, c.roomId);
        return true;
    }

    /** admin ปฏิเสธ: สัญญา REJECTED และปล่อยห้องกลับเป็น AVAILABLE */
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

    /** ผู้ใช้คนนี้มีสัญญา ACTIVE อยู่แล้วหรือไม่ */
    public static boolean hasActiveContract(String userId) {
        if (blank(userId))
            return false;
        for (Contract c : contracts()) {
            if (c.isActive() && c.userId.equalsIgnoreCase(userId.trim()))
                return true;
        }
        return false;
    }

    /** หาห้องจากเลขห้อง เช่น "101" */
    public static Room roomByNumber(String number) {
        if (blank(number))
            return null;
        for (Room r : rooms()) {
            if (r.number.equalsIgnoreCase(number.trim()))
                return r;
        }
        return null;
    }

    /** เปลี่ยนสถานะห้องใน rooms.csv (เช่น AVAILABLE -> RESERVED -> OCCUPIED) */
    public static synchronized boolean setRoomStatus(String roomId, String newStatus) {
        return updateCell("rooms.csv", "roomId", roomId, "status", newStatus);
    }

    /** แก้ค่าหนึ่งช่องในไฟล์ CSV: หาแถวที่คอลัมน์ keyCol = keyVal แล้วเปลี่ยนคอลัมน์ col เป็น newVal */
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

    // ===================== คำขอย้ายห้อง =====================

    /** ผู้ใช้คนนี้มีคำขอย้ายห้องที่ยังรอพิจารณา (PENDING) อยู่หรือไม่ */
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

    public static final class Complaint {
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

    /** ดึงประวัติการแจ้งซ่อมทั้งหมดของผู้ใช้คนนี้ */
    public static List<Complaint> complaintsForUser(String userId) {
        List<Complaint> list = new ArrayList<>();
        if (blank(userId))
            return list;
        for (Map<String, String> m : readTable(COMPLAINTS_FILE)) {
            Complaint c = new Complaint(m);
            if (c.userId.equalsIgnoreCase(userId.trim())) {
                list.add(c);
            }
        }
        return list;
    }
}