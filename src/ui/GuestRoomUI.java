package ui;

import logic.GuestRoomLogic;
import logic.ImageLoader;
import logic.LoginLogic;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * GuestRoomUI คือคลาสหน้าจอแสดงรายละเอียดของห้องพักที่เลือกและฟอร์มยื่นคำขอจองห้องพัก
 * logic คือ อ็อบเจกต์ตรรกะสำหรับจัดการข้อมูลห้องพักและการจองห้องพัก
 * mainUI คือ อ็อบเจกต์หน้าต่างหลัก GuestUI สำหรับการสลับมุมมองหน้าจอกลับ
 */
public class GuestRoomUI extends JPanel {

    // ===== representation =====
    // Abstraction Function:
    // AF(logic, mainUI) คือ หน้าจอแสดงรายละเอียดห้องพัก อัตราค่าเช่า สิ่งอำนวยความสะดวก และปุ่มจองห้องพัก
    // RI
    // 1. logic ต้องไม่เป็น null

    private final GuestRoomLogic logic;
    private final GuestUI mainUI;

    /**
     * คอนสตรัคเตอร์เริ่มต้นสำหรับสร้างหน้าจอแสดงรายละเอียดห้องพัก (ใช้ห้อง 101 เป็นค่าเริ่มต้น)
     * 
     * @param mainUI หน้าต่างหลัก GuestUI
     */
    public GuestRoomUI(GuestUI mainUI) {
        this(mainUI, "ห้อง 101", "ห้องพัดลม", "2,800", "ว่าง");
    }

    /**
     * คอนสตรัคเตอร์สำหรับสร้างหน้าจอแสดงรายละเอียดห้องพักตามข้อมูลห้องที่ระบุ
     * 
     * @param mainUI หน้าต่างหลัก GuestUI
     * @param roomName ชื่อหรือหมายเลขห้องพัก
     * @param roomType ประเภทห้องพัก
     * @param roomPrice อัตราค่าเช่า
     * @param roomStatus สถานะความพร้อมของห้องพัก
     */
    public GuestRoomUI(GuestUI mainUI, String roomName, String roomType, String roomPrice, String roomStatus) {
        this.mainUI = mainUI;
        this.logic = new GuestRoomLogic(roomName, roomType, roomPrice, roomStatus);

        setLayout(null);
        setBackground(GuestUI.BG);

        // --- Navbar ---
        JPanel nav = new JPanel(null);
        nav.setBackground(GuestUI.WHITE);
        nav.setBounds(0, 0, 1100, 56);
        nav.setBorder(new MatteBorder(0, 0, 1, 0, GuestUI.BORDER));

        JButton btnBack = GuestUI.flatButton("← ย้อนกลับ", false);
        btnBack.setBounds(40, 12, 100, 32);
        btnBack.addActionListener(e -> {
            if (this.mainUI != null) {
                this.mainUI.showListView();
            }
        });
        nav.add(btnBack);

        JLabel brand = GuestUI.label("รายละเอียดห้องพัก", Font.BOLD, 16, GuestUI.TEXT);
        brand.setBounds(160, 14, 250, 26);
        nav.add(brand);

        LoginLogic.User cu = logic.getCurrentUser();
        if (cu != null) {
            String shown = (cu.fullName == null || cu.fullName.isEmpty()) ? cu.username : cu.fullName;
            JLabel lblName = GuestUI.label(shown, Font.BOLD, 13, GuestUI.TEXT);
            lblName.setHorizontalAlignment(SwingConstants.RIGHT);
            lblName.setBounds(780, 15, 280, 26);
            nav.add(lblName);
        }
        add(nav);

        // --- การ์ดบน: แสดงรูปภาพและข้อมูลห้อง ---
        GuestUI.ShadowCardPanel topCard = new GuestUI.ShadowCardPanel(GuestUI.WHITE, GuestUI.BORDER, 12);
        topCard.setLayout(null);
        topCard.setBounds(70, 76, 960, 190);

        String roomNumOnly = logic.getRoomNumberOnly();

        // 🟢 ค้นหาไฟล์รูปภาพเฉพาะห้อง เช่น img/room_101.png หรือ .jpg หากไม่มีจะแสดงกรอบว่างๆ
        RoomImage bigImg = new RoomImage(new String[] { "img/room_" + roomNumOnly + ".png", "img/room_" + roomNumOnly + ".jpg" }, 240, 158);
        bigImg.setBounds(16, 16, 240, 158);
        topCard.add(bigImg);

        JLabel lblTitle = GuestUI.label("ห้องพักหมายเลข " + roomNumOnly, Font.BOLD, 22, GuestUI.TEXT);
        lblTitle.setBounds(280, 20, 400, 28);
        topCard.add(lblTitle);

        // Badge ประเภทห้อง
        JPanel badgeWrap = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(204, 251, 241));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badgeWrap.setOpaque(false);
        badgeWrap.setBounds(280, 56, 130, 26);
        badgeWrap.add(GuestUI.label(logic.getRoomType(), Font.BOLD, 12, GuestUI.PRIMARY));
        topCard.add(badgeWrap);

        JLabel lblSubInfo = GuestUI.label(
                "อาคารหอพักนักศึกษา  •  ชั้น " + logic.getFloorChar() + "  •  สถานะห้อง: " + logic.getRoomStatus(),
                Font.PLAIN, 13, GuestUI.TEXT_GRAY);
        lblSubInfo.setBounds(280, 96, 600, 20);
        topCard.add(lblSubInfo);

        JLabel lblPrice = GuestUI.label("อัตราค่าเช่า: " + logic.getRoomPrice() + " / เดือน", Font.BOLD, 18,
                GuestUI.PRIMARY);
        lblPrice.setBounds(280, 128, 400, 26);
        topCard.add(lblPrice);

        add(topCard);

        // --- การ์ดล่าง: เฟอร์นิเจอร์ ---
        GuestUI.ShadowCardPanel bottomCard = new GuestUI.ShadowCardPanel(GuestUI.WHITE, GuestUI.BORDER, 12);
        bottomCard.setLayout(null);
        bottomCard.setBounds(70, 280, 960, 210);

        JLabel furnTitle = GuestUI.label("รายการเฟอร์นิเจอร์และอุปกรณ์ประจำห้อง", Font.BOLD, 15, GuestUI.TEXT);
        furnTitle.setBounds(24, 20, 400, 22);
        bottomCard.add(furnTitle);

        String[] items = logic.getFurnitureList();

        for (int i = 0; i < items.length; i++) {
            int col = i / 4;
            int row = i % 4;
            int fx = col == 0 ? 30 : 500;
            int fy = 56 + (row * 34);

            JLabel itemLbl = GuestUI.label(items[i], Font.PLAIN, 13, new Color(51, 65, 85));
            itemLbl.setBounds(fx, fy, 420, 24);
            bottomCard.add(itemLbl);
        }

        add(bottomCard);

        // --- ปุ่มกดจองห้องพัก ---
        JButton btnBook = GuestUI.flatButton("จองห้องพักนี้", true);
        btnBook.setBounds(70, 508, 960, 46);

        Font baseFont = UIManager.getFont("Button.font");
        if (baseFont != null) {
            btnBook.setFont(baseFont.deriveFont(Font.BOLD, 14f));
        }

        if (logic.isPendingByCurrentUser()) {
            btnBook.setEnabled(false);
            btnBook.setText("รอการอนุมัติการจองห้องพักนี้");
            btnBook.setBackground(new Color(254, 240, 138));
            btnBook.setForeground(new Color(133, 77, 14));
        } else if (!logic.isAvailable()) {
            btnBook.setEnabled(false);
            if (logic.getRoomStatus() != null && logic.getRoomStatus().contains("ปรับปรุง")) {
                btnBook.setText("ห้องนี้ปิดปรับปรุง (ไม่สามารถจองได้)");
            } else {
                btnBook.setText("ห้องนี้ไม่ว่าง");
            }
            btnBook.setBackground(new Color(203, 213, 225));
            btnBook.setForeground(new Color(100, 116, 139));
        }

        btnBook.addActionListener(e -> {
            LoginLogic.User user = logic.getCurrentUser();

            if (user == null) {
                int res = JOptionPane.showConfirmDialog(this,
                        "กรุณาเข้าสู่ระบบก่อนทำการจองห้องพัก\nต้องการไปที่หน้าเข้าสู่ระบบเพื่อดำเนินการต่อหรือไม่?",
                        "แจ้งเตือนการจองห้อง", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    LoginUI login = new LoginUI();
                    if (this.mainUI != null) {
                        login.setLocation(this.mainUI.getLocation());
                        this.mainUI.dispose();
                    }
                    login.setVisible(true);
                }
                return;
            }

            GuestRoomLogic.BookResult result = logic.bookRoom(user);
            switch (result) {
                case OK:
                    JOptionPane.showMessageDialog(this,
                            "ส่งคำขอจองห้องพักหมายเลข " + roomNumOnly
                                    + " เรียบร้อยแล้ว\nกรุณารอผู้ดูแลระบบ (Admin) อนุมัติ",
                            "รออนุมัติ", JOptionPane.INFORMATION_MESSAGE);
                    if (this.mainUI != null) {
                        this.mainUI.showListView();
                    }
                    break;
                case PENDING_EXISTS:
                    JOptionPane.showMessageDialog(this,
                            "คุณมีคำขอจองที่รอการอนุมัติอยู่แล้ว\nกรุณารอ Admin พิจารณาก่อนจองห้องอื่น",
                            "ไม่สามารถจองได้", JOptionPane.WARNING_MESSAGE);
                    break;
                case ALREADY_HAS_ROOM:
                    JOptionPane.showMessageDialog(this, "บัญชีของคุณมีห้องพักอยู่แล้ว ไม่สามารถจองเพิ่มได้",
                            "ไม่สามารถจองได้", JOptionPane.WARNING_MESSAGE);
                    break;
                case ROOM_NOT_AVAILABLE:
                    JOptionPane.showMessageDialog(this, "ขออภัย ห้องนี้ไม่ว่างแล้ว",
                            "ไม่สามารถจองได้", JOptionPane.WARNING_MESSAGE);
                    break;
                case ROOM_NOT_FOUND:
                    JOptionPane.showMessageDialog(this, "ไม่พบข้อมูลห้องในระบบ",
                            "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "เกิดข้อผิดพลาดในการบันทึกข้อมูลการจอง",
                            "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
            }
        });
        add(btnBook);
    }

    /**
     * RoomImage คือคอมโพเนนต์สำหรับแสดงรูปภาพห้องพักขนาดใหญ่ในหน้ารายละเอียด
     * photo คือ อ็อบเจกต์รูปภาพห้องพักที่โหลดมาแสดงผล
     */
    static class RoomImage extends JPanel {
        // ===== representation =====
        // Abstraction Function:
        // AF(photo) คือ พาเนลแสดงรูปภาพห้องพัก หรือแสดงกรอบว่างพร้อมสัญลักษณ์เมื่อไม่มีภาพ
        // RI
        // - photo อาจเป็น null ได้หากไม่มีไฟล์รูปภาพ

        private final Image photo;

        /**
         * สร้างคอมโพเนนต์สำหรับแสดงรูปภาพห้องพักตามขนาดที่กำหนด
         * 
         * @param paths รายการพาธของไฟล์รูปภาพที่ต้องการค้นหาและโหลด
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
         * วาดรูปภาพห้องพักขอบโค้งมน หรือวาดไอคอนรูปกล้องและข้อความแจ้งเตือนเมื่อไม่มีภาพ
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
                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

                if (photo != null) {
                    g2.drawImage(photo, 0, 0, w, h, this);
                } else {
                    // 🟢 ทำเป็นกรอบว่างๆ ขนาดใหญ่สำหรับหน้ารายละเอียดห้อง
                    g2.setColor(new Color(248, 250, 252));
                    g2.fillRect(0, 0, w, h);

                    // วาดเส้นขอบประ
                    float[] dash = { 6.0f, 4.0f };
                    g2.setStroke(
                            new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                    g2.setColor(new Color(203, 213, 225));
                    g2.drawRoundRect(1, 1, w - 3, h - 3, 12, 12);

                    // วาดไอคอนกล้อง
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.setColor(new Color(148, 163, 184));
                    int cx = w / 2, cy = h / 2 - 10;
                    g2.drawRoundRect(cx - 20, cy - 14, 40, 28, 6, 6);
                    g2.drawOval(cx - 7, cy - 7, 14, 14);

                    // ข้อความระบุ
                    Font baseFont = UIManager.getFont("Label.font");
                    if (baseFont != null) {
                        g2.setFont(baseFont.deriveFont(Font.PLAIN, 12f));
                    }
                    String msg = "ไม่มีรูปภาพประกอบห้องพัก";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, cy + 30);
                }
            }
            g2.dispose();
        }
    }
}