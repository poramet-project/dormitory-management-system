package DMS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JPanel currentSelectedMenu;

    public AdminUI() {
        setTitle("KU Dormitory - Admin Dashboard");
        setSize(1180, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Sidebar ด้านซ้าย
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. CardLayout สำหรับสลับแต่ละหน้า
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        // เพิ่มแต่ละ Class หน้าลงในการ์ด
        mainContentPanel.add(new AdminDashboardPanel(), "DASHBOARD");
        mainContentPanel.add(new AdminRoomsPanel(), "ROOMS");
        mainContentPanel.add(new AdminTenantsPanel(), "TENANTS");
        mainContentPanel.add(new AdminBookingsPanel(), "BOOKINGS");
        mainContentPanel.add(new AdminTransfersPanel(), "TRANSFERS");
        mainContentPanel.add(new AdminAnnouncementsPanel(), "ANNOUNCEMENTS");

        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, 720));
        sidebar.setBackground(new Color(24, 38, 48));

        // Header โลโก้ KU Dormitory
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        brandPanel.setOpaque(false);
        JLabel logo = new JLabel("🏢  KU Dorm Admin");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logo.setForeground(Color.WHITE);
        brandPanel.add(logo);
        sidebar.add(brandPanel);
        sidebar.add(Box.createVerticalStrut(10));

        // เมนูต่าง ๆ เชื่อมโยงกับแต่ละ Panel Class
        addMenuItem(sidebar, "📊  Dashboard", "DASHBOARD", true);
        addMenuItem(sidebar, "🛏️  Rooms", "ROOMS", false);
        addMenuItem(sidebar, "👥  Tenants", "TENANTS", false);
        addMenuItem(sidebar, "📝  Bookings", "BOOKINGS", false);
        addMenuItem(sidebar, "🔄  Room Transfers", "TRANSFERS", false);
        addMenuItem(sidebar, "💳  Billing & Utilities", "BILLING", false);
        addMenuItem(sidebar, "📢  Announcements", "ANNOUNCEMENTS", false);

        sidebar.add(Box.createVerticalGlue());

        // ปุ่ม Logout ด้านล่างสุด
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        logoutPanel.setOpaque(false);
        JLabel logoutLabel = new JLabel("🚪  Logout");
        logoutLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutLabel.setForeground(new Color(240, 100, 100));
        logoutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LogicLogin.logout();
                dispose();
                new LoginUI().setVisible(true);
            }
        });
        logoutPanel.add(logoutLabel);
        sidebar.add(logoutPanel);

        return sidebar;
    }

    private void addMenuItem(JPanel sidebar, String title, String cardName, boolean isDefault) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        item.setMaximumSize(new Dimension(240, 48));
        item.setOpaque(true);
        item.setBackground(isDefault ? new Color(0, 121, 107) : new Color(24, 38, 48));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(Color.WHITE);
        item.add(lbl);

        if (isDefault) currentSelectedMenu = item;

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentSelectedMenu != null) {
                    currentSelectedMenu.setBackground(new Color(24, 38, 48));
                }
                item.setBackground(new Color(0, 121, 107));
                currentSelectedMenu = item;
                cardLayout.show(mainContentPanel, cardName);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (item != currentSelectedMenu) {
                    item.setBackground(new Color(36, 54, 68));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (item != currentSelectedMenu) {
                    item.setBackground(new Color(24, 38, 48));
                }
            }
        });

        sidebar.add(item);
    }
}