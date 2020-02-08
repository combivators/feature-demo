package net.tiny.db;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;

import net.tiny.dao.DaoHelper;
import net.tiny.dao.EntityManagerProducer;
import net.tiny.ws.Callback;

public class H2Engine implements Closeable {
    private final static Logger LOGGER  = Logger.getLogger(H2Engine.class.getName());

    public static final int    H2_PORT = 9092;
    public static final String H2_DIR = ".";
    public static final String H2_DB  = "h2";
    public static final String H2_USER = "sa";

    private final Builder builder;
    private final CountDownLatch serverLock = new CountDownLatch(1);
    private Server h2db;
    private Throwable lastError = null;

    private H2Engine(Builder builder) throws SQLException {
        this.builder = builder;
        this.h2db = Server.createTcpServer(createArguments());
    }

    private String[] createArguments() {
        LinkedList<String> args = new LinkedList<>();
        args.add("-tcpPort");
        args.add(Integer.toString(builder.port));
        args.add("-baseDir");
        args.add(builder.base);
        if (builder.allow) {
            args.add("-tcpAllowOthers");
        }
        if (builder.create) {
            args.add("-ifNotExists");
        }
        return args.toArray(new String[args.size()]);
    }

    public void listen(Consumer<Callback<H2Engine>> consumer) {
        try {
            start();
            if(null != consumer) {
                consumer.accept(Callback.succeed(this));
            }
        } catch (Throwable e) {
            lastError = e;
            if(null != consumer) {
                consumer.accept(Callback.failed(e));
            }
        }
    }

    public boolean start() {
        try {
            //调用H2-DB的服务入口
            h2db.start();
            LOGGER.info("[H2] Database engine started. - " + getURL());

            if (builder.changed != null) {
                Connection conn = createConnection();
                DaoHelper.executeScript(conn, Arrays.asList(String.format("alter user sa set password '%s'", builder.changed)));
                conn.close();
                builder.password = builder.changed;
            }
            Connection conn = createConnection();
            if (builder.script != null) {
                List<String> scripts = Files.readAllLines(Paths.get(builder.script) , StandardCharsets.UTF_8);
                if (builder.batch)
                    DaoHelper.batchScript(conn, scripts);
                else
                    DaoHelper.executeScript(conn, scripts);
            }
            if (builder.producer != null) {
                EntityManager em = builder.producer.create();
                builder.producer.dispose(em);
            }
            if (builder.load != null) {
                DaoHelper.load(conn, builder.load);
            }
            conn.close();
        } catch (IOException | SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            lastError = ex;
        }
        return lastError != null;
    }

    public void stop() {
        close();
    }

    @Override
    public void close() {
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stopH2();
                } catch(Exception ex) {
                    lastError = ex;
                }
            }
        });
        task.start();
    }

    private void stopH2() {
        try {
            h2db.stop();
            int i=0;
            while(i<10 && isRunning()){
                i++;
                try{
                    Thread.sleep(500);
                } catch (Exception ex){
                }
            }
            if(builder.clear) {
                DeleteDbFiles.execute(builder.base, builder.name, true);
                LOGGER.info(String.format("[H2] Database file '%s/%s.db' was deleted.", builder.base, builder.name));
            }
        } finally {
            serverLock.countDown();
            LOGGER.info("[H2] Database engine stoped.");
        }
    }
    public boolean isRunning(){
        try{
            return h2db.isRunning(true);
        } catch(RuntimeException  e){
            return false;
        }
    }

    public boolean hasError() {
        return (null != lastError);
    }

    public Throwable getLastError() {
        return lastError;
    }

    public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        serverLock.await(timeout, unit);
    }

    public void awaitTermination() throws InterruptedException {
        serverLock.await();
    }

    public String getURL() {
        String url = "jdbc:h2:" + h2db.getURL() + "/" + builder.name;
        return url;
    }

    Connection createConnection() throws SQLException {
        String url = getURL();
        return DriverManager.getConnection(url, builder.user, builder.password);
    }

    public static class Builder {
        String bind = "127.0.0.1";
        int port = H2_PORT;
        String name = H2_DB;
        String base = H2_DIR;
        String user = H2_USER;
        String password = "";
        String changed = null; // Change a new password
        String script;
        String load;
        boolean clear = false; // Clear database file on shutdown
        boolean allow = false; // Start option --tcpAllowOthers
        boolean create = true; // Start option --ifNotExists
        boolean batch = false; // Run batch scripts
        EntityManagerProducer producer = null;

        public Builder bind(String ip) {
            bind = ip;
            return this;
        }
        public Builder port(int p) {
            port = p;
            return this;
        }
        public Builder name(String n) {
            name = n;
            return this;
        }
        public Builder base(String p) {
            if (!Files.exists(Paths.get(p))) {
                throw new IllegalArgumentException("Not found H2 base path : " + p);
            }
            base = p;
            return this;
        }
        public Builder user(String u) {
            user = u;
            return this;
        }
        public Builder password(String p) {
            password = p;
            return this;
        }
        public Builder script(String p) {
            if (!Files.exists(Paths.get(p))) {
                throw new IllegalArgumentException("Not found script path : " + p);
            }
            script =  p;
            return this;
        }
        public Builder load(String p) {
            if (!Files.exists(Paths.get(p))) {
                throw new IllegalArgumentException("Not found load CSV data path : " + p);
            }
            load = p;
            return this;
        }
        public Builder clear(boolean enable) {
            clear = enable;
            return this;
        }
        public Builder allow(boolean enable) {
            allow = enable;
            return this;
        }
        public Builder create(boolean enable) {
            create = enable;
            return this;
        }
        public Builder batch(boolean enable) {
            batch = enable;
            return this;
        }
        public Builder jpa(EntityManagerProducer p) {
            producer = p;
            return this;
        }
        public Builder changed(String p) {
            changed = p;
            return this;
        }
        @Override
        public String toString() {
            return String.format("jdbc:h2:tcp://%s:%d/%s", bind, port, name);
        }
        public H2Engine build() {
            try {
                return new H2Engine(this);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

}
