package ui;

import logic.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.io.IOException;
import java.util.List;

/**
 * TenantRoomChangeUI คือคลาสหน้าจอสำหรับให้ผู้เช่ายื่นคำขอย้ายห้องพัก
 * เชื่อมโยงข้อมูลผู้เช่าและห้องพักปัจจุบัน และแสดงสถานะคำขอที่อยู่ระหว่างดำเนินการ
 * logic คือ อ็อบเจกต์ตรรกะสำหรับตรวจสอบห้องว่างและบันทึกคำขอย้ายห้อง
 */
public class TenantRoomChangeUI extends JPanel {

    // ===== representation =====
    // Abstraction Function:
    // AF(logic, outerContainer) คือ หน้าจอแบบฟอร์มแสดงห้องพักปัจจุบัน รายการห้องว่างที่สามารถย้ายได้ และช่องกรอกเหตุผลในการย้ายห้อง
    // RI
    // 1. logic ต้องไม่เป็น null
    // 2. outerContainer ต้องไม่เป็น null

    private static final Font THAI_FONT = new Font("Tahoma", Font.PLAIN, 13);
    private static final Font THAI_FONT_BOLD = new Font("Tahoma", Font.BOLD, 14);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color READONLY_BG = new Color(241, 245, 249);
    private static final Color ERROR_COLOR = new Color(220, 38, 38);

    private final TenantRoomChangeLogic logic;
    private final JPanel outerContainer;
    private final GridBagConstraints outerGbc;

    /**
     * คอนสตรักเตอร์สร้างหน้าจอยื่นคำขอย้ายห้องพักของผู้เช่า
     * @param user ข้อมูลผู้เช่าที่กำลังเข้าใช้งานระบบ
     */
    public TenantRoomChangeUI(LoginLogic.User user) {
        this.logic = new TenantRoomChangeLogic(user);
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);

        // Container หลักที่ช่วยจัดตำแหน่งการ์ดให้อยู่ตรงกลางหน้าจอพอดี
        outerContainer = new JPanel(new GridBagLayout());
        outerContainer.setOpaque(false);
        outerContainer.setBorder(new EmptyBorder(24, 24, 24, 24));

        outerGbc = new GridBagConstraints();
        outerGbc.gridx = 0;
        outerGbc.gridy = 0;
        outerGbc.weightx = 1.0;
        outerGbc.weighty = 1.0;
        outerGbc.fill = GridBagConstraints.HORIZONTAL;
        outerGbc.anchor = GridBagConstraints.NORTH; // ให้การ์ดอยู่ชิดด้านบนเสมอ

        rebuildContent();

        JScrollPane scroll = new JScrollPane(outerContainer);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // รีเฟรชข้อมูลอัตโนมัติเมื่อหน้าจอนี้ถูกเปิดแสดงผล
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                refresh();
            }
        });
    }

    /**
     * รีเฟรชข้อมูลและอัปเดตหน้าจอใหม่ตามสถานะล่าสุดของระบบ
     */
    public void refresh() {
        logic.reload();
        rebuildContent();
    }

    /**
     * สร้างเนื้อหาหน้าจอใหม่ ตรวจสอบสิทธิ์ว่าผู้เช่ามีห้องพักหรือไม่
     */
    private void rebuildContent() {
        outerContainer.removeAll();
        if (!logic.hasRoom()) {
            outerContainer.add(messageCard("คุณยังไม่มีห้องพัก จึงยังยื่นคำขอย้ายห้องไม่ได้"), outerGbc);
        } else {
            outerContainer.add(buildForm(), outerGbc);
        }
        outerContainer.revalidate();
        outerContainer.repaint();
    }

    /**
     * สร้างการ์ดแสดงข้อความแจ้งเตือนตรงกลางหน้าจอ (เช่น เมื่อผู้ใช้ยังไม่มีห้องพัก)
     * @param msg ข้อความแจ้งเตือน
     * @return พาเนลการ์ดข้อความ
     */
    private JPanel messageCard(String msg) {
        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(12, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(48, 24, 48, 24));
        card.add(TenantUI.label(msg, 16, Font.BOLD, TenantUI.TEXT_MUTED));
        return card;
    }

    /**
     * สร้างการ์ดแบบฟอร์มแสดงความประสงค์ขอย้ายห้องพักและปุ่มยื่นคำขอ
     * @return พาเนลแบบฟอร์ม
     */
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
        gbc.insets = new Insets(0, 0, 16, 0);
        formCard.add(headerLabel, gbc);

        // ตรวจสอบว่ามีคำขอย้ายห้องที่รอการพิจารณาอยู่หรือไม่
        TenantRoomChangeLogic.TransferItem pending = logic.getPendingTransfer();
        if (pending != null) {
            gbc.gridy++;
            JPanel banner = new JPanel(new BorderLayout(8, 0));
            banner.setBackground(new Color(254, 249, 195)); // สีเหลืองเตือนนุ่มนวล
            banner.setBorder(new CompoundBorder(
                    new LineBorder(new Color(250, 204, 21), 1, true),
                    new EmptyBorder(10, 14, 10, 14)
            ));
            JLabel bannerText = new JLabel("<html><b>📌 อยู่ระหว่างรอพิจารณา:</b> คุณได้ยื่นคำขอย้ายไปห้อง <b>"
                    + pending.newRoomNum + "</b> (รหัสคำขอ " + pending.id + ") เมื่อวันที่ " + pending.date
                    + "<br><span style='color: #854d0e;'>กรุณารอผู้ดูแลระบบ (Admin) ตรวจสอบและอนุมัติคำขอ</span></html>");
            bannerText.setFont(THAI_FONT);
            banner.add(bannerText, BorderLayout.CENTER);
            gbc.insets = new Insets(0, 0, 16, 0);
            formCard.add(banner, gbc);
        }

        // 2. ห้องพักปัจจุบัน (Read-only)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        formCard.add(TenantUI.label("ห้องพักปัจจุบัน:", 13, Font.PLAIN, TenantUI.TEXT), gbc);

        gbc.gridy++;
        DataStore.Room cur = logic.currentRoom();
        String curText = (cur != null) ? cur.toString() : "-";
        JTextField curField = new JTextField(curText);
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
        if (pending != null || available.isEmpty()) {
            targetBox.setEnabled(false);
        }
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

        if (pending != null) {
            reasonArea.setText(pending.reason);
            reasonArea.setEditable(false);
            reasonArea.setBackground(READONLY_BG);
        }

        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        gbc.insets = new Insets(0, 0, 20, 0);
        formCard.add(reasonScroll, gbc);

        // ข้อความเตือนกรณีไม่มีห้องว่าง
        if (available.isEmpty() && pending == null) {
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

        if (pending != null) {
            submitBtn.setEnabled(false);
            submitBtn.setText("มีคำขอย้ายห้องที่รอการพิจารณาอยู่แล้ว");
            submitBtn.setBackground(new Color(241, 245, 249));
            submitBtn.setForeground(new Color(148, 163, 184));
        } else if (available.isEmpty()) {
            submitBtn.setEnabled(false);
        }

        submitBtn.addActionListener(e -> {
            DataStore.Room target = (DataStore.Room) targetBox.getSelectedItem();
            String reason = reasonArea.getText().trim();

            try {
                TenantRoomChangeLogic.Result r = logic.submit(target, reason);
                switch (r.status) {
                    case INVALID_INPUT:
                        JOptionPane.showMessageDialog(this, "กรุณาเลือกห้องและกรอกเหตุผลให้ครบถ้วน",
                                "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                        return;
                    case PENDING_EXISTS:
                        JOptionPane.showMessageDialog(this, "คุณมีคำขอย้ายห้องที่รอพิจารณาอยู่แล้ว กรุณารอผู้ดูแลตอบกลับก่อน",
                                "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                        refresh();
                        return;
                    default:
                        break;
                }

                JOptionPane.showMessageDialog(this,
                        "ยื่นคำขอย้ายไปห้อง " + (target != null ? target.number : "") + " เรียบร้อย (รหัสคำขอ " + r.transferId + ")\n"
                                + "คำขอจะถูกส่งไปยังผู้ดูแลเพื่อพิจารณา",
                        "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
                refresh();
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

    /**
     * กำหนดรูปแบบสไตล์ ขนาดตัวอักษร ความสูง และเส้นขอบของคอนโทรลรับข้อมูล
     * @param comp คอมโพเนนต์ที่ต้องการจัดสไตล์
     */
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