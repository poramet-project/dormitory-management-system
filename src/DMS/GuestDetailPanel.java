package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class GuestDetailPanel extends JPanel {
    private final GuestUI mainUI;
    private GuestUI.RoomData currentRoom;

    private final JLabel titleLbl = new JLabel();
    private final JLabel typeLbl = new JLabel();
    private final JLabel priceLbl = new JLabel();
    private final JLabel floorLbl = new JLabel();

    private final JTextField nameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField dateField = new JTextField(LocalDate.now().toString());
    private final JTextArea noteArea = new JTextArea(3, 20);

    public GuestDetailPanel(GuestUI mainUI) {
        this.mainUI = mainUI;
        setLayout(new BorderLayout());
        setBackground(new Color(247, 249, 251));

        JPanel content = new JPanel(new BorderLayout(25, 20));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(25, 40, 25, 40));

        // ปุ่มย้อนกลับ
        JButton backBtn = new JButton("← กลับไปหน้ารายการห้อง");
        backBtn.setFont(new Font("Tahoma", Font.PLAIN, 13));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> mainUI.showSearchPage());
        content.add(backBtn, BorderLayout.NORTH);

        // ฝั่งซ้าย: ข้อมูลรายละเอียดห้องพัก
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(223, 231, 241), 1),
                new EmptyBorder(20, 25, 20, 25)
        ));

        titleLbl.setFont(new Font("Tahoma", Font.BOLD, 22));
        typeLbl.setFont(new Font("Tahoma", Font.BOLD, 15));
        typeLbl.setForeground(new Color(14, 121, 115));
        priceLbl.setFont(new Font("Tahoma", Font.BOLD, 18));
        floorLbl.setFont(new Font("Tahoma", Font.PLAIN, 14));

        leftPanel.add(titleLbl);
        leftPanel.add(Box.createVerticalStrut(6));
        leftPanel.add(typeLbl);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(priceLbl);
        leftPanel.add(Box.createVerticalStrut(6));
        leftPanel.add(floorLbl);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(new JSeparator());
        leftPanel.add(Box.createVerticalStrut(15));

        JLabel amTitle = new JLabel("สิ่งอำนวยความสะดวกในห้องพัก:");
        amTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
        leftPanel.add(amTitle);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(new JLabel("• เตียงนอนพร้อมที่นอน 5 ฟุต"));
        leftPanel.add(new JLabel("• ตู้เสื้อผ้าบิวท์อิน โต๊ะทำงานและเก้าอี้"));
        leftPanel.add(new JLabel("• เครื่องทำน้ำอุ่น ระเบียงส่วนตัว"));
        leftPanel.add(new JLabel("• ฟรีอินเทอร์เน็ต Wi-Fi ความเร็วสูง"));
        leftPanel.add(new JLabel("• ระบบความปลอดภัยคีย์การ์ดและกล้อง CCTV"));

        content.add(leftPanel, BorderLayout.CENTER);

        // ฝั่งขวา: ฟอร์มกรอกจองห้องพัก
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setPreferredSize(new Dimension(380, 500));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(223, 231, 241), 1),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel formHeader = new JLabel("แบบฟอร์มส่งคำขอจองห้อง");
        formHeader.setFont(new Font("Tahoma", Font.BOLD, 18));
        JLabel formSub = new JLabel("กรอกข้อมูลเพื่อให้เจ้าหน้าที่ติดต่อกลับและยืนยัน");
        formSub.setFont(new Font("Tahoma", Font.PLAIN, 12));
        formSub.setForeground(Color.GRAY);

        formPanel.add(formHeader);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(formSub);
        formPanel.add(Box.createVerticalStrut(15));

        formPanel.add(new JLabel("ชื่อ-นามสกุล (ภาษาอังกฤษ):"));
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(10));

        formPanel.add(new JLabel("เบอร์โทรศัพท์ติดต่อ:"));
        formPanel.add(phoneField);
        formPanel.add(Box.createVerticalStrut(10));

        formPanel.add(new JLabel("วันที่ต้องการเริ่มเข้าพัก (YYYY-MM-DD):"));
        formPanel.add(dateField);
        formPanel.add(Box.createVerticalStrut(10));

        formPanel.add(new JLabel("ข้อความเพิ่มเติม (ถ้ามี):"));
        noteArea.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        formPanel.add(new JScrollPane(noteArea));
        formPanel.add(Box.createVerticalStrut(20));

        JButton submitBtn = new JButton("ยืนยันส่งคำขอจองห้อง");
        submitBtn.setMaximumSize(new Dimension(380, 40));
        submitBtn.setBackground(new Color(14, 121, 115));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> processBooking());
        formPanel.add(submitBtn);

        content.add(formPanel, BorderLayout.EAST);
        add(content, BorderLayout.CENTER);
    }

    public void loadRoomDetails(GuestUI.RoomData room) {
        this.currentRoom = room;
        titleLbl.setText("ห้อง " + room.id);
        typeLbl.setText("ประเภท: " + room.type);
        priceLbl.setText("อัตราค่าเช่า: " + room.rent + " บาท / เดือน");
        floorLbl.setText("ตำแหน่ง: ชั้น " + room.floor);
    }

    private void processBooking() {
        if (currentRoom == null || !currentRoom.isAvailable()) {
            JOptionPane.showMessageDialog(this, "ห้องนี้ไม่ว่างสำหรับทำการจอง", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String date = dateField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "กรุณากรอกชื่อ-นามสกุล", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.matches(".*[\\u0E00-\\u0E7F].*")) {
            JOptionPane.showMessageDialog(this, "กรุณากรอกชื่อเป็นภาษาอังกฤษ (No Thai characters)", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!phone.matches("^0[0-9]{8,9}$")) {
            JOptionPane.showMessageDialog(this, "เบอร์โทรศัพท์ต้องเป็นตัวเลข 9-10 หลัก และขึ้นต้นด้วย 0", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            if (parsedDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "วันที่เริ่มเข้าอยู่ต้องไม่ใช่วันที่ในอดีต", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "รูปแบบวันที่ต้องเป็น YYYY-MM-DD เช่น " + LocalDate.now(), "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // บันทึกคำขอจองลง bookings.csv จริง
        try {
            saveBookingToCsv(name, phone, currentRoom.id, date);
            JOptionPane.showMessageDialog(this, "ส่งคำขอจองห้อง " + currentRoom.id + " เรียบร้อยแล้ว!\nเจ้าหน้าที่จะติดต่อกลับเพื่อยืนยันสัญญา", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            nameField.setText("");
            phoneField.setText("");
            noteArea.setText("");
            mainUI.showSearchPage();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "ไม่สามารถบันทึกข้อมูลการจองได้: " + ex.getMessage(), "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveBookingToCsv(String name, String phone, String roomId, String bookingDate) throws IOException {
        File file = new File("bookings.csv");
        boolean exists = file.exists() && file.length() > 0;
        long bookingId = System.currentTimeMillis() % 100000;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), java.nio.charset.StandardCharsets.UTF_8))) {
            if (!exists) {
                writer.write("bookingId,username,roomId,bookingDate,status");
                writer.newLine();
            }
            // บันทึกแถวใหม่: bookingId,username,roomId,bookingDate,status (PENDING)
            writer.write(String.format("B%d,%s,%s,%s,PENDING", bookingId, name, roomId, bookingDate));
            writer.newLine();
        }
    }
}