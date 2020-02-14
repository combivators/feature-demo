package net.tiny.feature.consumer;

import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import net.tiny.config.JsonParser;
import net.tiny.feature.entity.Setting;
import net.tiny.feature.service.MailService;
import net.tiny.feature.service.SettingService;
import net.tiny.messae.api.Message;
import net.tiny.messae.api.MessageConsumer;
import net.tiny.service.ServiceContext;
import net.tiny.ws.auth.Codec;

@Path("/")
public class MailConsumer implements MessageConsumer {

    private static final Logger LOGGER = Logger.getLogger(MailConsumer.class.getName());

    @Resource
    private ServiceContext serviceContext;

    @POST
    @Path("api/v1/consumer/mail")
    @Produces(MediaType.APPLICATION_JSON)
    public void accept(@BeanParam Message message) {
        String json = new String(Codec.decodeString(message.getMessage()));
        LOGGER.info("[Consumer] /api/v1/consumer/mail " + json);
        Map<?,?> map = JsonParser.unmarshal(json, Map.class);
        MailService service = serviceContext.lookup(MailService.class);
        //service.send(map.get("to"), map.get("subject"), map.get("template"), param);
    }
}
