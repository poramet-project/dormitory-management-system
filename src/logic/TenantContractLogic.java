package logic;



/**
 * TenantContractLogic คือคลาสสำหรับจัดการตรรกะทางธุรกิจของข้อมูลสัญญาเช่าฝั่งผู้เช่า
 * user คือ ข้อมูลผู้เช่าปัจจุบันที่กำลังเข้าสู่ระบบ
 * contract คือ สัญญาเช่าที่ผูกกับผู้เช่าปัจจุบัน
 * room คือ ข้อมูลห้องพักที่ระบุตามสัญญาเช่า
 */
public class TenantContractLogic {

    // ===== representation =====
    // Abstraction Function:
    // AF(user, contract, room) คือ ตัวแทนตรรกะสำหรับแสดงข้อมูลสรุปสัญญาเช่าและเงื่อนไขข้อตกลงแก่ผู้เช่า
    // RI
    // 1. user ต้องไม่เป็น null เพื่อระบุตัวตนของผู้เช่าในการดึงสัญญา
    // 2. หากมี contract ข้อมูล roomId ในสัญญาต้องสอดคล้องกับห้องพักในระบบ

    private final LoginLogic.User user;
    private final DataStore.Contract contract;
    private final DataStore.Room room;

    /**
     * คอนสตรักเตอร์สำหรับสร้างตรรกะข้อมูลสัญญาเช่าของผู้เช่า
     * 
     * @param user ข้อมูลผู้เช่าที่กำลังเข้าสู่ระบบ
     */
    public TenantContractLogic(LoginLogic.User user) {
        this.user = user;
        LoginLogic.User latest = (user != null && !DataStore.blank(user.username)) ? LoginLogic.findUser(user.username) : user;
        this.contract = DataStore.contractFor(latest != null ? latest : user);
        String roomId = (contract != null) ? contract.roomId : (latest == null ? "" : latest.roomId);
        DataStore.Room r = DataStore.room(roomId);
        if (r == null) r = DataStore.roomByNumber(roomId);
        this.room = r;
    }

    /**
     * คืนค่าข้อมูลสรุปสาระสำคัญของสัญญาเช่า ในรูปแบบตารางคู่ {หัวข้อ, ค่า}
     * 
     * @return อาร์เรย์ 2 มิติของคู่หัวข้อและค่าสรุปสัญญา เช่น เลขที่สัญญา สถานะ วันเริ่มต้น วันสิ้นสุด
     */
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

    /**
     * สร้างข้อความรายละเอียดสัญญาเช่าและข้อตกลงระเบียบหอพักแบบเต็ม
     * 
     * @return ข้อความรายละเอียดสัญญาเช่าและข้อตกลงหอพัก
     */
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
