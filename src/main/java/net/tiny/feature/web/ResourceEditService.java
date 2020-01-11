package net.tiny.feature.web;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.tiny.service.ServiceContext;
import net.tiny.ws.mvc.ModelAndView;

@Path("/home/res")
public class ResourceEditService {

    @Resource
    private ServiceContext serviceContext;

    @GET
    @Path("edit")
    @Produces(value = MediaType.TEXT_HTML)
    public ModelAndView edit() {
        //PropertiesEditor editor = getPropertiesEditor();
        final ModelAndView mv = new ModelAndView("resource/index.html");
        Properties prop = new Properties();
        prop.setProperty("title", "Resource Editor");
        prop.setProperty("description", "Online Resource Editor");
        prop.setProperty("keywords", "i18n multi resource editor");
        prop.setProperty("author", "combivators");
        mv.addParams(prop);
        return mv;
    }

    @GET
    @Path("tree/{lang}")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Set<TreeView.Node> tree(@PathParam("lang")String lang) {
        final PropertiesEditor editor = getPropertiesEditor();
        return editor.treeView(lang).getNodes();
    }

    @GET
    @Path("value/{lang}/{name}")
    @Produces(value = MediaType.APPLICATION_JSON)
    public TreeView.KeyValue value(@PathParam("lang")String lang, @PathParam("name")String name) {
        final PropertiesEditor editor = getPropertiesEditor();
        final TreeView.KeyValue keyValue = new TreeView.KeyValue();
        keyValue.lang = lang;
        keyValue.key = name;
        keyValue.value = editor.getValue(lang, name);
        return keyValue;
    }

    @POST
    @Path("save")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TreeView.KeyValue save(@BeanParam TreeView.KeyValue kv) throws IOException {
        PropertiesEditor editor = getPropertiesEditor();
        editor.setValue(kv.lang, kv.key, kv.value);
        return kv;
    }

    private  PropertiesEditor getPropertiesEditor() {
         return serviceContext.lookup("res.editor", PropertiesEditor.class);
    }
}
