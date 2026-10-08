package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class AdminRoomsPanel extends JPanel {
    private JTable roomTable;
    private DefaultTableModel model;

    public AdminRoomsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // Title + Controls
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Room Management");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topPanel.add(titleLbl, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton addRoomBtn = new JButton("➕ Add Room");
        JButton changeStatusBtn = new JButton("🔄 Change Status");
        btnPanel.add(addRoomBtn);
        btnPanel.add(changeStatusBtn);
        topPanel.add(btnPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Room ID", "Floor", "Type", "Status", "Monthly Rent", "Occupant"};
        model = new DefaultTableModel(cols, 0);
        roomTable = new JTable(model);
        roomTable.setRowHeight(26);
        add(new JScrollPane(roomTable), BorderLayout.CENTER);

        loadRoomsData();
    }

    private void loadRoomsData() {
        model.setRowCount(0);
        File file = new File("rooms.csv");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                String[] c = line.split(",", -1);
                if (c.length >= 6) {
                    model.addRow(new Object[]{c[0].trim(), c[1].trim(), c[2].trim(), c[3].trim(), c[4].trim(), c[5].trim()});
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}