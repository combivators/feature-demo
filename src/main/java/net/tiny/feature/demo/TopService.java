package net.tiny.feature.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.tiny.ws.mvc.ModelAndView;

@Path("/")
public class TopService {

    @GET
    @Path("ui/top")
    @Produces(value = MediaType.TEXT_HTML)
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView("top/index.html");
        return mv;
    }

    @GET
    @Path("api/v1/query/{query}")
    @Produces(value = MediaType.APPLICATION_JSON)
    public List<Entry> match(@PathParam("query")String query) {
        List<Entry> entries = generator();
        return entries;
    }

    private List<Entry> generator() {
        List<Entry> entries = new ArrayList<>();
        for (int i=0; i<50; i++) {
            entries.add(generate());
        }
        return entries;
    }

    protected Entry generate() {
        int size = ThreadLocalRandom.current().nextInt(30, 64);
        int id = ThreadLocalRandom.current().nextInt(10000, 19999);
        Entry entry = new Entry();
        entry.width = size;
        entry.height = size;
        entry.image = "/images/" + icons[ThreadLocalRandom.current().nextInt(0, 20)];
        entry.image = "/v1/svg/" + icons[ThreadLocalRandom.current().nextInt(0, 41)];
        entry.url = entry.url + id;
        entry.tooltip = "Hoge";
        return entry;
    }

    static String[] icons = new String[] {
            "icon-01.png",
            "icon-02.png",
            "icon-03.png",
            "icon-04.png",
            "icon-05.png",
            "icon-06.png",
            "icon-07.png",
            "icon-08.png",
            "icon-09.png",
            "icon-10.png",
            "icon-11.png",
            "icon-12.png",
            "icon-13.png",
            "icon-14.png",
            "icon-15.png",
            "icon-16.png",
            "icon-17.png",
            "icon-18.png",
            "icon-19.png",
            "icon-20.png",
    };

    static String[] fas = new String[] {
            "fas/fa-icon/fillColor",
            "icon-02.png",
            "icon-03.png",
            "icon-04.png",
            "icon-05.png",
            "icon-06.png",
            "icon-07.png",
            "icon-08.png",
            "icon-09.png",
            "icon-10.png",
            "icon-11.png",
            "icon-12.png",
            "icon-13.png",
            "icon-14.png",
            "icon-15.png",
            "icon-16.png",
            "icon-17.png",
            "icon-18.png",
            "icon-19.png",
            "icon-20.png",
    };

    public static class Entry {
        public String image = "/images/ic_account.svg";
        public int width;
        public int height;
        public String url;
        public String target = "_top";
        public String tooltip;
        public int weight;
    }
}
