package net.tiny.feature.demo.isj;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import net.tiny.mem.Codec;
import net.tiny.mem.Collector;
import net.tiny.mem.DirectMemory;
import net.tiny.mem.DistanceFunction;
import net.tiny.mem.Memory;
import net.tiny.mem.Neighbor;
import net.tiny.tree.NearestNeighbor;

public class JapanGeographyDataLoaderTest {

    @Test
    public void testGetDownloadSite() throws Exception {
        assertEquals("http://nlftp.mlit.go.jp/isj/dls/data/12.0b/13000-12.0b.zip", JapanGeographyDataLoader.getDownloadSite("12.0b", PrefectureCode.valueOf(13)));
        assertEquals("http://nlftp.mlit.go.jp/isj/dls/data/17.0a/13000-17.0a.zip", JapanGeographyDataLoader.getDownloadSite("17.0a", PrefectureCode.valueOf(13)));
    }

    @Test
    public void testFloatToLong() throws Exception {
        float[] query = new float[] {35.686301f, 139.768087f};
        String[] values = new String[] {"131010027001", "131010027002", "131010027003"};
        System.out.println(String.format("%.6f - %.6f", query[0], query[1]));
        System.out.println(String.format("%.6f - %.6f", Float.parseFloat(values[0]), Float.parseFloat(values[1])));
        System.out.println(String.format("%d - %d", 131010027001L, 131010027002L));
    }

    @Test
    public void testGetDownloadCachedName() throws Exception {
        URL url = new URL("http://nlftp.mlit.go.jp/isj/dls/data/17.0a/13000-17.0a.zip");
        final String uri = url.toString();
        final int pos = uri.lastIndexOf("/");
        final int end = uri.lastIndexOf(".");
        String name = uri.substring(pos+1, end).concat(".csv");
        assertEquals("13000-17.0a.csv", name);
    }

    @Test
    public void testDownloadCache() throws Exception {
        final String path = "src/test/resources/isj/cache";
        JapanGeographyDataLoader loader = new JapanGeographyDataLoader.Builder()
                .cachePath(path)
                .build();
        Path cached = loader.getDownloadlPath(PrefectureCode.valueOf(1), false);
        assertNotNull(cached);
        assertTrue(Files.exists(cached));
    }

    @Test
    public void testFetchGeographyData() throws Exception {
        final String path = "src/test/resources/isj/cache";
        JapanGeographyDataLoader loader = new JapanGeographyDataLoader.Builder()
                .cachePath(path)
                .build();
        PrefectureCode prefecture = PrefectureCode.valueOf(1);
        Stream<GeographyData> data =loader.getStream(prefecture);
        System.out.println(prefecture.name + " : " + data.count());

        Stream<String[]> csv =loader.getDataStream(prefecture, false);
        List<String[]> list = csv.limit(10)
                                    .collect(Collectors.toList());
        String[] records = list.get(0);
        assertEquals(10, records.length);
        StringBuffer sb = new StringBuffer();
        for (String s : records) {
            if (sb.length()> 0) {
                sb.append(", ");
            }
            sb.append(s);
        }
        System.out.println(prefecture.name + " : " + sb.toString());
    }

    @Test
    public void testParseGeographyData() throws Exception {
        final String path = "src/test/resources/isj/cache";
        JapanGeographyDataLoader loader = new JapanGeographyDataLoader.Builder()
                .cachePath(path)
                .build();
        PrefectureCode prefecture = PrefectureCode.valueOf(1);
        Stream<GeographyData> data =loader.getStream(prefecture);
        data.limit(10)
            .forEach(e -> System.out.println(e.toString()));
    }


    @Test
    public void testLoadGeographyData() throws Exception {
        final String path = "src/test/resources/isj/cache";
        JapanGeographyDataLoader loader = new JapanGeographyDataLoader.Builder()
                .cachePath(path)
                .build();
        PrefectureCode prefecture = PrefectureCode.valueOf(1);
        int dimension = 4;
        long capacity = 50000L;
        Memory<float[]> memory = DirectMemory.allocateFloat(dimension, capacity);
        loader.load(memory, prefecture, false);
        System.out.println(memory.toString()); // 'float[]'(25956) size:12x1000000=11.44MB
        memory.free();
    }

    //@Test
    @Disabled
    public void testLoadAllGeographyData() throws Exception {
        final String path = "src/test/resources/isj/cache";
        JapanGeographyDataLoader loader = new JapanGeographyDataLoader.Builder()
                .cachePath(path)
                .build();

        int dimension = 4;
        long capacity = 200000L;
        Memory<float[]> memory = DirectMemory.allocateFloat(dimension, capacity);
        loader.loadAll(memory, false);
        System.out.println(memory.toString()); //'float[]'(189817) size:12x1000000=11.44MB
        memory.free();

        JapanGeographyDataLoader.Metrics[] metrics = loader.metrics();
        for (JapanGeographyDataLoader.Metrics m : metrics) {
             System.out.println(m.toString());
        }
        // [Downloader] : EAT:1.925s Avg:41ms
        // [Memory] : EAT:0.876s Avg:18ms
    }


    @Test
    public void testSearchGeographyData() throws Exception {
        final String path = "src/test/resources/isj/cache";
        JapanGeographyDataLoader loader = new JapanGeographyDataLoader.Builder()
                .cachePath(path)
                .build();

        int dimension = 4;
        long capacity = 200000L;
        Memory<float[]> memory = DirectMemory.allocateFloat(dimension, capacity);
        loader.loadAll(memory, false);
        System.out.println(memory.toString()); //'float[]'(189817) size:12x1000000=11.44MB

        int limit = 10;
        float[] query = new float[] {35.686301f, 139.768087f}; //131010026002: 大手町二丁目
        DistanceFunction<float[]> comparator = DistanceFunction.floats(2);//2维经纬度查询 快：30ms : 0.00(131010026002)-0.01(131010028000)
        //DistanceFunction<float[]> comparator = Geography.DISTSNCE;//慢：130ms : 0.00(131010026002)-757.92(131020020001)

        Neighbor<float[]> neighbor = new Neighbor<>(comparator, query, limit);
        //1-Threads ETA:30ms
        long st = System.currentTimeMillis();
        Collector.search(memory, neighbor);
        long eta = System.currentTimeMillis() - st;
        System.out.println(String.format("Normal Top Search ETA: %dms Top[%d]: %.2f(%d)-%.2f(%d)", eta, neighbor.size(),
                neighbor.getNearest(), parseLong(neighbor.first()),
                neighbor.getFarthest(), parseLong(neighbor.last())));

        assertEquals(0.0d, neighbor.getNearest());
        assertEquals(131010026002L, parseLong(neighbor.first()));

        List<float[]> neighbors = neighbor.getNeighbors();
        for (float[] entry : neighbors) {
            float[] value = new float[] {entry[0], entry[1]};
            double dis = Geography.DISTSNCE.distance(query, value);
            double d2 = comparator.distance(query, value);
            System.out.println(String.format("%.4f - %.4f (%d)",  dis, d2, parseLong(entry)));
        }
        memory.free();
    }

    public static long parseLong(float[] entry) {
        int len = entry.length;
        float[] keys = new float[] {entry[len-2], entry[len-1]};
        return Codec.decodeLong(keys);
    }
}
