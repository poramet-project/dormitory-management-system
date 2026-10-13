package ui;

import logic.AdminManageProblemLogic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminManageProblemUI extends JPanel {

    static final Color PRIMARY    = AdminUI.PRIMARY;
    static final Color BG         = AdminUI.BG;
    static final Color WHITE      = AdminUI.WHITE;
    static final Color BORDER     = AdminUI.BORDER;
    static final Color TEXT       = AdminUI.TEXT;
    static final Color TEXT_GRAY  = AdminUI.TEXT_GRAY;
    static final Color ROW_SELECT = AdminManageRoomUI.ROW_SELECT;

    private final AdminManageProblemLogic logic = new AdminManageProblemLogic();

    JTextField searchField;
    JComboBox<String> statusFilter, typeFilter;
    JTable table;
    DefaultTableModel tableModel;
    JLabel detailTitle, detailSub, lblName, lblPhone, lblRoom, lblDate, lblStatus, lblCount, lblCategory, lblTopic;
    JTextArea txtDescription;
    JButton btnAccept;

    public AdminManageProblemUI() {
        setLayout(null);
        setBackground(BG);

        createToolbar();
        add(createTable());
        add(createDetailPanel());
        bindSelection();
        initActions();
        refreshData();
    }

    private void createToolbar() {
        searchField = new AdminManageRoomUI.RoundField("ค้นหาเลขคำร้อง ชื่อ หรือห้อง...", WHITE, true);
        searchField.setBounds(20, 20, 280, 34);
        add(searchField);

        statusFilter = styledCombo(new String[]{"สถานะทั้งหมด", "รับเรื่อง", "ดำเนินการ", "เสร็จสิ้น"});
        statusFilter.setBounds(310, 20, 250, 34);
        add(statusFilter);

        typeFilter = styledCombo(new String[]{"หมวดหมู่ทั้งหมด", "ระบบประปา", "ระบบไฟฟ้า", "เครื่องปรับอากาศ", "เฟอร์นิเจอร์", "อื่นๆ"});
        typeFilter.setBounds(20, 64, 155, 30);
        add(typeFilter);

        lblCount = AdminUI.label("แสดง 0 รายการ • เลือกแถวเพื่อดูรายละเอียด", Font.PLAIN, 11, TEXT_GRAY);
        lblCount.setBounds(185, 64, 375, 30);
        add(lblCount);
    }

    private JScrollPane createTable() {
        String[] columns = {"เลขที่", "ผู้แจ้ง", "ห้อง", "เรื่อง", "วันที่แจ้ง", "สถานะ"};
        tableModel = new DefaultTableModel(new Object[0][0], columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(AdminUI.font(Font.PLAIN, 11));
        table.setRowHeight(44);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        scroll.setBounds(20, 105, 540, 490);
        return scroll;
    }

    private JPanel createDetailPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(WHITE);
        p.setBounds(580, 0, 330, 640);
        p.setBorder(new MatteBorder(0, 1, 0, 0, BORDER));

        detailTitle = AdminUI.label("รายละเอียดคำร้อง", Font.BOLD, 16, TEXT);
        detailTitle.setBounds(20, 20, 276, 24);
        p.add(detailTitle);

        detailSub = AdminUI.label("- • แจ้งซ่อม", Font.PLAIN, 11, TEXT_GRAY);
        detailSub.setBounds(20, 46, 276, 18);
        p.add(detailSub);
        p.add(divider(78));

        lblName     = detailRow(p, "ผู้ยื่นคำขอ", "-", 94);
        lblPhone    = detailRow(p, "เบอร์โทรศัพท์", "-", 122);
        lblRoom     = detailRow(p, "ห้องพัก", "-", 150);
        lblCategory = detailRow(p, "หมวดหมู่", "-", 178);
        lblTopic    = detailRow(p, "เรื่อง", "-", 206);
        lblDate     = detailRow(p, "วันที่แจ้ง", "-", 234);
        lblStatus   = detailRow(p, "สถานะ", "-", 262);

        p.add(fieldLabel("รายละเอียดปัญหา", 294));
        txtDescription = textArea(false);
        p.add(areaScroll(txtDescription, 316, 84));

        AdminUI.RoundedPanel note = new AdminUI.RoundedPanel(AdminUI.PRIMARY_BG, AdminUI.PRIMARY_BG, 10);
        note.setLayout(null);
        note.setBounds(20, 410, 276, 64);
        JLabel n1 = AdminUI.label("สถานะคำร้องอัปเดตโดยผู้เช่า", Font.BOLD, 11, PRIMARY);
        n1.setBounds(12, 10, 252, 18);
        note.add(n1);
        JLabel n2 = AdminUI.label("<html>ผู้ดูแลมีหน้าที่รับเรื่องและประสานงานเท่านั้น</html>", Font.PLAIN, 10, TEXT_GRAY);
        n2.setBounds(12, 30, 252, 26);
        note.add(n2);
        p.add(note);

        btnAccept = new AdminManageRoomUI.FlatButton("รับเรื่อง", true);
        btnAccept.setBounds(20, 546, 276, 38);
        p.add(btnAccept);
        return p;
    }

    public void refreshData() {
        tableModel.setDataVector(logic.getTableRows(), new String[]{"เลขที่", "ผู้แจ้ง", "ห้อง", "เรื่อง", "วันที่แจ้ง", "สถานะ"});
        int[] widths = {46, 110, 40, 118, 86, 140};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        int total = logic.getAllProblems().size();
        lblCount.setText("แสดง " + total + " จาก " + total + " รายการ • เลือกแถวเพื่อดูรายละเอียด");

        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private void bindSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                List<AdminManageProblemLogic.ProblemModel> list = logic.getAllProblems();
                if (row < list.size()) {
                    AdminManageProblemLogic.ProblemModel p = list.get(row);
                    detailSub.setText(p.id + " • " + p.topic);
                    lblName.setText(p.name);
                    lblPhone.setText(p.phone);
                    lblRoom.setText(p.room);
                    lblCategory.setText(p.category);
                    lblTopic.setText(p.topic);
                    lblDate.setText(p.date);
                    lblStatus.setText(p.status);
                    txtDescription.setText(p.desc);
                }
            }
        });
    }

    private void initActions() {
        btnAccept.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String complaintId = String.valueOf(tableModel.getValueAt(row, 0));
                if (logic.acceptProblem(complaintId)) {
                    JOptionPane.showMessageDialog(this, "รับเรื่องและปรับสถานะเป็น 'ช่างกำลังเข้าตรวจสอบ' เรียบร้อย", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            }
        });
    }

    private static JLabel detailRow(JPanel p, String title, String text, int y) {
        JLabel label = AdminUI.label(title, Font.PLAIN, 11, TEXT_GRAY);
        label.setBounds(20, y, 130, 20);
        p.add(label);
        JLabel value = AdminUI.label(text, Font.BOLD, 11, TEXT);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        value.setBounds(130, y, 166, 20);
        p.add(value);
        return value;
    }

    private static JLabel fieldLabel(String title, int y) {
        JLabel label = AdminUI.label(title, Font.PLAIN, 11, TEXT_GRAY);
        label.setBounds(20, y, 276, 18);
        return label;
    }

    private static JPanel divider(int y) {
        JPanel line = new JPanel();
        line.setBackground(BORDER);
        line.setBounds(0, y, 330, 1);
        return line;
    }

    private static JTextArea textArea(boolean editable) {
        JTextArea area = new JTextArea();
        area.setFont(AdminUI.font(Font.PLAIN, 12));
        area.setForeground(TEXT);
        area.setBackground(AdminUI.ITEM_BG);
        area.setBorder(new EmptyBorder(6, 8, 6, 8));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(editable);
        return area;
    }

    private static JScrollPane areaScroll(JTextArea area, int y, int height) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setBounds(20, y, 276, height);
        return scroll;
    }

    private static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AdminUI.font(Font.PLAIN, 12));
        cb.setForeground(TEXT);
        cb.setBackground(WHITE);
        cb.setBorder(BorderFactory.createLineBorder(BORDER));
        cb.setFocusable(false);
        cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override protected JButton createArrowButton() { return new AdminManageRoomUI.ChevronButton(); }
        });
        return cb;
    }

    static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, focus, row, col);
            setFont(AdminUI.font(Font.PLAIN, 11));
            setForeground(TEXT_GRAY);
            setBackground(WHITE);
            setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 8, 0, 8)));
            return this;
        }
    }

    static class CellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            Color rowBg = selected ? ROW_SELECT : WHITE;
            if (col == 5) {
                String status = String.valueOf(value);
                Color bg = new Color(254, 243, 199), fg = new Color(180, 83, 9);
                if (status.equals("อนุมัติ") || status.equals("เสร็จสิ้น")) {
                    bg = new Color(209, 250, 229); fg = new Color(4, 120, 87);
                } else if (status.equals("ปฏิเสธ")) {
                    bg = new Color(254, 226, 226); fg = AdminUI.RED;
                } else if (status.equals("ช่างกำลังเข้าตรวจสอบ")) {
                    bg = AdminUI.PRIMARY_BG; fg = PRIMARY;
                }
                AdminUI.Badge badge = new AdminUI.Badge(status, bg, fg);
                badge.setPreferredSize(badge.getSize());
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
                cell.setBackground(rowBg);
                cell.add(badge);
                return cell;
            }
            super.getTableCellRendererComponent(t, value, false, false, row, col);
            setFont(AdminUI.font(col == 0 ? Font.BOLD : Font.PLAIN, 11));
            setForeground(col == 0 ? PRIMARY : TEXT);
            setBackground(rowBg);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            return this;
        }
    }
}