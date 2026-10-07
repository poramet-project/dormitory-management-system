package DMS;

import javax.swing.*;
import java.awt.*;

public class SignUpUI extends JFrame {
    public SignUpUI() {
        setTitle("KU Dormitory - Sign Up");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Welcome to Sign Up Page!", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(label);
    }
}