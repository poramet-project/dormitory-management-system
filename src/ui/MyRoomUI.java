package ui;
import logic.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MyRoomUI extends JPanel {
    private final MyRoomLogic logic;

    public MyRoomUI(LogicLogin.User user) {
        this.logic = new MyRoomLogic(user);
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

    private JPanel messageCard(String msg) {
        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 20, 40, 20));
        card.setMaximumSize(new Dimension(860, 140));
        card.add(TenantUI.label(msg, 16, Font.BOLD, TenantUI.TEXT_MUTED));
        return card;
    }

    private JPanel buildMainCard() {
        TenantUI.RoundedPanel mainCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        mainCard.setLayout(new BorderLayout(25, 0));
        mainCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        mainCard.setMaximumSize(new Dimension(860, 210));

        TenantUI.RoomImage roomImg = new TenantUI.RoomImage();
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
