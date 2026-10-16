package ui;

import logic.GuestLogic;
import logic.ImageLoader;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * GuestUI คือคลาสหน้าต่างหลักสำหรับผู้เข้าชมทั่วไปในการค้นหาและดูรายละเอียดห้องพัก
 * logic คือ อ็อบเจกต์ตรรกะสำหรับการค้นหาและกรองห้องพักของผู้เข้าชม
 * cbType คือ คอมโบ้บ็อกซ์สำหรับเลือกประเภทห้องพักที่ต้องการค้นหา
 * cbPrice คือ คอมโบ้บ็อกซ์สำหรับเลือกระดับราคาค่าเช่า
 * btnSearch คือ ปุ่มกดเพื่อดำเนินการค้นหาห้องพักตามเงื่อนไข
 * grid คือ พาเนลแบบกริดสำหรับแสดงการ์ดห้องพักที่ค้นพบ
 * scroll คือ ตัวเลื่อนดูรายการการ์ดห้องพัก
 * cardLayout คือ ตัวจัดการสลับมุมมองระหว่างหน้ารายการห้องพักและหน้ารายละเอียด
 * cardsContainer คือ พาเนลหลักที่บรรจุมุมมองต่าง ๆ
 * detailCardWrapper คือ พาเนลสำหรับแสดงคอมโพเนนต์รายละเอียดห้องพัก
 */
public class GuestUI extends JFrame {

    // ===== representation =====
    // Abstraction Function:
    // AF(logic, cardsContainer, cardLayout, grid) คือ หน้าต่างค้นหาห้องพักสำหรับบุคคลทั่วไปและผู้เข้าชม
    // RI
    // 1. logic ต้องไม่เป็น null
    // 2. cardsContainer และ cardLayout ต้องไม่เป็น null

    public static final Color PRIMARY = new Color(13, 148, 136); // Teal 600
    public static final Color PRIMARY_HOVER = new Color(15, 118, 110); // Teal 700
    public static final Color BG = new Color(248, 250, 252); // Slate 50
    public static final Color WHITE = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240); // Slate 200
    public static final Color TEXT = new Color(15, 23, 42); // Slate 900
    public static final Color TEXT_GRAY = new Color(100, 116, 139); // Slate 500
    public static final Color TEXT_LIGHT = new Color(148, 163, 184); // Slate 400

    private final GuestLogic logic;
    private JComboBox<String> cbType, cbPrice;
    private JButton btnSearch;
    private JPanel grid;
    private JScrollPane scroll;

    private final CardLayout cardLayout;
    private final JPanel cardsContainer;
    private final JPanel detailCardWrapper;

    /**
     * คอนสตรัคเตอร์สำหรับสร้างหน้าต่างค้นหาและดูรายละเอียดห้องพักสำหรับผู้เข้าชมทั่วไป
     */
    public GuestUI() {
        this.logic = new GuestLogic();

        setTitle("KU Dormitory - ค้นหาห้องพัก");
        setSize(1100, 730);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);

        cardsContainer.add(createListPanel(), "LIST");

        detailCardWrapper = new JPanel(new BorderLayout());
        cardsContainer.add(detailCardWrapper, "DETAIL");

        setContentPane(cardsContainer);
        setVisible(true);
    }

    /**
     * สร้างหน้าจอแสดงรายการห้องพัก ประกอบด้วยแถบนำทาง แผงตัวกรอง และตารางการ์ดห้องพัก
     * 
     * @return JPanel แผงหน้าจอรายการห้องพัก
     */
    private JPanel createListPanel() {
        JPanel root = new JPanel(null);
        root.setBackground(BG);

        JLabel title = label("ค้นหาห้องพักว่าง", Font.BOLD, 24, TEXT);
        title.setBounds(40, 72, 500, 32);
        root.add(title);

        JLabel sub = label("เลือกห้องพักที่เหมาะกับไลฟ์สไตล์ของคุณ พร้อมเข้าอยู่วันนี้", Font.PLAIN, 13, TEXT_GRAY);
        sub.setBounds(40, 108, 500, 20);
        root.add(sub);

        grid = new JPanel(null);
        grid.setBackground(BG);
        loadRoomCards();

        scroll = new JScrollPane(grid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getVerticalScrollBar().setUI(new ThinScrollBarUI());
        scroll.setBounds(40, 252, 1004 + 16, 424);
        root.add(scroll);

        JPanel filterCard = createFilterCard();
        root.add(filterCard);

        JPanel nav = createNavbar();
        root.add(nav);

        root.setComponentZOrder(nav, 0);
        root.setComponentZOrder(filterCard, 1);
        root.setComponentZOrder(scroll, 2);

        return root;
    }

    /**
     * เปลี่ยนมุมมองไปยังหน้ารายละเอียดของห้องพักที่เลือก
     * 
     * @param room ข้อมูลห้องพักที่ต้องการดูรายละเอียด
     */
    public void showDetailView(GuestLogic.RoomViewModel room) {
        detailCardWrapper.removeAll();
        GuestRoomUI detailUI = new GuestRoomUI(this, room.numberText, room.typeShort, room.priceText, room.statusLabel);
        detailCardWrapper.add(detailUI, BorderLayout.CENTER);
        detailCardWrapper.revalidate();
        detailCardWrapper.repaint();

        setTitle("KU Dormitory - รายละเอียด " + room.numberText);
        cardLayout.show(cardsContainer, "DETAIL");
    }

    /**
     * สลับมุมมองกลับมายังหน้ารายการค้นหาห้องพักและรีเฟรชข้อมูลการ์ดห้องพัก
     */
    public void showListView() {
        loadRoomCards();
        setTitle("KU Dormitory - ค้นหาห้องพัก");
        cardLayout.show(cardsContainer, "LIST");
    }

    /**
     * โหลดและสร้างการ์ดห้องพักทั้งหมดตามเงื่อนไขการกรองปัจจุบัน แล้วนำไปวางบนกริด
     */
    private void loadRoomCards() {
        grid.removeAll();
        List<GuestLogic.RoomViewModel> rooms = logic.getFilteredRooms();

        int cardW = 235, cardH = 328, gap = 18, cols = 4;
        int rowsCount = Math.max(1, (rooms.size() + cols - 1) / cols);

        grid.setPreferredSize(new Dimension(cols * cardW + (cols - 1) * gap,
                rowsCount * cardH + (rowsCount - 1) * gap + 40));

        for (int i = 0; i < rooms.size(); i++) {
            int x = (i % cols) * (cardW + gap);
            int y = (i / cols) * (cardH + gap);
            grid.add(createRoomCard(rooms.get(i), x, y));
        }
        grid.revalidate();
        grid.repaint();
    }

    /**
     * สร้างแถบนำทางส่วนหัว (Navbar) พร้อมโลโก้ และปุ่มเข้าสู่ระบบหรือออกจากระบบ
     * 
     * @return JPanel แถบนำทางด้านบน
     */
    private JPanel createNavbar() {
        JPanel nav = new JPanel(null);
        nav.setBackground(WHITE);
        nav.setBounds(0, 0, 1100, 56);
        nav.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        JPanel logo = new ShadowCardPanel(PRIMARY, null, 8);
        logo.setLayout(new GridBagLayout());
        logo.setBounds(40, 15, 28, 26);
        logo.add(label("KU", Font.BOLD, 11, WHITE));
        nav.add(logo);

        JLabel brand = label("KU Dormitory", Font.BOLD, 16, TEXT);
        brand.setBounds(76, 15, 140, 26);
        nav.add(brand);

        if (logic.isLoggedIn()) {
            JLabel lblName = label(logic.getCurrentDisplayName(), Font.BOLD, 13, TEXT);
            lblName.setHorizontalAlignment(SwingConstants.RIGHT);
            lblName.setBounds(650, 15, 280, 26);
            nav.add(lblName);

            JButton btnLogout = flatButton("Logout", false);
            btnLogout.setBounds(940, 12, 120, 32);
            btnLogout.setForeground(new Color(220, 38, 38));
            btnLogout.addActionListener(e -> {
                logic.performLogout();
                LoginUI login = new LoginUI();
                login.setLocation(getLocation());
                login.setVisible(true);
                dispose();
            });
            nav.add(btnLogout);
        } else {
            JButton btnLogin = flatButton("Login", true);
            btnLogin.setBounds(940, 12, 120, 32);
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

    /**
     * สร้างการ์ดแผงตัวกรองค้นหาตามประเภทห้องพักและช่วงราคา
     * 
     * @return JPanel แผงตัวกรอง
     */
    private JPanel createFilterCard() {
        ShadowCardPanel card = new ShadowCardPanel(WHITE, BORDER, 12);
        card.setLayout(null);
        card.setBounds(40, 140, 1004, 96);

        card.add(fieldLabel("ประเภทห้องพัก", 20, 14));
        cbType = combo(logic.getTypeOptions());
        cbType.setBounds(20, 38, 430, 40);
        card.add(cbType);

        card.add(fieldLabel("ช่วงราคาต่อเดือน", 470, 14));
        cbPrice = combo(logic.getPriceOptions());
        cbPrice.setBounds(470, 38, 400, 40);
        card.add(cbPrice);

        btnSearch = flatButton("ค้นหา", true);
        btnSearch.setBounds(886, 38, 98, 40);
        btnSearch.addActionListener(e -> {
            logic.setTypeFilter((String) cbType.getSelectedItem());
            logic.setPriceFilter((String) cbPrice.getSelectedItem());
            loadRoomCards();
        });
        card.add(btnSearch);

        return card;
    }

    /**
     * สร้างการ์ดแสดงข้อมูลห้องพักเดี่ยว พร้อมรูปภาพ ประเภท ราคา และเอฟเฟกต์การเด้งเมื่อเมาส์ชี้
     * 
     * @param room ข้อมูลวิวโมเดลห้องพัก
     * @param x ตำแหน่ง X
     * @param y ตำแหน่ง Y
     * @return JPanel การ์ดห้องพัก
     */
    private JPanel createRoomCard(GuestLogic.RoomViewModel room, int x, int y) {
        int w = 235, h = 328;
        ShadowCardPanel card = new ShadowCardPanel(WHITE, BORDER, 12);
        card.setLayout(null);
        card.setBounds(x, y, w, h);

        String roomNumOnly = room.numberText.replace("ห้อง ", "");

        // 🟢 ค้นหาไฟล์รูปภาพเฉพาะห้อง เช่น img/room_101.png หรือ .jpg หากไม่มีจะแสดงกรอบว่างๆ
        RoomImage img = new RoomImage(new String[] { "img/room_" + roomNumOnly + ".png", "img/room_" + roomNumOnly + ".jpg" }, w - 16, 132);
        img.setBounds(8, 8, w - 16, 132);
        card.add(img);

        JLabel name = label(room.numberText, Font.BOLD, 17, TEXT);
        name.setBounds(16, 150, 100, 24);
        card.add(name);

        JPanel badge = dotStatusBadge(room.statusLabel);
        badge.setLocation(w - 16 - badge.getWidth(), 152);
        card.add(badge);

        JLabel type = label(room.typeShort, Font.BOLD, 12, PRIMARY);
        type.setBounds(16, 180, w - 32, 18);
        card.add(type);

        JLabel floor = label(room.floorText, Font.PLAIN, 11, TEXT_GRAY);
        floor.setBounds(16, 200, w - 32, 16);
        card.add(floor);

        JLabel price = label(room.priceText, Font.BOLD, 19, TEXT);
        int pw = price.getFontMetrics(price.getFont()).stringWidth(room.priceText);
        price.setBounds(16, 226, pw + 4, 26);
        card.add(price);

        JLabel perMonth = label("/ เดือน", Font.PLAIN, 11, TEXT_LIGHT);
        perMonth.setBounds(16 + pw + 6, 232, 60, 18);
        card.add(perMonth);

        JButton btn = flatButton("ดูรายละเอียด", false);
        btn.setBounds(16, 270, w - 32, 38);
        card.add(btn);

        btn.addActionListener(e -> showDetailView(room));

        // Animation Spring Bounce
        final Timer[] animTimer = new Timer[1];
        final float[] targetOffsetY = { 0f };
        final float[] currentOffsetY = { 0f };

        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setHovered(true);
                targetOffsetY[0] = -7f;

                if (animTimer[0] != null && animTimer[0].isRunning())
                    animTimer[0].stop();

                animTimer[0] = new Timer(16, evt -> {
                    float diff = targetOffsetY[0] - currentOffsetY[0];
                    currentOffsetY[0] += diff * 0.28f;

                    card.setBounds(x, (int) (y + currentOffsetY[0]), w, h);
                    card.repaint();

                    if (Math.abs(diff) < 0.08f) {
                        currentOffsetY[0] = targetOffsetY[0];
                        card.setBounds(x, (int) (y + currentOffsetY[0]), w, h);
                        ((Timer) evt.getSource()).stop();
                    }
                });
                animTimer[0].start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point p = e.getPoint();
                if (p.x >= 0 && p.x < card.getWidth() && p.y >= 0 && p.y < card.getHeight()) {
                    return;
                }

                card.setHovered(false);
                targetOffsetY[0] = 0f;

                if (animTimer[0] != null && animTimer[0].isRunning())
                    animTimer[0].stop();

                animTimer[0] = new Timer(16, evt -> {
                    float diff = targetOffsetY[0] - currentOffsetY[0];
                    currentOffsetY[0] += diff * 0.28f;

                    card.setBounds(x, (int) (y + currentOffsetY[0]), w, h);
                    card.repaint();

                    if (Math.abs(diff) < 0.08f) {
                        currentOffsetY[0] = targetOffsetY[0];
                        card.setBounds(x, y, w, h);
                        ((Timer) evt.getSource()).stop();
                    }
                });
                animTimer[0].start();
            }
        };

        card.addMouseListener(hoverAdapter);
        for (Component comp : card.getComponents()) {
            comp.addMouseListener(hoverAdapter);
        }

        return card;
    }

    /**
     * สร้างป้ายสถานะห้องพักพร้อมจุดสีระบุความพร้อม (เขียว=ว่าง, แดง=มีผู้เช่า, ส้ม=ปรับปรุง)
     * 
     * @param status ข้อความสถานะห้องพัก
     * @return JPanel ป้ายสถานะ
     */
    private static JPanel dotStatusBadge(String status) {
        boolean isAvailable = status.contains("ว่าง");
        boolean isOccupied = status.contains("มีผู้เช่า");
        boolean isMaintenance = status.contains("ปรับปรุง");

        Color bg = isAvailable ? new Color(240, 253, 244)
                : (isOccupied ? new Color(254, 242, 242)
                : (isMaintenance ? new Color(254, 243, 199) : new Color(254, 252, 232)));
        Color fg = isAvailable ? new Color(22, 101, 52)
                : (isOccupied ? new Color(153, 27, 27)
                : (isMaintenance ? new Color(180, 83, 9) : new Color(133, 77, 14)));
        Color dotColor = isAvailable ? new Color(34, 197, 94)
                : (isOccupied ? new Color(239, 68, 68)
                : (isMaintenance ? new Color(217, 119, 6) : new Color(234, 179, 8)));

        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);

        JLabel dot = label("•", Font.BOLD, 14, dotColor);
        JLabel lbl = label(status, Font.BOLD, 10, fg);
        badge.add(dot);
        badge.add(lbl);
        badge.setSize(lbl.getFontMetrics(lbl.getFont()).stringWidth(status) + 26, 22);
        return badge;
    }

    /**
     * สร้างป้ายข้อความกำกับช่องกรอกข้อมูล
     * 
     * @param text ข้อความกำกับ
     * @param x ตำแหน่ง X
     * @param y ตำแหน่ง Y
     * @return JLabel ป้ายข้อความ
     */
    private static JLabel fieldLabel(String text, int x, int y) {
        JLabel l = label(text, Font.BOLD, 12, TEXT_GRAY);
        l.setBounds(x, y, 200, 18);
        return l;
    }

    /**
     * สร้างป้ายข้อความพร้อมกำหนดรูปแบบและสี
     * 
     * @param text ข้อความ
     * @param style รูปแบบฟอนต์ (Font.PLAIN, Font.BOLD)
     * @param size ขนาดตัวอักษร
     * @param color สีข้อความ
     * @return JLabel ป้ายข้อความ
     */
    public static JLabel label(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text);
        Font baseFont = UIManager.getFont("Label.font");
        if (baseFont != null) {
            l.setFont(baseFont.deriveFont(style, (float) size));
        } else {
            l.setFont(new Font("Tahoma", style, size));
        }
        l.setForeground(color);
        return l;
    }

    /**
     * สร้างคอมโบ้บ็อกซ์สำหรับเลือกรายการ
     * 
     * @param items รายการตัวเลือก
     * @return JComboBox คอมโบ้บ็อกซ์ที่จัดรูปแบบแล้ว
     */
    private static JComboBox<String> combo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        Font baseFont = UIManager.getFont("ComboBox.font");
        if (baseFont != null) {
            cb.setFont(baseFont.deriveFont(Font.PLAIN, 14f));
        } else {
            cb.setFont(new Font("Tahoma", Font.PLAIN, 14));
        }
        cb.setForeground(TEXT);
        cb.setFocusable(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cb.setBackground(WHITE);
        return cb;
    }

    public static class ShadowCardPanel extends JPanel {
        private final Color bgColor;
        private final Color borderColor;
        private final int radius;
        private boolean isHovered = false;

        /**
         * สร้างแผงการ์ดพร้อมเงาและมุมโค้งมน
         * 
         * @param bgColor สีพื้นหลัง
         * @param borderColor สีเส้นขอบ
         * @param radius รัศมีความโค้งมนของมุม
         */
        public ShadowCardPanel(Color bgColor, Color borderColor, int radius) {
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            this.radius = radius;
            setOpaque(false);
        }

        /**
         * กำหนดสถานะการชี้เมาส์เหนือการ์ดเพื่อสลับระดับเงาและสีขอบ
         * 
         * @param hovered ค่าความจริงว่าเมาส์ชี้อยู่หรือไม่
         */
        public void setHovered(boolean hovered) {
            this.isHovered = hovered;
        }

        /**
         * วาดเงาการ์ด พื้นหลัง และเส้นขอบขอบมน
         * 
         * @param g กราฟิกส์คอนเท็กซ์
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            if (isHovered) {
                g2.setColor(new Color(13, 148, 136, 22));
                g2.fillRoundRect(2, 6, w - 4, h - 5, radius + 4, radius + 4);
                g2.setColor(new Color(15, 23, 42, 18));
                g2.fillRoundRect(1, 3, w - 2, h - 3, radius + 2, radius + 2);
            } else {
                g2.setColor(new Color(15, 23, 42, 6));
                g2.fillRoundRect(2, 4, w - 4, h - 5, radius + 2, radius + 2);
                g2.setColor(new Color(15, 23, 42, 10));
                g2.fillRoundRect(1, 2, w - 2, h - 3, radius, radius);
            }

            if (bgColor != null) {
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, w - 2, h - 4, radius, radius);
            }

            if (borderColor != null) {
                g2.setColor(isHovered ? PRIMARY : borderColor);
                g2.setStroke(new BasicStroke(isHovered ? 1.5f : 1.0f));
                g2.drawRoundRect(0, 0, w - 2, h - 4, radius, radius);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * สร้างปุ่มกดสไตล์โมเดิร์นแบนราบ (Flat Button)
     * 
     * @param text ข้อความบนปุ่ม
     * @param isPrimary กำหนดว่าเป็นปุ่มหลักหรือปุ่มรอง
     * @return JButton ปุ่มที่จัดรูปแบบแล้ว
     */
    public static JButton flatButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text);
        Font baseFont = UIManager.getFont("Button.font");
        if (baseFont != null) {
            btn.setFont(baseFont.deriveFont(Font.BOLD, 12f));
        } else {
            btn.setFont(new Font("Tahoma", Font.BOLD, 12));
        }
        btn.setForeground(isPrimary ? WHITE : TEXT);
        btn.setBackground(isPrimary ? PRIMARY : new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(isPrimary ? PRIMARY_HOVER : new Color(226, 232, 240));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(isPrimary ? PRIMARY : new Color(241, 245, 249));
                }
            }
        });
        return btn;
    }

    /**
     * ThinScrollBarUI คือคลาสกำหนดการแสดงผลแถบเลื่อน Scrollbar ให้มีความบางและโมเดิร์น
     */
    static class ThinScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ คอมโพเนนต์กำหนดรูปแบบการแสดงผลของแถบเลื่อน
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * กำหนดชุดสีของตัวเลื่อนและรางเลื่อน
         */
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(203, 213, 225);
            trackColor = BG;
        }

        /**
         * สร้างปุ่มลูกศรเลื่อนขึ้นแบบซ่อน
         * 
         * @param o ทิศทาง
         * @return JButton ปุ่มขนาด 0x0
         */
        @Override
        protected JButton createDecreaseButton(int o) {
            return zeroButton();
        }

        /**
         * สร้างปุ่มลูกศรเลื่อนลงแบบซ่อน
         * 
         * @param o ทิศทาง
         * @return JButton ปุ่มขนาด 0x0
         */
        @Override
        protected JButton createIncreaseButton(int o) {
            return zeroButton();
        }

        /**
         * คืนค่าปุ่มขนาด 0 เพื่อซ่อนปุ่มหัวท้ายของแถบเลื่อน
         * 
         * @return JButton ปุ่มว่างเปล่า
         */
        private JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }

    /**
     * RoomImage คือคอมโพเนนต์สำหรับแสดงรูปภาพห้องพักพร้อมการจัดกรอบขอบมน
     * photo คือ อ็อบเจกต์รูปภาพห้องพักที่โหลดมาแสดงผล
     */
    static class RoomImage extends JPanel {
        // ===== representation =====
        // Abstraction Function:
        // AF(photo) คือ พาเนลแสดงภาพห้องพัก หรือแสดงกรอบว่างพร้อมข้อความหากไม่มีรูปภาพ
        // RI
        // - photo อาจเป็น null ได้หากไม่มีไฟล์รูปภาพ

        private final Image photo;

        /**
         * สร้างคอมโพเนนต์แสดงรูปภาพห้องพัก
         * 
         * @param paths รายชื่อพาธไฟล์รูปภาพที่ต้องการค้นหาและโหลด
         * @param width ความกว้าง
         * @param height ความสูง
         */
        RoomImage(String[] paths, int width, int height) {
            this.photo = ImageLoader.load(paths);
            setOpaque(false);
            setPreferredSize(new Dimension(width, height));
            setSize(width, height);
        }

        /**
         * วาดภาพห้องพักขอบมน หรือแสดงกรอบประพร้อมไอคอนเมื่อไม่มีรูป
         * 
         * @param g กราฟิกส์คอนเท็กซ์
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth(), h = getHeight();

            if (w > 0 && h > 0) {
                g2.clipRect(0, 0, w, h);
                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));

                if (photo != null) {
                    g2.drawImage(photo, 0, 0, w, h, this);
                } else {
                    // 🟢 ทำเป็นกรอบว่างๆ สำหรับห้องที่ไม่มีรูปภาพ
                    g2.setColor(new Color(248, 250, 252));
                    g2.fillRect(0, 0, w, h);

                    // วาดเส้นขอบประแสดงกรอบว่าง
                    float[] dash = { 6.0f, 4.0f };
                    g2.setStroke(
                            new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                    g2.setColor(new Color(203, 213, 225));
                    g2.drawRoundRect(1, 1, w - 3, h - 3, 10, 10);

                    // วาดไอคอนกล้อง
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.setColor(new Color(148, 163, 184));
                    int cx = w / 2, cy = h / 2 - 8;
                    g2.drawRoundRect(cx - 14, cy - 10, 28, 20, 4, 4);
                    g2.drawOval(cx - 5, cy - 5, 10, 10);

                    // ข้อความระบุ
                    Font baseFont = UIManager.getFont("Label.font");
                    if (baseFont != null) {
                        g2.setFont(baseFont.deriveFont(Font.PLAIN, 11f));
                    }
                    String msg = "ไม่มีรูปภาพห้องพัก";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, cy + 24);
                }
            }
            g2.dispose();
        }
    }
}