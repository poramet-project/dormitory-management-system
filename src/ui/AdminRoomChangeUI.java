package ui;

import logic.AdminRoomChangeLogic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminRoomChangeUI extends JPanel {

    static final Color PRIMARY    = AdminUI.PRIMARY;
    static final Color BG         = AdminUI.BG;
    static final Color WHITE      = AdminUI.WHITE;
    static final Color BORDER     = AdminUI.BORDER;
    static final Color TEXT       = AdminUI.TEXT;
    static final Color TEXT_GRAY  = AdminUI.TEXT_GRAY;
    static final Color ROW_SELECT = AdminRoomUI.ROW_SELECT;

    private final AdminRoomChangeLogic logic = new AdminRoomChangeLogic();

    JTable table;
    DefaultTableModel tableModel;
    JLabel detailTitle, detailSub, applicantName;
    JLabel lblMoveId, lblStatus, lblPhone, lblOldRoom, lblNewRoom, lblDate;
    JTextArea txtReason;
    JButton btnApprove, btnReject;

    private AdminRoomChangeLogic.MoveModel currentMove = null;

    public AdminRoomChangeUI() {
        setLayout(null);
        setBackground(BG);

        add(createTable());
        add(createDetailPanel());

        bindSelection();
        initActions();
        refreshData();
    }

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

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        scroll.setBounds(20, 20, 540, 550);
        return scroll;
    }

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

    public void refreshData() {
        tableModel.setDataVector(logic.getTableRows(), new String[]{"เลขคำขอ", "ชื่อผู้เช่า", "การย้ายห้อง", "วันที่ยื่น", "สถานะ"});
        if (table.getRowCount() > 0) table.setRowSelectionInterval(0, 0);
    }

    private void bindSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                List<AdminRoomChangeLogic.MoveModel> list = logic.getAllMoves();
                if (row < list.size()) {
                    currentMove = list.get(row);
                    applicantName.setText(currentMove.name);
                    detailSub.setText(currentMove.id + " • ขอย้ายห้อง");
                    lblMoveId.setText(currentMove.id);
                    lblStatus.setText(currentMove.status);
                    lblPhone.setText(currentMove.phone);
                    lblOldRoom.setText("ห้อง " + currentMove.oldRoomNum);
                    lblNewRoom.setText("ห้อง " + currentMove.newRoomNum);
                    lblDate.setText(currentMove.date);
                    txtReason.setText(currentMove.reason);
                }
            }
        });
    }

    private void initActions() {
        // ปุ่มอนุมัติคำขอย้ายห้อง
        btnApprove.addActionListener(e -> {
            if (currentMove == null) {
                JOptionPane.showMessageDialog(this, "กรุณาเลือกรายการย้ายห้องจากตาราง");
                return;
            }

            // Pop-up ยืนยันแบบสั้นกระชับ
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "อนุมัติย้ายห้องคุณ " + currentMove.name + " (ห้อง " + currentMove.oldRoomNum + " ➔ " + currentMove.newRoomNum + ") ?",
                "ยืนยันการอนุมัติ",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (logic.approveMove(currentMove.id)) {
                    // Pop-up แจ้งผลสำเร็จแบบสั้น
                    JOptionPane.showMessageDialog(this, "อนุมัติย้ายห้องและออกสัญญาใหม่เรียบร้อยแล้ว", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "เกิดข้อผิดพลาดในการทำรายการ", "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
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

    private static JLabel detailRow(JPanel p, String title, String value, int y) {
        JLabel t = AdminUI.label(title, Font.PLAIN, 11, TEXT_GRAY);
        t.setBounds(20, y, 120, 18); p.add(t);
        JLabel v = AdminUI.label(value, Font.BOLD, 11, TEXT);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        v.setBounds(120, y, 176, 18); p.add(v);
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
                boolean approved = s.contains("APPROVED") || s.contains("อนุมัติ");
                AdminUI.Badge badge = new AdminUI.Badge(s, approved ? new Color(209, 250, 229) : new Color(254, 226, 226), approved ? new Color(4, 120, 87) : new Color(220, 38, 38));
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
                cell.setBackground(rowBg); cell.add(badge);
                return cell;
            }
            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setBackground(rowBg); setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(AdminUI.font(col == 0 ? Font.BOLD : Font.PLAIN, 11));
            setForeground(col == 0 ? TEXT : (col == 2 ? PRIMARY : TEXT_GRAY));
            return this;
        }
    }
}