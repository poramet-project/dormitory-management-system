package logic;

/**
 * TenantHomeLogic คือคลาสสำหรับจัดการตรรกะทางธุรกิจของหน้าหลักผู้เช่า (Dashboard ผู้เช่า)
 * user คือ ข้อมูลบัญชีผู้เช่าที่กำลังใช้งานระบบ
 * room คือ ข้อมูลห้องพักของผู้เช่า
 * contract คือ ข้อมูลสัญญาเช่าของผู้เช่า
 * repairTotal คือ จำนวนรายการแจ้งซ่อมทั้งหมดของผู้เช่า
 * repairInProgress คือ จำนวนรายการแจ้งซ่อมที่อยู่ระหว่างดำเนินการ
 * repairDone คือ จำนวนรายการแจ้งซ่อมที่เสร็จสิ้นแล้ว
 */
public class TenantHomeLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF(user, room, contract, repairTotal, repairInProgress, repairDone) คือ
    // ตรรกะสรุปภาพรวมสถานะห้องพัก สัญญาเช่า และสถานะการแจ้งซ่อมในหน้าหลักของผู้เช่า
    // RI
    // 1. repairTotal, repairInProgress, repairDone ต้อง >= 0
    // 2. repairInProgress + repairDone ต้องเท่ากับ repairTotal

    private final LoginLogic.User user;
    private final DataStore.Room room;
    private final DataStore.Contract contract;

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
        this.room = (user == null) ? null : DataStore.room(user.roomId);
        this.contract = (user == null) ? null : DataStore.contractFor(user);
        reloadRepairStats();
    }

    /**
     * โหลดและนับจำนวนสถิติการแจ้งซ่อมของผู้ใช้อีกครั้งจาก complaints.csv
     */
    public void reloadRepairStats() {
        repairTotal = 0;
        repairInProgress = 0;
        repairDone = 0;

        if (user == null || user.userId == null) return;

        java.util.List<DataStore.Complaint> list = DataStore.complaintsForUser(user.userId);
        if (list == null) return;

        for (DataStore.Complaint c : list) {
            if (c == null || c.status == null) continue;
            repairTotal++;
            if ("RESOLVED".equalsIgnoreCase(c.status)) {
                repairDone++;
            } else {
                repairInProgress++; // PENDING / IN_PROGRESS ถือว่ากำลังดำเนินการ
            }
        }
    }

    /**
     * ดึงข้อความสรุปจำนวนรายการแจ้งซ่อมทั้งหมด
     * 
     * @return ข้อความจำนวนรายการแจ้งซ่อมทั้งหมด เช่น "3 รายการ"
     */
    public String repairTotalText()      { return repairTotal + " รายการ"; }

    /**
     * ดึงข้อความแสดงจำนวนรายการแจ้งซ่อมที่กำลังดำเนินการ
     * 
     * @return ข้อความจำนวนรายการที่กำลังดำเนินการ
     */
    public String repairInProgressText() { return "กำลังดำเนินการ " + repairInProgress; }

    /**
     * ดึงข้อความแสดงจำนวนรายการแจ้งซ่อมที่เสร็จสิ้นแล้ว
     * 
     * @return ข้อความจำนวนรายการที่เสร็จสิ้น
     */
    public String repairDoneText()       { return "เสร็จสิ้น " + repairDone; }

    /**
     * ดึงข้อความหมายเลขห้องพักสำหรับแสดงผลหัวการ์ด
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
     * ดึงข้อความแสดงที่ตั้ง ชั้น และสถานะของห้องพัก
     * 
     * @return ข้อความที่ตั้งห้องพัก
     */
    public String locationText() {
        return (room != null)
                ? "อาคารหอพักนักศึกษา | ชั้น " + room.floor() + " | " + room.statusThai()
                : "ติดต่อผู้ดูแลหอพักเพื่อจองห้อง";
    }

    /**
     * ดึงข้อความระบุวันสิ้นสุดสัญญาเช่า
     * 
     * @return ข้อความวันสิ้นสุดสัญญา เช่น "สิ้นสุดสัญญา: 16 ต.ค. 2570"
     */
    public String contractEndText() {
        return (contract != null)
                ? "สิ้นสุดสัญญา: " + DataStore.thaiDate(contract.end)
                : "ยังไม่มีข้อมูลสัญญาเช่า";
    }
}