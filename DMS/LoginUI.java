package DMS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class LoginUI extends JFrame {

    private Image backgroundImage;
    // กำหนดชื่อไฟล์รูปภาพ
    private final String IMAGE_NAME = "content.png";

    public LoginUI() {
        initUI();
    }

    private void initUI() {
        setTitle("KU Dormitory - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // โหลดรูปภาพพื้นหลัง พร้อมระบบค้นหาหลายเส้นทาง (Fallback System)
        loadImage();

        // Background Panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                
                // วาดภาพพื้นหลัง
                if (backgroundImage != null && backgroundImage.getWidth(null) > 0) {
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // หากยังหาไม่เจอจริงๆ จะใช้สีมืดแทน
                    g2.setColor(Color.decode("#1E1E1E"));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                // Overlay มืดบางๆ เพิ่มมิติให้กระจก
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
                super.paintComponent(g);
            }
        };

        backgroundPanel.setOpaque(false);
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        backgroundPanel.add(createGlassCard());
    }

    /**
     * ระบบค้นหาและโหลดรูปภาพ ป้องกันปัญหารูปไม่ขึ้น
     */
    private void loadImage() {
        java.net.URL imgURL = null;

        // 1. ค้นหาจาก Class Path (/img/content.png)
        imgURL = getClass().getResource("/img/" + IMAGE_NAME);

        // 2. ถ้าไม่เจอ ค้นหาผ่าน ClassLoader (img/content.png)
        if (imgURL == null) {
            imgURL = getClass().getClassLoader().getResource("img/" + IMAGE_NAME);
        }

        // 3. ถ้าไม่เจอ ค้นหาตรงๆ จาก root
        if (imgURL == null) {
            imgURL = getClass().getResource("/" + IMAGE_NAME);
        }

        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            backgroundImage = icon.getImage();
            System.out.println("✅ โหลดรูปภาพสำเร็จจาก URL: " + imgURL);
        } else {
            // 4. ค้นหาจาก File System ในเครื่อง (เผื่อกรณีรันสคริปต์สด)
            File localFile = new File("src/img/" + IMAGE_NAME);
            if (!localFile.exists()) {
                localFile = new File("img/" + IMAGE_NAME);
            }

            if (localFile.exists()) {
                backgroundImage = new ImageIcon(localFile.getAbsolutePath()).getImage();
                System.out.println("✅ โหลดรูปภาพสำเร็จจาก File Path: " + localFile.getAbsolutePath());
            } else {
                System.out.println("❌ ไม่พบไฟล์รูปภาพ! กรุณาตรวจสอบว่ามีไฟล์ 'content.png' อยู่ในโฟลเดอร์ 'src/img/' หรือไม่");
            }
        }

        // ดักจับรอให้ภาพโหลดเข้า memory สมบูรณ์ ป้องกันปัญหาวาดรูปไม่ทัน
        if (backgroundImage != null) {
            MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(backgroundImage, 0);
            try {
                tracker.waitForID(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private JPanel createGlassCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 1. Drop Shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 36, 36);

                // 2. Glass Background
                GradientPaint glassGradient = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 175),
                        0, getHeight(), new Color(255, 255, 255, 120)
                );
                g2.setPaint(glassGradient);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 32, 32);

                // 3. Border Rim
                GradientPaint borderGradient = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 240),
                        getWidth(), getHeight(), new Color(255, 255, 255, 80)
                );
                g2.setPaint(borderGradient);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 32, 32);

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setPreferredSize(new Dimension(390, 490));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 1. Logo Badge
        JPanel logoBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 26, 26);

                GradientPaint logoGrad = new GradientPaint(
                        0, 0, Color.decode("#00897B"),
                        0, getHeight(), Color.decode("#004D40")
                );
                g2.setPaint(logoGrad);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                
                g2.setColor(new Color(255, 255, 255, 220));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 23, 23);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setOpaque(false);
        logoBadge.setPreferredSize(new Dimension(88, 88));
        logoBadge.setMaximumSize(new Dimension(88, 88));
        logoBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoBadge.setLayout(new GridBagLayout());

        JLabel kuLabel = new JLabel("KU");
        kuLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        kuLabel.setForeground(Color.WHITE);
        logoBadge.add(kuLabel);

        // 2. Title Text
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleDorm = new JLabel("Dormi");
        titleDorm.setFont(new Font("Segoe UI", Font.BOLD, 23));
        titleDorm.setForeground(Color.decode("#1B2A2F"));
        
        JLabel titleTory = new JLabel("tory");
        titleTory.setFont(new Font("Segoe UI", Font.BOLD, 23));
        titleTory.setForeground(Color.decode("#00796B"));

        titlePanel.add(titleDorm);
        titlePanel.add(titleTory);

        // 3. Inputs
        JTextField userField = createModernTextField("\uD83D\uDC64  Username");
        JPasswordField passField = createModernPasswordField("\uD83D\uDD11  Password");

        // 4. Login Button
        JButton loginBtn = new JButton("Log in") {
            private boolean isHovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color c1 = isHovered ? Color.decode("#00897B") : Color.decode("#00796B");
                Color c2 = isHovered ? Color.decode("#004D40") : Color.decode("#005B4F");

                GradientPaint btnGradient = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
                g2.setPaint(btnGradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(310, 42));
        loginBtn.setMaximumSize(new Dimension(310, 42));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 5. Sign Up Link
        JLabel signUpLabel = new JLabel("Don't have an account? Sign Up") {
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        setForeground(Color.decode("#004D40"));
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        setForeground(Color.decode("#00796B"));
                    }
                });
            }
        };
        signUpLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signUpLabel.setForeground(Color.decode("#06685d"));
        signUpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ประกอบ UI
        contentPanel.add(logoBadge);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(titlePanel);
        contentPanel.add(Box.createVerticalStrut(26));
        contentPanel.add(userField);
        contentPanel.add(Box.createVerticalStrut(14));
        contentPanel.add(passField);
        contentPanel.add(Box.createVerticalStrut(24));
        contentPanel.add(loginBtn);
        contentPanel.add(Box.createVerticalStrut(24));
        contentPanel.add(signUpLabel);

        card.setLayout(new GridBagLayout());
        card.add(contentPanel);

        return card;
    }

    private JTextField createModernTextField(String placeholder) {
        final boolean[] isFocused = {false};
        
        JTextField field = new JTextField(placeholder) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(255, 255, 255, 190));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                if (isFocused[0]) {
                    g2.setColor(Color.decode("#00796B"));
                    g2.setStroke(new BasicStroke(1.8f));
                } else {
                    g2.setColor(new Color(255, 255, 255, 230));
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        setupFieldProps(field);
        
        field.setForeground(Color.decode("#777777"));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused[0] = true;
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.decode("#212121"));
                }
                field.repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                isFocused[0] = false;
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.decode("#777777"));
                }
                field.repaint();
            }
        });
        return field;
    }

    private JPasswordField createModernPasswordField(String placeholder) {
        final boolean[] isFocused = {false};
        
        JPasswordField field = new JPasswordField(placeholder) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(255, 255, 255, 190));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                if (isFocused[0]) {
                    g2.setColor(Color.decode("#00796B"));
                    g2.setStroke(new BasicStroke(1.8f));
                } else {
                    g2.setColor(new Color(255, 255, 255, 230));
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        setupFieldProps(field);
        
        final char defaultEchoChar = field.getEchoChar();
        field.setEchoChar((char) 0);
        field.setForeground(Color.decode("#777777"));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused[0] = true;
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar(defaultEchoChar);
                    field.setForeground(Color.decode("#212121"));
                }
                field.repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                isFocused[0] = false;
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(Color.decode("#777777"));
                }
                field.repaint();
            }
        });
        return field;
    }

    private void setupFieldProps(JTextField field) {
        field.setOpaque(false);
        field.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 13));
        field.setCaretColor(Color.decode("#00796B"));
        field.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        field.setPreferredSize(new Dimension(310, 40));
        field.setMaximumSize(new Dimension(310, 40));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}