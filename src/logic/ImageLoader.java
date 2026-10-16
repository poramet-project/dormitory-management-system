package logic;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.net.URL;

/**
 * ImageLoader คือคลาสยูทิลิตี้สำหรับโหลดรูปภาพของระบบหอพักจากตำแหน่งต่าง ๆ
 * DIRS คือ รายการไดเรกทอรีและโฟลเดอร์ที่เป็นไปได้สำหรับค้นหารูปภาพ
 */
public final class ImageLoader {

    // ===== representation =====
    // Abstraction Function:
    // AF(DIRS) คือ ตัวช่วยค้นหาและโหลดทรัพยากรรูปภาพ (Image) ทั้งจาก Classpath และระบบไฟล์ในเครื่อง
    // RI
    // 1. DIRS ต้องไม่เป็น null และมีองค์ประกอบอย่างน้อย 1 โฟลเดอร์

    private ImageLoader() { }

    private static final String[] DIRS = {
            "", "src/", "img/", "src/img/", "../src/", "../src/img/", "../img/", "DMS/src/img/", "DMS/img/",
            "dormitory-management-system-main/",
            "dormitory-management-system-main/src/",
            "dormitory-management-system-main/img/",
            "dormitory-management-system-main/src/img/"
    };

    /**
     * ค้นหาและโหลดทรัพยากรรูปภาพจาก Classpath หรือระบบไฟล์ตามรายชื่อและพาธที่กำหนด
     * 
     * @param names รายชื่อไฟล์หรือพาธของรูปภาพที่ต้องการค้นหาและโหลด
     * @return ออบเจ็กต์ Image ที่โหลดสำเร็จ หรือ null หากไม่พบไฟล์รูปภาพ
     */
    public static Image load(String... names) {
        for (String n : names) {
            String base = n.substring(Math.max(n.lastIndexOf('/'), n.lastIndexOf('\\')) + 1);

            // 1) classpath
            for (String res : new String[]{ "/" + n, "/img/" + base }) {
                try {
                    URL url = ImageLoader.class.getResource(res);
                    if (url != null) {
                        Image img = ImageIO.read(url);
                        if (img != null) return img;
                    }
                } catch (Exception ignored) { }
            }

            // 2) ไฟล์ในเครื่อง
            for (String d : DIRS) {
                for (String candidate : new String[]{ d + n, d + base }) {
                    try {
                        File f = new File(candidate);
                        if (f.isFile()) {
                            Image img = ImageIO.read(f);
                            if (img != null) return img;
                        }
                    } catch (Exception ignored) { }
                }
            }
        }
        System.err.println("[ImageLoader] ไม่พบรูป: " + String.join(", ", names)
                + "  (โฟลเดอร์ที่รัน: " + new File("").getAbsolutePath() + ")");
        return null;
    }
}
