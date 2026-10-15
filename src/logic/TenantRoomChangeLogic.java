package logic;


import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TenantRoomChangeLogic คือคลาสสำหรับจัดการตรรกะการยื่นคำขอย้ายห้องพักของผู้เช่า
 * TRANSFER_FILE คือ ชื่อไฟล์ CSV ที่เก็บข้อมูลคำขอย้ายห้อง
 * TRANSFER_HEADER คือ ส่วนหัวตารางของไฟล์ข้อมูลคำขอย้ายห้อง
 * user คือ ข้อมูลผู้ใช้งานปัจจุบัน
 * current คือ ข้อมูลห้องพักปัจจุบันของผู้ใช้งาน
 * contract คือ ข้อมูลสัญญาเช่าปัจจุบัน
 * available คือ รายการห้องพักที่ว่างและสามารถขอย้ายไปได้
 */
public class TenantRoomChangeLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF(user, current, contract, available) คือ ตัวแทนตรรกะสำหรับตรวจสอบห้องว่างและบันทึกคำขอย้ายห้องของผู้เช่า
    // RI
    // 1. user ต้องไม่เป็น null สำหรับผู้เช่าที่เข้าสู่ระบบ
    // 2. available ต้องไม่เป็น null

    public static final String TRANSFER_FILE = "room_transfers.csv";
    public static final String TRANSFER_HEADER =
            "transferId,userId,contractId,oldRoomId,newRoomId,reason,requestDate,status";

    public enum Status { INVALID_INPUT, PENDING_EXISTS, SUCCESS }

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

    private final LoginLogic.User user;
    private final DataStore.Room current;
    private final DataStore.Contract contract;
    private final List<DataStore.Room> available;

    /**
     * คอนสตรักเตอร์สำหรับสร้างตรรกะการยื่นขอย้ายห้องพักของผู้เช่า
     * 
     * @param user ข้อมูลผู้เช่าที่กำลังเข้าสู่ระบบ
     */
    public TenantRoomChangeLogic(LoginLogic.User user) {
        this.user = user;
        this.current = (user == null) ? null : DataStore.room(user.roomId);
        this.contract = DataStore.contractFor(user);
        this.available = (current == null)
                ? new ArrayList<>()
                : DataStore.availableRooms(current.id);
    }

    /**
     * ตรวจสอบว่าผู้เช่ามีห้องพักปัจจุบันอยู่หรือไม่
     * 
     * @return true หากมีห้องพักปัจจุบัน
     */
    public boolean hasRoom() {
        return current != null;
    }

    /**
     * ดึงข้อมูลห้องพักปัจจุบันของผู้เช่า
     * 
     * @return ออบเจ็กต์ Room ห้องปัจจุบัน หรือ null
     */
    public DataStore.Room currentRoom() {
        return current;
    }

    /**
     * ดึงรายการห้องพักที่ว่างและสามารถขอย้ายไปได้
     * 
     * @return รายการห้องพักที่ว่าง
     */
    public List<DataStore.Room> availableRooms() {
        return available;
    }

    /**
     * บันทึกคำขอย้ายห้องพักใหม่ลงในไฟล์ room_transfers.csv
     * 
     * @param target ห้องพักเป้าหมายที่ต้องการย้ายไป
     * @param reason เหตุผลความจำเป็นในการขอย้ายห้อง
     * @return ออบเจ็กต์ Result ระบุผลลัพธ์การส่งคำขอ
     * @throws IOException หากเกิดข้อผิดพลาดในการเขียนไฟล์
     */
    public Result submit(DataStore.Room target, String reason) throws IOException {
        if (target == null || reason.isEmpty()) {
            return new Result(Status.INVALID_INPUT, null);
        }
        if (DataStore.hasPendingTransfer(user.userId)) {
            return new Result(Status.PENDING_EXISTS, null);
        }

        String transferId = DataStore.nextId(TRANSFER_FILE, "transferId", "T", 3);
        DataStore.appendRow(TRANSFER_FILE, TRANSFER_HEADER,
                transferId,
                DataStore.blank(user.userId) ? "-" : user.userId,
                contract == null ? "-" : contract.id,
                current.id,
                target.id,
                reason,
                LocalDate.now().toString(),
                "PENDING");
        return new Result(Status.SUCCESS, transferId);
    }
}
