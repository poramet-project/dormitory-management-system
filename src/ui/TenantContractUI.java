package ui;

import logic.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * TenantContractUI
 * คือคลาสหน้าจอแสดงข้อมูลสัญญาเช่าและข้อกำหนดเงื่อนไขของผู้เช่า
 * logic คือ อ็อบเจกต์ตรรกะสำหรับดึงและจัดรูปแบบข้อมูลสัญญาเช่า
 */
public class TenantContractUI extends JPanel {

    // ===== representation =====
    // Abstraction Function:
    // AF(logic) คือ หน้าจอแสดงแถบสรุปเลขที่สัญญา วันเริ่ม-สิ้นสุดสัญญา
    // และข้อความข้อกำหนดสัญญาเช่า
    // RI
    // 1. logic ต้องไม่เป็น null

    private final TenantContractLogic logic;

    /**
     * คอนสตรัคเตอร์สำหรับสร้างหน้าจอแสดงสัญญาเช่าของผู้ใช้งาน
     * 
     * @param user ข้อมูลผู้ใช้งานที่เข้าสู่ระบบ
     */
    public TenantContractUI(LoginLogic.User user) {
        this.logic = new TenantContractLogic(user);
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        // แถบสรุปสัญญา
        TenantUI.RoundedPanel summary = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        summary.setLayout(new GridLayout(1, 4, 10, 0));
        summary.setBorder(new EmptyBorder(14, 18, 14, 18));
        summary.setMaximumSize(new Dimension(860, 75));
        for (String[] cell : logic.summaryCells()) {
            summary.add(createCell(cell[0], cell[1]));
        }
        content.add(summary);
        content.add(Box.createVerticalStrut(18));

        // เนื้อหาข้อกำหนดสัญญา
        TenantUI.RoundedPanel docCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        docCard.setLayout(new BorderLayout());
        docCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        docCard.setMaximumSize(new Dimension(860, 420));

        docCard.add(TenantUI.label("ข้อกำหนดและเงื่อนไขการพักอาศัย", 16, Font.BOLD, TenantUI.TEXT), BorderLayout.NORTH);

        JTextArea terms = new JTextArea();
        terms.setEditable(false);
        terms.setFont(TenantUI.font(Font.PLAIN, 13));
        terms.setLineWrap(true);
        terms.setWrapStyleWord(true);
        terms.setText(logic.buildTerms());
        terms.setCaretPosition(0);
        docCard.add(new JScrollPane(terms), BorderLayout.CENTER);
        content.add(docCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * สร้างเซลล์แสดงหัวข้อและค่าสรุปข้อมูลสัญญา
     * 
     * @param t หัวข้อข้อมูล
     * @param v ค่าข้อมูล
     * @return JPanel เซลล์สรุปข้อมูล
     */
    private JPanel createCell(String t, String v) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        p.add(TenantUI.label(t, 12, Font.PLAIN, TenantUI.TEXT_MUTED));
        p.add(TenantUI.label(v, 14, Font.BOLD, TenantUI.TEAL));
        return p;
    }
}
