package net.tiny.feature.demo.isj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PrefectureCodeTest {

    @Test
    public void testGetWebPage() throws Exception {
        PrefectureCode pc = PrefectureCode.valueOf(1);
        assertNotNull(pc);
        assertEquals("01", pc.code());
        assertEquals("北海", pc.name());
        assertEquals("北海道", pc.localName());
    }
}
