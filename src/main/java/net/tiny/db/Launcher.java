package net.tiny.db;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Launcher implements Runnable, AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());

    private H2Engine.Builder builder;
    private H2Engine server;

    public H2Engine.Builder getBuilder() {
        if (builder == null) {
            builder = new H2Engine.Builder();
        }
        return builder;
    }

    @Override
    public void run() {
        if (isStarting()) {
            LOGGER.warning(String.format("[BOOT] H2 Databse launcher already started!", builder.toString()));
            return;
        }

        server = builder.build();
        server.listen(callback -> {
            if(callback.success()) {
                LOGGER.info(String.format("[BOOT] H2 Databse '%s' launcher successful start.", builder.toString()));
                try {
                    server.awaitTermination();
                } catch (InterruptedException e) {
                }
                LOGGER.info(String.format("[BOOT] H2 Databse '%s' launcher stopped.", builder.toString()));
            } else {
                Throwable err = callback.cause();
                LOGGER.log(Level.SEVERE,
                        String.format("[BOOT] H2 Databse '%s' launcher startup failed - '%s'", builder.toString(), err.getMessage()), err);
            }
        });
    }

    public boolean isStarting() {
        return server != null && server.isRunning();
    }

    public void stop() {
        if (isStarting()) {
            server.stop();
            server = null;
        }
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    @Override
    public String toString() {
        return getBuilder().toString();
    }
}
