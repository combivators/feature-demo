package net.tiny.feature.demo;


import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.tiny.service.ServiceContext;

/**
 * 输出fontawesome格式图标
 * API: /ui/svg/{fas}/{icon}/{color}/{bg}
 */
@Path("/")
public class BrandImageService {

    @Resource
    private ServiceContext serviceContext;
/*
    @Override
    protected void execute(HTTP_METHOD method, HttpExchange he) throws IOException {
        final RequestHelper request = HttpHandlerHelper.getRequestHelper(he);
        final String[] ids = request.getAllParameters();
        final ResponseHeaderHelper header = HttpHandlerHelper.getHeaderHelper(he);
        try {
            byte[] svg = generateSvg(ids);
            header.setContentType(MIME_TYPE.SVG);
            header.set(HEADER_LAST_MODIFIED, HttpDateFormat.format(new Date(System.currentTimeMillis())));
            header.set("Server", DEFALUT_SERVER_NAME);
            header.set("Connection", "Keep-Alive");
            header.set("Keep-Alive", "timeout=10, max=1000");
            if (maxAge > 0L) {
              header.set("Cache-Control", "max-age=" + maxAge); //"max-age=0" 86400:1day
            }
            header.setContentLength(svg.length);
            he.sendResponseHeaders(HttpURLConnection.HTTP_OK, svg.length);
            he.getResponseBody().write(svg);
        } catch (Exception e) {
            //Not found
            he.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
        }
    }
*/
    @GET
    @Path("ui/svg/{fas}/{icon}/{color}/{bg}")
    @Produces(value = MediaType.APPLICATION_SVG_XML)
    public byte[] image(@PathParam("fas")String fas, @PathParam("icon")String icon, @PathParam("color")String color, @PathParam("bg")String background) {
        final BrandImage image = serviceContext.lookup(BrandImage.class);
        final byte[] svg = image.generate(fas, icon, color, background);
        return svg;
    }
}
