package ui;

import logic.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TenantHome extends JPanel {
    private final TenantHomeLogic logic;
    private JLabel totalLbl, inProgLbl, doneLbl;

    public TenantHome(TenantUI mainUI, LogicLogin.User user) {
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

    /** อ่านจำนวนแจ้งซ่อมจาก complaints.csv ใหม่ แล้วอัปเดตตัวเลขบนการ์ด (เรียกทุกครั้งที่กลับมาหน้าหลัก) */
    public void refresh() {
        logic.reloadRepairStats();
        if (totalLbl != null)  totalLbl.setText(logic.repairTotalText());
        if (inProgLbl != null) inProgLbl.setText(logic.repairInProgressText());
        if (doneLbl != null)   doneLbl.setText(logic.repairDoneText());
        
        revalidate();
        repaint();
    }

    private JPanel createRoomCard(TenantUI mainUI) {
        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setMaximumSize(new Dimension(860, 190));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        TenantUI.RoomImage img = new TenantUI.RoomImage();
        img.setPreferredSize(new Dimension(260, 150));
        card.add(img, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel roomLbl = TenantUI.label(logic.roomDisplay(), 26, Font.BOLD, TenantUI.TEXT);

        TenantUI.Pill typePill = new TenantUI.Pill(logic.pillText(), TenantUI.TEAL_LIGHT, TenantUI.TEAL_BORDER, TenantUI.TEAL, 12);
        typePill.setPreferredSize(new Dimension(90, 25));
        typePill.setMaximumSize(new Dimension(90, 25));
        typePill.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel loc = TenantUI.label(logic.locationText(), 13, Font.PLAIN, TenantUI.TEXT_MUTED);
        loc.setIcon(new TenantUI.LineIcon(TenantUI.LineIcon.Type.PIN, 14, TenantUI.TEXT_MUTED));
        loc.setIconTextGap(6);
        loc.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        btn.addActionListener(e -> mainUI.showPage("REPORT", 3));
        card.add(btn);

        return card;
    }

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

        JLabel end = TenantUI.label(logic.contractEndText(), 14, Font.PLAIN, TenantUI.TEXT);
        end.setIcon(new TenantUI.LineIcon(TenantUI.LineIcon.Type.CALENDAR, 16, TenantUI.TEXT));
        end.setIconTextGap(8);
        end.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(end);
        card.add(Box.createVerticalStrut(10));

        TenantUI.RoundButton btn = new TenantUI.RoundButton("ดูสัญญาเช่า");
        btn.setMaximumSize(new Dimension(220, 34));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> mainUI.showPage("CONTRACT", 2));
        card.add(btn);

        return card;
    }
}