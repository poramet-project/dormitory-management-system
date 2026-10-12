package ui;

import logic.GuestRoomLogic;
import logic.ImageLoader;
import logic.LoginLogic;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GuestRoomUI extends JFrame {
    private final GuestRoomLogic logic;

    public GuestRoomUI() {
        this("ห้อง 101", "ห้องพัดลม", "2,800", "ว่าง");
    }

    public GuestRoomUI(String roomName, String roomType, String roomPrice, String roomStatus) {
        this.logic = new GuestRoomLogic(roomName, roomType, roomPrice, roomStatus);

        setTitle("KU Dormitory - รายละเอียด " + logic.getRoomName());
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(null);
        root.setBackground(AdminUI.BG);
        setContentPane(root);

        // --- Navbar ด้านบน ---
        JPanel nav = new JPanel(null);
        nav.setBackground(AdminUI.WHITE);
        nav.setBounds(0, 0, 1100, 56);
        nav.setBorder(new MatteBorder(0, 0, 1, 0, AdminUI.BORDER));

        JButton btnBack = new AdminManageRoomUI.FlatButton("← ย้อนกลับ", false);
        btnBack.setBounds(40, 12, 100, 32);
        btnBack.addActionListener(e -> {
            GuestUI guest = new GuestUI();
            guest.setLocation(getLocation());
            guest.setVisible(true);
            dispose();
        });
        nav.add(btnBack);

        JLabel brand = AdminUI.label("รายละเอียดห้องพัก", Font.BOLD, 16, AdminUI.TEXT);
        brand.setBounds(160, 14, 250, 26);
        nav.add(brand);

        LoginLogic.User cu = logic.getCurrentUser();
        if (cu != null) {
            String shown = (cu.fullName == null || cu.fullName.isEmpty()) ? cu.username : cu.fullName;
            JLabel lblName = AdminUI.label(shown, Font.BOLD, 13, AdminUI.TEXT);
            lblName.setHorizontalAlignment(SwingConstants.RIGHT);
            lblName.setBounds(780, 15, 280, 26);
            nav.add(lblName);
        }
        root.add(nav);

        // --- การ์ดบน: แสดงรูปภาพขนาดใหญ่และข้อมูลหลักของห้อง ---
        AdminUI.RoundedPanel topCard = new AdminUI.RoundedPanel(AdminUI.WHITE, AdminUI.BORDER, 12);
        topCard.setLayout(null);
        topCard.setBounds(70, 76, 960, 190);

        String roomNumOnly = logic.getRoomNumberOnly();
        RoomImage bigImg = new RoomImage("img/room_" + roomNumOnly + ".png", 240, 158);
        bigImg.setBounds(16, 16, 240, 158);
        topCard.add(bigImg);

        JLabel lblTitle = AdminUI.label("ห้องพักหมายเลข " + roomNumOnly, Font.BOLD, 20, AdminUI.TEXT);
        lblTitle.setBounds(280, 20, 400, 28);
        topCard.add(lblTitle);

        // Badge ประเภทห้อง
        JPanel badgeWrap = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(204, 251, 241));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badgeWrap.setOpaque(false);
        badgeWrap.setBounds(280, 56, 140, 26);
        badgeWrap.add(AdminUI.label(logic.getRoomType(), Font.BOLD, 12, AdminUI.PRIMARY));
        topCard.add(badgeWrap);

        JLabel lblSubInfo = AdminUI.label("อาคารหอพักนักศึกษา • ชั้น " + logic.getFloorChar() + " • สถานะห้อง: " + logic.getRoomStatus(), Font.PLAIN, 12, AdminUI.TEXT_GRAY);
        lblSubInfo.setBounds(280, 94, 600, 20);
        topCard.add(lblSubInfo);

        JLabel lblPrice = AdminUI.label("อัตราค่าเช่า: " + logic.getRoomPrice() + " / เดือน", Font.BOLD, 16, AdminUI.PRIMARY);
        lblPrice.setBounds(280, 128, 400, 24);
        topCard.add(lblPrice);

        root.add(topCard);

        // --- การ์ดล่าง: รายการเฟอร์นิเจอร์และอุปกรณ์ประจำห้อง ---
        AdminUI.RoundedPanel bottomCard = new AdminUI.RoundedPanel(AdminUI.WHITE, AdminUI.BORDER, 12);
        bottomCard.setLayout(null);
        bottomCard.setBounds(70, 280, 960, 210);

        JLabel furnTitle = AdminUI.label("รายการเฟอร์นิเจอร์และอุปกรณ์ประจำห้อง", Font.BOLD, 15, AdminUI.TEXT);
        furnTitle.setBounds(24, 20, 400, 22);
        bottomCard.add(furnTitle);

        String[] items = logic.getFurnitureList();

        for (int i = 0; i < items.length; i++) {
            int col = i / 4;
            int row = i % 4;
            int fx = col == 0 ? 30 : 500;
            int fy = 56 + (row * 34);

            JLabel itemLbl = AdminUI.label(items[i], Font.PLAIN, 13, new Color(55, 65, 81));
            itemLbl.setBounds(fx, fy, 420, 24);
            bottomCard.add(itemLbl);
        }

        root.add(bottomCard);

        // --- ปุ่มกดจองห้องพัก ---
        JButton btnBook = new AdminManageRoomUI.FlatButton("จองห้องพักนี้", true);
        btnBook.setBounds(70, 508, 960, 44);
        if (!logic.isAvailable()) {
            btnBook.setEnabled(false);
            btnBook.setText("ห้องนี้ไม่ว่าง");
        }
        btnBook.addActionListener(e -> {
            LoginLogic.User user = logic.getCurrentUser();

            if (user == null) {
                int res = JOptionPane.showConfirmDialog(this,
                        "กรุณาเข้าสู่ระบบก่อนทำการจองห้องพัก\nต้องการไปที่หน้าเข้าสู่ระบบเพื่อดำเนินการต่อหรือไม่?",
                        "แจ้งเตือนการจองห้อง", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    LoginUI login = new LoginUI();
                    login.setLocation(getLocation());
                    login.setVisible(true);
                    dispose();
                }
                return;
            }

            GuestRoomLogic.BookResult result = logic.bookRoom(user);
            switch (result) {
                case OK:
                    JOptionPane.showMessageDialog(this,
                            "ส่งคำขอจองห้องพักหมายเลข " + roomNumOnly + " เรียบร้อยแล้ว\nกรุณารอผู้ดูแลระบบ (Admin) อนุมัติ",
                            "รออนุมัติ", JOptionPane.INFORMATION_MESSAGE);
                    GuestUI guest = new GuestUI();
                    guest.setLocation(getLocation());
                    guest.setVisible(true);
                    dispose();
                    break;
                case PENDING_EXISTS:
                    JOptionPane.showMessageDialog(this, "คุณมีคำขอจองที่รอการอนุมัติอยู่แล้ว\nกรุณารอ Admin พิจารณาก่อนจองห้องอื่น",
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
        root.add(btnBook);

        setVisible(true);
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