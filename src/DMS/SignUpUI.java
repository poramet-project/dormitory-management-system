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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SignUpUI extends JFrame {
    private Image bgImg;
    private JTextField userField;
    private JPasswordField passField;
    private JTextField nameField;
    private JTextField phoneField;
    private JButton signUpBtn;

    private static final String PH_USER = "Username";
    private static final String PH_PASS = "Password";
    private static final String PH_NAME = "Fullname";
    private static final String PH_PHONE = "Phone";

    // ===== ค่าคงที่ของไฟล์ users.csv =====
    // คอลัมน์: userId(0), username(1), password(2), fullName(3), phone(4), role(5), roomId(6)
    private static final String HEADER = "userId,username,password,fullName,phone,role,roomId";
    private static final String ID_PREFIX = "U";
    private static final int ID_DIGITS = 3;
    private static final String DEFAULT_ROLE = "GUEST"; // README ใช้ APPLICANT ให้ตรวจว่า LoginUI เช็คค่าไหน
    private static final String NO_ROOM = "-";

    public SignUpUI() {
        setTitle("KU Dormitory - Sign Up");
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

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 20));
        topBar.setOpaque(false);
        JLabel topLoginBtn = new JLabel("Login →");
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

        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(340, 55));
        headerPanel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel userIcon = new JLabel("👤⁺");
        userIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 32));
        userIcon.setForeground(Color.WHITE);

        JPanel titleTextPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titleTextPanel.setOpaque(false);

        JLabel titleMain = new JLabel("Sign Up");
        titleMain.setForeground(Color.WHITE);

        JLabel titleSub = new JLabel("Create account to access KU Dormitory");
        titleSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleSub.setForeground(new Color(220, 235, 240));

        titleTextPanel.add(titleMain);
        titleTextPanel.add(titleSub);

        headerPanel.add(userIcon, BorderLayout.WEST);
        headerPanel.add(titleTextPanel, BorderLayout.CENTER);

        userField = createModernField(PH_USER, false);
        passField = (JPasswordField) createModernField(PH_PASS, true);
        nameField = createModernField(PH_NAME, false);
        phoneField = createModernField(PH_PHONE, false);

        ((AbstractDocument) phoneField.getDocument()).setDocumentFilter(new NumericDocumentFilter(10));

        signUpBtn = createGradientButton("Sign Up");
        signUpBtn.addActionListener(e -> processSignUp());

        JLabel alreadyLabel = new JLabel("Already have an account? Login");
        alreadyLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
     * ตรวจสอบว่าข้อความมีตัวอักษรภาษาไทยปนอยู่หรือไม่
     */
    private boolean containsThai(String text) {
        return text != null && text.matches(".*[\\u0E00-\\u0E7F].*");
    }

    private void processSignUp() {
        try {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword()).trim();
            String n = nameField.getText().trim();
            String ph = phoneField.getText().trim();

            // 1. ตรวจสอบค่าว่างหรือ Placeholder
            if (u.isEmpty() || u.equals(PH_USER)) {
                throw new IllegalArgumentException("Please enter a username");
            }
            if (p.isEmpty() || p.equals(PH_PASS)) {
                throw new IllegalArgumentException("Please enter a password");
            }
            if (p.length() < 4) {
                throw new IllegalArgumentException("Password must be at least 4 characters long");
            }
            if (n.isEmpty() || n.equals(PH_NAME)) {
                throw new IllegalArgumentException("Please enter your full name");
            }
            if (ph.isEmpty() || ph.equals(PH_PHONE)) {
                throw new IllegalArgumentException("Please enter your phone number");
            }

            // 2. ดักจับภาษาไทย (ไม่อนุญาตให้มีภาษาไทยในระบบ)
            if (containsThai(u)) {
                throw new IllegalArgumentException("Username cannot contain Thai characters (English only)");
            }
            if (containsThai(p)) {
                throw new IllegalArgumentException("Password cannot contain Thai characters");
            }
            if (containsThai(n)) {
                throw new IllegalArgumentException("Full name must be in English only (No Thai characters)");
            }

            // 2.1 ห้ามมีเครื่องหมายจุลภาค (,) เพราะ CSV ใช้ , คั่นคอลัมน์ ถ้าใส่เข้าไปคอลัมน์จะเลื่อน
            if (u.contains(",") || p.contains(",") || n.contains(",")) {
                throw new IllegalArgumentException("Username, password and full name cannot contain a comma (,)");
            }

            // 3. ตรวจสอบเบอร์โทรศัพท์ (ตัวเลข 9-10 หลัก เริ่มต้นด้วย 0)
            if (!ph.matches("^0[0-9]{8,9}$")) {
                throw new NumberFormatException("Invalid phone number (must be 9-10 digits and start with 0)");
            }

            // 4. เตรียมไฟล์ (แปลงไฟล์เก่าให้มีคอลัมน์ userId) แล้วตรวจ Username ซ้ำ
            File csvFile = resolveCsvFile();
            ensureUserIdColumn(csvFile);
            if (isUsernameTaken(csvFile, u)) {
                throw new IllegalStateException("This username is already taken. Please choose another one.");
            }

            // 5. สร้าง userId ถัดไป แล้วบันทึกทันที (7 คอลัมน์ ด้วย UTF-8)
            String userId = nextUserId(csvFile);
            saveUserToCsv(csvFile, userId, u, p, n, ph, DEFAULT_ROLE, NO_ROOM);

            JOptionPane.showMessageDialog(this,
                    "Sign up successful!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();
            new LoginUI().setVisible(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid format: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save data: " + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================================================================
    //  ส่วนจัดการไฟล์ users.csv
    // ==================================================================

    private File resolveCsvFile() {
        String[] paths = {
            "users.csv",
            "src/users.csv",
            "data/users.csv",
            "src/data/users.csv",
            "DMS/src/data/users.csv"
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) return f;
        }
        File fallback = new File("users.csv");
        return fallback;
    }

    /**
     * ถ้าไฟล์เก่ายังไม่มีคอลัมน์ userId ให้เติมให้อัตโนมัติ (ทำครั้งเดียว)
     * - เพิ่ม userId (U001, U002, ...) ตามลำดับแถว
     * - roomId ที่ว่าง จะถูกแทนด้วย "-"
     * - ลบบรรทัดว่างทิ้ง
     */
    private void ensureUserIdColumn(File file) throws IOException {
        if (!file.exists() || file.length() == 0) return;

        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        List<String> out = new ArrayList<>();
        boolean headerSeen = false;
        int n = 1;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (!headerSeen) {
                headerSeen = true;
                if (line.startsWith("userId,")) return; // เป็นไฟล์รูปแบบใหม่แล้ว
                out.add(HEADER);
                continue;
            }

            String[] c = line.split(",", -1);
            if (c.length > 0 && c[c.length - 1].trim().isEmpty()) {
                c[c.length - 1] = NO_ROOM;
            }
            out.add(String.format("%s%0" + ID_DIGITS + "d,%s", ID_PREFIX, n++, String.join(",", c)));
        }
        Files.write(file.toPath(), out, StandardCharsets.UTF_8);
    }

    /**
     * สร้าง userId ถัดไป เช่น U001, U002, ... โดยหาเลขสูงสุดในไฟล์แล้วบวก 1
     */
    private String nextUserId(File file) throws IOException {
        int max = 0;
        if (file.exists()) {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String id = line.split(",", -1)[0].trim();
                if (id.matches(ID_PREFIX + "\\d+")) {
                    max = Math.max(max, Integer.parseInt(id.substring(ID_PREFIX.length())));
                }
            }
        }
        return String.format("%s%0" + ID_DIGITS + "d", ID_PREFIX, max + 1);
    }

    /**
     * ตรวจ username ซ้ำ (username อยู่คอลัมน์ index 1 เพราะ index 0 คือ userId)
     */
    private boolean isUsernameTaken(File file, String username) throws IOException {
        if (!file.exists()) return false;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("userId,")) continue;
                String[] cols = line.split(",", -1);
                if (cols.length > 1 && cols[1].trim().equalsIgnoreCase(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void saveUserToCsv(File file, String userId, String u, String p,
                               String name, String phone, String role, String roomId) throws IOException {
        boolean hasContent = file.exists() && file.length() > 0;
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            if (!hasContent) {
                writer.write(HEADER);
                writer.newLine();
            } else if (!endsWithNewline(file)) {
                writer.newLine(); // กันแถวใหม่ไปต่อท้ายแถวเก่า
            }
            writer.write(String.join(",", userId, u, p, name, phone, role, roomId));
            writer.newLine();
        }
    }

    /** เช็คว่าตัวอักษรสุดท้ายของไฟล์เป็นขึ้นบรรทัดใหม่หรือไม่ */
    private boolean endsWithNewline(File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            if (raf.length() == 0) return true;
            raf.seek(raf.length() - 1);
            int last = raf.read();
            return last == '\n' || last == '\r';
        }
    }

    // ==================================================================
    //  ส่วน UI ช่วยเหลือ
    // ==================================================================

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
        final boolean[] isPlaceholder = {true};

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
                super.paintComponent(g); // สำคัญ: ต้องเรียก super เพื่อให้ Swing วาดข้อความและเคอร์เซอร์
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
        f.setCaretColor(Color.WHITE);
        f.setBorder(new EmptyBorder(6, 14, 6, 14));
        f.setPreferredSize(new Dimension(340, 42));
        f.setMaximumSize(new Dimension(340, 42));
        f.setAlignmentX(CENTER_ALIGNMENT);
        f.setForeground(new Color(210, 225, 230));

        if (isPass) {
            ((JPasswordField) f).setEchoChar((char) 0);
        }

        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focus[0] = true;
                if (isPlaceholder[0]) {
                    f.setText("");
                    if (isPass) ((JPasswordField) f).setEchoChar('•');
                    f.setForeground(Color.WHITE);
                    isPlaceholder[0] = false;
                }
                f.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focus[0] = false;
                if (f.getText().isEmpty()) {
                    isPlaceholder[0] = true;
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