package DMS;
import javax.swing.*;
import javax.swing.border.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

/** Two native Swing screens. Java 8+, no browser or server required. */
public class GuestUI extends JFrame {
    static final Color BG = new Color(247, 249, 251), TEAL = new Color(14, 121, 115), INK = new Color(23, 32, 53),
            MUTED = new Color(81, 97, 123), LINE = new Color(223, 231, 241), LIGHT = new Color(147, 166, 194);
    static final Font BASE = new Font("Tahoma", Font.PLAIN, 14);
    static final Room[] ROOMS = { new Room(102, "ห้องพัดลม", 1, 22, 2800, true, 63),
            new Room(205, "ห้องแอร์", 2, 24, 3500, true, 354), new Room(301, "ห้องแอร์", 3, 24, 3500, false, 646),
            new Room(410, "ห้องวีไอพี (ห้องมุม)", 4, 30, 4500, true, 938) };

    static class Room {
        final int id, floor, size, rent, x;
        final String type;
        final boolean available;

        Room(int id, String type, int floor, int size, int rent, boolean available, int x) {
            this.id = id;
            this.type = type;
            this.floor = floor;
            this.size = size;
            this.rent = rent;
            this.available = available;
            this.x = x;
        }
    }

    static String money(int n) {
        return String.format(Locale.US, "%,d", n);
    }

    static BufferedImage load(String name) throws IOException {
        InputStream stream = GuestUI.class.getResourceAsStream("/assets/" + name);
        if (stream == null) {
            // IDEs may compile only src: locate assets from the project or output folder.
            File[] roots = { new File(System.getProperty("user.dir")), classDirectory() };
            for (File root : roots) {
                for (int depth = 0; root != null && depth < 8; depth++, root = root.getParentFile()) {
                    File asset = new File(new File(root, "assets"), name);
                    if (asset.isFile()) {
                        stream = new FileInputStream(asset);
                        break;
                    }
                }
                if (stream != null)
                    break;
            }
        }
        if (stream == null)
            throw new FileNotFoundException("Missing asset: " + name);
        try (InputStream in = stream) {
            BufferedImage image = ImageIO.read(in);
            if (image == null)
                throw new IOException("Invalid image: " + name);
            return image;
        }
    }

    static File classDirectory() {
        try {
            return new File(GuestUI.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            return null;
        }
    }

    public static void launch(String[] args) throws Exception {
        if (args.length > 0 && "--self-test".equals(args[0])) {
            selfTest();
            return;
        }
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        for (Object key : new ArrayList<Object>(UIManager.getDefaults().keySet()))
            if (UIManager.get(key) instanceof Font)
                UIManager.put(key, BASE);
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("KUGuest — KU Dormitory");
                View view = new View();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                JScrollPane scroll = new JScrollPane(view, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scroll.setBorder(null);
                scroll.getVerticalScrollBar().setUnitIncrement(24);
                frame.add(scroll);
                frame.setSize(1280, 875);
                frame.setMinimumSize(new Dimension(980, 720));
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "เปิดโปรแกรมไม่ได้: " + e.getMessage(), "KUGuest",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    static List<Room> filter(String query, int type, int maxPrice) {
        boolean floorOnly = query.trim().startsWith("ชั้น");
        String q = query.trim().replace("ชั้น", "").trim();
        List<Room> result = new ArrayList<Room>();
        for (Room r : ROOMS)
            if ((q.isEmpty() || (!floorOnly && String.valueOf(r.id).contains(q)) || String.valueOf(r.floor).equals(q))
                    && (type == 0 || (type == 1 && r.id == 102) || (type == 2 && (r.id == 205 || r.id == 301))
                            || (type == 3 && r.id == 410))
                    && (maxPrice == 0 || r.rent <= maxPrice))
                result.add(r);
        return result;
    }

    static String validateBooking(String name, String phone, String email, String date) {
        if (name.trim().length() < 3)
            return "กรุณากรอกชื่อ-นามสกุล";
        String digits = phone.replaceAll("[\\s()+-]", "");
        if (!digits.matches("[0-9]{9,15}"))
            return "กรุณากรอกเบอร์โทรศัพท์ให้ถูกต้อง";
        if (!email.trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            return "กรุณากรอกอีเมลให้ถูกต้อง";
        try {
            if (LocalDate.parse(date.trim()).isBefore(LocalDate.now()))
                return "วันที่เข้าอยู่ต้องเป็นวันนี้หรือวันถัดไป";
        } catch (DateTimeParseException e) {
            return "กรุณากรอกวันที่รูปแบบ YYYY-MM-DD เช่น " + LocalDate.now();
        }
        return null;
    }

    static void check(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }

    static void selfTest() throws Exception {
        check(filter("", 0, 0).size() == 4, "All rooms");
        check(filter("ชั้น 2", 0, 0).get(0).id == 205, "Floor filter");
        check(filter("", 1, 3000).size() == 1, "Fan/price filter");
        check(filter("", 2, 3000).isEmpty(), "Empty results");
        check(validateBooking("Test Guest", "081-234-5678", "guest@example.com", LocalDate.now().toString()) == null,
                "Valid booking");
        check(validateBooking("", "0812345678", "guest@example.com", LocalDate.now().toString()) != null,
                "Missing name");
        check(validateBooking("Test Guest", "bad", "guest@example.com", LocalDate.now().toString()) != null,
                "Invalid phone");
        check(validateBooking("Test Guest", "0812345678", "bad", LocalDate.now().toString()) != null, "Invalid email");
        check(validateBooking("Test Guest", "0812345678", "guest@example.com", "2000-01-01") != null, "Past date");
        check(!ROOMS[2].available, "Full room");
        load("search-reference.png").getSubimage(938, 287, 269, 139);
        load("detail-reference.png").getSubimage(66, 257, 537, 192);
        SwingUtilities.invokeAndWait(() -> {
            try {
                View v = new View();
                v.setSize(1260, 840);
                v.doLayout();
                BufferedImage img = new BufferedImage(1260, 840, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                v.paint(g);
                g.dispose();
                ImageIO.write(img, "png", new File("build/search-preview.png"));
                v.openRoom(ROOMS[1]);
                v.doLayout();
                g = img.createGraphics();
                v.paint(g);
                g.dispose();
                ImageIO.write(img, "png", new File("build/detail-preview.png"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(
                "PASS: room filters, booking validation, full-room status, bundled assets and both Swing screens.");
    }

    static class View extends JPanel implements Scrollable {
        final BufferedImage source, detail;
        final Map<Component, Rectangle> positions = new LinkedHashMap<Component, Rectangle>();
        final JTextField query = new JTextField(), name = new JTextField(), phone = new JTextField(),
                email = new JTextField(), date = new JTextField(LocalDate.now().toString());
        final JTextArea message = new JTextArea();
        final JComboBox<String> types = new JComboBox<String>(
                new String[] { "ทั้งหมด", "ห้องพัดลม", "ห้องแอร์", "ห้องวีไอพี (ห้องมุม)" }),
                prices = new JComboBox<String>(new String[] { "ช่วงราคา ทั้งหมด", "ไม่เกิน 3,000 บาท",
                        "ไม่เกิน 4,000 บาท", "ไม่เกิน 5,000 บาท" });
        JButton navSearch, navDetail, search, back, book;
        final List<JButton> roomButtons = new ArrayList<JButton>();
        List<Room> visible = Arrays.asList(ROOMS);
        Room selected = ROOMS[1];
        boolean detailPage = false;
        double scale = 1;

        View() throws IOException {
            source = load("search-reference.png");
            detail = load("detail-reference.png");
            setLayout(null);
            setBackground(BG);
            addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    revalidate();
                    repaint();
                }
            });
            navSearch = button("ค้นหาห้องพัก", false, () -> openSearch());
            place(navSearch, 704, 21, 100, 31);
            navDetail = button("รายละเอียด & จองห้อง", false, () -> openRoom(selected));
            place(navDetail, 806, 21, 141, 31);
            place(button("ติดต่อเรา", false,
                    () -> notice("ติดต่อเรา", "ข้อมูลติดต่อหอพักจะเพิ่มเมื่อเชื่อมต่อระบบจริง")), 948, 21, 65, 31);
            place(button("เข้าสู่ระบบ", false, () -> notice("เข้าสู่ระบบ", "ระบบบัญชีผู้ใช้ยังอยู่ระหว่างเตรียมการ")),
                    1023, 20, 77, 32);
            place(button("สร้างบัญชี", true, () -> notice("สร้างบัญชี", "ระบบบัญชีผู้ใช้ยังอยู่ระหว่างเตรียมการ")),
                    1128, 20, 79, 32);
            query.setToolTipText("เลขห้องหรือชั้น เช่น 205 หรือ ชั้น 2");
            query.getAccessibleContext().setAccessibleName("เลขห้อง หรือชั้น");
            query.addActionListener(e -> applyFilter());
            types.getAccessibleContext().setAccessibleName("ประเภทห้องพัก");
            prices.getAccessibleContext().setAccessibleName("ช่วงราคาต่อเดือน");
            search = button("ค้นหา", true, () -> applyFilter());
            back = button("←  กลับไปหน้าค้นหา", false, () -> openSearch());
            book = button("ส่งคำขอจองห้องพัก", true, () -> submitBooking());
            for (int i = 0; i < 4; i++) {
                final int index = i;
                roomButtons.add(button("ดูรายละเอียด", false, () -> {
                    if (index < visible.size())
                        openRoom(visible.get(index));
                }));
            }
            for (JTextField field : Arrays.asList(name, phone, email, date)) {
                field.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(4, 10, 4, 10)));
            }
            name.getAccessibleContext().setAccessibleName("ชื่อ-นามสกุล");
            phone.getAccessibleContext().setAccessibleName("เบอร์โทรศัพท์");
            email.getAccessibleContext().setAccessibleName("อีเมล");
            date.getAccessibleContext().setAccessibleName("วันที่เข้าอยู่ YYYY-MM-DD");
            date.setToolTipText("วันที่เข้าอยู่ รูปแบบ YYYY-MM-DD");
            message.setLineWrap(true);
            message.setWrapStyleWord(true);
            message.getAccessibleContext().setAccessibleName("ข้อความเพิ่มเติม");
            message.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(6, 10, 6, 10)));
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int x = (int) (e.getX() / scale), y = (int) (e.getY() / scale);
                    if (!detailPage) {
                        if (y >= 287 && y <= 426) {
                            int i = (x - 63) / 291;
                            if (x >= 63 && i >= 0 && i < visible.size() && x < 63 + i * 291 + 270)
                                openRoom(visible.get(i));
                        }
                    } else if (x >= 66 && x <= 791 && y >= 257 && y <= 449) {
                        if (x < 603)
                            showPhoto(selected.id == 205 ? detail.getSubimage(66, 257, 537, 192) : roomImage(selected));
                        else if (y < 346)
                            showPhoto(detail.getSubimage(617, 257, 174, 89));
                        else
                            showPhoto(detail.getSubimage(617, 359, 174, 90));
                    }
                }
            });
            openSearch();
        }

        JButton button(String text, boolean filled, Runnable action) {
            JButton b = new JButton(text);
            // WindowsButtonUI ignores the requested background when visual styles are
            // enabled.
            // BasicButtonUI paints our colors consistently, including disabled and pressed
            // states.
            b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            b.setFocusPainted(true);
            b.setForeground(filled ? Color.WHITE : TEAL);
            b.setBackground(filled ? TEAL : Color.WHITE);
            b.setOpaque(true);
            b.setContentAreaFilled(true);
            b.setBorder(new CompoundBorder(new LineBorder(filled ? TEAL : LINE), new EmptyBorder(2, 4, 2, 4)));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> action.run());
            return b;
        }

        void place(Component c, int x, int y, int w, int h) {
            if (c.getParent() != this)
                add(c);
            positions.put(c, new Rectangle(x, y, w, h));
            c.setVisible(true);
        }

        void hideBody() {
            for (Component c : getComponents()) {
                Rectangle r = positions.get(c);
                if (r != null && r.y > 65)
                    c.setVisible(false);
            }
        }

        void openSearch() {
            detailPage = false;
            hideBody();
            place(query, 80, 211, 492, 35);
            place(types, 586, 211, 227, 35);
            place(prices, 827, 211, 226, 35);
            place(search, 1067, 211, 123, 38);
            layoutCards();
            refresh();
        }

        void layoutCards() {
            for (int i = 0; i < roomButtons.size(); i++) {
                JButton b = roomButtons.get(i);
                if (i < visible.size())
                    place(b, 80 + i * 291, 565, 236, 34);
                else
                    b.setVisible(false);
            }
        }

        void applyFilter() {
            int max = prices.getSelectedIndex() == 0 ? 0 : 2000 + prices.getSelectedIndex() * 1000;
            visible = filter(query.getText(), types.getSelectedIndex(), max);
            layoutCards();
            refresh();
        }

        void openRoom(Room room) {
            selected = room;
            detailPage = true;
            hideBody();
            place(back, 66, 94, 160, 26);
            place(name, 851, 218, 335, 35);
            place(phone, 851, 285, 335, 35);
            place(email, 851, 352, 335, 35);
            place(date, 851, 419, 335, 35);
            place(message, 851, 486, 335, 61);
            place(book, 851, 565, 335, 42);
            book.setEnabled(room.available);
            book.setText(room.available ? "ส่งคำขอจองห้องพัก" : "ห้องนี้เต็มแล้ว");
            refresh();
        }

        void refresh() {
            navSearch.setForeground(detailPage ? MUTED : TEAL);
            navDetail.setForeground(detailPage ? TEAL : MUTED);
            revalidate();
            repaint();
        }

        void submitBooking() {
            if (!selected.available)
                return;
            String error = validateBooking(name.getText(), phone.getText(), email.getText(), date.getText());
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "ตรวจสอบข้อมูล", JOptionPane.WARNING_MESSAGE);
                return;
            }
            notice("ตรวจข้อมูลเรียบร้อย",
                    "ข้อมูลสำหรับห้อง " + selected.id + " ถูกต้องแล้ว\nนี่เป็น UI ตัวอย่าง ยังไม่ได้ส่งคำขอจองจริง");
        }

        void notice(String title, String text) {
            JOptionPane.showMessageDialog(this, text, title, JOptionPane.INFORMATION_MESSAGE);
        }

        void showPhoto(BufferedImage image) {
            JOptionPane.showMessageDialog(this,
                    new JLabel(new ImageIcon(image.getScaledInstance(800,
                            (int) (800.0 * image.getHeight() / image.getWidth()), Image.SCALE_SMOOTH))),
                    "ภาพตัวอย่างห้อง " + selected.id, JOptionPane.PLAIN_MESSAGE);
        }

        BufferedImage roomImage(Room r) {
            return source.getSubimage(r.x, 287, 269, 139);
        }

        public Dimension getPreferredSize() {
            int width = getWidth() > 0 ? getWidth() : 1260;
            return new Dimension(1260, (int) Math.ceil(840 * width / 1260.0));
        }

        public void doLayout() {
            scale = getWidth() / 1260.0;
            for (Map.Entry<Component, Rectangle> entry : positions.entrySet()) {
                Rectangle r = entry.getValue();
                Component c = entry.getKey();
                c.setBounds((int) (r.x * scale), (int) (r.y * scale), (int) (r.width * scale),
                        (int) (r.height * scale));
                Font font = BASE.deriveFont((float) (13 * scale));
                if (!font.equals(c.getFont()))
                    c.setFont(font);
            }
        }

        protected void paintComponent(Graphics original) {
            super.paintComponent(original);
            Graphics2D g = (Graphics2D) original.create();
            g.scale(scale, scale);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 1260, 66);
            line(g, 0, 66, 1260, 66);
            g.setColor(TEAL);
            g.fillRoundRect(63, 21, 29, 29, 12, 12);
            text(g, "KU", 67, 42, 15, false, Color.WHITE);
            text(g, "KU Dormitory", 99, 42, 17, true, INK);
            if (detailPage)
                paintDetail(g);
            else
                paintSearch(g);
            g.dispose();
        }

        void text(Graphics2D g, String value, int x, int y, int size, boolean bold, Color color) {
            g.setFont(BASE.deriveFont(bold ? Font.BOLD : Font.PLAIN, (float) size));
            g.setColor(color);
            g.drawString(value, x, y);
        }

        void panel(Graphics2D g, int x, int y, int w, int h) {
            g.setColor(Color.WHITE);
            g.fillRoundRect(x, y, w, h, 20, 20);
            g.setColor(LINE);
            g.drawRoundRect(x, y, w, h, 20, 20);
        }

        void line(Graphics2D g, int x, int y, int endX, int endY) {
            g.setColor(LINE);
            g.drawLine(x, y, endX, endY);
        }

        void image(Graphics2D g, BufferedImage image, int x, int y, int w, int h) {
            Shape old = g.getClip();
            g.clip(new java.awt.geom.RoundRectangle2D.Double(x, y, w, h, 12, 12));
            g.drawImage(image, x, y, w, h, null);
            g.setClip(old);
        }

        void badge(Graphics2D g, Room r, int x, int y, boolean status) {
            String text = (status ? "สถานะ: " : "") + (r.available ? "ว่าง" : "เต็ม");
            int w = status ? 75 : 35;
            g.setColor(r.available ? new Color(205, 249, 230) : new Color(255, 226, 229));
            g.fillRoundRect(x, y, w, 23, 8, 8);
            text(g, text, x + 8, y + 16, 12, false, r.available ? new Color(0, 154, 112) : new Color(243, 69, 84));
        }

        void paintSearch(Graphics2D g) {
            text(g, "ค้นหาห้องพักว่าง", 63, 121, 28, true, INK);
            text(g, "เลือกห้องพักที่เหมาะกับไลฟ์สไตล์ของคุณ พร้อมเข้าอยู่วันนี้", 63, 149, 13, false, MUTED);
            panel(g, 63, 173, 1144, 92);
            text(g, "เลขห้อง หรือชั้น", 80, 204, 12, false, MUTED);
            text(g, "ประเภทห้องพัก", 586, 204, 12, false, MUTED);
            text(g, "ช่วงราคาต่อเดือน", 827, 204, 12, false, MUTED);
            if (visible.isEmpty()) {
                panel(g, 63, 287, 1144, 150);
                text(g, "ไม่พบห้องพักที่ตรงกับการค้นหา", 450, 348, 20, true, INK);
                text(g, "ลองเปลี่ยนเลขห้อง ประเภทห้อง หรือช่วงราคา", 454, 382, 14, false, MUTED);
            }
            for (int i = 0; i < visible.size(); i++) {
                Room r = visible.get(i);
                int x = 63 + i * 291;
                panel(g, x, 287, 270, 328);
                image(g, roomImage(r), x, 287, 270, 139);
                text(g, "ห้อง " + r.id, x + 17, 461, 17, true, INK);
                badge(g, r, x + 219, 443, false);
                text(g, r.type, x + 17, 492, 13, true, TEAL);
                text(g, "ชั้น " + r.floor + " • " + r.size + " ตร.ม.", x + 17, 511, 12, false, MUTED);
                text(g, money(r.rent), x + 29, 546, 21, true, INK);
                text(g, "/ เดือน", x + 98, 546, 12, false, LIGHT);
            }
        }

        void paintDetail(Graphics2D g) {
            Room r = selected;
            panel(g, 66, 134, 725, 102);
            text(g, "ห้อง " + r.id, 87, 181, 27, true, INK);
            text(g, "(" + r.type + ")", 215, 177, 15, true, TEAL);
            badge(g, r, 696, 159, true);
            text(g, "ห้องพักระดับมาตรฐานพร้อมเฟอร์นิเจอร์ครบชุด ตั้งอยู่บนชั้น " + r.floor + " เดินทางสะดวก เงียบสงบ",
                    87, 212, 13, false, MUTED);
            image(g, r.id == 205 ? detail.getSubimage(66, 257, 537, 192) : roomImage(r), 66, 257, 537, 192);
            image(g, detail.getSubimage(617, 257, 174, 89), 617, 257, 174, 89);
            image(g, detail.getSubimage(617, 359, 174, 90), 617, 359, 174, 90);
            panel(g, 66, 470, 725, 237);
            text(g, "รายละเอียดและสิ่งอำนวยความสะดวก", 87, 507, 17, true, INK);
            String[] labels = { "ขนาดห้อง", "ตำแหน่งชั้น", "อัตราค่าเช่า", "เงินมัดจำ/ประกัน" };
            String[] values = { r.size + " ตารางเมตร",
                    "ชั้น " + r.floor + (r.id == 205 || r.id == 410 ? " (ห้องหัวมุม)" : ""), money(r.rent) + " / เดือน",
                    money(r.rent * 2) };
            for (int i = 0; i < 4; i++) {
                text(g, labels[i], 87 + i * 176, 541, 12, false, LIGHT);
                text(g, values[i], 87 + i * 176, 562, 14, false, i == 2 ? TEAL : INK);
            }
            line(g, 87, 582, 770, 582);
            text(g, "สิ่งอำนวยความสะดวกภายในห้อง", 87, 613, 13, true, MUTED);
            String[] tags = { r.id == 102 ? "พัดลม" : "เครื่องปรับอากาศ", "เตียงนอน 5 ฟุต", "ตู้เสื้อผ้าบิวท์อิน",
                    "โต๊ะทำงาน & เก้าอี้", "เครื่องทำน้ำอุ่น", "ระเบียงส่วนตัว", "Wi-Fi ฟรีความเร็วสูง",
                    "กล้องวงจรปิดรปภ." };
            int tx = 87, ty = 627;
            for (String tag : tags) {
                g.setFont(BASE.deriveFont(12f));
                int tw = g.getFontMetrics().stringWidth(tag) + 35;
                if (tx + tw > 772) {
                    tx = 87;
                    ty += 33;
                }
                g.setColor(BG);
                g.fillRoundRect(tx, ty, tw, 27, 8, 8);
                g.setColor(TEAL);
                g.setStroke(new BasicStroke(1.8f));
                g.drawLine(tx + 9, ty + 14, tx + 12, ty + 17);
                g.drawLine(tx + 12, ty + 17, tx + 19, ty + 10);
                g.setStroke(new BasicStroke(1f));
                text(g, tag, tx + 27, ty + 18, 12, false, INK);
                tx += tw + 7;
            }
            panel(g, 826, 97, 384, 534);
            text(g, "ส่งคำขอจองห้องพัก", 851, 138, 18, true, INK);
            text(g, "กรอกข้อมูลให้เจ้าหน้าที่ติดต่อกลับโดยเร็ว", 851, 159, 13, false, MUTED);
            line(g, 851, 179, 1186, 179);
            String[] formLabels = { "ชื่อ-นามสกุล", "เบอร์โทรศัพท์", "อีเมล", "วันที่ต้องการเริ่มเข้าอยู่ (YYYY-MM-DD)",
                    "ข้อความเพิ่มเติม (ถ้ามี)" };
            for (int i = 0; i < formLabels.length; i++)
                text(g, formLabels[i], 851, 209 + i * 67, 13, false, MUTED);
        }

        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(1260, 840);
        }

        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 24;
        }

        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return r.height - 24;
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}