package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * 
 * AdminReportLogic
 */
public class AdminReportLogic {

    public static class ProblemModel {
        public String id, name, phone, room, date, status, category, desc, topic;

        public ProblemModel(String id, String name, String phone, String room, String date, String status, String category, String desc, String topic) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.room = room;
            this.date = date;
            this.status = status;
            this.category = category;
            this.desc = desc;
            this.topic = topic;
        }
    }

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
                               ("RESOLVED".equalsIgnoreCase(statusRaw) ? "เสร็จสิ้น" : "กำลังดำเนินการ");

            list.add(new ProblemModel(id, name, phone, roomNum, DataStore.thaiDate(dateRaw), statusThai, category, desc, topic));
        }
        return list;
    }

    public Object[][] getTableRows() {
        List<ProblemModel> list = getAllProblems();
        Object[][] rows = new Object[list.size()][6];
        for (int i = 0; i < list.size(); i++) {
            ProblemModel p = list.get(i);
            rows[i] = new Object[]{p.id, p.name, p.room, p.topic, p.date, p.status};
        }
        return rows;
    }

    public boolean acceptProblem(String complaintId) {
        return DataStore.updateCell(DataStore.COMPLAINTS_FILE, "complaintId", complaintId, "status", "IN_PROGRESS");
    }
}