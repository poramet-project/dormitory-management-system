package logic;

import java.util.ArrayList;
import java.util.List;

public class AdminManageBookingLogic {

    public static class BookingModel {
        public String id, name, phone, room, date, status, type, detail;

        public BookingModel(String id, String name, String phone, String room, String date, String status, String type, String detail) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.room = room;
            this.date = date;
            this.status = status;
            this.type = type;
            this.detail = detail;
        }
    }

    public List<BookingModel> getAllBookings() {
        List<BookingModel> list = new ArrayList<>();
        list.add(new BookingModel("BK001", "Jame Bond", "012-345-6789", "103", "12 ต.ค. 2569", "รอพิจารณา", "จองห้อง",
                "ห้อง 103 • ห้องพัดลม • 2,800 บาท/เดือน"));
        return list;
    }

    public Object[][] getTableRows() {
        List<BookingModel> list = getAllBookings();
        Object[][] rows = new Object[list.size()][5];
        for (int i = 0; i < list.size(); i++) {
            BookingModel b = list.get(i);
            rows[i] = new Object[]{b.id, b.name, b.room, b.date, b.status};
        }
        return rows;
    }
}