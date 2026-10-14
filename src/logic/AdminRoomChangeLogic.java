package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Logic สำหรับจัดการคำขอย้ายห้องพัก
 * เมื่ออนุมัติคำขอ จะยกเลิกสัญญาเก่า (TERMINATED) และออกสัญญาใหม่ (ACTIVE, ประกัน x 2) ผูกกับห้องใหม่ทันที
 */
public class AdminRoomChangeLogic {

    public static class MoveModel {
        public String id, userId, name, phone, oldRoomId, oldRoomNum, newRoomId, newRoomNum, reason, date, status, detail;

        public MoveModel(String id, String userId, String name, String phone, String oldRoomId, String oldRoomNum,
                         String newRoomId, String newRoomNum, String reason, String date, String status, String detail) {
            this.id = id;
            this.userId = userId;
            this.name = name;
            this.phone = phone;
            this.oldRoomId = oldRoomId;
            this.oldRoomNum = oldRoomNum;
            this.newRoomId = newRoomId;
            this.newRoomNum = newRoomNum;
            this.reason = reason;
            this.date = date;
            this.status = status;
            this.detail = detail;
        }
    }

    /** ดึงรายการคำขอย้ายห้องทั้งหมดจาก room_transfers.csv */
    public List<MoveModel> getAllMoves() {
        List<MoveModel> list = new ArrayList<>();
        List<Map<String, String>> rows = DataStore.readTable("room_transfers.csv");

        for (Map<String, String> m : rows) {
            String tId = m.getOrDefault("transferId", "-").trim();
            String uId = m.getOrDefault("userId", "-").trim();
            String oldR = m.getOrDefault("oldRoomId", "-").trim();
            String newR = m.getOrDefault("newRoomId", "-").trim();
            String reason = m.getOrDefault("reason", "-").trim();
            String dateRaw = m.getOrDefault("requestDate", "-").trim();
            String stRaw = m.getOrDefault("status", "PENDING").trim();

            String dateThai = DataStore.thaiDate(dateRaw);
            String stThai = "APPROVED".equalsIgnoreCase(stRaw) ? "อนุมัติ" :
                           ("REJECTED".equalsIgnoreCase(stRaw) ? "ปฏิเสธ" : "รอพิจารณา");

            // ดึงชื่อและเบอร์ผู้ใช้
            LoginLogic.User u = LoginLogic.findUser(uId);
            String name = u != null ? u.fullName : uId;
            String phone = u != null ? u.phone : "-";

            // ดึงเลขห้อง
            DataStore.Room rOld = DataStore.room(oldR);
            DataStore.Room rNew = DataStore.room(newR);
            String oldNum = rOld != null ? rOld.number : oldR;
            String newNum = rNew != null ? rNew.number : newR;

            String detail = "ขอย้ายจากห้อง " + oldNum + " ไปห้อง " + newNum + " (เหตุผล: " + reason + ")";

            list.add(new MoveModel(tId, uId, name, phone, oldR, oldNum, newR, newNum, reason, dateThai, stThai, detail));
        }
        return list;
    }

    public Object[][] getTableRows() {
        List<MoveModel> list = getAllMoves();
        Object[][] rows = new Object[list.size()][5];
        for (int i = 0; i < list.size(); i++) {
            MoveModel m = list.get(i);
            rows[i] = new Object[]{m.id, m.name, "จากห้อง " + m.oldRoomNum + " ย้ายไป " + m.newRoomNum, m.date, m.status};
        }
        return rows;
    }

    /**
     * อนุมัติคำขอย้ายห้อง:
     * 1. เปลี่ยนสถานะคำขอย้ายเป็น APPROVED
     * 2. ยกเลิกสัญญาเช่าเดิม (TERMINATED) ในห้องเดิม
     * 3. สร้างสัญญาเช่าใหม่ (ACTIVE) ผูกกับห้องใหม่ (คำนวณประกัน = ค่าห้องใหม่ x 2)
     * 4. อัปเดตสถานะห้องเดิมเป็น AVAILABLE และห้องใหม่เป็น OCCUPIED
     * 5. อัปเดต roomId ของผู้เช่าใน users.csv
     */
    public boolean approveMove(String transferId) {
        for (Map<String, String> m : DataStore.readTable("room_transfers.csv")) {
            if (m.getOrDefault("transferId", "").equalsIgnoreCase(transferId)) {
                String userId = m.getOrDefault("userId", "").trim();
                String oldRoomId = m.getOrDefault("oldRoomId", "").trim();
                String newRoomId = m.getOrDefault("newRoomId", "").trim();

                // 1. อัปเดตสถานะใน room_transfers.csv
                DataStore.updateCell("room_transfers.csv", "transferId", transferId, "status", "APPROVED");

                // 2. ยกเลิกสัญญาเดิมของผู้เช่า/ห้องเดิม (TERMINATED)
                for (DataStore.Contract c : DataStore.contracts()) {
                    if ((c.userId.equalsIgnoreCase(userId) || c.roomId.equalsIgnoreCase(oldRoomId)) && c.isActive()) {
                        DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", c.id, "status", "TERMINATED");
                    }
                }

                // 3. คำนวณราคาและสร้างสัญญาใหม่ (ACTIVE)
                DataStore.Room newRoom = DataStore.room(newRoomId);
                int newRent = newRoom != null ? newRoom.rent : 2800;
                int newDeposit = newRent * 2; // เงินประกัน = ค่าห้องใหม่ x 2

                DataStore.saveContract(userId, newRoomId, newRent, "ACTIVE");

                // บันทึกเงินประกันลงในสัญญาฉบับใหม่ล่าสุด
                for (DataStore.Contract c : DataStore.contracts()) {
                    if (c.userId.equalsIgnoreCase(userId) && c.roomId.equalsIgnoreCase(newRoomId) && c.isActive()) {
                        DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", c.id, "deposit", String.valueOf(newDeposit));
                    }
                }

                // 4. สลับสถานะห้องพัก
                DataStore.setRoomStatus(oldRoomId, "AVAILABLE");
                DataStore.setRoomStatus(newRoomId, "OCCUPIED");

                // 5. อัปเดต roomId ใน users.csv
                DataStore.updateCell("users.csv", "userId", userId, "roomId", newRoomId);
                try {
                    LoginLogic.assignRoom(userId, newRoomId);
                } catch (Exception ignored) {}

                return true;
            }
        }
        return false;
    }

    /** ปฏิเสธคำขอย้ายห้อง */
    public boolean rejectMove(String transferId) {
        return DataStore.updateCell("room_transfers.csv", "transferId", transferId, "status", "REJECTED");
    }
}