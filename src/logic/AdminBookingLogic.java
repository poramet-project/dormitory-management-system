package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * AdminBookingLogic คือคลาสสำหรับจัดการตรรกะทางธุรกิจของคำขอจองห้องพักในระบบของผู้ดูแล
 * ให้บริการดึงข้อมูลคำขอจองทั้งหมด อนุมัติการจอง และปฏิเสธการจอง
 */
public class AdminBookingLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF() คือ ตัวแทนชุดคำสั่งสำหรับจัดการคำขอจองห้องพักที่บันทึกอยู่ใน DataStore.BOOKINGS_FILE
    // RI
    // 1. ตารางไฟล์การจอง DataStore.BOOKINGS_FILE ต้องมีอยู่จริงในระบบ
    // 2. การอนุมัติหรือปฏิเสธคำขอต้องระบุ bookingId ที่ถูกต้องและมีอยู่ในระบบ

    /**
     * BookingModel คือคลาสสำหรับเก็บข้อมูลคำขอจองห้องพักแต่ละรายการ
     * id คือ รหัสคำขอจอง (เช่น BK001)
     * userId คือ รหัสผู้ใช้งานที่ทำการจอง
     * name คือ ชื่อ-นามสกุลของผู้ยื่นคำขอ
     * phone คือ หมายเลขโทรศัพท์ติดต่อ
     * roomId คือ รหัสห้องพักที่ขอจอง
     * roomNumber คือ หมายเลขห้องพัก
     * date คือ วันที่ยื่นคำขอจอง
     * status คือ สถานะคำขอ (รอพิจารณา, อนุมัติ, ปฏิเสธ)
     * type คือ ประเภทคำขอ
     * detail คือ รายละเอียดคำขอ
     * roomDetail คือ รายละเอียดข้อมูลห้องพักและค่าเช่า
     */
    public static class BookingModel {

        // ===== representation =====
        // Abstraction Function:
        // AF(id, userId, name, phone, roomId, roomNumber, date, status, type, detail, roomDetail) คือ
        // ข้อมูลจำลองของคำขอจองห้องพัก 1 รายการในหอพัก
        // RI
        // 1. id, name, roomId, status ต้องไม่เป็น null
        // 2. phone ต้องไม่เป็น null
        // 3. status ต้องเป็นค่าสถานะที่ถูกต้อง ("รอพิจารณา", "อนุมัติ", "ปฏิเสธ")

        public String id, userId, name, phone, roomId, roomNumber, date, status, type, detail, roomDetail;

        /**
         * คอนสตรักเตอร์สำหรับกำหนดค่าเริ่มต้นของข้อมูลคำขอจองห้องพัก
         *
         * @param id รหัสคำขอจอง
         * @param userId รหัสผู้ใช้งานที่ทำการจอง
         * @param name ชื่อ-นามสกุลของผู้ยื่นคำขอ
         * @param phone หมายเลขโทรศัพท์ติดต่อ
         * @param roomId รหัสห้องพักที่ขอจอง
         * @param roomNumber หมายเลขห้องพัก
         * @param date วันที่ยื่นคำขอจอง
         * @param status สถานะคำขอจอง (เช่น รอพิจารณา, อนุมัติ, ปฏิเสธ)
         * @param type ประเภทของคำขอ
         * @param detail รายละเอียดคำขอ
         */
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

    /**
     * ดึงข้อมูลคำขอจองห้องพักทั้งหมดจากไฟล์ bookings.csv
     * ทำการเชื่อมโยงข้อมูลกับห้องพัก (rooms.csv) และผู้ใช้งาน (users.csv)
     * เพื่อแปลงสถานะและวันที่ให้อยู่ในรูปแบบภาษาไทยพร้อมนำไปแสดงผล
     *
     * @return รายการ (List) ของ BookingModel ที่มีข้อมูลคำขอจองครบถ้วน
     */
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

    /**
     * แปลงรายการคำขอจองห้องพักให้อยู่ในรูปแบบ Object[][]
     * เพื่อนำไปใช้เป็นข้อมูลตารางสำหรับ JTable และ DefaultTableModel
     * โดยมี 5 คอลัมน์ ได้แก่ เลขคำขอ, ชื่อผู้ยื่น, ห้อง, วันที่ยื่น, สถานะ
     *
     * @return อาร์เรย์ 2 มิติของข้อมูลสำหรับแสดงผลในตาราง
     */
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
     * ดำเนินการอนุมัติคำขอจองห้องพักตาม bookingId
     * ทำหน้าที่:
     * 1. ตรวจสอบและค้นหา userId ของผู้ยื่นคำขอ
     * 2. อัปเดตสถานะคำขอใน bookings.csv เป็น APPROVED
     * 3. สร้างสัญญาเช่าใหม่ใน contracts.csv โดยคิดเงินประกัน 2 เท่าของค่าห้อง
     * 4. ปรับปรุงสถานะห้องพักใน rooms.csv เป็น OCCUPIED (มีผู้เช่า)
     * 5. ปรับบทบาทผู้ใช้งานใน users.csv เป็น TENANT พร้อมบันทึก roomId
     *
     * @param bookingId รหัสคำขอจองห้องพักที่ต้องการอนุมัติ
     * @return true หากทำการอนุมัติสำเร็จ, false หากไม่พบคำขอจอง
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

    /**
     * ดำเนินการปฏิเสธคำขอจองห้องพักตาม bookingId
     * ทำหน้าที่อัปเดตสถานะของคำขอใน bookings.csv เป็น REJECTED
     *
     * @param bookingId รหัสคำขอจองห้องพักที่ต้องการปฏิเสธ
     * @return true หากทำการปฏิเสธและอัปเดตไฟล์สำเร็จ, false หากเกิดข้อผิดพลาดหรือไม่พบข้อมูล
     */
    public boolean rejectBooking(String bookingId) {
        return DataStore.updateCell(DataStore.BOOKINGS_FILE, "bookingId", bookingId, "status", "REJECTED");
    }
}