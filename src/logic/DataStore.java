package logic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ศูนย์กลางอ่าน/เขียนไฟล์ CSV ของระบบ (rooms, contracts, room_transfers, complaints, bookings)
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

    /** เช็คว่า User รายนี้เป็นเจ้าของคำขอจองที่กำลัง PENDING ของห้องนี้หรือไม่ */
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
                case "RESERVED":
                case "PENDING":
                    return "ห้องว่าง";
                case "OCCUPIED":
                    return "มีผู้เช่า";
                case "MAINTENANCE":
                case "UNDER_RENOVATION":
                    return "อยู่ระหว่างปรับปรุง";
                default:
                    return status.isEmpty() ? "-" : status;
            }
        }

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

    /** เรียงลำดับข้อมูลห้องพักใน rooms.csv ตามลำดับชั้นและเลขห้องพักอย่างสวยงาม */
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

    public static Room room(String roomId) {
        if (blank(roomId))
            return null;
        for (Room r : rooms()) {
            if (r.id.equalsIgnoreCase(roomId.trim()))
                return r;
        }
        return null;
    }

    public static List<Room> availableRooms(String excludeRoomId) {
        List<Room> list = new ArrayList<>();
        for (Room r : rooms()) {
            if (!r.status.equals("OCCUPIED")
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

    public static Contract contractFor(LoginLogic.User user) {
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

    public static synchronized boolean saveContract(String userId, String roomId, int monthlyRent) {
        return saveContract(userId, roomId, monthlyRent, "ACTIVE");
    }

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

    public static boolean hasPendingBooking(String userId) {
        if (blank(userId))
            return false;
        for (Contract c : contracts()) {
            if (c.status.equals("PENDING") && c.userId.equalsIgnoreCase(userId.trim()))
                return true;
        }
        return false;
    }

    public static List<Contract> pendingContracts() {
        List<Contract> list = new ArrayList<>();
        for (Contract c : contracts()) {
            if (c.status.equals("PENDING"))
                list.add(c);
        }
        return list;
    }

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

    public static boolean hasActiveContract(String userId) {
        if (blank(userId))
            return false;
        for (Contract c : contracts()) {
            if (c.isActive() && c.userId.equalsIgnoreCase(userId.trim()))
                return true;
        }
        return false;
    }

    public static Room roomByNumber(String number) {
        if (blank(number))
            return null;
        for (Room r : rooms()) {
            if (r.number.equalsIgnoreCase(number.trim()))
                return r;
        }
        return null;
    }

    public static synchronized boolean setRoomStatus(String roomId, String newStatus) {
        return updateCell("rooms.csv", "roomId", roomId, "status", newStatus);
    }

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