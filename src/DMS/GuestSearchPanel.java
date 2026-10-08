package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GuestSearchPanel extends JPanel {
    private final GuestUI mainUI;
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> typeFilter = new JComboBox<>(new String[]{"ประเภทห้องพักทั้งหมด", "ห้องพัดลม", "ห้องแอร์", "VIP"});
    private final JComboBox<String> priceFilter = new JComboBox<>(new String[]{"ราคาทั้งหมด", "ไม่เกิน 3,000 บาท", "ไม่เกิน 4,000 บาท", "ไม่เกิน 5,000 บาท"});
    private final JPanel roomCardsContainer = new JPanel();
    private final List<GuestUI.RoomData> allRooms = new ArrayList<>();

    public GuestSearchPanel(GuestUI mainUI) {
        this.mainUI = mainUI;
        setLayout(new BorderLayout());
        setBackground(new Color(247, 249, 251));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(25, 40, 25, 40));

        // 1. หัวข้อหน้า
        JLabel title = new JLabel("ค้นหาห้องพักว่าง");
        title.setFont(new Font("Tahoma", Font.BOLD, 24));
        title.setForeground(new Color(23, 32, 53));
        JLabel subtitle = new JLabel("เลือกห้องพักที่เหมาะกับคุณ พร้อมระบบส่งคำขอจองห้องออนไลน์");
        subtitle.setFont(new Font("Tahoma", Font.PLAIN, 14));
        subtitle.setForeground(new Color(81, 97, 123));

        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(20));

        // 2. แถบค้นหาและตัวกรอง (Filter Box)
        JPanel filterBox = new JPanel(new GridBagLayout());
        filterBox.setBackground(Color.WHITE);
        filterBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(223, 231, 241), 1),
                new EmptyBorder(12, 15, 12, 15)
        ));
        filterBox.setMaximumSize(new Dimension(1920, 70));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);

        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.setToolTipText("พิมพ์เลขห้อง เช่น 101, 205");

        JButton searchBtn = new JButton("ค้นหา");
        searchBtn.setBackground(new Color(14, 121, 115));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        searchBtn.setPreferredSize(new Dimension(90, 36));
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(e -> filterRooms());

        gbc.weightx = 0.4; gbc.gridx = 0; filterBox.add(searchField, gbc);
        gbc.weightx = 0.25; gbc.gridx = 1; filterBox.add(typeFilter, gbc);
        gbc.weightx = 0.25; gbc.gridx = 2; filterBox.add(priceFilter, gbc);
        gbc.weightx = 0.1; gbc.gridx = 3; filterBox.add(searchBtn, gbc);

        content.add(filterBox);
        content.add(Box.createVerticalStrut(25));

        // 3. Grid แสดงการ์ดห้องพัก
        roomCardsContainer.setLayout(new GridLayout(0, 3, 20, 20));
        roomCardsContainer.setOpaque(false);
        content.add(roomCardsContainer);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        reloadRooms();
    }

    public void reloadRooms() {
        allRooms.clear();
        File file = resolveRoomsFile();
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                boolean header = true;
                while ((line = reader.readLine()) != null) {
                    if (header) { header = false; continue; }
                    String[] c = line.split(",", -1);
                    if (c.length >= 5) {
                        String occupant = c.length > 5 ? c[5].trim() : "";
                        allRooms.add(new GuestUI.RoomData(c[0].trim(), c[1].trim(), c[2].trim(), c[3].trim(), c[4].trim(), occupant));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        filterRooms();
    }

    private File resolveRoomsFile() {
        String[] paths = {"rooms.csv", "src/rooms.csv", "data/rooms.csv", "src/data/rooms.csv"};
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) return f;
        }
        return new File("rooms.csv");
    }

    private void filterRooms() {
        roomCardsContainer.removeAll();
        String query = searchField.getText().trim().toLowerCase();
        int typeIdx = typeFilter.getSelectedIndex();
        int priceIdx = priceFilter.getSelectedIndex();

        int maxPrice = Integer.MAX_VALUE;
        if (priceIdx == 1) maxPrice = 3000;
        else if (priceIdx == 2) maxPrice = 4000;
        else if (priceIdx == 3) maxPrice = 5000;

        for (GuestUI.RoomData r : allRooms) {
            if (!query.isEmpty() && !r.id.toLowerCase().contains(query) && !r.floor.contains(query)) {
                continue;
            }
            if (typeIdx == 1 && !r.type.contains("พัดลม")) continue;
            if (typeIdx == 2 && !r.type.contains("แอร์")) continue;
            if (typeIdx == 3 && !r.type.toUpperCase().contains("VIP")) continue;

            try {
                int rent = Integer.parseInt(r.rent);
                if (rent > maxPrice) continue;
            } catch (NumberFormatException ignored) {}

            roomCardsContainer.add(createRoomCard(r));
        }

        roomCardsContainer.revalidate();
        roomCardsContainer.repaint();
    }

    private JPanel createRoomCard(GuestUI.RoomData r) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 235), 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Header ห้อง & Badge สถานะ
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel roomNo = new JLabel("ห้อง " + r.id);
        roomNo.setFont(new Font("Tahoma", Font.BOLD, 18));
        roomNo.setForeground(new Color(23, 32, 53));

        JLabel badge = new JLabel(r.isAvailable() ? " ว่าง " : " เต็ม ");
        badge.setOpaque(true);
        badge.setBackground(r.isAvailable() ? new Color(205, 249, 230) : new Color(255, 226, 229));
        badge.setForeground(r.isAvailable() ? new Color(0, 154, 112) : new Color(243, 69, 84));
        badge.setFont(new Font("Tahoma", Font.BOLD, 12));

        top.add(roomNo, BorderLayout.WEST);
        top.add(badge, BorderLayout.EAST);

        // ข้อมูลประเภท ชั้น และราคา
        JPanel body = new JPanel(new GridLayout(3, 1, 0, 4));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 0, 12, 0));

        JLabel typeLbl = new JLabel("ประเภท: " + r.type);
        typeLbl.setForeground(new Color(14, 121, 115));
        typeLbl.setFont(new Font("Tahoma", Font.BOLD, 13));

        JLabel floorLbl = new JLabel("ชั้น " + r.floor + " • เฟอร์นิเจอร์ครบ");
        floorLbl.setForeground(new Color(81, 97, 123));

        JLabel priceLbl = new JLabel(String.format("%,d บาท / เดือน", Integer.parseInt(r.rent.isEmpty() ? "0" : r.rent)));
        priceLbl.setFont(new Font("Tahoma", Font.BOLD, 15));
        priceLbl.setForeground(new Color(23, 32, 53));

        body.add(typeLbl);
        body.add(floorLbl);
        body.add(priceLbl);

        // ปุ่มดูรายละเอียด / จอง
        JButton viewBtn = new JButton(r.isAvailable() ? "ดูรายละเอียด & จอง" : "ห้องไม่ว่าง");
        viewBtn.setEnabled(r.isAvailable());
        viewBtn.setBackground(r.isAvailable() ? new Color(14, 121, 115) : new Color(200, 205, 210));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        viewBtn.setFocusPainted(false);
        viewBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewBtn.addActionListener(e -> mainUI.showDetailPage(r));

        card.add(top, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(viewBtn, BorderLayout.SOUTH);
        return card;
    }
}