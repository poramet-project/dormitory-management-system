package DMS;

import javax.swing.*;
import java.awt.*;

public class AdminUI extends JFrame {
    public AdminUI() {
        setTitle("KU Dormitory - Admin Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Welcome Admin", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(label);
    }
}
