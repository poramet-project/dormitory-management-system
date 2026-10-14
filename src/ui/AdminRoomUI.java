package ui;

import logic.AdminRoomLogic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class AdminRoomUI extends JPanel {

    static final Color PRIMARY    = AdminUI.PRIMARY;
    static final Color BG         = AdminUI.BG;
    static final Color WHITE      = AdminUI.WHITE;
    static final Color BORDER     = AdminUI.BORDER;
    static final Color TEXT       = AdminUI.TEXT;
    static final Color TEXT_GRAY  = AdminUI.TEXT_GRAY;
    static final Color TEXT_LIGHT = AdminUI.TEXT_LIGHT;
    static final Color ITEM_BG    = AdminUI.ITEM_BG;
    static final Color ROW_SELECT = new Color(240, 253, 250);

    JTextField searchField;
    JComboBox<String> statusFilter;
    JButton btnAdd;
    JTable table;
    DefaultTableModel tableModel;
    JLabel formTitle, formSub;
    JTextField txtNumber, txtRent;
    JComboBox<String> cbType;
    JRadioButton rbAvailable, rbOccupied, rbMaintenance;
    JButton btnCancel, btnSave;

    private String selectedRoomId = "";

    public AdminRoomUI() {
        setLayout(null);
        setBackground(BG);

        createToolbar();
        add(createTable());
        add(createFormPanel());

        loadTableData();
        initActions();
    }

    public void loadTableData() {
        tableModel.setRowCount(0);
        List<AdminRoomLogic.RoomModel> rooms = AdminRoomLogic.getAllRooms();
        for (AdminRoomLogic.RoomModel r : rooms) {
            tableModel.addRow(new Object[]{r.id, r.number, r.floor, r.type, r.rent, r.statusThai, "แก้ไข"});
        }
    }

    private void createToolbar() {
        searchField = new RoundField("ระบุเลขห้อง...", WHITE, true);
        searchField.setBounds(20, 20, 170, 34);
        add(searchField);

        statusFilter = styledCombo(new String[]{"สถานะทั้งหมด", "ห้องว่าง", "มีผู้เช่า", "อยู่ระหว่างปรับปรุง"}, WHITE);
        statusFilter.setBounds(200, 20, 150, 34);
        add(statusFilter);

        btnAdd = new FlatButton("+  เพิ่มห้องใหม่", true);
        btnAdd.setBounds(430, 19, 130, 36);
        add(btnAdd);
    }

    private JScrollPane createTable() {
        String[] columns = {"รหัส", "เลขห้อง", "ชั้น", "ประเภท", "ราคา/เดือน", "สถานะ", "การจัดการ"};

        tableModel = new DefaultTableModel(new Object[0][0], columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        table.setDefaultRenderer(Object.class, new CellRenderer());

        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        int[] widths = {65, 85, 60, 105, 90, 110, 88};
        TableColumnModel cols = table.getColumnModel();
        for (int i = 0; i < widths.length; i++) cols.getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        scroll.setBounds(20, 70, 540, 510);
        return scroll;
    }

    private JPanel createFormPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(WHITE);
        p.setBounds(580, 0, 330, 640);
        p.setBorder(new MatteBorder(0, 1, 0, 0, BORDER));

        formTitle = AdminUI.label("รายละเอียดห้องพัก", Font.BOLD, 16, TEXT);
        formTitle.setBounds(20, 20, 280, 24);
        p.add(formTitle);

        formSub = AdminUI.label("เลือกห้องจากตารางเพื่อแก้ไขข้อมูล", Font.PLAIN, 11, TEXT_GRAY);
        formSub.setBounds(20, 46, 280, 16);
        p.add(formSub);

        p.add(divider(78));

        p.add(fieldLabel("เลขห้องพัก", 94));
        txtNumber = new RoundField("เช่น 101", ITEM_BG, false);
        txtNumber.setBounds(20, 114, 276, 34);
        p.add(txtNumber);

        p.add(fieldLabel("ประเภทห้อง", 158));
        cbType = styledCombo(new String[]{"ห้องพัดลม", "ห้องแอร์"}, ITEM_BG);
        cbType.setBounds(20, 178, 276, 34);
        p.add(cbType);

        p.add(fieldLabel("ค่าเช่ารายเดือน (บาท)", 222));
        txtRent = new RoundField("เช่น 3500", ITEM_BG, false);
        txtRent.setBounds(20, 242, 276, 34);
        p.add(txtRent);

        p.add(fieldLabel("สถานะห้อง", 288));
        rbAvailable   = radio("ห้องว่าง (Available)");
        rbOccupied    = radio("มีผู้เช่า (Occupied)");
        rbMaintenance = radio("อยู่ระหว่างปรับปรุง (Maintenance)");
        ButtonGroup group = new ButtonGroup();
        JRadioButton[] radios = {rbAvailable, rbOccupied, rbMaintenance};
        for (int i = 0; i < radios.length; i++) {
            group.add(radios[i]);
            radios[i].setBounds(18, 308 + i * 24, 280, 22);
            p.add(radios[i]);
        }

        p.add(divider(388));

        btnCancel = new FlatButton("ล้างฟอร์ม", false);
        btnCancel.setBounds(20, 404, 128, 40);
        p.add(btnCancel);

        btnSave = new FlatButton("บันทึกข้อมูล", true);
        btnSave.setBounds(158, 404, 138, 40);
        p.add(btnSave);

        return p;
    }

    private void initActions() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                selectedRoomId = String.valueOf(tableModel.getValueAt(row, 0));
                String roomNum = String.valueOf(tableModel.getValueAt(row, 1)).replace("ห้อง ", "");
                String type = String.valueOf(tableModel.getValueAt(row, 3));
                String rent = String.valueOf(tableModel.getValueAt(row, 4)).replace(",", "");
                String status = String.valueOf(tableModel.getValueAt(row, 5));

                txtNumber.setText(roomNum);
                formSub.setText("แก้ไขข้อมูลห้อง " + roomNum);
                txtRent.setText(rent);
                cbType.setSelectedItem(type);

                if (status.contains("ว่าง")) rbAvailable.setSelected(true);
                else if (status.contains("มีผู้เช่า")) rbOccupied.setSelected(true);
                else rbMaintenance.setSelected(true);
            }
        });

        btnSave.addActionListener(e -> {
            if (selectedRoomId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "กรุณาเลือกห้องจากตาราง หรือกดเพิ่มห้องใหม่", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String num = txtNumber.getText().trim();
                int rent = Integer.parseInt(txtRent.getText().trim().replace(",", ""));
                String type = (String) cbType.getSelectedItem();
                String status = rbAvailable.isSelected() ? "AVAILABLE" : (rbOccupied.isSelected() ? "OCCUPIED" : "MAINTENANCE");

                // บันทึก/อัปเดตเฉพาะไฟล์ rooms.csv เท่านั้น
                AdminRoomLogic.saveOrUpdateRoom(selectedRoomId, num, type, rent, status);
                
                loadTableData();
                JOptionPane.showMessageDialog(this, "บันทึกข้อมูลห้องพักใน rooms.csv เรียบร้อยแล้ว", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "ข้อมูลไม่ถูกต้อง: " + ex.getMessage(), "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAdd.addActionListener(e -> {
            selectedRoomId = "";
            txtNumber.setText("");
            txtRent.setText("2800");
            cbType.setSelectedIndex(0);
            rbAvailable.setSelected(true);
            formSub.setText("เพิ่มห้องพักใหม่ (ระบุข้อมูลแล้วกดบันทึก)");
            txtNumber.requestFocus();
        });

        btnCancel.addActionListener(e -> {
            selectedRoomId = "";
            txtNumber.setText("");
            txtRent.setText("");
            rbAvailable.setSelected(true);
            formSub.setText("เลือกห้องจากตารางเพื่อแก้ไขข้อมูล");
        });
    }

    private static JLabel fieldLabel(String text, int y) {
        JLabel l = AdminUI.label(text, Font.PLAIN, 11, TEXT_GRAY);
        l.setBounds(20, y, 276, 16);
        return l;
    }

    private static JPanel divider(int y) {
        JPanel line = new JPanel();
        line.setBackground(BORDER);
        line.setBounds(0, y, 330, 1);
        return line;
    }

    private static JRadioButton radio(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(AdminUI.font(Font.PLAIN, 11));
        rb.setForeground(TEXT);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setIconTextGap(8);
        RadioIcon icon = new RadioIcon();
        rb.setIcon(icon);
        rb.setSelectedIcon(icon);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return rb;
    }

    private static JComboBox<String> styledCombo(String[] items, Color bg) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AdminUI.font(Font.PLAIN, 12));
        cb.setForeground(TEXT);
        cb.setFocusable(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(0, 6, 0, 0)));
        cb.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() { return new ChevronButton(); }
            @Override public void paintCurrentValueBackground(Graphics g, Rectangle b, boolean focus) {
                g.setColor(bg);
                g.fillRect(b.x, b.y, b.width, b.height);
            }
        });
        cb.setBackground(bg);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                setBorder(new EmptyBorder(5, 6, 5, 6));
                setOpaque(index >= 0);
                setBackground(selected ? ROW_SELECT : WHITE);
                setForeground(TEXT);
                return this;
            }
        });
        return cb;
    }

    static class RoundField extends JTextField {
        private final String placeholder;
        private final Color bg;
        private final boolean searchIcon;

        RoundField(String placeholder, Color bg, boolean searchIcon) {
            this.placeholder = placeholder;
            this.bg = bg;
            this.searchIcon = searchIcon;
            setOpaque(false);
            setFont(AdminUI.font(Font.PLAIN, 12));
            setForeground(TEXT);
            setCaretColor(TEXT);
            setBorder(new EmptyBorder(0, searchIcon ? 32 : 12, 0, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = AdminUI.smooth(g);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(hasFocus() ? PRIMARY : BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            if (searchIcon) {
                int cy = getHeight() / 2;
                g2.setColor(TEXT_LIGHT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(12, cy - 6, 10, 10);
                g2.drawLine(20, cy + 2, 24, cy + 6);
            }
            g2.dispose();
            super.paintComponent(g);

            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g3 = AdminUI.smooth(g);
                g3.setFont(getFont());
                g3.setColor(TEXT_LIGHT);
                FontMetrics fm = g3.getFontMetrics();
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g3.drawString(placeholder, getInsets().left, ty);
                g3.dispose();
            }
        }
    }

    static class FlatButton extends JButton {
        private final boolean primary;

        FlatButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(AdminUI.font(Font.BOLD, 12));
            setForeground(primary ? WHITE : TEXT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = AdminUI.smooth(g);
            boolean over = getModel().isRollover();
            if (primary) {
                g2.setColor(over ? PRIMARY.darker() : PRIMARY);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            } else {
                g2.setColor(over ? ITEM_BG : WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class ChevronButton extends JButton {
        ChevronButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = AdminUI.smooth(g);
            g2.setColor(TEXT_GRAY);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2, cy = getHeight() / 2;
            g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
            g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
            g2.dispose();
        }
    }

    static class RadioIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = AdminUI.smooth(g);
            g2.setColor(WHITE);
            g2.fillOval(x, y, 15, 15);
            g2.setColor(b.isSelected() ? PRIMARY : new Color(209, 213, 219));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawOval(x, y, 15, 15);
            if (b.isSelected()) {
                g2.setColor(PRIMARY);
                g2.fillOval(x + 4, y + 4, 8, 8);
            }
            g2.dispose();
        }
        @Override public int getIconWidth()  { return 16; }
        @Override public int getIconHeight() { return 16; }
    }

    static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setFont(AdminUI.font(Font.PLAIN, 11));
            setForeground(TEXT_GRAY);
            setBackground(WHITE);
            setHorizontalAlignment(col == 4 || col == 6 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 14, 0, 14)));
            return this;
        }
    }

    static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Color rowBg = sel ? ROW_SELECT : WHITE;

            if (col == 5) {
                String s = String.valueOf(v);
                Color bg, fg;
                if (s.contains("ว่าง"))        { bg = new Color(209, 250, 229); fg = new Color(4, 120, 87); }
                else if (s.contains("มีผู้เช่า")) { bg = new Color(204, 251, 241); fg = PRIMARY; }
                else                          { bg = new Color(254, 226, 226); fg = new Color(220, 38, 38); }
                AdminUI.Badge badge = new AdminUI.Badge(s, bg, fg);
                badge.setPreferredSize(badge.getSize());
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
                cell.setBackground(rowBg);
                cell.add(badge);
                return cell;
            }

            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setBackground(rowBg);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setHorizontalAlignment(col == 4 || col == 6 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (col == 0 || col == 1) {
                setFont(AdminUI.font(Font.BOLD, 12));
                setForeground(TEXT);
            } else if (col == 6) {
                setFont(AdminUI.font(Font.BOLD, 12));
                setForeground(PRIMARY);
            } else {
                setFont(AdminUI.font(Font.PLAIN, 12));
                setForeground(col == 4 ? TEXT : new Color(55, 65, 81));
            }
            return this;
        }
    }
}