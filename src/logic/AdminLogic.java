package logic;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AdminLogic
 * คือคลาสสำหรับคำนวณและประมวลผลข้อมูลสถิติของแผงควบคุมหลักผู้ดูแลระบบ (Admin
 * Dashboard)
 * คำนวณจำนวนห้องว่าง ห้องเต็ม อัตราการเข้าพัก คำขอรอดำเนินการ
 * และสถิติการเข้าพักรายเดือนจริง
 */
public class AdminLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF() คือ ระบบคำนวณและประมวลผลข้อมูลภาพรวมของหอพักจาก DataStore (rooms,
    // contracts, users, complaints, bookings)
    // RI
    // 1. DataStore ต้องพร้อมใช้งานและสามารถอ่านไฟล์ CSV ที่เกี่ยวข้องได้
    // 2. จำนวนห้องพักที่คำนวณได้ต้องมีค่าไม่น้อยกว่า 0

    /**
     * RequestItemModel
     * คือคลาสสำหรับเก็บข้อมูลคำร้องรอดำเนินการแต่ละรายการที่นำไปแสดงในการ์ดคำร้อง
     * tag คือ ป้ายกำกับประเภทคำร้อง (เช่น แจ้งซ่อม, ย้ายห้อง, จองห้อง)
     * room คือ หมายเลขห้องพักที่เกี่ยวข้อง
     * desc คือ รายละเอียดโดยสังเขปของคำร้อง
     * time คือ วันที่หรือเวลาที่ยื่นคำร้อง
     * type คือ รหัสระบุประเภทคำร้อง ("repair", "move", "booking")
     */
    public static class RequestItemModel {

        // ===== representation =====
        // Abstraction Function:
        // AF(tag, room, desc, time, type) คือ รายการคำร้องที่รอดำเนินการ 1 รายการ
        // RI
        // 1. tag, room, desc, time, type ต้องไม่เป็น null
        // 2. type ต้องเป็น "repair", "move", หรือ "booking"

        public final String tag;
        public final String room;
        public final String desc;
        public final String time;
        public final String type; // repair, move, contract

        /**
         * คอนสตรักเตอร์สำหรับสร้างอ็อบเจกต์คำร้องรอดำเนินการ
         *
         * @param tag  ป้ายกำกับประเภทคำร้อง
         * @param room หมายเลขห้องพักที่เกี่ยวข้อง
         * @param desc คำอธิบายหรือรายละเอียดของคำร้อง
         * @param time วันที่หรือเวลาที่ยื่นคำร้อง
         * @param type ชนิดของคำร้อง ("repair", "move", "booking")
         */
        public RequestItemModel(String tag, String room, String desc, String time, String type) {
            this.tag = tag;
            this.room = room;
            this.desc = desc;
            this.time = time;
            this.type = type;
        }
    }

    /**
     * MonthlyOccupancy คือคลาสสำหรับเก็บข้อมูลสถิติอัตราการเข้าพักรายเดือน
     * months คือ รายชื่อเดือนย่อภาษาไทย 6 เดือนล่าสุด (เช่น "พ.ค.", "ต.ค.")
     * counts คือ จำนวนห้องที่มีผู้เข้าพักจริงในแต่ละเดือน
     */
    public static class MonthlyOccupancy {

        // ===== representation =====
        // Abstraction Function:
        // AF(months, counts) คือ ข้อมูลคู่ลำดับเดือนและจำนวนห้องที่มีผู้เข้าพักจริง
        // RI
        // 1. months และ counts ต้องไม่เป็น null
        // 2. ขนาดความยาวของ months และ counts ต้องเท่ากัน
        // 3. ค่าทุกตัวใน counts ต้อง >= 0

        public final String[] months;
        public final int[] counts;

        /**
         * คอนสตรักเตอร์สำหรับสร้างข้อมูลสถิติการเข้าพักรายเดือน
         *
         * @param months อาร์เรย์รายชื่อเดือน
         * @param counts อาร์เรย์จำนวนห้องที่มีผู้เข้าพักจริงในแต่ละเดือน
         */
        public MonthlyOccupancy(String[] months, int[] counts) {
            this.months = months;
            this.counts = counts;
        }
    }

    /**
     * ดึงสถิติจำนวนห้องพักทั้งหมดที่มีอยู่ในหอพัก
     *
     * @return จำนวนห้องพักทั้งหมดจากฐานข้อมูล rooms.csv
     */
    public static int getTotalRooms() {
        return DataStore.rooms().size();
    }

    /**
     * ดึงจำนวนห้องพักที่มีผู้เช่าหรือผู้เข้าพักอาศัยจริงในปัจจุบัน
     * โดยตรวจนับจากห้องที่มีสถานะ OCCUPIED, สัญญาเช่าที่ยังใช้งานอยู่ (ACTIVE),
     * และผู้ใช้ที่มีบทบาทเป็นผู้เช่า
     *
     * @return จำนวนห้องที่ไม่ซ้ำกันซึ่งมีผู้เข้าพักจริง
     */
    public static int getOccupiedRoomsCount() {
        Set<String> occupied = new HashSet<>();
        for (DataStore.Room r : DataStore.rooms()) {
            if ("OCCUPIED".equalsIgnoreCase(r.status)) {
                occupied.add(r.id.toUpperCase());
            }
        }
        for (DataStore.Contract c : DataStore.contracts()) {
            if (c.isActive() && !DataStore.blank(c.roomId)) {
                occupied.add(c.roomId.toUpperCase());
            }
        }
        for (Map<String, String> u : DataStore.readTable("users.csv")) {
            if ("TENANT".equalsIgnoreCase(u.getOrDefault("role", ""))) {
                String rId = u.getOrDefault("roomId", "");
                if (!DataStore.blank(rId)) {
                    occupied.add(rId.toUpperCase());
                }
            }
        }
        return occupied.size();
    }

    /**
     * ดึงจำนวนห้องว่างที่พร้อมเปิดให้เช่าพักจริง
     * คำนวณจากจำนวนห้องทั้งหมดลบด้วยจำนวนห้องที่มีผู้เข้าพักจริง
     *
     * @return จำนวนห้องว่างในปัจจุบัน
     */
    public static int getAvailableRoomsCount() {
        int total = getTotalRooms();
        int occupied = getOccupiedRoomsCount();
        return Math.max(0, total - occupied);
    }

    /**
     * ดึงจำนวนผู้เข้าพักหรือผู้เช่าทั้งหมดที่มีอยู่ในระบบ
     * ตรวจนับจากบัญชีผู้ใช้งานที่มีบทบาท TENANT
     * และผู้เช่าตามสัญญาเช่าที่ยังใช้งานอยู่
     *
     * @return จำนวนผู้เช่าจริงทั้งหมดในระบบ
     */
    public static int getTenantCount() {
        Set<String> tenants = new HashSet<>();
        for (Map<String, String> u : DataStore.readTable("users.csv")) {
            if ("TENANT".equalsIgnoreCase(u.getOrDefault("role", ""))) {
                String uId = u.getOrDefault("userId", u.getOrDefault("username", ""));
                if (!DataStore.blank(uId)) {
                    tenants.add(uId.toUpperCase());
                }
            }
        }
        for (DataStore.Contract c : DataStore.contracts()) {
            if (c.isActive() && !DataStore.blank(c.userId)) {
                tenants.add(c.userId.toUpperCase());
            }
        }
        return tenants.size();
    }

    /**
     * คำนวณอัตราการเข้าพักของหอพักในปัจจุบันเป็นร้อยละ (Percentage)
     *
     * @return เปอร์เซ็นต์อัตราการเข้าพัก (0 ถึง 100)
     */
    public static int getOccupancyRate() {
        int total = getTotalRooms();
        if (total == 0)
            return 0;
        return (getOccupiedRoomsCount() * 100) / total;
    }

    /**
     * แปลงสตริงข้อความวันที่เป็นอ็อบเจกต์ LocalDate โดยรองรับหลายรูปแบบวันที่และปี
     * พ.ศ./ค.ศ.
     *
     * @param raw ข้อความสตริงวันที่
     * @return อ็อบเจกต์ LocalDate หรือ null หากแปลงไม่สำเร็จ
     */
    private static LocalDate parseDateSafe(String raw) {
        if (DataStore.blank(raw))
            return null;
        String s = raw.trim().split("[ T]")[0];
        String[] patterns = { "yyyy-MM-dd", "yyyy-M-d", "d/M/yyyy", "dd/MM/yyyy", "yyyy/M/d" };
        for (String p : patterns) {
            try {
                LocalDate d = LocalDate.parse(s, DateTimeFormatter.ofPattern(p));
                if (d.getYear() > 2400) {
                    d = d.minusYears(543);
                }
                return d;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * ดึงข้อมูลประวัติและสถิติอัตราการเข้าพักรายเดือนย้อนหลังตามจำนวนเดือนที่ระบุ
     * โดยเดือนปัจจุบันจะใช้ข้อมูลผู้เข้าพักจริง
     * และเดือนในอดีตจะคำนวณจากระยะเวลาสัญญาเช่า
     *
     * @param numberOfMonths จำนวนเดือนย้อนหลังที่ต้องการดึงข้อมูล
     * @return อ็อบเจกต์ MonthlyOccupancy ที่บรรจุชื่อเดือนและจำนวนผู้เข้าพัก
     */
    public static MonthlyOccupancy getMonthlyOccupancyHistory(int numberOfMonths) {
        String[] TH_MONTHS = { "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
                "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค." };

        YearMonth currentYM = YearMonth.now();
        String[] months = new String[numberOfMonths];
        int[] counts = new int[numberOfMonths];

        List<DataStore.Contract> allContracts = DataStore.contracts();

        for (int i = 0; i < numberOfMonths; i++) {
            int monthsBack = (numberOfMonths - 1) - i;
            YearMonth ym = currentYM.minusMonths(monthsBack);
            months[i] = TH_MONTHS[ym.getMonthValue() - 1];

            if (monthsBack == 0) {
                // เดือนปัจจุบัน: สะท้อนตามจำนวนห้องที่มีผู้เข้าพักจริงในปัจจุบัน
                counts[i] = getOccupiedRoomsCount();
            } else {
                // เดือนในอดีต: ตรวจสอบจากสัญญาเช่าที่ครอบคลุมเดือนนั้นจริง
                LocalDate startOfMonth = ym.atDay(1);
                LocalDate endOfMonth = ym.atEndOfMonth();
                Set<String> occupiedInMonth = new HashSet<>();

                for (DataStore.Contract c : allContracts) {
                    if (DataStore.blank(c.roomId))
                        continue;
                    if ("REJECTED".equalsIgnoreCase(c.status) || "PENDING".equalsIgnoreCase(c.status))
                        continue;

                    LocalDate cStart = parseDateSafe(c.start);
                    LocalDate cEnd = parseDateSafe(c.end);

                    if (cStart != null) {
                        boolean started = !cStart.isAfter(endOfMonth);
                        boolean notEnded = (cEnd == null) || !cEnd.isBefore(startOfMonth);

                        if (started && notEnded) {
                            occupiedInMonth.add(c.roomId.toUpperCase());
                        }
                    }
                }
                counts[i] = occupiedInMonth.size();
            }
        }
        return new MonthlyOccupancy(months, counts);
    }

    /**
     * ดึงจำนวนคำขอรอดำเนินการรวมทั้งหมดในระบบ (คำขอจองห้อง + แจ้งซ่อม + ขอย้ายห้อง)
     *
     * @return จำนวนคำขอที่มีสถานะ PENDING ทั้งหมด
     */
    public static int getPendingRequestsCount() {
        int count = 0;
        for (Map<String, String> m : DataStore.readTable("bookings.csv")) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", "")))
                count++;
        }
        for (Map<String, String> m : DataStore.readTable("complaints.csv")) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", "")))
                count++;
        }
        for (Map<String, String> m : DataStore.readTable("room_transfers.csv")) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", "")))
                count++;
        }
        return count;
    }

    /**
     * ดึงรายการคำร้องที่รอดำเนินการทั้งหมดจาก bookings.csv, complaints.csv และ
     * room_transfers.csv
     * เพื่อนำไปแสดงในรายการแจ้งเตือนของแผงควบคุมหลัก
     *
     * @return รายการ List ของ RequestItemModel
     */
    public static List<RequestItemModel> getPendingRequests() {
        List<RequestItemModel> list = new ArrayList<>();

        // 1. ดึงจากคำขอจองห้อง
        for (Map<String, String> m : DataStore.readTable("bookings.csv")) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", ""))) {
                String roomId = m.getOrDefault("roomId", "-");
                DataStore.Room r = DataStore.room(roomId);
                String roomNum = r != null ? r.number : roomId.replace("R", "");
                String name = m.getOrDefault("fullName", "จองห้อง");
                String date = m.getOrDefault("requestDate", "เร็วๆ นี้");
                list.add(new RequestItemModel("จองห้อง", "ห้อง " + roomNum, "ผู้ยื่น: " + name,
                        DataStore.thaiDate(date), "booking"));
            }
        }

        // 2. ดึงจากแจ้งซ่อม
        for (Map<String, String> m : DataStore.readTable("complaints.csv")) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", ""))) {
                String roomId = m.getOrDefault("roomId", "-");
                String topic = m.getOrDefault("topic", "แจ้งซ่อม");
                String date = m.getOrDefault("requestDate", "เร็วๆ นี้");
                list.add(new RequestItemModel("แจ้งซ่อม", "ห้อง " + roomId, topic, DataStore.thaiDate(date), "repair"));
            }
        }

        // 3. ดึงจากขอย้ายห้อง
        for (Map<String, String> m : DataStore.readTable("room_transfers.csv")) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", ""))) {
                String oldRoom = m.getOrDefault("oldRoomId", "-");
                String reason = m.getOrDefault("reason", "ขอย้ายห้อง");
                String date = m.getOrDefault("requestDate", "เร็วๆ นี้");
                list.add(new RequestItemModel("ย้ายห้อง", "ห้อง " + oldRoom, reason, DataStore.thaiDate(date), "move"));
            }
        }

        return list;
    }
}