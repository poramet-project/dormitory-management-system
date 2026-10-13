package ui;

import logic.TenantReportLogic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Calendar;
import java.util.Date;

public class TenantReportUI extends JPanel {

    // ---------- Theme ----------
    static final Color PRIMARY       = new Color(0x13776B);
    static final Color PRIMARY_DARK  = new Color(0x0E5E54);
    static final Color PRIMARY_LIGHT = new Color(0xD5F0EA);
    static final Color BG            = new Color(0xF2F3F5);
    static final Color CARD_BORDER   = new Color(0xDDE1E6);
    static final Color FIELD_BORDER  = new Color(0xC9CED6);
    static final Color TEXT          = new Color(0x222222);
    static final Color MUTED         = new Color(0x7A808A);
    static final Color STEP_GREY     = new Color(0xC9CED6);
    static final Color ORANGE        = new Color(0xE08A00);
    static final Color BLUE          = new Color(0x2F6FDB);
    static final Color RED           = new Color(0xD93025);
    static final String FONT_NAME    = "Leelawadee UI";

    static Font font(int style, int size) { return new Font(FONT_NAME, style, size); }

    // ---------- Logic & State ----------
    private final TenantReportLogic logic;
    private DefaultTableModel model;
    private JTable table;
    private final StepTracker tracker = new StepTracker();
    private final JLabel lblTicket = new JLabel("-");
    private final JLabel lblAppoint = new JLabel("-");
    private final JLabel lblStepNow = new JLabel("-");
    private RoundButton btnNextStep;

    // Form Components
    private JComboBox<String> cbCategory;
    private JTextField tfTitle;
    private JTextArea taDetail;
    private JSpinner spDate;
    private JComboBox<String> cbTime;
    private JCheckBox chkAllowEnter;

    public TenantReportUI() {
        this.logic = new TenantReportLogic();
        setLayout(new BorderLayout());
        setBackground(BG);

        // นำ Content ใส่โดยตรง ไม่ต้องมี Header ซ้ำกับ TenantUI
        add(buildContent(), BorderLayout.CENTER);

        // เลือกแถวแรกเริ่มต้น
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    // =====================================================================
    // Content Layout
    // =====================================================================
    private JPanel buildContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(18, 20, 18, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;

        JPanel left = buildLeftColumn();
        JPanel right = buildFormCard();
        left.setPreferredSize(new Dimension(1, 1));
        right.setPreferredSize(new Dimension(1, 1));

        c.gridx = 0; c.weightx = 0.58; c.insets = new Insets(0, 0, 0, 10);
        content.add(left, c);

        c.gridx = 1; c.weightx = 0.42; c.insets = new Insets(0, 10, 0, 0);
        content.add(right, c);
        return content;
    }

    private JPanel buildLeftColumn() {
        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.BOTH; c.weightx = 1;

        c.gridy = 0; c.weighty = 1; c.insets = new Insets(0, 0, 14, 0);
        col.add(buildHistoryCard(), c);

        c.gridy = 1; c.weighty = 0; c.insets = new Insets(0, 0, 0, 0);
        col.add(buildTrackingCard(), c);
        return col;
    }

    private JPanel buildHistoryCard() {
        RoundPanel card = new RoundPanel(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.add(sectionTitle("ประวัติการแจ้งซ่อม / ร้องเรียน", "คลิกที่รายการเพื่อดูสถานะด้านล่าง"), BorderLayout.NORTH);

        String[] cols = {"รหัส", "วันที่แจ้ง", "หมวด", "เรื่อง", "วันนัดซ่อม", "สถานะ"};
        model = new DefaultTableModel(logic.getTableData(), cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setFont(font(Font.PLAIN, 13));
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xEEF0F2));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader th = table.getTableHeader();
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(0, 34));
        th.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, col);
                l.setFont(font(Font.BOLD, 13));
                l.setBackground(new Color(0xF6F7F9));
                l.setForeground(MUTED);
                l.setHorizontalAlignment(LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER), new EmptyBorder(0, 10, 0, 6)));
                return l;
            }
        });

        DefaultTableCellRenderer pad = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, false, r, col);
                l.setBorder(new EmptyBorder(0, 10, 0, 6));
                return l;
            }
        };
        for (int i = 0; i < 5; i++) table.getColumnModel().getColumn(i).setCellRenderer(pad);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        int[] widths = {62, 85, 85, 125, 120, 100};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.removeColumn(table.getColumnModel().getColumn(1)); // ซ่อนวันที่แจ้ง

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) refreshTracking();
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(0, 150));
        sp.setBorder(new LineBorder(CARD_BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTrackingCard() {
        RoundPanel card = new RoundPanel(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("ติดตามสถานะการซ่อม");
        title.setFont(font(Font.BOLD, 15));
        title.setForeground(TEXT);
        top.add(title, BorderLayout.WEST);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        info.setOpaque(false);
        styleInfo(lblTicket);
        styleInfo(lblAppoint);
        info.add(lblTicket);
        info.add(lblAppoint);
        top.add(info, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        tracker.setPreferredSize(new Dimension(0, 66));
        tracker.setMinimumSize(new Dimension(0, 66));
        card.add(tracker, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        lblStepNow.setFont(font(Font.PLAIN, 13));
        lblStepNow.setForeground(MUTED);
        bottom.add(lblStepNow, BorderLayout.WEST);

        btnNextStep = new RoundButton("ยืนยันขั้นตอนถัดไป", PRIMARY, Color.WHITE);
        btnNextStep.setPreferredSize(new Dimension(190, 36));
        btnNextStep.addActionListener(e -> confirmNextStep());
        bottom.add(btnNextStep, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private void styleInfo(JLabel l) {
        l.setFont(font(Font.PLAIN, 12));
        l.setForeground(MUTED);
    }

    private void refreshTracking() {
        int r = table.getSelectedRow();
        TenantReportLogic.ReportItem item = logic.getReportAt(r);
        if (item == null) {
            tracker.setStep(-1);
            lblTicket.setText("ยังไม่ได้เลือกรายการ");
            lblAppoint.setText("");
            lblStepNow.setText(" ");
            btnNextStep.setEnabled(false);
            return;
        }

        tracker.setStep(item.step);
        lblTicket.setText(item.id + " · " + item.title);
        lblAppoint.setText("นัด: " + item.appointment);

        if (item.step >= TenantReportLogic.STEPS.length - 1) {
            lblStepNow.setText("ดำเนินการเสร็จสิ้นแล้ว");
            btnNextStep.setText("เสร็จสิ้นแล้ว");
            btnNextStep.setEnabled(false);
        } else {
            lblStepNow.setText("ขั้นตอนปัจจุบัน: " + TenantReportLogic.STEPS[item.step]);
            btnNextStep.setText("ยืนยัน: " + TenantReportLogic.STEPS[item.step + 1]);
            btnNextStep.setEnabled(true);
        }
    }

    private void confirmNextStep() {
        int r = table.getSelectedRow();
        TenantReportLogic.ReportItem item = logic.getReportAt(r);
        if (item == null || item.step >= TenantReportLogic.STEPS.length - 1) return;

        String next = TenantReportLogic.STEPS[item.step + 1];
        int ok = JOptionPane.showConfirmDialog(this,
                "ยืนยันว่ารายการ " + item.id + " ดำเนินการถึงขั้น \"" + next + "\" แล้วใช่หรือไม่?",
                "ยืนยันขั้นตอน", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        if (logic.advanceStep(r)) {
            model.setValueAt(next, r, 5);
            refreshTracking();
        }
    }

    private JPanel buildFormCard() {
        RoundPanel card = new RoundPanel(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.add(sectionTitle("แจ้งซ่อม / ร้องเรียนปัญหาใหม่", "กรอกข้อมูลและเลือกวันที่สะดวกให้เจ้าหน้าที่เข้าซ่อม"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridwidth = 2; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        int y = 0;

        cbCategory = new JComboBox<>(TenantReportLogic.CATEGORIES);
        styleField(cbCategory);
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0); form.add(fieldLabel("หมวดหมู่ปัญหา"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 10, 0); form.add(cbCategory, c);

        tfTitle = new JTextField();
        styleField(tfTitle);
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0); form.add(fieldLabel("เรื่อง"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 10, 0); form.add(tfTitle, c);

        taDetail = new JTextArea(4, 20);
        taDetail.setFont(font(Font.PLAIN, 13));
        taDetail.setLineWrap(true);
        taDetail.setWrapStyleWord(true);
        taDetail.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane spDetail = new JScrollPane(taDetail);
        spDetail.setBorder(new LineBorder(FIELD_BORDER));
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0); form.add(fieldLabel("รายละเอียดเพิ่มเติม"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 12, 0); c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        form.add(spDetail, c);
        c.fill = GridBagConstraints.HORIZONTAL; c.weighty = 0;

        c.gridy = y++; c.insets = new Insets(0, 0, 10, 0);
        form.add(buildAppointBox(), c);

        RoundButton submit = new RoundButton("ยืนยันการแจ้งซ่อม", PRIMARY, Color.WHITE);
        submit.setPreferredSize(new Dimension(0, 42));
        submit.setMinimumSize(new Dimension(0, 42));
        submit.addActionListener(e -> submitForm());
        c.gridy = y; c.insets = new Insets(0, 0, 0, 0);
        form.add(submit, c);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAppointBox() {
        JPanel box = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(new Color(0xF2FAF8));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(new Color(0xBFE3DB));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(10, 12, 10, 12));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel head = new JLabel("วัน-เวลาที่สะดวกให้เข้าซ่อม");
        head.setFont(font(Font.BOLD, 13));
        head.setForeground(PRIMARY_DARK);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.weightx = 1; c.insets = new Insets(0, 0, 8, 0);
        box.add(head, c);

        Date tomorrow = logic.getTomorrowDate();
        Date min = logic.getMinSelectableDate();
        spDate = new JSpinner(new SpinnerDateModel(tomorrow, min, null, Calendar.DAY_OF_MONTH));
        spDate.setEditor(new JSpinner.DateEditor(spDate, "dd/MM/yyyy"));
        styleField(spDate);

        cbTime = new JComboBox<>(TenantReportLogic.buildTimeSlots(8, 20));
        cbTime.setMaximumRowCount(8);
        styleField(cbTime);

        c.gridwidth = 1; c.gridy = 1; c.insets = new Insets(0, 0, 4, 8);
        c.gridx = 0; c.weightx = 0.5; box.add(fieldLabel("วันที่"), c);
        c.gridx = 1; c.weightx = 0.5; c.insets = new Insets(0, 0, 4, 0); box.add(fieldLabel("เวลา"), c);
        c.gridy = 2; c.insets = new Insets(0, 0, 8, 8);
        c.gridx = 0; box.add(spDate, c);
        c.gridx = 1; c.insets = new Insets(0, 0, 8, 0); box.add(cbTime, c);

        chkAllowEnter = new JCheckBox("<html>อนุญาตให้เจ้าหน้าที่เข้าห้องได้<br>หากไม่อยู่ในเวลานัด</html>");
        chkAllowEnter.setFont(font(Font.PLAIN, 12));
        chkAllowEnter.setForeground(TEXT);
        chkAllowEnter.setOpaque(false);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; c.insets = new Insets(0, 0, 0, 0);
        box.add(chkAllowEnter, c);
        return box;
    }

    private void submitForm() {
        String title = tfTitle.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "กรุณากรอกเรื่องที่ต้องการแจ้ง", "ข้อมูลไม่ครบ", JOptionPane.WARNING_MESSAGE);
            tfTitle.requestFocus();
            return;
        }

        String category = (String) cbCategory.getSelectedItem();
        String detail = taDetail.getText().trim();
        Date date = (Date) spDate.getValue();
        String timeSlot = (String) cbTime.getSelectedItem();
        boolean allowEnter = chkAllowEnter.isSelected();

        TenantReportLogic.ReportItem newItem = logic.addNewReport(category, title, detail, date, timeSlot, allowEnter);

        model.addRow(new Object[]{
                newItem.id,
                newItem.date,
                newItem.category,
                newItem.title,
                newItem.appointment,
                newItem.getStatusText()
        });

        int last = model.getRowCount() - 1;
        table.setRowSelectionInterval(last, last);
        table.scrollRectToVisible(table.getCellRect(last, 0, true));

        tfTitle.setText("");
        taDetail.setText("");
        chkAllowEnter.setSelected(false);
        JOptionPane.showMessageDialog(this, "ส่งเรื่องแจ้งซ่อม " + newItem.id + " เรียบร้อยแล้ว", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel sectionTitle(String title, String sub) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 16));
        t.setForeground(TEXT);
        JLabel s = new JLabel(sub);
        s.setFont(font(Font.PLAIN, 12));
        s.setForeground(MUTED);
        p.add(t);
        p.add(s);
        return p;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.PLAIN, 13));
        l.setForeground(TEXT);
        return l;
    }

    private void styleField(JComponent comp) {
        comp.setFont(font(Font.PLAIN, 13));
        comp.setPreferredSize(new Dimension(0, 34));
        comp.setMinimumSize(new Dimension(0, 34));
        comp.setBackground(Color.WHITE);
        if (comp instanceof JTextField) {
            comp.setBorder(BorderFactory.createCompoundBorder(new LineBorder(FIELD_BORDER), new EmptyBorder(0, 8, 0, 8)));
        } else if (comp instanceof JSpinner) {
            comp.setBorder(new LineBorder(FIELD_BORDER));
            JComponent ed = ((JSpinner) comp).getEditor();
            JFormattedTextField tf = ((JSpinner.DefaultEditor) ed).getTextField();
            tf.setFont(font(Font.PLAIN, 13));
            tf.setBorder(new EmptyBorder(0, 8, 0, 4));
            tf.setHorizontalAlignment(JTextField.LEFT);
        }
    }

    static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    static Color stepColor(String status) {
        if (status.equals(TenantReportLogic.STEPS[2])) return PRIMARY;
        if (status.equals(TenantReportLogic.STEPS[1])) return ORANGE;
        return BLUE;
    }

    static class RoundPanel extends JPanel {
        RoundPanel(LayoutManager lm) { super(lm); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
            g2.setColor(CARD_BORDER);
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
            g2.dispose();
        }
    }

    static class RoundButton extends JButton {
        private final Color bg;
        RoundButton(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            setForeground(fg);
            setFont(font(Font.BOLD, 14));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            Color c = !isEnabled() ? STEP_GREY : getModel().isPressed() ? PRIMARY_DARK
                    : getModel().isRollover() ? bg.darker() : bg;
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String t = getText();
            g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    static class StatusRenderer extends DefaultTableCellRenderer {
        private String text = "";
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            text = v == null ? "" : v.toString();
            setBackground(s ? t.getSelectionBackground() : Color.WHITE);
            return this;
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            Color c = stepColor(text);
            g2.setFont(font(Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(text) + 18, h = 22;
            int x = 8, y = (getHeight() - h) / 2;
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.setColor(c);
            g2.drawString(text, x + 9, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    static class StepTracker extends JComponent {
        private int step = -1;
        void setStep(int s) { step = s; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            int n = TenantReportLogic.STEPS.length;
            int d = 28;
            int cy = 6 + d / 2;
            int segW = getWidth() / n;

            for (int i = 0; i < n - 1; i++) {
                int x1 = segW * i + segW / 2 + d / 2 + 4;
                int x2 = segW * (i + 1) + segW / 2 - d / 2 - 4;
                g2.setColor(i < step ? PRIMARY : STEP_GREY);
                g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x1, cy, x2, cy);
            }

            for (int i = 0; i < n; i++) {
                int cx = segW * i + segW / 2;
                int x = cx - d / 2, y = cy - d / 2;
                boolean done = i < step, current = i == step;

                if (done) {
                    g2.setColor(PRIMARY);
                    g2.fillOval(x, y, d, d);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawPolyline(new int[]{cx - 6, cx - 1, cx + 7}, new int[]{cy, cy + 5, cy - 5}, 3);
                } else if (current) {
                    g2.setColor(PRIMARY_LIGHT);
                    g2.fillOval(x - 4, y - 4, d + 8, d + 8);
                    g2.setColor(PRIMARY);
                    g2.fillOval(x, y, d, d);
                    drawCentered(g2, String.valueOf(i + 1), cx, cy, Color.WHITE, font(Font.BOLD, 13));
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x, y, d, d);
                    g2.setColor(STEP_GREY);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawOval(x, y, d, d);
                    drawCentered(g2, String.valueOf(i + 1), cx, cy, MUTED, font(Font.BOLD, 13));
                }

                Font lf = font(current ? Font.BOLD : Font.PLAIN, 12);
                Color lc = (done || current) ? TEXT : MUTED;
                drawCentered(g2, TenantReportLogic.STEPS[i], cx, cy + d / 2 + 16, lc, lf);
            }
            g2.dispose();
        }

        private void drawCentered(Graphics2D g2, String s, int cx, int cy, Color c, Font f) {
            g2.setFont(f);
            g2.setColor(c);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, cx - fm.stringWidth(s) / 2, cy - fm.getHeight() / 2 + fm.getAscent());
        }
    }
}