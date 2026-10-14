package logic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * AdminTenantLogic
 */
public class AdminTenantLogic {

    public static class TenantModel {
        public String id, userId, name, roomId, room, startDate, endDate, status, phone, roomType, rent, deposit;

        public TenantModel(String id, String userId, String name, String roomId, String room, String startDate, String endDate,
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

    /** 1. ดึงรายการสัญญาเช่าทั้งหมด */
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

    public Object[][] getTableRows() {
        List<TenantModel> list = getAllTenants();
        Object[][] rows = new Object[list.size()][6];
        for (int i = 0; i < list.size(); i++) {
            TenantModel t = list.get(i);
            rows[i] = new Object[]{t.name, t.room, t.startDate, t.endDate, t.status, ""};
        }
        return rows;
    }

    /** 2. เพิ่มสัญญาใหม่ */
    public boolean addContract(String userId, String roomId, int rent, int deposit) {
        boolean ok = DataStore.saveContract(userId, roomId, rent);
        if (ok) {
            DataStore.setRoomStatus(roomId, "OCCUPIED");
            LoginLogic.assignRoom(userId, roomId);
        }
        return ok;
    }

    /** 3. แก้ไขสัญญา (ซิงค์ห้องและผู้ใช้หากมีการเปลี่ยนแปลง) */
    public boolean updateContract(String contractId, String oldUserId, String newUserId, String oldRoomId, String newRoomId, int rent, int deposit) {
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

    /** 4. ต่ออายุสัญญา (+1 ปี จากวันสิ้นสุดสัญญาเดิม) */
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

    /** 5. ยกเลิกสัญญา (คืนห้องพักเป็นห้องว่าง) */
    public boolean terminateContract(String contractId, String roomId, String userId) {
        boolean ok = DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", contractId, "status", "TERMINATED");
        if (ok) {
            DataStore.setRoomStatus(roomId, "AVAILABLE");
            DataStore.updateCell("users.csv", "userId", userId, "roomId", "-");
            DataStore.updateCell("users.csv", "userId", userId, "role", "GUEST");
        }
        return ok;
    }
}