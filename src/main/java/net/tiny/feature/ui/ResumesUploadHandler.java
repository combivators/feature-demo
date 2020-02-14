package net.tiny.feature.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.logging.Level;

import com.sun.net.httpserver.HttpExchange;

import net.tiny.config.JsonParser;
import net.tiny.feature.ui.Uploaded;
import net.tiny.resume.Crude;
import net.tiny.resume.Parser;
import net.tiny.ws.FormDataHandler;
import net.tiny.ws.HttpHandlerHelper;
import net.tiny.ws.ResponseHeaderHelper;
import net.tiny.ws.Unique;

/**
 * @see https://plugins.krajee.com/file-input
 * API :
 *  Method: POST
 *  URI:    /upload/v1/resume/
 */
public class ResumesUploadHandler extends FormDataHandler {

    @Override
    public void handle(HttpExchange he, List<MultiPart> parts) throws IOException {
        if (null == parts) {
            he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1);
            return;
        }

        final Uploaded uploaded = new Uploaded();

        //解析上传的简历文档
        final Crude crude = parseResume(parts, uploaded);
        if (crude == null) {
            //解析简历文档出错
            uploaded.error = "No upload file found.";
            uploaded.append = false;
        } else {
            final String json = JsonParser.marshal(crude);
            LOGGER.info(String.format("'%s' uploaded", crude.id));
        }
        final String res = JsonParser.marshal(uploaded);
        final ResponseHeaderHelper response = HttpHandlerHelper.getHeaderHelper(he);
        response.setContentType(MIME_TYPE.JSON);
        he.sendResponseHeaders(HttpURLConnection.HTTP_OK, res.length());
        he.getResponseBody().write(res.getBytes());
    }

    /**
     * 解析上传的简历文档，返回原始扫描文本。出错时返回空
     * @param parts
     * @return
     */
    Crude parseResume(List<MultiPart> parts, Uploaded uploaded) {
        ByteArrayInputStream stream = null;
        Parser.Type type = null;
        Uploaded.PreviewConfig config = new Uploaded.PreviewConfig();
        for (MultiPart part : parts) {
            if (PartType.FILE.equals(part.type)) {
                config.size = part.bytes.length;
                stream = new ByteArrayInputStream(part.bytes);
            } else {
                if( "fileId".equals(part.name)) {
                    type = Parser.guestType(part.value);
                    if (null != type) {
                        switch (type) {
                        case PDF:
                            config.type = "pdf";
                            break;
                        case Excel:
                            config.type = "xls";
                            break;
                        case Word:
                        case Docx:
                            config.type = "doc";
                            break;
                        }
                    }
                    int pos = part.value.indexOf("_");
                    if (pos != -1) {
                      //上传文件名
                        config.caption = part.value.substring(pos+1);
                    }
                }
            }
        }
        if (null == stream || type == null) {
            return null;
        }
        try {
            //解析上传的简历文档
            final Crude crude = new Parser().parse(stream, type);
            crude.id = Unique.uniqueKey();
            uploaded.preview.add(String.format("http://localhost:8080/v1/api/assess/%s/scores", crude.id));
            config.extra.put("id", crude.id);
            uploaded.config = config;
            return crude;
        } catch(IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }
}