package DMS;

import javax.swing.*;
import java.awt.*;

public class RoomChangeRequestUI extends JFrame {
    public RoomChangeRequestUI() {
        setTitle("KU Dormitory - Room Change Request");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("ขอย้ายห้อง", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(label);
    }
}