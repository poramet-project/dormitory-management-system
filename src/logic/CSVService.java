package logic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CSVService คือคลาสสำหรับจัดการการค้นหา อ่าน และเขียนไฟล์ข้อมูล CSV ของระบบ
 */
public final class CSVService {

    // ===== representation =====
    // Abstraction Function:
    // AF(cachedDataDir) คือ ตัวกลางจัดการไฟล์ CSV ระดับล่างของระบบ
    // RI
    // 1. DIRS ต้องมีโฟลเดอร์สำหรับค้นหาไฟล์ข้อมูลอย่างน้อย 1 รายการ
    // 2. ข้อมูลไฟล์ CSV ที่เขียนออกไปต้องเข้ารหัสด้วย UTF-8

    private CSVService() {
    }

    private static final String[] DIRS = {
            "dormitory-management-system-main/src/data/",
            "dormitory-management-system-main/data/",
            "src/data/",
            "data/",
            "DMS/src/data/",
            "dormitory-management-system-main/src/",
            "src/",
            "dormitory-management-system-main/",
            ""
    };

    private static File cachedDataDir = null;

    /**
     * ค้นหาไดเรกทอรีที่เก็บไฟล์ข้อมูล CSV จริงของระบบ
     *
     * @return ออบเจ็กต์ File ของไดเรกทอรีข้อมูล
     */
    public static synchronized File getDataDirectory() {
        if (cachedDataDir != null && cachedDataDir.isDirectory()) {
            return cachedDataDir;
        }

        // 1. ค้นหาโฟลเดอร์ที่มี rooms.csv หรือ users.csv ที่มีข้อมูลอยู่จริง
        for (String d : DIRS) {
            File dir = new File(d.isEmpty() ? "." : d);
            if (dir.isDirectory()) {
                File r = new File(dir, "rooms.csv");
                File u = new File(dir, "users.csv");
                if ((r.exists() && r.length() > 60) || (u.exists() && u.length() > 60)) {
                    cachedDataDir = dir;
                    return cachedDataDir;
                }
            }
        }

        // 2. หากยังไม่พบไฟล์ที่มีข้อมูล ให้เลือกโฟลเดอร์ data ที่มีอยู่จริง
        for (String d : DIRS) {
            File dir = new File(d.isEmpty() ? "." : d);
            if (dir.isDirectory() && d.contains("data")) {
                cachedDataDir = dir;
                return cachedDataDir;
            }
        }

        cachedDataDir = new File("data");
        return cachedDataDir;
    }

    /**
     * ค้นหาไฟล์ข้อมูลตามลำดับโฟลเดอร์ที่เป็นไปได้ในระบบ
     * 
     * @param name ชื่อไฟล์ที่ต้องการค้นหา
     * @return ออบเจ็กต์ File ของไฟล์ที่พบ หรือตำแหน่งเป้าหมาย
     */
    public static File file(String name) {
        File dir = getDataDirectory();
        File target = new File(dir, name);
        if (target.exists() && target.length() > 60) {
            return target;
        }

        File fallback = target.exists() ? target : null;
        for (String d : DIRS) {
            File f = new File(d + name);
            if (f.exists() && f.isFile()) {
                if (f.length() > 60) {
                    return f;
                }
                if (fallback == null) {
                    fallback = f;
                }
            }
        }

        return fallback != null ? fallback : target;
    }

    /**
     * แยกข้อความ 1 บรรทัดของ CSV ให้เป็นอาร์เรย์ของแต่ละคอลัมน์
     * โดยรองรับเครื่องหมายคำพูด (Quotes)
     * 
     * @param line ข้อความบรรทัดใน CSV
     * @return อาร์เรย์ของข้อความในแต่ละคอลัมน์
     */
    public static String[] parseLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else
                        quoted = false;
                } else
                    sb.append(c);
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }

    /**
     * อ่านข้อมูลทั้งหมดจากไฟล์ CSV ในรูปแบบตาราง (List ของแถวข้อมูลแบบ Map)
     * 
     * @param name ชื่อไฟล์ CSV
     * @return รายการแถวข้อมูลทั้งหมดในรูปแบบ Map คีย์เป็นชื่อคอลัมน์
     */
    public static synchronized List<Map<String, String>> readTable(String name) {
        List<Map<String, String>> rows = new ArrayList<>();
        File f = file(name);
        if (!f.exists() || f.length() == 0)
            return rows;
        try {
            String[] header = null;
            for (String raw : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) {
                String line = raw.replace("\uFEFF", "").trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                String[] c = parseLine(line);
                if (header == null) {
                    header = c;
                    for (int i = 0; i < header.length; i++)
                        header[i] = header[i].trim();
                    continue;
                }
                Map<String, String> m = new LinkedHashMap<>();
                for (int i = 0; i < header.length; i++) {
                    m.put(header[i], i < c.length ? c[i].trim() : "");
                }
                rows.add(m);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /**
     * แปลงข้อความให้ปลอดภัยสำหรับนำไปเขียนลงในไฟล์ CSV
     * (ครอบเครื่องหมายคำพูดหากจำเป็น)
     * 
     * @param v ข้อความที่ต้องการแปลง
     * @return ข้อความที่ปลอดภัยตามมาตรฐาน CSV
     */
    public static String csvEscape(String v) {
        if (v == null)
            return "";
        v = v.replace("\r", " ").replace("\n", " ").trim();
        if (v.contains(",") || v.contains("\"")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    /**
     * เพิ่มแถวข้อมูลใหม่ต่อท้ายไฟล์ CSV
     * 
     * @param name   ชื่อไฟล์ CSV
     * @param header ส่วนหัวตาราง (ใช้เมื่อไฟล์ยังไม่เคยมีอยู่)
     * @param values ค่าของแต่ละคอลัมน์ในแถวที่จะเพิ่ม
     * @throws IOException หากเกิดข้อผิดพลาดในการเขียนไฟล์
     */
    public static synchronized void appendRow(String name, String header, String... values) throws IOException {
        File f = file(name);
        if (f.getParentFile() != null)
            f.getParentFile().mkdirs();

        String existing = f.exists()
                ? new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8).replace("\uFEFF", "")
                : "";

        StringBuilder sb = new StringBuilder(existing.trim().isEmpty() ? header : existing.replaceAll("\\s+$", ""));
        String nl = System.lineSeparator();
        sb.append(nl);
        for (int i = 0; i < values.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(csvEscape(values[i]));
        }
        sb.append(nl);
        Files.write(f.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * สร้างรหัสถัดไปแบบอัตโนมัติ (Auto-increment ID) จากข้อมูลที่มีอยู่ในตาราง
     * 
     * @param name     ชื่อไฟล์ CSV
     * @param idColumn ชื่อคอลัมน์ที่เป็นรหัส
     * @param prefix   คำนำหน้ารหัส เช่น "BK", "CNT", "T"
     * @param digits   จำนวนหลักของตัวเลข เช่น 3 หลัก ("001")
     * @return รหัสใหม่ถัดไป
     */
    public static synchronized String nextId(String name, String idColumn, String prefix, int digits) {
        int max = 0;
        for (Map<String, String> m : readTable(name)) {
            String id = m.getOrDefault(idColumn, "").trim();
            if (id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return prefix + String.format("%0" + digits + "d", max + 1);
    }

    /**
     * ปรับปรุงค่าในคอลัมน์ของแถวข้อมูลที่ตรงกับค่าคีย์ที่กำหนดในไฟล์ CSV
     * 
     * @param name   ชื่อไฟล์ CSV
     * @param keyCol ชื่อคอลัมน์คีย์สำหรับค้นหาแถว
     * @param keyVal ค่าคีย์ที่ต้องการจับคู่
     * @param col    ชื่อคอลัมน์ที่ต้องการแก้ไขค่า
     * @param newVal ค่าใหม่ที่ต้องการเขียนลงไป
     * @return true หากพบแถวและปรับปรุงข้อมูลสำเร็จ
     */
    public static synchronized boolean updateCell(String name, String keyCol, String keyVal, String col,
            String newVal) {
        File f = file(name);
        if (!f.exists())
            return false;
        try {
            List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            int idCol = -1, stCol = -1;
            boolean changed = false;
            List<String> out = new ArrayList<>();
            for (String raw : lines) {
                String line = raw.replace("\uFEFF", "").trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    out.add(raw);
                    continue;
                }
                String[] c = parseLine(line);
                if (idCol < 0) { // บรรทัด header
                    for (int i = 0; i < c.length; i++) {
                        String h = c[i].trim();
                        if (h.equals(keyCol))
                            idCol = i;
                        if (h.equals(col))
                            stCol = i;
                    }
                    if (idCol < 0 || stCol < 0)
                        return false;
                    out.add(line);
                    continue;
                }
                if (idCol < c.length && stCol < c.length && c[idCol].trim().equalsIgnoreCase(keyVal.trim())) {
                    c[stCol] = newVal;
                    changed = true;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < c.length; i++) {
                    if (i > 0)
                        sb.append(',');
                    sb.append(csvEscape(c[i]));
                }
                out.add(sb.toString());
            }
            if (!changed)
                return false;
            Files.write(f.toPath(), (String.join(System.lineSeparator(), out) + System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
