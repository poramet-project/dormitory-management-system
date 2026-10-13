package logic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TenantReportLogic {

    public static final String[] STEPS = {"รับเรื่อง", "ดำเนินการ", "เสร็จสิ้น"};
    public static final String[] CATEGORIES = {
            "ระบบประปา", "ระบบไฟฟ้า", "เครื่องปรับอากาศ", "เฟอร์นิเจอร์", "อินเทอร์เน็ต", "อื่น ๆ"
    };

    public static class ReportItem {
        public String id;
        public String date;
        public String category;
        public String title;
        public String detail;
        public String appointment;
        public boolean allowEnter;
        public int step; // 0=รับเรื่อง, 1=ดำเนินการ, 2=เสร็จสิ้น

        public ReportItem(String id, String date, String category, String title, 
                          String detail, String appointment, boolean allowEnter, int step) {
            this.id = id;
            this.date = date;
            this.category = category;
            this.title = title;
            this.detail = detail;
            this.appointment = appointment;
            this.allowEnter = allowEnter;
            this.step = step;
        }

        public String getStatusText() {
            if (step >= 0 && step < STEPS.length) {
                return STEPS[step];
            }
            return "-";
        }
    }

    private final List<ReportItem> reportList = new ArrayList<>();

    public TenantReportLogic() {
        initSampleData();
    }

    private void initSampleData() {
        reportList.add(new ReportItem("R-001", "02/10/2026", "ระบบประปา", "ก๊อกน้ำอ่างล้างหน้ารั่ว", 
                "น้ำหยดตลอดเวลา", "05/10/26 10:00", true, 2));
        reportList.add(new ReportItem("R-002", "08/10/2026", "ระบบไฟฟ้า", "ไฟห้องน้ำไม่ติด", 
                "หลอดไฟกระพริบแล้วดับไป", "12/10/26 14:00", false, 1));
        reportList.add(new ReportItem("R-003", "10/10/2026", "เฟอร์นิเจอร์", "บานพับตู้เสื้อผ้าหลุด", 
                "น็อตหลุดปิดประตูไม่ได้", "14/10/26 09:00", true, 0));
    }

    public static String[] buildTimeSlots(int startHour, int endHour) {
        String[] slots = new String[endHour - startHour];
        for (int h = startHour; h < endHour; h++) {
            slots[h - startHour] = String.format("%02d:00 - %02d:00 น.", h, h + 1);
        }
        return slots;
    }

    public List<ReportItem> getReportList() {
        return reportList;
    }

    public ReportItem getReportAt(int index) {
        if (index >= 0 && index < reportList.size()) {
            return reportList.get(index);
        }
        return null;
    }

    public Object[][] getTableData() {
        Object[][] data = new Object[reportList.size()][6];
        for (int i = 0; i < reportList.size(); i++) {
            ReportItem item = reportList.get(i);
            data[i] = new Object[]{
                    item.id,
                    item.date,
                    item.category,
                    item.title,
                    item.appointment,
                    item.getStatusText()
            };
        }
        return data;
    }

    public ReportItem addNewReport(String category, String title, String detail, Date date, String timeSlot, boolean allowEnter) {
        String id = String.format("R-%03d", reportList.size() + 1);
        String today = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String appointDate = new SimpleDateFormat("dd/MM/yy").format(date);
        String slot = timeSlot.split(" ")[0];
        String appointment = appointDate + " " + slot;

        ReportItem newItem = new ReportItem(id, today, category, title, detail, appointment, allowEnter, 0);
        reportList.add(newItem);
        return newItem;
    }

    public boolean advanceStep(int index) {
        if (index >= 0 && index < reportList.size()) {
            ReportItem item = reportList.get(index);
            if (item.step < STEPS.length - 1) {
                item.step++;
                return true;
            }
        }
        return false;
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