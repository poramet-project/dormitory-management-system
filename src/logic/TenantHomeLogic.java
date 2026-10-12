package logic;

/** Logic ของหน้าแรกผู้เช่า (ไม่มี Swing) */
public class TenantHomeLogic {
    private final LogicLogin.User user;
    private final DataStore.Room room;
    private final DataStore.Contract contract;

    private int repairTotal;
    private int repairInProgress;
    private int repairDone;

    public TenantHomeLogic(LogicLogin.User user) {
        this.user = user;
        this.room = (user == null) ? null : DataStore.room(user.roomId);
        this.contract = (user == null) ? null : DataStore.contractFor(user);
        reloadRepairStats();
    }

    /** อ่าน complaints.csv ใหม่ แล้วนับจำนวนเรื่องแจ้งซ่อมของผู้ใช้ (เรียกซ้ำได้เมื่ออยากอัปเดต) */
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

    public String repairTotalText()      { return repairTotal + " รายการ"; }
    public String repairInProgressText() { return "กำลังดำเนินการ " + repairInProgress; }
    public String repairDoneText()       { return "เสร็จสิ้น " + repairDone; }

    public String roomDisplay() {
        return (room != null) ? "ห้อง " + room.number : "ยังไม่มีห้องพัก";
    }

    public String pillText() {
        return (room != null) ? room.typeShort() : "-";
    }

    public String locationText() {
        return (room != null)
                ? "อาคารหอพักนักศึกษา | ชั้น " + room.floor() + " | " + room.statusThai()
                : "ติดต่อผู้ดูแลหอพักเพื่อจองห้อง";
    }

    public String contractEndText() {
        return (contract != null)
                ? "สิ้นสุดสัญญา: " + DataStore.thaiDate(contract.end)
                : "ยังไม่มีข้อมูลสัญญาเช่า";
    }
}