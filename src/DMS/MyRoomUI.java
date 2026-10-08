package DMS;
import javax.swing.*;
import java.awt.*;

public class MyRoomUI extends JFrame {
    public MyRoomUI() {
        setTitle("KU Dormitory - My Room");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("ห้องพักของฉัน", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(label);
    }
}