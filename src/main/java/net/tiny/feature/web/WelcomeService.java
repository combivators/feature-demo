package net.tiny.feature.web;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.tiny.service.ServiceContext;
import net.tiny.ws.mvc.ModelAndView;
// https://bootsnipp.com/snippets/352pm
@Path("/home")
public class WelcomeService {

    @Resource
    private ServiceContext serviceContext;

    @GET
    @Path("index")
    @Produces(value = MediaType.TEXT_HTML)
    public ModelAndView home() {
        return home("en");
    }

    @GET
    @Path("index/{lang}")
    @Produces(value = MediaType.TEXT_HTML)
    public ModelAndView home(@DefaultValue("en") @PathParam("lang")String lang) {
        ModelAndView mv = new ModelAndView("welcome/index.html");
        /*
        Properties prop = ;
        prop.setProperty("page.title", "Welcome feature platform");
        prop.setProperty("page.description", "The future talent information platform with assess function by AI");
        prop.setProperty("page.keywords", "Job AI NLP Resume Assess Search Engine");
        prop.setProperty("author", "combivators");
        //Hi, :Hello, Welcome to feature platform
        prop.setProperty("welcome", "Hello, Welcome to feature platform");
        //我们是一群来自遥远东方大陆的拓荒者:We are a group of frontiersman.
        prop.setProperty("who", "我们是一群来自远方的拓荒者");
        */
        PropertiesEditor editor = serviceContext.lookup("res.editor", PropertiesEditor.class);
        mv.addParams(editor.resources(lang));
        return mv;
    }
}
