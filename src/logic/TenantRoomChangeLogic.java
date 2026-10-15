package logic;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TenantRoomChangeLogic
 * คือคลาสสำหรับจัดการตรรกะการยื่นคำขอย้ายห้องพักของผู้เช่า
 * เชื่อมโยงข้อมูลบัญชีผู้เช่า (User) ห้องพักปัจจุบัน (Room) สัญญาเช่า
 * (Contract) และรายการห้องว่าง
 * TRANSFER_FILE คือ ชื่อไฟล์ CSV ที่เก็บข้อมูลคำขอย้ายห้อง
 * TRANSFER_HEADER คือ ส่วนหัวตารางของไฟล์ข้อมูลคำขอย้ายห้อง
 * user คือ ข้อมูลผู้ใช้งานปัจจุบัน
 * transferList คือ รายการคำขอย้ายห้องของผู้ใช้งาน
 */
public class TenantRoomChangeLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF(user, transferList) คือ ตัวแทนตรรกะสำหรับตรวจสอบห้องพักปัจจุบัน ห้องว่าง
    // และบันทึกคำขอย้ายห้องของผู้เช่า
    // RI
    // 1. TRANSFER_FILE และ TRANSFER_HEADER ต้องไม่เป็น null หรือค่าว่าง
    // 2. transferList ต้องไม่เป็น null

    public static final String TRANSFER_FILE = "room_transfers.csv";
    public static final String TRANSFER_HEADER = "transferId,userId,contractId,oldRoomId,newRoomId,reason,requestDate,status";

    public enum Status {
        INVALID_INPUT, PENDING_EXISTS, SUCCESS
    }

    /**
     * Result คือคลาสผลลัพธ์การดำเนินการยื่นคำขอย้ายห้อง
     * status คือ สถานะผลการส่งคำขอ
     * transferId คือ รหัสคำขอย้ายห้องเมื่อทำรายการสำเร็จ
     */
    public static class Result {
        // ===== representation =====
        // Abstraction Function:
        // AF(status, transferId) คือ ตัวแทนผลลัพธ์ของการยื่นคำร้องขอย้ายห้องพัก
        // RI
        // 1. status ต้องไม่เป็น null
        // 2. ถ้า status เป็น SUCCESS ค่า transferId ต้องไม่เป็น null และไม่เป็นค่าว่าง

        public final Status status;
        public final String transferId; // มีค่าเมื่อ SUCCESS

        Result(Status status, String transferId) {
            this.status = status;
            this.transferId = transferId;
        }
    }

    /**
     * TransferItem คือคลาสข้อมูลประวัติคำขอย้ายห้องพักของผู้เช่า
     * id คือ รหัสคำขอย้ายห้อง
     * date คือ วันที่ยื่นคำขอ
     * oldRoomNum คือ หมายเลขห้องพักเดิม
     * newRoomNum คือ หมายเลขห้องพักใหม่ที่ขอย้ายไป
     * reason คือ เหตุผลการขอย้ายห้อง
     * status คือ สถานะคำขอ (PENDING, APPROVED, REJECTED)
     * statusThai คือ ข้อความสถานะภาษาไทย
     */
    public static class TransferItem {
        public final String id;
        public final String date;
        public final String oldRoomNum;
        public final String newRoomNum;
        public final String reason;
        public final String status;
        public final String statusThai;

        public TransferItem(String id, String date, String oldRoomNum, String newRoomNum, String reason,
                String status) {
            this.id = id;
            this.date = date;
            this.oldRoomNum = oldRoomNum;
            this.newRoomNum = newRoomNum;
            this.reason = reason;
            this.status = status;
            this.statusThai = "APPROVED".equalsIgnoreCase(status) ? "อนุมัติแล้ว"
                    : ("REJECTED".equalsIgnoreCase(status) ? "ปฏิเสธ" : "รอการอนุมัติ");
        }
    }

    private final LoginLogic.User user;
    private final List<TransferItem> transferList = new ArrayList<>();

    /**
     * คอนสตรักเตอร์สำหรับสร้างตรรกะการยื่นขอย้ายห้องพักของผู้เช่า
     * 
     * @param user ข้อมูลผู้เช่าที่กำลังเข้าสู่ระบบ
     */
    public TenantRoomChangeLogic(LoginLogic.User user) {
        this.user = user;
        reload();
    }

    // ===================== ผู้ใช้ / ห้อง =====================

    /**
     * ดึงข้อมูลผู้ใช้งานปัจจุบัน (จากพารามิเตอร์ หรือ Session ปัจจุบัน)
     * 
     * @return ออบเจ็กต์ User หรือ null
     */
    public LoginLogic.User getUser() {
        if (this.user != null && !DataStore.blank(this.user.username)) {
            return this.user;
        }
        LoginLogic.User cur = LoginLogic.getCurrentUser();
        if (cur != null)
            return cur;
        String curName = DataStore.getCurrentLoginUser();
        if (!DataStore.blank(curName) && !"Guest".equalsIgnoreCase(curName)) {
            return LoginLogic.findUser(curName);
        }
        return this.user;
    }

    /**
     * ดึงคีย์ระบุตัวตนของผู้ใช้ (userId หรือ username)
     * 
     * @return รหัสหรือชื่อผู้ใช้
     */
    public String userKey() {
        LoginLogic.User u = getUser();
        if (u == null)
            return "";
        return DataStore.blank(u.userId) ? u.username : u.userId;
    }

    /**
     * ดึงรหัสห้องพัก (roomId) ของผู้เช่าปัจจุบัน
     * ตรวจสอบทั้งจากข้อมูลผู้ใช้และสัญญาเช่าปัจจุบัน
     * 
     * @return รหัสห้องพัก หรือ "" หากไม่มีห้อง
     */
    public String roomId() {
        LoginLogic.User u = getUser();
        if (u == null)
            return "";
        if (!DataStore.blank(u.roomId) && !"-".equals(u.roomId.trim())) {
            return u.roomId.trim();
        }
        DataStore.Contract c = contract();
        return (c != null && !DataStore.blank(c.roomId) && !"-".equals(c.roomId.trim())) ? c.roomId.trim() : "";
    }

    /**
     * ตรวจสอบว่าผู้เช่ามีข้อมูลห้องพักผูกอยู่หรือไม่
     * 
     * @return true หากผู้เช่ามีห้องพักผูกอยู่
     */
    public boolean hasRoom() {
        return currentRoom() != null;
    }

    /**
     * ดึงข้อมูลห้องพักปัจจุบันของผู้เช่า
     * 
     * @return ออบเจ็กต์ Room ห้องปัจจุบัน หรือ null หากไม่พบ
     */
    public DataStore.Room currentRoom() {
        String rId = roomId();
        if (DataStore.blank(rId))
            return null;
        DataStore.Room r = DataStore.room(rId);
        if (r == null) {
            r = DataStore.roomByNumber(rId);
        }
        return r;
    }

    /**
     * ดึงข้อมูลสัญญาเช่าปัจจุบันของผู้เช่า
     * 
     * @return ออบเจ็กต์ Contract หรือ null
     */
    public DataStore.Contract contract() {
        LoginLogic.User u = getUser();
        if (u == null)
            return null;
        DataStore.Contract c = DataStore.contractFor(u);
        if (c == null) {
            String uKey = userKey();
            for (DataStore.Contract item : DataStore.contracts()) {
                if (item.userId != null && item.userId.equalsIgnoreCase(uKey) && item.isActive()) {
                    return item;
                }
            }
        }
        return c;
    }

    /**
     * ดึงรายการห้องพักที่ว่างและสามารถขอย้ายไปได้ (ยกเว้นห้องปัจจุบันของผู้เช่า)
     * 
     * @return รายการห้องพักที่มีสถานะว่าง
     */
    public List<DataStore.Room> availableRooms() {
        DataStore.Room cur = currentRoom();
        return (cur == null) ? DataStore.availableRooms(null) : DataStore.availableRooms(cur.id);
    }

    // ===================== อ่านจากไฟล์ =====================

    /**
     * โหลดข้อมูลคำขอย้ายห้องของผู้ใช้และห้องพักปัจจุบันจากไฟล์ room_transfers.csv
     * ใหม่
     */
    public void reload() {
        transferList.clear();
        String currentUserId = userKey();

        for (Map<String, String> row : DataStore.readTable(TRANSFER_FILE)) {
            String tId = row.getOrDefault("transferId", "").trim();
            String uId = row.getOrDefault("userId", "").trim();
            String oldR = row.getOrDefault("oldRoomId", "").trim();
            String newR = row.getOrDefault("newRoomId", "").trim();
            String reason = row.getOrDefault("reason", "").trim();
            String reqDate = row.getOrDefault("requestDate", "").trim();
            String status = row.getOrDefault("status", "PENDING").trim();

            // กรองเงื่อนไข: ตรวจสอบว่าเป็นของผู้ใช้คนนี้เท่านั้น
            // ไม่แสดงประวัติคำขอของผู้เช่าคนก่อนในห้องเดิม
            boolean isUserMatch = !DataStore.blank(currentUserId) && currentUserId.equalsIgnoreCase(uId);

            if (isUserMatch) {
                DataStore.Room rOld = DataStore.room(oldR);
                if (rOld == null)
                    rOld = DataStore.roomByNumber(oldR);
                DataStore.Room rNew = DataStore.room(newR);
                if (rNew == null)
                    rNew = DataStore.roomByNumber(newR);

                String oldNum = rOld != null ? rOld.number : oldR;
                String newNum = rNew != null ? rNew.number : newR;
                String dateDisplay = displayDate(reqDate);

                transferList.add(new TransferItem(tId, dateDisplay, oldNum, newNum, reason, status));
            }
        }
    }

    /**
     * แปลงสตริงวันที่เป็นรูปแบบ dd/MM/yyyy สำหรับแสดงผล
     * 
     * @param isoOrFormatted ข้อความวันที่ต้นทาง
     * @return ข้อความวันที่ในรูปแบบ dd/MM/yyyy
     */
    private static String displayDate(String isoOrFormatted) {
        if (DataStore.blank(isoOrFormatted))
            return "-";
        try {
            return LocalDate.parse(isoOrFormatted.trim()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoOrFormatted.trim();
        }
    }

    /**
     * ดึงรายการประวัติคำขอย้ายห้องพักทั้งหมดของผู้เช่า
     * 
     * @return รายการ TransferItem
     */
    public List<TransferItem> getTransferList() {
        return transferList;
    }

    /**
     * ตรวจสอบว่าผู้เช่ามีคำขอย้ายห้องที่รอการพิจารณาอยู่หรือไม่
     * 
     * @return true หากมีคำขอย้ายห้องสถานะ PENDING อยู่แล้ว
     */
    public boolean hasPendingTransfer() {
        return getPendingTransfer() != null;
    }

    /**
     * ดึงคำขอย้ายห้องที่กำลังรอการพิจารณา (สถานะ PENDING)
     * 
     * @return ออบเจ็กต์ TransferItem ที่รอดำเนินการ หรือ null
     */
    public TransferItem getPendingTransfer() {
        reload();
        for (TransferItem item : transferList) {
            if ("PENDING".equalsIgnoreCase(item.status)) {
                return item;
            }
        }
        return null;
    }

    // ===================== เขียนลงไฟล์ =====================

    /**
     * บันทึกคำขอย้ายห้องพักใหม่ลงในไฟล์ room_transfers.csv
     * 
     * @param target ห้องพักเป้าหมายที่ต้องการย้ายไป
     * @param reason เหตุผลความจำเป็นในการขอย้ายห้อง
     * @return ออบเจ็กต์ Result ระบุผลลัพธ์การส่งคำขอ
     * @throws IOException หากเกิดข้อผิดพลาดในการเขียนไฟล์
     */
    public Result submit(DataStore.Room target, String reason) throws IOException {
        if (target == null || reason == null || reason.trim().isEmpty()) {
            return new Result(Status.INVALID_INPUT, null);
        }
        if (hasPendingTransfer()) {
            return new Result(Status.PENDING_EXISTS, null);
        }

        DataStore.Room cur = currentRoom();
        if (cur == null) {
            return new Result(Status.INVALID_INPUT, null);
        }
        DataStore.Contract c = contract();
        String uKey = userKey();

        String transferId = DataStore.nextId(TRANSFER_FILE, "transferId", "T", 3);
        DataStore.appendRow(TRANSFER_FILE, TRANSFER_HEADER,
                transferId,
                DataStore.blank(uKey) ? "-" : uKey,
                c == null ? "-" : c.id,
                cur.id,
                target.id,
                reason.replace(",", " ").trim(),
                LocalDate.now().toString(),
                "PENDING");

        reload();
        return new Result(Status.SUCCESS, transferId);
    }
}
