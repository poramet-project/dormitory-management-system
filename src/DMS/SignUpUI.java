package DMS;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SignUpUI extends JFrame {
    private Image bgImg;

    public SignUpUI() {
        setTitle("KU Dormitory - Sign Up");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // 1. โหลดรูปภาพ
        loadBgImage();

        // 2. สร้าง Background Panel พร้อมวาดภาพพื้นหลัง
        JPanel bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                
                // วาดภาพพื้นหลัง
                if (bgImg != null) {
                    g2.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2.setColor(Color.decode("#1E1E1E"));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                // Overlay มืดเพิ่มมิติ
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        bgPanel.setLayout(new GridBagLayout());
        setContentPane(bgPanel);
      
    }

    private void loadBgImage() {
        String name = "content.png";
        
        // ค้นหาจาก ClassPath ก่อน
        java.net.URL url = getClass().getResource("/img/" + name);
        if (url == null) {
            url = getClass().getClassLoader().getResource("img/" + name);
        }

        if (url != null) {
            bgImg = new ImageIcon(url).getImage();
        } else {
            // Fallback ค้นหาจาก File System
            File f = new File("src/img/" + name);
            if (!f.exists()) f = new File("img/" + name);
            if (f.exists()) {
                bgImg = new ImageIcon(f.getAbsolutePath()).getImage();
            }
        }
    }
}