package DMS;

import javax.swing.*;
import java.awt.*;

public class ReportUI extends JFrame {
    public ReportUI() {
        setTitle("KU Dormitory - Report Issue");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("รายงาน", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(label);
    }
}