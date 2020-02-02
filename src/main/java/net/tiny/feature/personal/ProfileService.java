package net.tiny.feature.personal;

import java.util.Properties;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.tiny.service.ServiceContext;
import net.tiny.ws.mvc.ModelAndView;

@Path("/")
public class ProfileService {

    @Resource
    private ServiceContext serviceContext;

    @GET
    @Path("ui/profile/edit")
    @Produces(value = MediaType.TEXT_HTML)
    public ModelAndView edit() {
        //PropertiesEditor editor = getPropertiesEditor();
        final ModelAndView mv = new ModelAndView("profile/index.html");
        Properties prop = new Properties();
        prop.setProperty("title", "Profile Editor");
        prop.setProperty("description", "Online Profile Editor");
        prop.setProperty("keywords", "i18n multi Profile editor");
        prop.setProperty("author", "combivators");
        mv.addParams(prop);
        return mv;
    }
}
