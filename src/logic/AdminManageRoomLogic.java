package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Logic สำหรับจัดการห้องพัก (อ่าน/เพิ่ม/อัปเดต ข้อมูลเฉพาะไฟล์ rooms.csv เท่านั้น)
 */
public class AdminManageRoomLogic {

    public static class RoomModel {
        public final String id;
        public final String number;
        public final String floor;
        public final String type;
        public final String rent;
        public final String statusThai;
        public final String statusRaw;

        public RoomModel(DataStore.Room r) {
            this.id = r.id;
            this.number = "ห้อง " + r.number;
            this.floor = "ชั้น " + r.floor();
            this.type = r.typeShort();
            this.rent = DataStore.money(r.rent);
            this.statusThai = r.statusThai();
            this.statusRaw = r.status;
        }
    }

    /** ดึงรายการห้องพักทั้งหมดจาก rooms.csv แปลงเป็น Model สำหรับแสดงตาราง */
    public static List<RoomModel> getAllRooms() {
        List<RoomModel> list = new ArrayList<>();
        for (DataStore.Room r : DataStore.rooms()) {
            list.add(new RoomModel(r));
        }
        return list;
    }

    /**
     * บันทึกหรืออัปเดตข้อมูลห้องพักเฉพาะไฟล์ rooms.csv
     */
    public static void saveOrUpdateRoom(String roomId, String roomNumber, String roomType, int rent, String status) throws IOException {
        String typeVal = roomType.contains("แอร์") ? "Air" : "Fan";
        
        // แปลงค่าสถานะภาษาไทย/อังกฤษ ให้เป็น Enum ใน rooms.csv
        String statusVal;
        if (status.contains("ว่าง") || "AVAILABLE".equalsIgnoreCase(status)) {
            statusVal = "AVAILABLE";
        } else if (status.contains("มีผู้เช่า") || "OCCUPIED".equalsIgnoreCase(status)) {
            statusVal = "OCCUPIED";
        } else {
            statusVal = "MAINTENANCE";
        }

        // หากเป็นห้องใหม่ที่ยังไม่มี ID ให้สร้าง ID ขึ้นมาใหม่ เช่น R101
        if (DataStore.blank(roomId)) {
            roomId = "R" + roomNumber;
        }

        // อัปเดตข้อมูลเฉพาะไฟล์ rooms.csv
        DataStore.updateCell("rooms.csv", "roomId", roomId, "roomNumber", roomNumber);
        DataStore.updateCell("rooms.csv", "roomId", roomId, "roomType", typeVal);
        DataStore.updateCell("rooms.csv", "roomId", roomId, "baseRent", String.valueOf(rent));
        DataStore.setRoomStatus(roomId, statusVal);
    }
}