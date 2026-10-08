package logic;



/** Logic ของหน้าสัญญาเช่า (ไม่มี Swing) */
public class LeaseContractLogic {
    private final LogicLogin.User user;
    private final DataStore.Contract contract;
    private final DataStore.Room room;

    public LeaseContractLogic(LogicLogin.User user) {
        this.user = user;
        this.contract = DataStore.contractFor(user);
        String roomId = (contract != null) ? contract.roomId : (user == null ? "" : user.roomId);
        this.room = DataStore.room(roomId);
    }

    /** คืนค่าแถบสรุปสัญญา เป็นคู่ {หัวข้อ, ค่า} */
    public String[][] summaryCells() {
        if (contract != null) {
            return new String[][]{
                    {"เลขที่สัญญา", contract.id},
                    {"สถานะสัญญา", contract.statusThai()},
                    {"วันเริ่มสัญญา", DataStore.thaiDate(contract.start)},
                    {"วันสิ้นสุดสัญญา", DataStore.thaiDate(contract.end)}
            };
        }
        return new String[][]{
                {"เลขที่สัญญา", "-"},
                {"สถานะสัญญา", "ยังไม่มีสัญญา"},
                {"วันเริ่มสัญญา", "-"},
                {"วันสิ้นสุดสัญญา", "-"}
        };
    }

    public String buildTerms() {
        String name = (user == null) ? "-" : TenantLogic.orDefault(user.fullName, "-");
        String phone = (user == null) ? "-" : TenantLogic.orDefault(user.phone, "-");
        String roomText = (room != null) ? "ห้อง " + room.number + " (" + room.typeFull() + ")" : "-";

        if (contract == null) {
            return "ผู้เช่า: " + name + " (เบอร์โทรศัพท์: " + phone + ")\n"
                    + "ห้องพัก: " + roomText + "\n\n"
                    + "ไม่พบข้อมูลสัญญาเช่าของคุณในไฟล์ contracts.csv\n"
                    + "กรุณาติดต่อผู้ดูแลหอพักเพื่อทำสัญญา";
        }

        return "ผู้เช่า: " + name + " (เบอร์โทรศัพท์: " + phone + ")\n"
                + "ห้องพัก: " + roomText + " อาคารหอพักนักศึกษา มหาวิทยาลัยเกษตรศาสตร์\n"
                + "ระยะเวลาสัญญา: " + DataStore.thaiDate(contract.start) + " ถึง " + DataStore.thaiDate(contract.end) + "\n\n"
                + "1. อัตราค่าเช่าและเงินประกัน:\n"
                + "   • ค่าเช่าห้องพักเดือนละ " + DataStore.money(contract.rent) + " บาท กำหนดชำระทุกวันที่ 1 - 5 ของเดือน\n"
                + "   • เงินประกันความเสียหาย " + DataStore.money(contract.deposit) + " บาท ได้รับคืนเต็มจำนวนหลังสิ้นสุดสัญญา\n\n"
                + "2. ระเบียบหอพัก:\n"
                + "   • หอพักเปิดเวลา 05:30 น. และปิดเวลา 23:00 น.\n"
                + "   • ห้ามนำสัตว์เลี้ยงเข้ามาในอาคารหอพักโดยเด็ดขาด\n"
                + "   • ห้ามส่งเสียงดังรบกวนผู้อื่นหลังเวลา 23:00 น.\n"
                + "   • ห้ามสูบบุหรี่ หรือดื่มสุราภายในห้องพัก\n\n"
                + "3. การบอกเลิกสัญญา: หากประสงค์จะย้ายออกต้องแจ้งล่วงหน้าไม่น้อยกว่า 30 วัน";
    }
}
