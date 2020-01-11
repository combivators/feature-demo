package net.tiny.feature.demo;

import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.tiny.ws.mvc.ModelAndView;

@Path("/home/demo")
public class SidebarSerice {

    @GET
    @Path("sidebar")
    @Produces(value = MediaType.TEXT_HTML)
    public ModelAndView home() {
        ModelAndView mv = new ModelAndView("sidebar/index.html");
        Properties prop = new Properties();
        prop.setProperty("title", "Bootstrap Sidebar template");
        prop.setProperty("description", "Bootstrap4 Collapsible Sidebar");
        prop.setProperty("keywords", "bootstrap sidebar template");
        prop.setProperty("author", "combivators");
        mv.addParams(prop);
        return mv;
    }
}
