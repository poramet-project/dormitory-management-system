package DMS;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TenantUI extends JFrame {

    static final Color TEAL        = new Color(0x12756B);
    static final Color TEAL_HOVER  = new Color(0x0E5F57);
    static final Color TEAL_LIGHT  = new Color(0xD3F1EC);
    static final Color TEAL_BORDER = new Color(0x8FD8CC);
    static final Color BG          = new Color(0xF2F2F2);
    static final Color CARD_BORDER = new Color(0xD9D9D9);
    static final Color TEXT        = new Color(0x333333);
    static final Color TEXT_MUTED  = new Color(0x666666);
    static final Color YELLOW      = new Color(0xF2D80C);
    static final Color GREEN       = new Color(0x3BD675);

    static final String FONT = pickThaiFont();

    static String pickThaiFont() {
        String[] prefs = {"Sarabun", "Noto Sans Thai", "Leelawadee UI", "Leelawadee", "Thonburi", "Tahoma"};
        Set<String> avail = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String p : prefs) if (avail.contains(p)) return p;
        return Font.DIALOG;
    }

    static Font font(int style, float size) {
        return new Font(FONT, style, Math.round(size));
    }

    static JLabel label(String text, float size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    private final CardLayout cardLayout;
    private final JPanel centerCardsPanel;
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final LogicLogin.User currentUser;

    public TenantUI() {
        super("KU Dormitory");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(null);

        LogicLogin.User user = LogicLogin.getCurrentUser();
        if (user == null) {
            user = new LogicLogin.User("tenant", "1234", "กิตติพงษ์ รักสงบ", "0812345678", "TENANT", "R101");
        }
        this.currentUser = user;

        JPanel root = new JPanel(new BorderLayout());

        // 1. Sidebar ด้านซ้าย (คงอยู่ตลอดเวลา)
        root.add(buildSidebar(), BorderLayout.WEST);

        // 2. ฝั่งขวา (TopBar + Content Cards)
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(buildTopBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        centerCardsPanel = new JPanel(cardLayout);

        // เพิ่มแต่ละ Class Panel เข้าสู่ CardLayout
        centerCardsPanel.add(new TenantHome(this, currentUser), "HOME");
        centerCardsPanel.add(new MyRoomUI(currentUser), "ROOM");
        centerCardsPanel.add(new LeaseContractUI(currentUser), "CONTRACT");
        centerCardsPanel.add(new ReportUI(currentUser), "REPORT");
        centerCardsPanel.add(new RoomChangeRequestUI(currentUser), "TRANSFER");

        rightContainer.add(centerCardsPanel, BorderLayout.CENTER);
        root.add(rightContainer, BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
    }

    public void showPage(String pageName, int menuIndex) {
        cardLayout.show(centerCardsPanel, pageName);
        for (int i = 0; i < menuItems.size(); i++) {
            menuItems.get(i).setSelected(i == menuIndex);
        }
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(null);
        side.setBackground(Color.WHITE);
        side.setPreferredSize(new Dimension(195, 0));
        side.setBorder(new MatteBorder(0, 0, 0, 1, CARD_BORDER));

        JLabel logo = new JLabel("KU", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(TEAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logo.setFont(font(Font.BOLD, 14));
        logo.setForeground(Color.WHITE);
        logo.setBounds(14, 14, 32, 32);
        side.add(logo);

        JLabel title = label("KU Dormitory", 15, Font.BOLD, Color.BLACK);
        title.setBounds(54, 17, 130, 26);
        side.add(title);

        String[] names = {"หน้าหลัก", "ห้องพักของฉัน", "สัญญาเช่า", "แจ้งซ่อม/ร้องเรียน", "ยื่นคำขอย้ายห้อง"};
        String[] cards = {"HOME", "ROOM", "CONTRACT", "REPORT", "TRANSFER"};
        LineIcon.Type[] icons = {LineIcon.Type.GRID, LineIcon.Type.BED, LineIcon.Type.DOC,
                LineIcon.Type.WRENCH, LineIcon.Type.ARROW};

        for (int i = 0; i < names.length; i++) {
            final int index = i;
            MenuItem m = new MenuItem(names[i], icons[i], () -> showPage(cards[index], index));
            m.setBounds(12, 70 + i * 40, 172, 34);
            side.add(m);
            menuItems.add(m);
        }
        menuItems.get(0).setSelected(true);

        JLabel logoutBtn = label("🚪 ออกจากระบบ", 13, Font.PLAIN, new Color(200, 50, 50));
        logoutBtn.setBounds(20, 620, 150, 30);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                LogicLogin.logout();
                dispose();
                new LoginUI().setVisible(true);
            }
        });
        side.add(logoutBtn);

        return side;
    }

    class MenuItem extends JPanel {
        private boolean selected, hover;
        private final JLabel text;
        private final LineIcon icon;

        MenuItem(String name, LineIcon.Type type, Runnable onClick) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 7));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            icon = new LineIcon(type, 15, TEXT);
            text = new JLabel(name, icon, SwingConstants.LEFT);
            text.setIconTextGap(10);
            text.setFont(font(Font.PLAIN, 13));
            text.setForeground(TEXT);
            add(text);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (onClick != null) onClick.run();
                }
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        void setSelected(boolean s) {
            selected = s;
            Color c = s ? TEAL : TEXT;
            text.setForeground(c);
            icon.color = c;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            if (selected || hover) {
                Graphics2D g2 = aa(g);
                g2.setColor(selected ? TEAL_LIGHT : new Color(0xF2F7F6));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        top.setBackground(Color.WHITE);
        top.setPreferredSize(new Dimension(0, 52));
        top.setBorder(new MatteBorder(0, 0, 1, 0, CARD_BORDER));

        top.add(new Avatar(36));

        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));

        String displayName = (currentUser.fullName != null && !currentUser.fullName.isEmpty()) 
                ? currentUser.fullName : currentUser.username;
        String roleText = "ผู้อยู่อาศัย (ห้อง " + (currentUser.roomId.isEmpty() ? "ไม่ระบุ" : currentUser.roomId) + ")";

        names.add(label(displayName, 13, Font.BOLD, Color.BLACK));
        names.add(label(roleText, 11, Font.PLAIN, TEXT_MUTED));
        names.setBorder(new EmptyBorder(0, 0, 0, 15));
        top.add(names);
        return top;
    }

    static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    static class RoundedPanel extends JPanel {
        final int radius; final Color fill; final Color border;
        RoundedPanel(int radius, Color fill, Color border) {
            this.radius = radius; this.fill = fill; this.border = border;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);
            }
            g2.dispose();
        }
    }

    static class RoundButton extends JButton {
        RoundButton(String text) {
            super(text);
            setFont(font(Font.BOLD, 14));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            ButtonModel m = getModel();
            g2.setColor(m.isPressed() ? TEAL_HOVER.darker() : m.isRollover() ? TEAL_HOVER : TEAL);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class Pill extends JLabel {
        final Color fill, border;
        Pill(String text, Color fill, Color border, Color fg, float size) {
            super(text, SwingConstants.CENTER);
            this.fill = fill; this.border = border;
            setFont(font(Font.PLAIN, size));
            setForeground(fg);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            int h = getHeight();
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, h - 1, h, h);
            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, h - 3, h - 2, h - 2);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class CircleIcon extends JComponent {
        final Icon icon; final int size;
        CircleIcon(Icon icon, int size) { this.icon = icon; this.size = size; }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            g2.setColor(TEAL_LIGHT);
            g2.fillOval(0, 0, size, size);
            icon.paintIcon(this, g2, (size - icon.getIconWidth()) / 2, (size - icon.getIconHeight()) / 2);
            g2.dispose();
        }
    }

    static Icon dot(Color c, int d) {
        return new Icon() {
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = aa(g);
                g2.setColor(c);
                g2.fillOval(x, y, d, d);
                g2.dispose();
            }
            public int getIconWidth()  { return d; }
            public int getIconHeight() { return d; }
        };
    }

    static class Avatar extends JComponent {
        final int size; final Image img;
        Avatar(int size) {
            this.size = size;
            this.img = loadImage("avatar.png", "avatar.jpg", "src/img/avatar.png");
            setPreferredSize(new Dimension(size, size + 2));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            Shape circle = new Ellipse2D.Double(0, 1, size, size);
            if (img != null) {
                g2.setClip(circle);
                g2.drawImage(img, 0, 1, size, size, null);
            } else {
                g2.setColor(new Color(0xE3E7EA));
                g2.fill(circle);
                g2.setClip(circle);
                g2.setColor(new Color(0x9AA4AB));
                g2.fillOval(size / 2 - 7, 7, 14, 14);
                g2.fillOval(size / 2 - 13, 23, 26, 22);
            }
            g2.dispose();
        }
    }

    static class RoomImage extends JComponent {
        final Image img = loadImage("src/img/room205.jpg", "room205.jpg", "src/img/room.jpg");
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            int w = getWidth(), h = getHeight();
            g2.setClip(new RoundRectangle2D.Double(0, 0, w, h, 10, 10));
            if (img != null) {
                int iw = img.getWidth(null), ih = img.getHeight(null);
                double s = Math.max((double) w / iw, (double) h / ih);
                int dw = (int) (iw * s), dh = (int) (ih * s);
                g2.drawImage(img, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
            } else {
                g2.setPaint(new GradientPaint(0, 0, new Color(0xF4EFE8), 0, h, new Color(0xE2DBD0)));
                g2.fillRect(0, 0, w, h);
            }
            g2.dispose();
        }
    }

    static Image loadImage(String... names) {
        for (String n : names) {
            try {
                File f = new File(n);
                if (f.exists()) {
                    BufferedImage b = ImageIO.read(f);
                    if (b != null) return b;
                }
            } catch (Exception ignored) { }
        }
        return null;
    }

    static class LineIcon implements Icon {
        enum Type { GRID, BED, DOC, WRENCH, ARROW, PIN, CALENDAR, RECEIPT }
        final Type type; final int size; Color color;
        LineIcon(Type type, int size, Color color) { this.type = type; this.size = size; this.color = color; }
        public int getIconWidth()  { return size; }
        public int getIconHeight() { return size; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = aa(g);
            g2.translate(x, y);
            g2.scale(size / 24.0, size / 24.0);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (type) {
                case GRID:
                    g2.draw(new RoundRectangle2D.Double(3, 3, 7, 7, 2, 2));
                    g2.draw(new RoundRectangle2D.Double(14, 3, 7, 7, 2, 2));
                    g2.draw(new RoundRectangle2D.Double(3, 14, 7, 7, 2, 2));
                    g2.draw(new RoundRectangle2D.Double(14, 14, 7, 7, 2, 2));
                    break;
                case BED:
                    g2.draw(new RoundRectangle2D.Double(2, 11, 20, 7, 2, 2));
                    g2.draw(new RoundRectangle2D.Double(4, 6, 7, 5, 2, 2));
                    g2.draw(new Line2D.Double(3, 18, 3, 21));
                    g2.draw(new Line2D.Double(21, 18, 21, 21));
                    break;
                case DOC:
                    Path2D p = new Path2D.Double();
                    p.moveTo(5, 2); p.lineTo(14, 2); p.lineTo(19, 7); p.lineTo(19, 22); p.lineTo(5, 22); p.closePath();
                    g2.draw(p);
                    g2.draw(new Line2D.Double(8.5, 11, 15.5, 11));
                    g2.draw(new Line2D.Double(8.5, 15, 15.5, 15));
                    g2.draw(new Line2D.Double(8.5, 19, 12.5, 19));
                    break;
                case WRENCH:
                    g2.draw(new Arc2D.Double(11, 3, 10, 10, 100, 270, Arc2D.OPEN));
                    g2.draw(new Line2D.Double(12.5, 11.5, 4, 20));
                    g2.draw(new Line2D.Double(14.5, 13.5, 6, 22));
                    g2.draw(new Arc2D.Double(3, 19, 3.5, 3.5, 135, 180, Arc2D.OPEN));
                    break;
                case ARROW:
                    g2.draw(new Line2D.Double(3, 12, 21, 12));
                    g2.draw(new Line2D.Double(15, 6, 21, 12));
                    g2.draw(new Line2D.Double(15, 18, 21, 12));
                    break;
                case PIN:
                    Path2D pin = new Path2D.Double();
                    pin.moveTo(12, 22); pin.curveTo(4, 14, 4, 3, 12, 3); pin.curveTo(20, 3, 20, 14, 12, 22);
                    g2.draw(pin);
                    g2.draw(new Ellipse2D.Double(9, 7.5, 6, 6));
                    break;
                case CALENDAR:
                    g2.draw(new RoundRectangle2D.Double(3, 5, 18, 16, 3, 3));
                    g2.draw(new Line2D.Double(3, 10, 21, 10));
                    g2.draw(new Line2D.Double(8, 3, 8, 7));
                    g2.draw(new Line2D.Double(16, 3, 16, 7));
                    g2.draw(new Line2D.Double(7, 14, 17, 14));
                    g2.draw(new Line2D.Double(7, 17.5, 13, 17.5));
                    break;
                case RECEIPT:
                    Path2D rec = new Path2D.Double();
                    rec.moveTo(10, 21); rec.lineTo(8, 19.5); rec.lineTo(6, 21); rec.lineTo(4, 19.5);
                    rec.lineTo(4, 3); rec.lineTo(17, 3); rec.lineTo(17, 9);
                    g2.draw(rec);
                    g2.draw(new Line2D.Double(7, 7, 14, 7));
                    g2.draw(new Line2D.Double(7, 10.5, 12, 10.5));
                    g2.draw(new Ellipse2D.Double(11, 11, 10, 10));
                    g2.draw(new Line2D.Double(16, 13.5, 16, 16));
                    g2.draw(new Line2D.Double(16, 16, 18, 17.5));
                    break;
            }
            g2.dispose();
        }
    }
}