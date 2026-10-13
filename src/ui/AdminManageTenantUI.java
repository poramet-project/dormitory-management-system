package ui;

import logic.AdminManageTenantLogic;
import logic.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AdminManageTenantUI extends JPanel {

    static final Color PRIMARY    = AdminUI.PRIMARY;
    static final Color BG         = AdminUI.BG;
    static final Color WHITE      = AdminUI.WHITE;
    static final Color BORDER     = AdminUI.BORDER;
    static final Color TEXT       = AdminUI.TEXT;
    static final Color TEXT_GRAY  = AdminUI.TEXT_GRAY;
    static final Color ROW_SELECT = AdminManageRoomUI.ROW_SELECT;

    private final AdminManageTenantLogic logic = new AdminManageTenantLogic();

    JTable table;
    DefaultTableModel tableModel;
    JLabel detailSub, tenantName;
    JLabel lblContractId, lblStatus, lblPhone, lblRoomType, lblRent, lblDeposit, lblStart, lblEnd;
    JButton btnAdd, btnEdit, btnRenew, btnTerminate;

    private AdminManageTenantLogic.TenantModel currentTenant = null;

    public AdminManageTenantUI() {
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
        btnAdd = new AdminManageRoomUI.FlatButton("+ ทำสัญญาใหม่", true);
        btnAdd.setBounds(20, 20, 140, 36);
        add(btnAdd);
    }

    private JScrollPane createTable() {
        String[] columns = {"ชื่อผู้เช่า", "ห้อง", "เริ่มสัญญา", "สิ้นสุดสัญญา", "สถานะ", "การจัดการ"};
        tableModel = new DefaultTableModel(new Object[0][0], columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        scroll.setBounds(20, 70, 540, 500);
        return scroll;
    }

    private JPanel createDetailPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(WHITE);
        p.setBounds(580, 0, 330, 640);
        p.setBorder(new MatteBorder(0, 1, 0, 0, BORDER));

        JLabel detailTitle = AdminUI.label("ข้อมูลสัญญาเช่า", Font.BOLD, 16, TEXT);
        detailTitle.setBounds(20, 20, 280, 24);
        p.add(detailTitle);

        detailSub = AdminUI.label("-", Font.PLAIN, 11, TEXT_GRAY);
        detailSub.setBounds(20, 46, 280, 16);
        p.add(detailSub);

        tenantName = AdminUI.label("-", Font.BOLD, 13, TEXT);
        tenantName.setBounds(20, 80, 280, 22);
        p.add(tenantName);

        int y = 120;
        lblContractId = detailRow(p, "เลขที่สัญญา", "-", y);
        lblStatus     = detailRow(p, "สถานะสัญญา", "-", y += 24);
        lblPhone      = detailRow(p, "เบอร์โทรศัพท์", "-", y += 24);
        lblRoomType   = detailRow(p, "ห้องพัก", "-", y += 24);
        lblRent       = detailRow(p, "ค่าเช่า / เดือน", "-", y += 24);
        lblDeposit    = detailRow(p, "เงินประกัน", "-", y += 24);
        lblStart      = detailRow(p, "วันเริ่มสัญญา", "-", y += 24);
        lblEnd        = detailRow(p, "วันสิ้นสุดสัญญา", "-", y += 24);

        btnEdit = new AdminManageRoomUI.FlatButton("แก้ไขสัญญา", false);
        btnEdit.setBounds(20, 360, 276, 36);
        p.add(btnEdit);

        btnRenew = new AdminManageRoomUI.FlatButton("ต่ออายุสัญญา (+1 ปี)", true);
        btnRenew.setBounds(20, 406, 276, 36);
        p.add(btnRenew);

        btnTerminate = new AdminManageRoomUI.FlatButton("ยกเลิกสัญญา", false);
        btnTerminate.setForeground(AdminUI.RED);
        btnTerminate.setBounds(20, 452, 276, 36);
        p.add(btnTerminate);

        return p;
    }

    public void refreshData() {
        tableModel.setDataVector(logic.getTableRows(), new String[]{"ชื่อผู้เช่า", "ห้อง", "เริ่มสัญญา", "สิ้นสุดสัญญา", "สถานะ", "การจัดการ"});
        if (table.getRowCount() > 0) table.setRowSelectionInterval(0, 0);
    }

    private void bindSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                List<AdminManageTenantLogic.TenantModel> list = logic.getAllTenants();
                if (row < list.size()) {
                    currentTenant = list.get(row);
                    tenantName.setText(currentTenant.name);
                    detailSub.setText(currentTenant.id + " • ห้อง " + currentTenant.room);
                    lblContractId.setText(currentTenant.id);
                    lblStatus.setText(currentTenant.status);
                    lblPhone.setText(currentTenant.phone);
                    lblRoomType.setText(currentTenant.roomType);
                    lblRent.setText(currentTenant.rent);
                    lblDeposit.setText(currentTenant.deposit);
                    lblStart.setText(currentTenant.startDate);
                    lblEnd.setText(currentTenant.endDate);
                }
            }
        });
    }

    private void initActions() {
        // 1. ปุ่มทำสัญญาใหม่
        btnAdd.addActionListener(e -> showFormDialog(null));

        // 2. ปุ่มแก้ไขสัญญา
        btnEdit.addActionListener(e -> {
            if (currentTenant != null) showFormDialog(currentTenant);
            else JOptionPane.showMessageDialog(this, "กรุณาเลือกสัญญาที่ต้องการแก้ไข");
        });

        // 3. ปุ่มต่ออายุสัญญา
        btnRenew.addActionListener(e -> {
            if (currentTenant == null) {
                JOptionPane.showMessageDialog(this, "กรุณาเลือกสัญญาที่ต้องการต่ออายุ");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "ยืนยันการต่ออายุสัญญา " + currentTenant.id + " ออกไปอีก 1 ปี?", "ยืนยันต่ออายุ", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && logic.renewContract(currentTenant.id)) {
                JOptionPane.showMessageDialog(this, "ต่ออายุสัญญาเรียบร้อยแล้ว");
                refreshData();
            }
        });

        // 4. ปุ่มยกเลิกสัญญา
        btnTerminate.addActionListener(e -> {
            if (currentTenant != null) {
                int confirm = JOptionPane.showConfirmDialog(this, "ยืนยันยกเลิกสัญญา " + currentTenant.id + " ?", "ยืนยันยกเลิก", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION && logic.terminateContract(currentTenant.id, currentTenant.roomId, currentTenant.userId)) {
                    JOptionPane.showMessageDialog(this, "ยกเลิกสัญญาเรียบร้อยแล้ว");
                    refreshData();
                }
            }
        });
    }

    /** Form Dialog กลางรองรับทั้ง เพิ่มสัญญาใหม่ และ แก้ไขสัญญาเดิม */
    private void showFormDialog(AdminManageTenantLogic.TenantModel model) {
        boolean isEdit = (model != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "แก้ไขสัญญา" : "ทำสัญญาใหม่", true);
        dialog.setSize(320, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(null);

        JLabel l1 = new JLabel("ผู้ใช้งาน:"); l1.setBounds(20, 20, 80, 25); dialog.add(l1);
        JComboBox<String> cbUser = new JComboBox<>();
        for (Map<String, String> u : DataStore.readTable("users.csv")) {
            if (!"ADMIN".equalsIgnoreCase(u.get("role"))) {
                cbUser.addItem(u.get("userId") + " - " + u.get("fullName"));
            }
        }
        if (isEdit) cbUser.setSelectedItem(model.userId + " - " + model.name);
        cbUser.setBounds(100, 20, 180, 25); dialog.add(cbUser);

        JLabel l2 = new JLabel("ห้องพัก:"); l2.setBounds(20, 60, 80, 25); dialog.add(l2);
        JComboBox<String> cbRoom = new JComboBox<>();
        for (DataStore.Room r : DataStore.rooms()) {
            cbRoom.addItem(r.id + " (ห้อง " + r.number + ")");
        }
        if (isEdit) cbRoom.setSelectedItem(model.roomId + " (ห้อง " + model.room + ")");
        cbRoom.setBounds(100, 60, 180, 25); dialog.add(cbRoom);

        JLabel l3 = new JLabel("ค่าเช่า:"); l3.setBounds(20, 100, 80, 25); dialog.add(l3);
        JTextField txtRent = new JTextField(isEdit ? model.rent.replace(" บาท", "").replace(",", "") : "2800");
        txtRent.setBounds(100, 100, 180, 25); dialog.add(txtRent);

        JLabel l4 = new JLabel("เงินประกัน:"); l4.setBounds(20, 140, 80, 25); dialog.add(l4);
        JTextField txtDeposit = new JTextField(isEdit ? model.deposit.replace(" บาท", "").replace(",", "") : "5600");
        txtDeposit.setBounds(100, 140, 180, 25); dialog.add(txtDeposit);

        JButton btnSave = new JButton("บันทึก");
        btnSave.setBounds(100, 200, 120, 35);
        btnSave.addActionListener(ev -> {
            try {
                String uSel = (String) cbUser.getSelectedItem();
                String rSel = (String) cbRoom.getSelectedItem();
                String uId = uSel.split(" - ")[0].trim();
                String rId = rSel.split(" ")[0].trim();
                int rent = Integer.parseInt(txtRent.getText().trim());
                int dep = Integer.parseInt(txtDeposit.getText().trim());

                boolean success;
                if (isEdit) {
                    success = logic.updateContract(model.id, model.userId, uId, model.roomId, rId, rent, dep);
                } else {
                    success = logic.addContract(uId, rId, rent, dep);
                }

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "บันทึกข้อมูลเรียบร้อยแล้ว");
                    dialog.dispose();
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "ข้อมูลไม่ถูกต้อง");
            }
        });
        dialog.add(btnSave);
        dialog.setVisible(true);
    }

    private static JLabel detailRow(JPanel p, String title, String value, int y) {
        JLabel t = AdminUI.label(title, Font.PLAIN, 11, TEXT_GRAY);
        t.setBounds(20, y, 120, 18); p.add(t);
        JLabel v = AdminUI.label(value, Font.BOLD, 11, TEXT);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        v.setBounds(110, y, 186, 18); p.add(v);
        return v;
    }

    static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setFont(AdminUI.font(Font.PLAIN, 11)); setForeground(TEXT_GRAY); setBackground(WHITE);
            setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 10, 0, 10)));
            return this;
        }
    }

    static class CellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Color rowBg = sel ? ROW_SELECT : WHITE;
            if (col == 4) {
                String s = String.valueOf(v);
                boolean active = s.contains("ใช้งานอยู่") || s.contains("Active");
                AdminUI.Badge badge = new AdminUI.Badge(s, active ? new Color(209, 250, 229) : new Color(254, 226, 226), active ? new Color(4, 120, 87) : new Color(220, 38, 38));
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
                cell.setBackground(rowBg); cell.add(badge);
                return cell;
            }
            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setBackground(rowBg); setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(AdminUI.font(col == 0 ? Font.BOLD : Font.PLAIN, 11));
            setForeground(col == 0 ? TEXT : (col == 1 ? PRIMARY : TEXT_GRAY));
            return this;
        }
    }
}