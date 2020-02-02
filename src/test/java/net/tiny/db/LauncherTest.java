package net.tiny.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.logging.LogManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LauncherTest {

    @BeforeAll
    public static void beforeAll() throws Exception {
        LogManager.getLogManager()
            .readConfiguration(Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties"));
    }

    @Test
    public void testH2StartStop() throws Exception {
        Launcher launcher = new Launcher();
        H2Engine.Builder builder = launcher.getBuilder()
            .port(19001)
            .name("test_db")
            .changed("sa")
            .allow(true)
            .batch(true)
            .clear(true)
            .script("src/test/resources/sql/create_sequence.sql");
        assertEquals("jdbc:h2:tcp://127.0.0.1:19001/test_db", builder.toString());
        assertFalse(launcher.isStarting());

        Thread task = new Thread(launcher);
        task.start();
        Thread.sleep(1000L);
        assertTrue(launcher.isStarting());

        launcher.stop();

        Thread.sleep(1000L);
        assertFalse(launcher.isStarting());
    }
}
