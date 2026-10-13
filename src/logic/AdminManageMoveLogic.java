package logic;

import java.util.ArrayList;
import java.util.List;

public class AdminManageMoveLogic {

    public static class MoveModel {
        public String id, name, phone, rooms, date, status, type, reason;

        public MoveModel(String id, String name, String phone, String rooms, String date, String status, String type, String reason) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.rooms = rooms;
            this.date = date;
            this.status = status;
            this.type = type;
            this.reason = reason;
        }
    }

    public List<MoveModel> getAllMoves() {
        List<MoveModel> list = new ArrayList<>();
        list.add(new MoveModel("T001", "กิตติพงษ์ รักสงบ", "089-876-5432", "101 > 103", "9 ต.ค. 2569", "รอพิจารณา", "ย้ายห้อง",
                "อยากเปลี่ยนบรรยากาศ"));
        return list;
    }

    public Object[][] getTableRows() {
        List<MoveModel> list = getAllMoves();
        Object[][] rows = new Object[list.size()][5];
        for (int i = 0; i < list.size(); i++) {
            MoveModel m = list.get(i);
            rows[i] = new Object[]{m.id, m.name, m.rooms, m.date, m.status};
        }
        return rows;
    }
}