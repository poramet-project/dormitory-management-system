package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LeaseContractUI extends JPanel {
    public LeaseContractUI(LogicLogin.User user) {
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);

        // ดึงข้อมูลจริงจาก contracts.csv และ rooms.csv
        DataStore.Contract contract = DataStore.contractFor(user);
        String roomId = (contract != null) ? contract.roomId : (user == null ? "" : user.roomId);
        DataStore.Room room = DataStore.room(roomId);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        // แถบสรุปสัญญา
        TenantUI.RoundedPanel summary = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        summary.setLayout(new GridLayout(1, 4, 10, 0));
        summary.setBorder(new EmptyBorder(14, 18, 14, 18));
        summary.setMaximumSize(new Dimension(860, 75));

        if (contract != null) {
            summary.add(createCell("เลขที่สัญญา", contract.id));
            summary.add(createCell("สถานะสัญญา", contract.statusThai()));
            summary.add(createCell("วันเริ่มสัญญา", DataStore.thaiDate(contract.start)));
            summary.add(createCell("วันสิ้นสุดสัญญา", DataStore.thaiDate(contract.end)));
        } else {
            summary.add(createCell("เลขที่สัญญา", "-"));
            summary.add(createCell("สถานะสัญญา", "ยังไม่มีสัญญา"));
            summary.add(createCell("วันเริ่มสัญญา", "-"));
            summary.add(createCell("วันสิ้นสุดสัญญา", "-"));
        }
        content.add(summary);
        content.add(Box.createVerticalStrut(18));

        // เนื้อหาข้อกำหนดสัญญา
        TenantUI.RoundedPanel docCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        docCard.setLayout(new BorderLayout());
        docCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        docCard.setMaximumSize(new Dimension(860, 420));

        docCard.add(TenantUI.label("ข้อกำหนดและเงื่อนไขการพักอาศัย", 16, Font.BOLD, TenantUI.TEXT), BorderLayout.NORTH);

        JTextArea terms = new JTextArea();
        terms.setEditable(false);
        terms.setFont(TenantUI.font(Font.PLAIN, 13));
        terms.setLineWrap(true);
        terms.setWrapStyleWord(true);
        terms.setText(buildTerms(user, contract, room));
        terms.setCaretPosition(0);
        docCard.add(new JScrollPane(terms), BorderLayout.CENTER);
        content.add(docCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private String buildTerms(LogicLogin.User user, DataStore.Contract contract, DataStore.Room room) {
        String name = (user == null) ? "-" : TenantUI.orDefault(user.fullName, "-");
        String phone = (user == null) ? "-" : TenantUI.orDefault(user.phone, "-");
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

    private JPanel createCell(String t, String v) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        p.add(TenantUI.label(t, 12, Font.PLAIN, TenantUI.TEXT_MUTED));
        p.add(TenantUI.label(v, 14, Font.BOLD, TenantUI.TEAL));
        return p;
    }
}