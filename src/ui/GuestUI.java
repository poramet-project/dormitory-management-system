package ui;

import logic.GuestLogic;
import logic.ImageLoader;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class GuestUI extends JFrame {

    static final Color PRIMARY    = new Color(14, 121, 115);
    static final Color BG         = new Color(248, 250, 252);
    static final Color WHITE      = Color.WHITE;
    static final Color BORDER     = new Color(226, 232, 240);
    static final Color TEXT       = new Color(23, 32, 53);
    static final Color TEXT_GRAY  = new Color(100, 116, 139);
    static final Color TEXT_LIGHT = Color.GRAY;

    private final GuestLogic logic;
    private JComboBox<String> cbType, cbPrice;
    private JButton btnSearch;
    private JPanel grid;
    private JScrollPane scroll;

    public GuestUI() {
        this.logic = new GuestLogic();

        setTitle("KU Dormitory - ค้นหาห้องพัก");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(null);
        root.setBackground(BG);
        setContentPane(root);

        root.add(createNavbar());

        JLabel title = label("ค้นหาห้องพักว่าง", Font.BOLD, 24, TEXT);
        title.setBounds(40, 76, 500, 36);
        root.add(title);

        JLabel sub = label("เลือกห้องพักที่เหมาะกับไลฟ์สไตล์ของคุณ พร้อมเข้าอยู่วันนี้", Font.PLAIN, 12, TEXT_GRAY);
        sub.setBounds(40, 114, 500, 18);
        root.add(sub);

        root.add(createFilterCard());

        grid = new JPanel(null);
        grid.setBackground(BG);
        loadRoomCards();

        scroll = new JScrollPane(grid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getVerticalScrollBar().setUI(new ThinScrollBarUI());
        scroll.setBounds(40, 250, 1004 + 16, 410);
        root.add(scroll);

        setVisible(true);
    }

    private void loadRoomCards() {
        grid.removeAll();
        List<GuestLogic.RoomViewModel> rooms = logic.getFilteredRooms();

        int cardW = 235, cardH = 308, gap = 18, cols = 4;
        int rowsCount = Math.max(1, (rooms.size() + cols - 1) / cols);
        grid.setPreferredSize(new Dimension(cols * cardW + (cols - 1) * gap,
                rowsCount * cardH + (rowsCount - 1) * gap + 24));

        for (int i = 0; i < rooms.size(); i++) {
            int x = (i % cols) * (cardW + gap);
            int y = (i / cols) * (cardH + gap);
            grid.add(createRoomCard(rooms.get(i), x, y));
        }
        grid.revalidate();
        grid.repaint();
    }

    private JPanel createNavbar() {
        JPanel nav = new JPanel(null);
        nav.setBackground(WHITE);
        nav.setBounds(0, 0, 1100, 56);
        nav.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        JPanel logo = new RoundedPanel(PRIMARY, null, 8);
        logo.setLayout(new GridBagLayout());
        logo.setBounds(40, 15, 26, 26);
        logo.add(label("KU", Font.BOLD, 11, WHITE));
        nav.add(logo);

        JLabel brand = label("KU Dormitory", Font.BOLD, 15, TEXT);
        brand.setBounds(74, 15, 140, 26);
        nav.add(brand);

        if (logic.isLoggedIn()) {
            JLabel lblName = label(logic.getCurrentDisplayName(), Font.BOLD, 13, TEXT);
            lblName.setHorizontalAlignment(SwingConstants.RIGHT);
            lblName.setBounds(700, 15, 250, 26);
            nav.add(lblName);

            JButton btnLogout = flatButton("ออกจากระบบ", false);
            btnLogout.setBounds(960, 12, 100, 32);
            btnLogout.addActionListener(e -> {
                logic.performLogout();
                LoginUI login = new LoginUI();
                login.setLocation(getLocation());
                login.setVisible(true);
                dispose();
            });
            nav.add(btnLogout);
        } else {
            JButton btnLogin = flatButton("เข้าสู่ระบบ", true);
            btnLogin.setBounds(960, 12, 100, 32);
            btnLogin.addActionListener(e -> {
                LoginUI login = new LoginUI();
                login.setLocation(getLocation());
                login.setVisible(true);
                dispose();
            });
            nav.add(btnLogin);
        }

        return nav;
    }

    private JPanel createFilterCard() {
        RoundedPanel card = new RoundedPanel(WHITE, BORDER, 12);
        card.setLayout(null);
        card.setBounds(40, 148, 1004, 86);

        card.add(fieldLabel("ประเภทห้องพัก", 16, 14));
        cbType = combo(logic.getTypeOptions());
        cbType.setBounds(16, 34, 450, 36);
        card.add(cbType);

        card.add(fieldLabel("ช่วงราคาต่อเดือน", 484, 14));
        cbPrice = combo(logic.getPriceOptions());
        cbPrice.setBounds(484, 34, 390, 36);
        card.add(cbPrice);

        btnSearch = flatButton("ค้นหา", true);
        btnSearch.setBounds(894, 34, 94, 36);
        btnSearch.addActionListener(e -> {
            logic.setTypeFilter((String) cbType.getSelectedItem());
            logic.setPriceFilter((String) cbPrice.getSelectedItem());
            loadRoomCards();
        });
        card.add(btnSearch);

        return card;
    }

    private JPanel createRoomCard(GuestLogic.RoomViewModel room, int x, int y) {
        int w = 235;
        RoundedPanel card = new RoundedPanel(WHITE, BORDER, 12);
        card.setLayout(null);
        card.setBounds(x, y, w, 308);

        String roomNumOnly = room.numberText.replace("ห้อง ", "");
        RoomImage img = new RoomImage("img/room_" + roomNumOnly + ".png", w - 2, 130);
        img.setBounds(1, 1, w - 2, 130);
        card.add(img);

        JLabel name = label(room.numberText, Font.BOLD, 16, TEXT);
        name.setBounds(16, 142, 100, 24);
        card.add(name);

        JPanel badge = statusBadge(room.statusLabel);
        badge.setLocation(w - 16 - badge.getWidth(), 145);
        card.add(badge);

        JLabel type = label(room.typeShort, Font.BOLD, 12, PRIMARY);
        type.setBounds(16, 172, w - 32, 18);
        card.add(type);

        JLabel floor = label(room.floorText, Font.PLAIN, 10, TEXT_GRAY);
        floor.setBounds(16, 190, w - 32, 16);
        card.add(floor);

        JLabel price = label(room.priceText, Font.BOLD, 18, TEXT);
        int pw = price.getFontMetrics(price.getFont()).stringWidth(room.priceText);
        price.setBounds(16, 214, pw + 4, 26);
        card.add(price);

        JLabel perMonth = label("/ เดือน", Font.PLAIN, 10, TEXT_LIGHT);
        perMonth.setBounds(16 + pw + 8, 220, 60, 18);
        card.add(perMonth);

        JButton btn = flatButton("ดูรายละเอียด", false);
        btn.setBounds(16, 256, w - 32, 36);
        card.add(btn);

        btn.addActionListener(e -> {
            GuestRoomUI detailUI = new GuestRoomUI(room.numberText, room.typeShort, room.priceText, room.statusLabel);
            detailUI.setLocation(getLocation());
            detailUI.setVisible(true);
            dispose();
        });

        return card;
    }

    private static JPanel statusBadge(String status) {
        Color bg = status.contains("ว่าง") ? new Color(209, 250, 229) : (status.contains("มีผู้เช่า") ? new Color(254, 226, 226) : new Color(254, 243, 199));
        Color fg = status.contains("ว่าง") ? new Color(4, 120, 87) : (status.contains("มีผู้เช่า") ? new Color(220, 38, 38) : new Color(180, 83, 9));

        JPanel badge = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        JLabel lbl = label(status, Font.BOLD, 10, fg);
        badge.add(lbl);
        badge.setSize(lbl.getFontMetrics(lbl.getFont()).stringWidth(status) + 16, 20);
        return badge;
    }

    private static JLabel fieldLabel(String text, int x, int y) {
        JLabel l = label(text, Font.PLAIN, 11, TEXT);
        l.setBounds(x, y, 200, 16);
        return l;
    }

    public static JLabel label(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Tahoma", style, size));
        l.setForeground(color);
        return l;
    }

    private static JComboBox<String> combo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(new Font("Tahoma", Font.PLAIN, 12));
        cb.setForeground(TEXT);
        cb.setFocusable(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cb.setBackground(WHITE);
        return cb;
    }

    public static class RoundedPanel extends JPanel {
        private final Color bgColor;
        private final Color borderColor;
        private final int radius;

        public RoundedPanel(Color bgColor, Color borderColor, int radius) {
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (bgColor != null) {
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);
            }
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JButton flatButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Tahoma", Font.BOLD, 12));
        btn.setForeground(isPrimary ? WHITE : TEXT);
        btn.setBackground(isPrimary ? PRIMARY : new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static class ThinScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(209, 213, 219);
            trackColor = BG;
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
        private JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }

    static class RoomImage extends JPanel {
        private final Image photo;

        RoomImage(String path, int width, int height) {
            this.photo = ImageLoader.load(path, "img/room_101.png");
            setOpaque(false);
            setPreferredSize(new Dimension(width, height));
            setSize(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            if (w > 0 && h > 0) {
                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
            }

            if (photo != null) {
                g2.drawImage(photo, 0, 0, w, h, null);
            } else {
                g2.setColor(new Color(245, 240, 232));
                g2.fillRect(0, 0, w, h);
            }
            g2.dispose();
        }
    }
}