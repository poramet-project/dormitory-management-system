package ui;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) {
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
            if (value instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }
}