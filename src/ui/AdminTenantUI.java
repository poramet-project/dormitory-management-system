package ui;

import logic.AdminTenantLogic;
import logic.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * AdminTenantUI คือคลาสหน้าจอส่วนแสดงผลสำหรับผู้ดูแลระบบในการจัดการทะเบียนผู้เช่าและสัญญาเช่า
 * logic คือ อ็อบเจกต์ตรรกะสำหรับจัดการข้อมูลผู้เช่าและสัญญาเช่า
 * statusFilter คือ คอมโบ้บ็อกซ์สำหรับเลือกกรองตามสถานะสัญญาเช่า
 * lblCount คือ ป้ายแสดงจำนวนรายการผู้เช่าที่แสดงผล
 * table คือ ตารางแสดงรายชื่อผู้เช่าและสัญญาเช่า
 * tableModel คือ โมเดลข้อมูลของตารางผู้เช่า
 * detailSub คือ ป้ายแสดงคำอธิบายย่อยของรายละเอียดผู้เช่า
 * tenantName คือ ป้ายแสดงชื่อผู้เช่า
 * lblContractId คือ ป้ายแสดงเลขที่สัญญาเช่า
 * lblStatus คือ ป้ายแสดงสถานะสัญญาเช่า
 * lblPhone คือ ป้ายแสดงเบอร์โทรศัพท์ผู้เช่า
 * lblRoomType คือ ป้ายแสดงประเภทห้องพัก
 * lblRent คือ ป้ายแสดงอัตราค่าเช่า
 * lblDeposit คือ ป้ายแสดงเงินมัดจำประกัน
 * lblStart คือ ป้ายแสดงวันที่เริ่มต้นสัญญา
 * lblEnd คือ ป้ายแสดงวันที่สิ้นสุดสัญญา
 * btnAdd คือ ปุ่มกดสำหรับทำสัญญาเช่าใหม่
 * btnEdit คือ ปุ่มกดสำหรับแก้ไขข้อมูลสัญญา
 * btnRenew คือ ปุ่มกดสำหรับต่ออายุสัญญาเช่า
 * btnTerminate คือ ปุ่มกดสำหรับยกเลิกสัญญาเช่า
 * currentTenant คือ ข้อมูลผู้เช่าที่กำลังเลือกดูรายละเอียดอยู่ในปัจจุบัน
 */
public class AdminTenantUI extends JPanel {

    // ===== representation =====
    // Abstraction Function:
    // AF(logic, table, tableModel, currentTenant) คือ หน้าจอสำหรับแสดงทะเบียนผู้เช่า ตรวจสอบสัญญา ทำสัญญาใหม่ ต่ออายุ และยกเลิกสัญญาเช่า
    // RI
    // 1. logic ต้องไม่เป็น null
    // 2. table และ tableModel ต้องไม่เป็น null

    static final Color PRIMARY    = AdminUI.PRIMARY;
    static final Color BG         = AdminUI.BG;
    static final Color WHITE      = AdminUI.WHITE;
    static final Color BORDER     = AdminUI.BORDER;
    static final Color TEXT       = AdminUI.TEXT;
    static final Color TEXT_GRAY  = AdminUI.TEXT_GRAY;
    static final Color ROW_SELECT = AdminRoomUI.ROW_SELECT;

    private final AdminTenantLogic logic = new AdminTenantLogic();

    JComboBox<String> statusFilter;
    JLabel lblCount;
    JTable table;
    DefaultTableModel tableModel;
    JLabel detailSub, tenantName;
    JLabel lblContractId, lblStatus, lblPhone, lblRoomType, lblRent, lblDeposit, lblStart, lblEnd;
    JButton btnAdd, btnEdit, btnRenew, btnTerminate;

    private AdminTenantLogic.TenantModel currentTenant = null;

    /**
     * คอนสตรัคเตอร์สำหรับสร้างหน้าจอจัดการทะเบียนผู้เช่าและสัญญาเช่า
     * กำหนดการจัดวางองค์ประกอบ แถบเครื่องมือ ตาราง ข้อมูลรายละเอียด และการผูกเหตุการณ์
     */
    public AdminTenantUI() {
        setLayout(null);
        setBackground(BG);

        createToolbar();
        add(createTable());
        add(createDetailPanel());

        bindSelection();
        initActions();
        refreshData();
    }

    /**
     * สร้างแถบเครื่องมือด้านบน ประกอบด้วยตัวกรองสถานะสัญญา จำนวนรายการ และปุ่มทำสัญญาใหม่
     */
    private void createToolbar() {
        statusFilter = styledCombo(new String[]{"สถานะทั้งหมด", "ใช้งานอยู่", "รออนุมัติ", "หมดอายุ", "ยกเลิกสัญญา"}, WHITE);
        statusFilter.setBounds(20, 20, 170, 34);
        add(statusFilter);

        lblCount = AdminUI.label("แสดง 0 รายการ", Font.PLAIN, 12, TEXT_GRAY);
        lblCount.setBounds(198, 20, 192, 34);
        lblCount.setVerticalAlignment(SwingConstants.CENTER);
        add(lblCount);

        btnAdd = new AdminRoomUI.FlatButton("+ ทำสัญญาใหม่", true);
        btnAdd.setBounds(400, 19, 160, 36);
        add(btnAdd);
    }

    /**
     * สร้างตารางแสดงรายชื่อผู้เช่า ข้อมูลห้อง วันที่สัญญา และสถานะสัญญา
     * 
     * @return JScrollPane ที่บรรจุตารางผู้เช่า
     */
    private JScrollPane createTable() {
        String[] columns = {"ชื่อผู้เช่า", "ห้อง", "เริ่มสัญญา", "สิ้นสุดสัญญา", "สถานะ"};
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
        scroll.setBounds(20, 70, 540, 510);
        return scroll;
    }

    /**
     * สร้างแผงแสดงรายละเอียดสัญญาเช่าของผู้เช่าที่ถูกเลือก พร้อมปุ่มจัดการสัญญา
     * 
     * @return JPanel แผงรายละเอียดสัญญา
     */
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

        btnEdit = new AdminRoomUI.FlatButton("แก้ไขสัญญา", false);
        btnEdit.setBounds(20, 360, 276, 36);
        p.add(btnEdit);

        btnRenew = new AdminRoomUI.FlatButton("ต่ออายุสัญญาเพิ่ม 1 ปี", true);
        btnRenew.setBounds(20, 406, 276, 36);
        p.add(btnRenew);

        btnTerminate = new AdminRoomUI.FlatButton("ยกเลิกสัญญา", false);
        btnTerminate.setForeground(AdminUI.RED);
        btnTerminate.setBounds(20, 452, 276, 36);
        p.add(btnTerminate);

        return p;
    }

    /**
     * ดึงข้อมูลผู้เช่าและสัญญาเช่าทั้งหมดจาก DataStore มากรองตามสถานะและแสดงผลในตาราง
     */
    public void refreshData() {
        tableModel.setRowCount(0);
        List<AdminTenantLogic.TenantModel> all = logic.getAllTenants();
        String filter = (statusFilter != null && statusFilter.getSelectedItem() != null)
                ? (String) statusFilter.getSelectedItem() : "สถานะทั้งหมด";

        int matched = 0;
        for (AdminTenantLogic.TenantModel t : all) {
            boolean match = false;
            if ("สถานะทั้งหมด".equals(filter)) {
                match = true;
            } else if ("ใช้งานอยู่".equals(filter) && t.status.contains("Active")) {
                match = true;
            } else if ("รออนุมัติ".equals(filter) && t.status.contains("Pending")) {
                match = true;
            } else if ("หมดอายุ".equals(filter) && t.status.contains("Expired")) {
                match = true;
            } else if ("ยกเลิกสัญญา".equals(filter) && t.status.contains("Terminated")) {
                match = true;
            }

            if (match) {
                tableModel.addRow(new Object[]{t.name, t.room, t.startDate, t.endDate, t.status});
                matched++;
            }
        }

        int[] widths = {110, 50, 95, 95, 190};
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        if (lblCount != null) {
            lblCount.setText("แสดง " + matched + " รายการ จากทั้งหมด " + all.size() + " รายการ");
        }

        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        } else {
            clearDetailPanel();
        }
    }

    /**
     * ล้างข้อมูลในแผงรายละเอียดสัญญาและปิดการใช้งานปุ่มคำสั่งเมื่อไม่มีการเลือกแถวในตาราง
     */
    private void clearDetailPanel() {
        currentTenant = null;
        detailSub.setText("-");
        tenantName.setText("-");
        lblContractId.setText("-");
        lblStatus.setText("-");
        lblPhone.setText("-");
        lblRoomType.setText("-");
        lblRent.setText("-");
        lblDeposit.setText("-");
        lblStart.setText("-");
        lblEnd.setText("-");

        if (btnEdit != null) btnEdit.setEnabled(false);
        if (btnRenew != null) btnRenew.setEnabled(false);
        if (btnTerminate != null) btnTerminate.setEnabled(false);
    }

    /**
     * ผูกเหตุการณ์การเลือกแถวในตาราง เพื่อดึงข้อมูลสัญญาเช่าของผู้เช่านั้นมาแสดงในแผงรายละเอียด
     */
    private void bindSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                String tenantNameText = String.valueOf(tableModel.getValueAt(row, 0));
                String roomText = String.valueOf(tableModel.getValueAt(row, 1));
                for (AdminTenantLogic.TenantModel t : logic.getAllTenants()) {
                    if (t.name.equals(tenantNameText) && t.room.equals(roomText)) {
                        currentTenant = t;
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

                        if (btnEdit != null) btnEdit.setEnabled(true);
                        if (btnRenew != null) btnRenew.setEnabled(true);
                        if (btnTerminate != null) btnTerminate.setEnabled(true);
                        break;
                    }
                }
            }
        });
    }

    /**
     * กำหนดการทำงานของส่วนควบคุมและปุ่มคำสั่งต่างๆ บนหน้าจอ
     */
    private void initActions() {
        statusFilter.addActionListener(e -> refreshData());

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

    /**
     * แสดงหน้าต่าง Dialog สำหรับการกรอกข้อมูลสัญญาเช่า (รองรับทั้งการทำสัญญาใหม่และการแก้ไขสัญญาเดิม)
     * 
     * @param model ข้อมูลสัญญาเดิมกรณีแก้ไข หรือ null กรณีทำสัญญาใหม่
     */
    private void showFormDialog(AdminTenantLogic.TenantModel model) {
        boolean isEdit = (model != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "แก้ไขสัญญา" : "ทำสัญญาใหม่", true);
        dialog.setSize(340, 340);
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
        cbUser.setBounds(100, 20, 200, 25); dialog.add(cbUser);

        JLabel l2 = new JLabel("ห้องพัก:"); l2.setBounds(20, 60, 80, 25); dialog.add(l2);
        JComboBox<String> cbRoom = new JComboBox<>();
        for (DataStore.Room r : DataStore.rooms()) {
            boolean occupied = AdminTenantLogic.isRoomOccupied(r.id);
            // สัญญาใหม่แสดงเฉพาะห้องว่าง / แก้ไขสัญญาแสดงห้องว่างและห้องเดิมของสัญญานี้
            if (!occupied || (isEdit && (r.id.equalsIgnoreCase(model.roomId) || r.number.equalsIgnoreCase(model.room)))) {
                cbRoom.addItem(r.id + " (ห้อง " + r.number + " - " + r.typeShort() + ")");
            }
        }

        if (cbRoom.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "ขณะนี้ไม่มีห้องว่างสำหรับทำสัญญาใหม่", "ไม่มีห้องว่าง", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (isEdit) {
            for (int i = 0; i < cbRoom.getItemCount(); i++) {
                String item = cbRoom.getItemAt(i);
                if (item.startsWith(model.roomId + " ")) {
                    cbRoom.setSelectedIndex(i);
                    break;
                }
            }
        }
        cbRoom.setBounds(100, 60, 200, 25); dialog.add(cbRoom);

        JLabel l3 = new JLabel("ค่าเช่า:"); l3.setBounds(20, 100, 80, 25); dialog.add(l3);
        JTextField txtRent = new JTextField();
        txtRent.setBounds(100, 100, 200, 25); dialog.add(txtRent);

        JLabel l4 = new JLabel("เงินประกัน:"); l4.setBounds(20, 140, 80, 25); dialog.add(l4);
        JTextField txtDeposit = new JTextField();
        txtDeposit.setBounds(100, 140, 200, 25); dialog.add(txtDeposit);

        // คำนวณค่าเช่าและเงินประกันอัตโนมัติตามประเภทห้อง (เช่น ห้องแอร์ เงินประกัน 7,000, ห้องพัดลม 5,600)
        Runnable updateRentAndDeposit = () -> {
            String rSel = (String) cbRoom.getSelectedItem();
            if (rSel != null) {
                String rId = rSel.split(" ")[0].trim();
                DataStore.Room r = DataStore.room(rId);
                if (r == null) r = DataStore.roomByNumber(rId);
                if (r != null) {
                    txtRent.setText(String.valueOf(r.rent));
                    int dep = (r.isAir() || (r.type != null && r.type.toLowerCase().contains("air"))) ? 7000 : (r.rent * 2);
                    txtDeposit.setText(String.valueOf(dep));
                }
            }
        };

        if (isEdit) {
            txtRent.setText(model.rent.replace(" บาท", "").replace(",", ""));
            txtDeposit.setText(model.deposit.replace(" บาท", "").replace(",", ""));
        } else {
            updateRentAndDeposit.run();
        }

        cbRoom.addActionListener(ev -> updateRentAndDeposit.run());

        JButton btnSave = new JButton("บันทึก");
        btnSave.setBounds(100, 200, 120, 35);
        btnSave.addActionListener(ev -> {
            try {
                String uSel = (String) cbUser.getSelectedItem();
                String rSel = (String) cbRoom.getSelectedItem();
                if (uSel == null || rSel == null) {
                    JOptionPane.showMessageDialog(dialog, "กรุณาเลือกผู้ใช้งานและห้องพัก", "ข้อมูลไม่ครบ", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String uId = uSel.split(" - ")[0].trim();
                String rId = rSel.split(" ")[0].trim();

                // ตรวจสอบห้องซ้ำว่ามีผู้อาศัยอยู่แล้วหรือไม่
                if (!isEdit && AdminTenantLogic.isRoomOccupied(rId)) {
                    JOptionPane.showMessageDialog(dialog, "ไม่สามารถทำสัญญาได้ เนื่องจากห้องนี้มีผู้อาศัยอยู่แล้ว", "ห้องไม่ว่าง", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (isEdit && !rId.equalsIgnoreCase(model.roomId) && AdminTenantLogic.isRoomOccupied(rId)) {
                    JOptionPane.showMessageDialog(dialog, "ไม่สามารถย้ายสัญญาไปยังห้องนี้ได้ เนื่องจากห้องนี้มีผู้อาศัยอยู่แล้ว", "ห้องไม่ว่าง", JOptionPane.WARNING_MESSAGE);
                    return;
                }

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
                } else {
                    JOptionPane.showMessageDialog(dialog, "ไม่สามารถทำสัญญาได้ กรุณาตรวจสอบสถานะห้องพัก", "เกิดข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "กรุณากรอกค่าเช่าและเงินประกันเป็นตัวเลข", "ข้อมูลไม่ถูกต้อง", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "ข้อมูลไม่ถูกต้อง: " + ex.getMessage());
            }
        });
        dialog.add(btnSave);
        dialog.setVisible(true);
    }

    /**
     * สร้างแถวแสดงข้อมูลหัวข้อและค่าของรายละเอียดสัญญา พร้อมเพิ่มลงในแผง
     * 
     * @param p แผงพาเนลเป้าหมาย
     * @param title หัวข้อข้อมูล
     * @param value ค่าข้อมูล
     * @param y พิกัดแกน Y
     * @return JLabel ป้ายข้อความแสดงค่าข้อมูล
     */
    private static JLabel detailRow(JPanel p, String title, String value, int y) {
        JLabel t = AdminUI.label(title, Font.PLAIN, 11, TEXT_GRAY);
        t.setBounds(20, y, 120, 18); p.add(t);
        JLabel v = AdminUI.label(value, Font.BOLD, 11, TEXT);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        v.setBounds(110, y, 186, 18); p.add(v);
        return v;
    }

    /**
     * HeaderRenderer คือคลาสกำหนดการแสดงผลส่วนหัวของตารางผู้เช่า
     */
    static class HeaderRenderer extends DefaultTableCellRenderer {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ คอมโพเนนต์เรนเดอร์ส่วนหัวของตารางผู้เช่า
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * กำหนดรูปแบบและสีส่วนหัวตารางผู้เช่า
         * 
         * @param t ตาราง
         * @param v ค่าหัวตาราง
         * @param sel ถูกเลือกหรือไม่
         * @param focus มีโฟกัสหรือไม่
         * @param row ดัชนีแถว
         * @param col ดัชนีคอลัมน์
         * @return คอมโพเนนต์สำหรับแสดงผลหัวตาราง
         */
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setFont(AdminUI.font(Font.PLAIN, 11)); setForeground(TEXT_GRAY); setBackground(WHITE);
            setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 10, 0, 10)));
            return this;
        }
    }

    /**
     * CellRenderer คือคลาสกำหนดการแสดงผลข้อมูลแต่ละเซลล์ในตารางผู้เช่า
     */
    static class CellRenderer extends DefaultTableCellRenderer {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ คอมโพเนนต์เรนเดอร์เซลล์ตารางผู้เช่าพร้อมป้ายสถานะ Badge
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * กำหนดรูปแบบเซลล์ตารางผู้เช่า และวาดป้ายสถานะ (Badge) สำหรับคอลัมน์สถานะ
         * 
         * @param t ตาราง
         * @param v ค่าข้อมูล
         * @param sel เซลล์ถูกเลือกหรือไม่
         * @param focus เซลล์มีโฟกัสหรือไม่
         * @param row ลำดับแถว
         * @param col ลำดับคอลัมน์
         * @return คอมโพเนนต์สำหรับแสดงผลเซลล์
         */
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Color rowBg = sel ? ROW_SELECT : WHITE;
            if (col == 4) {
                String s = String.valueOf(v);
                Color bg = new Color(254, 226, 226);
                Color fg = new Color(220, 38, 38);
                if (s.contains("ใช้งานอยู่") || s.contains("Active")) {
                    bg = new Color(209, 250, 229);
                    fg = new Color(4, 120, 87);
                } else if (s.contains("รออนุมัติ") || s.contains("Pending")) {
                    bg = new Color(254, 243, 199);
                    fg = new Color(180, 83, 9);
                } else if (s.contains("หมดอายุ") || s.contains("Expired")) {
                    bg = new Color(243, 244, 246);
                    fg = new Color(107, 114, 128);
                }
                AdminUI.Badge badge = new AdminUI.Badge(s, bg, fg);
                badge.setPreferredSize(badge.getSize());
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
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

    /**
     * สร้างคอมโบ้บ็อกซ์ที่จัดรูปแบบสีและขอบโค้งมนตามมาตรฐานระบบ
     * 
     * @param items รายการตัวเลือก
     * @param bg สีพื้นหลัง
     * @return JComboBox ที่ตกแต่งแล้ว
     */
    private static JComboBox<String> styledCombo(String[] items, Color bg) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AdminUI.font(Font.PLAIN, 12));
        cb.setForeground(TEXT);
        cb.setBackground(bg);
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(0, 6, 0, 0)));
        cb.setFocusable(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override protected JButton createArrowButton() { return new AdminRoomUI.ChevronButton(); }
        });
        return cb;
    }
}