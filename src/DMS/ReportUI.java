package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ReportUI extends JPanel {

    private DefaultTableModel tableModel;
    private LogicLogin.User currentUser;
    private static final Font THAI_FONT = new Font("Tahoma", Font.PLAIN, 13);
    private static final Font THAI_FONT_BOLD = new Font("Tahoma", Font.BOLD, 13);

    public ReportUI(LogicLogin.User user) {
        this.currentUser = user;
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
        
        // **ตั้งค่า Font สำหรับ JTable และ Header ให้รองรับภาษาไทย**
        table.setFont(THAI_FONT);
        table.getTableHeader().setFont(THAI_FONT_BOLD);

        historyCard.add(new JScrollPane(table), BorderLayout.CENTER);
        split.add(historyCard);

        // โหลดข้อมูลประวัติจาก complaints.csv
        loadHistoryData();

        // ------------------ ฝั่งขวา: ฟอร์มส่งเรื่องใหม่ ------------------
        TenantUI.RoundedPanel formCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        formCard.add(TenantUI.label("แจ้งซ่อม / ร้องเรียนปัญหาใหม่", 16, Font.BOLD, TenantUI.TEXT));
        formCard.add(Box.createVerticalStrut(12));

        formCard.add(TenantUI.label("หมวดหมู่ปัญหา:", 13, Font.PLAIN, TenantUI.TEXT));
        JComboBox<String> catBox = new JComboBox<>(new String[]{
                "ระบบประปา / สุขาภิบาล",
                "ระบบไฟฟ้า / แสงสว่าง",
                "เครื่องปรับอากาศ / พัดลม",
                "เฟอร์นิเจอร์ / ประตูหน้าต่าง"
        });
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

            if (topic.isEmpty()) {
                JOptionPane.showMessageDialog(this, "กรุณากรอกหัวข้อปัญหา", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (currentUser == null || DataStore.blank(currentUser.userId)) {
                JOptionPane.showMessageDialog(this, "ไม่พบข้อมูลผู้ใช้งาน ไม่สามารถส่งเรื่องได้", "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String complaintId = DataStore.nextId(DataStore.COMPLAINTS_FILE, "complaintId", "C", 3);
                String todayIso = LocalDate.now().toString();

                DataStore.appendRow(
                        DataStore.COMPLAINTS_FILE,
                        DataStore.COMPLAINTS_HEADER,
                        complaintId,
                        currentUser.userId,
                        DataStore.blank(currentUser.roomId) ? "-" : currentUser.roomId,
                        category,
                        topic,
                        desc,
                        todayIso,
                        "PENDING"
                );

                JOptionPane.showMessageDialog(this,
                        "ส่งเรื่องแจ้งซ่อมเรียบร้อยแล้ว (รหัสคำขอ: " + complaintId + ")",
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
        if (currentUser == null) return;

        List<DataStore.Complaint> list = DataStore.complaintsForUser(currentUser.userId);
        for (DataStore.Complaint c : list) {
            tableModel.addRow(new Object[]{
                    c.id,
                    DataStore.thaiDate(c.date),
                    c.category.split(" ")[0],
                    c.topic,
                    c.statusThai()
            });
        }
    }
}