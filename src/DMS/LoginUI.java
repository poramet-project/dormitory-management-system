package DMS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class LoginUI extends JFrame {
    private Image bgImg;

    public LoginUI() {
        setTitle("KU Dormitory - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        loadBgImage();

        // Main Background Panel
        JPanel bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                if (bgImg != null) {
                    g2.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2.setColor(Color.decode("#1E1E1E"));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bgPanel.setLayout(new GridBagLayout());
        setContentPane(bgPanel);
        bgPanel.add(createGlassCard());
    }

    private void loadBgImage() {
        String name = "content.png";
        var url = getClass().getResource("/img/" + name);
        if (url == null) url = getClass().getClassLoader().getResource("img/" + name);

        if (url != null) {
            bgImg = new ImageIcon(url).getImage();
        } else {
            File f = new File("src/img/" + name);
            if (!f.exists()) f = new File("img/" + name);
            if (f.exists()) bgImg = new ImageIcon(f.getAbsolutePath()).getImage();
        }
    }

    private JPanel createGlassCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 36, 36);
                // Glass Body
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 175), 0, getHeight(), new Color(255, 255, 255, 120)));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 32, 32);
                // Glass Border
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 240), getWidth(), getHeight(), new Color(255, 255, 255, 80)));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 32, 32);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(390, 490));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Components
        JTextField userField = createField("👤  Username", false);
        JPasswordField passField = (JPasswordField) createField("🔑  Password", true);
        JButton loginBtn = createButton("Log in");
        JLabel signUp = new JLabel("Don't have an account? Sign Up");

        signUp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signUp.setForeground(Color.decode("#06685d"));
        signUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUp.setAlignmentX(CENTER_ALIGNMENT);

        // -------------------------------------------------------------
        // ดักจับการคลิกที่ปุ่ม Sign Up -> ปิดหน้าเดิม แล้ว new หน้าใหม่
        // -------------------------------------------------------------
        signUp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose(); // ปิดหน้า LoginUI ปัจจุบัน
                
                // เปลี่ยนเป็นชื่อคลาสหน้า Register/SignUp ของคุณตรงนี้ได้เลย เช่น new RegisterUI().setVisible(true);
                new SignUpUI().setVisible(true); 
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                signUp.setForeground(Color.decode("#004D40")); // เปลี่ยนสีเมื่อ Hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                signUp.setForeground(Color.decode("#06685d"));
            }
        });

        content.add(createLogo());
        content.add(Box.createVerticalStrut(12));
        content.add(createTitle());
        content.add(Box.createVerticalStrut(26));
        content.add(userField);
        content.add(Box.createVerticalStrut(14));
        content.add(passField);
        content.add(Box.createVerticalStrut(24));
        content.add(loginBtn);
        content.add(Box.createVerticalStrut(24));
        content.add(signUp);

        card.setLayout(new GridBagLayout());
        card.add(content);
        return card;
    }

    private JPanel createLogo() {
        JPanel logo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Color.decode("#00897B"), 0, getHeight(), Color.decode("#004D40")));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(88, 88));
        logo.setMaximumSize(new Dimension(88, 88));
        logo.setAlignmentX(CENTER_ALIGNMENT);
        logo.setLayout(new GridBagLayout());

        JLabel lbl = new JLabel("KU");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lbl.setForeground(Color.WHITE);
        logo.add(lbl);
        return logo;
    }

    private JPanel createTitle() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(CENTER_ALIGNMENT);

        JLabel d = new JLabel("Dormi"), t = new JLabel("tory");
        d.setFont(new Font("Segoe UI", Font.BOLD, 23)); d.setForeground(Color.decode("#1B2A2F"));
        t.setFont(new Font("Segoe UI", Font.BOLD, 23)); t.setForeground(Color.decode("#00796B"));
        p.add(d); p.add(t);
        return p;
    }

    private JTextField createField(String ph, boolean isPass) {
        final boolean[] focus = {false};

        JTextField f = isPass ? new JPasswordField(ph) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(focus[0] ? Color.decode("#00796B") : new Color(210, 210, 210));
                g2.setStroke(new BasicStroke(focus[0] ? 1.8f : 1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        } : new JTextField(ph) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(focus[0] ? Color.decode("#00796B") : new Color(210, 210, 210));
                g2.setStroke(new BasicStroke(focus[0] ? 1.8f : 1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        f.setOpaque(false);
        f.setFont(new Font("Tahoma", Font.PLAIN, 13));
        f.setCaretColor(Color.decode("#00796B"));
        f.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        f.setPreferredSize(new Dimension(310, 40));
        f.setMaximumSize(new Dimension(310, 40));
        f.setAlignmentX(CENTER_ALIGNMENT);
        f.setForeground(Color.decode("#777777"));

        if (isPass) ((JPasswordField) f).setEchoChar((char) 0);

        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focus[0] = true;
                if (f.getText().equals(ph)) {
                    f.setText("");
                    if (isPass) ((JPasswordField) f).setEchoChar('•');
                    f.setForeground(Color.decode("#212121"));
                }
                f.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focus[0] = false;
                if (f.getText().isEmpty()) {
                    if (isPass) ((JPasswordField) f).setEchoChar((char) 0);
                    f.setText(ph);
                    f.setForeground(Color.decode("#777777"));
                }
                f.repaint();
            }
        });
        return f;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = hovered ? Color.decode("#00897B") : Color.decode("#00796B");
                Color c2 = hovered ? Color.decode("#004D40") : Color.decode("#005B4F");
                g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), 0, c2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(310, 42));
        btn.setMaximumSize(new Dimension(310, 42));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        return btn;
    }
}