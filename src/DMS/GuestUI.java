package DMS;

import javax.swing.*;
import java.awt.*;

public class GuestUI extends JFrame {
    public GuestUI() {
        setTitle("KU Dormitory - Tenant Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Welcome Guest", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(label);
    }
}
