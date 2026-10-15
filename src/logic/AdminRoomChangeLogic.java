package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AdminRoomChangeLogic คือคลาสสำหรับจัดการตรรกะทางธุรกิจของคำขอย้ายห้องพักสำหรับผู้ดูแล
 * ดึงรายการคำขอย้ายห้อง อนุมัติการย้ายห้อง (ยกเลิกสัญญาเดิม ออกสัญญาใหม่ และอัปเดตห้องพัก) และปฏิเสธคำขอย้ายห้อง
 */
public class AdminRoomChangeLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF() คือ ตรรกะการประมวลผลคำขอย้ายห้องพักในระบบที่บันทึกใน room_transfers.csv
    // RI
    // 1. ไฟล์ room_transfers.csv ต้องมีอยู่ในระบบ
    // 2. การอนุมัติการย้ายห้อง ต้องทำให้ห้องเดิมเป็นห้องว่าง (AVAILABLE) และห้องใหม่เป็นห้องที่มีผู้เช่า (OCCUPIED)

    /**
     * MoveModel คือคลาสสำหรับเก็บข้อมูลคำขอย้ายห้องพักแต่ละรายการ
     * id คือ รหัสคำขอย้ายห้อง (เช่น T001)
     * userId คือ รหัสผู้เช่าที่ขอย้ายห้อง
     * name คือ ชื่อผู้เช่า
     * phone คือ หมายเลขโทรศัพท์
     * oldRoomId คือ รหัสห้องเดิม
     * oldRoomNum คือ หมายเลขห้องเดิม
     * newRoomId คือ รหัสห้องใหม่ที่ต้องการย้ายไป
     * newRoomNum คือ หมายเลขห้องใหม่
     * reason คือ เหตุผลในการขอย้ายห้อง
     * date คือ วันที่ยื่นคำขอ
     * status คือ สถานะคำขอ (รอพิจารณา, อนุมัติ, ปฏิเสธ)
     * detail คือ รายละเอียดเพิ่มเติม
     */
    public static class MoveModel {

        // ===== representation =====
        // Abstraction Function:
        // AF(id, userId, name, phone, oldRoomId, oldRoomNum, newRoomId, newRoomNum, reason, date, status, detail) คือ
        // ข้อมูลคำขอย้ายห้องพัก 1 รายการ
        // RI
        // 1. id, oldRoomId, newRoomId, status ต้องไม่เป็น null
        // 2. oldRoomId ต้องไม่เท่ากับ newRoomId

        public String id, userId, name, phone, oldRoomId, oldRoomNum, newRoomId, newRoomNum, reason, date, status, detail;

        /**
         * คอนสตรักเตอร์สำหรับสร้างอ็อบเจกต์ MoveModel
         *
         * @param id รหัสคำขอย้ายห้อง
         * @param userId รหัสผู้ใช้งานที่ยื่นคำขอ
         * @param name ชื่อ-นามสกุลของผู้ยื่นคำขอ
         * @param phone เบอร์โทรศัพท์ติดต่อ
         * @param oldRoomId รหัสห้องพักเดิม
         * @param oldRoomNum หมายเลขห้องพักเดิม
         * @param newRoomId รหัสห้องพักใหม่ที่ต้องการย้ายไป
         * @param newRoomNum หมายเลขห้องพักใหม่ที่ต้องการย้ายไป
         * @param reason เหตุผลในการขอย้ายห้อง
         * @param date วันที่ยื่นคำขอย้าย
         * @param status สถานะคำขอ (รอพิจารณา, อนุมัติ, ปฏิเสธ)
         * @param detail รายละเอียดคำขอย้ายห้องโดยย่อ
         */
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

    /**
     * ดึงรายการคำขอย้ายห้องพักทั้งหมดจาก room_transfers.csv
     * พร้อมเชื่อมโยงข้อมูลผู้เช่าและหมายเลขห้องพักเดิมและห้องพักใหม่
     *
     * @return รายการ List ของ MoveModel ทั้งหมดในระบบ
     */
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

    /**
     * แปลงรายการคำขอย้ายห้องให้อยู่ในรูปแบบ Object[][] สำหรับแสดงผลบน JTable
     * ประกอบด้วย 5 คอลัมน์: รหัสคำขอ, ผู้ยื่นคำขอ, รายละเอียดการย้าย, วันที่ยื่น, สถานะ
     *
     * @return อาร์เรย์ 2 มิติของข้อมูลสำหรับ DefaultTableModel
     */
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
     * ดำเนินการอนุมัติคำขอย้ายห้องพัก:
     * 1. เปลี่ยนสถานะคำขอย้ายเป็น APPROVED
     * 2. ยกเลิกสัญญาเช่าเดิม (TERMINATED) ในห้องเดิม
     * 3. สร้างสัญญาเช่าใหม่ (ACTIVE) ผูกกับห้องใหม่ (คำนวณเงินประกัน = ค่าห้องใหม่ x 2)
     * 4. สลับสถานะห้องพัก: ห้องเดิมเป็น AVAILABLE และห้องใหม่เป็น OCCUPIED
     * 5. อัปเดต roomId ของผู้เช่าใน users.csv เป็นห้องใหม่
     *
     * @param transferId รหัสคำขอย้ายห้อง
     * @return true หากดำเนินการสำเร็จ, false หากไม่พบคำขอย้าย
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

    /**
     * ปฏิเสธคำขอย้ายห้องพัก โดยปรับสถานะคำขอใน room_transfers.csv เป็น REJECTED
     *
     * @param transferId รหัสคำขอย้ายห้อง
     * @return true หากบันทึกสถานะปฏิเสธสำเร็จ, false หากเกิดข้อผิดพลาด
     */
    public boolean rejectMove(String transferId) {
        return DataStore.updateCell("room_transfers.csv", "transferId", transferId, "status", "REJECTED");
    }
}