package DMS;

import javax.swing.*;
import java.awt.*;

public class LeaseContractUI extends JFrame {
    public LeaseContractUI() {
        setTitle("KU Dormitory - Lease Contract");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("สัญญาเช่า", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(label);
    }
}