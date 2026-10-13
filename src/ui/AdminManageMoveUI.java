package ui;

import logic.AdminManageMoveLogic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminManageMoveUI extends JPanel {

    static final Color PRIMARY    = AdminUI.PRIMARY;
    static final Color BG         = AdminUI.BG;
    static final Color WHITE      = AdminUI.WHITE;
    static final Color BORDER     = AdminUI.BORDER;
    static final Color TEXT       = AdminUI.TEXT;
    static final Color TEXT_GRAY  = AdminUI.TEXT_GRAY;
    static final Color ROW_SELECT = AdminManageRoomUI.ROW_SELECT;

    private final AdminManageMoveLogic logic = new AdminManageMoveLogic();

    JTextField searchField;
    JComboBox<String> statusFilter;
    JTable table;
    DefaultTableModel tableModel;
    JLabel detailTitle, detailSub, lblName, lblPhone, lblRoom, lblDate, lblStatus, lblCount;
    JTextArea txtDescription, txtNote;
    JButton btnApprove, btnReject;

    public AdminManageMoveUI() {
        setLayout(null);
        setBackground(BG);

        createToolbar();
        add(createTable());
        add(createDetailPanel());
        bindSelection();
    }

    private void createToolbar() {
        searchField = new AdminManageRoomUI.RoundField("ค้นหาเลขคำขอ ชื่อ หรือห้อง...", WHITE, true);
        searchField.setBounds(20, 20, 280, 34);
        add(searchField);

        statusFilter = styledCombo(new String[]{"สถานะทั้งหมด", "รอพิจารณา", "อนุมัติ", "ปฏิเสธ"});
        statusFilter.setBounds(310, 20, 250, 34);
        add(statusFilter);

        int total = logic.getAllMoves().size();
        lblCount = AdminUI.label("แสดง " + total + " จาก " + total + " รายการ • เลือกแถวเพื่อดูรายละเอียด",
                Font.PLAIN, 11, TEXT_GRAY);
        lblCount.setBounds(20, 64, 540, 20);
        add(lblCount);
    }

    private JScrollPane createTable() {
        String[] columns = {"เลขคำขอ", "ชื่อผู้เช่า", "เดิม > ใหม่", "วันที่ยื่น", "สถานะ"};
        Object[][] rows = logic.getTableRows();

        tableModel = new DefaultTableModel(rows, columns) {
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
        table.setRowSelectionInterval(0, 0);

        int[] widths = {68, 135, 112, 100, 125};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        scroll.setBounds(20, 95, 540, 500);
        return scroll;
    }

    private JPanel createDetailPanel() {
        List<AdminManageMoveLogic.MoveModel> list = logic.getAllMoves();
        AdminManageMoveLogic.MoveModel m = list.get(0);

        JPanel p = new JPanel(null);
        p.setBackground(WHITE);
        p.setBounds(580, 0, 330, 640);
        p.setBorder(new MatteBorder(0, 1, 0, 0, BORDER));

        detailTitle = AdminUI.label("รายละเอียดคำขอ", Font.BOLD, 16, TEXT);
        detailTitle.setBounds(20, 20, 276, 24);
        p.add(detailTitle);

        detailSub = AdminUI.label(m.id + " • " + m.type, Font.PLAIN, 11, TEXT_GRAY);
        detailSub.setBounds(20, 46, 276, 18);
        p.add(detailSub);
        p.add(divider(78));

        lblName   = detailRow(p, "ผู้ยื่นคำขอ", m.name, 94);
        lblPhone  = detailRow(p, "เบอร์โทรศัพท์", m.phone, 122);
        lblRoom   = detailRow(p, "ห้องเดิม > ห้องใหม่", m.rooms, 150);
        lblDate   = detailRow(p, "วันที่ยื่นคำขอ", m.date, 178);
        lblStatus = detailRow(p, "สถานะ", m.status, 206);

        p.add(fieldLabel("รายละเอียด / เหตุผล", 242));
        txtDescription = textArea(false);
        txtDescription.setText(m.reason);
        p.add(areaScroll(txtDescription, 264, 86));

        p.add(fieldLabel("บันทึกผลพิจารณา (ระบุเหตุผลเมื่อปฏิเสธ)", 366));
        txtNote = textArea(true);
        p.add(areaScroll(txtNote, 388, 90));

        p.add(divider(498));
        btnReject = new AdminManageRoomUI.FlatButton("ปฏิเสธคำขอ", false);
        btnReject.setForeground(AdminUI.RED);
        btnReject.setBounds(20, 516, 132, 40);
        p.add(btnReject);

        btnApprove = new AdminManageRoomUI.FlatButton("อนุมัติคำขอ", true);
        btnApprove.setBounds(162, 516, 134, 40);
        p.add(btnApprove);
        return p;
    }

    private void bindSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                List<AdminManageMoveLogic.MoveModel> list = logic.getAllMoves();
                if (row < list.size()) {
                    AdminManageMoveLogic.MoveModel m = list.get(row);
                    detailSub.setText(m.id + " • " + m.type);
                    lblName.setText(m.name);
                    lblPhone.setText(m.phone);
                    lblRoom.setText(m.rooms);
                    lblDate.setText(m.date);
                    lblStatus.setText(m.status);
                    txtDescription.setText(m.reason);
                }
            }
        });
    }

    private static JLabel detailRow(JPanel p, String title, String text, int y) {
        JLabel label = AdminUI.label(title, Font.PLAIN, 11, TEXT_GRAY);
        label.setBounds(20, y, 110, 20);
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
            if (col == 4) {
                String status = String.valueOf(value);
                Color bg = new Color(254, 243, 199), fg = new Color(180, 83, 9);
                if (status.equals("อนุมัติ") || status.equals("เสร็จสิ้น")) {
                    bg = new Color(209, 250, 229); fg = new Color(4, 120, 87);
                } else if (status.equals("ปฏิเสธ")) {
                    bg = new Color(254, 226, 226); fg = AdminUI.RED;
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