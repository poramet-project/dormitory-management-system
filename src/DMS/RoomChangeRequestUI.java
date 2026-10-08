package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;

// 1. เปลี่ยน extends JFrame เป็น JPanel
public class RoomChangeRequestUI extends JPanel {

    static final Color TEAL = new Color(0x12756B);
    static final Color BG = new Color(0xF2F2F2);
    static final Color CARD_BORDER = new Color(0xD9D9D9);
    static final Color TEXT = new Color(0x333333);
    static final Color TEXT_MUTED = new Color(0x666666);

    // 2. ให้ Constructor รับ (LogicLogin.User user)
    public RoomChangeRequestUI(LogicLogin.User user) {
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);

        if (user == null) {
            user = new LogicLogin.User("tenant", "1234", "ผู้เช่าทดสอบ", "0812345678", "TENANT", "R101");
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        TenantUI.RoundedPanel formCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(20, 25, 20, 25));
        formCard.setMaximumSize(new Dimension(860, 420));

        formCard.add(TenantUI.label("แบบฟอร์มแสดงความประสงค์ขอย้ายห้องพัก", 18, Font.BOLD, TenantUI.TEXT));
        formCard.add(Box.createVerticalStrut(14));

        String roomNo = (user.roomId == null || user.roomId.isEmpty()) ? "R101" : user.roomId;
        formCard.add(TenantUI.label("ห้องพักปัจจุบัน:", 13, Font.PLAIN, TenantUI.TEXT));
        JTextField curField = new JTextField(roomNo);
        curField.setEditable(false);
        curField.setMaximumSize(new Dimension(800, 32));
        formCard.add(curField);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(TenantUI.label("หมายเลขห้องพักเป้าหมายที่ต้องการย้ายไป:", 13, Font.PLAIN, TenantUI.TEXT));
        JTextField targetField = new JTextField();
        targetField.setMaximumSize(new Dimension(800, 32));
        formCard.add(targetField);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(TenantUI.label("เหตุผลความจำเป็นในการขอย้ายห้อง:", 13, Font.PLAIN, TenantUI.TEXT));
        JTextArea reasonArea = new JTextArea(4, 20);
        reasonArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        formCard.add(new JScrollPane(reasonArea));
        formCard.add(Box.createVerticalStrut(18));

        TenantUI.RoundButton submitBtn = new TenantUI.RoundButton("ยื่นคำขอย้ายห้อง");
        submitBtn.setMaximumSize(new Dimension(800, 38));
        
        final LogicLogin.User finalUser = user;
        submitBtn.addActionListener(e -> {
            String target = targetField.getText().trim();
            String reason = reasonArea.getText().trim();
            if (target.isEmpty() || reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "กรุณากรอกข้อมูลให้ครบถ้วน", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }

            saveTransfer(finalUser.username, curField.getText(), target, reason);
            JOptionPane.showMessageDialog(this, "ยื่นคำขอย้ายห้องเรียบร้อย คำขอจะถูกส่งไปยังระบบของ Admin", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            targetField.setText("");
            reasonArea.setText("");
        });
        formCard.add(submitBtn);

        content.add(formCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void saveTransfer(String username, String cur, String target, String reason) {
        File f = new File("room_transfers.csv");
        boolean exists = f.exists() && f.length() > 0;
        long transferId = System.currentTimeMillis() % 10000;
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true), java.nio.charset.StandardCharsets.UTF_8))) {
            if (!exists) {
                writer.write("transferId,username,currentRoom,targetRoom,reason,status");
                writer.newLine();
            }
            writer.write(String.format("T%d,%s,%s,%s,%s,PENDING", transferId, username, cur, target, reason.replace(",", " ")));
            writer.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}