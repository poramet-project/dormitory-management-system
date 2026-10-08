package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class AdminDashboardPanel extends JPanel {
    private JLabel totalUsersLbl, tenantsLbl, availableRoomsLbl, pendingBookingsLbl;
    private JTable recentBookingsTable;
    private JTable recentTransfersTable;

    public AdminDashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // 1. Title Bar
        JLabel titleLbl = new JLabel("Admin Dashboard Overview");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(new Color(33, 43, 54));
        add(titleLbl, BorderLayout.NORTH);

        // 2. Center Panel: Stats Card + Tables
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Metric Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 15, 15));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(1920, 110));

        totalUsersLbl = new JLabel("0", SwingConstants.CENTER);
        tenantsLbl = new JLabel("0", SwingConstants.CENTER);
        availableRoomsLbl = new JLabel("0", SwingConstants.CENTER);
        pendingBookingsLbl = new JLabel("0", SwingConstants.CENTER);

        statsGrid.add(createStatCard("Total Users", totalUsersLbl, new Color(41, 128, 185)));
        statsGrid.add(createStatCard("Active Tenants", tenantsLbl, new Color(39, 174, 96)));
        statsGrid.add(createStatCard("Available Rooms", availableRoomsLbl, new Color(142, 68, 173)));
        statsGrid.add(createStatCard("Pending Bookings", pendingBookingsLbl, new Color(230, 126, 34)));

        centerPanel.add(statsGrid);
        centerPanel.add(Box.createVerticalStrut(20));

        // Recent Activity Section (Split Pane ระหว่าง Bookings กับ Transfers)
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        tablesPanel.setOpaque(false);

        // Recent Bookings
        JPanel bookingsWrapper = new JPanel(new BorderLayout());
        bookingsWrapper.setBackground(Color.WHITE);
        bookingsWrapper.setBorder(BorderFactory.createTitledBorder("Recent Bookings"));
        recentBookingsTable = new JTable(new DefaultTableModel(new String[]{"User", "Room", "Status"}, 0));
        bookingsWrapper.add(new JScrollPane(recentBookingsTable), BorderLayout.CENTER);
        tablesPanel.add(bookingsWrapper);

        // Recent Transfers
        JPanel transfersWrapper = new JPanel(new BorderLayout());
        transfersWrapper.setBackground(Color.WHITE);
        transfersWrapper.setBorder(BorderFactory.createTitledBorder("Recent Room Transfers"));
        recentTransfersTable = new JTable(new DefaultTableModel(new String[]{"User", "From", "To", "Status"}, 0));
        transfersWrapper.add(new JScrollPane(recentTransfersTable), BorderLayout.CENTER);
        tablesPanel.add(transfersWrapper);

        centerPanel.add(tablesPanel);
        add(centerPanel, BorderLayout.CENTER);

        loadDashboardData();
    }

    private JPanel createStatCard(String title, JLabel valueLbl, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 235), 1),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(Color.GRAY);

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    public void loadDashboardData() {
        // นับข้อมูลจากไฟล์ CSV
        int usersCount = 0;
        int tenantsCount = 0;
        File usersFile = new File("users.csv");
        if (usersFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
                String line;
                boolean header = true;
                while ((line = reader.readLine()) != null) {
                    if (header) { header = false; continue; }
                    String[] cols = line.split(",");
                    if (cols.length > 4) {
                        usersCount++;
                        if ("TENANT".equalsIgnoreCase(cols[4].trim())) tenantsCount++;
                    }
                }
            } catch (Exception ignored) {}
        }
        totalUsersLbl.setText(String.valueOf(usersCount));
        tenantsLbl.setText(String.valueOf(tenantsCount));

        // นับห้องว่าง
        int availRooms = 0;
        File roomsFile = new File("rooms.csv");
        if (roomsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(roomsFile))) {
                String line;
                boolean header = true;
                while ((line = reader.readLine()) != null) {
                    if (header) { header = false; continue; }
                    String[] cols = line.split(",");
                    if (cols.length > 3 && "AVAILABLE".equalsIgnoreCase(cols[3].trim())) {
                        availRooms++;
                    }
                }
            } catch (Exception ignored) {}
        }
        availableRoomsLbl.setText(String.valueOf(availRooms));

        // ดึงการจองล่าสุด
        DefaultTableModel bookModel = (DefaultTableModel) recentBookingsTable.getModel();
        bookModel.setRowCount(0);
        int pending = 0;
        File bookingsFile = new File("bookings.csv");
        if (bookingsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(bookingsFile))) {
                String line;
                boolean header = true;
                while ((line = reader.readLine()) != null) {
                    if (header) { header = false; continue; }
                    String[] cols = line.split(",");
                    if (cols.length >= 5) {
                        bookModel.addRow(new Object[]{cols[1].trim(), cols[2].trim(), cols[4].trim()});
                        if ("PENDING".equalsIgnoreCase(cols[4].trim())) pending++;
                    }
                }
            } catch (Exception ignored) {}
        }
        pendingBookingsLbl.setText(String.valueOf(pending));
    }
}