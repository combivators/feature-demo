package net.tiny.feature.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.logging.Level;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import net.tiny.dao.EntityManagerProducer;
import net.tiny.feature.entity.Admin;
import net.tiny.service.ServiceLocator;
import net.tiny.unit.db.Database;

@Database(persistence="persistence-test.properties"
 ,createScript="src/test/resources/sql/create_sequence.sql"
 ,imports="src/test/resources/csv"
 )
public class AdminServiceTest {

    @Test
    public void testUpdateAdminToken() throws Exception {
        EntityManagerProducer producer = new EntityManagerProducer();
        producer.setLevel(Level.INFO);
        producer.setProfile("test");
        ServiceLocator context = new ServiceLocator();
        context.bind("producer", producer, true);

        EntityManager em = producer.getScoped(true);

        AdminService service = new AdminService(context);
        Optional<Admin> entity = service.get(1L);
        assertTrue(entity.isPresent());

        Optional<String> token = service.updateToken(1L);
        assertTrue(token.isPresent());
        String t = token.get();
        System.out.println(token.get());
        Optional<Admin> found = service.findByToken(t);
        assertTrue(found.isPresent());

        producer.dispose(em);
    }
}
