package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MyRoomUI extends JPanel {
    public MyRoomUI(LogicLogin.User user) {
        setLayout(new BorderLayout());
        setBackground(TenantUI.BG);

        // ดึงข้อมูลจริงจาก rooms.csv และ contracts.csv
        DataStore.Room room = (user == null) ? null : DataStore.room(user.roomId);
        DataStore.Contract contract = DataStore.contractFor(user);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        if (room == null) {
            String msg = (user == null || DataStore.blank(user.roomId))
                    ? "คุณยังไม่มีห้องพัก"
                    : "ไม่พบข้อมูลห้อง " + user.roomId + " ในไฟล์ rooms.csv";
            content.add(messageCard(msg));
        } else {
            content.add(buildMainCard(room, contract));
            content.add(Box.createVerticalStrut(18));
            content.add(buildFacilityCard(room));
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

    private JPanel buildMainCard(DataStore.Room room, DataStore.Contract contract) {
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

        JLabel title = TenantUI.label("ห้องพักหมายเลข " + room.number, 24, Font.BOLD, TenantUI.TEXT);

        TenantUI.Pill pill = new TenantUI.Pill(room.typeFull(), TenantUI.TEAL_LIGHT, TenantUI.TEAL_BORDER, TenantUI.TEAL, 12);
        pill.setPreferredSize(new Dimension(190, 25));
        pill.setMaximumSize(new Dimension(190, 25));

        JLabel floor = TenantUI.label("อาคารหอพักนักศึกษา • ชั้น " + room.floor() + " • สถานะห้อง: " + room.statusThai(),
                13, Font.PLAIN, TenantUI.TEXT_MUTED);

        // ถ้ามีสัญญา ใช้ค่าเช่าตามสัญญา ไม่งั้นใช้ค่าเช่าพื้นฐานของห้อง
        int rentValue = (contract != null && contract.rent > 0) ? contract.rent : room.rent;
        JLabel rent = TenantUI.label("อัตราค่าเช่า: " + DataStore.money(rentValue) + " บาท / เดือน",
                15, Font.BOLD, TenantUI.TEAL);

        info.add(title);
        info.add(Box.createVerticalStrut(6));
        info.add(pill);
        info.add(Box.createVerticalStrut(8));
        info.add(floor);
        info.add(Box.createVerticalStrut(6));
        info.add(rent);

        mainCard.add(info, BorderLayout.CENTER);
        return mainCard;
    }

    private JPanel buildFacilityCard(DataStore.Room room) {
        TenantUI.RoundedPanel facCard = new TenantUI.RoundedPanel(10, Color.WHITE, TenantUI.CARD_BORDER);
        facCard.setLayout(new BorderLayout());
        facCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        facCard.setMaximumSize(new Dimension(860, 240));

        facCard.add(TenantUI.label("รายการเฟอร์นิเจอร์และอุปกรณ์ประจำห้อง", 16, Font.BOLD, TenantUI.TEXT),
                BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 2, 20, 10));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(12, 0, 5, 0));

        // อุปกรณ์ทำความเย็นเปลี่ยนตามประเภทห้องใน rooms.csv
        String cooling = room.isAir()
                ? "❄️ เครื่องปรับอากาศ Inverter ประหยัดไฟ"
                : "🌀 พัดลมติดเพดาน";

        grid.add(TenantUI.label("🛏️ เตียงนอนเดี่ยว 3.5 ฟุต พร้อมฟูก (2 ชุด)", 13, Font.PLAIN, TenantUI.TEXT));
        grid.add(TenantUI.label(cooling, 13, Font.PLAIN, TenantUI.TEXT));
        grid.add(TenantUI.label("🚪 ตู้เสื้อผ้าบิวท์อิน 2 บาน", 13, Font.PLAIN, TenantUI.TEXT));
        grid.add(TenantUI.label("🚿 เครื่องทำน้ำอุ่นในห้องน้ำส่วนตัว", 13, Font.PLAIN, TenantUI.TEXT));
        grid.add(TenantUI.label("🪑 โต๊ะทำงานและเก้าอี้ (2 ชุด)", 13, Font.PLAIN, TenantUI.TEXT));
        grid.add(TenantUI.label("📶 Wi-Fi ความเร็วสูงฟรี", 13, Font.PLAIN, TenantUI.TEXT));

        facCard.add(grid, BorderLayout.CENTER);
        return facCard;
    }
}