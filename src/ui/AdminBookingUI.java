package ui;

import logic.AdminBookingLogic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AdminBookingUI คือคลาสหน้าจอส่วนแสดงผลสำหรับผู้ดูแลระบบในการพิจารณาคำขอจองห้องพัก
 * logic คือ อ็อบเจกต์ตรรกะสำหรับจัดการข้อมูลคำขอจองห้องพัก
 * searchField คือ ช่องสำหรับค้นหาคำขอจองห้องพัก
 * statusFilter คือ คอมโบ้บ็อกซ์สำหรับเลือกกรองตามสถานะคำขอ
 * table คือ ตารางแสดงรายการคำขอจองห้องพัก
 * tableModel คือ โมเดลข้อมูลของตารางคำขอจองห้องพัก
 * detailTitle คือ ป้ายแสดงหัวเรื่องรายละเอียดคำขอที่เลือก
 * detailSub คือ ป้ายแสดงคำอธิบายย่อยของรายละเอียด
 * lblName คือ ป้ายแสดงชื่อผู้ขอจอง
 * lblPhone คือ ป้ายแสดงเบอร์โทรศัพท์ของผู้ขอจอง
 * lblRoom คือ ป้ายแสดงหมายเลขห้องพักที่ขอจอง
 * lblDate คือ ป้ายแสดงวันที่ยื่นคำขอจอง
 * lblStatus คือ ป้ายแสดงสถานะปัจจุบันของคำขอ
 * lblCount คือ ป้ายแสดงจำนวนรายการคำขอที่แสดงผล
 * txtDescription คือ กล่องข้อความแสดงรายละเอียดความต้องการเพิ่มเติม
 * btnApprove คือ ปุ่มกดสำหรับอนุมัติคำขอจองห้อง
 * btnReject คือ ปุ่มกดสำหรับปฏิเสธคำขอจองห้อง
 * currentBooking คือ ข้อมูลคำขอจองห้องที่กำลังเลือกดูรายละเอียดอยู่ในปัจจุบัน
 */
public class AdminBookingUI extends JPanel {

    // ===== representation =====
    // Abstraction Function:
    // AF(logic, table, tableModel, currentBooking) คือ หน้าจอสำหรับแสดงรายการคำขอจองห้องพัก ตรวจสอบรายละเอียด และบันทึกผลการพิจารณาคำขอ
    // RI
    // 1. logic ต้องไม่เป็น null
    // 2. table และ tableModel ต้องไม่เป็น null

    static final Color PRIMARY = AdminUI.PRIMARY;
    static final Color BG = AdminUI.BG;
    static final Color WHITE = AdminUI.WHITE;
    static final Color BORDER = AdminUI.BORDER;
    static final Color TEXT = AdminUI.TEXT;
    static final Color TEXT_GRAY = AdminUI.TEXT_GRAY;
    static final Color ROW_SELECT = AdminRoomUI.ROW_SELECT;

    private final AdminBookingLogic logic = new AdminBookingLogic();

    JTextField searchField;
    JComboBox<String> statusFilter;
    JTable table;
    DefaultTableModel tableModel;
    JLabel detailTitle, detailSub, lblName, lblPhone, lblRoom, lblDate, lblStatus, lblCount;
    JTextArea txtDescription;
    JButton btnApprove, btnReject;

    private AdminBookingLogic.BookingModel currentBooking = null;

    /**
     * คอนสตรักเตอร์สร้างหน้าจอจัดการคำขอจองห้องพักสำหรับผู้ดูแลระบบ
     */
    public AdminBookingUI() {
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
        statusFilter = styledCombo(new String[] { "สถานะทั้งหมด", "รอพิจารณา", "อนุมัติ", "ปฏิเสธ" });
        statusFilter.setBounds(20, 20, 200, 34);
        add(statusFilter);

        lblCount = AdminUI.label("แสดง 0 รายการ", Font.PLAIN, 12, TEXT_GRAY);
        lblCount.setBounds(235, 20, 320, 34);
        lblCount.setVerticalAlignment(SwingConstants.CENTER);
        add(lblCount);
    }

    /**
     * สร้างและจัดรูปแบบตารางแสดงรายการคำขอจองห้องพัก
     * 
     * @return JScrollPane ที่ครอบตารางคำขอจองห้อง
     */
    private JScrollPane createTable() {
        String[] columns = { "เลขคำขอ", "ชื่อผู้ยื่น", "ห้อง", "วันที่ยื่น", "สถานะ" };
        tableModel = new DefaultTableModel(new Object[0][0], columns) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
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
        scroll.setBounds(20, 70, 540, 510);
        return scroll;
    }

    /**
     * สร้างพาเนลแสดงรายละเอียดคำขอจองห้องพักที่เลือกและปุ่มอนุมัติ/ปฏิเสธ
     * 
     * @return JPanel แสดงรายละเอียดคำขอ
     */
    private JPanel createDetailPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(WHITE);
        p.setBounds(580, 0, 330, 640);
        p.setBorder(new MatteBorder(0, 1, 0, 0, BORDER));

        detailTitle = AdminUI.label("รายละเอียดคำขอ", Font.BOLD, 16, TEXT);
        detailTitle.setBounds(20, 20, 276, 24);
        p.add(detailTitle);

        detailSub = AdminUI.label("- • จองห้อง", Font.PLAIN, 11, TEXT_GRAY);
        detailSub.setBounds(20, 46, 276, 18);
        p.add(detailSub);
        p.add(divider(78));

        lblName = detailRow(p, "ผู้ยื่นคำขอ", "-", 94);
        lblPhone = detailRow(p, "เบอร์โทรศัพท์", "-", 122);
        lblRoom = detailRow(p, "ห้องพัก", "-", 150);
        lblDate = detailRow(p, "วันที่ยื่นคำขอ", "-", 178);
        lblStatus = detailRow(p, "สถานะ", "-", 206);

        p.add(fieldLabel("ห้องที่ขอจอง", 242));
        txtDescription = textArea(false);
        p.add(areaScroll(txtDescription, 264, 110));

        p.add(divider(398));
        btnReject = new AdminRoomUI.FlatButton("ปฏิเสธคำขอ", false);
        btnReject.setForeground(AdminUI.RED);
        btnReject.setBounds(20, 420, 132, 40);
        p.add(btnReject);

        btnApprove = new AdminRoomUI.FlatButton("อนุมัติคำขอ", true);
        btnApprove.setBounds(162, 420, 134, 40);
        p.add(btnApprove);
        return p;
    }

    /**
     * โหลดข้อมูลคำขอจองห้องพักใหม่และอัปเดตการแสดงผลในตารางตามตัวกรอง
     */
    public void refreshData() {
        tableModel.setRowCount(0);
        List<AdminBookingLogic.BookingModel> all = logic.getAllBookings();
        String filter = (statusFilter != null && statusFilter.getSelectedItem() != null)
                ? (String) statusFilter.getSelectedItem() : "สถานะทั้งหมด";

        java.util.List<AdminBookingLogic.BookingModel> filtered = new java.util.ArrayList<>();
        for (AdminBookingLogic.BookingModel b : all) {
            if ("สถานะทั้งหมด".equals(filter) || filter.equalsIgnoreCase(b.status)) {
                filtered.add(b);
                tableModel.addRow(new Object[] { b.id, b.name, b.roomNumber, b.date, b.status });
            }
        }

        int[] widths = { 68, 135, 112, 100, 125 };
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

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
     * ล้างข้อมูลในพาเนลแสดงรายละเอียดให้เป็นค่าเริ่มต้น
     */
    private void clearDetailPanel() {
        currentBooking = null;
        detailSub.setText("- • จองห้อง");
        lblName.setText("-");
        lblPhone.setText("-");
        lblRoom.setText("-");
        lblDate.setText("-");
        lblStatus.setText("-");
        txtDescription.setText("");
        if (btnApprove != null) btnApprove.setEnabled(false);
        if (btnReject != null) btnReject.setEnabled(false);
    }

    /**
     * ผูก Listener การเลือกแถวในตารางเพื่อแสดงรายละเอียดคำขอที่เลือกในพาเนลด้านขวา
     */
    private void bindSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                String bookingId = String.valueOf(tableModel.getValueAt(row, 0));
                for (AdminBookingLogic.BookingModel b : logic.getAllBookings()) {
                    if (b.id.equals(bookingId)) {
                        currentBooking = b;
                        detailSub.setText(currentBooking.id + " • " + currentBooking.type);
                        lblName.setText(currentBooking.name);
                        lblPhone.setText(currentBooking.phone);
                        lblRoom.setText(currentBooking.roomNumber);
                        lblDate.setText(currentBooking.date);
                        lblStatus.setText(currentBooking.status);
                        txtDescription.setText(currentBooking.roomDetail);

                        boolean isPending = "รอพิจารณา".equalsIgnoreCase(currentBooking.status) || "PENDING".equalsIgnoreCase(currentBooking.status);
                        btnApprove.setEnabled(isPending);
                        btnReject.setEnabled(isPending);
                        break;
                    }
                }
            }
        });
    }

    /**
     * กำหนด Event Handlers สำหรับตัวกรองสถานะ ปุ่มอนุมัติ และปุ่มปฏิเสธคำขอ
     */
    private void initActions() {
        statusFilter.addActionListener(e -> refreshData());

        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String bookingId = String.valueOf(tableModel.getValueAt(row, 0));
                if (logic.approveBooking(bookingId)) {
                    JOptionPane.showMessageDialog(this,
                            "อนุมัติคำขอจอง ปรับบทบาทเป็น TENANT และสร้างสัญญาเช่า (เงินประกัน x 2) เรียบร้อยแล้ว",
                            "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            }
        });

        btnReject.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String bookingId = String.valueOf(tableModel.getValueAt(row, 0));
                if (logic.rejectBooking(bookingId)) {
                    JOptionPane.showMessageDialog(this, "ปฏิเสธคำขอเรียบร้อยแล้ว", "สำเร็จ",
                            JOptionPane.INFORMATION_MESSAGE);
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
     * @param text ค่ารายละเอียด
     * @param y ตำแหน่งแกน Y
     * @return JLabel แสดงค่ารายละเอียด
     */
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

    /**
     * สร้างป้ายข้อความกำกับฟิลด์ข้อมูล
     * 
     * @param title ข้อความหัวข้อ
     * @param y ตำแหน่งแกน Y
     * @return JLabel ป้ายกำกับ
     */
    private static JLabel fieldLabel(String title, int y) {
        JLabel label = AdminUI.label(title, Font.PLAIN, 11, TEXT_GRAY);
        label.setBounds(20, y, 276, 18);
        return label;
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
     * สร้างกล่องข้อความหลายบรรทัด
     * 
     * @param editable กำหนดว่าสามารถแก้ไขข้อความได้หรือไม่
     * @return JTextArea ที่จัดรูปแบบแล้ว
     */
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

    /**
     * สร้าง JScrollPane ครอบกล่องข้อความพร้อมกำหนดพิกัดและความสูง
     * 
     * @param area กล่องข้อความ
     * @param y ตำแหน่งแกน Y
     * @param height ความสูง
     * @return JScrollPane
     */
    private static JScrollPane areaScroll(JTextArea area, int y, int height) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setBounds(20, y, 276, height);
        return scroll;
    }

    /**
     * สร้างคอมโบ้บ็อกซ์พร้อมตกแต่งรูปแบบตามธีมระบบ
     * 
     * @param items รายการตัวเลือก
     * @return JComboBox ที่จัดรูปแบบแล้ว
     */
    private static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AdminUI.font(Font.PLAIN, 12));
        cb.setForeground(TEXT);
        cb.setBackground(WHITE);
        cb.setBorder(BorderFactory.createLineBorder(BORDER));
        cb.setFocusable(false);
        cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new AdminRoomUI.ChevronButton();
            }
        });
        return cb;
    }

    /**
     * HeaderRenderer คือคลาสกำหนดการแสดงผลส่วนหัวของตารางคำขอจองห้อง
     */
    static class HeaderRenderer extends DefaultTableCellRenderer {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ คอมโพเนนต์เรนเดอร์ส่วนหัวของตารางให้มีสีและฟอนต์ตามธีมระบบ
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * กำหนดการแสดงผลของส่วนหัวตารางคำขอจองห้องพัก
         */
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row,
                int col) {
            super.getTableCellRendererComponent(t, value, selected, focus, row, col);
            setFont(AdminUI.font(Font.PLAIN, 11));
            setForeground(TEXT_GRAY);
            setBackground(WHITE);
            setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(0, 8, 0, 8)));
            return this;
        }
    }

    /**
     * CellRenderer คือคลาสกำหนดการแสดงผลข้อมูลแต่ละเซลล์ในตารางคำขอจองห้อง
     */
    static class CellRenderer extends DefaultTableCellRenderer {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ คอมโพเนนต์เรนเดอร์เซลล์ตารางพร้อมป้ายสถานะ Badge
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * กำหนดการแสดงผลของแต่ละเซลล์ในตารางคำขอจองห้องพักพร้อม Badge แสดงสถานะ
         */
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row,
                int col) {
            Color rowBg = selected ? ROW_SELECT : WHITE;
            if (col == 4) {
                String status = String.valueOf(value);
                Color bg = new Color(254, 243, 199), fg = new Color(180, 83, 9);
                if (status.equals("อนุมัติ") || status.equals("เสร็จสิ้น")) {
                    bg = new Color(209, 250, 229);
                    fg = new Color(4, 120, 87);
                } else if (status.equals("ปฏิเสธ")) {
                    bg = new Color(254, 226, 226);
                    fg = AdminUI.RED;
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