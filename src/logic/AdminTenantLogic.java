package logic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AdminTenantLogic
 * คือคลาสสำหรับจัดการตรรกะทางธุรกิจของระบบทะเบียนผู้เช่าและสัญญาเช่าสำหรับผู้ดูแล
 * ให้บริการดึงรายการสัญญาเช่า ทำสัญญาใหม่ แก้ไขสัญญา ต่ออายุสัญญา
 * และยกเลิกสัญญาเช่า
 */
public class AdminTenantLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF() คือ ตัวแทนตรรกะการจัดการสัญญาเช่าและผู้เช่าใน contracts.csv และ
    // users.csv
    // RI
    // 1. contracts.csv และ users.csv ต้องพร้อมใช้งานและสามารถบันทึกข้อมูลได้
    // 2. การทำสัญญาใหม่หรือต่ออายุสัญญา ต้องมีเงินประกันและค่าเช่ามากกว่า 0

    /**
     * TenantModel คือคลาสสำหรับเก็บข้อมูลผู้เช่าและสัญญาเช่าเพื่อแสดงผลบนตาราง
     * id คือ รหัสสัญญาเช่า (เช่น CNT001)
     * userId คือ รหัสผู้เช่า
     * name คือ ชื่อ-นามสกุลของผู้เช่า
     * roomId คือ รหัสห้องพัก
     * room คือ หมายเลขห้องพัก
     * startDate คือ วันที่เริ่มต้นสัญญา
     * endDate คือ วันที่สิ้นสุดสัญญา
     * status คือ สถานะสัญญาเช่า
     * phone คือ หมายเลขโทรศัพท์
     * roomType คือ ประเภทห้องพัก
     * rent คือ ค่าเช่ารายเดือน
     * deposit คือ เงินประกันสัญญา
     */
    public static class TenantModel {

        // ===== representation =====
        // Abstraction Function:
        // AF(id, userId, name, roomId, room, startDate, endDate, status, phone,
        // roomType, rent, deposit) คือ
        // ข้อมูลจำลองสัญญาเช่าของผู้เช่า 1 รายการ
        // RI
        // 1. id, userId, name, roomId, status ต้องไม่เป็น null
        // 2. startDate และ endDate ต้องไม่เป็น null

        public String id, userId, name, roomId, room, startDate, endDate, status, phone, roomType, rent, deposit;

        /**
         * คอนสตรักเตอร์สำหรับสร้างอ็อบเจกต์ TenantModel
         *
         * @param id        เลขที่สัญญาเช่า
         * @param userId    รหัสผู้ใช้งานผู้เช่า
         * @param name      ชื่อ-นามสกุลของผู้เช่า
         * @param roomId    รหัสห้องพัก
         * @param room      หมายเลขห้องพัก
         * @param startDate วันที่เริ่มสัญญา
         * @param endDate   วันที่สิ้นสุดสัญญา
         * @param status    สถานะสัญญา (ใช้งานอยู่, รออนุมัติ, หมดอายุ, ยกเลิกสัญญา)
         * @param phone     เบอร์โทรศัพท์ติดต่อ
         * @param roomType  ข้อความระบุห้องพักและประเภทห้อง
         * @param rent      ข้อความแสดงอัตราค่าเช่า
         * @param deposit   ข้อความแสดงเงินมัดจำประกัน
         */
        public TenantModel(String id, String userId, String name, String roomId, String room, String startDate,
                String endDate,
                String status, String phone, String roomType, String rent, String deposit) {
            this.id = id;
            this.userId = userId;
            this.name = name;
            this.roomId = roomId;
            this.room = room;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.phone = phone;
            this.roomType = roomType;
            this.rent = rent;
            this.deposit = deposit;
        }
    }

    /**
     * ดึงรายการสัญญาเช่าทั้งหมดจากฐานข้อมูล DataStore
     * พร้อมเชื่อมโยงข้อมูลผู้เช่าและรายละเอียดห้องพักเพื่อเตรียมนำไปแสดงผล
     *
     * @return รายการ List ของ TenantModel ทั้งหมดในระบบ
     */
    public List<TenantModel> getAllTenants() {
        List<TenantModel> list = new ArrayList<>();
        for (DataStore.Contract c : DataStore.contracts()) {
            LoginLogic.User u = LoginLogic.findUser(c.userId);
            DataStore.Room r = DataStore.room(c.roomId);

            String name = (u != null) ? u.fullName : c.userId;
            String phone = (u != null) ? u.phone : "-";
            String roomNum = (r != null) ? r.number : c.roomId;
            String typeStr = (r != null) ? "ห้อง " + r.number + " • " + r.typeShort() : "ห้อง " + c.roomId;

            list.add(new TenantModel(c.id, c.userId, name, c.roomId, roomNum,
                    DataStore.thaiDate(c.start), DataStore.thaiDate(c.end), c.statusThai(),
                    phone, typeStr, DataStore.money(c.rent) + " บาท", DataStore.money(c.deposit) + " บาท"));
        }
        return list;
    }

    /**
     * แปลงรายการสัญญาเช่าให้อยู่ในรูปแบบ Object[][] สำหรับแสดงผลบน JTable
     * ประกอบด้วย 5 คอลัมน์: ชื่อผู้เช่า, ห้องพัก, วันเริ่มสัญญา, วันสิ้นสุดสัญญา,
     * สถานะ
     *
     * @return อาร์เรย์ 2 มิติของข้อมูลตารางผู้เช่า
     */
    public Object[][] getTableRows() {
        List<TenantModel> list = getAllTenants();
        Object[][] rows = new Object[list.size()][5];
        for (int i = 0; i < list.size(); i++) {
            TenantModel t = list.get(i);
            rows[i] = new Object[] { t.name, t.room, t.startDate, t.endDate, t.status };
        }
        return rows;
    }

    /**
     * ตรวจสอบว่าห้องพักมีผู้เช่าหรือมีสัญญาเช่าที่เปิดใช้งานอยู่แล้วหรือไม่
     *
     * @param roomId รหัสห้องพัก เช่น "R101" หรือ "101"
     * @return true หากห้องพักมีผู้อาศัยอยู่แล้ว
     */
    public static boolean isRoomOccupied(String roomId) {
        if (DataStore.blank(roomId))
            return false;
        DataStore.Room r = DataStore.room(roomId);
        if (r == null)
            r = DataStore.roomByNumber(roomId);
        String rId = (r != null) ? r.id : roomId.trim();
        String rNum = (r != null) ? r.number : roomId.trim();

        // 1. ตรวจสอบสถานะห้องพักใน rooms.csv
        if (r != null && !"AVAILABLE".equalsIgnoreCase(r.status)) {
            return true;
        }

        // 2. ตรวจสอบว่ามีสัญญาเช่าสถานะ ACTIVE สำหรับห้องนี้หรือไม่
        for (DataStore.Contract c : DataStore.contracts()) {
            if (c.isActive()) {
                if (c.roomId.equalsIgnoreCase(rId) || c.roomId.equalsIgnoreCase(rNum)) {
                    return true;
                }
            }
        }

        // 3. ตรวจสอบว่ามีผู้ใช้ใน users.csv ที่เป็น TENANT และผูกกับห้องนี้หรือไม่
        for (Map<String, String> u : DataStore.readTable("users.csv")) {
            String role = u.getOrDefault("role", "").trim();
            String uRoom = u.getOrDefault("roomId", "").trim();
            if ("TENANT".equalsIgnoreCase(role)) {
                if (uRoom.equalsIgnoreCase(rId) || uRoom.equalsIgnoreCase(rNum)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * สร้างและบันทึกสัญญาเช่าใหม่เข้าสู่ระบบ
     * พร้อมปรับสถานะห้องพักเป็น OCCUPIED และตั้งค่าบทบาทผู้ใช้งานเป็น TENANT
     *
     * @param userId  รหัสผู้ใช้งาน
     * @param roomId  รหัสห้องพัก
     * @param rent    อัตราค่าเช่าต่อเดือน
     * @param deposit เงินประกันสัญญา
     * @return true หากบันทึกสัญญาสำเร็จ, false
     *         หากเกิดข้อผิดพลาดหรือห้องมีผู้อาศัยอยู่แล้ว
     */
    public boolean addContract(String userId, String roomId, int rent, int deposit) {
        if (isRoomOccupied(roomId)) {
            return false;
        }

        // หากผู้ใช้นี้มีสัญญา ACTIVE เดิมอยู่
        // ให้ยกเลิกสัญญาเดิมก่อนและปล่อยห้องเดิมเป็น AVAILABLE
        for (DataStore.Contract oldC : DataStore.contracts()) {
            if (oldC.userId.equalsIgnoreCase(userId) && oldC.isActive()) {
                DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", oldC.id, "status", "TERMINATED");
                if (!oldC.roomId.equalsIgnoreCase(roomId)) {
                    DataStore.setRoomStatus(oldC.roomId, "AVAILABLE");
                }
            }
        }

        boolean ok = DataStore.saveContract(userId, roomId, rent, deposit, "ACTIVE");
        if (ok) {
            DataStore.setRoomStatus(roomId, "OCCUPIED");
            LoginLogic.assignRoom(userId, roomId);
        }
        return ok;
    }

    /**
     * แก้ไขข้อมูลสัญญาเช่าที่มีอยู่เดิม
     * พร้อมซิงค์ข้อมูลผู้ใช้งานและห้องพักที่เกี่ยวข้อง
     *
     * @param contractId เลขที่สัญญาเช่า
     * @param oldUserId  รหัสผู้ใช้เดิม
     * @param newUserId  รหัสผู้ใช้ใหม่
     * @param oldRoomId  รหัสห้องเดิม
     * @param newRoomId  รหัสห้องใหม่
     * @param rent       อัตราค่าเช่าใหม่
     * @param deposit    เงินมัดจำประกันใหม่
     * @return true หากแก้ไขสำเร็จ, false หากห้องใหม่มีผู้อาศัยอยู่แล้ว
     */
    public boolean updateContract(String contractId, String oldUserId, String newUserId, String oldRoomId,
            String newRoomId, int rent, int deposit) {
        if (!oldRoomId.equalsIgnoreCase(newRoomId) && isRoomOccupied(newRoomId)) {
            return false;
        }
        DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", contractId, "userId", newUserId);
        DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", contractId, "roomId", newRoomId);
        DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", contractId, "monthlyRent", String.valueOf(rent));
        DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", contractId, "deposit", String.valueOf(deposit));

        // ซิงค์ฝั่งผู้ใช้ (users.csv)
        if (!oldUserId.equalsIgnoreCase(newUserId)) {
            DataStore.updateCell("users.csv", "userId", oldUserId, "roomId", "-");
            DataStore.updateCell("users.csv", "userId", oldUserId, "role", "GUEST");
        }
        LoginLogic.assignRoom(newUserId, newRoomId);

        // ซิงค์ฝั่งห้องพัก (rooms.csv)
        if (!oldRoomId.equalsIgnoreCase(newRoomId)) {
            DataStore.setRoomStatus(oldRoomId, "AVAILABLE");
        }
        DataStore.setRoomStatus(newRoomId, "OCCUPIED");

        return true;
    }

    /**
     * ต่ออายุสัญญาเช่าเพิ่มอีก 1 ปี จากวันสิ้นสุดสัญญาเดิม และปรับสถานะเป็น ACTIVE
     *
     * @param contractId เลขที่สัญญาเช่าที่ต้องการต่ออายุ
     * @return true หากต่ออายุสำเร็จ, false หากไม่พบสัญญาหรือเกิดข้อผิดพลาด
     */
    public boolean renewContract(String contractId) {
        for (DataStore.Contract c : DataStore.contracts()) {
            if (c.id.equalsIgnoreCase(contractId.trim())) {
                try {
                    LocalDate currentEnd;
                    try {
                        currentEnd = LocalDate.parse(c.end.trim());
                    } catch (Exception e) {
                        currentEnd = LocalDate.now();
                    }
                    String newEndDate = currentEnd.plusYears(1).toString();

                    DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", contractId, "endDate", newEndDate);
                    DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", contractId, "status", "ACTIVE");

                    if (!DataStore.blank(c.userId) && !DataStore.blank(c.roomId)) {
                        LoginLogic.assignRoom(c.userId, c.roomId);
                        DataStore.setRoomStatus(c.roomId, "OCCUPIED");
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * ยกเลิกสัญญาเช่า (TERMINATED) พร้อมคืนสถานะห้องพักเป็นห้องว่าง (AVAILABLE)
     * และปรับบทบาทผู้ใช้กลับเป็น GUEST
     *
     * @param contractId เลขที่สัญญาเช่า
     * @param roomId     รหัสห้องพัก
     * @param userId     รหัสผู้ใช้งาน
     * @return true หากยกเลิกสัญญาสำเร็จ
     */
    public boolean terminateContract(String contractId, String roomId, String userId) {
        boolean ok = DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", contractId, "status", "TERMINATED");
        if (ok) {
            DataStore.setRoomStatus(roomId, "AVAILABLE");

            // ตรวจสอบว่าผู้เช่ายังมีสัญญา ACTIVE อื่นอยู่อีกหรือไม่
            DataStore.Contract otherActive = null;
            for (DataStore.Contract c : DataStore.contracts()) {
                if (c.isActive() && c.userId.equalsIgnoreCase(userId) && !c.id.equalsIgnoreCase(contractId)) {
                    otherActive = c;
                    break;
                }
            }

            if (otherActive != null) {
                // หากยังมีสัญญาอื่นที่ active อยู่ ให้คงสถานะ TENANT และผูกกับห้องนั้น
                DataStore.updateCell("users.csv", "userId", userId, "roomId", otherActive.roomId);
                DataStore.updateCell("users.csv", "userId", userId, "role", "TENANT");
                LoginLogic.assignRoom(userId, otherActive.roomId);
            } else {
                DataStore.updateCell("users.csv", "userId", userId, "roomId", "-");
                DataStore.updateCell("users.csv", "userId", userId, "role", "GUEST");
            }
        }
        return ok;
    }
}