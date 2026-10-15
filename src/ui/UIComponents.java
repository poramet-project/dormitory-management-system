package ui;

import logic.ImageLoader;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * UIComponents คือคลาสศูนย์กลางสำหรับเก็บคอมโพเนนต์ส่วนติดต่อผู้ใช้ (Swing
 * Components) ที่ใช้งานร่วมกัน
 */
public final class UIComponents {

    // ===== representation =====
    // Abstraction Function:
    // AF() คือ คลังรวมคอมโพเนนต์ Swing
    // ที่ได้รับการตกแต่งให้สวยงามและใช้ซ้ำได้ทั่วทั้งระบบ
    // RI
    // - ไม่มีตัวแปรสถานะภายใน

    private UIComponents() {
    }

    /**
     * กำหนดการวาดภาพกราฟิกแบบลบรอยหยัก (Anti-aliasing) เพื่อความคมชัดและสวยงาม
     * 
     * @param g กราฟิกส์คอนเท็กซ์เดิม
     * @return Graphics2D กราฟิกส์คอนเท็กซ์ที่เปิดใช้งาน Anti-aliasing แล้ว
     */
    public static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    /**
     * ตั้งค่าแบบอักษรเริ่มต้น (Default Font) ให้กับคอมโพเนนต์ Swing ทุกชนิดในระบบ
     * 
     * @param f แบบอักษร FontUIResource ที่ต้องการกำหนด
     */
    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        for (Object key : java.util.Collections.list(UIManager.getDefaults().keys())) {
            if (UIManager.get(key) instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    /**
     * RoundedPanel คือคอมโพเนนต์พาเนลพื้นหลังขอบโค้งมน
     */
    public static class RoundedPanel extends JPanel {
        protected Color bg;
        protected Color border;
        protected final int radius;

        public RoundedPanel(Color bg, Color border, int radius) {
            this.bg = bg;
            this.border = border;
            this.radius = radius;
            setOpaque(false);
        }

        public RoundedPanel(int radius, Color fill, Color border) {
            this(fill, border, radius);
        }

        public void setBorderColor(Color c) {
            this.border = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Badge คือคอมโพเนนต์ป้ายข้อความขนาดเล็กพร้อมพื้นหลังโค้งมน
     */
    public static class Badge extends JLabel {
        protected final Color bg;

        public Badge(String text, Color bg, Color fg) {
            super(text, SwingConstants.CENTER);
            this.bg = bg;
            setFont(new Font("Tahoma", Font.BOLD, 10));
            setForeground(fg);
            FontMetrics fm = getFontMetrics(getFont());
            int w = fm.stringWidth(text) + 16;
            setSize(w, 20);
            setPreferredSize(new Dimension(w, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * FlatButton คือคอมโพเนนต์ปุ่มกดดีไซน์แบนราบแบบโมเดิร์น
     */
    public static class FlatButton extends JButton {
        protected final boolean primary;
        protected Color primaryColor = new Color(15, 118, 110);
        protected Color borderColor = new Color(229, 231, 235);
        protected Color itemBg = new Color(249, 250, 251);

        public FlatButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(new Font("Tahoma", Font.BOLD, 12));
            setForeground(primary ? Color.WHITE : new Color(17, 24, 39));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void setColors(Color primaryColor, Color borderColor, Color itemBg) {
            this.primaryColor = primaryColor;
            this.borderColor = borderColor;
            this.itemBg = itemBg;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            boolean over = getModel().isRollover();
            if (primary) {
                g2.setColor(over ? primaryColor.darker() : primaryColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            } else {
                g2.setColor(over ? itemBg : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * RoundField คือคอมโพเนนต์กล่องข้อความขอบโค้งมนพร้อมข้อความตัวอย่าง
     * (Placeholder)
     */
    public static class RoundField extends JTextField {
        protected final String placeholder;
        protected final Color bg;
        protected final boolean searchIcon;
        protected Color primaryColor = new Color(15, 118, 110);
        protected Color borderColor = new Color(229, 231, 235);
        protected Color textLight = new Color(156, 163, 175);

        public RoundField(String placeholder, Color bg, boolean searchIcon) {
            this.placeholder = placeholder;
            this.bg = bg;
            this.searchIcon = searchIcon;
            setOpaque(false);
            setFont(new Font("Tahoma", Font.PLAIN, 12));
            setForeground(new Color(17, 24, 39));
            setCaretColor(new Color(17, 24, 39));
            setBorder(new EmptyBorder(0, searchIcon ? 32 : 12, 0, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(hasFocus() ? primaryColor : borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            if (searchIcon) {
                int cy = getHeight() / 2;
                g2.setColor(textLight);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(12, cy - 6, 10, 10);
                g2.drawLine(20, cy + 2, 24, cy + 6);
            }
            g2.dispose();
            super.paintComponent(g);

            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g3 = smooth(g);
                g3.setFont(getFont());
                g3.setColor(textLight);
                FontMetrics fm = g3.getFontMetrics();
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g3.drawString(placeholder, getInsets().left, ty);
                g3.dispose();
            }
        }
    }

    /**
     * ChevronButton คือคอมโพเนนต์ปุ่มลูกศรชี้ลงสำหรับคอมโบ้บ็อกซ์
     */
    public static class ChevronButton extends JButton {
        public ChevronButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(new Color(107, 114, 128));
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
    public static class RadioIcon implements Icon {
        protected Color primaryColor = new Color(15, 118, 110);

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = smooth(g);
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, 15, 15);
            g2.setColor(b.isSelected() ? primaryColor : new Color(209, 213, 219));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawOval(x, y, 15, 15);
            if (b.isSelected()) {
                g2.setColor(primaryColor);
                g2.fillOval(x + 4, y + 4, 8, 8);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    /**
     * HeaderRenderer คือคลาสกำหนดการแสดงผลส่วนหัวของตารางในธีมระบบ
     */
    public static class HeaderRenderer extends DefaultTableCellRenderer {
        protected final int rightAlignCol;

        public HeaderRenderer() {
            this(-1);
        }

        public HeaderRenderer(int rightAlignCol) {
            this.rightAlignCol = rightAlignCol;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row,
                int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setFont(new Font("Tahoma", Font.PLAIN, 11));
            setForeground(new Color(107, 114, 128));
            setBackground(Color.WHITE);
            setHorizontalAlignment(col == rightAlignCol ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(229, 231, 235)), new EmptyBorder(0, 14, 0, 14)));
            return this;
        }
    }

    /**
     * AvatarPanel คือคอมโพเนนต์แสดงรูปโปรไฟล์ผู้ใช้งานแบบวงกลม
     */
    public static class AvatarPanel extends JPanel {
        public AvatarPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            int s = Math.min(getWidth(), getHeight()) - 1;
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, s, s));
            g2.setColor(new Color(209, 213, 219));
            g2.fillRect(0, 0, s, s);
            g2.setColor(new Color(107, 114, 128));
            g2.fillOval(s / 2 - 6, 7, 12, 12);
            g2.fillOval(s / 2 - 12, 21, 24, 20);
            g2.setClip(null);
            g2.setColor(new Color(229, 231, 235));
            g2.drawOval(0, 0, s, s);
            g2.dispose();
        }
    }

    /**
     * RoomImage
     * คือคอมโพเนนต์สำหรับแสดงรูปภาพห้องพักพร้อมการจัดกรอบขอบมนและไอคอนสำรอง
     */
    public static class RoomImage extends JPanel {
        protected final Image photo;

        public RoomImage(String[] paths, int width, int height) {
            this.photo = ImageLoader.load(paths);
            setOpaque(false);
            setPreferredSize(new Dimension(width, height));
            setSize(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth(), h = getHeight();
            if (w > 0 && h > 0) {
                g2.clipRect(0, 0, w, h);
                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));

                if (photo != null) {
                    g2.drawImage(photo, 0, 0, w, h, this);
                } else {
                    g2.setColor(new Color(248, 250, 252));
                    g2.fillRect(0, 0, w, h);

                    float[] dash = { 6.0f, 4.0f };
                    g2.setStroke(
                            new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                    g2.setColor(new Color(203, 213, 225));
                    g2.drawRoundRect(1, 1, w - 3, h - 3, 10, 10);

                    g2.setStroke(new BasicStroke(1.5f));
                    g2.setColor(new Color(148, 163, 184));
                    int cx = w / 2, cy = h / 2 - 8;
                    g2.drawRoundRect(cx - 14, cy - 10, 28, 20, 4, 4);
                    g2.drawOval(cx - 5, cy - 5, 10, 10);

                    Font baseFont = UIManager.getFont("Label.font");
                    if (baseFont != null) {
                        g2.setFont(baseFont.deriveFont(Font.PLAIN, 11f));
                    }
                    String msg = "ไม่มีรูปภาพห้องพัก";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, cy + 24);
                }
            }
            g2.dispose();
        }
    }
}
