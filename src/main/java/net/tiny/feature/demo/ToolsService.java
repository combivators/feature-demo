package net.tiny.feature.demo;

import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.tiny.ws.mvc.ModelAndView;

@Path("/")
public class ToolsService {

    @GET
    @Path("ui/tools")
    @Produces(value = MediaType.TEXT_HTML)
    public ModelAndView home() {
        ModelAndView mv = new ModelAndView("tools/index.html");
        Properties prop = new Properties();
        prop.setProperty("title", "Online Base64 Encoder Decoder");
        prop.setProperty("description", "online base64 decoder utility. Base64 to text. Base64 to file download.");
        prop.setProperty("keywords", "base64 encode decode convert");
        prop.setProperty("author", "combivators");
        mv.addParams(prop);
        return mv;
    }
}
