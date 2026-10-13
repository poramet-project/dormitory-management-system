package ui;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) {
        // เปิด Anti-Aliasing ระดับ JVM เพื่อให้ฟอนต์ Tahoma ภาษาไทยคมชัด ไม่แตก
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // ตั้งค่า Font ภาษาไทย (Tahoma) ให้ Component ทุกชิ้นในระบบ Java Swing
        setUIFont(new FontUIResource("Tahoma", Font.PLAIN, 13));

        SwingUtilities.invokeLater(() -> {
            new LoginUI();
        });
    }

    private static void setUIFont(FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            
            // เช็คทั้ง FontUIResource และ Key ที่ลงท้ายด้วย .font เพื่อให้ครอบคลุมทุก Component ใน GuestUI
            if (value instanceof FontUIResource || (key != null && key.toString().endsWith(".font"))) {
                UIManager.put(key, f);
            }
        }
    }
}