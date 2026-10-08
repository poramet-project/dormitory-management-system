package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TenantHome extends JPanel {
    public TenantHome(TenantUI mainUI, LogicLogin.User user) {
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(18, 25, 20, 25));

        // 1. การ์ดห้องพัก (อ่านจาก rooms.csv)
        content.add(createRoomCard(mainUI, user));
        content.add(Box.createVerticalStrut(18));

        // 2. การ์ดแจ้งซ่อม
        content.add(createRepairCard(mainUI));
        content.add(Box.createVerticalStrut(18));

        // 3. การ์ดสัญญาเช่า (อ่านจาก contracts.csv)
        content.add(createContractCard(mainUI, user));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createRoomCard(TenantUI mainUI, LogicLogin.User user) {
        DataStore.Room room = (user == null) ? null : DataStore.room(user.roomId);

        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setMaximumSize(new Dimension(860, 190));

        TenantUI.RoomImage img = new TenantUI.RoomImage();
        img.setPreferredSize(new Dimension(260, 150));
        card.add(img, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        String roomDisplay = (room != null) ? "ห้อง " + room.number : "ยังไม่มีห้องพัก";
        JLabel roomLbl = TenantUI.label(roomDisplay, 26, Font.BOLD, TenantUI.TEXT);

        String pillText = (room != null) ? room.typeShort() : "-";
        TenantUI.Pill typePill = new TenantUI.Pill(pillText, TenantUI.TEAL_LIGHT, TenantUI.TEAL_BORDER, TenantUI.TEAL, 12);
        typePill.setPreferredSize(new Dimension(90, 25));
        typePill.setMaximumSize(new Dimension(90, 25));

        String locText = (room != null)
                ? "อาคารหอพักนักศึกษา | ชั้น " + room.floor() + " | " + room.statusThai()
                : "ติดต่อผู้ดูแลหอพักเพื่อจองห้อง";
        JLabel loc = TenantUI.label(locText, 13, Font.PLAIN, TenantUI.TEXT_MUTED);
        loc.setIcon(new TenantUI.LineIcon(TenantUI.LineIcon.Type.PIN, 14, TenantUI.TEXT_MUTED));
        loc.setIconTextGap(6);

        TenantUI.RoundButton btn = new TenantUI.RoundButton("ดูรายละเอียด");
        btn.setPreferredSize(new Dimension(200, 36));
        btn.setMaximumSize(new Dimension(200, 36));
        btn.addActionListener(e -> mainUI.showPage("ROOM", 1));

        info.add(roomLbl);
        info.add(Box.createVerticalStrut(6));
        info.add(typePill);
        info.add(Box.createVerticalStrut(8));
        info.add(loc);
        info.add(Box.createVerticalStrut(14));
        info.add(btn);

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRepairCard(TenantUI mainUI) {
        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(860, 200));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        header.add(new TenantUI.CircleIcon(new TenantUI.LineIcon(TenantUI.LineIcon.Type.WRENCH, 26, TenantUI.TEAL), 46));
        header.add(TenantUI.label("แจ้งซ่อม/ร้องเรียน", 16, Font.BOLD, TenantUI.TEXT));
        card.add(header);
        card.add(Box.createVerticalStrut(10));

        TenantUI.RoundedPanel stat = new TenantUI.RoundedPanel(6, TenantUI.TEAL_LIGHT, null);
        stat.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        stat.setMaximumSize(new Dimension(820, 68));

        stat.add(new JLabel(new TenantUI.LineIcon(TenantUI.LineIcon.Type.RECEIPT, 36, TenantUI.TEAL)));
        stat.add(TenantUI.label("1 รายการ", 18, Font.BOLD, TenantUI.TEXT));

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 40));
        stat.add(sep);

        JLabel inProg = TenantUI.label("กำลังดำเนินการ 1", 14, Font.PLAIN, TenantUI.TEXT_MUTED);
        inProg.setIcon(TenantUI.dot(TenantUI.YELLOW, 8));
        stat.add(inProg);

        JLabel done = TenantUI.label("เสร็จสิ้น 0", 14, Font.PLAIN, TenantUI.TEXT_MUTED);
        done.setIcon(TenantUI.dot(TenantUI.GREEN, 8));
        stat.add(done);

        card.add(stat);
        card.add(Box.createVerticalStrut(10));

        TenantUI.RoundButton btn = new TenantUI.RoundButton("แจ้งซ่อม / ร้องเรียน");
        btn.setMaximumSize(new Dimension(820, 36));
        btn.addActionListener(e -> mainUI.showPage("REPORT", 3));
        card.add(btn);

        return card;
    }

    private JPanel createContractCard(TenantUI mainUI, LogicLogin.User user) {
        DataStore.Contract contract = DataStore.contractFor(user);

        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(860, 150));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        header.add(new TenantUI.CircleIcon(new TenantUI.LineIcon(TenantUI.LineIcon.Type.CALENDAR, 24, TenantUI.TEAL), 42));
        header.add(TenantUI.label("สัญญาเช่า", 16, Font.BOLD, TenantUI.TEXT));
        card.add(header);
        card.add(Box.createVerticalStrut(8));

        String endText = (contract != null)
                ? "สิ้นสุดสัญญา: " + DataStore.thaiDate(contract.end)
                : "ยังไม่มีข้อมูลสัญญาเช่า";
        JLabel end = TenantUI.label(endText, 14, Font.PLAIN, TenantUI.TEXT);
        end.setIcon(new TenantUI.LineIcon(TenantUI.LineIcon.Type.CALENDAR, 16, TenantUI.TEXT));
        end.setIconTextGap(8);
        card.add(end);
        card.add(Box.createVerticalStrut(10));

        TenantUI.RoundButton btn = new TenantUI.RoundButton("ดูสัญญาเช่า");
        btn.setMaximumSize(new Dimension(220, 34));
        btn.addActionListener(e -> mainUI.showPage("CONTRACT", 2));
        card.add(btn);

        return card;
    }
}