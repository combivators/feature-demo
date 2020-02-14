package net.tiny.feature.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import net.tiny.dao.EntityManagerProducer;
import net.tiny.feature.entity.Account;
import net.tiny.feature.entity.secure.Email;
import net.tiny.service.ServiceLocator;
import net.tiny.unit.db.Database;
import net.tiny.ws.auth.Codec;

@Database(persistence="persistence-test.properties"
  ,createScript="src/test/resources/sql/create_sequence.sql"
  ,imports="src/test/resources/csv"
  )
public class AccountServiceTest {

    @Test
    public void testUpdateAccountToken() throws Exception {
        EntityManagerProducer producer = new EntityManagerProducer();
        producer.setLevel(Level.INFO);
        producer.setProfile("test");
        ServiceLocator context = new ServiceLocator();
        context.bind("producer", producer, true);

        SettingService settingService = new SettingService();
        settingService.setContext(context);
        context.bind("setting", settingService, true);

        EntityManager em = producer.getScoped(true);

        AccountService service = new AccountService(context);
        Optional<Account> entity = service.get(1L);
        assertTrue(entity.isPresent());

        Optional<String> token = service.updateToken(1L);
        assertTrue(token.isPresent());
        String t = token.get();
        System.out.println(token.get());
        Optional<Account> found = service.findByToken(t);
        assertTrue(found.isPresent());

        producer.dispose(em);
    }

    @Test
    public void testAppendEmail() throws Exception {
        EntityManagerProducer producer = new EntityManagerProducer();
        producer.setLevel(Level.INFO);
        producer.setProfile("test");
        ServiceLocator context = new ServiceLocator();
        context.bind("producer", producer, true);

        SettingService settingService = new SettingService();
        settingService.setContext(context);
        context.bind("setting", settingService, true);

        EntityManager em = producer.getScoped(true);

        AccountService service = new AccountService(context);
        Optional<Account> entity = service.get(1L);
        assertTrue(entity.isPresent());

        Account account = entity.get();

        //随机生成4位验证号码
        String code = AccountService.randomNumeric(4);
        String encoded = Codec.encodeString("hoge@company.com".getBytes());
        Optional<Email> email = service.email(account.getId(), "hoge@company.com", code
                , callback -> {
                    if(callback.success()) {
                        System.out.println(callback.result());
                        assertTrue(callback.result().contains(code));
                    } else {
                        Throwable err = callback.cause();
                        fail(err);
                    }
                });
        assertTrue(email.isPresent());
        assertEquals("hoge@company.com", email.get().getAddress());
        assertFalse(email.get().getIsEnabled());
        assertNotNull(email.get().getAccount());

        entity = service.findByEmail("hoge@company.com");
        assertTrue(entity.isPresent());

        entity = service.activation(code, encoded
                , callback -> {
                    if(callback.success()) {
                        System.out.println(callback.result());
                        assertTrue(callback.result().contains(code));
                    } else {
                        Throwable err = callback.cause();
                        fail(err);
                    }
                });
        account = entity.get();
        assertTrue(account.getIsEnabled());
        assertTrue(account.getEmail().getIsEnabled());

        producer.dispose(em);
    }

    @Test
    public void testRandomNumeric() throws Exception {
        int loop = 10000;
        List<String> list = new ArrayList<>();
        for (int i=0; i<loop; i++) {
            String r = AccountService.randomNumeric(4);
            assertFalse(list.contains(r));
        }
    }
}
