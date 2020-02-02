package net.tiny.feature.demo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.tiny.ws.mvc.ModelAndView;

@Path("/")
public class LoginService {

    @GET
    @Path("ui/logon")
    @Produces(value = MediaType.TEXT_HTML)
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView("login/index.html");
        return mv;
    }

    @GET
    @Path("ui/signup")
    @Produces(value = MediaType.TEXT_HTML)
    public ModelAndView signup() {
        ModelAndView mv = new ModelAndView("login/index.html");
        return mv;
    }

    @GET
    @Path("ui/signin")
    @Produces(value = MediaType.TEXT_HTML)
    public ModelAndView signin() {
        ModelAndView mv = new ModelAndView("login/index.html");
        return mv;
    }
}
