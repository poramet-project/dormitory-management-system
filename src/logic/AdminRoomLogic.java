package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminRoomLogic คือคลาสสำหรับจัดการตรรกะทางธุรกิจของข้อมูลห้องพักสำหรับผู้ดูแล
 * ให้บริการอ่านข้อมูลห้องพัก เพิ่มห้องพักใหม่ แก้ไขข้อมูลห้องพัก และอัปเดตสถานะห้องพักใน rooms.csv
 */
public class AdminRoomLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF() คือ ตรรกะการจัดการข้อมูลห้องพักทั้งหมดที่บันทึกอยู่ในไฟล์ rooms.csv
    // RI
    // 1. rooms.csv ต้องมีข้อมูลคอลัมน์ roomId, roomNumber, roomType, baseRent, status อย่างครบถ้วน
    // 2. ค่าเช่าห้องพัก (baseRent) ต้องเป็นจำนวนเต็มบวก

    /**
     * RoomModel คือคลาสสำหรับเก็บข้อมูลห้องพักที่ผ่านการจัดรูปแบบเพื่อแสดงผลบนตาราง
     * id คือ รหัสห้องพัก (เช่น R101)
     * number คือ หมายเลขห้องพัก (เช่น ห้อง 101)
     * floor คือ ชั้นที่ตั้งของห้องพัก (เช่น ชั้น 1)
     * type คือ ประเภทห้องพักภาษาไทย (ห้องแอร์, ห้องพัดลม)
     * rent คือ อัตราค่าเช่าพร้อมจัดรูปแบบตัวเลข (เช่น 2,800)
     * statusThai คือ สถานะห้องพักภาษาไทย (ห้องว่าง, มีผู้เช่า, ปิดปรับปรุง)
     * statusRaw คือ สถานะห้องพักภาษาอังกฤษตามฐานข้อมูล (AVAILABLE, OCCUPIED, ฯลฯ)
     */
    public static class RoomModel {

        // ===== representation =====
        // Abstraction Function:
        // AF(id, number, floor, type, rent, statusThai, statusRaw) คือ ข้อมูลจำลองของห้องพัก 1 ห้อง
        // RI
        // 1. id, number, floor, type, statusThai, statusRaw ต้องไม่เป็น null
        // 2. statusThai ต้องสอดคล้องกับ statusRaw

        public final String id;
        public final String number;
        public final String floor;
        public final String type;
        public final String rent;
        public final String statusThai;
        public final String statusRaw;

        /**
         * คอนสตรักเตอร์สำหรับสร้าง RoomModel จากอ็อบเจกต์ DataStore.Room
         *
         * @param r อ็อบเจกต์ Room ข้อมูลห้องพักจาก DataStore
         */
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

    /**
     * ดึงรายการห้องพักทั้งหมดจาก rooms.csv แปลงเป็น RoomModel สำหรับนำไปแสดงผลบนตาราง
     *
     * @return รายการ List ของ RoomModel ทั้งหมด
     */
    public static List<RoomModel> getAllRooms() {
        List<RoomModel> list = new ArrayList<>();
        for (DataStore.Room r : DataStore.rooms()) {
            list.add(new RoomModel(r));
        }
        return list;
    }

    /**
     * บันทึกข้อมูลห้องพักใหม่ หรืออัปเดตข้อมูลห้องพักเดิมลงในไฟล์ rooms.csv
     * พร้อมตรวจสอบเงื่อนไขความถูกต้อง (ห้ามเลขห้องซ้ำ, ห้ามเปลี่ยนสถานะเป็นว่างหากยังมีสัญญา ACTIVE)
     *
     * @param roomId รหัสห้องพัก (หากเป็นค่าว่างจะถือว่าเป็นการเพิ่มห้องใหม่)
     * @param roomNumber หมายเลขห้องพัก
     * @param roomType ประเภทห้องพัก (แอร์ หรือ พัดลม)
     * @param rent อัตราค่าเช่าต่อเดือน
     * @param status สถานะห้องพัก (ห้องว่าง, มีผู้เช่า, ปิดปรับปรุง)
     * @return true หากบันทึกข้อมูลสำเร็จ
     * @throws IOException หากเกิดข้อผิดพลาดในการอ่านหรือเขียนไฟล์
     * @throws IllegalStateException หากข้อมูลไม่ผ่านเงื่อนไขความถูกต้องของระบบ
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

            // บันทึกห้องใหม่ต่อท้ายไฟล์ rooms.csv แล้วจัดเรียงลำดับห้องทันที
            String header = "roomId,roomNumber,roomType,baseRent,status";
            DataStore.appendRow("rooms.csv", header, newRoomId, targetNumber, typeVal, String.valueOf(rent), statusVal);
            DataStore.sortRoomsFile();
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
        boolean ok = DataStore.setRoomStatus(roomId, statusVal);
        DataStore.sortRoomsFile();
        return ok;
    }
}