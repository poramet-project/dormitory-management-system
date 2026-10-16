package ui;

import logic.AdminReportLogic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AdminReportUI คือคลาสหน้าจอส่วนแสดงผลสำหรับผู้ดูแลระบบในการจัดการคำร้องเรียนและงานแจ้งซ่อม
 * logic คือ อ็อบเจกต์ตรรกะสำหรับจัดการคำร้องเรียนและงานแจ้งซ่อม
 * searchField คือ ช่องสำหรับค้นหาข้อมูลคำร้องเรียน
 * statusFilter คือ คอมโบ้บ็อกซ์สำหรับเลือกกรองตามสถานะ
 * typeFilter คือ คอมโบ้บ็อกซ์สำหรับเลือกกรองตามหมวดหมู่งานซ่อม
 * table คือ ตารางแสดงรายการคำร้องเรียนและแจ้งซ่อม
 * tableModel คือ โมเดลข้อมูลของตารางคำร้องเรียน
 * detailTitle คือ ป้ายแสดงหัวเรื่องรายละเอียดคำร้อง
 * detailSub คือ ป้ายแสดงคำอธิบายย่อยของรายละเอียด
 * lblName คือ ป้ายแสดงชื่อผู้แจ้ง
 * lblPhone คือ ป้ายแสดงเบอร์โทรศัพท์ผู้แจ้ง
 * lblRoom คือ ป้ายแสดงหมายเลขห้องพักที่แจ้ง
 * lblDate คือ ป้ายแสดงวันที่แจ้ง
 * lblTime คือ ป้ายแสดงเวลานัดหมาย
 * lblStatus คือ ป้ายแสดงสถานะปัจจุบัน
 * lblCount คือ ป้ายแสดงจำนวนรายการคำร้องที่แสดงผล
 * lblCategory คือ ป้ายแสดงหมวดหมู่งานซ่อม
 * lblTopic คือ ป้ายแสดงหัวข้อเรื่อง
 * txtDescription คือ กล่องข้อความแสดงรายละเอียดปัญหา
 * btnAccept คือ ปุ่มกดสำหรับดำเนินการรับเรื่องหรือปรับปรุงสถานะงานซ่อม
 */
public class AdminReportUI extends JPanel {

    // ===== representation =====
    // Abstraction Function:
    // AF(logic, table, tableModel) คือ หน้าจอสำหรับแสดงรายการคำร้องเรียน ติดตามสถานะงานซ่อม และปรับปรุงความคืบหน้าการซ่อมบำรุง
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

    private final AdminReportLogic logic = new AdminReportLogic();

    JTextField searchField;
    JComboBox<String> statusFilter, typeFilter;
    JTable table;
    DefaultTableModel tableModel;
    JLabel detailTitle, detailSub, lblName, lblPhone, lblRoom, lblDate, lblTime, lblStatus, lblCount, lblCategory,
            lblTopic;
    JTextArea txtDescription;
    JButton btnAccept;

    /**
     * คอนสตรักเตอร์สร้างหน้าจอจัดการคำร้องเรียนและงานแจ้งซ่อมสำหรับผู้ดูแลระบบ
     */
    public AdminReportUI() {
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
     * สร้างแถบเครื่องมือด้านบน ประกอบด้วยตัวกรองหมวดหมู่ ตัวกรองสถานะ และป้ายแสดงจำนวนรายการ
     */
    private void createToolbar() {
        typeFilter = styledCombo(new String[] { "หมวดหมู่ทั้งหมด", "ระบบประปา", "ระบบไฟฟ้า", "เครื่องปรับอากาศ",
                "เฟอร์นิเจอร์", "อื่นๆ" });
        typeFilter.setBounds(20, 20, 160, 34);
        add(typeFilter);

        statusFilter = styledCombo(new String[] { "สถานะทั้งหมด", "รอดำเนินการ", "ช่างกำลังเข้าตรวจสอบ", "เสร็จสิ้น" });
        statusFilter.setBounds(190, 20, 170, 34);
        add(statusFilter);

        lblCount = AdminUI.label("แสดง 0 รายการ", Font.PLAIN, 12, TEXT_GRAY);
        lblCount.setBounds(370, 20, 190, 34);
        lblCount.setVerticalAlignment(SwingConstants.CENTER);
        add(lblCount);
    }

    /**
     * สร้างและจัดรูปแบบตารางแสดงรายการคำร้องเรียนและแจ้งซ่อม
     * 
     * @return JScrollPane ที่ครอบตารางคำร้องเรียน
     */
    private JScrollPane createTable() {
        String[] columns = { "เลขที่", "ผู้แจ้ง", "ห้อง", "หมวดหมู่", "วันนัดซ้อม", "สถานะ" };
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
     * สร้างพาเนลแสดงรายละเอียดคำร้องเรียนและปุ่มสำหรับรับเรื่องเพื่อส่งช่าง
     * 
     * @return JPanel แสดงรายละเอียดคำร้อง
     */
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

        lblName = detailRow(p, "ผู้ยื่นคำขอ", "-", 90);
        lblPhone = detailRow(p, "เบอร์โทรศัพท์", "-", 114);
        lblRoom = detailRow(p, "ห้องพัก", "-", 138);
        lblCategory = detailRow(p, "หมวดหมู่", "-", 162);
        lblTopic = detailRow(p, "เรื่อง", "-", 186);
        lblDate = detailRow(p, "วันนัดซ้อม", "-", 210);
        lblTime = detailRow(p, "เวลานัดซ้อม", "-", 234);
        lblStatus = detailRow(p, "สถานะ", "-", 258);

        p.add(fieldLabel("รายละเอียดปัญหา", 288));
        txtDescription = textArea(false);
        p.add(areaScroll(txtDescription, 310, 160));

        p.add(divider(494));

        JLabel lblNotice = AdminUI.label("* หมายเหตุ: ผู้เช่าจะเป็นผู้กดยืนยันเมื่อซ่อมเสร็จสิ้น", Font.PLAIN, 11,
                TEXT_GRAY);
        lblNotice.setBounds(20, 502, 276, 18);
        p.add(lblNotice);

        btnAccept = new AdminRoomUI.FlatButton("รับเรื่อง (ส่งช่างเข้าตรวจสอบ)", true);
        btnAccept.setBounds(20, 528, 276, 42);
        p.add(btnAccept);
        return p;
    }

    /**
     * โหลดข้อมูลคำร้องเรียนใหม่และอัปเดตการแสดงผลในตารางตามตัวกรองหมวดหมู่และสถานะ
     */
    public void refreshData() {
        tableModel.setRowCount(0);
        List<AdminReportLogic.ProblemModel> all = logic.getAllProblems();

        String selCat = (typeFilter != null && typeFilter.getSelectedItem() != null)
                ? (String) typeFilter.getSelectedItem()
                : "หมวดหมู่ทั้งหมด";
        String selStatus = (statusFilter != null && statusFilter.getSelectedItem() != null)
                ? (String) statusFilter.getSelectedItem()
                : "สถานะทั้งหมด";

        java.util.List<AdminReportLogic.ProblemModel> filtered = new java.util.ArrayList<>();
        for (AdminReportLogic.ProblemModel p : all) {
            boolean matchCat = "หมวดหมู่ทั้งหมด".equals(selCat);
            if (!matchCat) {
                if ("อื่นๆ".equals(selCat)) {
                    matchCat = p.category == null || p.category.contains("อื่น")
                            || (!p.category.contains("ประปา") && !p.category.contains("ไฟฟ้า")
                                     && !p.category.contains("ปรับอากาศ") && !p.category.contains("แอร์")
                                    && !p.category.contains("เฟอร์นิเจอร์"));
                } else if (p.category != null) {
                    String cleanKey = selCat.replace("ระบบ", "").replace("เครื่อง", "").trim();
                    matchCat = p.category.contains(cleanKey) || selCat.contains(p.category);
                }
            }

            boolean matchStatus = "สถานะทั้งหมด".equals(selStatus) || selStatus.equalsIgnoreCase(p.status);

            if (matchCat && matchStatus) {
                filtered.add(p);
                tableModel.addRow(new Object[] { p.id, p.name, p.room, p.category, p.date, p.status });
            }
        }

        int[] widths = { 46, 105, 45, 110, 105, 129 };
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
     * ล้างข้อมูลในพาเนลแสดงรายละเอียดคำร้องให้เป็นค่าเริ่มต้น
     */
    private void clearDetailPanel() {
        detailSub.setText("- • แจ้งซ่อม");
        lblName.setText("-");
        lblPhone.setText("-");
        lblRoom.setText("-");
        lblCategory.setText("-");
        lblTopic.setText("-");
        lblDate.setText("-");
        lblTime.setText("-");
        lblStatus.setText("-");
        txtDescription.setText("");
        if (btnAccept != null)
            btnAccept.setEnabled(false);
    }

    /**
     * ผูก Listener การเลือกแถวในตารางเพื่อแสดงรายละเอียดคำร้องที่เลือกในพาเนลด้านขวา
     */
    private void bindSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                String complaintId = String.valueOf(tableModel.getValueAt(row, 0));
                for (AdminReportLogic.ProblemModel p : logic.getAllProblems()) {
                    if (p.id.equals(complaintId)) {
                        detailSub.setText(p.id + " • " + p.topic);
                        lblName.setText(p.name);
                        lblPhone.setText(p.phone);
                        lblRoom.setText(p.room);
                        lblCategory.setText(p.category);
                        lblTopic.setText(p.topic);
                        lblDate.setText(p.date);
                        lblTime.setText(p.time);
                        lblStatus.setText(p.status);
                        txtDescription.setText(p.desc);

                        boolean isPending = "รอดำเนินการ".equalsIgnoreCase(p.status)
                                || "PENDING".equalsIgnoreCase(p.status);
                        if (btnAccept != null)
                            btnAccept.setEnabled(isPending);
                        break;
                    }
                }
            }
        });
    }

    /**
     * กำหนด Event Handlers สำหรับตัวกรองหมวดหมู่ ตัวกรองสถานะ และปุ่มรับเรื่องส่งช่าง
     */
    private void initActions() {
        typeFilter.addActionListener(e -> refreshData());
        statusFilter.addActionListener(e -> refreshData());

        btnAccept.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String complaintId = String.valueOf(tableModel.getValueAt(row, 0));
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "ยืนยันการรับเรื่องแจ้งซ่อม " + complaintId + " เพื่อส่งช่างเข้าตรวจสอบ?",
                        "ยืนยันการรับเรื่อง",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION && logic.acceptProblem(complaintId)) {
                    JOptionPane.showMessageDialog(this,
                            "รับเรื่องและปรับสถานะเป็น 'ช่างกำลังเข้าตรวจสอบ' เรียบร้อยแล้ว\n(สถานะจะเปลี่ยนเป็น 'เสร็จสิ้น' เมื่อผู้เช่ายืนยัน)",
                            "สำเร็จ",
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
        label.setBounds(20, y, 130, 20);
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
     * HeaderRenderer คือคลาสกำหนดการแสดงผลส่วนหัวของตารางคำร้องเรียน
     */
    static class HeaderRenderer extends DefaultTableCellRenderer {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ คอมโพเนนต์เรนเดอร์ส่วนหัวของตารางคำร้องเรียน
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * กำหนดการแสดงผลของส่วนหัวตารางคำร้องเรียน
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
     * CellRenderer คือคลาสกำหนดการแสดงผลข้อมูลแต่ละเซลล์ในตารางคำร้องเรียน
     */
    static class CellRenderer extends DefaultTableCellRenderer {
        // ===== representation =====
        // Abstraction Function:
        // AF() คือ คอมโพเนนต์เรนเดอร์เซลล์ตารางพร้อมป้ายสถานะ Badge
        // RI
        // - ไม่มีตัวแปรสถานะภายใน

        /**
         * กำหนดการแสดงผลของแต่ละเซลล์ในตารางคำร้องเรียนพร้อม Badge แสดงสถานะ
         */
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row,
                int col) {
            Color rowBg = selected ? ROW_SELECT : WHITE;
            if (col == 5) {
                String status = String.valueOf(value);
                Color bg = new Color(254, 243, 199), fg = new Color(180, 83, 9);
                if (status.equals("อนุมัติ") || status.equals("เสร็จสิ้น")) {
                    bg = new Color(209, 250, 229);
                    fg = new Color(4, 120, 87);
                } else if (status.equals("ปฏิเสธ")) {
                    bg = new Color(254, 226, 226);
                    fg = AdminUI.RED;
                } else if (status.equals("ช่างกำลังเข้าตรวจสอบ")) {
                    bg = AdminUI.PRIMARY_BG;
                    fg = PRIMARY;
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