package logic;


import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Logic ของหน้าแจ้งซ่อม/ร้องเรียน (ไม่มี Swing) */
public class ReportLogic {

    public enum Status { EMPTY_TOPIC, NO_USER, SUCCESS }

    public static class Result {
        public final Status status;
        public final String complaintId; // มีค่าเมื่อ SUCCESS

        Result(Status status, String complaintId) {
            this.status = status;
            this.complaintId = complaintId;
        }
    }

    public static final String[] CATEGORIES = {
            "ระบบประปา / สุขาภิบาล",
            "ระบบไฟฟ้า / แสงสว่าง",
            "เครื่องปรับอากาศ / พัดลม",
            "เฟอร์นิเจอร์ / ประตูหน้าต่าง"
    };

    private final LogicLogin.User currentUser;

    public ReportLogic(LogicLogin.User user) {
        this.currentUser = user;
    }

    /** แถวข้อมูลประวัติสำหรับแสดงในตาราง */
    public List<Object[]> historyRows() {
        List<Object[]> rows = new ArrayList<>();
        if (currentUser == null) return rows;

        List<DataStore.Complaint> list = DataStore.complaintsForUser(currentUser.userId);
        for (DataStore.Complaint c : list) {
            rows.add(new Object[]{
                    c.id,
                    DataStore.thaiDate(c.date),
                    c.category.split(" ")[0],
                    c.topic,
                    c.statusThai()
            });
        }
        return rows;
    }

    /** ตรวจสอบและบันทึกเรื่องใหม่ลง complaints.csv */
    public Result submit(String category, String topic, String desc) throws IOException {
        if (topic.isEmpty()) {
            return new Result(Status.EMPTY_TOPIC, null);
        }
        if (currentUser == null || DataStore.blank(currentUser.userId)) {
            return new Result(Status.NO_USER, null);
        }

        String complaintId = DataStore.nextId(DataStore.COMPLAINTS_FILE, "complaintId", "C", 3);
        String todayIso = LocalDate.now().toString();

        DataStore.appendRow(
                DataStore.COMPLAINTS_FILE,
                DataStore.COMPLAINTS_HEADER,
                complaintId,
                currentUser.userId,
                DataStore.blank(currentUser.roomId) ? "-" : currentUser.roomId,
                category,
                topic,
                desc,
                todayIso,
                "PENDING"
        );
        return new Result(Status.SUCCESS, complaintId);
    }
}
