package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * 
 * AdminBookingLogic
 */
public class AdminBookingLogic {

    public static class BookingModel {
        public String id, userId, name, phone, roomId, roomNumber, date, status, type, detail, roomDetail;

        public BookingModel(String id, String userId, String name, String phone, String roomId, String roomNumber, String date, String status, String type, String detail) {
            this.id = id;
            this.userId = userId;
            this.name = name;
            this.phone = phone;
            this.roomId = roomId;
            this.roomNumber = roomNumber;
            this.date = date;
            this.status = status;
            this.type = type;
            this.detail = detail;
            this.roomDetail = detail;
        }
    }

    public List<BookingModel> getAllBookings() {
        List<BookingModel> list = new ArrayList<>();
        List<Map<String, String>> rows = DataStore.readTable(DataStore.BOOKINGS_FILE);

        for (Map<String, String> m : rows) {
            String bId = m.getOrDefault("bookingId", "-");
            String rId = m.getOrDefault("roomId", "-");
            String name = m.getOrDefault("fullName", "-");
            String phone = m.getOrDefault("phone", "-");
            String dateRaw = m.getOrDefault("requestDate", "-");
            String statusRaw = m.getOrDefault("status", "PENDING");

            String dateThai = DataStore.thaiDate(dateRaw);
            String statusThai = "APPROVED".equalsIgnoreCase(statusRaw) ? "อนุมัติ" :
                               ("REJECTED".equalsIgnoreCase(statusRaw) ? "ปฏิเสธ" : "รอพิจารณา");

            String uId = name;
            for (Map<String, String> u : DataStore.readTable("users.csv")) {
                if (name.equalsIgnoreCase(u.getOrDefault("fullName", "").trim())) {
                    uId = u.getOrDefault("userId", "").trim();
                    break;
                }
            }

            DataStore.Room r = DataStore.room(rId);
            String roomNum = r != null ? r.number : rId;
            String roomDetail = r != null ? "ห้อง " + r.number + " • " + r.typeShort() + " • " + DataStore.money(r.rent) + " บาท/เดือน" : "ห้อง " + rId;

            list.add(new BookingModel(bId, uId, name, phone, rId, roomNum, dateThai, statusThai, "จองห้อง", roomDetail));
        }
        return list;
    }

    public Object[][] getTableRows() {
        List<BookingModel> list = getAllBookings();
        Object[][] rows = new Object[list.size()][5];
        for (int i = 0; i < list.size(); i++) {
            BookingModel b = list.get(i);
            rows[i] = new Object[]{b.id, b.name, b.roomNumber, b.date, b.status};
        }
        return rows;
    }

    /**
     * อนุมัติการจอง + บังคับอัปเดตบทบาทใน users.csv เป็น TENANT + เขียนสัญญา (ประกัน x 2)
     */
    public boolean approveBooking(String bookingId) {
        for (Map<String, String> m : DataStore.readTable(DataStore.BOOKINGS_FILE)) {
            if (m.getOrDefault("bookingId", "").equalsIgnoreCase(bookingId)) {
                String roomId = m.getOrDefault("roomId", "");
                String name = m.getOrDefault("fullName", "").trim();
                String phone = m.getOrDefault("phone", "").trim();

                // 1. ค้นหา userId ที่ตรงกับชื่อ หรือ เบอร์โทร ใน users.csv
                String targetUserId = null;
                for (Map<String, String> u : DataStore.readTable("users.csv")) {
                    String uName = u.getOrDefault("fullName", "").trim();
                    String uPhone = u.getOrDefault("phone", "").trim();
                    if (name.equalsIgnoreCase(uName) || (!phone.isEmpty() && phone.equals(uPhone))) {
                        targetUserId = u.getOrDefault("userId", "").trim();
                        break;
                    }
                }

                if (targetUserId == null) {
                    targetUserId = name; // หากไม่พบให้ใช้ชื่อเป็น ID สำรอง
                }

                DataStore.Room r = DataStore.room(roomId);
                int rent = r != null ? r.rent : 2800;
                int deposit = rent * 2; // เงินประกัน = 2 เท่าของค่าห้อง

                // 2. อัปเดตใบจองเป็น APPROVED
                DataStore.updateCell(DataStore.BOOKINGS_FILE, "bookingId", bookingId, "status", "APPROVED");

                // 3. สร้างสัญญาใน contracts.csv
                DataStore.saveContract(targetUserId, roomId, rent, "ACTIVE");

                // 4. บันทึกเงินประกัน (ประกัน x 2) ลงในสัญญาฉบับล่าสุด
                for (DataStore.Contract c : DataStore.contracts()) {
                    if (c.userId.equalsIgnoreCase(targetUserId) && c.roomId.equalsIgnoreCase(roomId) && c.isActive()) {
                        DataStore.updateCell(DataStore.CONTRACTS_FILE, "contractId", c.id, "deposit", String.valueOf(deposit));
                    }
                }

                // 5. ปรับสถานะห้องเป็น OCCUPIED
                DataStore.setRoomStatus(roomId, "OCCUPIED");

                // 6. บังคับอัปเดตไฟล์ users.csv โดยตรง (เปลี่ยน role เป็น TENANT และผูก roomId)
                DataStore.updateCell("users.csv", "userId", targetUserId, "roomId", roomId);
                DataStore.updateCell("users.csv", "userId", targetUserId, "role", "TENANT");

                // เรียก LoginLogic เพื่อให้แรม/เซสชันซิงค์ตาม (ถ้ามี)
                try {
                    LoginLogic.assignRoom(targetUserId, roomId);
                } catch (Exception ignored) {}

                return true;
            }
        }
        return false;
    }

    public boolean rejectBooking(String bookingId) {
        return DataStore.updateCell(DataStore.BOOKINGS_FILE, "bookingId", bookingId, "status", "REJECTED");
    }
}