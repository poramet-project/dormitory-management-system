package logic;

import java.util.ArrayList;
import java.util.List;

public class AdminManageProblemLogic {

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
        list.add(new ProblemModel("C004", "อนันต์ มุ่งมั่น", "086-111-2233", "201", "12 ต.ค. 2569", "กำลังดำเนินการ", "เครื่องปรับอากาศ", "-", "แอร์เสีย"));
        list.add(new ProblemModel("C003", "กิตติพงษ์ รักสงบ", "089-876-5432", "101", "9 ต.ค. 2569", "กำลังดำเนินการ", "เครื่องปรับอากาศ", "-", "แอร์เสีย"));
        list.add(new ProblemModel("C002", "กิตติพงษ์ รักสงบ", "089-876-5432", "101", "9 ต.ค. 2569", "กำลังดำเนินการ", "ระบบประปา", "ท่อน้ำทิ้งตรงพื้นห้องน้ำตันครับ เวลาอาบน้ำแล้วน้ำไม่ยอมลง เอ่อท่วมขึ้นมาเกือบล้นห้องเลยครับ", "ท่อน้ำตัน"));
        list.add(new ProblemModel("C001", "กิตติพงษ์ รักสงบ", "089-876-5432", "101", "9 ต.ค. 2569", "กำลังดำเนินการ", "เครื่องปรับอากาศ / พัดลม", "เปิด 16 องศายังไม่เย็นเลยครับ", "แอร์ไม่เย็น"));
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
}