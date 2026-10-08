package DMS;                                            // ชื่อแพ็กเกจ (โฟลเดอร์) ที่ไฟล์นี้อยู่

import javax.swing.*;                                   // คลาส Swing: JFrame, JPanel, JLabel, JButton ฯลฯ
import java.awt.*;                                      // Color, Font, Graphics, Layout ต่างๆ
import java.awt.event.*;                                // ActionListener, MouseListener และ Event ต่างๆ

// คลาสหน้าต่างหลัก สืบทอด JFrame และ implements ActionListener เพื่อรับเหตุการณ์กดปุ่ม
public class TenantUI extends JFrame implements ActionListener {

    // สีและฟอนต์ของโปรแกรม
    Color TEAL      = new Color(0x13766F);              // สีเขียวหลักของระบบ
    Color TEAL_DARK = new Color(0x0E5F59);              // เขียวเข้ม ใช้ตอนเมาส์ชี้ปุ่ม
    Color TEAL_TINT = new Color(0xE3F4F1);              // เขียวอ่อน ใช้พื้นเมนูที่เลือก
    Color CIRCLE_BG = new Color(0xDDF1EE);              // สีวงกลมหลังไอคอน
    Color PILL_BG   = new Color(0xDDF4E8);              // พื้นป้ายสถานะสีเขียว
    Color PILL_TEXT = new Color(0x1B8A55);              // ตัวอักษรป้ายสถานะ
    Color BG        = new Color(0xF3F8F8);              // สีพื้นหลังส่วนเนื้อหา
    Color LINE      = new Color(0xE6E9E9);              // สีเส้นคั่น
    Color TEXT      = new Color(0x22282B);              // สีตัวอักษรหลัก
    Color GRAY      = new Color(0x6F787D);              // สีตัวอักษรรอง
    Color YELLOW    = new Color(0xF2C23C);              // จุดสถานะ "กำลังดำเนินการ"
    String FONT     = pickFont();                       // ฟอนต์ไทยที่เลือกจากเครื่อง
    String SYMBOL   = "Segoe UI Symbol";                // ฟอนต์สำหรับสัญลักษณ์/ไอคอน

    // แอตทริบิวต์ของหน้าต่าง
    Container cp;                                       // ที่วาง Component ของ JFrame (ContentPane)
    JButton btDetail, btLease, btTrack;                 // ปุ่มที่ต้องแยกให้ออกใน actionPerformed

    // ---------------------------------------------------------------
    // Constructor: แบ่งงานเป็น 3 ขั้นตอนตามสไลด์ (Initial -> setComponent -> Finally)
    // ---------------------------------------------------------------
    public TenantUI() {
        super("KU Dormitory");                          // เรียก constructor ของ JFrame เพื่อตั้งชื่อหน้าต่าง
        Initial();                                      // 1. กำหนดค่าเริ่มต้น
        setComponent();                                 // 2. สร้างและวาง Component
        Finally();                                      // 3. กำหนดขนาด/แสดงผลหน้าต่าง
    }

    public void Initial() {
        cp = getContentPane();                          // ดึง ContentPane มาเป็นที่วาง Component
        cp.setLayout(new BorderLayout());               // แบ่งเป็นซ้าย (เมนู) / กลาง (เนื้อหา)
    }

    public void setComponent() {
        cp.add(createSidebar(), BorderLayout.WEST);     // เมนูอยู่ซ้าย

        JPanel right = new JPanel(new BorderLayout());  // แผงฝั่งขวา แบ่งบน/กลาง
        right.add(createTopBar(), BorderLayout.NORTH);  // แถบบนสีขาวไว้ด้านบน
        right.add(createContent(), BorderLayout.CENTER);// พื้นที่การ์ดไว้ตรงกลาง
        cp.add(right, BorderLayout.CENTER);             // ใส่ฝั่งขวาลงหน้าต่าง
    }

    public void Finally() {
        setSize(1536, 1024);                            // ขนาดหน้าต่างเริ่มต้น
        setLocationRelativeTo(null);                    // แสดงกลางจอ
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // กดปิดแล้วจบโปรแกรม
        setVisible(true);                               // แสดงหน้าต่าง
    }

    // ---------------------------------------------------------------
    // Event Handling: ทำงานเมื่อกดปุ่มที่ addActionListener(this) ไว้
    // ---------------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btDetail) {                // ปุ่มดูรายละเอียดห้อง
            JOptionPane.showMessageDialog(this, "รายละเอียดห้อง 205");
        } else if (e.getSource() == btLease) {          // ปุ่มดูสัญญา
            JOptionPane.showMessageDialog(this, "เปิดหน้าสัญญาเช่า");
        } else if (e.getSource() == btTrack) {          // ปุ่มติดตามสถานะ
            JOptionPane.showMessageDialog(this, "ไปหน้าติดตามสถานะ");
        } else {                                        // ที่เหลือคือปุ่มเมนูซ้าย (แยกด้วย ActionCommand)
            System.out.println("เลือกเมนู: " + e.getActionCommand()); // ตัวอย่าง: ภายหลังเปลี่ยนเป็นสลับหน้า
        }
    }

    // ---------------------------------------------------------------
    // ส่วนประกอบของหน้าจอ
    // ---------------------------------------------------------------

    public JPanel createSidebar() {                     // สร้างเมนูด้านซ้าย
        JPanel side = new JPanel(new BorderLayout());   // แผงนอก: เมนูอยู่กลาง เส้นคั่นอยู่ขวา
        side.setPreferredSize(new Dimension(270, 0));   // กว้าง 270px ความสูงปล่อยตามหน้าต่าง

        JPanel line = new JPanel();                     // เส้นคั่นบางๆ ด้านขวาของเมนู
        line.setBackground(LINE);                       // สีเส้น
        line.setPreferredSize(new Dimension(1, 0));     // กว้าง 1px
        side.add(line, BorderLayout.EAST);              // วางไว้ขอบขวา

        JPanel menu = new JPanel();                     // แผงเมนูด้านซ้าย
        menu.setBackground(Color.WHITE);                // พื้นขาว
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS)); // เรียงจากบนลงล่าง
        side.add(menu, BorderLayout.CENTER);            // ใส่เมนูตรงกลางของแผงนอก

        // โลโก้
        menu.add(Box.createVerticalStrut(24));          // เว้นด้านบน 24
        JPanel logoRow = clear(new FlowLayout(FlowLayout.LEFT, 0, 0)); // แถวโลโก้ เรียงซ้ายไปขวา
        logoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));  // จำกัดความสูงแถว
        RoundPanel logo = new RoundPanel(new GridBagLayout(), TEAL, null, 14); // กล่องโลโก้สีเขียว
        logo.setPreferredSize(new Dimension(52, 52));   // ขนาดกล่องโลโก้
        logo.add(lbl("KU", th(Font.BOLD, 20), Color.WHITE)); // ตัวอักษร KU กลางกล่อง
        logoRow.add(Box.createHorizontalStrut(26));     // เว้นด้านซ้าย 26
        logoRow.add(logo);                              // ใส่กล่องโลโก้
        logoRow.add(Box.createHorizontalStrut(14));     // เว้นระหว่างโลโก้กับชื่อ
        logoRow.add(lbl("KU Dormitory", th(Font.BOLD, 18), TEXT)); // ใส่ชื่อระบบข้างโลโก้
        menu.add(logoRow);                              // ใส่แถวโลโก้ลงเมนู
        menu.add(Box.createVerticalStrut(24));          // เว้นระยะแนวตั้ง

        // รายการเมนู
        String[] glyphs = {"⌂", "🛏", "▤", "⇄", "🔧"};  // ไอคอนของแต่ละเมนู
        String[] labels = {"หน้าหลัก", "ห้องพักของฉัน", "สัญญาเช่า", "คำขอย้ายห้อง", "แจ้งซ่อม/ร้องเรียน"}; // ชื่อเมนู
        for (int i = 0; i < labels.length; i++) {       // วนสร้างทีละเมนู
            boolean active = (i == 0);                  // เมนูแรกคือหน้าที่เปิดอยู่
            MenuButton bt = new MenuButton(active ? TEAL_TINT : Color.WHITE); // ปุ่มเมนู (พื้นเขียวอ่อนถ้าเลือกอยู่)
            JLabel ic = sym(glyphs[i], 20, active ? TEAL : GRAY); // ไอคอนเมนู
            ic.setPreferredSize(new Dimension(30, 26)); // ความกว้างไอคอนเท่ากันทุกเมนู
            bt.add(ic);                                 // ใส่ไอคอน
            bt.add(lbl(labels[i], th(active ? Font.BOLD : Font.PLAIN, 14), active ? TEAL : TEXT)); // ใส่ชื่อเมนู
            bt.setActionCommand(labels[i]);             // ตั้งชื่อคำสั่ง ไว้แยกปุ่มใน actionPerformed
            bt.addActionListener(this);                 // ให้ปุ่มส่งเหตุการณ์มาที่ actionPerformed

            JPanel wrap = clear(new FlowLayout(FlowLayout.CENTER, 0, 4)); // ตัวห่อให้เมนูอยู่กึ่งกลางแนวนอน
            wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));    // จำกัดความสูง
            wrap.add(bt);                               // ใส่ปุ่มเมนู
            menu.add(wrap);                             // ใส่ลงแถบเมนู
        }
        return side;                                    // ส่งกลับ
    }

    public JPanel createTopBar() {                      // สร้างแถบด้านบนสีขาว
        JPanel top = new JPanel(new BorderLayout());    // แผงนอก: แถบอยู่กลาง เส้นคั่นอยู่ล่าง
        top.setPreferredSize(new Dimension(0, 64));     // สูง 64px ความกว้างปล่อยตามหน้าต่าง

        JPanel line = new JPanel();                     // เส้นคั่นบางๆ ด้านล่างแถบ
        line.setBackground(LINE);                       // สีเส้น
        line.setPreferredSize(new Dimension(0, 1));     // สูง 1px
        top.add(line, BorderLayout.SOUTH);              // วางไว้ขอบล่าง

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10)); // เรียงชิดขวา เว้นขอบ 16 แนวนอน 10 แนวตั้ง
        bar.setBackground(Color.WHITE);                 // พื้นสีขาว
        top.add(bar, BorderLayout.CENTER);              // ใส่แถบตรงกลางของแผงนอก

        ImageBox avatar = new ImageBox("src/img/profile.jpg", 0, true, Color.WHITE, GRAY); // รูปโปรไฟล์วงกลม
        avatar.setPreferredSize(new Dimension(44, 44)); // ขนาดรูปโปรไฟล์
        bar.add(avatar);                                // ใส่รูปโปรไฟล์

        JPanel who = clear(null);                       // กลุ่มชื่อ + บทบาท
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS)); // เรียงแนวตั้ง
        who.add(lbl("นางสาวณัชชา บุญญกามะ", th(Font.BOLD, 13), TEXT)); // ชื่อผู้ใช้
        who.add(lbl("ผู้พักอาศัย", th(Font.PLAIN, 11), GRAY));          // บทบาท
        bar.add(who);                                   // ใส่กลุ่มชื่อ
        return top;                                     // ส่งกลับ
    }

    public JPanel createContent() {                     // พื้นที่การ์ด 3 ใบ
        JPanel content = new JPanel(new GridBagLayout()); // ใช้ GridBag วางการ์ดเป็นตาราง
        content.setBackground(BG);                      // สีพื้นหลัง
        GridBagConstraints c = new GridBagConstraints();// ตัวกำหนดตำแหน่งและขนาดในตาราง
        c.fill = GridBagConstraints.BOTH;               // ให้การ์ดขยายเต็มช่อง

        c.gridx = 0; c.gridy = 0; c.weightx = 0.6;      // ช่องซ้ายบน กว้าง 60%
        c.insets = new Insets(28, 40, 24, 24);          // ระยะห่างรอบการ์ด (บน ซ้าย ล่าง ขวา)
        content.add(createRoomCard(), c);               // การ์ดห้องพัก

        c.gridx = 1; c.gridy = 0; c.weightx = 0.4;      // ช่องขวาบน กว้าง 40%
        c.insets = new Insets(28, 0, 24, 40);           // ช่องขวาเว้นขอบขวา 40
        content.add(createLeaseCard(), c);              // การ์ดสัญญาเช่า (สูงเท่าการ์ดห้อง)

        c.gridx = 0; c.gridy = 1; c.weightx = 0.6;      // ช่องซ้ายล่าง
        c.insets = new Insets(0, 40, 0, 24);            // เว้นซ้าย 40 ขวา 24
        content.add(createRepairCard(), c);             // การ์ดแจ้งซ่อม

        c.gridx = 0; c.gridy = 2; c.weighty = 1.0;      // แถวว่างล่างสุด ดูดพื้นที่ที่เหลือ
        c.weightx = 0;                                  // ไม่ต้องกำหนดความกว้าง
        c.insets = new Insets(0, 0, 30, 0);             // เว้นขอบล่าง 30
        content.add(Box.createGlue(), c);               // ตัวเว้นว่าง ทำให้การ์ดชิดด้านบน
        return content;                                 // ส่งกลับ
    }

    public JPanel createRoomCard() {                    // การ์ด "ข้อมูลห้องพักของฉัน"
        JPanel inner = cardBody("⌂", "ข้อมูลห้องพักของฉัน"); // สร้างเนื้อการ์ดพร้อมหัวเรื่อง
        JPanel body = clear(new BorderLayout(30, 0));   // เนื้อหา: รูปซ้าย ข้อมูลขวา

        ImageBox photo = new ImageBox("src/img/room205.jpg", 12, false, Color.WHITE, GRAY); // รูปห้อง มุมโค้ง
        photo.setPreferredSize(new Dimension(275, 210));// ขนาดรูปห้อง
        body.add(photo, BorderLayout.WEST);             // วางรูปไว้ด้านซ้าย

        JPanel info = clear(null);                      // กลุ่มข้อมูลห้องด้านขวา
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS)); // เรียงแนวตั้ง
        JLabel room = lbl("ห้อง 205", th(Font.BOLD, 26), TEXT); // เลขห้อง
        room.setAlignmentX(Component.LEFT_ALIGNMENT);   // ชิดซ้าย
        info.add(room);                                 // ใส่เลขห้อง
        info.add(Box.createVerticalStrut(6));           // เว้นระยะ
        info.add(infoRow("🏢", "อาคารหอพักหญิง 2"));     // แถวอาคาร
        info.add(infoRow("▭", "ชั้น 2"));                // แถวชั้น
        info.add(Box.createVerticalStrut(6));           // เว้นระยะ
        info.add(pill("กำลังพักอาศัย", PILL_TEXT, 13)); // ป้ายสถานะการพัก
        info.add(Box.createVerticalGlue());             // ดันปุ่มลงไปติดด้านล่าง

        btDetail = new RoundButton("ดูรายละเอียด", true, th(Font.BOLD, 14), TEAL, TEAL_DARK, TEAL_TINT); // ปุ่มทึบสีเขียว
        btDetail.setAlignmentX(Component.LEFT_ALIGNMENT); // ชิดซ้าย
        btDetail.addActionListener(this);               // ให้ส่งเหตุการณ์มาที่ actionPerformed
        info.add(btDetail);                             // ใส่ปุ่ม

        body.add(info, BorderLayout.CENTER);            // ข้อมูลอยู่ตรงกลาง (ขยายเต็มพื้นที่ที่เหลือ)
        inner.add(body, BorderLayout.CENTER);           // ใส่เนื้อหาลงการ์ด
        return makeCard(inner);                         // ห่อด้วยการ์ดสีขาวมุมโค้ง แล้วส่งกลับ
    }

    public JPanel createLeaseCard() {                   // การ์ด "สัญญาเช่า"
        JPanel inner = cardBody("▤", "สัญญาเช่า");      // สร้างเนื้อการ์ดพร้อมหัวเรื่อง
        JPanel body = clear(new BorderLayout(0, 14));   // เนื้อหา: ข้อมูลบน ปุ่มล่าง

        JPanel top = clear(null);                       // กลุ่มข้อมูลด้านบน
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS)); // เรียงแนวตั้ง
        top.add(pill("ใช้งานอยู่", PILL_TEXT, 17));     // ป้ายสถานะสัญญา (ตัวใหญ่)
        top.add(Box.createVerticalStrut(18));           // เว้นระยะ

        JPanel dateRow = clear(new BorderLayout());     // แถววันสิ้นสุดสัญญา
        dateRow.setAlignmentX(Component.LEFT_ALIGNMENT);// ชิดซ้าย
        JPanel dateLine = new JPanel();                 // เส้นคั่นใต้แถว
        dateLine.setBackground(LINE);                   // สีเส้น
        dateLine.setPreferredSize(new Dimension(0, 1)); // สูง 1px
        dateRow.add(dateLine, BorderLayout.SOUTH);      // วางไว้ล่างสุดของแถว

        JPanel dateText = clear(new BorderLayout());    // ส่วนข้อความของแถว
        JPanel left = clear(new FlowLayout(FlowLayout.LEFT, 12, 8)); // ไอคอน + ป้ายชื่อ
        left.add(sym("📅", 20, GRAY));                  // ไอคอนปฏิทิน
        left.add(lbl("สิ้นสุดสัญญา", th(Font.PLAIN, 15), TEXT)); // ป้ายชื่อ
        dateText.add(left, BorderLayout.WEST);          // วางไว้ซ้าย
        dateText.add(lbl("31 พ.ค. 2569   ", th(Font.BOLD, 16), TEXT), BorderLayout.EAST); // วันที่ชิดขวา
        dateRow.add(dateText, BorderLayout.CENTER);     // ใส่ข้อความลงแถว
        top.add(dateRow);                               // ใส่แถววันที่
        body.add(top, BorderLayout.NORTH);              // ข้อมูลไว้ด้านบนของเนื้อหา

        btLease = new RoundButton("ดูสัญญา", false, th(Font.BOLD, 14), TEAL, TEAL_DARK, TEAL_TINT); // ปุ่มแบบขอบเขียว
        btLease.addActionListener(this);                // ให้ส่งเหตุการณ์มาที่ actionPerformed
        body.add(btLease, BorderLayout.SOUTH);          // ปุ่มไว้ด้านล่างสุด

        inner.add(body, BorderLayout.CENTER);           // ใส่เนื้อหาลงการ์ด
        return makeCard(inner);                         // ห่อด้วยการ์ดสีขาวมุมโค้ง แล้วส่งกลับ
    }

    public JPanel createRepairCard() {                  // การ์ด "แจ้งซ่อม / ร้องเรียน"
        JPanel inner = cardBody("🔧", "แจ้งซ่อม / ร้องเรียน"); // สร้างเนื้อการ์ดพร้อมหัวเรื่อง

        RoundPanel tint = new RoundPanel(new FlowLayout(FlowLayout.LEFT, 22, 14), new Color(0xEEF6F5), null, 12); // กล่องสรุปพื้นเขียวอ่อน
        tint.add(sym("▤", 30, TEAL));                   // ไอคอนเอกสาร

        JPanel count = clear(null);                     // กลุ่มตัวเลขจำนวนรายการ
        count.setLayout(new BoxLayout(count, BoxLayout.Y_AXIS)); // เรียงแนวตั้ง
        count.add(lbl("2", th(Font.BOLD, 24), TEXT));   // จำนวนรายการ
        count.add(lbl("รายการ", th(Font.PLAIN, 14), TEXT)); // หน่วย
        tint.add(count);                                // ใส่กลุ่มตัวเลข

        JPanel divider = new JPanel();                  // เส้นคั่นแนวตั้ง
        divider.setBackground(new Color(0xD0DEDB));     // สีเส้น
        divider.setPreferredSize(new Dimension(1, 44)); // กว้าง 1px สูง 44px
        tint.add(divider);                              // ใส่เส้นคั่น

        JPanel status = clear(null);                    // กลุ่มสถานะ 2 บรรทัด
        status.setLayout(new BoxLayout(status, BoxLayout.Y_AXIS)); // เรียงแนวตั้ง
        status.add(statusLine(YELLOW, "กำลังดำเนินการ", "1")); // บรรทัดสถานะ: จุดเหลือง
        status.add(statusLine(Color.GRAY, "เสร็จสิ้น", "1"));   // บรรทัดสถานะ: จุดเทา
        tint.add(status);                               // ใส่กลุ่มสถานะ

        JPanel center = clear(new BorderLayout());      // ตัวห่อกล่องสรุป
        center.add(tint, BorderLayout.NORTH);           // วางไว้ด้านบนเพื่อไม่ให้ยืดสูงเกิน
        inner.add(center, BorderLayout.CENTER);         // ใส่ลงการ์ด

        btTrack = new RoundButton("ติดตามสถานะ", false, th(Font.BOLD, 14), TEAL, TEAL_DARK, TEAL_TINT); // ปุ่มแบบขอบเขียว
        btTrack.addActionListener(this);                // ให้ส่งเหตุการณ์มาที่ actionPerformed
        inner.add(btTrack, BorderLayout.SOUTH);         // ปุ่มไว้ล่างสุดของการ์ด
        return makeCard(inner);                         // ห่อด้วยการ์ดสีขาวมุมโค้ง แล้วส่งกลับ
    }

    public JPanel statusLine(Color dot, String text, String n) { // บรรทัดสถานะ: จุด + ข้อความ + ตัวเลข
        JPanel row = clear(new FlowLayout(FlowLayout.LEFT, 8, 2)); // เรียงซ้ายไปขวา
        row.add(sym("●", 10, dot));                     // จุดสี
        row.add(lbl(text, th(Font.PLAIN, 13), GRAY));   // ข้อความสถานะ
        row.add(lbl(n, th(Font.BOLD, 13), TEAL));       // จำนวน
        return row;                                     // ส่งกลับ
    }

    // ---------------------------------------------------------------
    // เมธอดช่วยสร้างคอมโพเนนต์
    // ---------------------------------------------------------------

    // เลือกฟอนต์ไทยตัวแรกที่ติดตั้งอยู่ในเครื่อง (เรียงตามลำดับความสวย)
    public String pickFont() {
        String[] want = {"Sarabun", "Prompt", "Noto Sans Thai", "Leelawadee UI", "Tahoma"}; // ฟอนต์ที่อยากใช้ ตามลำดับ
        String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(); // ฟอนต์ที่มีในเครื่อง
        for (String name : want) {                      // ไล่ดูทีละชื่อที่อยากใช้ (For-each)
            for (String f : installed) {                // ไล่ดูฟอนต์ในเครื่อง
                if (f.equals(name)) return name;        // เจอตัวแรกที่ตรงกันก็ใช้ตัวนั้น
            }
        }
        return "SansSerif";                             // ถ้าไม่มีเลย ใช้ฟอนต์พื้นฐานของ Java
    }

    // ฟอนต์ไทยของทั้งโปรแกรม (+1 เพราะตัวอักษรไทยมักดูเล็กกว่าตัวอังกฤษที่ขนาดเท่ากัน)
    public Font th(int style, int size) {
        return new Font(FONT, style, size + 1);         // ระบุชื่อฟอนต์ รูปแบบ และขนาด (บวก 1)
    }

    public JLabel lbl(String text, Font font, Color color) { // สร้างข้อความ
        JLabel l = new JLabel(text);                    // สร้าง JLabel จากข้อความ
        l.setFont(font);                                // กำหนดฟอนต์
        l.setForeground(color);                         // กำหนดสีตัวอักษร
        return l;                                       // ส่งกลับไปใช้งาน
    }

    public JLabel sym(String glyph, int size, Color color) { // สร้างไอคอนแบบสัญลักษณ์
        JLabel l = new JLabel(glyph, SwingConstants.CENTER); // จัดไอคอนไว้กึ่งกลาง
        l.setFont(new Font(SYMBOL, Font.PLAIN, size));  // ใช้ฟอนต์สัญลักษณ์
        l.setForeground(color);                         // กำหนดสี
        return l;                                       // ส่งกลับ
    }

    public JPanel clear(LayoutManager lm) {             // สร้างแผงโปร่งใส
        JPanel p = new JPanel(lm);                      // สร้างแผงพร้อม layout ที่รับมา
        p.setOpaque(false);                             // ไม่ทึบ เพื่อให้เห็นพื้นหลังด้านล่าง
        return p;                                       // ส่งกลับ
    }

    // ป้ายสถานะแบบมีจุดสี เช่น "● ใช้งานอยู่"
    public JComponent pill(String text, Color dot, int size) {
        RoundPanel p = new RoundPanel(new FlowLayout(FlowLayout.CENTER, 10, 6), PILL_BG, null, 24); // พื้นเขียวอ่อนมุมโค้งมาก
        p.add(sym("●", 11, dot));                       // จุดสีหน้าข้อความ
        p.add(lbl(text, th(Font.BOLD, size), PILL_TEXT)); // ข้อความสถานะ
        p.setMaximumSize(p.getPreferredSize());         // ล็อกขนาดไม่ให้ BoxLayout ยืดป้าย
        p.setAlignmentX(Component.LEFT_ALIGNMENT);      // ชิดซ้าย
        return p;                                       // ส่งกลับ
    }

    // แถวข้อมูล: ไอคอนเล็ก + ข้อความ
    public JPanel infoRow(String glyph, String text) {
        JPanel row = clear(new FlowLayout(FlowLayout.LEFT, 10, 2)); // เรียงซ้ายไปขวา
        JLabel ic = sym(glyph, 16, GRAY);               // ไอคอน
        ic.setPreferredSize(new Dimension(24, 22));     // กำหนดความกว้างไอคอนให้ตรงกันทุกแถว
        row.add(ic);                                    // ใส่ไอคอน
        row.add(lbl(text, th(Font.PLAIN, 13), GRAY));   // ใส่ข้อความ
        row.setAlignmentX(Component.LEFT_ALIGNMENT);    // ชิดซ้าย
        return row;                                     // ส่งกลับ
    }

    // เนื้อการ์ด: มีหัวเรื่อง (วงกลมไอคอน + ชื่อการ์ด) ให้ผู้เรียกเติมเนื้อหาต่อที่ CENTER / SOUTH
    public JPanel cardBody(String glyph, String title) {
        JPanel inner = clear(new BorderLayout(0, 14));  // เนื้อการ์ด
        JPanel head = clear(new BorderLayout(16, 0));   // แถวหัวเรื่อง
        head.add(new CircleIcon(glyph, 62, CIRCLE_BG, TEAL, SYMBOL), BorderLayout.WEST); // วงกลมไอคอนทางซ้าย
        head.add(lbl(title, th(Font.BOLD, 18), TEXT), BorderLayout.CENTER); // ชื่อการ์ดตรงกลาง
        inner.add(head, BorderLayout.NORTH);            // วางหัวเรื่องไว้บนสุดของการ์ด
        return inner;                                   // ส่งกลับ ให้ผู้เรียกเติมเนื้อหาต่อ
    }

    // การ์ดสีขาวขอบบางมุมโค้ง เว้นขอบด้านในด้วย strut (บน 16 ล่าง 20 ซ้าย 20 ขวา 20)
    public RoundPanel makeCard(JPanel inner) {
        RoundPanel c = new RoundPanel(new BorderLayout(), Color.WHITE, new Color(0xE3E8E8), 14); // การ์ดขาวขอบบาง
        c.add(Box.createVerticalStrut(16), BorderLayout.NORTH);   // เว้นขอบบน
        c.add(Box.createVerticalStrut(20), BorderLayout.SOUTH);   // เว้นขอบล่าง
        c.add(Box.createHorizontalStrut(20), BorderLayout.WEST);  // เว้นขอบซ้าย
        c.add(Box.createHorizontalStrut(20), BorderLayout.EAST);  // เว้นขอบขวา
        c.add(inner, BorderLayout.CENTER);              // เนื้อการ์ดอยู่ตรงกลาง
        return c;                                       // ส่งกลับ
    }
}

// =====================================================================
// คลาสที่วาดเอง: สืบทอด JPanel / JButton แล้ว override paintComponent
// (วางไว้ท้ายไฟล์เดียวกัน แบบเดียวกับ Form1 + Draw1 ในสไลด์)
// วาดด้วย fillRect / fillOval / drawLine / drawArc / fillPolygon ตามที่เรียนเท่านั้น
// =====================================================================

// ตัวช่วยวาดสี่เหลี่ยมมุมโค้ง จากสี่เหลี่ยม + วงกลม 4 มุม + เส้นตรง + ส่วนโค้ง
class RoundDraw {
    // ระบายสี่เหลี่ยมมุมโค้ง (arc = เส้นผ่านศูนย์กลางของมุมโค้ง)
    public void fill(Graphics g, int x, int y, int w, int h, int arc) {
        int r = Math.min(arc / 2, Math.min(w, h) / 2); // รัศมีมุมโค้ง
        g.fillRect(x + r, y, w - 2 * r, h);             // แถบกลางแนวตั้ง
        g.fillRect(x, y + r, w, h - 2 * r);             // แถบกลางแนวนอน
        g.fillOval(x, y, 2 * r, 2 * r);                 // มุมซ้ายบน
        g.fillOval(x + w - 2 * r, y, 2 * r, 2 * r);     // มุมขวาบน
        g.fillOval(x, y + h - 2 * r, 2 * r, 2 * r);     // มุมซ้ายล่าง
        g.fillOval(x + w - 2 * r, y + h - 2 * r, 2 * r, 2 * r); // มุมขวาล่าง
    }

    // วาดเส้นขอบสี่เหลี่ยมมุมโค้ง
    public void outline(Graphics g, int x, int y, int w, int h, int arc) {
        int r = Math.min(arc / 2, Math.min(w, h) / 2); // รัศมีมุมโค้ง
        g.drawLine(x + r, y, x + w - r, y);             // เส้นบน
        g.drawLine(x + r, y + h, x + w - r, y + h);     // เส้นล่าง
        g.drawLine(x, y + r, x, y + h - r);             // เส้นซ้าย
        g.drawLine(x + w, y + r, x + w, y + h - r);     // เส้นขวา
        g.drawArc(x, y, 2 * r, 2 * r, 90, 90);          // โค้งซ้ายบน
        g.drawArc(x + w - 2 * r, y, 2 * r, 2 * r, 0, 90);   // โค้งขวาบน
        g.drawArc(x, y + h - 2 * r, 2 * r, 2 * r, 180, 90); // โค้งซ้ายล่าง
        g.drawArc(x + w - 2 * r, y + h - 2 * r, 2 * r, 2 * r, 270, 90); // โค้งขวาล่าง
    }
}

// แผงมุมโค้ง มีสีพื้นและเส้นขอบ (เส้นขอบเป็น null ได้)
class RoundPanel extends JPanel {
    private Color fill, line;                           // สีพื้นและสีเส้นขอบ
    private int arc;                                    // ความโค้งของมุม
    private RoundDraw rd = new RoundDraw();             // ตัวช่วยวาดมุมโค้ง

    public RoundPanel(LayoutManager lm, Color fill, Color line, int arc) {
        super(lm);                                      // ส่ง layout ให้ JPanel
        this.fill = fill;                               // เก็บสีพื้น
        this.line = line;                               // เก็บสีขอบ
        this.arc = arc;                                 // เก็บความโค้ง
        setOpaque(false);                               // ปิดการทาสีพื้นเดิมของ JPanel เพราะเราวาดเอง
    }

    public void paintComponent(Graphics g) {            // เมธอดที่ Swing เรียกเมื่อต้องวาด
        g.setColor(fill);                               // ตั้งสีพื้น
        rd.fill(g, 0, 0, getWidth() - 1, getHeight() - 1, arc); // ระบายสี่เหลี่ยมมุมโค้ง
        if (line != null) {                             // ถ้ากำหนดสีขอบไว้
            g.setColor(line);                           // ตั้งสีขอบ
            rd.outline(g, 0, 0, getWidth() - 1, getHeight() - 1, arc); // วาดเส้นขอบ
        }
        super.paintComponent(g);                        // ให้ JPanel วาดส่วนที่เหลือตามปกติ
    }
}

// ปุ่มมุมโค้ง: filled=true พื้นเขียว / false พื้นขาวขอบเขียว (เปลี่ยนสีเมื่อเมาส์ชี้ด้วย MouseListener)
class RoundButton extends JButton implements MouseListener {
    private boolean filled;                             // เก็บว่าเป็นปุ่มแบบทึบหรือแบบขอบ
    private boolean hover = false;                      // เมาส์ชี้ปุ่มอยู่หรือไม่
    private Color teal, tealDark, tealTint;             // สีที่ใช้วาดปุ่ม
    private RoundDraw rd = new RoundDraw();             // ตัวช่วยวาดมุมโค้ง

    public RoundButton(String text, boolean filled, Font font, Color teal, Color tealDark, Color tealTint) {
        super(text + "   ›");                           // ข้อความปุ่ม ตามด้วยลูกศร
        this.filled = filled;                           // เก็บชนิดปุ่ม
        this.teal = teal;                               // เก็บสีเขียว
        this.tealDark = tealDark;                       // เก็บสีเขียวเข้ม
        this.tealTint = tealTint;                       // เก็บสีเขียวอ่อน
        setFont(font);                                  // ฟอนต์ตัวหนา
        setForeground(filled ? Color.WHITE : teal);     // ตัวอักษรขาวถ้าปุ่มทึบ ไม่งั้นเขียว
        setFocusPainted(false);                         // ไม่วาดกรอบโฟกัสเวลาคลิก
        setBorderPainted(false);                        // ไม่ใช้เส้นขอบมาตรฐาน
        setContentAreaFilled(false);                    // ไม่ใช้พื้นปุ่มมาตรฐาน
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // เมาส์เป็นรูปมือ
        setPreferredSize(new Dimension(100, 52));       // ความสูงปุ่ม 52px
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 52)); // ยืดเต็มความกว้างได้ใน BoxLayout
        addMouseListener(this);                         // รับเหตุการณ์เมาส์เข้า/ออก
    }

    public void paintComponent(Graphics g) {
        if (filled) {                                   // ปุ่มแบบทึบ
            g.setColor(hover ? tealDark : teal);        // เปลี่ยนสีเมื่อชี้
            rd.fill(g, 0, 0, getWidth(), getHeight(), 12); // ระบายพื้น
        } else {                                        // ปุ่มแบบขอบ
            g.setColor(hover ? tealTint : Color.WHITE); // พื้นขาว (ชี้แล้วเป็นเขียวอ่อน)
            rd.fill(g, 0, 0, getWidth(), getHeight(), 12); // ระบายพื้น
            g.setColor(teal);                           // สีเส้นขอบ
            rd.outline(g, 0, 0, getWidth() - 1, getHeight() - 1, 12); // เส้นขอบชั้นนอก
            rd.outline(g, 1, 1, getWidth() - 3, getHeight() - 3, 10); // เส้นขอบชั้นใน (ทำให้เส้นหนาขึ้น)
        }
        super.paintComponent(g);                        // วาดข้อความบนปุ่ม
    }

    // เมธอดของ MouseListener (ต้อง Override ครบทุกตัว)
    public void mouseEntered(MouseEvent e) { hover = true;  repaint(); } // เมาส์เข้า -> เปลี่ยนสี
    public void mouseExited(MouseEvent e)  { hover = false; repaint(); } // เมาส์ออก -> กลับสีเดิม
    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
}

// ปุ่มเมนูด้านซ้าย: พื้นมุมโค้ง แล้วผู้สร้างค่อยเอาไอคอน + ชื่อเมนูมาใส่ (bg = สีพื้นของปุ่ม)
class MenuButton extends JButton {
    private Color bg;                                   // สีพื้นของปุ่ม
    private RoundDraw rd = new RoundDraw();             // ตัวช่วยวาดมุมโค้ง

    public MenuButton(Color bg) {
        this.bg = bg;                                   // เก็บสีพื้น
        setLayout(new FlowLayout(FlowLayout.LEFT, 14, 13)); // เรียงไอคอน+ชื่อ ซ้ายไปขวา
        setPreferredSize(new Dimension(250, 54));       // ขนาดแต่ละเมนู
        setMaximumSize(new Dimension(250, 54));         // ไม่ให้ยืดเกินนี้
        setFocusPainted(false);                         // ไม่วาดกรอบโฟกัสเวลาคลิก
        setBorderPainted(false);                        // ไม่ใช้เส้นขอบมาตรฐาน
        setContentAreaFilled(false);                    // ไม่ใช้พื้นปุ่มมาตรฐาน
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // เมาส์เป็นรูปมือ
    }

    public void paintComponent(Graphics g) {
        g.setColor(bg);                                 // พื้นเขียวอ่อนถ้าเลือกอยู่ ไม่งั้นขาว
        rd.fill(g, 0, 0, getWidth() - 1, getHeight() - 1, 12); // ระบายพื้นมุมโค้ง
        super.paintComponent(g);                        // วาดส่วนที่เหลือของปุ่ม
    }
}

// วงกลมสีเขียวอ่อนมีไอคอนอยู่กลาง (วงกลมวาดด้วย fillOval ส่วนไอคอนเป็น JLabel วางกึ่งกลาง)
class CircleIcon extends JPanel {
    private Color circleColor;                          // สีวงกลม

    public CircleIcon(String glyph, int d, Color circleColor, Color iconColor, String symbolFont) {
        super(new BorderLayout());                      // ใช้ BorderLayout เพื่อวางไอคอนกลางวงกลม
        this.circleColor = circleColor;                 // เก็บสีวงกลม
        setOpaque(false);                               // ไม่ทาสีพื้นเดิม
        setPreferredSize(new Dimension(d, d));          // กำหนดขนาดที่ต้องการ
        JLabel ic = new JLabel(glyph, SwingConstants.CENTER); // ไอคอนจัดกึ่งกลาง
        ic.setFont(new Font(symbolFont, Font.PLAIN, d / 2)); // ขนาดไอคอนครึ่งหนึ่งของวงกลม
        ic.setForeground(iconColor);                    // สีไอคอน
        add(ic, BorderLayout.CENTER);                   // วางไว้ตรงกลาง
    }

    public void paintComponent(Graphics g) {
        g.setColor(circleColor);                        // สีวงกลม
        g.fillOval(0, 0, getWidth() - 1, getHeight() - 1); // ระบายวงกลม
        super.paintComponent(g);                        // วาดส่วนที่เหลือ
    }
}

// กล่องแสดงรูปภาพ: circle=true ตัดเป็นวงกลม / false ตัดเป็นสี่เหลี่ยมมุมโค้ง (ถ้าไม่มีไฟล์รูปจะวาดสีพื้นแทน)
// การตัดมุมทำโดยวาดรูป แล้วระบายสีพื้นหลัง (bg) ทับมุมด้วย fillPolygon
class ImageBox extends JPanel {
    private String path;                                // ที่อยู่ไฟล์รูป
    private int arc;                                    // ความโค้งของมุม (ใช้เมื่อ circle=false)
    private boolean circle;                             // ตัดเป็นวงกลมหรือไม่
    private Color bg;                                   // สีพื้นหลังที่อยู่ด้านหลังรูป (ใช้ระบายทับมุม)
    private Color textColor;                            // สีข้อความบอกชื่อไฟล์
    private Image img;                                  // รูปที่โหลดมา
    private boolean found;                              // เจอไฟล์รูปหรือไม่

    public ImageBox(String path, int arc, boolean circle, Color bg, Color textColor) {
        this.path = path;                               // เก็บที่อยู่ไฟล์
        this.arc = arc;                                 // เก็บความโค้ง
        this.circle = circle;                           // เก็บชนิดการตัดรูป
        this.bg = bg;                                   // เก็บสีพื้นหลัง
        this.textColor = textColor;                     // เก็บสีข้อความ
        setOpaque(false);                               // ไม่ทาสีพื้นเดิม
        ImageIcon icon = new ImageIcon(path);           // โหลดรูป (ถ้าไม่มีไฟล์ ความกว้างจะเป็น -1)
        this.img = icon.getImage();                     // เก็บรูป
        this.found = icon.getIconWidth() > 0;           // มีไฟล์รูปจริงหรือไม่
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);                        // วาดส่วนพื้นฐานของ JPanel
        int w = getWidth();                             // ความกว้างกล่อง
        int h = getHeight();                            // ความสูงกล่อง
        if (found) {                                    // มีรูป
            g.drawImage(img, 0, 0, w, h, this);         // วาดรูปเต็มกล่อง
        } else {                                        // ไม่มีรูป ใช้ที่ว่างแทน
            g.setColor(circle ? new Color(0xCBD5D1) : new Color(0xE5E0D5)); // สีพื้นกันที่
            g.fillRect(0, 0, w, h);                     // ระบายพื้น
            if (!circle) {                              // รูปสี่เหลี่ยมมีที่พอจะบอกชื่อไฟล์
                g.setColor(textColor);                  // สีข้อความ
                g.drawString(path, w / 2 - 35, h / 2);  // บอกชื่อไฟล์ที่ต้องวาง
            }
        }
        int r = circle ? Math.min(w, h) / 2 : arc / 2;  // รัศมีมุมที่จะตัด (วงกลม = ครึ่งหนึ่งของด้านสั้น)
        if (r > 0) {                                    // ถ้ามีมุมให้ตัด
            g.setColor(bg);                             // ใช้สีพื้นหลังระบายทับมุม
            maskCorner(g, 0, 0, r, r, -1, -1, r);       // มุมซ้ายบน
            maskCorner(g, w, 0, w - r, r, 1, -1, r);    // มุมขวาบน
            maskCorner(g, 0, h, r, h - r, -1, 1, r);    // มุมซ้ายล่าง
            maskCorner(g, w, h, w - r, h - r, 1, 1, r); // มุมขวาล่าง
        }
    }

    // ระบายทับมุม 1 มุม: รูปหลายเหลี่ยมที่มีจุดมุมกล่อง + จุดบนส่วนโค้งของวงกลม
    // (px,py) = จุดมุมกล่อง, (cx,cy) = จุดศูนย์กลางส่วนโค้ง, sx/sy = ทิศของมุม (-1 หรือ 1)
    private void maskCorner(Graphics g, int px, int py, int cx, int cy, int sx, int sy, int r) {
        Polygon p = new Polygon();                      // รูปหลายเหลี่ยมของมุม
        p.addPoint(px, py);                             // จุดมุมกล่อง
        for (int a = 0; a <= 90; a += 5) {              // ไล่จุดบนส่วนโค้งทีละ 5 องศา
            double t = Math.toRadians(a);               // แปลงองศาเป็นเรเดียน
            p.addPoint((int) Math.round(cx + sx * r * Math.cos(t)),
                       (int) Math.round(cy + sy * r * Math.sin(t))); // จุดบนส่วนโค้ง
        }
        g.fillPolygon(p);                               // ระบายรูปหลายเหลี่ยม
    }
}
