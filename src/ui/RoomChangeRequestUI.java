package ui;

import logic.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class RoomChangeRequestUI extends JPanel {

    private static final Font THAI_FONT = new Font("Tahoma", Font.PLAIN, 13);
    private static final Font THAI_FONT_BOLD = new Font("Tahoma", Font.BOLD, 14);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color READONLY_BG = new Color(241, 245, 249);
    private static final Color ERROR_COLOR = new Color(220, 38, 38);

    private final RoomChangeLogic logic;

    public RoomChangeRequestUI(LogicLogin.User user) {
        this.logic = new RoomChangeLogic(user);
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);

        // Container หลักที่ช่วยจัดตำแหน่งการ์ดให้อยู่ตรงกลางหน้าจอพอดี
        JPanel outerContainer = new JPanel(new GridBagLayout());
        outerContainer.setOpaque(false);
        outerContainer.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints outerGbc = new GridBagConstraints();
        outerGbc.gridx = 0;
        outerGbc.gridy = 0;
        outerGbc.weightx = 1.0;
        outerGbc.weighty = 1.0;
        outerGbc.fill = GridBagConstraints.HORIZONTAL;
        outerGbc.anchor = GridBagConstraints.NORTH; // ให้การ์ดอยู่ชิดด้านบนเสมอ

        if (!logic.hasRoom()) {
            outerContainer.add(messageCard("คุณยังไม่มีห้องพัก จึงยังยื่นคำขอย้ายห้องไม่ได้"), outerGbc);
        } else {
            outerContainer.add(buildForm(), outerGbc);
        }

        JScrollPane scroll = new JScrollPane(outerContainer);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel messageCard(String msg) {
        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(12, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(48, 24, 48, 24));
        card.add(TenantUI.label(msg, 16, Font.BOLD, TenantUI.TEXT_MUTED));
        return card;
    }

    private JPanel buildForm() {
        TenantUI.RoundedPanel formCard = new TenantUI.RoundedPanel(12, Color.WHITE, TenantUI.CARD_BORDER);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(28, 32, 28, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 1. หัวข้อฟอร์ม
        JLabel headerLabel = TenantUI.label("แบบฟอร์มแสดงความประสงค์ขอย้ายห้องพัก", 18, Font.BOLD, TenantUI.TEXT);
        gbc.insets = new Insets(0, 0, 20, 0);
        formCard.add(headerLabel, gbc);

        // 2. ห้องพักปัจจุบัน (Read-only)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        formCard.add(TenantUI.label("ห้องพักปัจจุบัน:", 13, Font.PLAIN, TenantUI.TEXT), gbc);

        gbc.gridy++;
        JTextField curField = new JTextField(logic.currentRoom() != null ? logic.currentRoom().toString() : "");
        styleControl(curField);
        curField.setEditable(false);
        curField.setBackground(READONLY_BG);
        gbc.insets = new Insets(0, 0, 16, 0);
        formCard.add(curField, gbc);

        // 3. เลือกห้องพักปลายทาง
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        formCard.add(TenantUI.label("เลือกห้องพักที่ต้องการย้ายไป (เฉพาะห้องว่าง):", 13, Font.PLAIN, TenantUI.TEXT), gbc);

        gbc.gridy++;
        List<DataStore.Room> available = logic.availableRooms();
        JComboBox<DataStore.Room> targetBox = new JComboBox<>(available.toArray(new DataStore.Room[0]));
        styleControl(targetBox);
        gbc.insets = new Insets(0, 0, 16, 0);
        formCard.add(targetBox, gbc);

        // 4. เหตุผลความจำเป็น
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        formCard.add(TenantUI.label("เหตุผลความจำเป็นในการขอย้ายห้อง:", 13, Font.PLAIN, TenantUI.TEXT), gbc);

        gbc.gridy++;
        JTextArea reasonArea = new JTextArea(4, 20);
        reasonArea.setFont(THAI_FONT);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        gbc.insets = new Insets(0, 0, 20, 0);
        formCard.add(reasonScroll, gbc);

        // ข้อความเตือนกรณีไม่มีห้องว่าง
        if (available.isEmpty()) {
            gbc.gridy++;
            JLabel noRoomLabel = TenantUI.label("ขณะนี้ไม่มีห้องว่างให้ย้าย", 13, Font.BOLD, ERROR_COLOR);
            gbc.insets = new Insets(0, 0, 12, 0);
            formCard.add(noRoomLabel, gbc);
        }

        // 5. ปุ่มยื่นคำขอ
        gbc.gridy++;
        TenantUI.RoundButton submitBtn = new TenantUI.RoundButton("ยื่นคำขอย้ายห้อง");
        submitBtn.setPreferredSize(new Dimension(0, 40));
        submitBtn.setFont(THAI_FONT_BOLD);

        if (available.isEmpty()) {
            submitBtn.setEnabled(false);
        }

        submitBtn.addActionListener(e -> {
            DataStore.Room target = (DataStore.Room) targetBox.getSelectedItem();
            String reason = reasonArea.getText().trim();

            try {
                RoomChangeLogic.Result r = logic.submit(target, reason);
                switch (r.status) {
                    case INVALID_INPUT:
                        JOptionPane.showMessageDialog(this, "กรุณาเลือกห้องและกรอกเหตุผลให้ครบถ้วน",
                                "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                        return;
                    case PENDING_EXISTS:
                        JOptionPane.showMessageDialog(this, "คุณมีคำขอย้ายห้องที่รอพิจารณาอยู่แล้ว กรุณารอผู้ดูแลตอบกลับก่อน",
                                "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                        return;
                    default:
                        break;
                }

                JOptionPane.showMessageDialog(this,
                        "ยื่นคำขอย้ายไปห้อง " + (target != null ? target.number : "") + " เรียบร้อย (รหัสคำขอ " + r.transferId + ")\n"
                                + "คำขอจะถูกส่งไปยังผู้ดูแลเพื่อพิจารณา",
                        "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
                reasonArea.setText("");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "บันทึกคำขอไม่สำเร็จ: " + ex.getMessage(),
                        "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.insets = new Insets(0, 0, 0, 0);
        formCard.add(submitBtn, gbc);

        return formCard;
    }

    // Custom helper สำหรับจัดสไตล์ Input Control ให้สวยสะอาดตา
    private void styleControl(JComponent comp) {
        comp.setFont(THAI_FONT);
        comp.setPreferredSize(new Dimension(0, 36));
        if (comp instanceof JTextField) {
            comp.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true),
                    new EmptyBorder(0, 10, 0, 10)
            ));
        } else if (comp instanceof JComboBox) {
            comp.setBackground(Color.WHITE);
        }
    }
}