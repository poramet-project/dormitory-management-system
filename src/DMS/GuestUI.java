package DMS;

import javax.swing.*;
import java.awt.*;

public class GuestUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private GuestSearchPanel searchPanel;
    private GuestDetailPanel detailPanel;
    private GuestContactPanel contactPanel;

    public GuestUI() {
        setTitle("KU Dormitory - ระบบค้นหาและจองห้องพัก");
        setSize(1180, 750);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. แถบนำทางด้านบน (Top Navigation Bar)
        add(createTopNavBar(), BorderLayout.NORTH);

        // 2. CardLayout สำหรับสลับหน้า
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        searchPanel = new GuestSearchPanel(this);
        detailPanel = new GuestDetailPanel(this);
        contactPanel = new GuestContactPanel();

        contentPanel.add(searchPanel, "SEARCH");
        contentPanel.add(detailPanel, "DETAIL");
        contentPanel.add(contactPanel, "CONTACT");

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTopNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 230, 235)));
        nav.setPreferredSize(new Dimension(1180, 65));

        // โลโก้ KU Dormitory
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        logoPanel.setOpaque(false);
        JLabel logoBadge = new JLabel("KU", SwingConstants.CENTER);
        logoBadge.setOpaque(true);
        logoBadge.setBackground(new Color(14, 121, 115));
        logoBadge.setForeground(Color.WHITE);
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoBadge.setPreferredSize(new Dimension(36, 36));

        JLabel title = new JLabel("KU Dormitory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(23, 32, 53));
        logoPanel.add(logoBadge);
        logoPanel.add(title);

        // ปุ่มเมนู
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 16));
        menuPanel.setOpaque(false);

        JButton btnSearch = createNavButton("ค้นหาห้องพัก");
        JButton btnContact = createNavButton("ติดต่อเรา");
        JButton btnLogin = createNavButton("เข้าสู่ระบบ");
        JButton btnSignUp = new JButton("สมัครสมาชิก");

        btnSignUp.setFont(new Font("Tahoma", Font.BOLD, 13));
        btnSignUp.setForeground(Color.WHITE);
        btnSignUp.setBackground(new Color(14, 121, 115));
        btnSignUp.setFocusPainted(false);
        btnSignUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnSearch.addActionListener(e -> showSearchPage());
        btnContact.addActionListener(e -> cardLayout.show(contentPanel, "CONTACT"));
        btnLogin.addActionListener(e -> {
            dispose();
            new LoginUI().setVisible(true);
        });
        btnSignUp.addActionListener(e -> {
            dispose();
            new SignUpUI().setVisible(true);
        });

        menuPanel.add(btnSearch);
        menuPanel.add(btnContact);
        menuPanel.add(btnLogin);
        menuPanel.add(btnSignUp);

        nav.add(logoPanel, BorderLayout.WEST);
        nav.add(menuPanel, BorderLayout.EAST);
        return nav;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Tahoma", Font.PLAIN, 13));
        btn.setForeground(new Color(81, 97, 123));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void showSearchPage() {
        searchPanel.reloadRooms();
        cardLayout.show(contentPanel, "SEARCH");
    }

    public void showDetailPage(RoomData room) {
        detailPanel.loadRoomDetails(room);
        cardLayout.show(contentPanel, "DETAIL");
    }

    public static class RoomData {
        public final String id, floor, type, status, rent, occupant;
        public RoomData(String id, String floor, String type, String status, String rent, String occupant) {
            this.id = id;
            this.floor = floor;
            this.type = type;
            this.status = status;
            this.rent = rent;
            this.occupant = occupant;
        }
        public boolean isAvailable() {
            return "AVAILABLE".equalsIgnoreCase(status);
        }
    }
}