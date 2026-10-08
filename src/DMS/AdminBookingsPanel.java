package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class AdminBookingsPanel extends JPanel {
    private JTable bookingTable;
    private DefaultTableModel model;

    public AdminBookingsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Booking Requests");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topPanel.add(titleLbl, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);
        JButton approveBtn = new JButton("✅ Approve");
        JButton rejectBtn = new JButton("❌ Reject");
        actionBtns.add(approveBtn);
        actionBtns.add(rejectBtn);
        topPanel.add(actionBtns, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Booking ID", "Username", "Room ID", "Booking Date", "Status"};
        model = new DefaultTableModel(cols, 0);
        bookingTable = new JTable(model);
        bookingTable.setRowHeight(26);
        add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        loadBookings();
    }

    private void loadBookings() {
        model.setRowCount(0);
        File file = new File("bookings.csv");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                String[] c = line.split(",", -1);
                if (c.length >= 5) {
                    model.addRow(new Object[]{c[0].trim(), c[1].trim(), c[2].trim(), c[3].trim(), c[4].trim()});
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}