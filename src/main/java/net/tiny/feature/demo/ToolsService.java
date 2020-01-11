package net.tiny.feature.demo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.tiny.ws.mvc.ModelAndView;

@Path("/home/tools")
public class ToolsService {

    @GET
    @Path("index")
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

    @POST
    @Path("post")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(value = MediaType.TEXT_HTML)
    public ModelAndView post(final Map<Object, Object> params) {
        ModelAndView mv = new ModelAndView("tools/index.html");
        Properties prop = new Properties();
        prop.setProperty("title", "Online Base64 Encoder Decoder");
        prop.setProperty("description", "online base64 decoder utility. Base64 to text. Base64 to file download.");
        prop.setProperty("keywords", "base64 encode decode convert");
        prop.setProperty("author", "combivators");
        mv.addParams(prop);
        if (null != params) {
            for (Entry<Object, Object> entry : params.entrySet()) {
                System.out.println(String.format("%s : '%s'", entry.getKey().toString(), entry.getValue().toString()));
            }
        }
        return mv;
    }
}
