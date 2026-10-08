package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class RoomChangeRequestUI extends JPanel {

    private static final String TRANSFER_FILE = "room_transfers.csv";
    private static final String TRANSFER_HEADER =
            "transferId,userId,contractId,oldRoomId,newRoomId,reason,requestDate,status";

    private static final Font THAI_FONT = new Font("Tahoma", Font.PLAIN, 13);

    public RoomChangeRequestUI(LogicLogin.User user) {
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);

        DataStore.Room current = (user == null) ? null : DataStore.room(user.roomId);
        DataStore.Contract contract = DataStore.contractFor(user);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        if (current == null) {
            content.add(messageCard("คุณยังไม่มีห้องพัก จึงยังยื่นคำขอย้ายห้องไม่ได้"));
        } else {
            content.add(buildForm(user, current, contract));
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel messageCard(String msg) {
        TenantUI.RoundedPanel card = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 20, 40, 20));
        card.setMaximumSize(new Dimension(860, 140));
        card.add(TenantUI.label(msg, 16, Font.BOLD, TenantUI.TEXT_MUTED));
        return card;
    }

    private JPanel buildForm(LogicLogin.User user, DataStore.Room current, DataStore.Contract contract) {
        TenantUI.RoundedPanel formCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(20, 25, 20, 25));
        formCard.setMaximumSize(new Dimension(860, 460));

        formCard.add(TenantUI.label("แบบฟอร์มแสดงความประสงค์ขอย้ายห้องพัก", 18, Font.BOLD, TenantUI.TEXT));
        formCard.add(Box.createVerticalStrut(14));

        formCard.add(TenantUI.label("ห้องพักปัจจุบัน:", 13, Font.PLAIN, TenantUI.TEXT));
        JTextField curField = new JTextField(current.toString());
        curField.setFont(THAI_FONT);
        curField.setEditable(false);
        curField.setMaximumSize(new Dimension(800, 32));
        formCard.add(curField);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(TenantUI.label("เลือกห้องพักที่ต้องการย้ายไป (เฉพาะห้องว่าง):", 13, Font.PLAIN, TenantUI.TEXT));
        List<DataStore.Room> available = DataStore.availableRooms(current.id);
        JComboBox<DataStore.Room> targetBox = new JComboBox<>(available.toArray(new DataStore.Room[0]));
        targetBox.setFont(THAI_FONT);
        targetBox.setMaximumSize(new Dimension(800, 32));
        formCard.add(targetBox);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(TenantUI.label("เหตุผลความจำเป็นในการขอย้ายห้อง:", 13, Font.PLAIN, TenantUI.TEXT));
        JTextArea reasonArea = new JTextArea(4, 20);
        reasonArea.setFont(THAI_FONT);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        formCard.add(new JScrollPane(reasonArea));
        formCard.add(Box.createVerticalStrut(18));

        TenantUI.RoundButton submitBtn = new TenantUI.RoundButton("ยื่นคำขอย้ายห้อง");
        submitBtn.setMaximumSize(new Dimension(800, 38));

        if (available.isEmpty()) {
            submitBtn.setEnabled(false);
            formCard.add(TenantUI.label("ขณะนี้ไม่มีห้องว่างให้ย้าย", 13, Font.BOLD, new Color(0xB00020)));
            formCard.add(Box.createVerticalStrut(8));
        }

        submitBtn.addActionListener(e -> {
            DataStore.Room target = (DataStore.Room) targetBox.getSelectedItem();
            String reason = reasonArea.getText().trim();

            if (target == null || reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "กรุณาเลือกห้องและกรอกเหตุผลให้ครบถ้วน",
                        "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (DataStore.hasPendingTransfer(user.userId)) {
                JOptionPane.showMessageDialog(this, "คุณมีคำขอย้ายห้องที่รอพิจารณาอยู่แล้ว กรุณารอผู้ดูแลตอบกลับก่อน",
                        "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String transferId = DataStore.nextId(TRANSFER_FILE, "transferId", "T", 3);
                DataStore.appendRow(TRANSFER_FILE, TRANSFER_HEADER,
                        transferId,
                        DataStore.blank(user.userId) ? "-" : user.userId,
                        contract == null ? "-" : contract.id,
                        current.id,
                        target.id,
                        reason,
                        LocalDate.now().toString(),
                        "PENDING");

                JOptionPane.showMessageDialog(this,
                        "ยื่นคำขอย้ายไปห้อง " + target.number + " เรียบร้อย (รหัสคำขอ " + transferId + ")\n"
                                + "คำขอจะถูกส่งไปยังผู้ดูแลเพื่อพิจารณา",
                        "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
                reasonArea.setText("");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "บันทึกคำขอไม่สำเร็จ: " + ex.getMessage(),
                        "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
            }
        });
        formCard.add(submitBtn);
        return formCard;
    }
}