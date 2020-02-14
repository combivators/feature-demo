package net.tiny.feature.service;

import java.util.logging.Logger;

import net.tiny.config.JsonParser;
import net.tiny.messae.api.Message;
import net.tiny.messae.api.MessageConsumer;

public class LocalMailSender implements MessageConsumer {

    private static final Logger LOGGER = Logger.getLogger(LocalMailSender.class.getName());

    @Override
    public void accept(Message message) {
        LOGGER.info("[Consumer] LocalMailSender#accept " + JsonParser.marshal(message));
    }
}
