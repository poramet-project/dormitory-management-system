package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LeaseContractUI extends JFrame {
    public LeaseContractUI(LogicLogin.User user) {
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);
        setVisible(true);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        // แถบสรุปสัญญา
        TenantUI.RoundedPanel summary = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        summary.setLayout(new GridLayout(1, 4, 10, 0));
        summary.setBorder(new EmptyBorder(14, 18, 14, 18));
        summary.setMaximumSize(new Dimension(860, 75));

        String roomNo = (user.roomId == null || user.roomId.isEmpty()) ? "R101" : user.roomId;
        summary.add(createCell("เลขที่สัญญา", "CT-2569-" + roomNo));
        summary.add(createCell("สถานะสัญญา", "ใช้งานอยู่ (Active)"));
        summary.add(createCell("วันเริ่มสัญญา", "1 มิ.ย. 2569"));
        summary.add(createCell("วันสิ้นสุดสัญญา", "31 พ.ค. 2570"));
        content.add(summary);
        content.add(Box.createVerticalStrut(18));

        // เนื้อหาข้อกำหนดสัญญา
        TenantUI.RoundedPanel docCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        docCard.setLayout(new BorderLayout());
        docCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        docCard.setMaximumSize(new Dimension(860, 420));

        JLabel title = TenantUI.label("ข้อกำหนดและเงื่อนไขการพักอาศัย", 16, Font.BOLD, TenantUI.TEXT);
        docCard.add(title, BorderLayout.NORTH);

        JTextArea terms = new JTextArea();
        terms.setEditable(false);
        terms.setFont(TenantUI.font(Font.PLAIN, 13));
        terms.setLineWrap(true);
        terms.setWrapStyleWord(true);
        terms.setText(
                "ผู้เช่า: " + user.fullName + " (เบอร์โทรศัพท์: " + user.phone + ")\n" +
                "ห้องพัก: ห้อง " + roomNo + " อาคารหอพักนักศึกษา มหาวิทยาลัยเกษตรศาสตร์\n\n" +
                "1. อัตราค่าเช่าและเงินประกัน:\n" +
                "   • ค่าเช่าห้องพักเดือนละ 3,500 บาท กำหนดชำระทุกวันที่ 1 - 5 ของเดือน\n" +
                "   • เงินประกันความเสียหาย 7,000 บาท ได้รับคืนเต็มจำนวนหลังสิ้นสุดสัญญา\n\n" +
                "2. ระเบียบหอพัก:\n" +
                "   • หอพักเปิดเวลา 05:30 น. และปิดเวลา 23:00 น.\n" +
                "   • ห้ามนำสัตว์เลี้ยงเข้ามาในอาคารหอพักโดยเด็ดขาด\n" +
                "   • ห้ามส่งเสียงดังรบกวนผู้อื่นหลังเวลา 23:00 น.\n" +
                "   • ห้ามสูบบุหรี่ หรือดื่มสุราภายในห้องพัก\n\n" +
                "3. การบอกเลิกสัญญา: หากประสงค์จะย้ายออกต้องแจ้งล่วงหน้าไม่น้อยกว่า 30 วัน"
        );
        docCard.add(new JScrollPane(terms), BorderLayout.CENTER);
        content.add(docCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createCell(String t, String v) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        p.add(TenantUI.label(t, 12, Font.PLAIN, TenantUI.TEXT_MUTED));
        p.add(TenantUI.label(v, 14, Font.BOLD, TenantUI.TEAL));
        return p;
    }
}