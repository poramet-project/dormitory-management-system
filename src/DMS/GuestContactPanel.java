package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GuestContactPanel extends JPanel {
    public GuestContactPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(247, 249, 251));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("ติดต่อสำนักงานหอพัก KU Dormitory");
        title.setFont(new Font("Tahoma", Font.BOLD, 22));
        title.setForeground(new Color(23, 32, 53));
        content.add(title);
        content.add(Box.createVerticalStrut(15));

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(223, 231, 241), 1),
                new EmptyBorder(20, 25, 20, 25)
        ));

        box.add(createItem("📍 ที่ตั้งสำนักงาน:", "อาคารสำนักงานหอพัก มหาวิทยาลัยเกษตรศาสตร์ วิทยาเขตกำแพงแสน"));
        box.add(Box.createVerticalStrut(10));
        box.add(createItem("📞 เบอร์โทรศัพท์:", "034-351-XXX หรือ 081-XXX-XXXX (เวลาทำการ 08:30 - 16:30 น.)"));
        box.add(Box.createVerticalStrut(10));
        box.add(createItem("✉️ อีเมล:", "dormitory@ku.th"));
        box.add(Box.createVerticalStrut(10));
        box.add(createItem("🕒 เวลาเปิด-ปิดหอพัก:", "ประตูหอพักเปิด 05:30 น. ปิด 23:00 น. (สแกนคีย์การ์ด)"));

        content.add(box);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createItem(String header, String detail) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JLabel h = new JLabel(header);
        h.setFont(new Font("Tahoma", Font.BOLD, 14));
        JLabel d = new JLabel(detail);
        d.setFont(new Font("Tahoma", Font.PLAIN, 14));
        p.add(h);
        p.add(d);
        return p;
    }
}