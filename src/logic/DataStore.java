package logic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DataStore คือคลาสศูนย์กลางสำหรับจัดการการอ่านและเขียนข้อมูลของระบบหอพัก
 * ทำหน้าที่เป็น Facade เชื่อมโยงระหว่าง Service และโมเดลข้อมูลหลัก
 */
public final class DataStore {

    // ===== representation =====
    // Abstraction Function:
    // AF(currentLoginUser) คือ
    // คลังข้อมูลหลักและกลไกเข้าถึงฐานข้อมูลสำหรับระบบจัดการหอพัก
    // RI
    // 1. currentLoginUser ต้องไม่เป็น null

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

    // ===================== จัดการไฟล์และ CSV (ส่งต่อให้ CSVService)
    // =====================

    public static File getDataDirectory() {
        return CSVService.getDataDirectory();
    }

    public static File file(String name) {
        return CSVService.file(name);
    }

    static String[] parseLine(String line) {
        return CSVService.parseLine(line);
    }

    public static synchronized List<Map<String, String>> readTable(String name) {
        return CSVService.readTable(name);
    }

    static String csvEscape(String v) {
        return CSVService.csvEscape(v);
    }

    public static synchronized void appendRow(String name, String header, String... values) throws IOException {
        CSVService.appendRow(name, header, values);
    }

    public static synchronized String nextId(String name, String idColumn, String prefix, int digits) {
        return CSVService.nextId(name, idColumn, prefix, digits);
    }

    public static synchronized boolean updateCell(String name, String keyCol, String keyVal, String col,
            String newVal) {
        return CSVService.updateCell(name, keyCol, keyVal, col, newVal);
    }

    // ===================== ตัวช่วยจัดรูปแบบและเปรียบเทียบ (ส่งต่อให้
    // DataFormatUtils) =====================

    public static boolean blank(String s) {
        return DataFormatUtils.blank(s);
    }

    static int toInt(String s) {
        return DataFormatUtils.toInt(s);
    }

    public static String money(int v) {
        return DataFormatUtils.money(v);
    }

    public static String thaiDate(String raw) {
        return DataFormatUtils.thaiDate(raw);
    }

    public static String thaiTime(String raw) {
        return DataFormatUtils.thaiTime(raw);
    }

    public static int compareRoomNumbers(String n1, String n2) {
        return DataFormatUtils.compareRoomNumbers(n1, n2);
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
        if (blank(fullName))
            return false;
        for (Map<String, String> m : readTable(BOOKINGS_FILE)) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", "")) &&
                    m.getOrDefault("fullName", "").equalsIgnoreCase(fullName.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRoomPendingByUser(String roomId, String fullName) {
        if (blank(roomId) || blank(fullName))
            return false;
        for (Map<String, String> m : readTable(BOOKINGS_FILE)) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", "")) &&
                    roomId.equalsIgnoreCase(m.getOrDefault("roomId", "")) &&
                    fullName.trim().equalsIgnoreCase(m.getOrDefault("fullName", "").trim())) {
                return true;
            }
        }
        return false;
    }

    // ===================== ข้อมูลห้องพัก (Room Model & Data Access)
    // =====================

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
                    return "ปิดปรับปรุง";
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

    public static synchronized void sortRoomsFile() {
        File f = file("rooms.csv");
        if (!f.exists())
            return;
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

    public static Room roomByNumber(String number) {
        if (blank(number))
            return null;
        for (Room r : rooms()) {
            if (r.number.equalsIgnoreCase(number.trim()))
                return r;
        }
        return null;
    }

    public static List<Room> availableRooms(String excludeRoomId) {
        List<Room> list = new ArrayList<>();
        String exclude = excludeRoomId == null ? "" : excludeRoomId.trim();
        for (Room r : rooms()) {
            if ("AVAILABLE".equalsIgnoreCase(r.status) && !r.id.equalsIgnoreCase(exclude)) {
                list.add(r);
            }
        }
        return list;
    }

    public static synchronized boolean setRoomStatus(String roomId, String newStatus) {
        return updateCell("rooms.csv", "roomId", roomId, "status", newStatus);
    }

    public static synchronized void cascadeUpdateRoomId(String oldRoomId, String newRoomId) {
        if (oldRoomId == null || newRoomId == null || oldRoomId.trim().equalsIgnoreCase(newRoomId.trim())) {
            return;
        }
        String oldId = oldRoomId.trim();
        String newId = newRoomId.trim();

        updateCell(CONTRACTS_FILE, "roomId", oldId, "roomId", newId);
        updateCell("users.csv", "roomId", oldId, "roomId", newId);
        updateCell(BOOKINGS_FILE, "roomId", oldId, "roomId", newId);
        updateCell(COMPLAINTS_FILE, "roomId", oldId, "roomId", newId);
        updateCell("room_transfers.csv", "oldRoomId", oldId, "oldRoomId", newId);
        updateCell("room_transfers.csv", "newRoomId", oldId, "newRoomId", newId);
    }

    public static synchronized boolean updateRoomFull(String oldRoomId, String newRoomId, String roomNumber,
            String roomType, int rent, String status) {
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
                if (idCol < 0) {
                    for (int i = 0; i < c.length; i++) {
                        String h = c[i].trim();
                        if (h.equals("roomId"))
                            idCol = i;
                        else if (h.equals("roomNumber"))
                            numCol = i;
                        else if (h.equals("roomType"))
                            typeCol = i;
                        else if (h.equals("baseRent"))
                            rentCol = i;
                        else if (h.equals("status"))
                            stCol = i;
                    }
                    out.add(line);
                    continue;
                }
                if (idCol >= 0 && idCol < c.length && c[idCol].trim().equalsIgnoreCase(oldRoomId.trim())) {
                    c[idCol] = newRoomId;
                    if (numCol >= 0 && numCol < c.length)
                        c[numCol] = roomNumber;
                    if (typeCol >= 0 && typeCol < c.length)
                        c[typeCol] = roomType;
                    if (rentCol >= 0 && rentCol < c.length)
                        c[rentCol] = String.valueOf(rent);
                    if (stCol >= 0 && stCol < c.length)
                        c[stCol] = status;
                    changed = true;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < c.length; i++) {
                    if (i > 0)
                        sb.append(',');
                    sb.append(csvEscape(c[i]));
                }
                out.add(sb.toString());
            }
            if (!changed)
                return false;
            Files.write(f.toPath(), (String.join(System.lineSeparator(), out) + System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8));

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

    // ===================== สัญญาเช่า (Contract Model & Data Access)
    // =====================

    public static final class Contract {
        // ===== representation =====
        // Abstraction Function:
        // AF(id, userId, roomId, start, end, status, rent, deposit) คือ
        // สัญญาเช่าห้องพัก 1 ฉบับ
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
        String uId = blank(user.userId) ? "" : user.userId.trim();
        String uRoom = blank(user.roomId) ? "" : user.roomId.trim();

        // 1. ลองหาสัญญาที่ ACTIVE ของห้องปัจจุบันก่อน
        if (!blank(uRoom) && !"-".equals(uRoom)) {
            Room r = room(uRoom);
            if (r == null)
                r = roomByNumber(uRoom);
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

    public static synchronized boolean saveContract(String userId, String roomId, int monthlyRent, int deposit,
            String status) {
        try {
            String contractId = nextId(CONTRACTS_FILE, "contractId", "CNT", 3);
            String startDate = LocalDate.now().toString();
            String endDate = LocalDate.now().plusYears(1).toString();
            appendRow(CONTRACTS_FILE, CONTRACTS_HEADER, contractId, userId, roomId, startDate, endDate,
                    String.valueOf(monthlyRent), String.valueOf(deposit), status);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized boolean saveContract(String userId, String roomId, int monthlyRent, String status) {
        Room r = room(roomId);
        if (r == null)
            r = roomByNumber(roomId);
        int dep = (r != null && (r.isAir() || (r.type != null && r.type.toLowerCase().contains("air")))) ? 7000
                : (monthlyRent * 2);
        return saveContract(userId, roomId, monthlyRent, dep, status);
    }

    public static synchronized boolean saveContract(String userId, String roomId, int monthlyRent, int deposit) {
        return saveContract(userId, roomId, monthlyRent, deposit, "ACTIVE");
    }

    public static synchronized boolean saveContract(String userId, String roomId, int monthlyRent) {
        return saveContract(userId, roomId, monthlyRent, "ACTIVE");
    }

    public static boolean hasPendingBooking(String userId) {
        if (blank(userId))
            return false;
        for (Contract c : contracts()) {
            if (c.status.equals("PENDING") && c.userId.equalsIgnoreCase(userId.trim())) {
                return true;
            }
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

    private static Contract findPendingContract(String contractId) {
        if (contractId == null)
            return null;
        for (Contract x : pendingContracts()) {
            if (x.id.equalsIgnoreCase(contractId.trim()))
                return x;
        }
        return null;
    }

    public static synchronized boolean approveBooking(String contractId) {
        Contract c = findPendingContract(contractId);
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
        Contract c = findPendingContract(contractId);
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

    // ===================== แจ้งซ่อม / ร้องเรียน (Complaint Model & Data Access)
    // =====================

    public static final class Complaint {
        // ===== representation =====
        // Abstraction Function:
        // AF(id, userId, roomId, category, topic, description, date, status) คือ
        // ข้อมูลรายการแจ้งซ่อมหรือร้องเรียน 1 รายการ
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
        if (blank(userId))
            return new ArrayList<>();
        LoginLogic.User u = LoginLogic.findUser(userId);
        String rId = (u != null) ? u.roomId : null;
        if (blank(rId) || "-".equals(rId.trim())) {
            Contract c = contractFor(u);
            if (c != null && c.isActive())
                rId = c.roomId;
        }
        return complaintsForUserAndRoom(userId, rId);
    }

    public static List<Complaint> complaintsForUserAndRoom(String userId, String roomId) {
        List<Complaint> list = new ArrayList<>();
        if (blank(userId) || blank(roomId) || "-".equals(roomId.trim()))
            return list;
        Room r = room(roomId);
        if (r == null)
            r = roomByNumber(roomId);
        String curId = r != null ? r.id : roomId.trim();
        String curNum = r != null ? r.number : roomId.trim();

        for (Map<String, String> m : readTable(COMPLAINTS_FILE)) {
            Complaint c = new Complaint(m);
            boolean userMatch = c.userId.equalsIgnoreCase(userId.trim());
            boolean roomMatch = c.roomId.equalsIgnoreCase(curId) || c.roomId.equalsIgnoreCase(curNum);
            if (userMatch && roomMatch)
                list.add(c);
        }
        return list;
    }
}