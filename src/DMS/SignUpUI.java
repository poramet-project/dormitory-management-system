package DMS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class SignUpUI extends JFrame {
    private Image bgImg;
    private JTextField userField;
    private JPasswordField passField;
    private JTextField nameField;
    private JTextField phoneField;
    private JButton signUpBtn;

    private static final String THAI_FONT = "Tahoma"; 
    private static final String PH_USER = "👤  ชื่อผู้ใช้ (Username)";
    private static final String PH_PASS = "🔑  รหัสผ่าน (Password)";
    private static final String PH_NAME = "👤  ชื่อ-นามสกุล (Fullname)";
    private static final String PH_PHONE = "📞  เบอร์โทรศัพท์ (Phone)";

    public SignUpUI() {
        setTitle("KU Dormitory - สมัครสมาชิก");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        loadBgImage();

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

                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        bgPanel.setLayout(new BorderLayout());

        // Header ด้านบนขวา: เข้าสู่ระบบ →
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 20));
        topBar.setOpaque(false);
        JLabel topLoginBtn = new JLabel("เข้าสู่ระบบ →");
        topLoginBtn.setFont(new Font(THAI_FONT, Font.BOLD, 15));
        topLoginBtn.setForeground(Color.WHITE);
        topLoginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topLoginBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new LoginUI().setVisible(true);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                topLoginBtn.setForeground(Color.decode("#80CBC4"));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                topLoginBtn.setForeground(Color.WHITE);
            }
        });
        topBar.add(topLoginBtn);
        bgPanel.add(topBar, BorderLayout.NORTH);

        // วางการ์ดไว้ฝั่งซ้าย
        JPanel centerContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 70, 10));
        centerContainer.setOpaque(false);
        centerContainer.add(createGlassCard());

        bgPanel.add(centerContainer, BorderLayout.CENTER);
        setContentPane(bgPanel);
    }

    private void loadBgImage() {
        String name = "content2.png";
        java.net.URL url = getClass().getResource("/img/" + name);
        if (url == null) {
            url = getClass().getClassLoader().getResource("img/" + name);
        }

        if (url != null) {
            bgImg = new ImageIcon(url).getImage();
        } else {
            File f = new File("src/img/" + name);
            if (!f.exists()) f = new File("img/" + name);
            if (!f.exists()) f = new File("DMS/src/img/" + name);
            if (f.exists()) {
                bgImg = new ImageIcon(f.getAbsolutePath()).getImage();
            }
        }
    }

    private JPanel createGlassCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 42, 42);

                GradientPaint glassGrad = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 65),
                        getWidth(), getHeight(), new Color(255, 255, 255, 25)
                );
                g2.setPaint(glassGrad);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 38, 38);

                GradientPaint borderGrad = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 140),
                        getWidth(), getHeight(), new Color(255, 255, 255, 30)
                );
                g2.setPaint(borderGrad);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 38, 38);

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 560));
        card.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // หัวข้อสมัครสมาชิก
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(340, 55));
        headerPanel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel userIcon = new JLabel("👤⁺");
        userIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 32));
        userIcon.setForeground(Color.WHITE);

        JPanel titleTextPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titleTextPanel.setOpaque(false);

        JLabel titleMain = new JLabel("สมัครสมาชิก");
        titleMain.setFont(new Font(THAI_FONT, Font.BOLD, 22));
        titleMain.setForeground(Color.WHITE);

        JLabel titleSub = new JLabel("สร้างบัญชีเพื่อใช้งานระบบจองหอพัก");
        titleSub.setFont(new Font(THAI_FONT, Font.PLAIN, 13));
        titleSub.setForeground(new Color(220, 235, 240));

        titleTextPanel.add(titleMain);
        titleTextPanel.add(titleSub);

        headerPanel.add(userIcon, BorderLayout.WEST);
        headerPanel.add(titleTextPanel, BorderLayout.CENTER);

        // ช่อง Input
        userField = createModernField(PH_USER, false);
        passField = (JPasswordField) createModernField(PH_PASS, true);
        nameField = createModernField(PH_NAME, false);
        phoneField = createModernField(PH_PHONE, false);

        // ตรวจสอบ DocumentFilter ให้ช่องเบอร์โทรศัพท์ (พิมพ์ได้เฉพาะตัวเลข และไม่เกิน 10 หลัก)
        ((AbstractDocument) phoneField.getDocument()).setDocumentFilter(new NumericDocumentFilter(10));

        // ปุ่มสมัครสมาชิก
        signUpBtn = createGradientButton("สมัครสมาชิก");
        signUpBtn.addActionListener(e -> processSignUp());

        // ลิงก์ด้านล่างสุด
        JLabel alreadyLabel = new JLabel("มีบัญชีอยู่แล้ว? เข้าสู่ระบบ");
        alreadyLabel.setFont(new Font(THAI_FONT, Font.BOLD, 13));
        alreadyLabel.setForeground(new Color(235, 245, 245));
        alreadyLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        alreadyLabel.setAlignmentX(CENTER_ALIGNMENT);

        alreadyLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new LoginUI().setVisible(true);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                alreadyLabel.setForeground(Color.decode("#80CBC4"));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                alreadyLabel.setForeground(new Color(235, 245, 245));
            }
        });

        content.add(headerPanel);
        content.add(Box.createVerticalStrut(20));
        content.add(userField);
        content.add(Box.createVerticalStrut(12));
        content.add(passField);
        content.add(Box.createVerticalStrut(12));
        content.add(nameField);
        content.add(Box.createVerticalStrut(12));
        content.add(phoneField);
        content.add(Box.createVerticalStrut(22));
        content.add(signUpBtn);
        content.add(Box.createVerticalStrut(16));
        content.add(alreadyLabel);

        card.add(content);
        return card;
    }

    /**
     * ดักจับและตรวจสอบความถูกต้องของข้อมูล พร้อมจัดการ Exception ทั้งหมด
     */
    private void processSignUp() {
        try {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword()).trim();
            String n = nameField.getText().trim();
            String ph = phoneField.getText().trim();

            //ตรวจสอบค่าว่างหรือยังไม่ได้พิมพ์
            if (u.isEmpty() || u.equals(PH_USER)) {
                throw new IllegalArgumentException("กรุณากรอกชื่อผู้ใช้ (Username)");
            }
            if (p.isEmpty() || p.equals(PH_PASS)) {
                throw new IllegalArgumentException("กรุณากรอกรหัสผ่าน (Password)");
            }
            if (p.length() < 4) {
                throw new IllegalArgumentException("รหัสผ่านต้องมีความยาวอย่างน้อย 4 ตัวอักษร");
            }
            if (n.isEmpty() || n.equals(PH_NAME)) {
                throw new IllegalArgumentException("กรุณากรอกชื่อ-นามสกุล (Fullname)");
            }
            if (ph.isEmpty() || ph.equals(PH_PHONE)) {
                throw new IllegalArgumentException("กรุณากรอกเบอร์โทรศัพท์ (Phone)");
            }

            //ตรวจสอบเงื่อนไขเบอร์โทรศัพท์ (ตัวเลข 9 - 10 หลัก และขึ้นต้นด้วย 0)
            if (!ph.matches("^0[0-9]{8,9}$")) {
                throw new NumberFormatException("เบอร์โทรศัพท์ไม่ถูกต้อง (ต้องเป็นตัวเลข 9-10 หลัก และขึ้นต้นด้วย 0)");
            }

            //ตรวจสอบว่ามีชื่อผู้ใช้นี้อยู่แล้วใน users.csv หรือไม่
            File csvFile = resolveCsvFile();
            if (isUsernameTaken(csvFile, u)) {
                throw new IllegalStateException("ชื่อผู้ใช้นี้มีคนใช้งานแล้ว กรุณาใช้ชื่ออื่น");
            }

            //บันทึกข้อมูลใหม่ลงใน users.csv
            saveUserToCsv(csvFile, u, p, n, ph);

            JOptionPane.showMessageDialog(this, 
                    "สมัครสมาชิกสำเร็จเรียบร้อย!", 
                    "สำเร็จ", 
                    JOptionPane.INFORMATION_MESSAGE);

            // ปิดหน้านี้แล้วพากลับไปหน้า Login
            dispose();
            new LoginUI().setVisible(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                    "รูปแบบตัวเลขไม่ถูกต้อง: " + ex.getMessage(), 
                    "ข้อผิดพลาด", 
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, 
                    ex.getMessage(), 
                    "ข้อผิดพลาดในการกรอกข้อมูล", 
                    JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                    "ไม่สามารถบันทึกข้อมูลลงไฟล์ได้: " + ex.getMessage(), 
                    "ข้อผิดพลาดระบบไฟล์", 
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "เกิดข้อผิดพลาดที่ไม่คาดคิด: " + ex.getMessage(), 
                    "ข้อผิดพลาด", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * ค้นหาไฟล์ users.csv ในโปรเจกต์
     */
    private File resolveCsvFile() {
        String[] paths = {
            "src/data/users.csv",
            "data/users.csv",
            "DMS/src/data/users.csv"
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) return f;
        }
        // หากยังไม่มี ให้สร้างไฟล์ที่ src/data/users.csv
        File fallback = new File("src/data/users.csv");
        fallback.getParentFile().mkdirs();
        return fallback;
    }

    private boolean isUsernameTaken(File file, String username) throws IOException {
        if (!file.exists()) return false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] cols = line.split(",");
                if (cols.length > 0 && cols[0].trim().equalsIgnoreCase(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void saveUserToCsv(File file, String u, String p, String name, String phone) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            // เขียนแถวข้อมูลใหม่: username,password,fullname,phone
            writer.write(String.format("%s,%s,%s,%s", u, p, name, phone));
            writer.newLine();
        }
    }

    /**
     * DocumentFilter กรองให้พิมพ์ได้เฉพาะตัวเลข และจำกัดความยาว
     */
    static class NumericDocumentFilter extends DocumentFilter {
        private final int maxLength;

        public NumericDocumentFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            if (isNumeric(string) && (fb.getDocument().getLength() + string.length() <= maxLength)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            if (isNumeric(text) && (fb.getDocument().getLength() - length + text.length() <= maxLength)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isNumeric(String str) {
            return str.matches("\\d*");
        }
    }

    private JTextField createModernField(String ph, boolean isPass) {
        final boolean[] focus = {false};

        JTextField f = isPass ? new JPasswordField(ph) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(new Color(40, 65, 80, 140));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                if (focus[0]) {
                    g2.setColor(Color.decode("#4DB6AC"));
                    g2.setStroke(new BasicStroke(1.8f));
                } else {
                    g2.setColor(new Color(255, 255, 255, 90));
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        } : new JTextField(ph) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(new Color(40, 65, 80, 140));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                if (focus[0]) {
                    g2.setColor(Color.decode("#4DB6AC"));
                    g2.setStroke(new BasicStroke(1.8f));
                } else {
                    g2.setColor(new Color(255, 255, 255, 90));
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        f.setOpaque(false);
        f.setFont(new Font(THAI_FONT, Font.PLAIN, 13));
        f.setCaretColor(Color.WHITE);
        f.setBorder(new EmptyBorder(6, 14, 6, 14));
        f.setPreferredSize(new Dimension(340, 42));
        f.setMaximumSize(new Dimension(340, 42));
        f.setAlignmentX(CENTER_ALIGNMENT);
        f.setForeground(new Color(210, 225, 230));

        if (isPass) ((JPasswordField) f).setEchoChar((char) 0);

        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focus[0] = true;
                if (f.getText().equals(ph)) {
                    f.setText("");
                    if (isPass) ((JPasswordField) f).setEchoChar('•');
                    f.setForeground(Color.WHITE);
                }
                f.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focus[0] = false;
                if (f.getText().isEmpty()) {
                    if (isPass) ((JPasswordField) f).setEchoChar((char) 0);
                    f.setText(ph);
                    f.setForeground(new Color(210, 225, 230));
                }
                f.repaint();
            }
        });
        return f;
    }

    private JButton createGradientButton(String text) {
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
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color c1 = hovered ? Color.decode("#00A896") : Color.decode("#028090");
                Color c2 = hovered ? Color.decode("#028090") : Color.decode("#056676");

                g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), 0, c2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font(THAI_FONT, Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(340, 42));
        btn.setMaximumSize(new Dimension(340, 42));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        return btn;
    }
}