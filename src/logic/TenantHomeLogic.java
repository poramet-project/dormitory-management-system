package logic;

import java.util.Map;

/**
 * TenantHomeLogic คือคลาสสำหรับจัดการตรรกะทางธุรกิจของหน้าหลักผู้เช่า (Dashboard ผู้เช่า)
 * user คือ ข้อมูลบัญชีผู้เช่าที่กำลังใช้งานระบบ
 * room คือ ข้อมูลห้องพักของผู้เช่า
 * contract คือ ข้อมูลสัญญาเช่าของผู้เช่า
 * repairTotal คือ จำนวนรายการแจ้งซ่อมทั้งหมดของผู้เช่าในห้องปัจจุบัน
 * repairInProgress คือ จำนวนรายการแจ้งซ่อมที่อยู่ระหว่างดำเนินการ
 * repairDone คือ จำนวนรายการแจ้งซ่อมที่เสร็จสิ้นแล้ว
 */
public class TenantHomeLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF(user, room, contract, repairTotal, repairInProgress, repairDone) คือ
    // ตรรกะสรุปภาพรวมสถานะห้องพัก สัญญาเช่า และสถานะการแจ้งซ่อมในหน้าหลักของผู้เช่า (เฉพาะห้องปัจจุบัน ไม่รวมห้องเก่า)
    // RI
    // 1. repairTotal, repairInProgress, repairDone ต้อง >= 0
    // 2. repairInProgress + repairDone ต้องเท่ากับ repairTotal

    private final LoginLogic.User user;
    private DataStore.Room room;
    private DataStore.Contract contract;

    private int repairTotal;
    private int repairInProgress;
    private int repairDone;

    /**
     * คอนสตรักเตอร์สำหรับสร้างตรรกะหน้าหลักของผู้เช่า
     * 
     * @param user ข้อมูลผู้เช่าที่กำลังเข้าสู่ระบบ
     */
    public TenantHomeLogic(LoginLogic.User user) {
        this.user = user;
        reload();
    }

    /**
     * ดึงข้อมูลผู้ใช้งานล่าสุดจากฐานข้อมูลหรือ Session
     * 
     * @return ข้อมูล User ล่าสุด
     */
    public LoginLogic.User getLatestUser() {
        if (this.user == null) return null;
        String curName = this.user.username;
        if (!DataStore.blank(curName)) {
            LoginLogic.User found = LoginLogic.findUser(curName);
            if (found != null) return found;
        }
        LoginLogic.User cur = LoginLogic.getCurrentUser();
        if (cur != null) return cur;
        return this.user;
    }

    /**
     * ค้นหาห้องพักปัจจุบันของผู้เช่า ตรวจสอบทั้งจากข้อมูลผู้ใช้และสัญญาเช่าปัจจุบัน
     * 
     * @param u ข้อมูลผู้เช่า
     * @return ออบเจ็กต์ Room ห้องปัจจุบัน หรือ null หากไม่พบ
     */
    public static DataStore.Room resolveRoom(LoginLogic.User u) {
        if (u == null) return null;
        String rId = u.roomId;
        if (DataStore.blank(rId) || "-".equals(rId.trim())) {
            for (DataStore.Contract c : DataStore.contracts()) {
                String uId = DataStore.blank(u.userId) ? u.username : u.userId;
                if (c.userId.equalsIgnoreCase(uId) && c.isActive() && !DataStore.blank(c.roomId)) {
                    rId = c.roomId;
                    break;
                }
            }
        }
        if (DataStore.blank(rId) || "-".equals(rId.trim())) return null;
        DataStore.Room r = DataStore.room(rId);
        if (r == null) {
            r = DataStore.roomByNumber(rId);
        }
        return r;
    }

    /**
     * ค้นหาสัญญาเช่าที่ใช้งานอยู่ (ACTIVE) ของห้องปัจจุบันของผู้เช่า
     * 
     * @param u ข้อมูลผู้เช่า
     * @param curRoom ห้องปัจจุบันของผู้เช่า
     * @return สัญญาเช่าที่ผูกกับห้องปัจจุบัน หรือสัญญาที่ยังใช้งานอยู่
     */
    public static DataStore.Contract resolveContract(LoginLogic.User u, DataStore.Room curRoom) {
        if (u == null) return null;
        String uId = DataStore.blank(u.userId) ? u.username : u.userId;
        if (curRoom != null) {
            for (DataStore.Contract c : DataStore.contracts()) {
                if (c.userId.equalsIgnoreCase(uId) && c.isActive()
                        && (c.roomId.equalsIgnoreCase(curRoom.id) || c.roomId.equalsIgnoreCase(curRoom.number))) {
                    return c;
                }
            }
        }
        for (DataStore.Contract c : DataStore.contracts()) {
            if (c.userId.equalsIgnoreCase(uId) && c.isActive()) {
                return c;
            }
        }
        return DataStore.contractFor(u);
    }

    /**
     * โหลดข้อมูลห้อง สัญญา และสถิติแจ้งซ่อมล่าสุดของผู้เช่าใหม่ทั้งหมด
     */
    public void reload() {
        LoginLogic.User u = getLatestUser();
        this.room = resolveRoom(u);
        this.contract = resolveContract(u, this.room);
        reloadRepairStats();
    }

    /**
     * โหลดและนับจำนวนสถิติการแจ้งซ่อมของผู้เช่าเฉพาะห้องปัจจุบันเท่านั้นจาก complaints.csv
     * หากผู้เช่าย้ายห้อง รายการแจ้งซ่อมของห้องเดิมจะไม่ถูกนำมานับหรือแสดงผล
     */
    public void reloadRepairStats() {
        repairTotal = 0;
        repairInProgress = 0;
        repairDone = 0;

        LoginLogic.User u = getLatestUser();
        if (u == null) return;
        String uId = DataStore.blank(u.userId) ? u.username : u.userId;
        if (DataStore.blank(uId)) return;

        DataStore.Room curRoom = (this.room != null) ? this.room : resolveRoom(u);
        if (curRoom == null) return; // หากยังไม่มีห้องพักปัจจุบัน จะไม่มีรายการแจ้งซ่อมของห้องเดิม

        String curRoomId = curRoom.id;
        String curRoomNum = curRoom.number;

        for (Map<String, String> row : DataStore.readTable(DataStore.COMPLAINTS_FILE)) {
            String rowUser = row.getOrDefault("userId", "").trim();
            String rowRoom = row.getOrDefault("roomId", "").trim();
            String status = row.getOrDefault("status", "").trim().toUpperCase();

            // กรองเงื่อนไข: เป็นของผู้ใช้คนนี้ และต้องเป็นห้องพักปัจจุบันเท่านั้น (ไม่รวมห้องเก่า)
            boolean userMatch = rowUser.equalsIgnoreCase(uId)
                    || (!DataStore.blank(u.username) && rowUser.equalsIgnoreCase(u.username));
            boolean roomMatch = rowRoom.equalsIgnoreCase(curRoomId) || rowRoom.equalsIgnoreCase(curRoomNum);

            if (userMatch && roomMatch) {
                repairTotal++;
                if ("RESOLVED".equalsIgnoreCase(status)) {
                    repairDone++;
                } else {
                    repairInProgress++;
                }
            }
        }
    }

    public DataStore.Room getRoom() { return room; }
    public DataStore.Contract getContract() { return contract; }

    /**
     * ดึงข้อความสรุปจำนวนรายการแจ้งซ่อมทั้งหมดของห้องปัจจุบัน
     * 
     * @return ข้อความจำนวนรายการแจ้งซ่อมทั้งหมด เช่น "3 รายการ"
     */
    public String repairTotalText()      { return repairTotal + " รายการ"; }

    /**
     * ดึงข้อความแสดงจำนวนรายการแจ้งซ่อมที่กำลังดำเนินการของห้องปัจจุบัน
     * 
     * @return ข้อความจำนวนรายการที่กำลังดำเนินการ
     */
    public String repairInProgressText() { return "กำลังดำเนินการ " + repairInProgress; }

    /**
     * ดึงข้อความแสดงจำนวนรายการแจ้งซ่อมที่เสร็จสิ้นแล้วของห้องปัจจุบัน
     * 
     * @return ข้อความจำนวนรายการที่เสร็จสิ้น
     */
    public String repairDoneText()       { return "เสร็จสิ้น " + repairDone; }

    /**
     * ดึงข้อความหมายเลขห้องพักปัจจุบันสำหรับแสดงผลหัวการ์ด
     * 
     * @return ข้อความหมายเลขห้องพัก เช่น "ห้อง 101" หรือ "ยังไม่มีห้องพัก"
     */
    public String roomDisplay() {
        return (room != null) ? "ห้อง " + room.number : "ยังไม่มีห้องพัก";
    }

    /**
     * ดึงข้อความป้ายประเภทห้องพักแบบย่อ
     * 
     * @return ประเภทห้องพัก เช่น "ห้องแอร์"
     */
    public String pillText() {
        return (room != null) ? room.typeShort() : "-";
    }

    /**
     * ดึงข้อความแสดงที่ตั้ง ชั้น และสถานะของห้องพักปัจจุบัน
     * 
     * @return ข้อความที่ตั้งห้องพัก
     */
    public String locationText() {
        return (room != null)
                ? "อาคารหอพักนักศึกษา | ชั้น " + room.floor() + " | " + room.statusThai()
                : "ติดต่อผู้ดูแลหอพักเพื่อจองห้อง";
    }

    /**
     * ดึงข้อความระบุวันสิ้นสุดสัญญาเช่าของห้องปัจจุบัน
     * 
     * @return ข้อความวันสิ้นสุดสัญญา เช่น "สิ้นสุดสัญญา: 16 ต.ค. 2570"
     */
    public String contractEndText() {
        return (contract != null)
                ? "สิ้นสุดสัญญา: " + DataStore.thaiDate(contract.end)
                : "ยังไม่มีข้อมูลสัญญาเช่า";
    }
}