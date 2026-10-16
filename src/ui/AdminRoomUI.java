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

/**
 * AdminRoomUI คือคลาสหน้าจอส่วนแสดงผลสำหรับผู้ดูแลระบบในการจัดการข้อมูลห้องพัก
 * statusFilter คือ คอมโบ้บ็อกซ์สำหรับเลือกกรองตามสถานะห้องพัก
 * lblCount คือ ป้ายแสดงจำนวนห้องพักที่แสดงผล
 * btnAdd คือ ปุ่มกดสำหรับเพิ่มข้อมูลห้องพักใหม่
 * table คือ ตารางแสดงรายการห้องพักทั้งหมด
 * tableModel คือ โมเดลข้อมูลของตารางห้องพัก
 * formTitle คือ ป้ายแสดงหัวเรื่องของฟอร์มกรอกข้อมูลห้องพัก
 * formSub คือ ป้ายแสดงคำอธิบายของฟอร์ม
 * txtNumber คือ ช่องกรอกหมายเลขห้องพัก
 * txtRent คือ ช่องกรอกอัตราค่าเช่าห้องพัก
 * cbType คือ คอมโบ้บ็อกซ์สำหรับเลือกประเภทห้องพัก
 * rbAvailable คือ ปุ่มตัวเลือกสถานะห้องว่าง
 * rbOccupied คือ ปุ่มตัวเลือกสถานะมีผู้เช่า
 * rbMaintenance คือ ปุ่มตัวเลือกสถานะปิดปรับปรุง
 * btnSave คือ ปุ่มกดสำหรับบันทึกข้อมูลห้องพัก
 * selectedRoomId คือ รหัสห้องพักที่กำลังเลือกแก้ไขข้อมูลอยู่ในปัจจุบัน
 * previousStatus คือ ตัวแปรสำหรับเก็บสถานะเดิมของห้องพักก่อนการแก้ไข
 */
public class AdminRoomUI extends JPanel {

    // ===== representation =====
    // Abstraction Function:
    // AF(table, tableModel, selectedRoomId, previousStatus) คือ หน้าจอสำหรับแสดงรายการห้องพัก เพิ่ม แก้ไข และปรับปรุงสถานะห้องพักในระบบ
    // RI
    // 1. table และ tableModel ต้องไม่เป็น null
    // 2. previousStatus ต้องไม่เป็น null

    static final Color PRIMARY    = AdminUI.PRIMARY;
    static final Color BG         = AdminUI.BG;
    static final Color WHITE      = AdminUI.WHITE;
    static final Color BORDER     = AdminUI.BORDER;
    static final Color TEXT       = AdminUI.TEXT;
    static final Color TEXT_GRAY  = AdminUI.TEXT_GRAY;
    static final Color TEXT_LIGHT = AdminUI.TEXT_LIGHT;
    static final Color ITEM_BG    = AdminUI.ITEM_BG;
    public static final Color ROW_SELECT = new Color(240, 253, 250);

    JComboBox<String> statusFilter;
    JLabel lblCount;
    JButton btnAdd;
    JTable table;
    DefaultTableModel tableModel;
    JLabel formTitle, formSub;
    JTextField txtNumber, txtRent;
    JComboBox<String> cbType;
    JRadioButton rbAvailable, rbOccupied, rbMaintenance;
    JButton btnSave;

    private String selectedRoomId = "";
    private String previousStatus = "AVAILABLE"; // ตัวแปรสำหรับเก็บสถานะเดิมของห้องพัก

    /**
     * คอนสตรักเตอร์สร้างหน้าจอจัดการข้อมูลห้องพักสำหรับผู้ดูแลระบบ
     */
    public AdminRoomUI() {
        setLayout(null);
        setBackground(BG);

        createToolbar();
        add(createTable());
        add(createFormPanel());

        loadTableData();
        initActions();
    }

    /**
     * โหลดข้อมูลห้องพักทั้งหมดและอัปเดตการแสดงผลในตารางตามตัวกรองสถานะห้อง
     */
    public void loadTableData() {
        tableModel.setRowCount(0);
        List<AdminRoomLogic.RoomModel> rooms = AdminRoomLogic.getAllRooms();
        
        String selectedFilter = (statusFilter != null && statusFilter.getSelectedItem() != null) 
                                ? (String) statusFilter.getSelectedItem() 
                                : "สถานะทั้งหมด";

        int matched = 0;
        for (AdminRoomLogic.RoomModel r : rooms) {
            boolean matchFilter = false;
            if ("สถานะทั้งหมด".equals(selectedFilter)) {
                matchFilter = true;
            } else if ("ห้องว่าง".equals(selectedFilter) && "ห้องว่าง".equalsIgnoreCase(r.statusThai)) {
                matchFilter = true;
            } else if ("มีผู้เช่า".equals(selectedFilter) && "มีผู้เช่า".equalsIgnoreCase(r.statusThai)) {
                matchFilter = true;
            } else if ("ปิดปรับปรุง".equals(selectedFilter) && "ปิดปรับปรุง".equalsIgnoreCase(r.statusThai)) {
                matchFilter = true;
            }

            if (matchFilter) {
                tableModel.addRow(new Object[]{r.id, r.number, r.floor, r.type, r.rent, r.statusThai});
                matched++;
            }
        }

        if (lblCount != null) {
            lblCount.setText("แสดง " + matched + " รายการ จากทั้งหมด " + rooms.size() + " รายการ");
        }

        if (table.getRowCount() > 0) {
            int targetRow = 0;
            if (!selectedRoomId.isEmpty()) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (selectedRoomId.equalsIgnoreCase(String.valueOf(tableModel.getValueAt(i, 0)))) {
                        targetRow = i;
                        break;
                    }
                }
            }
            table.setRowSelectionInterval(targetRow, targetRow);
        } else {
            clearForm();
        }
    }

    /**
     * ล้างข้อมูลในฟอร์มแก้ไขห้องพักให้เป็นค่าว่าง
     */
    private void clearForm() {
        selectedRoomId = "";
        if (txtNumber != null) txtNumber.setText("");
        if (formSub != null) formSub.setText("เลือกห้องจากตารางเพื่อแก้ไขข้อมูล");
        if (txtRent != null) txtRent.setText("");
        if (btnSave != null) btnSave.setEnabled(false);
    }

    /**
     * สร้างแถบเครื่องมือด้านบน ประกอบด้วยตัวกรองสถานะห้อง ป้ายแสดงจำนวน และปุ่มเพิ่มห้องใหม่
     */
    private void createToolbar() {
        statusFilter = styledCombo(new String[]{"สถานะทั้งหมด", "ห้องว่าง", "มีผู้เช่า", "ปิดปรับปรุง"}, WHITE);
        statusFilter.setBounds(20, 20, 180, 34);
        add(statusFilter);

        lblCount = AdminUI.label("แสดง 0 รายการ", Font.PLAIN, 12, TEXT_GRAY);
        lblCount.setBounds(215, 20, 205, 34);
        lblCount.setVerticalAlignment(SwingConstants.CENTER);
        add(lblCount);

        btnAdd = new FlatButton("+  เพิ่มห้องใหม่", true);
        btnAdd.setBounds(430, 19, 130, 36);
        add(btnAdd);
    }

    /**
     * สร้างและจัดรูปแบบตารางแสดงรายการห้องพัก
     * 
     * @return JScrollPane ที่ครอบตารางห้องพัก
     */
    private JScrollPane createTable() {
        String[] columns = {"รหัส", "เลขห้อง", "ชั้น", "ประเภท", "ราคา/เดือน", "สถานะ"};

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

        int[] widths = {85, 80, 55, 100, 90, 110};
        TableColumnModel cols = table.getColumnModel();
        for (int i = 0; i < widths.length; i++) cols.getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        scroll.setBounds(20, 70, 540, 510);
        return scroll;
    }

    /**
     * สร้างพาเนลฟอร์มสำหรับแก้ไขรายละเอียดห้องพัก
     * 
     * @return JPanel แผงฟอร์มแก้ไขห้องพัก
     */
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
        rbMaintenance = radio("ปิดปรับปรุง (Maintenance)");
        ButtonGroup group = new ButtonGroup();
        JRadioButton[] radios = {rbAvailable, rbOccupied, rbMaintenance};
        for (int i = 0; i < radios.length; i++) {
            group.add(radios[i]);
            radios[i].setBounds(18, 308 + i * 24, 280, 22);
            p.add(radios[i]);
        }

        p.add(divider(388));

        btnSave = new FlatButton("บันทึกข้อมูล", true);
        btnSave.setBounds(20, 404, 276, 40);
        p.add(btnSave);

        return p;
    }

    /**
     * กำหนด Event Handlers สำหรับการโต้ตอบกับตาราง การบันทึกแก้ไข และการเปิดหน้าต่างเพิ่มห้องใหม่
     */
    private void initActions() {
        statusFilter.addActionListener(e -> loadTableData());

        // เลือกแถวจากตาราง -> โหลดข้อมูลเข้าแผงแก้ไขฝั่งขวา
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                selectedRoomId = String.valueOf(tableModel.getValueAt(row, 0));
                String roomNum = String.valueOf(tableModel.getValueAt(row, 1)).replace("ห้อง ", "");
                String type = String.valueOf(tableModel.getValueAt(row, 3));
                String rent = String.valueOf(tableModel.getValueAt(row, 4)).replace(",", "");
                String status = String.valueOf(tableModel.getValueAt(row, 5));

                txtNumber.setText(roomNum);
                formSub.setText("แก้ไขข้อมูลห้อง " + roomNum + " (รหัส " + selectedRoomId + ")");
                txtRent.setText(rent);
                cbType.setSelectedItem(type);

                if (status.equalsIgnoreCase("ห้องว่าง") || status.equalsIgnoreCase("AVAILABLE")) {
                    previousStatus = "AVAILABLE";
                    rbAvailable.setSelected(true);
                } else if (status.equalsIgnoreCase("มีผู้เช่า") || status.equalsIgnoreCase("OCCUPIED")) {
                    previousStatus = "OCCUPIED";
                    rbOccupied.setSelected(true);
                } else {
                    previousStatus = "MAINTENANCE";
                    rbMaintenance.setSelected(true);
                }
                btnSave.setEnabled(true);
            }
        });

        // ตรวจจับการพิมพ์เลขห้องเพื่อแสดงตัวอย่างรหัสห้องพักแบบไดนามิก
        txtNumber.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateLabel() {
                String t = txtNumber.getText().trim().replace("ห้อง ", "").trim();
                if (t.toUpperCase().startsWith("R") && t.length() > 1 && Character.isDigit(t.charAt(1))) {
                    t = t.substring(1).trim();
                }
                if (!t.isEmpty() && !selectedRoomId.isEmpty()) {
                    formSub.setText("แก้ไขข้อมูลห้อง " + t + " (รหัส R" + t + ")");
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateLabel(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateLabel(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateLabel(); }
        });

        // บันทึกการแก้ไขข้อมูลห้องพัก (แผงขวามือ)
        btnSave.addActionListener(e -> {
            if (selectedRoomId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "กรุณาเลือกห้องจากตารางเพื่อแก้ไขข้อมูล", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String num = txtNumber.getText().trim();
                int rent = Integer.parseInt(txtRent.getText().trim().replace(",", ""));
                String type = (String) cbType.getSelectedItem();
                String status = rbAvailable.isSelected() ? "AVAILABLE" : (rbOccupied.isSelected() ? "OCCUPIED" : "MAINTENANCE");

                String newRoomId = AdminRoomLogic.saveOrUpdateRoom(selectedRoomId, num, type, rent, status);
                selectedRoomId = newRoomId;
                previousStatus = status;
                loadTableData();
                JOptionPane.showMessageDialog(this, "บันทึกข้อมูลห้องพักเรียบร้อยแล้ว (รหัส " + newRoomId + ")", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "ไม่อนุญาตให้ดำเนินการ", JOptionPane.ERROR_MESSAGE);
                resetStatusToPrevious();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "ข้อมูลไม่ถูกต้อง: " + ex.getMessage(), "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 🟢 ปุ่มเพิ่มห้องใหม่ -> เรียกใช้งาน Pop-up Dialog
        btnAdd.addActionListener(e -> showAddRoomDialog());
    }

    /**
     * แสดงหน้าต่าง Pop-up Dialog สำหรับการเพิ่มห้องพักใหม่ลงในระบบ
     */
    private void showAddRoomDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "เพิ่มห้องพักใหม่", true);
        dialog.setSize(340, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(null);
        dialog.getContentPane().setBackground(WHITE);

        JLabel l1 = new JLabel("เลขห้องพัก:"); 
        l1.setBounds(25, 20, 90, 25); 
        l1.setFont(AdminUI.font(Font.PLAIN, 12));
        dialog.add(l1);
        
        RoundField txtAddNumber = new RoundField("เช่น 101", ITEM_BG, false);
        txtAddNumber.setBounds(115, 20, 180, 34); 
        dialog.add(txtAddNumber);

        JLabel l2 = new JLabel("ประเภทห้อง:"); 
        l2.setBounds(25, 65, 90, 25); 
        l2.setFont(AdminUI.font(Font.PLAIN, 12));
        dialog.add(l2);
        
        JComboBox<String> cbAddType = styledCombo(new String[]{"ห้องพัดลม", "ห้องแอร์"}, ITEM_BG);
        cbAddType.setBounds(115, 65, 180, 34); 
        dialog.add(cbAddType);

        JLabel l3 = new JLabel("ค่าเช่า/เดือน:"); 
        l3.setBounds(25, 110, 90, 25); 
        l3.setFont(AdminUI.font(Font.PLAIN, 12));
        dialog.add(l3);
        
        RoundField txtAddRent = new RoundField("เช่น 3500", ITEM_BG, false);
        txtAddRent.setText("2800");
        txtAddRent.setBounds(115, 110, 180, 34); 
        dialog.add(txtAddRent);

        JLabel l4 = new JLabel("สถานะห้อง:"); 
        l4.setBounds(25, 155, 90, 25); 
        l4.setFont(AdminUI.font(Font.PLAIN, 12));
        dialog.add(l4);

        JRadioButton rbAddAvailable   = radio("ห้องว่าง");
        JRadioButton rbAddOccupied    = radio("มีผู้เช่า");
        JRadioButton rbAddMaintenance = radio("ปิดปรับปรุง");
        
        ButtonGroup group = new ButtonGroup();
        group.add(rbAddAvailable); 
        group.add(rbAddOccupied); 
        group.add(rbAddMaintenance);

        rbAddAvailable.setBounds(115, 155, 180, 22);
        rbAddOccupied.setBounds(115, 180, 180, 22);
        rbAddMaintenance.setBounds(115, 205, 180, 22);
        rbAddAvailable.setSelected(true);

        dialog.add(rbAddAvailable);
        dialog.add(rbAddOccupied);
        dialog.add(rbAddMaintenance);

        JButton btnSaveNew = new FlatButton("บันทึกข้อมูล", true);
        btnSaveNew.setBounds(25, 255, 270, 40);
        btnSaveNew.addActionListener(ev -> {
            try {
                String num = txtAddNumber.getText().trim();
                if (num.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "กรุณาระบุเลขห้องพัก", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int rent = Integer.parseInt(txtAddRent.getText().trim().replace(",", ""));
                String type = (String) cbAddType.getSelectedItem();
                String status = rbAddAvailable.isSelected() ? "AVAILABLE" : (rbAddOccupied.isSelected() ? "OCCUPIED" : "MAINTENANCE");

                // ส่งค่า roomId เป็น "" เพื่อสร้างห้องใหม่ลงไฟล์ rooms.csv
                String newRoomId = AdminRoomLogic.saveOrUpdateRoom("", num, type, rent, status);
                selectedRoomId = newRoomId;
                
                JOptionPane.showMessageDialog(dialog, "เพิ่มห้องพักใหม่เรียบร้อยแล้ว (รหัส " + newRoomId + ")", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadTableData();

            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "ไม่สามารถเพิ่มห้องพักได้", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "ข้อมูลไม่ถูกต้อง: " + ex.getMessage(), "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(btnSaveNew);
        dialog.setVisible(true);
    }

    /**
     * คืนค่าการเลือกของ Radio Button สถานะห้องพักกลับเป็นสถานะเดิมก่อนแก้ไข
     */
    private void resetStatusToPrevious() {
        if ("AVAILABLE".equals(previousStatus)) {
            rbAvailable.setSelected(true);
        } else if ("OCCUPIED".equals(previousStatus)) {
            rbOccupied.setSelected(true);
        } else {
            rbMaintenance.setSelected(true);
        }
    }

    /**
     * สร้างป้ายข้อความกำกับฟิลด์ข้อมูล
     * 
     * @param text ข้อความหัวข้อ
     * @param y ตำแหน่งแกน Y
     * @return JLabel ป้ายกำกับ
     */
    private static JLabel fieldLabel(String text, int y) {
        JLabel l = AdminUI.label(text, Font.PLAIN, 11, TEXT_GRAY);
        l.setBounds(20, y, 276, 16);
        return l;
    }

    /**
     * สร้างเส้นแบ่งแนวนอน
     * 
     * @param y ตำแหน่งแกน Y
     * @return JPanel เส้นแบ่ง
     */
    private static JPanel divider(int y) {
        JPanel line = new JPanel();
        line.setBackground(BORDER);
        line.setBounds(0, y, 330, 1);
        return line;
    }

    /**
     * สร้างปุ่มตัวเลือก Radio Button พร้อมตกแต่งไอคอนเฉพาะของระบบ
     * 
     * @param text ข้อความตัวเลือก
     * @return JRadioButton ที่จัดรูปแบบแล้ว
     */
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

    /**
     * สร้างคอมโบ้บ็อกซ์พร้อมจัดรูปแบบและสีพื้นหลังตามที่กำหนด
     * 
     * @param items รายการตัวเลือก
     * @param bg สีพื้นหลัง
     * @return JComboBox ที่จัดรูปแบบแล้ว
     */
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

    /**
     * RoundField คือคอมโพเนนต์กล่องข้อความขอบโค้งมนพร้อมข้อความตัวอย่าง (Placeholder)
     * placeholder คือ ข้อความแนะนำในช่องกรอกเมื่อยังไม่มีข้อมูล
     * bg คือ สีพื้นหลังของช่องกรอก
     * searchIcon คือ ตัวกำหนดว่าต้องวาดไอคอนรูปแว่นขยายค้นหาหรือไม่
     */
    public static class RoundField extends JTextField {
        // ===== representation =====
        // Abstraction Function:
        // AF(placeholder, bg, searchIcon) คือ กล่องข้อความกรอกข้อมูลที่มีขอบโค้งมนและแสดงไอคอนค้นหาตามต้องการ
        // RI
        // 1. bg ต้องไม่เป็น null

        private final String placeholder;
        private final Color bg;
        private final boolean searchIcon;

        /**
         * สร้างกล่องข้อความขอบมนพร้อมกำหนดข้อความตัวอย่าง สีพื้นหลัง และไอคอนค้นหา
         * 
         * @param placeholder ข้อความแนะนำในช่องกรอก
         * @param bg สีพื้นหลัง
         * @param searchIcon กำหนดว่าต้องวาดไอคอนแว่นขยายหรือไม่
         */
        public RoundField(String placeholder, Color bg, boolean searchIcon) {
            this.placeholder = placeholder;
            this.bg = bg;
            this.searchIcon = searchIcon;
            setOpaque(false);
            setFont(AdminUI.font(Font.PLAIN, 12));
            setForeground(TEXT);
            setCaretColor(TEXT);
            setBorder(new EmptyBorder(0, searchIcon ? 32 : 12, 0, 12));
        }

        /**
         * วาดพื้นหลังขอบมน เส้นขอบ ไอคอนค้นหา และข้อความ placeholder
         * 
         * @param g กราฟิกส์คอนเท็กซ์สำหรับวาด
         */
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

    /**
     * FlatButton คือคอมโพเนนต์ปุ่มกดดีไซน์แบนราบแบบโมเดิร์น
     * primary คือ ตัวกำหนดว่าปุ่มนี้เป็นปุ่มหลัก (เน้นสีธีม) หรือปุ่มรอง
     */
    public static class FlatButton extends JButton {
        // ===== representation =====
        // Abstraction Function:
        // AF(primary) คือ ปุ่มกดสไตล์โมเดิร์นที่ปรับสีตามประเภทหลักหรือรอง
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        private final boolean primary;

        /**
         * สร้างปุ่มกดสไตล์โมเดิร์น
         * 
         * @param text ข้อความบนปุ่ม
         * @param primary กำหนดว่าเป็นปุ่มหลัก (สีเด่น) หรือปุ่มรอง
         */
        public FlatButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(AdminUI.font(Font.BOLD, 12));
            setForeground(primary ? WHITE : TEXT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        /**
         * วาดปุ่มขอบมนและเอฟเฟกต์สีเมื่อเมาส์ชี้ผ่าน (Rollover)
         * 
         * @param g กราฟิกส์คอนเท็กซ์สำหรับวาด
         */
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

    /**
     * ChevronButton คือคอมโพเนนต์ปุ่มลูกศรชี้ลงสำหรับคอมโบ้บ็อกซ์
     */
    public static class ChevronButton extends JButton {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ ปุ่มไอคอนรูปลูกศรชี้ลงสำหรับเลือกรายการ
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * สร้างปุ่มลูกศรชี้ลงแบบโปร่งใสไม่มีเส้นขอบเริ่มต้น
         */
        public ChevronButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        /**
         * วาดสัญลักษณ์ลูกศรชี้ลงด้วยลายเส้นโค้งมน
         * 
         * @param g กราฟิกส์คอนเท็กซ์สำหรับวาด
         */
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

    /**
     * RadioIcon คือไอคอนแบบกำหนดเองสำหรับปุ่มตัวเลือก Radio Button
     */
    static class RadioIcon implements Icon {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ ไอคอนวงกลมแสดงสถานะเลือกหรือไม่เลือกของ Radio Button
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * วาดไอคอนปุ่มวิทยุ (Radio) ตามสถานะการเลือกของปุ่ม
         * 
         * @param c คอมโพเนนต์ที่เรียกใช้วาดไอคอน
         * @param g กราฟิกส์คอนเท็กซ์
         * @param x ตำแหน่งแกน X
         * @param y ตำแหน่งแกน Y
         */
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

        /**
         * คืนค่าความกว้างของไอคอน
         * 
         * @return ความกว้าง 16 พิกเซล
         */
        @Override public int getIconWidth()  { return 16; }

        /**
         * คืนค่าความสูงของไอคอน
         * 
         * @return ความสูง 16 พิกเซล
         */
        @Override public int getIconHeight() { return 16; }
    }

    /**
     * HeaderRenderer คือคลาสกำหนดการแสดงผลส่วนหัวของตารางห้องพัก
     */
    static class HeaderRenderer extends DefaultTableCellRenderer {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ คอมโพเนนต์เรนเดอร์ส่วนหัวตารางห้องพัก
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * กำหนดรูปแบบและฟอนต์สำหรับส่วนหัวตาราง
         * 
         * @param t ตาราง
         * @param v ค่าของเซลล์หัวตาราง
         * @param sel ถูกเลือกหรือไม่
         * @param focus มีโฟกัสหรือไม่
         * @param row ดัชนีแถว
         * @param col ดัชนีคอลัมน์
         * @return คอมโพเนนต์ที่ใช้แสดงผลหัวตาราง
         */
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setFont(AdminUI.font(Font.PLAIN, 11));
            setForeground(TEXT_GRAY);
            setBackground(WHITE);
            setHorizontalAlignment(col == 4 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 14, 0, 14)));
            return this;
        }
    }

    /**
     * CellRenderer คือคลาสกำหนดการแสดงผลข้อมูลแต่ละเซลล์ในตารางห้องพัก
     */
    static class CellRenderer extends DefaultTableCellRenderer {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ คอมโพเนนต์เรนเดอร์เซลล์ตารางห้องพักพร้อมป้ายสถานะ Badge
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * กำหนดการแสดงผลเซลล์ตารางห้องพัก พร้อมวาดป้ายสถานะ (Badge) สำหรับคอลัมน์สถานะ
         * 
         * @param t ตาราง
         * @param v ค่าข้อมูลในเซลล์
         * @param sel เซลล์ถูกเลือกหรือไม่
         * @param focus เซลล์มีโฟกัสหรือไม่
         * @param row ลำดับแถว
         * @param col ลำดับคอลัมน์
         * @return คอมโพเนนต์สำหรับแสดงผลเซลล์
         */
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Color rowBg = sel ? ROW_SELECT : WHITE;

            if (col == 5) {
                String s = (v != null) ? String.valueOf(v).trim() : "";
                
                Color bg, fg;

                if ("ห้องว่าง".equalsIgnoreCase(s) || "AVAILABLE".equalsIgnoreCase(s)) {
                    bg = new Color(209, 250, 229); 
                    fg = new Color(4, 120, 87);
                } 
                else if ("มีผู้เช่า".equalsIgnoreCase(s) || "OCCUPIED".equalsIgnoreCase(s)) {
                    bg = new Color(224, 242, 254); 
                    fg = new Color(3, 105, 161);
                } 
                else {
                    bg = new Color(254, 243, 199); 
                    fg = new Color(180, 83, 9);
                }

                JPanel badgePanel = new JPanel(new GridBagLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                badgePanel.setOpaque(false);

                JLabel lbl = new JLabel(s);
                lbl.setFont(AdminUI.font(Font.BOLD, 10));
                lbl.setForeground(fg);
                badgePanel.add(lbl);

                FontMetrics fm = t.getFontMetrics(lbl.getFont());
                int badgeW = fm.stringWidth(s) + 16;
                badgePanel.setPreferredSize(new Dimension(badgeW, 22));

                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 9));
                cell.setBackground(rowBg);
                cell.add(badgePanel);
                return cell;
            }

            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setBackground(rowBg);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setHorizontalAlignment(col == 4 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (col == 0 || col == 1) {
                setFont(AdminUI.font(Font.BOLD, 12));
                setForeground(TEXT);
            } else {
                setFont(AdminUI.font(Font.PLAIN, 12));
                setForeground(col == 4 ? TEXT : new Color(55, 65, 81));
            }
            return this;
        }
    }
}