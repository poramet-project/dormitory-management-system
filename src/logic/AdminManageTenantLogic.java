package logic;

import java.util.ArrayList;
import java.util.List;

public class AdminManageTenantLogic {

    public static class TenantModel {
        public String name;
        public String room;
        public String startDate;
        public String endDate;
        public String status;
        public String contractId;
        public String phone;
        public String roomType;
        public String rent;
        public String deposit;

        public TenantModel(String name, String room, String startDate, String endDate, String status,
                           String contractId, String phone, String roomType, String rent, String deposit) {
            this.name = name;
            this.room = room;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.contractId = contractId;
            this.phone = phone;
            this.roomType = roomType;
            this.rent = rent;
            this.deposit = deposit;
        }
    }

    public List<TenantModel> getAllTenants() {
        List<TenantModel> list = new ArrayList<>();
        list.add(new TenantModel("กิตติพงษ์ รักสงบ", "101", "1 มิ.ย. 2569", "31 พ.ค. 2570", "ใช้งานอยู่",
                "C001", "089-876-5432", "ห้อง 101 • ห้องพัดลม", "2,800 บาท", "5,600 บาท"));
        list.add(new TenantModel("อนันต์ มุ่งมั่น", "102", "2 มิ.ย. 2569", "1 ก.ค. 2570", "ใช้งานอยู่",
                "C002", "086-111-2233", "ห้อง 102 • ห้องแอร์", "3,500 บาท", "7,000 บาท"));
        return list;
    }

    public Object[][] getTableRows() {
        List<TenantModel> list = getAllTenants();
        Object[][] rows = new Object[list.size()][6];
        for (int i = 0; i < list.size(); i++) {
            TenantModel t = list.get(i);
            rows[i] = new Object[]{t.name, t.room, t.startDate, t.endDate, t.status, ""};
        }
        return rows;
    }
}