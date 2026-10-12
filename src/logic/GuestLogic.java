package logic;

import java.util.ArrayList;
import java.util.List;

/** Logic สำหรับหน้าค้นหาห้องพักและการจัดการ Session ของ Guest / ผู้เยี่ยมชม */
public class GuestLogic {

    private String selectedTypeFilter = "ทั้งหมด";
    private String selectedPriceFilter = "ทั้งหมด";
    private String keyword = "";

    /** Model ข้อมูลห้องพักสำหรับส่งให้ UI นำไปแสดงผล */
    public static class RoomViewModel {
        public final String id;
        public final String numberText;
        public final String typeShort;
        public final String floorText;
        public final String priceText;
        public final int rawPrice;
        public final String status;
        public final String statusLabel;

        public RoomViewModel(DataStore.Room room) {
            this.id = room.id;
            this.numberText = "ห้อง " + room.number;
            this.typeShort = room.typeShort();
            this.floorText = "ชั้น " + room.floor() + "  •  22 ตร.ม.";
            this.rawPrice = room.rent;
            this.priceText = DataStore.money(room.rent);
            this.status = room.status;
            this.statusLabel = room.statusThai();
        }
    }

    // --- Getters / Setters สำหรับ Filter State ---
    public void setTypeFilter(String typeFilter) {
        this.selectedTypeFilter = typeFilter != null ? typeFilter : "ทั้งหมด";
    }

    public void setPriceFilter(String priceFilter) {
        this.selectedPriceFilter = priceFilter != null ? priceFilter : "ทั้งหมด";
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword != null ? keyword.trim() : "";
    }

    /** ดึงรายการตัวเลือกประเภทห้องพัก */
    public String[] getTypeOptions() {
        return new String[]{"ทั้งหมด", "ห้องพัดลม", "ห้องแอร์"};
    }

    /** ดึงรายการตัวเลือกช่วงราคา */
    public String[] getPriceOptions() {
        return new String[]{"ทั้งหมด", "ต่ำกว่า 3,000 บาท", "3,000 บาทขึ้นไป"};
    }

    /** ตรวจสอบว่ามีผู้ใช้ล็อกอินอยู่หรือไม่ */
    public boolean isLoggedIn() {
        return LoginLogic.getCurrentUser() != null;
    }

    /** ดึงชื่อผู้ใช้ปัจจุบันที่ล็อกอินอยู่ */
    public String getCurrentDisplayName() {
        LoginLogic.User u = LoginLogic.getCurrentUser();
        if (u == null) return "";
        return (DataStore.blank(u.fullName)) ? u.username : u.fullName;
    }

    /** ออกจากระบบและตั้งสถานะเป็น Guest */
    public void performLogout() {
        LoginLogic.logout();
        DataStore.setCurrentLoginUser("Guest");
    }

    /** ดึงรายการห้องพักที่ผ่านการกรองตามเงื่อนไขปัจจุบัน */
    public List<RoomViewModel> getFilteredRooms() {
        return getFilteredRooms(this.keyword, this.selectedTypeFilter, this.selectedPriceFilter);
    }

    /** ดึงรายการห้องพักทั้งหมดและแปลงเป็น ViewModel พร้อมกรองตามเงื่อนไข */
    public static List<RoomViewModel> getFilteredRooms(String keyword, String typeFilter, String priceFilter) {
        List<RoomViewModel> result = new ArrayList<>();
        List<DataStore.Room> allRooms = DataStore.rooms();

        for (DataStore.Room r : allRooms) {
            RoomViewModel vm = new RoomViewModel(r);

            // 1. กรองตาม Keyword (เลขห้อง หรือ ชั้น)
            if (keyword != null && !keyword.isEmpty() && !keyword.equals("เช่น 101, ชั้น 2...")) {
                String kw = keyword.toLowerCase();
                boolean matchNum = vm.numberText.toLowerCase().contains(kw) || r.number.toLowerCase().contains(kw);
                boolean matchFloor = vm.floorText.toLowerCase().contains(kw);
                if (!matchNum && !matchFloor) {
                    continue;
                }
            }

            // 2. กรองตามประเภทห้องพัก
            if (typeFilter != null && !typeFilter.equals("ทั้งหมด")) {
                if (typeFilter.equals("ห้องพัดลม") && !r.typeShort().contains("พัดลม")) continue;
                if (typeFilter.equals("ห้องแอร์") && !r.typeShort().contains("แอร์")) continue;
            }

            // 3. กรองตามช่วงราคา
            if (priceFilter != null && !priceFilter.equals("ทั้งหมด")) {
                if (priceFilter.equals("ต่ำกว่า 3,000 บาท") && r.rent >= 3000) continue;
                if (priceFilter.equals("3,000 บาทขึ้นไป") && r.rent < 3000) continue;
            }

            result.add(vm);
        }
        return result;
    }
}