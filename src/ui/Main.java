package ui;

import javax.swing.SwingUtilities;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;

/**
 * Main คือคลาสหลักที่เป็นจุดเริ่มต้นการทำงานของโปรแกรม (Entry Point)
 */
public class Main {

    // ===== representation =====
    // Abstraction Function:
    // AF() คือ จุดเริ่มต้นในการตั้งค่าสภาพแวดล้อม ฟอนต์ระบบ และเปิดหน้าจอ LoginUI
    // RI
    // - ไม่มีตัวแปรสถานะภายใน

    /**
     * เมธอดหลักสำหรับเริ่มต้นการทำงานของแอปพลิเคชัน ตั้งค่าแบบอักษรภาษาไทย
     * และเปิดหน้าจอ LoginUI
     * 
     * @param args อาร์กิวเมนต์จากบรรทัดคำสั่ง
     */
    public static void main(String[] args) {
        // ตั้งค่า Font ภาษาไทย (Tahoma) ให้ Component ทุกชิ้นในระบบ Java Swing
        setUIFont(new FontUIResource("Tahoma", Font.PLAIN, 13));
        SwingUtilities.invokeLater(LoginUI::new);
    }

    /**
     * ตั้งค่าแบบอักษรเริ่มต้น (Default Font) ให้กับคอมโพเนนต์ Swing ทุกชนิดในระบบ
     * 
     * @param f แบบอักษร FontUIResource ที่ต้องการกำหนด
     */
    private static void setUIFont(FontUIResource f) {
        UIComponents.setUIFont(f);
    }
}