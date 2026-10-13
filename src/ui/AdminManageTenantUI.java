package ui;

import logic.AdminManageTenantLogic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class AdminManageTenantUI extends JPanel {

    static final Color PRIMARY    = AdminUI.PRIMARY;
    static final Color BG         = AdminUI.BG;
    static final Color WHITE      = AdminUI.WHITE;
    static final Color BORDER     = AdminUI.BORDER;
    static final Color TEXT       = AdminUI.TEXT;
    static final Color TEXT_GRAY  = AdminUI.TEXT_GRAY;
    static final Color ROW_SELECT = AdminManageRoomUI.ROW_SELECT;

    private final AdminManageTenantLogic logic = new AdminManageTenantLogic();

    JTextField searchField;
    JButton btnNewContract;
    JTable table;
    DefaultTableModel tableModel;
    JLabel detailTitle, detailSub, tenantName;
    JLabel lblContractId, lblStatus, lblPhone, lblRoomType, lblRent, lblDeposit, lblStart, lblEnd;
    JButton btnPrint, btnRenew;

    public AdminManageTenantUI() {
        setLayout(null);
        setBackground(BG);

        createToolbar();
        add(createTable());
        add(createDetailPanel());
        bindSelection();
    }

    private void createToolbar() {
        searchField = new AdminManageRoomUI.RoundField("ค้นหาชื่อผู้เช่า หรือห้อง...", WHITE, true);
        searchField.setBounds(20, 20, 250, 34);
        add(searchField);

        btnNewContract = new AdminManageRoomUI.FlatButton("+  ทำสัญญาใหม่", false);
        btnNewContract.setForeground(PRIMARY);
        btnNewContract.setBounds(440, 19, 120, 36);
        add(btnNewContract);
    }

    private JScrollPane createTable() {
        String[] columns = {"ชื่อผู้เช่า", "ห้อง", "เริ่มสัญญา", "สิ้นสุดสัญญา", "สถานะ", "การจัดการ"};
        Object[][] rows = logic.getTableRows();

        tableModel = new DefaultTableModel(rows, columns) {
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
        table.setRowSelectionInterval(0, 0);

        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        int[] widths = {112, 48, 94, 94, 76, 116};
        TableColumnModel cols = table.getColumnModel();
        for (int i = 0; i < widths.length; i++) cols.getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        scroll.setBounds(20, 70, 540, 500);
        return scroll;
    }

    private JPanel createDetailPanel() {
        List<AdminManageTenantLogic.TenantModel> list = logic.getAllTenants();
        AdminManageTenantLogic.TenantModel first = list.get(0);

        JPanel p = new JPanel(null);
        p.setBackground(WHITE);
        p.setBounds(580, 0, 330, 640);
        p.setBorder(new MatteBorder(0, 1, 0, 0, BORDER));

        detailTitle = AdminUI.label("ข้อมูลสัญญาเช่า", Font.BOLD, 16, TEXT);
        detailTitle.setBounds(20, 20, 280, 24);
        p.add(detailTitle);

        detailSub = AdminUI.label(first.contractId + " • ห้อง " + first.room + " (คุณ" + first.name + ")", Font.PLAIN, 11, TEXT_GRAY);
        detailSub.setBounds(20, 46, 280, 16);
        p.add(detailSub);
        p.add(divider(78));

        AdminUI.AvatarPanel avatar = new AdminUI.AvatarPanel();
        avatar.setBounds(20, 94, 42, 42);
        p.add(avatar);

        tenantName = AdminUI.label(first.name, Font.BOLD, 13, TEXT);
        tenantName.setBounds(74, 104, 220, 22);
        p.add(tenantName);
        p.add(divider(152));

        int y = 168;
        lblContractId = detailRow(p, "เลขที่สัญญา", first.contractId, y);
        lblStatus     = detailRow(p, "สถานะสัญญา", first.status, y += 26);
        lblPhone      = detailRow(p, "เบอร์โทรศัพท์", first.phone, y += 26);
        lblRoomType   = detailRow(p, "ห้องพัก", first.roomType, y += 26);
        lblRent       = detailRow(p, "ค่าเช่า / เดือน", first.rent, y += 26);
        lblDeposit    = detailRow(p, "เงินประกัน", first.deposit, y += 26);
        lblStart      = detailRow(p, "วันเริ่มสัญญา", first.startDate, y += 26);
        lblEnd        = detailRow(p, "วันสิ้นสุดสัญญา", first.endDate, y += 26);

        p.add(divider(y + 38));

        btnPrint = new AdminManageRoomUI.FlatButton("พิมพ์เอกสารสัญญา", false);
        btnPrint.setForeground(PRIMARY);
        btnPrint.setBounds(20, y + 54, 276, 40);
        p.add(btnPrint);

        btnRenew = new AdminManageRoomUI.FlatButton("ต่ออายุสัญญาเช่า", true);
        btnRenew.setBounds(20, y + 102, 276, 40);
        p.add(btnRenew);

        return p;
    }

    private void bindSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                List<AdminManageTenantLogic.TenantModel> list = logic.getAllTenants();
                if (row < list.size()) {
                    AdminManageTenantLogic.TenantModel t = list.get(row);
                    tenantName.setText(t.name);
                    detailSub.setText(t.contractId + " • ห้อง " + t.room + " (คุณ" + t.name + ")");
                    lblContractId.setText(t.contractId);
                    lblStatus.setText(t.status);
                    lblPhone.setText(t.phone);
                    lblRoomType.setText(t.roomType);
                    lblRent.setText(t.rent);
                    lblDeposit.setText(t.deposit);
                    lblStart.setText(t.startDate);
                    lblEnd.setText(t.endDate);
                }
            }
        });
    }

    private static JLabel detailRow(JPanel p, String title, String value, int y) {
        JLabel t = AdminUI.label(title, Font.PLAIN, 11, TEXT_GRAY);
        t.setBounds(20, y, 120, 18);
        p.add(t);

        JLabel v = AdminUI.label(value, Font.BOLD, 11, TEXT);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        v.setBounds(110, y, 186, 18);
        p.add(v);
        return v;
    }

    private static JPanel divider(int y) {
        JPanel line = new JPanel();
        line.setBackground(BORDER);
        line.setBounds(0, y, 330, 1);
        return line;
    }

    static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setFont(AdminUI.font(Font.PLAIN, 11));
            setForeground(TEXT_GRAY);
            setBackground(WHITE);
            setHorizontalAlignment(col == 5 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 10, 0, 10)));
            return this;
        }
    }

    static class CellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Color rowBg = sel ? ROW_SELECT : WHITE;
            if (col == 4) {
                String s = String.valueOf(v);
                boolean active = s.startsWith("ใช้งาน");
                AdminUI.Badge badge = new AdminUI.Badge(s,
                        active ? new Color(209, 250, 229) : new Color(254, 226, 226),
                        active ? new Color(4, 120, 87) : new Color(220, 38, 38));
                badge.setPreferredSize(badge.getSize());
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
                cell.setBackground(rowBg);
                cell.add(badge);
                return cell;
            }
            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setBackground(rowBg);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(col == 5 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (col == 0) {
                setFont(AdminUI.font(Font.BOLD, 12));
                setForeground(TEXT);
            } else if (col == 1) {
                setFont(AdminUI.font(Font.BOLD, 11));
                setForeground(PRIMARY);
            } else if (col == 5) {
                setFont(AdminUI.font(Font.PLAIN, 11));
                setText("<html><nobr><b><font color='#0F766E'>ดูรายละเอียด</font></b>&nbsp;&nbsp;<font color='#9CA3AF'>แก้ไข</font></nobr></html>");
            } else {
                setFont(AdminUI.font(Font.PLAIN, 11));
                setForeground(TEXT_GRAY);
            }
            return this;
        }
    }
}