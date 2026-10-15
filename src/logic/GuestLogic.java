package logic;

import java.util.ArrayList;
import java.util.List;

/**
 * GuestLogic
 * คือคลาสสำหรับจัดการตรรกะทางธุรกิจของหน้าค้นหาห้องพักสำหรับผู้เยี่ยมชม (Guest)
 * selectedTypeFilter คือ ตัวกรองประเภทห้องพักที่เลือก ("ทั้งหมด", "ห้องแอร์",
 * "ห้องพัดลม")
 * selectedPriceFilter คือ ตัวกรองช่วงราคาห้องพักที่เลือก
 * keyword คือ คำค้นหาหมายเลขห้องหรือรายละเอียด
 */
public class GuestLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF(selectedTypeFilter, selectedPriceFilter, keyword) คือ
    // สถานะของเงื่อนไขการค้นหาและกรองห้องพักสำหรับผู้เยี่ยมชม
    // RI
    // 1. selectedTypeFilter, selectedPriceFilter, keyword ต้องไม่เป็น null

    private String selectedTypeFilter = "ทั้งหมด";
    private String selectedPriceFilter = "ทั้งหมด";
    private String keyword = "";

    /**
     * RoomViewModel คือคลาสแบบจำลองข้อมูลห้องพักสำหรับแสดงผลเป็นการ์ดห้องพักในหน้า
     * GuestUI
     * id คือ รหัสห้องพัก
     * numberText คือ ข้อความหมายเลขห้องพัก (เช่น ห้อง 101)
     * typeShort คือ ประเภทห้องพักย่อ (ห้องแอร์, ห้องพัดลม)
     * floorText คือ ข้อความระบุชั้นและขนาดห้อง
     * priceText คือ ข้อความอัตราค่าเช่า
     * rawPrice คือ ราคาค่าเช่าตัวเลข
     * status คือ สถานะห้องพัก
     * statusLabel คือ ข้อความป้ายสถานะภาษาไทย
     */
    public static class RoomViewModel {

        // ===== representation =====
        // Abstraction Function:
        // AF(id, numberText, typeShort, floorText, priceText, rawPrice, status,
        // statusLabel) คือ
        // ข้อมูลจำลองมุมมองของห้องพัก 1 ห้องสำหรับแสดงผลแก่ผู้เยี่ยมชม
        // RI
        // 1. id, numberText, typeShort, status, statusLabel ต้องไม่เป็น null
        // 2. rawPrice ต้อง >= 0

        public final String id;
        public final String numberText;
        public final String typeShort;
        public final String floorText;
        public final String priceText;
        public final int rawPrice;
        public final String status;
        public final String statusLabel;

        /**
         * สร้างแบบจำลองข้อมูลห้องพักสำหรับการแสดงผลบนหน้าจอค้นหาห้องพัก
         * 
         * @param room        ข้อมูลห้องพักจากระบบ
         * @param currentUser ข้อมูลผู้ใช้งานที่กำลังเข้าสู่ระบบอยู่ในขณะนั้น
         */
        public RoomViewModel(DataStore.Room room, LoginLogic.User currentUser) {
            this.id = room.id;
            this.numberText = "ห้อง " + room.number;
            this.typeShort = room.typeShort();
            this.floorText = "ชั้น " + room.floor() + "  •  22 ตร.ม.";
            this.rawPrice = room.rent;
            this.priceText = DataStore.money(room.rent);
            this.status = room.status;

            if ("OCCUPIED".equalsIgnoreCase(room.status)) {
                this.statusLabel = "มีผู้เช่า";
            } else if ("MAINTENANCE".equalsIgnoreCase(room.status)
                    || "UNDER_RENOVATION".equalsIgnoreCase(room.status)) {
                this.statusLabel = "ปิดปรับปรุง";
            } else {
                // เช็คว่า User ปัจจุบันเป็นเจ้าของคำขอจองห้องนี้ที่รออนุมัติอยู่หรือไม่
                if (currentUser != null) {
                    String name = DataStore.blank(currentUser.fullName) ? currentUser.username : currentUser.fullName;
                    if (DataStore.isRoomPendingByUser(room.id, name)) {
                        this.statusLabel = "รออนุมัติการจอง";
                    } else {
                        this.statusLabel = "ห้องว่าง";
                    }
                } else {
                    this.statusLabel = "ห้องว่าง";
                }
            }
        }
    }

    /**
     * กำหนดเงื่อนไขตัวกรองประเภทห้องพัก
     * 
     * @param typeFilter ตัวกรองประเภทห้องพัก เช่น "ทั้งหมด", "ห้องแอร์",
     *                   "ห้องพัดลม"
     */
    public void setTypeFilter(String typeFilter) {
        this.selectedTypeFilter = typeFilter != null ? typeFilter : "ทั้งหมด";
    }

    /**
     * กำหนดเงื่อนไขตัวกรองช่วงราคาห้องพัก
     * 
     * @param priceFilter ตัวกรองช่วงราคา เช่น "ทั้งหมด", "ต่ำกว่า 3,000 บาท"
     */
    public void setPriceFilter(String priceFilter) {
        this.selectedPriceFilter = priceFilter != null ? priceFilter : "ทั้งหมด";
    }

    /**
     * กำหนดคำค้นหาสำหรับค้นหาห้องพัก
     * 
     * @param keyword คำค้นหาเลขห้องหรือชั้น
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword != null ? keyword.trim() : "";
    }

    /**
     * ดึงรายการตัวเลือกประเภทห้องพักสำหรับแสดงใน Dropdown กรองข้อมูล
     * 
     * @return รายการตัวเลือกประเภทห้องพัก
     */
    public String[] getTypeOptions() {
        return new String[] { "ทั้งหมด", "ห้องพัดลม", "ห้องแอร์" };
    }

    /**
     * ดึงรายการตัวเลือกช่วงราคาสำหรับแสดงใน Dropdown กรองข้อมูล
     * 
     * @return รายการตัวเลือกช่วงราคา
     */
    public String[] getPriceOptions() {
        return new String[] { "ทั้งหมด", "ต่ำกว่า 3,000 บาท", "3,000 บาทขึ้นไป" };
    }

    /**
     * ตรวจสอบว่าผู้ใช้งานปัจจุบันได้เข้าสู่ระบบแล้วหรือไม่
     * 
     * @return true หากเข้าสู่ระบบแล้ว
     */
    public boolean isLoggedIn() {
        return LoginLogic.getCurrentUser() != null;
    }

    /**
     * ดึงชื่อสำหรับแสดงผลของผู้ใช้งานที่กำลังเข้าสู่ระบบอยู่
     * 
     * @return ชื่อ-นามสกุล หรือชื่อผู้ใช้งาน
     */
    public String getCurrentDisplayName() {
        LoginLogic.User u = LoginLogic.getCurrentUser();
        if (u == null)
            return "";
        return (DataStore.blank(u.fullName)) ? u.username : u.fullName;
    }

    /**
     * ดำเนินการออกจากระบบและรีเซ็ตค่าสถานะ Session เป็นผู้เยี่ยมชม (Guest)
     */
    public void performLogout() {
        LoginLogic.logout();
        DataStore.setCurrentLoginUser("Guest");
    }

    /**
     * ดึงรายการแบบจำลองห้องพักที่ผ่านการกรองตามเงื่อนไขที่กำหนดไว้ในออบเจ็กต์นี้
     * 
     * @return รายการห้องพักที่ตรงตามเงื่อนไข
     */
    public List<RoomViewModel> getFilteredRooms() {
        return getFilteredRooms(this.keyword, this.selectedTypeFilter, this.selectedPriceFilter);
    }

    /**
     * ดึงรายการแบบจำลองห้องพักที่ผ่านการกรองตามคำค้นหา ประเภทห้อง
     * และช่วงราคาที่กำหนด
     * 
     * @param keyword     คำค้นหาเลขห้องหรือชั้น
     * @param typeFilter  ตัวกรองประเภทห้อง
     * @param priceFilter ตัวกรองช่วงราคา
     * @return รายการห้องพักที่ตรงตามเงื่อนไขทั้งหมด
     */
    public static List<RoomViewModel> getFilteredRooms(String keyword, String typeFilter, String priceFilter) {
        List<RoomViewModel> result = new ArrayList<>();
        List<DataStore.Room> allRooms = DataStore.rooms();
        LoginLogic.User currentUser = LoginLogic.getCurrentUser();

        for (DataStore.Room r : allRooms) {
            RoomViewModel vm = new RoomViewModel(r, currentUser);

            if (keyword != null && !keyword.isEmpty() && !keyword.equals("เช่น 101, ชั้น 2...")) {
                String kw = keyword.toLowerCase();
                boolean matchNum = vm.numberText.toLowerCase().contains(kw) || r.number.toLowerCase().contains(kw);
                boolean matchFloor = vm.floorText.toLowerCase().contains(kw);
                if (!matchNum && !matchFloor) {
                    continue;
                }
            }

            if (typeFilter != null && !typeFilter.equals("ทั้งหมด")) {
                if (typeFilter.equals("ห้องพัดลม") && !r.typeShort().contains("พัดลม"))
                    continue;
                if (typeFilter.equals("ห้องแอร์") && !r.typeShort().contains("แอร์"))
                    continue;
            }

            if (priceFilter != null && !priceFilter.equals("ทั้งหมด")) {
                if (priceFilter.equals("ต่ำกว่า 3,000 บาท") && r.rent >= 3000)
                    continue;
                if (priceFilter.equals("3,000 บาทขึ้นไป") && r.rent < 3000)
                    continue;
            }

            result.add(vm);
        }
        return result;
    }
}