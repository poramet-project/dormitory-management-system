package ui;
import logic.*;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

public class ReportUI extends JPanel {

    private DefaultTableModel tableModel;
    private final ReportLogic logic;
    private static final Font THAI_FONT = new Font("Tahoma", Font.PLAIN, 13);
    private static final Font THAI_FONT_BOLD = new Font("Tahoma", Font.BOLD, 13);

    public ReportUI(LogicLogin.User user) {
        this.logic = new ReportLogic(user);
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel split = new JPanel(new GridLayout(1, 2, 18, 0));
        split.setOpaque(false);
        split.setMaximumSize(new Dimension(860, 480));

        // ------------------ ฝั่งซ้าย: ประวัติการแจ้งซ่อม ------------------
        TenantUI.RoundedPanel historyCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        historyCard.setLayout(new BorderLayout());
        historyCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        historyCard.add(TenantUI.label("ประวัติการแจ้งซ่อม / ร้องเรียน", 16, Font.BOLD, TenantUI.TEXT), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"รหัส", "วันที่", "หมวด", "เรื่อง", "สถานะ"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(THAI_FONT);
        table.getTableHeader().setFont(THAI_FONT_BOLD);

        historyCard.add(new JScrollPane(table), BorderLayout.CENTER);
        split.add(historyCard);

        loadHistoryData();

        // ------------------ ฝั่งขวา: ฟอร์มส่งเรื่องใหม่ ------------------
        TenantUI.RoundedPanel formCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        formCard.add(TenantUI.label("แจ้งซ่อม / ร้องเรียนปัญหาใหม่", 16, Font.BOLD, TenantUI.TEXT));
        formCard.add(Box.createVerticalStrut(12));

        formCard.add(TenantUI.label("หมวดหมู่ปัญหา:", 13, Font.PLAIN, TenantUI.TEXT));
        JComboBox<String> catBox = new JComboBox<>(ReportLogic.CATEGORIES);
        catBox.setFont(THAI_FONT);
        catBox.setMaximumSize(new Dimension(800, 32));
        formCard.add(catBox);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(TenantUI.label("หัวข้อปัญหา:", 13, Font.PLAIN, TenantUI.TEXT));
        JTextField topicField = new JTextField();
        topicField.setFont(THAI_FONT);
        topicField.setMaximumSize(new Dimension(800, 32));
        formCard.add(topicField);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(TenantUI.label("รายละเอียดเพิ่มเติม:", 13, Font.PLAIN, TenantUI.TEXT));
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setFont(THAI_FONT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        formCard.add(new JScrollPane(descArea));
        formCard.add(Box.createVerticalStrut(14));

        TenantUI.RoundButton submitBtn = new TenantUI.RoundButton("ส่งเรื่องแจ้งซ่อม");
        submitBtn.setMaximumSize(new Dimension(800, 36));

        submitBtn.addActionListener(e -> {
            String topic = topicField.getText().trim();
            String desc = descArea.getText().trim();
            String category = (String) catBox.getSelectedItem();

            try {
                ReportLogic.Result r = logic.submit(category, topic, desc);
                switch (r.status) {
                    case EMPTY_TOPIC:
                        JOptionPane.showMessageDialog(this, "กรุณากรอกหัวข้อปัญหา", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                        return;
                    case NO_USER:
                        JOptionPane.showMessageDialog(this, "ไม่พบข้อมูลผู้ใช้งาน ไม่สามารถส่งเรื่องได้", "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
                        return;
                    default:
                        break;
                }

                JOptionPane.showMessageDialog(this,
                        "ส่งเรื่องแจ้งซ่อมเรียบร้อยแล้ว (รหัสคำขอ: " + r.complaintId + ")",
                        "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);

                topicField.setText("");
                descArea.setText("");
                loadHistoryData();

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "บันทึกข้อมูลไม่สำเร็จ: " + ex.getMessage(),
                        "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
            }
        });

        formCard.add(submitBtn);
        split.add(formCard);
        content.add(split);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadHistoryData() {
        tableModel.setRowCount(0);
        for (Object[] row : logic.historyRows()) {
            tableModel.addRow(row);
        }
    }
}
