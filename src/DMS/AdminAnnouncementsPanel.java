package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminAnnouncementsPanel extends JPanel {
    public AdminAnnouncementsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel titleLbl = new JLabel("Dormitory Announcements");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(titleLbl, BorderLayout.NORTH);

        JTextArea announceArea = new JTextArea("Write a new announcement for all tenants here...");
        announceArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        announceArea.setBorder(BorderFactory.createLineBorder(new Color(200, 205, 210)));

        JButton postBtn = new JButton("📢 Post Announcement");

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(postBtn);

        add(new JScrollPane(announceArea), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}