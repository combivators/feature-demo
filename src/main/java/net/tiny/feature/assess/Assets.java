package net.tiny.feature.assess;

import java.util.List;

/**
 * Assets : 数值化的资历表
 * JSON格式大小约1K
 */
public class Assets {

    public static final String VERSION = Version.CURRENT.ver();

    public static class Asset {
        public String name;
        public double score = 0.0d;
    }

    /**
     * Qualifications : 用于评价计算的资历表
     * JSON格式大小约360字节
     */
    public static class Qualifications {
        public String ver = VERSION;
        public String id;
        public int[] orders;
        public String[] titles;
        public Double[] scores;
        public long modified = System.currentTimeMillis();
        public String signature;
    }

    public String ver = VERSION;
    public String id;
    public List<Asset> assets;
    public long modified = System.currentTimeMillis();
    public String signature;

    /**
     * 有效的评估项目
     * @param name
     * @return
     */
    public boolean vaild(String name) {
        return assets.stream()
            .anyMatch(a -> a.name.equals(name) && a.score > 0.0d);
    }
    /**
     * 评估项目数值
     * @param name
     * @return
     */
    public double score(String name) {
        for (Asset as : assets) {
            if (as.name.equals(name)) {
                return as.score;
            }
        }
        return 0.0d;
    }

    public String[] titles() {
        if (null == assets || assets.isEmpty()) {
            return null;
        }
        int size = assets.size();
        String[] values = new String[size];
        for (int i=0; i<assets.size(); i++) {
            values[i] = assets.get(i).name;
        }
        return values;
    }

    /**
     * 评估数值矩阵
     * @return
     */
    public Double[] scores() {
        if (null == assets || assets.isEmpty()) {
            return null;
        }
        int size = assets.size();
        Double[] values = new Double[size];
        for (int i=0; i<assets.size(); i++) {
            values[i] = assets.get(i).score;
        }
        return values;
    }

    public Qualifications toQualifications() {
        Qualifications qualifications = new Qualifications();
        qualifications.id = id;
        qualifications.signature = signature;
        qualifications.titles = titles();
        qualifications.scores = scores();
        return qualifications;
    }
}
