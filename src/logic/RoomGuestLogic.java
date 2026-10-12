package logic;

public class RoomGuestLogic {
    private final String roomName;
    private final String roomType;
    private final String roomPrice;
    private final String roomStatus;

    public enum BookResult { OK, PENDING_EXISTS, ALREADY_HAS_ROOM, ROOM_NOT_AVAILABLE, ROOM_NOT_FOUND }

    public RoomGuestLogic(String roomName, String roomType, String roomPrice, String roomStatus) {
        this.roomName = roomName;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.roomStatus = roomStatus;
    }

    public String getRoomName() { return roomName; }
    public String getRoomType() { return roomType; }
    public String getRoomPrice() { return roomPrice; }
    public String getRoomStatus() { return roomStatus; }

    public boolean isAvailable() {
        return roomStatus.contains("ว่าง");
    }

    public BookResult bookRoom(LogicLogin.User user) {
        if (user == null) return BookResult.ROOM_NOT_FOUND;

        // 1. ตรวจสอบว่าผู้ใช้มีห้องพักอยู่แล้วหรือไม่
        if (!DataStore.blank(user.roomId)) {
            return BookResult.ALREADY_HAS_ROOM;
        }

        String roomNumOnly = roomName.replace("ห้อง ", "").trim();
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