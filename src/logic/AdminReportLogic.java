package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * AdminReportLogic คือคลาสสำหรับจัดการตรรกะทางธุรกิจของระบบงานแจ้งซ่อมและข้อร้องเรียนสำหรับผู้ดูแล
 * ทำหน้าที่อ่านรายการปัญหา อัปเดตสถานะการส่งช่างเข้าตรวจสอบ และแปลงข้อมูลเพื่อแสดงผลบนตาราง
 */
public class AdminReportLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF() คือ ตัวแทนตรรกะการประมวลผลคำร้องแจ้งซ่อมจาก DataStore.COMPLAINTS_FILE
    // RI
    // 1. DataStore.COMPLAINTS_FILE ต้องสามารถอ่านและอัปเดตสถานะได้ถูกต้อง

    /**
     * ProblemModel คือคลาสสำหรับเก็บข้อมูลปัญหาการแจ้งซ่อมแต่ละรายการ
     * id คือ รหัสคำร้องแจ้งซ่อม (เช่น C001)
     * name คือ ชื่อผู้ยื่นคำร้อง
     * phone คือ เบอร์โทรศัพท์ของผู้ยื่น
     * room คือ หมายเลขห้องพักที่แจ้งซ่อม
     * date คือ วันที่นัดหมายซ่อม
     * time คือ เวลานัดหมายซ่อม
     * status คือ สถานะการดำเนินการ (รอดำเนินการ, ช่างกำลังเข้าตรวจสอบ, เสร็จสิ้น)
     * category คือ หมวดหมู่งานซ่อม (ระบบประปา, ไฟฟ้า, อื่นๆ)
     * desc คือ รายละเอียดปัญหา
     * topic คือ หัวข้อเรื่องที่แจ้งซ่อม
     */
    public static class ProblemModel {

        // ===== representation =====
        // Abstraction Function:
        // AF(id, name, phone, room, date, time, status, category, desc, topic) คือ
        // ข้อมูลปัญหาคำร้องแจ้งซ่อม 1 รายการ
        // RI
        // 1. id, room, status, category ต้องไม่เป็น null
        // 2. name และ phone ต้องไม่เป็น null

        public String id, name, phone, room, date, time, status, category, desc, topic;

        /**
         * คอนสตรักเตอร์สำหรับสร้างอ็อบเจกต์ ProblemModel
         *
         * @param id รหัสรายการแจ้งซ่อม
         * @param name ชื่อ-นามสกุลของผู้แจ้ง
         * @param phone เบอร์โทรศัพท์ของผู้แจ้ง
         * @param room หมายเลขห้องพัก
         * @param date วันที่แจ้งซ่อม
         * @param time เวลานัดหมายหรือเวลาที่แจ้ง
         * @param status สถานะการดำเนินการ (รอดำเนินการ, ช่างกำลังเข้าตรวจสอบ, เสร็จสิ้น)
         * @param category หมวดหมู่งานซ่อม
         * @param desc รายละเอียดปัญหา
         * @param topic หัวข้อเรื่องที่แจ้ง
         */
        public ProblemModel(String id, String name, String phone, String room, String date, String time, String status, String category, String desc, String topic) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.room = room;
            this.date = date;
            this.time = time;
            this.status = status;
            this.category = category;
            this.desc = desc;
            this.topic = topic;
        }
    }

    /**
     * ดึงข้อมูลรายการแจ้งซ่อมและข้อร้องเรียนทั้งหมดจากไฟล์ complaints.csv
     * พร้อมเชื่อมโยงข้อมูลผู้ใช้และห้องพักเพื่อแสดงชื่อ เบอร์โทร และหมายเลขห้อง
     *
     * @return รายการ List ของ ProblemModel ทั้งหมดในระบบ
     */
    public List<ProblemModel> getAllProblems() {
        List<ProblemModel> list = new ArrayList<>();
        List<Map<String, String>> rows = DataStore.readTable(DataStore.COMPLAINTS_FILE);
        for (Map<String, String> m : rows) {
            String id = m.getOrDefault("complaintId", "-");
            String userId = m.getOrDefault("userId", "-");
            String roomId = m.getOrDefault("roomId", "-");
            String category = m.getOrDefault("category", "-");
            String topic = m.getOrDefault("topic", "-");
            String desc = m.getOrDefault("description", "-");
            String dateRaw = m.getOrDefault("requestDate", "-");
            String statusRaw = m.getOrDefault("status", "PENDING");

            LoginLogic.User u = LoginLogic.findUser(userId);
            String name = u != null ? u.fullName : userId;
            String phone = u != null ? u.phone : "-";

            DataStore.Room r = DataStore.room(roomId);
            String roomNum = r != null ? r.number : roomId;

            String statusThai = "IN_PROGRESS".equalsIgnoreCase(statusRaw) ? "ช่างกำลังเข้าตรวจสอบ" :
                               ("RESOLVED".equalsIgnoreCase(statusRaw) ? "เสร็จสิ้น" : "รอดำเนินการ");

            String dateThai = DataStore.thaiDate(dateRaw);
            String timeThai = DataStore.thaiTime(dateRaw);

            list.add(new ProblemModel(id, name, phone, roomNum, dateThai, timeThai, statusThai, category, desc, topic));
        }
        return list;
    }

    /**
     * แปลงรายการแจ้งซ่อมให้อยู่ในรูปแบบ Object[][] สำหรับแสดงผลบน JTable
     * ประกอบด้วย 6 คอลัมน์: รหัสแจ้งซ่อม, ชื่อผู้แจ้ง, ห้อง, หมวดหมู่, วันที่, สถานะ
     *
     * @return อาร์เรย์ 2 มิติของข้อมูลสำหรับ DefaultTableModel
     */
    public Object[][] getTableRows() {
        List<ProblemModel> list = getAllProblems();
        Object[][] rows = new Object[list.size()][6];
        for (int i = 0; i < list.size(); i++) {
            ProblemModel p = list.get(i);
            rows[i] = new Object[]{p.id, p.name, p.room, p.category, p.date, p.status};
        }
        return rows;
    }

    /**
     * รับเรื่องแจ้งซ่อม โดยปรับสถานะเป็น IN_PROGRESS (ช่างกำลังเข้าตรวจสอบ)
     *
     * @param complaintId รหัสรายการแจ้งซ่อม
     * @return true หากอัปเดตสถานะสำเร็จ, false หากเกิดข้อผิดพลาด
     */
    public boolean acceptProblem(String complaintId) {
        return DataStore.updateCell(DataStore.COMPLAINTS_FILE, "complaintId", complaintId, "status", "IN_PROGRESS");
    }

    /**
     * ปิดงานแจ้งซ่อม โดยปรับสถานะเป็น RESOLVED (เสร็จสิ้น)
     *
     * @param complaintId รหัสรายการแจ้งซ่อม
     * @return true หากอัปเดตสถานะสำเร็จ, false หากเกิดข้อผิดพลาด
     */
    public boolean resolveProblem(String complaintId) {
        return DataStore.updateCell(DataStore.COMPLAINTS_FILE, "complaintId", complaintId, "status", "RESOLVED");
    }
}