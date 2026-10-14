package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Logic สำหรับแผงควบคุมผู้ดูแลระบบ (Admin Dashboard) 
 * 
*/
public class AdminLogic {

    public static class RequestItemModel {
        public final String tag;
        public final String room;
        public final String desc;
        public final String time;
        public final String type; // repair, move, contract

        public RequestItemModel(String tag, String room, String desc, String time, String type) {
            this.tag = tag;
            this.room = room;
            this.desc = desc;
            this.time = time;
            this.type = type;
        }
    }

    /** ดึงสถิติจำนวนห้องพักทั้งหมด จาก rooms.csv จริง */
    public static int getTotalRooms() {
        return DataStore.rooms().size();
    }

    /** ดึงจำนวนห้องว่าง */
    public static int getAvailableRoomsCount() {
        int count = 0;
        for (DataStore.Room r : DataStore.rooms()) {
            if ("AVAILABLE".equalsIgnoreCase(r.status)) count++;
        }
        return count;
    }

    /** ดึงจำนวนห้องที่มีผู้เช่า */
    public static int getOccupiedRoomsCount() {
        int count = 0;
        for (DataStore.Room r : DataStore.rooms()) {
            if ("OCCUPIED".equalsIgnoreCase(r.status)) count++;
        }
        return count;
    }

    /** คำนวณอัตราการเข้าพักเป็นเปอร์เซ็นต์ */
    public static int getOccupancyRate() {
        int total = getTotalRooms();
        if (total == 0) return 0;
        return (getOccupiedRoomsCount() * 100) / total;
    }

    /** ดึงจำนวนคำขอรอดำเนินการรวม (แจ้งซ่อม + ขอย้ายห้อง) */
    public static int getPendingRequestsCount() {
        int count = 0;
        for (Map<String, String> m : DataStore.readTable("complaints.csv")) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", ""))) count++;
        }
        for (Map<String, String> m : DataStore.readTable("room_transfers.csv")) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", ""))) count++;
        }
        return count;
    }

    /** ดึงรายการคำร้องรอดำเนินการล่าสุดจาก complaints.csv และ room_transfers.csv */
    public static List<RequestItemModel> getPendingRequests() {
        List<RequestItemModel> list = new ArrayList<>();

        // 1. ดึงจากแจ้งซ่อม
        for (Map<String, String> m : DataStore.readTable("complaints.csv")) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", ""))) {
                String roomId = m.getOrDefault("roomId", "-");
                String topic = m.getOrDefault("topic", "แจ้งซ่อม");
                String date = m.getOrDefault("requestDate", "เร็วๆ นี้");
                list.add(new RequestItemModel("แจ้งซ่อม", "ห้อง " + roomId, topic, date, "repair"));
            }
        }

        // 2. ดึงจากขอย้ายห้อง
        for (Map<String, String> m : DataStore.readTable("room_transfers.csv")) {
            if ("PENDING".equalsIgnoreCase(m.getOrDefault("status", ""))) {
                String oldRoom = m.getOrDefault("oldRoomId", "-");
                String reason = m.getOrDefault("reason", "ขอย้ายห้อง");
                String date = m.getOrDefault("requestDate", "เร็วๆ นี้");
                list.add(new RequestItemModel("ย้ายห้อง", "ห้อง " + oldRoom, reason, date, "move"));
            }
        }

        return list;
    }
}