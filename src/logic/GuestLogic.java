package logic;

import java.util.ArrayList;
import java.util.List;

/** Logic สำหรับหน้าค้นหาห้องพักของ Guest / ผู้เยี่ยมชม */
public class GuestLogic {

    public static class RoomViewModel {
        public final String id;
        public final String numberText;
        public final String typeShort;
        public final String floorText;
        public final String priceText;
        public final int rawPrice;
        public final String status;
        public final String statusLabel;
        public final String availableDate;

        public RoomViewModel(DataStore.Room room) {
            this.id = room.id;
            this.numberText = "ห้อง " + room.number;
            this.typeShort = room.typeShort();
            this.floorText = "ชั้น " + room.floor() + "  •  22 ตร.ม.";
            this.rawPrice = room.rent;
            this.priceText = DataStore.money(room.rent);
            this.status = room.status;
            this.statusLabel = room.statusThai();
            this.availableDate = "";
        }
    }

    /** ดึงรายการห้องพักทั้งหมดและแปลงเป็น ViewModel พร้อมกรองตามเงื่อนไข */
    public static List<RoomViewModel> getFilteredRooms(String keyword, String typeFilter, String priceFilter) {
        List<RoomViewModel> result = new ArrayList<>();
        List<DataStore.Room> allRooms = DataStore.rooms();

        for (DataStore.Room r : allRooms) {
            RoomViewModel vm = new RoomViewModel(r);

            // 1. กรองตาม Keyword (เลขห้อง หรือ ชั้น)
            if (keyword != null && !keyword.trim().isEmpty() && !keyword.equals("เช่น 101, ชั้น 2...")) {
                String kw = keyword.trim().toLowerCase();
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