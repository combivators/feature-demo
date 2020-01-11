package net.tiny.feature.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class PropertiesEditor {

    public static enum Lang {
        en("en", "US"),
        zh("zh", "CN"),
        ja("ja", "JP");

        Locale locale;
        Lang(String language, String country) {
            locale = new Locale(language, country);
        }
        @Override
        public String toString() {
            return locale.toString();
        }
        private static Map<String, Lang> map = new HashMap<>();
        static {
            for(Lang lang : Lang.values()){
                map.put(lang.locale.getLanguage(), lang);
            }
        }
        public String lang() {
            return locale.getLanguage();
        }
        public static Lang get(String lang) {
            return map.get(lang);
        }
    }

    private String path;
    private Map<String, Editor> editors = new HashMap<>();

    public void setPath(String p) {
        Path file = Paths.get(p);
        if (!file.toFile().exists() || !file.toFile().isDirectory()) {
            throw new IllegalArgumentException(String.format("Invaid resource directory '%s'.", p));
        }
        path = p;
    }

    public Properties resources(String lang) {
        return getEditor(lang).store;
    }

    public TreeView treeView(String lang) {
        return getEditor(lang).treeView();
    }

    public String getValue(String lang, String name) {
        return getEditor(lang).getValue(name);
    }

    public void setValue(String lang, String key, String value) {
        getEditor(lang).setValue(key, value);
    }

    protected Editor getEditor(String lang) {
        Editor editor = editors.get(lang);
        if (null == editor) {
            editor = new Editor(lang, path);
            editors.put(lang, editor);
        }
        return editor;
    }

    class Editor {
        private Path file;
        private String lastComment;
        private Properties store;

        public Editor(String lang, String root) {
            final String p =  String.format("%s/i18n_%s.properties", root, Lang.get(lang).toString());
            file = Paths.get(p);
            if (!file.toFile().exists() || !file.toFile().isFile()) {
                throw new IllegalArgumentException(String.format("Invaid resource file '%s'.", p));
            }
            store = new Properties();
            try {
                store.load(Files.newInputStream(file));
                lastComment = lastModified();
            } catch (IOException e) {
                throw new IllegalArgumentException(String.format("Read file '%s' error - %s.", p, e.getMessage()));
            }
        }


        public String getValue(String name) {
            return store.getProperty(name);
        }

        public void setValue(String key, String value) {
            store.setProperty(key, value);
            try {
                store.store(Files.newOutputStream(file), lastComment);
                lastComment = lastModified();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        public TreeView treeView() {
            return TreeView.of(store.stringPropertyNames());
        }

        private String lastModified() {
            try {
                return Files.getLastModifiedTime(file).toString();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
