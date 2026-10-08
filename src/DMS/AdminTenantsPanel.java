package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class AdminTenantsPanel extends JPanel {
    private JTable tenantTable;
    private DefaultTableModel model;

    public AdminTenantsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Tenant & User Accounts");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topPanel.add(titleLbl, BorderLayout.WEST);

        JButton refreshBtn = new JButton("🔄 Refresh Data");
        refreshBtn.addActionListener(e -> loadUsersData());
        topPanel.add(refreshBtn, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Username", "Full Name", "Phone", "Role", "Assigned Room"};
        model = new DefaultTableModel(cols, 0);
        tenantTable = new JTable(model);
        tenantTable.setRowHeight(26);
        add(new JScrollPane(tenantTable), BorderLayout.CENTER);

        loadUsersData();
    }

    private void loadUsersData() {
        model.setRowCount(0);
        File file = new File("users.csv");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                String[] c = line.split(",", -1);
                if (c.length >= 6) {
                    model.addRow(new Object[]{c[0].trim(), c[2].trim(), c[3].trim(), c[4].trim(), c[5].trim()});
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}