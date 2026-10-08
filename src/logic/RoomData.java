package logic;


/** ข้อมูลห้องพักสำหรับหน้า Guest (เดิมเป็นคลาสซ้อนใน GuestUI) */
public class RoomData {
    public final String id, floor, type, status, rent, occupant;

    public RoomData(String id, String floor, String type, String status, String rent, String occupant) {
        this.id = id;
        this.floor = floor;
        this.type = type;
        this.status = status;
        this.rent = rent;
        this.occupant = occupant;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }
}
