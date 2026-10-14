package logic;
/**
 * 
 * GuestRoomLogic
 */
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

    public String getRoomNumberOnly() {
        return roomName.replace("ห้อง ", "").trim();
    }

    public char getFloorChar() {
        String num = getRoomNumberOnly();
        return num.isEmpty() ? '1' : num.charAt(0);
    }

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

    public boolean isAvailable() {
        return roomStatus == null || !roomStatus.contains("มีผู้เช่า");
    }

    public LoginLogic.User getCurrentUser() {
        return LoginLogic.getCurrentUser();
    }

    /** เช็คว่า User ปัจจุบันเป็นคนส่งคำขอจองห้องนี้ค้างไว้หรือไม่ */
    public boolean isPendingByCurrentUser() {
        LoginLogic.User u = getCurrentUser();
        if (u == null) return false;
        String fullName = DataStore.blank(u.fullName) ? u.username : u.fullName;
        DataStore.Room r = DataStore.roomByNumber(getRoomNumberOnly());
        return r != null && DataStore.isRoomPendingByUser(r.id, fullName);
    }

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
        if (r == null || "OCCUPIED".equalsIgnoreCase(r.status)) {
            return BookResult.ROOM_NOT_AVAILABLE;
        }

        String phone = DataStore.blank(user.phone) ? "-" : user.phone;
        boolean saved = DataStore.saveBooking(r.id, fullName, phone);
        if (!saved) return BookResult.ROOM_NOT_FOUND;

        return BookResult.OK;
    }
}