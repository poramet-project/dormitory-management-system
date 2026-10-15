package logic;


/**
 * TenantMyRoomLogic คือคลาสที่ใช้สำหรับจัดการตรรกะข้อมูลห้องพักของผู้เช่าปัจจุบัน
 * user คือ ข้อมูลผู้ใช้งานปัจจุบันที่กำลังล็อกอิน
 * room คือ ข้อมูลห้องพักของผู้ใช้งาน
 * contract คือ ข้อมูลสัญญาเช่าของผู้ใช้งาน
 */
public class TenantMyRoomLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF(user, room, contract) คือ ตัวแทนตรรกะสำหรับการแสดงรายละเอียดห้องพัก อัตราค่าเช่า และสิ่งอำนวยความสะดวกของผู้เช่า
    // RI
    // 1. user ต้องไม่เป็น null สำหรับผู้เช่าที่เข้าสู่ระบบ
    // 2. ถ้า room ไม่เป็น null ต้องมีหมายเลขห้อง (number) ที่ถูกต้อง

    private final LoginLogic.User user;
    private final DataStore.Room room;
    private final DataStore.Contract contract;

    /**
     * คอนสตรักเตอร์สำหรับสร้างตรรกะข้อมูลห้องพักของผู้เช่า
     * 
     * @param user ข้อมูลผู้เช่าที่กำลังเข้าสู่ระบบ
     */
    public TenantMyRoomLogic(LoginLogic.User user) {
        this.user = user;
        this.room = (user == null) ? null : DataStore.room(user.roomId);
        this.contract = DataStore.contractFor(user);
    }

    /**
     * ตรวจสอบว่าผู้เช่ามีข้อมูลห้องพักผูกอยู่หรือไม่
     * 
     * @return true หากพบข้อมูลห้องพักของผู้เช่า
     */
    public boolean hasRoom() {
        return room != null;
    }

    /**
     * ดึงข้อความแจ้งเตือนกรณีที่ผู้เช่ายังไม่มีห้องพักหรือหาข้อมูลห้องไม่พบ
     * 
     * @return ข้อความแจ้งเตือน
     */
    public String emptyMessage() {
        return (user == null || DataStore.blank(user.roomId))
                ? "คุณยังไม่มีห้องพัก"
                : "ไม่พบข้อมูลห้อง " + user.roomId + " ในไฟล์ rooms.csv";
    }

    /**
     * ดึงข้อความหัวเรื่องหมายเลขห้องพัก
     * 
     * @return ข้อความหัวเรื่อง เช่น "ห้องพักหมายเลข 101"
     */
    public String title() {
        return "ห้องพักหมายเลข " + room.number;
    }

    /**
     * ดึงชื่อประเภทห้องพักแบบเต็ม
     * 
     * @return ประเภทห้องพักแบบเต็ม เช่น "ห้องแอร์ (Air)"
     */
    public String typeFull() {
        return room.typeFull();
    }

    /**
     * ดึงข้อความแสดงที่ตั้ง ชั้น และสถานะของห้องพัก
     * 
     * @return ข้อความรายละเอียดที่ตั้ง
     */
    public String locationText() {
        return "อาคารหอพักนักศึกษา • ชั้น " + room.floor() + " • สถานะห้อง: " + room.statusThai();
    }

    /**
     * ดึงข้อความอัตราค่าเช่าต่อเดือน (อิงตามสัญญาหรือค่าเช่าพื้นฐานของห้อง)
     * 
     * @return ข้อความอัตราค่าเช่า เช่น "อัตราค่าเช่า: 4,500 บาท / เดือน"
     */
    public String rentText() {
        // ถ้ามีสัญญา ใช้ค่าเช่าตามสัญญา ไม่งั้นใช้ค่าเช่าพื้นฐานของห้อง
        int rentValue = (contract != null && contract.rent > 0) ? contract.rent : room.rent;
        return "อัตราค่าเช่า: " + DataStore.money(rentValue) + " บาท / เดือน";
    }

    /**
     * ดึงรายการสิ่งอำนวยความสะดวกและเฟอร์นิเจอร์ภายในห้องพัก
     * 
     * @return อาร์เรย์ของรายการสิ่งอำนวยความสะดวก
     */
    public String[] facilities() {
        String cooling = room.isAir()
                ? " เครื่องปรับอากาศ Inverter ประหยัดไฟ"
                : " พัดลมติดเพดาน";
        return new String[]{
                " เตียงนอนเดี่ยว 3.5 ฟุต พร้อมฟูก (2 ชุด)",
                cooling,
                " ตู้เสื้อผ้าบิวท์อิน 2 บาน",
                " เครื่องทำน้ำอุ่นในห้องน้ำส่วนตัว",
                " โต๊ะทำงานและเก้าอี้ (2 ชุด)",
                " Wi-Fi ความเร็วสูงฟรี"
        };
    }
}
