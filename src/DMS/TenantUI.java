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

/**
 * KU Dormitory - หน้าหลักผู้อยู่อาศัย (Java Swing)
 * คอมไพล์:  javac -encoding UTF-8 TenantUI.java
 * รัน:      java TenantUI
 * (ถ้ามีไฟล์ room.jpg / avatar.png อยู่ในโฟลเดอร์เดียวกัน โปรแกรมจะใช้รูปนั้นแทนรูปตัวอย่าง)
 */
public class TenantUI extends JFrame {

    // ---------- สีหลัก ----------
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

    // =====================================================================
    public TenantUI() {
        super("KU Dormitory");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.add(buildTopBar(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        right.add(scroll, BorderLayout.CENTER);

        root.add(right, BorderLayout.CENTER);
        setContentPane(root);
        setVisible(true);
    }

    // ---------------------------- Sidebar ----------------------------
    private final List<MenuItem> menuItems = new ArrayList<>();

    private JPanel buildSidebar() {
        JPanel side = new JPanel(null);
        side.setBackground(Color.WHITE);
        side.setPreferredSize(new Dimension(192, 0));
        side.setBorder(new MatteBorder(0, 0, 0, 2, CARD_BORDER));

        // โลโก้
        JLabel logo = new JLabel("KU", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(TEAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logo.setFont(font(Font.PLAIN, 14));
        logo.setForeground(Color.WHITE);
        logo.setBounds(10, 7, 27, 27);
        side.add(logo);

        JLabel title = label("KU Dormitory", 15, Font.PLAIN, Color.BLACK);
        title.setBounds(44, 8, 140, 26);
        side.add(title);

        String[] names = {"หน้าหลัก", "ห้องพักของฉัน", "สัญญาเช่า", "แจ้งซ่อม/ร้องเรียน", "ยื่นคำขอย้ายห้อง"};
        LineIcon.Type[] icons = {LineIcon.Type.GRID, LineIcon.Type.BED, LineIcon.Type.DOC,
                LineIcon.Type.WRENCH, LineIcon.Type.ARROW};
        for (int i = 0; i < names.length; i++) {
            MenuItem m = new MenuItem(names[i], icons[i]);
            m.setBounds(11, 67 + i * 36, 162, 31);
            side.add(m);
            menuItems.add(m);
        }
        menuItems.get(0).setSelected(true);
        return side;
    }

    class MenuItem extends JPanel {
        private boolean selected, hover;
        private final JLabel text;
        private final LineIcon icon;

        MenuItem(String name, LineIcon.Type type) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            icon = new LineIcon(type, 14, TEXT);
            text = new JLabel(name, icon, SwingConstants.LEFT);
            text.setIconTextGap(10);
            text.setFont(font(Font.PLAIN, 12));
            text.setForeground(TEXT);
            add(text);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    for (MenuItem m : menuItems) m.setSelected(m == MenuItem.this);
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
                g2.setColor(selected ? TEAL_LIGHT : new Color(0xF0F7F6));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    // ---------------------------- Top bar ----------------------------
    private JPanel buildTopBar() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        top.setBackground(Color.WHITE);
        top.setPreferredSize(new Dimension(0, 47));
        top.setBorder(new MatteBorder(0, 0, 2, 0, CARD_BORDER));

        top.add(new Avatar(36));

        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.add(label("นางสาวณัชชา อรุณประเสริฐ", 13, Font.PLAIN, Color.BLACK));
        names.add(label("ผู้อยู่อาศัย", 11, Font.PLAIN, TEXT_MUTED));
        names.setBorder(new EmptyBorder(0, 0, 0, 10));
        top.add(names);
        return top;
    }

    // ---------------------------- Content ----------------------------
    private JPanel buildContent() {
        JPanel content = new JPanel(null);
        content.setBackground(BG);
        content.setPreferredSize(new Dimension(860, 596));

        content.add(roomCard(36, 14));
        content.add(repairCard(36, 208));
        content.add(contractCard(41, 428));
        return content;
    }

    /** การ์ดห้องพัก */
    private JPanel roomCard(int x, int y) {
        RoundedPanel card = new RoundedPanel(10, Color.WHITE, CARD_BORDER);
        card.setLayout(null);
        card.setBounds(x, y, 584, 184);

        RoomImage img = new RoomImage();
        img.setBounds(12, 12, 267, 160);
        card.add(img);

        JLabel room = label("ห้อง 205", 28, Font.PLAIN, TEXT);
        room.setBounds(303, 12, 250, 40);
        card.add(room);

        Pill air = new Pill("ห้องแอร์", TEAL_LIGHT, TEAL_BORDER, TEAL, 12);
        air.setBounds(317, 52, 72, 27);
        card.add(air);

        JLabel loc = label("อาคารหอพักหญิง 2 | ชั้น 2", 13, Font.PLAIN, TEXT_MUTED);
        loc.setIcon(new LineIcon(LineIcon.Type.PIN, 14, TEXT_MUTED));
        loc.setIconTextGap(6);
        loc.setBounds(300, 90, 270, 22);
        card.add(loc);

        RoundButton btn = new RoundButton("ดูรายละเอียด");
        btn.setBounds(335, 132, 227, 38);
        btn.addActionListener(e -> info("รายละเอียดห้อง 205\nอาคารหอพักหญิง 2 ชั้น 2\nประเภท: ห้องแอร์"));
        card.add(btn);
        return card;
    }

    /** การ์ดแจ้งซ่อม/ร้องเรียน */
    private JPanel repairCard(int x, int y) {
        RoundedPanel card = new RoundedPanel(10, Color.WHITE, CARD_BORDER);
        card.setLayout(null);
        card.setBounds(x, y, 584, 208);

        CircleIcon circle = new CircleIcon(new LineIcon(LineIcon.Type.WRENCH, 28, TEAL), 52);
        circle.setBounds(16, 10, 52, 52);
        card.add(circle);

        JLabel title = label("แจ้งซ่อม/ร้องเรียน", 16, Font.PLAIN, TEXT);
        title.setBounds(80, 23, 250, 26);
        card.add(title);

        // กล่องสถิติ
        RoundedPanel stat = new RoundedPanel(6, TEAL_LIGHT, null);
        stat.setLayout(null);
        stat.setBounds(36, 72, 515, 76);

        JLabel receipt = new JLabel(new LineIcon(LineIcon.Type.RECEIPT, 40, TEAL));
        receipt.setBounds(24, 16, 44, 44);
        stat.add(receipt);

        JLabel count = label("2", 26, Font.PLAIN, TEXT);
        count.setHorizontalAlignment(SwingConstants.CENTER);
        count.setBounds(76, 8, 34, 34);
        stat.add(count);

        JLabel unit = label("รายการ", 15, Font.PLAIN, TEXT);
        unit.setHorizontalAlignment(SwingConstants.CENTER);
        unit.setBounds(70, 38, 66, 24);
        stat.add(unit);

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setForeground(new Color(0xA9CFC9));
        sep.setBounds(141, 8, 2, 60);
        stat.add(sep);

        JLabel inProgress = label("กำลังดำเนินการ 1", 15, Font.PLAIN, TEXT_MUTED);
        inProgress.setIcon(dot(YELLOW, 7));
        inProgress.setIconTextGap(6);
        inProgress.setBounds(170, 13, 250, 24);
        stat.add(inProgress);

        JLabel done = label("เสร็จสิ้น 2", 15, Font.PLAIN, TEXT_MUTED);
        done.setIcon(dot(GREEN, 7));
        done.setIconTextGap(6);
        done.setBounds(170, 39, 250, 24);
        stat.add(done);

        card.add(stat);

        RoundButton btn = new RoundButton("ติดตามสถานะ");
        btn.setBounds(36, 160, 515, 36);
        btn.addActionListener(e -> info("รายการแจ้งซ่อม\n• กำลังดำเนินการ 1 รายการ\n• เสร็จสิ้น 2 รายการ"));
        card.add(btn);
        return card;
    }

    /** การ์ดสัญญาเช่า */
    private JPanel contractCard(int x, int y) {
        RoundedPanel card = new RoundedPanel(10, Color.WHITE, CARD_BORDER);
        card.setLayout(null);
        card.setBounds(x, y, 330, 156);

        CircleIcon circle = new CircleIcon(new LineIcon(LineIcon.Type.CALENDAR, 24, TEAL), 44);
        circle.setBounds(12, 10, 44, 44);
        card.add(circle);

        JLabel title = label("สัญญาเช่า", 16, Font.PLAIN, TEXT);
        title.setBounds(68, 14, 200, 26);
        card.add(title);

        Pill active = new Pill("ใช้งานอยู่", TEAL_LIGHT, null, new Color(0x0E8F5E), 11);
        active.setIcon(dot(new Color(0x1DB86A), 7));
        active.setIconTextGap(6);
        active.setBounds(60, 46, 78, 17);
        card.add(active);

        JLabel end = label("สิ้นสุดสัญญา     31 พ.ค.2569", 15, Font.PLAIN, TEXT);
        end.setIcon(new LineIcon(LineIcon.Type.CALENDAR, 16, TEXT));
        end.setIconTextGap(18);
        end.setBounds(46, 74, 280, 24);
        card.add(end);

        RoundButton btn = new RoundButton("ดูสัญญาเช่า");
        btn.setBounds(48, 108, 227, 36);
        btn.addActionListener(e -> info("สัญญาเช่าห้อง 205\nสถานะ: ใช้งานอยู่\nสิ้นสุดสัญญา: 31 พ.ค. 2569"));
        card.add(btn);
        return card;
    }

    private void info(String msg) {
        JLabel l = new JLabel("<html>" + msg.replace("\n", "<br>") + "</html>");
        l.setFont(font(Font.PLAIN, 14));
        JOptionPane.showMessageDialog(this, l, "KU Dormitory", JOptionPane.INFORMATION_MESSAGE);
    }

    // =====================================================================
    //  Custom components
    // =====================================================================
    static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    /** พาเนลมุมโค้ง */
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
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);
            }
            g2.dispose();
        }
    }

    /** ปุ่มสีเขียวมุมโค้ง */
    static class RoundButton extends JButton {
        RoundButton(String text) {
            super(text);
            setFont(font(Font.PLAIN, 16));
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

    /** ป้ายแคปซูล (badge) */
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
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, h - 3, h - 2, h - 2);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** วงกลมสีเขียวอ่อนที่มีไอคอนอยู่ตรงกลาง */
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

    /** รูปโปรไฟล์วงกลม — ใช้ avatar.png ถ้ามี */
    static class Avatar extends JComponent {
        final int size; final Image img;
        Avatar(int size) {
            this.size = size;
            this.img = loadImage("avatar.png", "avatar.jpg");
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

    /** รูปห้องพัก — ใช้ room.jpg ถ้ามี ไม่งั้นวาดภาพตัวอย่าง */
    static class RoomImage extends JComponent {
        final Image img = loadImage("/src/img/room205.jpg");
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            int w = getWidth(), h = getHeight();
            g2.setClip(new RoundRectangle2D.Double(0, 0, w, h, 10, 10));
            if (img != null) {
                // ครอปแบบ cover
                int iw = img.getWidth(null), ih = img.getHeight(null);
                double s = Math.max((double) w / iw, (double) h / ih);
                int dw = (int) (iw * s), dh = (int) (ih * s);
                g2.drawImage(img, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
            } else {
                g2.setPaint(new GradientPaint(0, 0, new Color(0xF4EFE8), 0, h, new Color(0xE2DBD0)));
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(0xD9CFC2));                  // พื้น
                g2.fillRect(0, h * 3 / 4, w, h / 4);
                g2.setColor(new Color(0xBFD9EC));                  // หน้าต่าง
                g2.fillRect(w / 2 - 50, 25, 100, 105);
                g2.setColor(new Color(0x8E8E8E));
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(w / 2 - 50, 25, 100, 105);
                g2.drawLine(w / 2, 25, w / 2, 130);
                g2.setColor(new Color(0xB98B5E));                  // หัวเตียง
                g2.fillRoundRect(25, 95, 110, 28, 6, 6);
                g2.setColor(new Color(0x7E9C83));                  // เตียง
                g2.fillRoundRect(20, 118, 125, 45, 8, 8);
                g2.setColor(Color.WHITE);                          // หมอน
                g2.fillRoundRect(32, 108, 40, 16, 8, 8);
                g2.fillRoundRect(84, 108, 40, 16, 8, 8);
                g2.setColor(new Color(0xC79A6B));                  // โต๊ะ
                g2.fillRect(170, 125, 80, 8);
                g2.fillRect(240, 133, 6, 40);
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

    /** ไอคอนเส้น (outline) วาดด้วย Java2D บนกริด 24x24 */
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
                case DOC: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(5, 2); p.lineTo(14, 2); p.lineTo(19, 7); p.lineTo(19, 22); p.lineTo(5, 22); p.closePath();
                    g2.draw(p);
                    g2.draw(new Line2D.Double(8.5, 11, 15.5, 11));
                    g2.draw(new Line2D.Double(8.5, 15, 15.5, 15));
                    g2.draw(new Line2D.Double(8.5, 19, 12.5, 19));
                    break;
                }
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
                case PIN: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(12, 22);
                    p.curveTo(4, 14, 4, 3, 12, 3);
                    p.curveTo(20, 3, 20, 14, 12, 22);
                    g2.draw(p);
                    g2.draw(new Ellipse2D.Double(9, 7.5, 6, 6));
                    break;
                }
                case CALENDAR:
                    g2.draw(new RoundRectangle2D.Double(3, 5, 18, 16, 3, 3));
                    g2.draw(new Line2D.Double(3, 10, 21, 10));
                    g2.draw(new Line2D.Double(8, 3, 8, 7));
                    g2.draw(new Line2D.Double(16, 3, 16, 7));
                    g2.draw(new Line2D.Double(7, 14, 17, 14));
                    g2.draw(new Line2D.Double(7, 17.5, 13, 17.5));
                    break;
                case RECEIPT: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(10, 21); p.lineTo(8, 19.5); p.lineTo(6, 21); p.lineTo(4, 19.5);
                    p.lineTo(4, 3); p.lineTo(17, 3); p.lineTo(17, 9);
                    g2.draw(p);
                    g2.draw(new Line2D.Double(7, 7, 14, 7));
                    g2.draw(new Line2D.Double(7, 10.5, 12, 10.5));
                    g2.draw(new Ellipse2D.Double(11, 11, 10, 10));
                    g2.draw(new Line2D.Double(16, 13.5, 16, 16));
                    g2.draw(new Line2D.Double(16, 16, 18, 17.5));
                    break;
                }
            }
            g2.dispose();
        }
    }
}