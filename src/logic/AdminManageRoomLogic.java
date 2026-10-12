package logic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/** Logic สำหรับจัดการห้องพัก (อ่าน/เพิ่ม/อัปเดตข้อมูลใน rooms.csv) */
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

    /** บันทึกหรืออัปเดตข้อมูลห้องพักลงใน rooms.csv */
    public static void saveOrUpdateRoom(String roomId, String roomNumber, String roomType, int rent, String status) throws IOException {
        Path file = DataStore.file("rooms.csv").toPath();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<String> newLines = new ArrayList<>();

        boolean updated = false;
        String header = lines.isEmpty() ? "roomId,roomNumber,roomType,baseRent,status" : lines.get(0);
        newLines.add(header);

        // แปลงประเภทห้องกลับเป็นค่าดั้งเดิมใน CSV
        String typeVal = roomType.contains("แอร์") ? "Air" : "Fan";
        String statusVal = status.equals("ว่าง") ? "AVAILABLE" : (status.equals("มีผู้เช่า") ? "OCCUPIED" : "MAINTENANCE");

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split(",", -1);
            if (parts.length > 0 && parts[0].trim().equalsIgnoreCase(roomId)) {
                // แทนที่ข้อมูลเดิม
                newLines.add(String.join(",", roomId, roomNumber, typeVal, String.valueOf(rent), statusVal));
                updated = true;
            } else {
                newLines.add(line);
            }
        }

        // ถ้าไม่พบ ID เดิม ให้เพิ่มเป็นรายการใหม่
        if (!updated) {
            String newId = "R" + roomNumber;
            newLines.add(String.join(",", newId, roomNumber, typeVal, String.valueOf(rent), statusVal));
        }

        Files.write(file, newLines, StandardCharsets.UTF_8);
    }
}