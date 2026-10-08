package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ReportUI extends JFrame {
    public ReportUI(LogicLogin.User user) {
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);
        setVisible(true);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel split = new JPanel(new GridLayout(1, 2, 18, 0));
        split.setOpaque(false);
        split.setMaximumSize(new Dimension(860, 480));

        // ฝั่งซ้าย: ประวัติการแจ้งซ่อม
        TenantUI.RoundedPanel historyCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        historyCard.setLayout(new BorderLayout());
        historyCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        historyCard.add(TenantUI.label("ประวัติการแจ้งซ่อม / ร้องเรียน", 16, Font.BOLD, TenantUI.TEXT), BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"วันที่", "หมวด", "เรื่อง", "สถานะ"}, 0);
        model.addRow(new Object[]{"02/10/69", "ไฟฟ้า", "หลอดไฟระเบียงดับ", "เสร็จสิ้น"});
        model.addRow(new Object[]{"08/10/69", "ประปา", "น้ำก๊อกห้องน้ำไหลช้า", "กำลังดำเนินการ"});

        JTable table = new JTable(model);
        table.setRowHeight(28);
        historyCard.add(new JScrollPane(table), BorderLayout.CENTER);
        split.add(historyCard);

        // ฝั่งขวา: ฟอร์มส่งเรื่องใหม่
        TenantUI.RoundedPanel formCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        formCard.add(TenantUI.label("แจ้งซ่อม / ร้องเรียนปัญหาใหม่", 16, Font.BOLD, TenantUI.TEXT));
        formCard.add(Box.createVerticalStrut(12));

        formCard.add(TenantUI.label("หมวดหมู่ปัญหา:", 13, Font.PLAIN, TenantUI.TEXT));
        JComboBox<String> catBox = new JComboBox<>(new String[]{"ระบบประปา / สุขาภิบาล", "ระบบไฟฟ้า / แสงสว่าง", "เครื่องปรับอากาศ", "เฟอร์นิเจอร์ / ประตูหน้าต่าง"});
        catBox.setMaximumSize(new Dimension(800, 32));
        formCard.add(catBox);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(TenantUI.label("หัวข้อปัญหา:", 13, Font.PLAIN, TenantUI.TEXT));
        JTextField topicField = new JTextField();
        topicField.setMaximumSize(new Dimension(800, 32));
        formCard.add(topicField);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(TenantUI.label("รายละเอียดเพิ่มเติม:", 13, Font.PLAIN, TenantUI.TEXT));
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        formCard.add(new JScrollPane(descArea));
        formCard.add(Box.createVerticalStrut(14));

        TenantUI.RoundButton submitBtn = new TenantUI.RoundButton("ส่งเรื่องแจ้งซ่อม");
        submitBtn.setMaximumSize(new Dimension(800, 36));
        submitBtn.addActionListener(e -> {
            if (topicField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "กรุณากรอกหัวข้อปัญหา", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }
            model.addRow(new Object[]{"วันนี้", catBox.getSelectedItem().toString().split(" ")[0], topicField.getText().trim(), "กำลังดำเนินการ"});
            JOptionPane.showMessageDialog(this, "ส่งเรื่องแจ้งซ่อมเรียบร้อย ช่างจะเข้าดำเนินการตรวจสอบ", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            topicField.setText("");
            descArea.setText("");
        });
        formCard.add(submitBtn);

        split.add(formCard);
        content.add(split);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }
}