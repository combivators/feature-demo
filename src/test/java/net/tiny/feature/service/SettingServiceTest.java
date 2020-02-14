package net.tiny.feature.service;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.logging.Level;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import net.tiny.dao.CSVLoader;
import net.tiny.dao.EntityManagerProducer;
import net.tiny.feature.entity.Setting;
import net.tiny.feature.service.SettingService;
import net.tiny.service.ServiceLocator;
import net.tiny.unit.db.Database;

@Database(persistence="persistence-test.properties"
  ,createScript="src/test/resources/sql/create_sequence.sql")
public class SettingServiceTest {

    @Resource
    private DataSource dataSource;

    @Test
    public void testUpdateSetting() throws Exception {
        assertNotNull(dataSource);

        CSVLoader.Options options = new CSVLoader.Options("src/test/resources/csv/setting.csv", "setting")
                .truncated(true)
                .skip(1);
        Connection conn = dataSource.getConnection();
        CSVLoader.load(conn, options);
        conn.close();

        EntityManagerProducer producer = new EntityManagerProducer();
        producer.setLevel(Level.INFO);
        producer.setProfile("test");
        ServiceLocator context = new ServiceLocator();
        context.bind("producer", producer, true);

        //EntityManager em = producer.getScoped(true);
        //em.getTransaction().begin();
        SettingService service = new SettingService();
        service.setContext(context);
        Setting setting = service.get();
        assertNotNull(setting);
        assertNull(setting.getPublicKey());
        service.setTokenKey("HS256");

        service = new SettingService();
        service.setContext(context);
        setting = service.get();
        assertNotNull(new String(setting.getPublicKey()));
        assertEquals(new String(setting.getPublicKey()), new String(setting.getPrivateKey()));

        //em.getTransaction().commit();
        EntityManager em = producer.getScoped(false);
        producer.dispose(em);

    }
}
