package logic;

public class GuestRoomLogic {
    private final String roomName;
    private final String roomType;
    private final String roomPrice;
    private final String roomStatus;

    public enum BookResult { OK, PENDING_EXISTS, ALREADY_HAS_ROOM, ROOM_NOT_AVAILABLE, ROOM_NOT_FOUND }

    public GuestRoomLogic(String roomName, String roomType, String roomPrice, String roomStatus) {
        this.roomName = roomName;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.roomStatus = roomStatus;
    }

    public String getRoomName() { return roomName; }
    public String getRoomType() { return roomType; }
    public String getRoomPrice() { return roomPrice; }
    public String getRoomStatus() { return roomStatus; }

    /** ดึงเลขห้องอย่างเดียว เช่น "101" จาก "ห้อง 101" */
    public String getRoomNumberOnly() {
        return roomName.replace("ห้อง ", "").trim();
    }

    /** ดึงข้อมูลชั้นของห้องพัก */
    public char getFloorChar() {
        String num = getRoomNumberOnly();
        return num.isEmpty() ? '1' : num.charAt(0);
    }

    /** ดึงรายการเฟอร์นิเจอร์และอุปกรณ์ประจำห้องแยกตามประเภทห้องพัก */
    public String[] getFurnitureList() {
        if (roomType != null && (roomType.contains("แอร์") || roomType.equalsIgnoreCase("Air"))) {
            return new String[]{
                "- เตียงนอนเดี่ยว 3.5 ฟุต พร้อมฟูก (2 ชุด)",
                "- ตู้เสื้อผ้าบิวท์อิน 2 บาน",
                "- โต๊ะทำงานและเก้าอี้ (2 ชุด)",
                "- เครื่องปรับอากาศ (Air Conditioner)",
                "- ตู้เย็นขนาด 6.0 คิว",
                "- เครื่องทำน้ำอุ่นในห้องน้ำส่วนตัว",
                "- Wi-Fi ความเร็วสูงฟรี"
            };
        } else {
            return new String[]{
                "- เตียงนอนเดี่ยว 3.5 ฟุต พร้อมฟูก (2 ชุด)",
                "- ตู้เสื้อผ้าบิวท์อิน 2 บาน",
                "- โต๊ะทำงานและเก้าอี้ (2 ชุด)",
                "- พัดลมติดเพดาน",
                "- เครื่องทำน้ำอุ่นในห้องน้ำส่วนตัว",
                "- Wi-Fi ความเร็วสูงฟรี"
            };
        }
    }

    /** ตรวจสอบสถานะว่าห้องว่างหรือไม่ */
    public boolean isAvailable() {
        return roomStatus != null && roomStatus.contains("ว่าง");
    }

    /** ดึงผู้ใช้ปัจจุบัน */
    public LogicLogin.User getCurrentUser() {
        return LogicLogin.getCurrentUser();
    }

    /** ส่งคำขอจองห้องพัก */
    public BookResult bookRoom(LogicLogin.User user) {
        if (user == null) return BookResult.ROOM_NOT_FOUND;

        // 1. ตรวจสอบว่าผู้ใช้มีห้องพักอยู่แล้วหรือไม่
        if (!DataStore.blank(user.roomId)) {
            return BookResult.ALREADY_HAS_ROOM;
        }

        String roomNumOnly = getRoomNumberOnly();
        DataStore.Room r = DataStore.roomByNumber(roomNumOnly);
        if (r == null || !r.status.equalsIgnoreCase("AVAILABLE")) {
            return BookResult.ROOM_NOT_AVAILABLE;
        }

        // 2. ตรวจสอบว่ามีคำขอจองค้างอยู่แล้วหรือไม่
        String fullName = DataStore.blank(user.fullName) ? user.username : user.fullName;
        if (DataStore.hasPendingBookingByName(fullName)) {
            return BookResult.PENDING_EXISTS;
        }

        // 3. บันทึกข้อมูลคำขอจองลงใน bookings.csv
        String phone = DataStore.blank(user.phone) ? "-" : user.phone;
        boolean saved = DataStore.saveBooking(r.id, fullName, phone);
        if (!saved) return BookResult.ROOM_NOT_FOUND;

        // 4. อัปเดตสถานะห้องใน rooms.csv ให้เป็น RESERVED
        DataStore.setRoomStatus(r.id, "RESERVED");

        return BookResult.OK;
    }
}