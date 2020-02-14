package net.tiny.feature.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.tiny.ws.cache.BarakbCache;

public class BrandImage {

    private String path = "/home/fontawesome-free-5.11.2-web";
    private long maxAge  = 86400L; //1 day
    private int cacheSize = -1;
    private Function<Fontawesome, byte[]> function = null;
    private Map<String, String> index = new HashMap<>();
    private Map<String, String> colors = new HashMap<>();

    public BrandImage() {
        index.put("fa", "solid");
        index.put("fas", "solid");
        index.put("fab", "brnds");
        index.put("far", "regular");
        Collections.unmodifiableMap(index);

        //Bootstrap colors
        colors.put("primary", "#007bff");
        colors.put("secondary", "#6c757d");
        colors.put("success", "#28a745");
        colors.put("info", "#17a2b8");
        colors.put("muted", "solid");
        colors.put("warning", "#ffc107");
        colors.put("danger", "#dc3545");
        colors.put("light", "#f8f9fa");
        colors.put("dark", "#343a40");
        colors.put("white", "#fff");
        colors.put("blue", "#007bff");
        colors.put("indigo", "#6610f2");
        colors.put("purple", "#6f42c1");
        colors.put("pink", "#e83e8c");
        colors.put("red", "#dc3545");
        colors.put("orange", "#fd7e14");
        colors.put("yellow", "#ffc107");
        colors.put("green", "#28a745");
        colors.put("teal", "#20c997");
        colors.put("cyan", "#17a2b8");
        colors.put("gray", "#6c757d");
        colors.put("graydark", "#343a40");
        Collections.unmodifiableMap(colors);
    }

    public void setPath(String p) {
        path = p;
        Path home = Paths.get(path);
        if (!Files.exists(home)) {
            throw new IllegalArgumentException("Not found fontawesome path : " + path);
        }
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int size) {
        cacheSize = size;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long age) {
        maxAge = age;
    }

    private byte[] getCacheableImage(Fontawesome target) {
        if (function == null) {
            if (cacheSize > 0) {
                // Cache max files
                function = new ImagedCache(cacheSize);
            } else {
                function = new Function<Fontawesome, byte[]>() {
                    @Override
                    public byte[] apply(Fontawesome t) {
                        return generateImage(t);
                    }

                };
            }
        }
        return function.apply(target);
    }

    // Generate fontawesome icon as SVG image.
    public byte[] generate(String fas, String icon, String color, String background) {
        return getCacheableImage(new Fontawesome(fas, icon, color, background));
    }

    //<svg xmlns="http://www.w3.org/2000/svg" style="background-color:#ccc">
    //<path style="fill:red;fill-opacity:1">
    private byte[] setStyle(byte[] buffer, String fill, String bgcolor) {
        StringBuffer sb = new StringBuffer(new String(buffer));
        int pos;
        String style;
        if (bgcolor != null) {
            pos = sb.indexOf("<svg");
            pos = sb.indexOf(">", (pos+5));
            style = String.format(" style=\"background-color:%s\"", color(bgcolor));
            sb.insert(pos, style);
        }
        if (fill != null) {
            pos = sb.indexOf("<path");
            pos = sb.indexOf(">", (pos+6));
            style = String.format(" style=\"fill:%s\"", color(fill));
            sb.insert(pos-1, style);
        }
        return sb.toString().getBytes();
    }

    private final String color(String c) {
        String sc = colors.get(c);
        return sc != null ? sc : c;
    }

    private final byte[] readResource(String resource) throws IOException {
        return Files.readAllBytes(Paths.get(resource));
    }

    final byte[] generateImage(Fontawesome target) {
        try {
            final StringBuilder resource = new StringBuilder(path);
            resource.append("/svgs/");
            resource.append(index.get(target.fas));  //fas:solid fab:brnds far:regular
            resource.append("/");
            resource.append(target.icon.substring(3));
            resource.append(".svg");
            byte[] template = readResource(resource.toString());
            if (target.background != null  && !target.background.isEmpty()) {
                template = setStyle(template, target.color, target.background);
            } else {
                template = setStyle(template, target.color, null);
            }
            return template;
        } catch (Exception ex) {
            try {
                return readResource(path + "/svgs/solid/question.svg");
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public static class Fontawesome {
        final String fas, icon, color, background;
        public Fontawesome(String f, String i, String c, String b) {
            fas = f;
            icon = i;
            color = c;
            background = b;
        }
        public Fontawesome(String f, String i, String c) {
            this(f,i,c,null);
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(fas);
            sb.append("/")
              .append("icon")
              .append("/")
              .append(color);
            if (background != null) {
                sb.append("/").append(background);
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Fontawesome))
                return false;
            return hashCode() == o.hashCode();
        }
    }

    class ImagedCache implements Function<Fontawesome, byte[]> {
        private final BarakbCache<Fontawesome, byte[]> cache;
        public ImagedCache(int capacity) {
            this.cache = new BarakbCache<>(key -> generateImage(key), capacity);
            // Have an error from the file system that a file was deleted.
            this.cache.setRemoveableException(RuntimeException.class);
        }

        public void clear() {
            cache.clear();
        }

        public byte[] get(Fontawesome target) throws IOException {
            try {
                return cache.get(target);
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

        @Override
        public byte[] apply(Fontawesome target) {
            try {
                return get(target);
            } catch (IOException e) {
                throw new IllegalArgumentException(String.format("Read fontawesome '%s' error.", target.toString()));
            }
        }

    }


}
