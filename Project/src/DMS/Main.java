package DMS;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // ตั้งค่า Anti-Aliasing สำหรับฟอนต์
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // เรียกใช้ Class DormitoryLoginUI
        SwingUtilities.invokeLater(() -> {
           LoginUI loginFrame = new LoginUI();
            loginFrame.setVisible(true);
        });
    }
}