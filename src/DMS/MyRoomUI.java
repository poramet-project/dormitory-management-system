package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MyRoomUI extends JFrame {
    public MyRoomUI(LogicLogin.User user) {
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);
        setVisible(true);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        // การ์ดแสดงข้อมูลห้องหลัก
        TenantUI.RoundedPanel mainCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        mainCard.setLayout(new BorderLayout(25, 0));
        mainCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        mainCard.setMaximumSize(new Dimension(860, 210));

        TenantUI.RoomImage roomImg = new TenantUI.RoomImage();
        roomImg.setPreferredSize(new Dimension(260, 160));
        mainCard.add(roomImg, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        String roomNo = (user.roomId == null || user.roomId.isEmpty()) ? "R101" : user.roomId;
        JLabel title = TenantUI.label("ห้องพักหมายเลข " + roomNo, 24, Font.BOLD, TenantUI.TEXT);

        TenantUI.Pill pill = new TenantUI.Pill("ห้องแอร์ (Air-Conditioned)", TenantUI.TEAL_LIGHT, TenantUI.TEAL_BORDER, TenantUI.TEAL, 12);
        pill.setPreferredSize(new Dimension(160, 25));

        JLabel floor = TenantUI.label("อาคารหอพักนักศึกษา • ชั้น " + roomNo.replaceAll("[^0-9]", "").substring(0, 1) + " • ขนาดห้อง 24 ตร.ม.", 13, Font.PLAIN, TenantUI.TEXT_MUTED);
        JLabel rent = TenantUI.label("อัตราค่าเช่า: 3,500 บาท / เดือน", 15, Font.BOLD, TenantUI.TEAL);

        info.add(title);
        info.add(Box.createVerticalStrut(6));
        info.add(pill);
        info.add(Box.createVerticalStrut(8));
        info.add(floor);
        info.add(Box.createVerticalStrut(6));
        info.add(rent);

        mainCard.add(info, BorderLayout.CENTER);
        content.add(mainCard);
        content.add(Box.createVerticalStrut(18));

        // การ์ดเฟอร์นิเจอร์
        TenantUI.RoundedPanel facCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        facCard.setLayout(new BorderLayout());
        facCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        facCard.setMaximumSize(new Dimension(860, 240));

        JLabel facHeader = TenantUI.label("รายการเฟอร์นิเจอร์และอุปกรณ์ประจำห้อง", 16, Font.BOLD, TenantUI.TEXT);
        facCard.add(facHeader, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 2, 20, 10));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(12, 0, 5, 0));

        grid.add(TenantUI.label("🛏️ เตียงนอนเดี่ยว 3.5 ฟุต พร้อมฟูก (2 ชุด)", 13, Font.PLAIN, TenantUI.TEXT));
        grid.add(TenantUI.label("❄️ เครื่องปรับอากาศ Inverter ประหยัดไฟ", 13, Font.PLAIN, TenantUI.TEXT));
        grid.add(TenantUI.label("🚪 ตู้เสื้อผ้าบิวท์อิน 2 บาน", 13, Font.PLAIN, TenantUI.TEXT));
        grid.add(TenantUI.label("🚿 เครื่องทำน้ำอุ่นในห้องน้ำส่วนตัว", 13, Font.PLAIN, TenantUI.TEXT));
        grid.add(TenantUI.label("🪑 โต๊ะทำงานและเก้าอี้ (2 ชุด)", 13, Font.PLAIN, TenantUI.TEXT));
        grid.add(TenantUI.label("📶 Wi-Fi ความเร็วสูงฟรี", 13, Font.PLAIN, TenantUI.TEXT));

        facCard.add(grid, BorderLayout.CENTER);
        content.add(facCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }
}