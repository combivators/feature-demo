package net.tiny.feature.assess;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.tiny.ws.auth.Codec;

/**
 * Assessment : 评估结果数值表
 * JSON格式大小约500字节
 */
public final class Assessment {

    public static final String VERSION = Version.CURRENT.ver();

    public static class Score {
        public String type;
        public double value;
    }

    /**
     * Scores : 评估结果
     * JSON格式大小约250字节
     */
    public static class Scores {
        public String ver = VERSION;
        public String id;
        public String[] types;
        public float[] scores;
        public long modified = System.currentTimeMillis();
        public String signature;
    }

    public String ver = VERSION;
    public int id;
    public String form;
    public String assets;
    public List<Score> scores;
    public long modified = System.currentTimeMillis();
    public String signature;

    public Map<String, Double> summary(Function<Score, String> catalog) {
        return scores.stream()
                     .collect(Collectors.groupingBy(catalog, Collectors.averagingDouble(s -> s.value)));
    }

    /**
     * 输出2位小数的评估结果
     *
     * @return
     */
    public Scores toScores() {
        return toScores(2);
    }

    /**
     * 输出评估结果
     * @param places 小数点后的精度
     * @return
     */
    public Scores toScores(int places) {
        Scores result = new Scores();
        result.signature = signature;
        result.types = new String[scores.size()];
        result.scores = new float[scores.size()];
        for (int i=0; i<scores.size(); i++) {
            Score s = scores.get(i);
            result.types[i] = s.type;
            result.scores[i] = (float)round(s.value, places);
        }
        result.id = generateScoresId(id, result.scores);
        return result;
    }

    public static String generateScoresId(int id, float[] values) {
        final int capacity = values.length + 1;
        final ByteBuffer buffer = ByteBuffer.allocate(capacity*4);
        buffer.putInt(id);
        for (int i=0; i<values.length; i++) {
            buffer.putFloat(values[i]);
        }
        return Codec.encodeString(buffer.array());
    }

    static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long ret = Math.round(value);
        return (double) ret / factor;
    }
}
