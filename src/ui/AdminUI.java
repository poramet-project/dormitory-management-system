package ui;

import logic.AdminLogic;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminUI คือคลาสหน้าต่างหลัก (Main Window) สำหรับส่วนของผู้ดูแลระบบหอพัก
 * cardLayout คือ ตัวจัดการเลย์เอาต์แบบ CardLayout สำหรับสลับการแสดงผลหน้าย่อย
 * centerCardsPanel คือ พาเนลหลักสำหรับบรรจุหน้าย่อยต่าง ๆ ของระบบ
 * headerTitleLabel คือ ป้ายแสดงชื่อเรื่องของหน้าปัจจุบันที่กำลังเปิดใช้งาน
 * menuItems คือ รายการปุ่มเมนูแถบด้านข้างสำหรับเลือกเปลี่ยนหน้า
 * dashboardPanel คือ พาเนลหน้าแดชบอร์ดสรุปภาพรวม
 * roomUI คือ พาเนลจัดการข้อมูลห้องพัก
 * tenantUI คือ พาเนลจัดการทะเบียนผู้เช่าและสัญญาเช่า
 * bookingUI คือ พาเนลพิจารณาคำขอจองห้องพัก
 * roomChangeUI คือ พาเนลพิจารณาคำขอย้ายห้องพัก
 * reportUI คือ พาเนลจัดการคำร้องเรียนและงานแจ้งซ่อม
 */
public class AdminUI extends JFrame {

    // ===== representation =====
    // Abstraction Function:
    // AF(cardLayout, centerCardsPanel, headerTitleLabel, menuItems, dashboardPanel, roomUI, tenantUI, bookingUI, roomChangeUI, reportUI)
    // คือ หน้าต่างหลักของผู้ดูแลระบบที่รวบรวมเมนูและหน้าการทำงานย่อยทั้งหมดของระบบหอพัก
    // RI
    // 1. centerCardsPanel และ cardLayout ต้องไม่เป็น null
    // 2. menuItems ต้องไม่เป็น null

    static final Color PRIMARY     = new Color(15, 118, 110);
    static final Color PRIMARY_BG  = new Color(204, 251, 241);
    static final Color BG          = new Color(243, 245, 248);
    static final Color WHITE       = Color.WHITE;
    static final Color BORDER      = new Color(229, 231, 235);
    static final Color TEXT        = new Color(17, 24, 39);
    static final Color TEXT_GRAY   = new Color(107, 114, 128);
    static final Color TEXT_LIGHT  = new Color(156, 163, 175);
    static final Color GREEN       = new Color(16, 185, 129);
    static final Color RED         = new Color(220, 38, 38);
    static final Color ITEM_BG     = new Color(249, 250, 251);

    static final String FONT_NAME = "Tahoma";

    private final CardLayout cardLayout;
    private final JPanel centerCardsPanel;
    private final JLabel headerTitleLabel;
    private final List<MenuItem> menuItems = new ArrayList<>();

    // เก็บอ้างอิงของแต่ละหน้าย่อยเพื่อเรียกสั่ง Refresh ข้อมูลเวลาสลับแท็บ
    private JPanel dashboardPanel;
    private AdminRoomUI roomUI;
    private AdminTenantUI tenantUI;
    private AdminBookingUI bookingUI;
    private AdminRoomChangeUI roomChangeUI;
    private AdminReportUI reportUI;

    private static final String[] PAGE_KEYS = {
            "DASHBOARD", "ROOMS", "TENANTS", "BOOKINGS", "MOVES", "PROBLEMS"
    };

    private static final String[] PAGE_TITLES = {
            "แผงควบคุมหลักผู้ดูแลหอพัก",
            "จัดการห้องพัก",
            "ทะเบียนผู้เช่าและสัญญาเช่า",
            "พิจารณาคำขอจองห้อง",
            "พิจารณาคำขอย้ายห้อง",
            "จัดการคำร้องเรียนและงานแจ้งซ่อม"
    };

    /**
     * คอนสตรัคเตอร์สำหรับสร้างหน้าต่างหลักของผู้ดูแลระบบหอพัก
     * กำหนดการตั้งค่าเฟรม แถบเมนูด้านข้าง แถบส่วนหัวด้านบน และแผง CardLayout สำหรับบรรจุหน้าย่อย
     */
    public AdminUI() {
        setTitle("KU Dormitory - แผงควบคุมผู้ดูแล");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(null);
        root.setBackground(BG);
        setContentPane(root);

        // Sidebar
        root.add(buildSidebar());

        // Header
        JPanel header = createHeader(PAGE_TITLES[0]);
        headerTitleLabel = (JLabel) header.getClientProperty("titleLabel");
        root.add(header);

        // CardLayout ตรงกลางสำหรับสลับหน้า
        cardLayout = new CardLayout();
        centerCardsPanel = new JPanel(cardLayout);
        centerCardsPanel.setBounds(190, 60, 910, 640);
        centerCardsPanel.setOpaque(false);

        // สร้าง Instance ของหน้าย่อยต่าง ๆ
        dashboardPanel = buildDashboardContent();
        roomUI         = new AdminRoomUI();
        tenantUI       = new AdminTenantUI();
        bookingUI      = new AdminBookingUI();
        roomChangeUI   = new AdminRoomChangeUI();
        reportUI       = new AdminReportUI();

        // เพิ่มการ์ดของแต่ละหน้าย่อย
        centerCardsPanel.add(dashboardPanel, "DASHBOARD");
        centerCardsPanel.add(roomUI, "ROOMS");
        centerCardsPanel.add(tenantUI, "TENANTS");
        centerCardsPanel.add(bookingUI, "BOOKINGS");
        centerCardsPanel.add(roomChangeUI, "MOVES");
        centerCardsPanel.add(reportUI, "PROBLEMS");

        root.add(centerCardsPanel);
        setVisible(true);
    }

    /**
     * สลับหน้าจอการทำงานไปยังหน้าย่อยที่ระบุตามดัชนี พร้อมสั่งรีเฟรชข้อมูลล่าสุดของหน้านั้น
     * 
     * @param index ดัชนีของหน้าย่อยที่ต้องการเปิด (0 ถึง 5)
     */
    public void showPage(int index) {
        cardLayout.show(centerCardsPanel, PAGE_KEYS[index]);
        if (headerTitleLabel != null) {
            headerTitleLabel.setText(PAGE_TITLES[index]);
        }
        setTitle("KU Dormitory - " + PAGE_TITLES[index]);

        for (int i = 0; i < menuItems.size(); i++) {
            menuItems.get(i).setSelected(i == index);
        }

        // 🟢 เรียกอัปเดตข้อมูลใหม่ของหน้านั้น ๆ เมื่อกดเปิด
        switch (PAGE_KEYS[index]) {
            case "DASHBOARD":
                centerCardsPanel.remove(dashboardPanel);
                dashboardPanel = buildDashboardContent();
                centerCardsPanel.add(dashboardPanel, "DASHBOARD");
                cardLayout.show(centerCardsPanel, "DASHBOARD");
                break;
            case "ROOMS":
                if (roomUI != null) roomUI.loadTableData();
                break;
            case "TENANTS":
                if (tenantUI != null) tenantUI.refreshData();
                break;
            case "BOOKINGS":
                if (bookingUI != null) bookingUI.refreshData();
                break;
            case "MOVES":
                if (roomChangeUI != null) roomChangeUI.refreshData();
                break;
            case "PROBLEMS":
                if (reportUI != null) reportUI.refreshData();
                break;
        }
    }

    /**
     * สร้างแถบเมนูด้านข้าง (Sidebar) ประกอบด้วยโลโก้ รายการเมนูหลัก และปุ่มออกจากระบบ
     * 
     * @return JPanel แถบด้านข้าง
     */
    private JPanel buildSidebar() {
        JPanel side = new JPanel(null);
        side.setBackground(WHITE);
        side.setBounds(0, 0, 190, 700);
        side.setBorder(new MatteBorder(0, 0, 0, 1, BORDER));

        RoundedPanel logo = new RoundedPanel(PRIMARY, null, 8);
        logo.setLayout(new GridBagLayout());
        logo.setBounds(16, 16, 26, 26);
        JLabel ku = label("KU", Font.BOLD, 11, WHITE);
        logo.add(ku);
        side.add(logo);

        JLabel brand = label("KU Dormitory", Font.BOLD, 15, TEXT);
        brand.setBounds(50, 16, 130, 26);
        side.add(brand);

        String[] menus = {
                "Dashboard ผู้ดูแล", "จัดการห้องพัก", "ผู้เช่า/สัญญา",
                "คำขอจองห้อง", "คำขอย้ายห้อง", "คำร้อง/แจ้งซ่อม"
        };
        for (int i = 0; i < menus.length; i++) {
            final int index = i;
            MenuItem item = new MenuItem(menus[i], i, i == 0, () -> showPage(index));
            item.setBounds(14, 62 + i * 36, 162, 32);
            side.add(item);
            menuItems.add(item);
        }

        JLabel logout = label("Logout", Font.BOLD, 12, RED);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setBounds(28, 600, 140, 24);
        logout.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int confirm = JOptionPane.showConfirmDialog(AdminUI.this,
                        "คุณต้องการออกจากระบบใช่หรือไม่?", "ยืนยันการออกจากระบบ", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                    new LoginUI().setVisible(true);
                }
            }
        });
        side.add(logout);
        return side;
    }

    /**
     * สร้างและรวบรวมคอมโพเนนต์สำหรับหน้าแผงควบคุมหลัก (Dashboard)
     * 
     * @return JPanel แผงควบคุมแดชบอร์ด
     */
    private JPanel buildDashboardContent() {
        JPanel p = new JPanel(null);
        p.setOpaque(false);
        createStatCards(p);
        p.add(createChartCard());
        p.add(createRequestCard());
        return p;
    }

    /**
     * สร้างแถบเมนูด้านข้างพื้นฐาน
     * 
     * @param frame หน้าต่างหลัก
     * @param activeIndex ลำดับเมนูที่เปิดอยู่
     * @return JPanel แถบด้านข้าง
     */
    public static JPanel createSidebar(JFrame frame, int activeIndex) {
        JPanel side = new JPanel(null);
        side.setBackground(WHITE);
        side.setBounds(0, 0, 190, 700);
        side.setBorder(new MatteBorder(0, 0, 0, 1, BORDER));
        return side;
    }

    /**
     * สร้างแถบส่วนหัวด้านบน (Header Bar) แสดงชื่อหน้าและข้อมูลโปรไฟล์ผู้ดูแลระบบ
     * 
     * @param pageTitle ชื่อเรื่องของหน้า
     * @return JPanel แถบส่วนหัว
     */
    public static JPanel createHeader(String pageTitle) {
        JPanel header = new JPanel(null);
        header.setBackground(WHITE);
        header.setBounds(190, 0, 910, 60);
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        JLabel title = label(pageTitle, Font.BOLD, 17, TEXT);
        title.setBounds(22, 0, 400, 60);
        header.add(title);
        header.putClientProperty("titleLabel", title);

        AvatarPanel avatar = new AvatarPanel();
        avatar.setBounds(715, 13, 34, 34);
        header.add(avatar);

        JLabel name = label("ผู้ดูแลระบบ", Font.BOLD, 12, TEXT);
        name.setBounds(757, 13, 140, 18);
        header.add(name);

        JLabel role = label("ผู้ดูแลระบบกลาง", Font.PLAIN, 10, TEXT_LIGHT);
        role.setBounds(757, 31, 140, 15);
        header.add(role);

        return header;
    }

    /**
     * สร้างการ์ดแสดงข้อมูลสรุปตัวเลขสถิติ 4 ใบ (ห้องพักทั้งหมด, ห้องว่าง, ห้องเต็ม, คำขอรอพิจารณา)
     * 
     * @param root พาเนลสำหรับเพิ่มการ์ดสถิติ
     */
    private void createStatCards(JPanel root) {
        int x = 20, y = 18, w = 202, h = 88, gap = 16;
        int total = AdminLogic.getTotalRooms();
        int avail = AdminLogic.getAvailableRoomsCount();
        int occupied = AdminLogic.getOccupiedRoomsCount();
        int rate = AdminLogic.getOccupancyRate();
        int pending = AdminLogic.getPendingRequestsCount();
        int tenants = AdminLogic.getTenantCount();

        root.add(statCard("ห้องพักทั้งหมด", total + " ห้อง", TEXT, "ใช้งานอยู่ " + rate + "%", x, y, w, h));
        root.add(statCard("ห้องว่างปัจจุบัน", avail + " ห้อง", GREEN, "พร้อมให้เช่า", x + (w + gap), y, w, h));
        root.add(statCard("ห้องเต็ม / มีผู้เช่า", occupied + " ห้อง", RED, "ผู้เข้าพักจริง " + tenants + " คน (" + rate + "%)", x + (w + gap) * 2, y, w, h));

        JPanel pendingCard = statCard("คำขอรอพิจารณา", pending + " รายการ", PRIMARY, "ตรวจสอบรายการรอดำเนินการ", x + (w + gap) * 3, y, w, h);
        pendingCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pendingCard.setToolTipText("คลิกเพื่อดูคำขอจองห้อง");
        pendingCard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                showPage(3); // ไปหน้าพิจารณาคำขอจองห้อง
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (pendingCard instanceof RoundedPanel) {
                    ((RoundedPanel) pendingCard).setBorderColor(PRIMARY);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (pendingCard instanceof RoundedPanel) {
                    ((RoundedPanel) pendingCard).setBorderColor(BORDER);
                }
            }
        });
        root.add(pendingCard);
    }

    /**
     * สร้างการ์ดแสดงค่าสถิติใบเดียวพร้อมระบุสีและคำอธิบายย่อย
     * 
     * @param title หัวข้อสถิติ
     * @param value ค่าตัวเลขหรือข้อความสถิติ
     * @param valueColor สีของค่าตัวเลข
     * @param sub คำอธิบายย่อย
     * @param x ตำแหน่ง X
     * @param y ตำแหน่ง Y
     * @param w ความกว้าง
     * @param h ความสูง
     * @return JPanel การ์ดสถิติ
     */
    private JPanel statCard(String title, String value, Color valueColor, String sub, int x, int y, int w, int h) {
        RoundedPanel card = new RoundedPanel(WHITE, BORDER, 12);
        card.setLayout(null);
        card.setBounds(x, y, w, h);

        JLabel t = label(title, Font.BOLD, 11, TEXT);
        t.setBounds(16, 12, w - 32, 16);
        card.add(t);

        JLabel v = label(value, Font.BOLD, 24, valueColor);
        v.setBounds(16, 30, w - 32, 32);
        card.add(v);

        JLabel s = label(sub, Font.PLAIN, 10, TEXT_LIGHT);
        s.setBounds(16, 64, w - 32, 14);
        card.add(s);
        return card;
    }

    /**
     * สร้างการ์ดแสดงกราฟแท่งประวัติอัตราการเข้าพักรายเดือนย้อนหลัง
     * 
     * @return JPanel การ์ดกราฟสถิติ
     */
    private JPanel createChartCard() {
        RoundedPanel card = new RoundedPanel(WHITE, BORDER, 12);
        card.setLayout(null);
        card.setBounds(20, 122, 520, 250);

        JLabel title = label("อัตราการเข้าพักรายเดือน", Font.BOLD, 14, TEXT);
        title.setBounds(16, 14, 300, 20);
        card.add(title);

        int total = AdminLogic.getTotalRooms();
        int occupied = AdminLogic.getOccupiedRoomsCount();
        int tenants = AdminLogic.getTenantCount();
        JLabel sub = label("ผู้เข้าพักจริง " + tenants + " คน (" + occupied + " จากทั้งหมด " + total + " ห้อง)", Font.PLAIN, 10, TEXT_LIGHT);
        sub.setBounds(16, 34, 300, 14);
        card.add(sub);

        Badge badge = new Badge("6 เดือนล่าสุด", new Color(240, 253, 250), PRIMARY);
        badge.setLocation(520 - 16 - badge.getWidth(), 16);
        card.add(badge);

        AdminLogic.MonthlyOccupancy occ = AdminLogic.getMonthlyOccupancyHistory(6);
        BarChart chart = new BarChart(occ.months, occ.counts, Math.max(10, total), " ห้อง");
        chart.setBounds(16, 60, 488, 178);
        card.add(chart);
        return card;
    }

    /**
     * สร้างการ์ดแสดงรายการคำร้องที่รอดำเนินการ (คำขอจอง, ย้ายห้อง, แจ้งซ่อม)
     * 
     * @return JPanel การ์ดรายการคำร้อง
     */
    private JPanel createRequestCard() {
        RoundedPanel card = new RoundedPanel(WHITE, BORDER, 12);
        card.setLayout(null);
        card.setBounds(556, 122, 320, 470);

        JLabel title = label("คำร้องรอดำเนินการ", Font.BOLD, 14, TEXT);
        title.setBounds(16, 14, 200, 22);
        card.add(title);

        List<AdminLogic.RequestItemModel> requests = AdminLogic.getPendingRequests();
        Badge count = new Badge(requests.size() + " รายการ", new Color(254, 226, 226), RED);
        count.setLocation(320 - 16 - count.getWidth(), 16);
        card.add(count);

        int startY = 52;
        int displayLimit = Math.min(requests.size(), 5);
        for (int i = 0; i < displayLimit; i++) {
            AdminLogic.RequestItemModel req = requests.get(i);
            Color tagBg = req.type.equals("repair") ? new Color(254, 243, 199) :
                          req.type.equals("booking") ? new Color(224, 231, 255) : PRIMARY_BG;
            Color tagFg = req.type.equals("repair") ? new Color(180, 83, 9) :
                          req.type.equals("booking") ? new Color(67, 56, 202) : PRIMARY;

            card.add(requestItem(req.tag, tagBg, tagFg, req.room, req.desc, req.time, startY, req.type));
            startY += 70;
        }

        if (requests.isEmpty()) {
            JLabel emptyLbl = label("ไม่มีคำร้องรอดำเนินการในขณะนี้", Font.PLAIN, 12, TEXT_LIGHT);
            emptyLbl.setBounds(16, 80, 280, 30);
            card.add(emptyLbl);
        }

        return card;
    }

    /**
     * สร้างรายการแสดงคำร้องแต่ละรายการในการ์ดคำร้อง
     * 
     * @param tag ป้ายประเภทคำร้อง
     * @param tagBg สีพื้นหลังป้าย
     * @param tagFg สีตัวอักษรป้าย
     * @param room หมายเลขห้อง
     * @param desc คำอธิบายปัญหาหรือรายละเอียด
     * @param time เวลาหรือวันที่
     * @param y ตำแหน่งแกน Y
     * @param type ประเภทคำร้อง (booking, move, repair)
     * @return JPanel แผงรายการคำร้อง
     */
    private JPanel requestItem(String tag, Color tagBg, Color tagFg, String room, String desc, String time, int y, String type) {
        RoundedPanel item = new RoundedPanel(ITEM_BG, BORDER, 10);
        item.setLayout(null);
        item.setBounds(14, y, 292, 60);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Badge badge = new Badge(tag, tagBg, tagFg);
        badge.setLocation(12, 10);
        item.add(badge);

        JLabel r = label(room, Font.BOLD, 12, TEXT);
        r.setBounds(12 + badge.getWidth() + 8, 9, 100, 20);
        item.add(r);

        JLabel t = label(time, Font.PLAIN, 10, TEXT_LIGHT);
        t.setHorizontalAlignment(SwingConstants.RIGHT);
        t.setBounds(180, 10, 100, 16);
        item.add(t);

        JLabel d = label(desc, Font.PLAIN, 11, new Color(55, 65, 81));
        d.setBounds(12, 34, 270, 18);
        item.add(d);

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { item.setBorderColor(PRIMARY); }
            @Override public void mouseExited(MouseEvent e)  { item.setBorderColor(BORDER); }
            @Override public void mouseClicked(MouseEvent e) {
                if ("booking".equalsIgnoreCase(type)) {
                    showPage(3);
                } else if ("move".equalsIgnoreCase(type)) {
                    showPage(4);
                } else if ("repair".equalsIgnoreCase(type)) {
                    showPage(5);
                }
            }
        });
        return item;
    }

    /**
     * สร้างแบบอักษร Font ตามรูปแบบและขนาดที่กำหนด
     * 
     * @param style รูปแบบฟอนต์ (Font.PLAIN, Font.BOLD)
     * @param size ขนาดตัวอักษร
     * @return Font อ็อบเจกต์ฟอนต์
     */
    static Font font(int style, int size) {
        return new Font(FONT_NAME, style, size);
    }

    /**
     * สร้างป้ายข้อความ JLabel พร้อมกำหนดฟอนต์และสี
     * 
     * @param text ข้อความ
     * @param style รูปแบบฟอนต์
     * @param size ขนาดตัวอักษร
     * @param color สีข้อความ
     * @return JLabel ป้ายข้อความ
     */
    static JLabel label(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    /**
     * กำหนดการวาดภาพกราฟิกแบบลบรอยหยัก (Anti-aliasing) เพื่อความคมชัดและสวยงาม
     * 
     * @param g กราฟิกส์คอนเท็กซ์เดิม
     * @return Graphics2D กราฟิกส์คอนเท็กซ์ที่เปิดใช้งาน Anti-aliasing แล้ว
     */
    static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    static class RoundedPanel extends JPanel {
        private final Color bg;
        private Color border;
        private final int radius;

        /**
         * สร้างแผงพาเนลขอบโค้งมน
         * 
         * @param bg สีพื้นหลัง
         * @param border สีเส้นขอบ (null หากไม่ต้องการเส้นขอบ)
         * @param radius รัศมีความโค้งมนของมุม
         */
        RoundedPanel(Color bg, Color border, int radius) {
            this.bg = bg;
            this.border = border;
            this.radius = radius;
            setOpaque(false);
        }

        /**
         * เปลี่ยนสีเส้นขอบของแผงพาเนลและวาดใหม่
         * 
         * @param c สีเส้นขอบใหม่
         */
        void setBorderColor(Color c) { border = c; repaint(); }

        /**
         * วาดพื้นหลังและขอบโค้งมนตามรัศมีที่ระบุ
         * 
         * @param g กราฟิกส์คอนเท็กซ์
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Badge คือคอมโพเนนต์ป้ายข้อความขนาดเล็กพร้อมพื้นหลังโค้งมน
     * bg คือ สีพื้นหลังของป้าย
     */
    static class Badge extends JLabel {
        // ===== representation =====
        // Abstraction Function:
        // AF(bg) คือ ป้ายกำกับสถานะพร้อมสีพื้นหลังที่กำหนด
        // RI
        // 1. bg ต้องไม่เป็น null

        private final Color bg;

        /**
         * สร้างป้ายข้อความ Badge ขนาดกะทัดรัด
         * 
         * @param text ข้อความบนป้าย
         * @param bg สีพื้นหลัง
         * @param fg สีตัวอักษร
         */
        Badge(String text, Color bg, Color fg) {
            super(text, SwingConstants.CENTER);
            this.bg = bg;
            setFont(font(Font.BOLD, 10));
            setForeground(fg);
            FontMetrics fm = getFontMetrics(getFont());
            int w = fm.stringWidth(text) + 16;
            setSize(w, 20);
            setPreferredSize(new Dimension(w, 20));
        }

        /**
         * วาดพื้นหลังขอบมนของป้าย Badge
         * 
         * @param g กราฟิกส์คอนเท็กซ์
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * AvatarPanel คือคอมโพเนนต์แสดงรูปโปรไฟล์ผู้ดูแลระบบแบบวงกลม
     */
    static class AvatarPanel extends JPanel {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ คอมโพเนนต์กราฟิกแสดงสัญลักษณ์อวตารผู้ใช้งาน
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * สร้างคอมโพเนนต์ไอคอนอวตารแบบโปร่งใส
         */
        AvatarPanel() { setOpaque(false); }

        /**
         * วาดรูปสัญลักษณ์บุคคลในวงกลม
         * 
         * @param g กราฟิกส์คอนเท็กซ์
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            int s = Math.min(getWidth(), getHeight()) - 1;
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, s, s));
            g2.setColor(new Color(209, 213, 219));
            g2.fillRect(0, 0, s, s);
            g2.setColor(new Color(107, 114, 128));
            g2.fillOval(s / 2 - 6, 7, 12, 12);
            g2.fillOval(s / 2 - 12, 21, 24, 20);
            g2.setClip(null);
            g2.setColor(BORDER);
            g2.drawOval(0, 0, s, s);
            g2.dispose();
        }
    }

    /**
     * MenuItem คือคอมโพเนนต์ปุ่มเมนูแถบด้านข้างสำหรับเลือกเปลี่ยนหน้าการทำงาน
     * text คือ ข้อความชื่อเมนู
     * iconType คือ รหัสประเภทไอคอนของเมนู
     * active คือ สถานะว่าเมนูนี้กำลังถูกเลือกใช้งานอยู่หรือไม่
     * hover คือ สถานะว่าเมาส์กำลังชี้อยู่เหนือปุ่มเมนูหรือไม่
     */
    static class MenuItem extends JPanel {
        // ===== representation =====
        // Abstraction Function:
        // AF(text, iconType, active, hover) คือ ปุ่มเมนูการนำทางพร้อมการตอบสนองต่อการคลิกและการชี้เมาส์
        // RI
        // 1. text ต้องไม่เป็น null

        private final String text;
        private final int iconType;
        private boolean active;
        private boolean hover;

        /**
         * สร้างปุ่มเมนูนำทางด้านข้าง
         * 
         * @param text ข้อความชื่อเมนู
         * @param iconType ประเภทไอคอน (0 ถึง 5)
         * @param active สถานะกำลังเปิดใช้งานอยู่หรือไม่
         * @param onClick โค้ดการทำงานเมื่อคลิกเลือกเมนู
         */
        MenuItem(String text, int iconType, boolean active, Runnable onClick) {
            this.text = text;
            this.iconType = iconType;
            this.active = active;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
            });
        }

        /**
         * กำหนดสถานะการเลือกเมนูและสั่งวาดใหม่
         * 
         * @param s ค่าความจริงว่าถูกเลือกหรือไม่
         */
        void setSelected(boolean s) {
            this.active = s;
            repaint();
        }

        /**
         * วาดพื้นหลัง ไอคอน และข้อความชื่อเมนูตามสถานะการเลือกหรือการชี้เมาส์
         * 
         * @param g กราฟิกส์คอนเท็กซ์
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            if (active) {
                g2.setColor(PRIMARY_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            } else if (hover) {
                g2.setColor(new Color(243, 244, 246));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            Color c = active ? PRIMARY : new Color(75, 85, 99);
            drawIcon(g2, iconType, 12, (getHeight() - 16) / 2, c);

            g2.setFont(font(active ? Font.BOLD : Font.PLAIN, 12));
            g2.setColor(active ? PRIMARY : new Color(55, 65, 81));
            FontMetrics fm = g2.getFontMetrics();
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, 38, ty);
            g2.dispose();
        }

        /**
         * วาดไอคอนแบบเวกเตอร์ตามประเภทของเมนู
         * 
         * @param g2 กราฟิกส์คอนเท็กซ์
         * @param type ประเภทไอคอน
         * @param x พิกัด X
         * @param y พิกัด Y
         * @param c สีของไอคอน
         */
        private void drawIcon(Graphics2D g2, int type, int x, int y, Color c) {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (type) {
                case 0:
                    g2.drawLine(x + 1, y + 1, x + 1, y + 15);
                    g2.drawLine(x + 1, y + 15, x + 15, y + 15);
                    g2.drawLine(x + 5, y + 11, x + 5, y + 12);
                    g2.drawLine(x + 9, y + 7, x + 9, y + 12);
                    g2.drawLine(x + 13, y + 4, x + 13, y + 12);
                    break;
                case 1: {
                    Path2D p = new Path2D.Float();
                    p.moveTo(x + 1, y + 7); p.lineTo(x + 8, y + 1); p.lineTo(x + 15, y + 7);
                    g2.draw(p);
                    g2.drawRect(x + 3, y + 7, 10, 8);
                    g2.drawRect(x + 7, y + 11, 2, 4);
                    break;
                }
                case 2: {
                    Path2D p = new Path2D.Float();
                    p.moveTo(x + 3, y + 1); p.lineTo(x + 10, y + 1); p.lineTo(x + 14, y + 5);
                    p.lineTo(x + 14, y + 15); p.lineTo(x + 3, y + 15); p.closePath();
                    g2.draw(p);
                    g2.drawLine(x + 6, y + 9, x + 11, y + 9);
                    g2.drawLine(x + 6, y + 12, x + 11, y + 12);
                    break;
                }
                case 3:
                    g2.drawRoundRect(x + 1, y + 3, 14, 12, 3, 3);
                    g2.drawLine(x + 1, y + 7, x + 15, y + 7);
                    g2.drawLine(x + 5, y + 1, x + 5, y + 4);
                    g2.drawLine(x + 11, y + 1, x + 11, y + 4);
                    break;
                case 4:
                    g2.drawLine(x + 2, y + 5, x + 14, y + 5);
                    g2.drawLine(x + 11, y + 2, x + 14, y + 5);
                    g2.drawLine(x + 11, y + 8, x + 14, y + 5);
                    g2.drawLine(x + 2, y + 11, x + 14, y + 11);
                    g2.drawLine(x + 5, y + 8, x + 2, y + 11);
                    g2.drawLine(x + 5, y + 14, x + 2, y + 11);
                    break;
                case 5:
                    g2.drawLine(x + 2, y + 14, x + 9, y + 7);
                    g2.drawOval(x + 8, y + 1, 7, 7);
                    break;
                default:
                    g2.drawOval(x + 1, y + 1, 14, 14);
                    g2.drawLine(x + 8, y + 1, x + 8, y + 8);
                    g2.drawLine(x + 8, y + 8, x + 15, y + 8);
                    break;
            }
        }
    }

    /**
     * BarChart คือคอมโพเนนต์แสดงกราฟแท่งสถิติข้อมูล
     * labels คือ รายการป้ายชื่อแท่งกราฟ
     * values คือ ค่าข้อมูลตัวเลขของแต่ละแท่ง
     * max คือ ค่าสูงสุดของข้อมูลสำหรับปรับสเกลความสูงกราฟ
     * unit คือ หน่วยของข้อมูล
     */
    static class BarChart extends JPanel {
        // ===== representation =====
        // Abstraction Function:
        // AF(labels, values, max, unit) คือ กราฟแท่งแสดงข้อมูลสถิติเปรียบเทียบในระบบ
        // RI
        // 1. labels และ values ต้องไม่เป็น null
        // 2. max ต้องมีค่ามากกว่า 0

        private final String[] labels;
        private final int[] values;
        private final int max;
        private final String unit;

        /**
         * สร้างคอมโพเนนต์กราฟแท่ง
         * 
         * @param labels ป้ายกำกับใต้แท่งกราฟ
         * @param values ค่าตัวเลขของแต่ละแท่ง
         * @param max ค่าสูงสุดของแกนกราฟ
         * @param unit หน่วยของข้อมูล
         */
        BarChart(String[] labels, int[] values, int max, String unit) {
            this.labels = labels;
            this.values = values;
            this.max = max;
            this.unit = unit;
            setOpaque(false);
        }

        /**
         * วาดเส้นฐาน แท่งกราฟ ค่าตัวเลข และป้ายกำกับของแต่ละเดือน
         * 
         * @param g กราฟิกส์คอนเท็กซ์
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            int w = getWidth(), h = getHeight();
            int bottom = h - 24, top = 20, barW = 44;
            int slot = w / values.length;

            g2.setColor(BORDER);
            g2.drawLine(0, bottom, w, bottom);

            for (int i = 0; i < values.length; i++) {
                int bh = (int) ((double) values[i] / max * (bottom - top));
                int bx = slot * i + (slot - barW) / 2;
                int by = bottom - bh;

                if (bh > 0) {
                    g2.setColor(PRIMARY);
                    g2.fillRoundRect(bx, by, barW, bh, 6, 6);
                    if (bh > 4) {
                        g2.fillRect(bx, by + 4, barW, bh - 4);
                    }
                }

                g2.setFont(font(Font.PLAIN, 10));
                g2.setColor(new Color(55, 65, 81));
                String v = values[i] + unit;
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(v, bx + (barW - fm.stringWidth(v)) / 2, by - 6);

                g2.setColor(TEXT_LIGHT);
                String m = labels[i];
                g2.drawString(m, bx + (barW - fm.stringWidth(m)) / 2, bottom + 16);
            }
            g2.dispose();
        }
    }

}