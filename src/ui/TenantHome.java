package ui;

import logic.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * TenantHome คือคลาสหน้าจอหลัก (Home Dashboard) สำหรับผู้เช่า
 * logic คือ อ็อบเจกต์ตรรกะสำหรับดึงและประมวลผลข้อมูลในหน้าหลักของผู้เช่า
 * user คือ ข้อมูลผู้ใช้งานปัจจุบันที่เข้าสู่ระบบ
 * totalLbl คือ ป้ายแสดงสถิติการแจ้งซ่อมทั้งหมด
 * inProgLbl คือ ป้ายแสดงสถิติการแจ้งซ่อมที่กำลังดำเนินการ
 * doneLbl คือ ป้ายแสดงสถิติการแจ้งซ่อมที่เสร็จสิ้นแล้ว
 */
public class TenantHome extends JPanel {

    // ===== representation =====
    // Abstraction Function:
    // AF(logic, user, totalLbl, inProgLbl, doneLbl) คือ หน้าจอสรุปภาพรวมห้องพัก สถานะการแจ้งซ่อม และสัญญาเช่าของผู้เช่า
    // RI
    // 1. logic และ user ต้องไม่เป็น null

    private final TenantHomeLogic logic;
    private final LoginLogic.User user;
    private JLabel totalLbl, inProgLbl, doneLbl;
    private JLabel roomLbl, locLbl, conEndLbl;
    private TenantUI.Pill typePill;
    private TenantUI.RoomImage roomImg;

    /**
     * คอนสตรัคเตอร์สำหรับสร้างหน้าจอภาพรวมแดชบอร์ดของผู้เช่า (Home)
     * 
     * @param mainUI หน้าต่างหลัก TenantUI
     * @param user ข้อมูลผู้ใช้งานปัจจุบัน
     */
    public TenantHome(TenantUI mainUI, LoginLogic.User user) {
        this.user = user;
        this.logic = new TenantHomeLogic(user);
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(18, 25, 20, 25));

        content.add(createRoomCard(mainUI));
        content.add(Box.createVerticalStrut(18));
        content.add(createRepairCard(mainUI));
        content.add(Box.createVerticalStrut(18));
        content.add(createContractCard(mainUI));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * โหลดข้อมูลสถิติการแจ้งซ่อมและอัปเดตการแสดงผลล่าสุดบนหน้าจอ
     */
    public void refresh() {
        logic.reload();
        if (totalLbl != null)  totalLbl.setText(logic.repairTotalText());
        if (inProgLbl != null) inProgLbl.setText(logic.repairInProgressText());
        if (doneLbl != null)   doneLbl.setText(logic.repairDoneText());

        if (roomLbl != null)   roomLbl.setText(logic.roomDisplay());
        if (typePill != null)  typePill.setText(logic.pillText());
        if (locLbl != null)    locLbl.setText(logic.locationText());
        if (conEndLbl != null) conEndLbl.setText(logic.contractEndText());

        DataStore.Room cur = logic.getRoom();
        if (roomImg != null) {
            roomImg.setRoomId(cur != null ? cur.id : null);
        }

        revalidate();
        repaint();
    }

    /**
     * สร้างการ์ดแสดงข้อมูลห้องพักของผู้เช่า พร้อมรูปภาพ ประเภท และตำแหน่งชั้น
     * 
     * @param mainUI หน้าต่างหลัก TenantUI
     * @return JPanel การ์ดข้อมูลห้องพัก
     */
    private JPanel createRoomCard(TenantUI mainUI) {
        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setMaximumSize(new Dimension(860, 190));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        DataStore.Room cur = logic.getRoom();
        String roomId = (cur != null) ? cur.id : ((user != null) ? user.roomId : null);
        roomImg = new TenantUI.RoomImage(roomId);
        roomImg.setPreferredSize(new Dimension(260, 150));
        card.add(roomImg, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        roomLbl = TenantUI.label(logic.roomDisplay(), 26, Font.BOLD, TenantUI.TEXT);

        typePill = new TenantUI.Pill(logic.pillText(), TenantUI.TEAL_LIGHT, TenantUI.TEAL_BORDER, TenantUI.TEAL, 12);
        typePill.setPreferredSize(new Dimension(150, 25));
        typePill.setMaximumSize(new Dimension(150, 25));
        typePill.setAlignmentX(Component.LEFT_ALIGNMENT);

        locLbl = TenantUI.label(logic.locationText(), 13, Font.PLAIN, TenantUI.TEXT_MUTED);
        locLbl.setIcon(new TenantUI.LineIcon(TenantUI.LineIcon.Type.PIN, 14, TenantUI.TEXT_MUTED));
        locLbl.setIconTextGap(6);
        locLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        TenantUI.RoundButton btn = new TenantUI.RoundButton("ดูรายละเอียด");
        btn.setPreferredSize(new Dimension(200, 36));
        btn.setMaximumSize(new Dimension(200, 36));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> mainUI.showPage("ROOM", 1));

        roomLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(roomLbl);
        info.add(Box.createVerticalStrut(6));
        info.add(typePill);
        info.add(Box.createVerticalStrut(8));
        info.add(locLbl);
        info.add(Box.createVerticalStrut(14));
        info.add(btn);

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    /**
     * สร้างการ์ดแสดงสถานะและสถิติการแจ้งซ่อม/ร้องเรียน พร้อมปุ่มเปิดหน้าแจ้งซ่อม
     * 
     * @param mainUI หน้าต่างหลัก TenantUI
     * @return JPanel การ์ดแจ้งซ่อม
     */
    private JPanel createRepairCard(TenantUI mainUI) {
        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(860, 200));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(new TenantUI.CircleIcon(new TenantUI.LineIcon(TenantUI.LineIcon.Type.WRENCH, 26, TenantUI.TEAL), 46));
        header.add(TenantUI.label("แจ้งซ่อม/ร้องเรียน", 16, Font.BOLD, TenantUI.TEXT));
        card.add(header);
        card.add(Box.createVerticalStrut(10));

        TenantUI.RoundedPanel stat = new TenantUI.RoundedPanel(6, TenantUI.TEAL_LIGHT, null);
        stat.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        stat.setMaximumSize(new Dimension(820, 68));
        stat.setAlignmentX(Component.LEFT_ALIGNMENT);

        stat.add(new JLabel(new TenantUI.LineIcon(TenantUI.LineIcon.Type.RECEIPT, 36, TenantUI.TEAL)));
        totalLbl = TenantUI.label(logic.repairTotalText(), 18, Font.BOLD, TenantUI.TEXT);
        stat.add(totalLbl);

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 40));
        stat.add(sep);

        inProgLbl = TenantUI.label(logic.repairInProgressText(), 14, Font.PLAIN, TenantUI.TEXT_MUTED);
        inProgLbl.setIcon(TenantUI.dot(TenantUI.YELLOW, 8));
        stat.add(inProgLbl);

        doneLbl = TenantUI.label(logic.repairDoneText(), 14, Font.PLAIN, TenantUI.TEXT_MUTED);
        doneLbl.setIcon(TenantUI.dot(TenantUI.GREEN, 8));
        stat.add(doneLbl);

        card.add(stat);
        card.add(Box.createVerticalStrut(10));

        TenantUI.RoundButton btn = new TenantUI.RoundButton("แจ้งซ่อม / ร้องเรียน");
        btn.setMaximumSize(new Dimension(820, 36));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> mainUI.showPage("REPORT", 3)); // สลับไปหน้าแจ้งซ่อม
        card.add(btn);

        return card;
    }

    /**
     * สร้างการ์ดแสดงข้อมูลวันสิ้นสุดสัญญาเช่า พร้อมปุ่มเปิดดูสัญญาเช่า
     * 
     * @param mainUI หน้าต่างหลัก TenantUI
     * @return JPanel การ์ดสัญญาเช่า
     */
    private JPanel createContractCard(TenantUI mainUI) {
        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(860, 150));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(new TenantUI.CircleIcon(new TenantUI.LineIcon(TenantUI.LineIcon.Type.CALENDAR, 24, TenantUI.TEAL), 42));
        header.add(TenantUI.label("สัญญาเช่า", 16, Font.BOLD, TenantUI.TEXT));
        card.add(header);
        card.add(Box.createVerticalStrut(8));

        conEndLbl = TenantUI.label(logic.contractEndText(), 14, Font.PLAIN, TenantUI.TEXT);
        conEndLbl.setIcon(new TenantUI.LineIcon(TenantUI.LineIcon.Type.CALENDAR, 16, TenantUI.TEXT));
        conEndLbl.setIconTextGap(8);
        conEndLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(conEndLbl);
        card.add(Box.createVerticalStrut(10));

        TenantUI.RoundButton btn = new TenantUI.RoundButton("ดูสัญญาเช่า");
        btn.setMaximumSize(new Dimension(220, 34));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> mainUI.showPage("CONTRACT", 2));
        card.add(btn);

        return card;
    }
}