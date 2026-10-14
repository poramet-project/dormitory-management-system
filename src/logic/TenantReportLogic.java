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

/** Logic หน้าแจ้งซ่อม/ร้องเรียนของผู้เช่า (อ่าน/เขียน complaints.csv) ไม่มี Swing */
public class TenantReportLogic {

    public static final String[] STEPS = {"ส่งเรื่อง", "ดำเนินการ", "เสร็จสิ้น"};
    public static final String[] CATEGORIES = {
            "ระบบประปา", "ระบบไฟฟ้า", "เครื่องปรับอากาศ", "เฟอร์นิเจอร์", "อินเทอร์เน็ต", "อื่น ๆ"
    };

    // 0=PENDING, 1=IN_PROGRESS, 2=RESOLVED
    private static final String[] STATUS_BY_STEP = {"PENDING", "IN_PROGRESS", "RESOLVED"};

    private static final Pattern ID_P = Pattern.compile("^(\\D*)(\\d+)$");

    public static class ReportItem {
        public String id;
        public String date;
        public String category;
        public String title;
        public String detail;
        public String appointment;
        public int step; // 0=รับเรื่อง, 1=ดำเนินการ, 2=เสร็จสิ้น

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

        public String getStatusText() {
            return (step >= 0 && step < STEPS.length) ? STEPS[step] : "-";
        }
    }

    private final LoginLogic.User user;
    private final List<ReportItem> reportList = new ArrayList<>();

    public TenantReportLogic(LoginLogic.User user) {
        this.user = user;
        reload();
    }

    // ===================== ผู้ใช้ / ห้อง =====================

    private String userKey() {
        if (user == null) return "";
        return DataStore.blank(user.userId) ? user.username : user.userId;
    }

    /** roomId ของผู้เช่า (จาก users.csv ก่อน ไม่มีก็ใช้จากสัญญา) */
    public String roomId() {
        if (user == null) return "";
        if (!DataStore.blank(user.roomId)) return user.roomId.trim();
        DataStore.Contract c = DataStore.contractFor(user);
        return c != null ? c.roomId : "";
    }

    public boolean hasRoom() {
        return !DataStore.blank(roomId());
    }

    // ===================== อ่านจากไฟล์ =====================

    /** โหลดรายการแจ้งซ่อมของผู้ใช้และห้องปัจจุบันจาก complaints.csv ใหม่ */
   /** โหลดรายการแจ้งซ่อมของผู้ใช้และห้องปัจจุบันจาก complaints.csv ใหม่ */
    public void reload() {
        reportList.clear();
        String currentUserId = userKey();
        String currentRoomId = roomId();

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

            // กรองเงื่อนไข: ตรวจสอบว่าเป็นของผู้ใช้คนนี้ และเป็นของห้องพักปัจจุบันเท่านั้น
            boolean isUserMatch = !DataStore.blank(currentUserId) && currentUserId.equalsIgnoreCase(uId);
            boolean isRoomMatch = !DataStore.blank(currentRoomId) && currentRoomId.equalsIgnoreCase(rId);

            if (isUserMatch && isRoomMatch) {
                String appointment = displayDate(reqDate);

                reportList.add(new ReportItem(cId, appointment, cat, topic,
                        desc, appointment, stepOf(status)));
            }
        }
    }
    private static int stepOf(String status) {
        if ("RESOLVED".equalsIgnoreCase(status)) return 2;
        if ("IN_PROGRESS".equalsIgnoreCase(status)) return 1;
        return 0;
    }

    private static String displayDate(String isoOrFormatted) {
        if (DataStore.blank(isoOrFormatted)) return "-";
        try {
            return LocalDate.parse(isoOrFormatted.trim()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoOrFormatted.trim();
        }
    }

    public List<ReportItem> getReportList() {
        return reportList;
    }

    public ReportItem getReportAt(int index) {
        return (index >= 0 && index < reportList.size()) ? reportList.get(index) : null;
    }

    public Object[][] getTableData() {
        Object[][] data = new Object[reportList.size()][6];
        for (int i = 0; i < reportList.size(); i++) {
            ReportItem it = reportList.get(i);
            data[i] = new Object[]{it.id, it.date, it.category, it.title, it.appointment, it.getStatusText()};
        }
        return data;
    }

    // ===================== เขียนลงไฟล์ =====================

    /** สร้าง ID ถัดไปโดยใช้ prefix/จำนวนหลักตามที่มีอยู่ในไฟล์ (ไม่มีให้ใช้ C001) */
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

    /** บันทึกเรื่องแจ้งซ่อมใหม่ลง complaints.csv (ผูกกับ User ID และ Room ID ปัจจุบัน) */
    public synchronized ReportItem addNewReport(String category, String title, String detail,
                                                Date date, String timeSlot) throws IOException {
        String appointDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
        String slot = timeSlot.split(" ")[0];
        String requestDate = appointDate + " " + slot;

        String description = detail.replace(",", " ").trim();
        String id = nextComplaintId();

        DataStore.appendRow(DataStore.COMPLAINTS_FILE, DataStore.COMPLAINTS_HEADER,
                id, userKey(), roomId(), category, title, description, requestDate, "PENDING");

        reload();
        for (ReportItem it : reportList) {
            if (it.id.equals(id)) return it;
        }
        return null;
    }

    /** ผู้เช่ายืนยันว่าซ่อมเสร็จ (ทำได้เมื่อ Admin รับเรื่องแล้วเท่านั้น: IN_PROGRESS → RESOLVED) */
    public boolean confirmResolved(int index) {
        ReportItem item = getReportAt(index);
        if (item == null || item.step != 1) return false;
        boolean ok = DataStore.updateCell(DataStore.COMPLAINTS_FILE, "complaintId", item.id,
                "status", STATUS_BY_STEP[2]);
        if (ok) reload();
        return ok;
    }

    // ===================== ตัวช่วยฟอร์ม =====================

    public static String[] buildTimeSlots(int startHour, int endHour) {
        String[] slots = new String[endHour - startHour];
        for (int h = startHour; h < endHour; h++) {
            slots[h - startHour] = String.format("%02d:00 - %02d:00 น.", h, h + 1);
        }
        return slots;
    }

    public Date getTomorrowDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public Date getMinSelectableDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}