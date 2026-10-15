package ui;
import logic.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * TenantMyRoomUI คือคลาสหน้าจอแสดงข้อมูลห้องพักของผู้เช่าปัจจุบัน
 * logic คือ อ็อบเจกต์ตรรกะสำหรับดึงข้อมูลรายละเอียดห้องพัก
 * user คือ ข้อมูลผู้ใช้งานปัจจุบันที่เข้าสู่ระบบ
 */
public class TenantMyRoomUI extends JPanel {

    // ===== representation =====
    // Abstraction Function:
    // AF(logic, user) คือ หน้าจอแสดงข้อมูลหมายเลขห้อง ประเภทห้อง อัตราค่าเช่า และรายการสิ่งอำนวยความสะดวกประจำห้อง
    // RI
    // 1. logic ต้องไม่เป็น null

    private final TenantMyRoomLogic logic;
    private final LoginLogic.User user;

    /**
     * คอนสตรัคเตอร์สำหรับสร้างหน้าจอแสดงข้อมูลห้องพักของผู้เช่าปัจจุบัน
     * 
     * @param user ข้อมูลผู้ใช้งานที่เข้าสู่ระบบ
     */
    public TenantMyRoomUI(LoginLogic.User user) {
        this.user = user;
        this.logic = new TenantMyRoomLogic(user);
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        if (!logic.hasRoom()) {
            content.add(messageCard(logic.emptyMessage()));
        } else {
            content.add(buildMainCard());
            content.add(Box.createVerticalStrut(18));
            content.add(buildFacilityCard());
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * สร้างการ์ดแสดงข้อความแจ้งเตือน (กรณีที่ผู้ใช้ยังไม่มีห้องพัก)
     * 
     * @param msg ข้อความแจ้งเตือน
     * @return JPanel แผงข้อความแจ้งเตือน
     */
    private JPanel messageCard(String msg) {
        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 20, 40, 20));
        card.setMaximumSize(new Dimension(860, 140));
        card.add(TenantUI.label(msg, 16, Font.BOLD, TenantUI.TEXT_MUTED));
        return card;
    }

    /**
     * สร้างการ์ดหลักแสดงรูปภาพห้องพัก หมายเลขห้อง ประเภท ชั้น และอัตราค่าเช่า
     * 
     * @return JPanel การ์ดข้อมูลห้องพัก
     */
    private JPanel buildMainCard() {
        TenantUI.RoundedPanel mainCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        mainCard.setLayout(new BorderLayout(25, 0));
        mainCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        mainCard.setMaximumSize(new Dimension(860, 210));

        // ดึง roomId จาก user ส่งไปสร้าง RoomImage
        String roomId = (user != null) ? user.roomId : null;
        TenantUI.RoomImage roomImg = new TenantUI.RoomImage(roomId);
        roomImg.setPreferredSize(new Dimension(260, 160));
        mainCard.add(roomImg, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel title = TenantUI.label(logic.title(), 24, Font.BOLD, TenantUI.TEXT);

        TenantUI.Pill pill = new TenantUI.Pill(logic.typeFull(), TenantUI.TEAL_LIGHT, TenantUI.TEAL_BORDER, TenantUI.TEAL, 12);
        pill.setPreferredSize(new Dimension(190, 25));
        pill.setMaximumSize(new Dimension(190, 25));

        JLabel floor = TenantUI.label(logic.locationText(), 13, Font.PLAIN, TenantUI.TEXT_MUTED);
        JLabel rent = TenantUI.label(logic.rentText(), 15, Font.BOLD, TenantUI.TEAL);

        info.add(title);
        info.add(Box.createVerticalStrut(6));
        info.add(pill);
        info.add(Box.createVerticalStrut(8));
        info.add(floor);
        info.add(Box.createVerticalStrut(6));
        info.add(rent);

        mainCard.add(info, BorderLayout.CENTER);
        return mainCard;
    }

    /**
     * สร้างการ์ดแสดงรายการสิ่งอำนวยความสะดวกและเฟอร์นิเจอร์ประจำห้อง
     * 
     * @return JPanel การ์ดสิ่งอำนวยความสะดวก
     */
    private JPanel buildFacilityCard() {
        TenantUI.RoundedPanel facCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        facCard.setLayout(new BorderLayout());
        facCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        facCard.setMaximumSize(new Dimension(860, 240));

        facCard.add(TenantUI.label("รายการเฟอร์นิเจอร์และอุปกรณ์ประจำห้อง", 16, Font.BOLD, TenantUI.TEXT),
                BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 2, 20, 10));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(12, 0, 5, 0));

        for (String item : logic.facilities()) {
            grid.add(TenantUI.label(item, 13, Font.PLAIN, TenantUI.TEXT));
        }

        facCard.add(grid, BorderLayout.CENTER);
        return facCard;
    }
}