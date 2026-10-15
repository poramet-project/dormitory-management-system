package logic;
/**
 * GuestRoomLogic คือคลาสสำหรับจัดการข้อมูลรายละเอียดห้องพักและตรรกะการจองห้องพักสำหรับผู้เยี่ยมชม
 * roomName คือ ชื่อ/หมายเลขห้องพัก (เช่น ห้อง 101)
 * roomType คือ ประเภทห้องพัก (เช่น ห้องแอร์, ห้องพัดลม)
 * roomPrice คือ อัตราค่าเช่าต่อเดือน
 * roomStatus คือ สถานะความพร้อมของห้องพัก (เช่น ว่าง, มีผู้เช่า)
 */
public class GuestRoomLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF(roomName, roomType, roomPrice, roomStatus) คือ ตรรกะแสดงรายละเอียดและการยื่นคำขอจองห้องพัก 1 ห้อง
    // RI
    // 1. roomName, roomType, roomPrice, roomStatus ต้องไม่เป็น null
    // 2. roomName ต้องมีหมายเลขห้องระบุอยู่

    private final String roomName;
    private final String roomType;
    private final String roomPrice;
    private final String roomStatus;

    public enum BookResult { OK, PENDING_EXISTS, ALREADY_HAS_ROOM, ROOM_NOT_AVAILABLE, ROOM_NOT_FOUND }

    /**
     * คอนสตรักเตอร์สำหรับสร้างตรรกะข้อมูลรายละเอียดห้องพัก
     * 
     * @param roomName ชื่อหรือข้อความหมายเลขห้องพัก เช่น "ห้อง 101"
     * @param roomType ประเภทห้องพัก เช่น "ห้องแอร์", "ห้องพัดลม"
     * @param roomPrice ข้อความอัตราค่าเช่า
     * @param roomStatus ข้อความสถานะห้องพัก
     */
    public GuestRoomLogic(String roomName, String roomType, String roomPrice, String roomStatus) {
        this.roomName = roomName;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.roomStatus = roomStatus;
    }

    /**
     * ดึงชื่อห้องพัก
     * 
     * @return ชื่อห้องพัก เช่น "ห้อง 101"
     */
    public String getRoomName() { return roomName; }

    /**
     * ดึงประเภทห้องพัก
     * 
     * @return ประเภทห้องพัก เช่น "ห้องแอร์"
     */
    public String getRoomType() { return roomType; }

    /**
     * ดึงข้อความอัตราค่าเช่าห้องพัก
     * 
     * @return อัตราค่าเช่า
     */
    public String getRoomPrice() { return roomPrice; }

    /**
     * ดึงสถานะห้องพัก
     * 
     * @return สถานะห้องพัก
     */
    public String getRoomStatus() { return roomStatus; }

    /**
     * ดึงเฉพาะตัวเลขห้องพักโดยตัดคำว่า "ห้อง " ออก
     * 
     * @return หมายเลขห้องพัก เช่น "101"
     */
    public String getRoomNumberOnly() {
        return roomName.replace("ห้อง ", "").trim();
    }

    /**
     * ดึงตัวอักษรระบุชั้นจากตัวเลขห้องพัก
     * 
     * @return ตัวอักษรระบุชั้น เช่น '1', '2'
     */
    public char getFloorChar() {
        String num = getRoomNumberOnly();
        return num.isEmpty() ? '1' : num.charAt(0);
    }

    /**
     * ดึงรายการสิ่งอำนวยความสะดวกและเฟอร์นิเจอร์ภายในห้องพักตามประเภทห้อง
     * 
     * @return อาร์เรย์ของรายการสิ่งอำนวยความสะดวก
     */
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

    /**
     * ตรวจสอบว่าห้องพักนี้มีสถานะว่างพร้อมให้จองหรือไม่
     * 
     * @return true หากห้องว่าง
     */
    public boolean isAvailable() {
        return roomStatus != null && roomStatus.contains("ว่าง");
    }

    /**
     * ดึงข้อมูลผู้ใช้งานที่กำลังเข้าสู่ระบบอยู่
     * 
     * @return ออบเจ็กต์ User หรือ null
     */
    public LoginLogic.User getCurrentUser() {
        return LoginLogic.getCurrentUser();
    }

    /**
     * ตรวจสอบว่าผู้ใช้งานปัจจุบันเป็นผู้ส่งคำขอจองห้องนี้และกำลังรออนุมัติอยู่หรือไม่
     * 
     * @return true หากผู้ใช้ปัจจุบันมีคำขอจองค้างอยู่สำหรับห้องนี้
     */
    public boolean isPendingByCurrentUser() {
        LoginLogic.User u = getCurrentUser();
        if (u == null) return false;
        String fullName = DataStore.blank(u.fullName) ? u.username : u.fullName;
        DataStore.Room r = DataStore.roomByNumber(getRoomNumberOnly());
        return r != null && DataStore.isRoomPendingByUser(r.id, fullName);
    }

    /**
     * ดำเนินการยื่นคำขอจองห้องพักสำหรับผู้ใช้งาน
     * 
     * @param user ผู้ใช้งานที่ต้องการจองห้อง
     * @return ผลลัพธ์ของการจองห้องพักในรูปแบบ BookResult
     */
    public BookResult bookRoom(LoginLogic.User user) {
        if (user == null) return BookResult.ROOM_NOT_FOUND;

        if (!DataStore.blank(user.roomId)) {
            return BookResult.ALREADY_HAS_ROOM;
        }

        String fullName = DataStore.blank(user.fullName) ? user.username : user.fullName;
        if (DataStore.hasPendingBookingByName(fullName) || DataStore.hasPendingBooking(user.userId)) {
            return BookResult.PENDING_EXISTS;
        }

        String roomNumOnly = getRoomNumberOnly();
        DataStore.Room r = DataStore.roomByNumber(roomNumOnly);
        if (r == null || !"AVAILABLE".equalsIgnoreCase(r.status)) {
            return BookResult.ROOM_NOT_AVAILABLE;
        }

        String phone = DataStore.blank(user.phone) ? "-" : user.phone;
        boolean saved = DataStore.saveBooking(r.id, fullName, phone);
        if (!saved) return BookResult.ROOM_NOT_FOUND;

        return BookResult.OK;
    }
}