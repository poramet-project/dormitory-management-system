package logic;


/**
 * GuestRoomData คือคลาสสำหรับเก็บข้อมูลห้องพักสำหรับแสดงผลในหน้าต่างของผู้เยี่ยมชม
 * id คือ รหัสห้องพัก
 * floor คือ ชั้นที่ตั้งของห้องพัก
 * type คือ ประเภทห้องพัก
 * status คือ สถานะห้องพัก (เช่น AVAILABLE)
 * rent คือ อัตราค่าเช่า
 * occupant คือ ข้อมูลผู้พักอาศัย (ถ้ามี)
 */
public class GuestRoomData {

    // ===== representation =====
    // Abstraction Function:
    // AF(id, floor, type, status, rent, occupant) คือ ข้อมูลจำลองของห้องพักสำหรับแสดงผลแก่ผู้เยี่ยมชม
    // RI
    // 1. id, floor, type, status ต้องไม่เป็น null
    // 2. rent ต้องไม่เป็น null

    public final String id, floor, type, status, rent, occupant;

    /**
     * คอนสตรักเตอร์สำหรับสร้างข้อมูลห้องพักสำหรับผู้เยี่ยมชม
     * 
     * @param id รหัสห้องพัก
     * @param floor ชั้นที่ห้องพักตั้งอยู่
     * @param type ประเภทห้องพัก
     * @param status สถานะของห้องพัก
     * @param rent อัตราค่าเช่า
     * @param occupant ข้อมูลผู้พักอาศัยปัจจุบัน (ถ้ามี)
     */
    public GuestRoomData(String id, String floor, String type, String status, String rent, String occupant) {
        this.id = id;
        this.floor = floor;
        this.type = type;
        this.status = status;
        this.rent = rent;
        this.occupant = occupant;
    }

    /**
     * ตรวจสอบว่าห้องพักนี้มีสถานะว่าง (AVAILABLE) หรือไม่
     * 
     * @return true หากเป็นห้องว่าง
     */
    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }
}
