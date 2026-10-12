package logic;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.net.URL;

/**
 * โหลดรูปภาพโดยลองหลายที่ จึงไม่ขึ้นกับว่ารันโปรแกรมจากโฟลเดอร์ไหน
 * ลำดับ: classpath ( /ชื่อไฟล์ และ /img/ชื่อไฟล์ ) แล้วค่อยหาในไฟล์ตามโฟลเดอร์ต่าง ๆ
 * เช่น src/img, img, ../src/img
 */
public final class ImageLoader {
    private ImageLoader() { }

    private static final String[] DIRS = {
            "", "src/", "img/", "src/img/", "../src/", "../src/img/", "../img/", "DMS/src/img/", "DMS/img/"
    };

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
