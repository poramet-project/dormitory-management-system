package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Logic สำหรับจัดการห้องพัก (อ่าน/เพิ่ม/อัปเดต ข้อมูลเฉพาะไฟล์ rooms.csv เท่านั้น)
 */
public class AdminRoomLogic {

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
            this.statusRaw = r.status;

            if ("AVAILABLE".equalsIgnoreCase(r.status) || "RESERVED".equalsIgnoreCase(r.status) || "PENDING".equalsIgnoreCase(r.status)) {
                this.statusThai = "ห้องว่าง";
            } else if ("OCCUPIED".equalsIgnoreCase(r.status)) {
                this.statusThai = "มีผู้เช่า";
            } else {
                this.statusThai = "ปิดปรับปรุง";
            }
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
     * บันทึกหรืออัปเดตข้อมูลห้องพักในไฟล์ rooms.csv
     */
    public static boolean saveOrUpdateRoom(String roomId, String roomNumber, String roomType, int rent, String status) throws IOException {
        String typeVal = roomType.contains("แอร์") ? "Air" : "Fan";
        
        String statusVal;
        String sUpper = status != null ? status.trim().toUpperCase() : "";
        
        if (sUpper.contains("ว่าง") || sUpper.equals("AVAILABLE")) {
            statusVal = "AVAILABLE";
        } else if (sUpper.contains("มีผู้เช่า") || sUpper.equals("OCCUPIED")) {
            statusVal = "OCCUPIED";
        } else {
            statusVal = "MAINTENANCE";
        }

        String targetNumber = roomNumber.trim().replace("ห้อง ", "");

        // 🔴 กรณีเพิ่มห้องใหม่ (roomId เป็นค่าว่าง)
        if (DataStore.blank(roomId)) {
            String newRoomId = "R" + targetNumber;

            // ตรวจสอบเงื่อนไขห้ามเลขห้องพักซ้ำ หรือ รหัสห้องพักซ้ำ
            for (DataStore.Room existingRoom : DataStore.rooms()) {
                if (existingRoom.number.equalsIgnoreCase(targetNumber)) {
                    throw new IllegalStateException("ไม่สามารถเพิ่มได้ เนื่องจากมีเลขห้อง " + targetNumber + " อยู่ในระบบแล้ว");
                }
                if (existingRoom.id.equalsIgnoreCase(newRoomId)) {
                    throw new IllegalStateException("ไม่สามารถเพิ่มได้ เนื่องจากมีรหัสห้อง " + newRoomId + " อยู่ในระบบแล้ว");
                }
            }

            // บันทึกห้องใหม่ต่อท้ายไฟล์ rooms.csv ผ่าน DataStore.appendRow
            String header = "roomId,roomNumber,roomType,baseRent,status";
            DataStore.appendRow("rooms.csv", header, newRoomId, targetNumber, typeVal, String.valueOf(rent), statusVal);
            return true;
        }

        // 🔴 กรณีแก้ไขห้องเดิม
        // ตรวจสอบห้ามเปลี่ยนเลขห้องไปซ้ำกับห้องอื่น
        for (DataStore.Room existingRoom : DataStore.rooms()) {
            if (!existingRoom.id.equalsIgnoreCase(roomId.trim()) && existingRoom.number.equalsIgnoreCase(targetNumber)) {
                throw new IllegalStateException("ไม่สามารถแก้ไขเป็นเลขห้อง " + targetNumber + " ได้ เนื่องจากมีห้องนี้อยู่ในระบบแล้ว");
            }
        }

        // ป้องกันการเปลี่ยนสถานะ: ห้ามเปลี่ยนเป็น "ห้องว่าง" หรือ "ปิดปรับปรุง" หากยังมีสัญญาเช่า ACTIVE
        if ("AVAILABLE".equals(statusVal) || "MAINTENANCE".equals(statusVal)) {
            for (DataStore.Contract c : DataStore.contracts()) {
                if (c.roomId.equalsIgnoreCase(roomId.trim()) && c.isActive()) {
                    String statusTarget = "AVAILABLE".equals(statusVal) ? "ห้องว่าง" : "ปิดปรับปรุง";
                    throw new IllegalStateException("ไม่สามารถเปลี่ยนสถานะเป็น '" + statusTarget + "' ได้ เนื่องจากห้องนี้ยังมีผู้เช่าและสัญญาเช่ายังดำเนินการอยู่ (ACTIVE)");
                }
            }
        }

        // อัปเดตข้อมูลในไฟล์ rooms.csv
        DataStore.updateCell("rooms.csv", "roomId", roomId, "roomNumber", targetNumber);
        DataStore.updateCell("rooms.csv", "roomId", roomId, "roomType", typeVal);
        DataStore.updateCell("rooms.csv", "roomId", roomId, "baseRent", String.valueOf(rent));
        return DataStore.setRoomStatus(roomId, statusVal);
    }
}