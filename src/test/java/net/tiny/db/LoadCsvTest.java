package net.tiny.db;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import net.tiny.dao.CSVLoader;
import net.tiny.unit.db.Database;

@Database(persistence="persistence-eclipselink.properties"
,trace=true
,before= {"create sequence xx_log_sequence increment by 1 start with 1;"
       ,"create sequence id_seq increment by 1 start with 0 NOCYCLE;"
}
)public class LoadCsvTest {

    @Resource
    private DataSource ds;

    @Test
    public void testTableOrdering() throws Exception {
        Connection conn = ds.getConnection();
    	CSVLoader.tableOrdering(conn, "src/test/resources/csv");
        conn.commit();
        conn.close();
    }
}
