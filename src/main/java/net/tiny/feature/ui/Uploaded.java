package net.tiny.feature.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @see uploads.js$Outcome
 */
public class Uploaded {

    public String error = "";
    public List<String> errorkeys = new ArrayList<>();
    public List<String> preview = new ArrayList<>();
    public PreviewConfig config = null;
    public boolean append = true;

    public static class PreviewConfig {
        public String type;    //
        public String filetype;
        public String caption; // 'desert.jpg'
        public int size;
        public Map<String,String> extra = new HashMap<>();
    }
}
