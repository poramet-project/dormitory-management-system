package logic;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TenantReportLogic คือคลาสสำหรับจัดการตรรกะการแจ้งซ่อมและข้อร้องเรียนของผู้เช่า
 * STEPS คือ รายการข้อความแสดงขั้นตอนสถานะการแจ้งซ่อม
 * CATEGORIES คือ รายการหมวดหมู่การแจ้งซ่อม
 * STATUS_BY_STEP คือ รหัสสถานะในระบบตามขั้นตอน
 * ID_P คือ รูปแบบ regex สำหรับตรวจจับและแยกตัวเลขในรหัส
 * user คือ ข้อมูลผู้ใช้งานปัจจุบัน
 * reportList คือ รายการคำร้องแจ้งซ่อมของผู้ใช้งาน
 */
public class TenantReportLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF(user, reportList) คือ ตัวแทนตรรกะสำหรับการโหลด บันทึก และติดตามสถานะการแจ้งซ่อมของผู้เช่า
    // RI
    // 1. user ต้องไม่เป็น null สำหรับผู้เช่าที่เข้าสู่ระบบ
    // 2. reportList ต้องไม่เป็น null

    public static final String[] STEPS = {"ส่งเรื่อง", "ดำเนินการ", "เสร็จสิ้น"};
    public static final String[] CATEGORIES = {
            "ระบบประปา", "ระบบไฟฟ้า", "เครื่องปรับอากาศ", "เฟอร์นิเจอร์", "อินเทอร์เน็ต", "อื่น ๆ"
    };

    // 0=PENDING, 1=IN_PROGRESS, 2=RESOLVED
    private static final String[] STATUS_BY_STEP = {"PENDING", "IN_PROGRESS", "RESOLVED"};

    private static final Pattern ID_P = Pattern.compile("^(\\D*)(\\d+)$");

    /**
     * ReportItem คือคลาสข้อมูลรายการแจ้งซ่อมหรือร้องเรียนของผู้เช่า
     * id คือ รหัสรายการแจ้งซ่อม
     * date คือ วันที่แจ้งซ่อม
     * category คือ หมวดหมู่การแจ้งซ่อม
     * title คือ หัวข้อเรื่อง
     * detail คือ รายละเอียดปัญหา
     * appointment คือ ข้อมูลเวลานัดหมายหรือหมายเหตุ
     * step คือ ลำดับขั้นตอนสถานะการดำเนินการ (0=ส่งเรื่อง, 1=ดำเนินการ, 2=เสร็จสิ้น)
     */
    public static class ReportItem {
        // ===== representation =====
        // Abstraction Function:
        // AF(id, date, category, title, detail, appointment, step) คือ ตัวแทนของข้อมูลคำร้องแจ้งซ่อมหนึ่งรายการ
        // RI
        // 1. id, date, category, title ต้องไม่เป็น null
        // 2. step ต้องมีค่าระหว่าง 0 ถึง 2

        public String id;
        public String date;
        public String category;
        public String title;
        public String detail;
        public String appointment;
        public int step; // 0=รับเรื่อง, 1=ดำเนินการ, 2=เสร็จสิ้น

        /**
         * คอนสตรักเตอร์สำหรับสร้างข้อมูลคำร้องแจ้งซ่อมหนึ่งรายการ
         * 
         * @param id รหัสรายการแจ้งซ่อม
         * @param date วันที่แจ้งซ่อม
         * @param category หมวดหมู่ปัญหา
         * @param title หัวข้อเรื่อง
         * @param detail รายละเอียดปัญหา
         * @param appointment ข้อมูลนัดหมาย
         * @param step ลำดับสถานะ (0=ส่งเรื่อง, 1=ดำเนินการ, 2=เสร็จสิ้น)
         */
        public ReportItem(String id, String date, String category, String title,
                          String detail, String appointment, int step) {
            this.id = id;
            this.date = date;
            this.category = category;
            this.title = title;
            this.detail = detail;
            this.appointment = appointment;
            this.step = step;
        }

        /**
         * ดึงข้อความสถานะภาษาไทยตามลำดับขั้นตอน (Step)
         * 
         * @return ข้อความสถานะ เช่น "ส่งเรื่อง", "ดำเนินการ", "เสร็จสิ้น"
         */
        public String getStatusText() {
            return (step >= 0 && step < STEPS.length) ? STEPS[step] : "-";
        }
    }

    private final LoginLogic.User user;
    private final List<ReportItem> reportList = new ArrayList<>();

    /**
     * คอนสตรักเตอร์สำหรับสร้างตรรกะหน้าต่างแจ้งซ่อมของผู้เช่า และโหลดข้อมูลแจ้งซ่อม
     * 
     * @param user ข้อมูลผู้เช่าที่กำลังเข้าสู่ระบบ
     */
    public TenantReportLogic(LoginLogic.User user) {
        this.user = user;
        reload();
    }

    // ===================== ผู้ใช้ / ห้อง =====================

    /**
     * ดึงข้อมูลผู้ใช้งานล่าสุดจากฐานข้อมูลหรือ Session
     * 
     * @return ข้อมูล User ล่าสุด
     */
    public LoginLogic.User getUser() {
        if (this.user != null && !DataStore.blank(this.user.username)) {
            LoginLogic.User found = LoginLogic.findUser(this.user.username);
            if (found != null) return found;
        }
        LoginLogic.User cur = LoginLogic.getCurrentUser();
        if (cur != null) return cur;
        return this.user;
    }

    /**
     * ดึงคีย์ระบุตัวตนของผู้ใช้ (userId หรือ username)
     * 
     * @return รหัสหรือชื่อผู้ใช้
     */
    private String userKey() {
        LoginLogic.User u = getUser();
        if (u == null) return "";
        return DataStore.blank(u.userId) ? u.username : u.userId;
    }

    /**
     * ดึงรหัสห้องพัก (roomId) ของผู้เช่าปัจจุบัน
     * 
     * @return รหัสห้องพัก หรือ "" หากไม่มีห้อง
     */
    public String roomId() {
        LoginLogic.User u = getUser();
        if (u == null) return "";
        if (!DataStore.blank(u.roomId) && !"-".equals(u.roomId.trim())) return u.roomId.trim();
        DataStore.Contract c = DataStore.contractFor(u);
        return (c != null && !DataStore.blank(c.roomId) && !"-".equals(c.roomId.trim())) ? c.roomId.trim() : "";
    }

    /**
     * ดึงข้อมูลห้องพักปัจจุบันของผู้เช่า
     * 
     * @return ออบเจ็กต์ Room ห้องปัจจุบัน หรือ null หากไม่พบ
     */
    public DataStore.Room currentRoom() {
        String rId = roomId();
        if (DataStore.blank(rId)) return null;
        DataStore.Room r = DataStore.room(rId);
        if (r == null) {
            r = DataStore.roomByNumber(rId);
        }
        return r;
    }

    /**
     * ตรวจสอบว่าผู้เช่ามีข้อมูลห้องพักหรือไม่
     * 
     * @return true หากผู้เช่ามีห้องพักผูกอยู่
     */
    public boolean hasRoom() {
        return currentRoom() != null;
    }

    // ===================== อ่านจากไฟล์ =====================

    /**
     * โหลดรายการแจ้งซ่อมของผู้ใช้และห้องพักปัจจุบันจากไฟล์ complaints.csv ใหม่ (ไม่รวมห้องเดิมที่ย้ายออกแล้ว)
     */
    public void reload() {
        reportList.clear();
        String currentUserId = userKey();
        DataStore.Room cur = currentRoom();
        if (cur == null) return;

        String curId = cur.id;
        String curNum = cur.number;

        // อ่านข้อมูลทุกแถวจาก COMPLAINTS_FILE (complaints.csv)
        for (Map<String, String> row : DataStore.readTable(DataStore.COMPLAINTS_FILE)) {
            String cId = row.getOrDefault("complaintId", "").trim();
            String uId = row.getOrDefault("userId", "").trim();
            String rId = row.getOrDefault("roomId", "").trim();
            String cat = row.getOrDefault("category", "").trim();
            String topic = row.getOrDefault("topic", "").trim();
            String desc = row.getOrDefault("description", "").trim();
            String reqDate = row.getOrDefault("requestDate", "").trim();
            String status = row.getOrDefault("status", "").trim();

            // กรองเงื่อนไข: ตรวจสอบว่าเป็นของผู้ใช้คนนี้ และเป็นของห้องพักปัจจุบันเท่านั้น (ไม่รวมห้องเก่า)
            boolean isUserMatch = !DataStore.blank(currentUserId) && currentUserId.equalsIgnoreCase(uId);
            boolean isRoomMatch = rId.equalsIgnoreCase(curId) || rId.equalsIgnoreCase(curNum);

            if (isUserMatch && isRoomMatch) {
                String appointment = displayDate(reqDate);

                reportList.add(new ReportItem(cId, appointment, cat, topic,
                        desc, appointment, stepOf(status)));
            }
        }
    }

    /**
     * แปลงสถานะข้อความภาษาอังกฤษเป็นลำดับขั้นตอนตัวเลข (Step)
     * 
     * @param status ข้อความสถานะ เช่น "RESOLVED", "IN_PROGRESS", "PENDING"
     * @return ลำดับตัวเลข 0, 1 หรือ 2
     */
    private static int stepOf(String status) {
        if ("RESOLVED".equalsIgnoreCase(status)) return 2;
        if ("IN_PROGRESS".equalsIgnoreCase(status)) return 1;
        return 0;
    }

    /**
     * แปลงสตริงวันที่เป็นรูปแบบ dd/MM/yyyy สำหรับแสดงผล
     * 
     * @param isoOrFormatted ข้อความวันที่ต้นทาง
     * @return ข้อความวันที่ในรูปแบบ dd/MM/yyyy
     */
    private static String displayDate(String isoOrFormatted) {
        if (DataStore.blank(isoOrFormatted)) return "-";
        try {
            return LocalDate.parse(isoOrFormatted.trim()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoOrFormatted.trim();
        }
    }

    /**
     * ดึงรายการคำร้องแจ้งซ่อมทั้งหมดของผู้เช่า
     * 
     * @return รายการ ReportItem
     */
    public List<ReportItem> getReportList() {
        return reportList;
    }

    /**
     * ดึงรายการคำร้องแจ้งซ่อมตามลำดับดัชนีที่ระบุ
     * 
     * @param index ดัชนีในรายการ
     * @return ออบเจ็กต์ ReportItem หรือ null
     */
    public ReportItem getReportAt(int index) {
        return (index >= 0 && index < reportList.size()) ? reportList.get(index) : null;
    }

    /**
     * แปลงรายการแจ้งซ่อมเป็นตารางข้อมูล Object[][] เพื่อนำไปผูกกับ JTable
     * 
     * @return ข้อมูลตาราง 2 มิติ
     */
    public Object[][] getTableData() {
        Object[][] data = new Object[reportList.size()][6];
        for (int i = 0; i < reportList.size(); i++) {
            ReportItem it = reportList.get(i);
            data[i] = new Object[]{it.id, it.date, it.category, it.title, it.appointment, it.getStatusText()};
        }
        return data;
    }

    // ===================== เขียนลงไฟล์ =====================

    /**
     * คำนวณและสร้างรหัสคำร้องแจ้งซ่อมถัดไป (เช่น C001, C002)
     * 
     * @return รหัสคำร้องแจ้งซ่อมใหม่
     */
    private static String nextComplaintId() {
        String prefix = "C";
        int digits = 3, max = 0;
        boolean seen = false;
        for (Map<String, String> m : DataStore.readTable(DataStore.COMPLAINTS_FILE)) {
            Matcher mt = ID_P.matcher(m.getOrDefault("complaintId", "").trim());
            if (!mt.matches()) continue;
            if (!seen) {
                prefix = mt.group(1);
                digits = mt.group(2).length();
                seen = true;
            }
            if (mt.group(1).equals(prefix)) {
                max = Math.max(max, Integer.parseInt(mt.group(2)));
            }
        }
        return prefix + String.format("%0" + digits + "d", max + 1);
    }

    /**
     * บันทึกคำร้องแจ้งซ่อมใหม่ลงไฟล์ complaints.csv
     * 
     * @param category หมวดหมู่การแจ้งซ่อม
     * @param title หัวข้อเรื่อง
     * @param detail รายละเอียดปัญหา
     * @param date วันที่นัดหมาย
     * @param timeSlot ช่วงเวลาที่สะดวก
     * @return ออบเจ็กต์ ReportItem ที่บันทึกสำเร็จ หรือ null
     * @throws IOException หากเกิดข้อผิดพลาดในการเขียนไฟล์
     */
    public synchronized ReportItem addNewReport(String category, String title, String detail,
                                                Date date, String timeSlot) throws IOException {
        String appointDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
        String slot = timeSlot.split(" ")[0];
        String requestDate = appointDate + " " + slot;

        String description = detail.replace(",", " ").trim();
        String id = nextComplaintId();

        DataStore.Room cur = currentRoom();
        String saveRoomId = cur != null ? cur.id : roomId();

        DataStore.appendRow(DataStore.COMPLAINTS_FILE, DataStore.COMPLAINTS_HEADER,
                id, userKey(), saveRoomId, category, title, description, requestDate, "PENDING");

        reload();
        for (ReportItem it : reportList) {
            if (it.id.equals(id)) return it;
        }
        return null;
    }

    /**
     * ผู้เช่ายืนยันว่าการซ่อมเสร็จสิ้นแล้ว (ปรับสถานะจาก IN_PROGRESS เป็น RESOLVED)
     * 
     * @param index ดัชนีของรายการที่ต้องการยืนยัน
     * @return true หากปรับปรุงสถานะสำเร็จ
     */
    public boolean confirmResolved(int index) {
        ReportItem item = getReportAt(index);
        if (item == null || item.step != 1) return false;
        boolean ok = DataStore.updateCell(DataStore.COMPLAINTS_FILE, "complaintId", item.id,
                "status", STATUS_BY_STEP[2]);
        if (ok) reload();
        return ok;
    }

    // ===================== ตัวช่วยฟอร์ม =====================

    /**
     * สร้างรายการตัวเลือกช่วงเวลานัดหมาย เช่น "09:00 - 10:00 น."
     * 
     * @param startHour ชั่วโมงเริ่มต้น
     * @param endHour ชั่วโมงสิ้นสุด
     * @return อาร์เรย์ของข้อความช่วงเวลา
     */
    public static String[] buildTimeSlots(int startHour, int endHour) {
        String[] slots = new String[endHour - startHour];
        for (int h = startHour; h < endHour; h++) {
            slots[h - startHour] = String.format("%02d:00 - %02d:00 น.", h, h + 1);
        }
        return slots;
    }

    /**
     * ดึงวันพรุ่งนี้สำหรับใช้เป็นค่าตั้งต้นของการนัดหมาย
     * 
     * @return ออบเจ็กต์ Date ของวันพรุ่งนี้
     */
    public Date getTomorrowDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /**
     * ดึงวันที่ปัจจุบันเวลา 00:00 น. สำหรับใช้เป็นวันเริ่มต้นที่เลือกได้
     * 
     * @return ออบเจ็กต์ Date ของวันปัจจุบัน
     */
    public Date getMinSelectableDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}