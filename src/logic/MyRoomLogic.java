package logic;


/** Logic ของหน้าห้องของฉัน (ไม่มี Swing) */
public class MyRoomLogic {
    private final LogicLogin.User user;
    private final DataStore.Room room;
    private final DataStore.Contract contract;

    public MyRoomLogic(LogicLogin.User user) {
        this.user = user;
        this.room = (user == null) ? null : DataStore.room(user.roomId);
        this.contract = DataStore.contractFor(user);
    }

    public boolean hasRoom() {
        return room != null;
    }

    public String emptyMessage() {
        return (user == null || DataStore.blank(user.roomId))
                ? "คุณยังไม่มีห้องพัก"
                : "ไม่พบข้อมูลห้อง " + user.roomId + " ในไฟล์ rooms.csv";
    }

    public String title() {
        return "ห้องพักหมายเลข " + room.number;
    }

    public String typeFull() {
        return room.typeFull();
    }

    public String locationText() {
        return "อาคารหอพักนักศึกษา • ชั้น " + room.floor() + " • สถานะห้อง: " + room.statusThai();
    }

    public String rentText() {
        // ถ้ามีสัญญา ใช้ค่าเช่าตามสัญญา ไม่งั้นใช้ค่าเช่าพื้นฐานของห้อง
        int rentValue = (contract != null && contract.rent > 0) ? contract.rent : room.rent;
        return "อัตราค่าเช่า: " + DataStore.money(rentValue) + " บาท / เดือน";
    }

    /** รายการเฟอร์นิเจอร์ เรียงตามลำดับการแสดงผลในตาราง 3x2 */
    public String[] facilities() {
        String cooling = room.isAir()
                ? "❄️ เครื่องปรับอากาศ Inverter ประหยัดไฟ"
                : "🌀 พัดลมติดเพดาน";
        return new String[]{
                "🛏️ เตียงนอนเดี่ยว 3.5 ฟุต พร้อมฟูก (2 ชุด)",
                cooling,
                "🚪 ตู้เสื้อผ้าบิวท์อิน 2 บาน",
                "🚿 เครื่องทำน้ำอุ่นในห้องน้ำส่วนตัว",
                "🪑 โต๊ะทำงานและเก้าอี้ (2 ชุด)",
                "📶 Wi-Fi ความเร็วสูงฟรี"
        };
    }
}
