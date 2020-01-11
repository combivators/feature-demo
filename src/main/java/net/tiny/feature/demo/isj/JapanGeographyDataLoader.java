package net.tiny.feature.demo.isj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.tiny.dao.SeparatedValues;
import net.tiny.mem.Codec;
import net.tiny.mem.Memory;
import net.tiny.ws.cache.BarakbCache;

/**
 * 日本国土交通省公開の位置参照情報データベース
 * 位置参照情報ダウンロードサービス
 * @see http://nlftp.mlit.go.jp/cgi-bin/isj/dls/_view_cities_wards.cgi
 *
 * 位置参照情報のデータ形式
 * @see http://nlftp.mlit.go.jp/isj/data.html
 * ダウンロードサイトURL http://nlftp.mlit.go.jp/isj/dls/data/{version}/{code}-{version}.zip
 *
 * 日本都道府県コード表
 * @see http://nlftp.mlit.go.jp/ksj/gml/codelist/PrefCd.html
 *
 */
public class JapanGeographyDataLoader {

    private static final Logger LOGGER = Logger.getLogger(JapanGeographyDataLoader.class.getName());

    final static int BUFFER_SIZE = 4096;
    final static String LAST_BLOCK_VERSION = "17.0a";
    final static String LAST_VERSION = "12.0b";
    static final String DOWNLOAD_URL_FORMAT = "http://nlftp.mlit.go.jp/isj/dls/data/%s/%s000-%s.zip";

    private final Builder builder;
    private DownloadCache cache;
    private Metrics downloader = new Metrics("Downloader");
    private Metrics meter = new Metrics("Memory");

    private JapanGeographyDataLoader(Builder builder) {
        this.builder = builder;
    }

    public Stream<GeographyData> getStream(PrefectureCode prefecture) throws IOException {
        return getStream(prefecture, false);
    }

    public Stream<GeographyData> getStream(PrefectureCode prefecture, boolean block) throws IOException {
        return Files.lines(getDownloadlPath(prefecture, block))
                .skip(1L) //Skip Title
                .map(line -> parseGeographyData(line));
    }

    public Stream<String[]> getDataStream(PrefectureCode prefecture, boolean block) throws IOException {
        return Files.lines(getDownloadlPath(prefecture, block))
                .skip(1L) //Skip Title
                .map(line -> new SeparatedValues(line).toArray());
    }

    public Path getDownloadlPath(PrefectureCode prefecture, boolean block) throws IOException {
        return getDownloadCache().get(prefecture, block);
    }

    //CSV数据调整：ID，緯度，経度，地名
    private GeographyData parseGeographyData(String line) {
        String[] array = new SeparatedValues(line).toArray();
        GeographyData data = new GeographyData();
        data.id = array[4];//ID
        data.longitude = Double.parseDouble(array[6]);//緯度
        data.latitude  = Double.parseDouble(array[7]);//経度
        data.address = String.format("%s %s %s", array[1], array[3], array[5]);//丁目区
        return data;
    }

    private DownloadCache getDownloadCache() {
        if (null == cache) {
            cache = new DownloadCache(builder.cacheSize);
        }
        return cache;
    }

    public void loadAll(Memory<float[]> memory, boolean block) {
        PrefectureCode[] codes = PrefectureCode.values();
        for (PrefectureCode pc : codes) {
            load(memory, pc, block);
        }
    }

    //加载地理位置数据到内存
    public void load(Memory<float[]> memory, PrefectureCode prefecture, boolean block) {
        try {
            Stream<GeographyData> stream = getStream(prefecture, block);
            long st = System.currentTimeMillis();
            stream.forEach(e -> append(memory, e));//加载到内存
            meter.count(st);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Metrics[] metrics() {
        return new Metrics[] {downloader, meter};
    }

    private void append(Memory<float[]> memory, GeographyData data) {
        memory.add(mapperData(data));
    }

    //地理位置数据浮点数值变换
    private float[] mapperData(GeographyData data) {
        float[] array = new float[4];
        array[0] = (float)data.longitude;//緯度
        array[1] = (float)data.latitude; //経度
        float[] key = Codec.encodeLong(Long.parseLong(data.id));//key
        array[2] = key[0];
        array[3] = key[1];
        return array;
    }

    String getDownloadSite(PrefectureCode prefecture, boolean block) {
        if (block) {
            return getDownloadSite(builder.lastBlockVersion, prefecture);
        } else {
            return getDownloadSite(builder.lastVersion, prefecture);
        }
    }

    static String getDownloadSite(String version, PrefectureCode prefecture) {
        return String.format(DOWNLOAD_URL_FORMAT, version, prefecture.code(), version);
    }

    public static class Builder {
        int cacheSize = 100;
        String cachePath = System.getProperty("java.io.tmpdir");
        String lastBlockVersion = LAST_BLOCK_VERSION;
        String lastVersion = LAST_VERSION;

        public Builder cachePath(String path) {
            final File dir = new File(path);
            if (! (dir.exists() && dir.isDirectory())) {
                throw new IllegalArgumentException(String.format("Set valid cache path. '%s'", path));
            }
            this.cachePath = path;
            return this;
        }

        public Builder cacheSize(int size) {
            if (size <= 1) {
                throw new IllegalArgumentException(String.format("Set cache size > %d number.", size));
            }
            this.cacheSize = size;
            return this;
        }
        public Builder blockVersion(String ver) {
            lastBlockVersion = ver;
            return this;
        }
        public Builder version(String ver) {
            lastVersion = ver;
            return this;
        }

        public JapanGeographyDataLoader build() {
            return new JapanGeographyDataLoader(this);
        }
    }

    class DownloadCache {
        private final BarakbCache<URL, Path> cache;

        /**
         * Create cache for the last capacity number used file.
         *
         * @param capacity
         */
        public DownloadCache(int capacity) {
            this.cache = new BarakbCache<>(key -> fetch(key), capacity);
            // Have an error from the file system that a file was deleted.
            this.cache.setRemoveableException(RuntimeException.class);
        }


        public Path get(PrefectureCode prefecture, boolean block) throws IOException {
            try {
                return cache.get(new URL(getDownloadSite(prefecture, block)));
            } catch (Throwable e) {
                Throwable cause = findErrorCause(e);
                if(cause instanceof IOException) {
                    throw (IOException)cause;
                } else {
                    throw new IOException(cause.getMessage(), cause);
                }
            }
        }

        private Throwable findErrorCause(Throwable err) {
            if(err instanceof IOException)
                return err;
            Throwable cause = err.getCause();
            if (null != cause) {
                return findErrorCause(cause);
            } else {
                return err;
            }
        }

        private Path fetch(URL url) {
            try {
                final String uri = url.toString();
                final int pos = uri.lastIndexOf("/");
                final int end = uri.lastIndexOf(".");
                final String name = uri.substring(pos+1, end).concat(".csv");
                Path cached = Paths.get(builder.cachePath, name);
                if (Files.exists(cached)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("Found a cached data '%s'.", cached.toString()));
                    }
                    // If download and uncompress, converted to 'UTF-8'
                    return cached;
                }

                long st = System.currentTimeMillis();
                final Path zip = download(url);
                downloader.count(st);
                cached = unzip(zip, name, "MS932", "UTF-8");
                Files.deleteIfExists(zip);
                return cached;
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        // Download
        private Path download(URL url) throws IOException {
            final String uri = url.toString();
            int pos = uri.lastIndexOf("/");
            String res = uri.substring(pos);
            Path target = Paths.get(builder.cachePath, res);
            InputStream in = url.openStream();
            Files.copy(in, target);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Cached '%s' contents to '%s'.", uri, target.toString()));
            }
            in.close();
            return target;
        }

        // Uncompress, converted 'MS932' to 'UTF-8'
        private Path unzip(Path zip, String name, String decode, String encode) throws IOException {
            final Path archive = Paths.get(builder.cachePath, name);
            final File outFile = archive.toFile();
            final ZipFile zipFile = new ZipFile(zip.toFile());
            Optional<? extends ZipEntry> entry = zipFile.stream()
                    .filter(e -> e.getName().endsWith(".csv"))
                    .findFirst();
            if (entry.isPresent()) {
                convert(zipFile.getInputStream(entry.get()), decode, new FileOutputStream(outFile), encode);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("Uncompress '%s' to '%s' and converted '%s' > '%s'.",
                            entry.get().getName(), outFile.getName(), decode, encode));
                }
            }
            zipFile.close();

            return archive;
        }
    }

    public static class Metrics {
        final String name;
        long total = 0L;
        long avg = 0L;
        long n = 0L;

        Metrics(String n) {
            name = n;
        }

        void count(long start) {
            total += (System.currentTimeMillis() - start);
            avg = total/++n;
        }

        public long total() {
            return total;
        }

        public long avg() {
            return avg;
        }
        @Override
        public String toString() {
            return String.format("[%s] : EAT:%.3fs Avg:%dms", name, (float)total/1000.0f, avg);
        }
    }

    public static void convert(InputStream in, String decode, OutputStream out, String encode) throws IOException {
        BufferedReader reader;
        BufferedWriter writer;
        if (null != decode) {
            reader = new BufferedReader(new InputStreamReader(in, decode));
        } else {
            reader = new BufferedReader(new InputStreamReader(in));
        }
        if (null != encode) {
            writer = new BufferedWriter(new OutputStreamWriter(out, encode), BUFFER_SIZE);
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(out), BUFFER_SIZE);
        }
        char[] buffer = new char[BUFFER_SIZE];
        int readSize = 0;
        try {
            // read and write until last byte is encountered
            while ((readSize = reader.read(buffer, 0, BUFFER_SIZE)) != -1) {
                writer.write(buffer, 0, readSize);
            }
            writer.flush();
        } finally {
            writer.close();
            reader.close();
        }
    }
}
