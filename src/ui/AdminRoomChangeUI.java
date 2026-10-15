package ui;

import logic.AdminRoomChangeLogic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AdminRoomChangeUI คือคลาสหน้าจอส่วนแสดงผลสำหรับผู้ดูแลระบบในการพิจารณาคำขอย้ายห้องพัก
 * logic คือ อ็อบเจกต์ตรรกะสำหรับจัดการคำขอย้ายห้องพัก
 * statusFilter คือ คอมโบ้บ็อกซ์สำหรับเลือกกรองตามสถานะคำขอย้ายห้อง
 * lblCount คือ ป้ายแสดงจำนวนรายการคำขอที่แสดงผล
 * table คือ ตารางแสดงรายการคำขอย้ายห้องพัก
 * tableModel คือ โมเดลข้อมูลของตารางคำขอย้ายห้องพัก
 * detailTitle คือ ป้ายแสดงหัวเรื่องรายละเอียดคำขอย้ายห้อง
 * detailSub คือ ป้ายแสดงคำอธิบายย่อยของรายละเอียด
 * applicantName คือ ป้ายแสดงชื่อผู้ยื่นคำขอย้ายห้อง
 * lblMoveId คือ ป้ายแสดงรหัสคำขอย้ายห้อง
 * lblStatus คือ ป้ายแสดงสถานะปัจจุบันของคำขอ
 * lblPhone คือ ป้ายแสดงเบอร์โทรศัพท์ของผู้ยื่นคำขอ
 * lblOldRoom คือ ป้ายแสดงหมายเลขห้องพักเดิม
 * lblNewRoom คือ ป้ายแสดงหมายเลขห้องพักใหม่ที่ต้องการย้ายไป
 * lblDate คือ ป้ายแสดงวันที่ยื่นคำขอย้ายห้อง
 * txtReason คือ กล่องข้อความแสดงเหตุผลการขอย้ายห้อง
 * btnApprove คือ ปุ่มกดสำหรับอนุมัติคำขอย้ายห้อง
 * btnReject คือ ปุ่มกดสำหรับปฏิเสธคำขอย้ายห้อง
 * currentMove คือ ข้อมูลคำขอย้ายห้องที่กำลังเลือกดูรายละเอียดอยู่ในปัจจุบัน
 */
public class AdminRoomChangeUI extends JPanel {

    // ===== representation =====
    // Abstraction Function:
    // AF(logic, table, tableModel, currentMove) คือ หน้าจอสำหรับแสดงรายการคำขอย้ายห้องพัก ตรวจสอบห้องเดิม-ห้องใหม่ และบันทึกผลการพิจารณา
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

    private final AdminRoomChangeLogic logic = new AdminRoomChangeLogic();

    JComboBox<String> statusFilter;
    JLabel lblCount;
    JTable table;
    DefaultTableModel tableModel;
    JLabel detailTitle, detailSub, applicantName;
    JLabel lblMoveId, lblStatus, lblPhone, lblOldRoom, lblNewRoom, lblDate;
    JTextArea txtReason;
    JButton btnApprove, btnReject;

    private AdminRoomChangeLogic.MoveModel currentMove = null;

    /**
     * คอนสตรักเตอร์สร้างหน้าจอจัดการคำขอย้ายห้องพักสำหรับผู้ดูแลระบบ
     */
    public AdminRoomChangeUI() {
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
     * สร้างแถบเครื่องมือด้านบน ประกอบด้วยตัวกรองสถานะและป้ายแสดงจำนวนรายการ
     */
    private void createToolbar() {
        statusFilter = styledCombo(new String[]{"สถานะทั้งหมด", "รอพิจารณา", "อนุมัติ", "ปฏิเสธ"}, WHITE);
        statusFilter.setBounds(20, 20, 200, 34);
        add(statusFilter);

        lblCount = AdminUI.label("แสดง 0 รายการ", Font.PLAIN, 12, TEXT_GRAY);
        lblCount.setBounds(235, 20, 320, 34);
        lblCount.setVerticalAlignment(SwingConstants.CENTER);
        add(lblCount);
    }

    /**
     * สร้างและจัดรูปแบบตารางแสดงรายการคำขอย้ายห้องพัก
     * 
     * @return JScrollPane ที่ครอบตารางคำขอย้ายห้อง
     */
    private JScrollPane createTable() {
        String[] columns = {"เลขคำขอ", "ชื่อผู้เช่า", "การย้ายห้อง", "วันที่ยื่น", "สถานะ"};
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
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);
        applyColumnWidths();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        scroll.setBounds(20, 70, 540, 510);
        return scroll;
    }

    /**
     * กำหนดความกว้างของแต่ละคอลัมน์ในตารางคำขอย้ายห้องพักให้เหมาะสม ไม่ตกหล่น
     */
    private void applyColumnWidths() {
        // ลดความกว้างช่องก่อนหน้า (เลขคำขอ 65, ชื่อผู้เช่า 110) เพื่อให้ช่องการย้ายห้องกว้าง 180 แสดงข้อความได้ครบถ้วนไม่ตกหล่น
        int[] widths = { 65, 110, 180, 95, 90 };
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /**
     * สร้างพาเนลแสดงรายละเอียดคำขอย้ายห้องพักที่เลือกและปุ่มอนุมัติ/ปฏิเสธ
     * 
     * @return JPanel แสดงรายละเอียดคำขอย้ายห้อง
     */
    private JPanel createDetailPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(WHITE);
        p.setBounds(580, 0, 330, 640);
        p.setBorder(new MatteBorder(0, 1, 0, 0, BORDER));

        detailTitle = AdminUI.label("รายละเอียดคำขอย้ายห้อง", Font.BOLD, 16, TEXT);
        detailTitle.setBounds(20, 20, 280, 24);
        p.add(detailTitle);

        detailSub = AdminUI.label("- • คำขอย้ายห้อง", Font.PLAIN, 11, TEXT_GRAY);
        detailSub.setBounds(20, 46, 280, 16);
        p.add(detailSub);

        applicantName = AdminUI.label("-", Font.BOLD, 13, TEXT);
        applicantName.setBounds(20, 80, 280, 22);
        p.add(applicantName);

        int y = 115;
        lblMoveId   = detailRow(p, "รหัสคำขอ", "-", y);
        lblStatus   = detailRow(p, "สถานะ", "-", y += 24);
        lblPhone    = detailRow(p, "เบอร์โทรศัพท์", "-", y += 24);
        lblOldRoom  = detailRow(p, "ห้องเดิม", "-", y += 24);
        lblNewRoom  = detailRow(p, "ห้องใหม่ที่ต้องการ", "-", y += 24);
        lblDate     = detailRow(p, "วันที่ยื่นเรื่อง", "-", y += 24);

        JLabel lReason = AdminUI.label("เหตุผลในการขอย้ายห้อง", Font.PLAIN, 11, TEXT_GRAY);
        lReason.setBounds(20, y + 30, 276, 18);
        p.add(lReason);

        txtReason = new JTextArea();
        txtReason.setFont(AdminUI.font(Font.PLAIN, 12));
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setEditable(false);
        txtReason.setBackground(AdminUI.ITEM_BG);

        JScrollPane areaScroll = new JScrollPane(txtReason);
        areaScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        areaScroll.setBounds(20, y + 52, 276, 100);
        p.add(areaScroll);

        btnApprove = new AdminRoomUI.FlatButton("อนุมัติการย้ายห้อง", true);
        btnApprove.setBounds(20, 480, 276, 40);
        p.add(btnApprove);

        btnReject = new AdminRoomUI.FlatButton("ปฏิเสธคำขอ", false);
        btnReject.setForeground(AdminUI.RED);
        btnReject.setBounds(20, 530, 276, 40);
        p.add(btnReject);

        return p;
    }

    /**
     * โหลดข้อมูลคำขอย้ายห้องใหม่และอัปเดตการแสดงผลในตารางตามตัวกรองสถานะ
     */
    public void refreshData() {
        String filter = statusFilter != null ? (String) statusFilter.getSelectedItem() : "สถานะทั้งหมด";
        List<AdminRoomChangeLogic.MoveModel> all = logic.getAllMoves();
        List<AdminRoomChangeLogic.MoveModel> filtered = new java.util.ArrayList<>();

        for (AdminRoomChangeLogic.MoveModel m : all) {
            if (filter == null || "สถานะทั้งหมด".equals(filter) || m.status.equalsIgnoreCase(filter) || ("รอพิจารณา".equals(filter) && m.status.contains("รอ"))) {
                filtered.add(m);
            }
        }

        tableModel.setRowCount(0);
        for (AdminRoomChangeLogic.MoveModel m : filtered) {
            tableModel.addRow(new Object[]{m.id, m.name, "จากห้อง " + m.oldRoomNum + " ย้ายไป " + m.newRoomNum, m.date, m.status});
        }
        applyColumnWidths();

        if (lblCount != null) {
            lblCount.setText("แสดง " + filtered.size() + " รายการ จากทั้งหมด " + all.size() + " รายการ");
        }

        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        } else {
            clearDetailPanel();
        }
    }

    /**
     * ล้างข้อมูลในพาเนลแสดงรายละเอียดคำขอย้ายห้องให้เป็นค่าเริ่มต้น
     */
    private void clearDetailPanel() {
        currentMove = null;
        if (applicantName != null) applicantName.setText("-");
        if (detailSub != null) detailSub.setText("- • คำขอย้ายห้อง");
        if (lblMoveId != null) lblMoveId.setText("-");
        if (lblStatus != null) lblStatus.setText("-");
        if (lblPhone != null) lblPhone.setText("-");
        if (lblOldRoom != null) lblOldRoom.setText("-");
        if (lblNewRoom != null) lblNewRoom.setText("-");
        if (lblDate != null) lblDate.setText("-");
        if (txtReason != null) txtReason.setText("");
        if (btnApprove != null) btnApprove.setEnabled(false);
        if (btnReject != null) btnReject.setEnabled(false);
    }

    /**
     * ผูก Listener การเลือกแถวในตารางเพื่อแสดงรายละเอียดคำขอย้ายห้องในพาเนลด้านขวา
     */
    private void bindSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                String moveId = String.valueOf(tableModel.getValueAt(row, 0));
                for (AdminRoomChangeLogic.MoveModel m : logic.getAllMoves()) {
                    if (m.id.equalsIgnoreCase(moveId)) {
                        currentMove = m;
                        applicantName.setText(currentMove.name);
                        detailSub.setText(currentMove.id + " • ขอย้ายห้อง");
                        lblMoveId.setText(currentMove.id);
                        lblStatus.setText(currentMove.status);
                        lblPhone.setText(currentMove.phone);
                        lblOldRoom.setText("ห้อง " + currentMove.oldRoomNum);
                        lblNewRoom.setText("ห้อง " + currentMove.newRoomNum);
                        lblDate.setText(currentMove.date);
                        txtReason.setText(currentMove.reason);

                        boolean isPending = "รอพิจารณา".equalsIgnoreCase(currentMove.status) || "PENDING".equalsIgnoreCase(currentMove.status);
                        btnApprove.setEnabled(isPending);
                        btnReject.setEnabled(isPending);
                        break;
                    }
                }
            }
        });
    }

    /**
     * กำหนด Event Handlers สำหรับตัวกรองสถานะ ปุ่มอนุมัติ และปุ่มปฏิเสธคำขอย้ายห้อง
     */
    private void initActions() {
        if (statusFilter != null) {
            statusFilter.addActionListener(e -> refreshData());
        }

        // ปุ่มอนุมัติคำขอย้ายห้อง
        btnApprove.addActionListener(e -> {
            if (currentMove == null) {
                JOptionPane.showMessageDialog(this, "กรุณาเลือกรายการย้ายห้องจากตาราง");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "อนุมัติย้ายห้องคุณ " + currentMove.name + " (จากห้อง " + currentMove.oldRoomNum + " ย้ายไป " + currentMove.newRoomNum + ") ?",
                "ยืนยันการอนุมัติ",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (logic.approveMove(currentMove.id)) {
                    JOptionPane.showMessageDialog(this, "อนุมัติย้ายห้องและออกสัญญาใหม่เรียบร้อยแล้ว", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "ไม่สามารถอนุมัติได้ เนื่องจากห้องพักปลายทางมีผู้อาศัยอยู่แล้ว หรือห้องไม่ว่าง", "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ปุ่มปฏิเสธ
        btnReject.addActionListener(e -> {
            if (currentMove != null) {
                int confirm = JOptionPane.showConfirmDialog(
                    this, 
                    "ปฏิเสธคำขอย้ายห้องของ " + currentMove.name + " ?", 
                    "ยืนยันการปฏิเสธ", 
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION && logic.rejectMove(currentMove.id)) {
                    JOptionPane.showMessageDialog(this, "ปฏิเสธคำขอเรียบร้อยแล้ว", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            }
        });
    }

    /**
     * สร้างแถวแสดงข้อมูลรายละเอียดคู่หัวข้อและค่า
     * 
     * @param p พาเนลที่จะวางองค์ประกอบ
     * @param title หัวข้อรายละเอียด
     * @param value ค่ารายละเอียด
     * @param y ตำแหน่งแกน Y
     * @return JLabel แสดงค่ารายละเอียด
     */
    private static JLabel detailRow(JPanel p, String title, String value, int y) {
        JLabel t = AdminUI.label(title, Font.PLAIN, 11, TEXT_GRAY);
        t.setBounds(20, y, 120, 18); p.add(t);
        JLabel v = AdminUI.label(value, Font.BOLD, 11, TEXT);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        v.setBounds(120, y, 176, 18); p.add(v);
        return v;
    }

    static class HeaderRenderer extends UIComponents.HeaderRenderer {
    }

    /**
     * CellRenderer คือคลาสกำหนดการแสดงผลข้อมูลแต่ละเซลล์ในตารางคำขอย้ายห้อง
     */
    static class CellRenderer extends DefaultTableCellRenderer {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ คอมโพเนนต์เรนเดอร์เซลล์ตารางพร้อมป้ายสถานะ Badge
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * กำหนดการแสดงผลของแต่ละเซลล์ในตารางคำขอย้ายห้องพักพร้อม Badge แสดงสถานะ
         */
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Color rowBg = sel ? ROW_SELECT : WHITE;
            if (col == 4) {
                String s = String.valueOf(v);
                Color bg = new Color(254, 243, 199), fg = new Color(180, 83, 9);
                if (s.contains("APPROVED") || s.contains("อนุมัติ")) {
                    bg = new Color(209, 250, 229);
                    fg = new Color(4, 120, 87);
                } else if (s.contains("REJECTED") || s.contains("ปฏิเสธ")) {
                    bg = new Color(254, 226, 226);
                    fg = AdminUI.RED;
                }
                AdminUI.Badge badge = new AdminUI.Badge(s, bg, fg);
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
                cell.setBackground(rowBg); cell.add(badge);
                return cell;
            }
            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setBackground(rowBg); setBorder(new EmptyBorder(0, 8, 0, 8));
            setFont(AdminUI.font(col == 0 ? Font.BOLD : Font.PLAIN, 11));
            setForeground(col == 0 ? PRIMARY : TEXT);
            return this;
        }
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