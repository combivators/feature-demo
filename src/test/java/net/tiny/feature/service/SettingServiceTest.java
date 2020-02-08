package net.tiny.feature.service;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import net.tiny.dao.CSVLoader;
import net.tiny.feature.entity.Setting;
import net.tiny.feature.service.SettingService;
import net.tiny.unit.db.Database;

@Database(persistence="persistence-test.properties"
  ,createScript="src/test/resources/sql/create_sequence.sql")
public class SettingServiceTest {

    @PersistenceContext(unitName = "persistenceUnit")
    private EntityManager manager;

    @Resource
    private DataSource dataSource;

    @Test
    public void testUpdateSetting() throws Exception {
        assertNotNull(dataSource);
        assertNotNull(manager);

        CSVLoader.Options options = new CSVLoader.Options("src/test/resources/csv/setting.csv", "setting")
                .truncated(true)
                .skip(1);
        Connection conn = dataSource.getConnection();
        CSVLoader.load(conn, options);
        conn.close();


        manager.getTransaction().begin();
        SettingService service = new SettingService(manager);
        Setting setting = service.get();
        assertNotNull(setting);
        assertNull(setting.getPublicKey());
        service.setTokenKey("HS256");

        service = new SettingService(manager);
        setting = service.get();
        assertNotNull(new String(setting.getPublicKey()));
        assertEquals(new String(setting.getPublicKey()), new String(setting.getPrivateKey()));

        manager.getTransaction().commit();

    }
}
