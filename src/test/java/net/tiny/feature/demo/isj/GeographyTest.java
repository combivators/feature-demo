package net.tiny.feature.demo.isj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GeographyTest {

    @Test
    public void testDistance() throws Exception {
        assertNotNull(Geography.METER);
        assertEquals(1987.3842d, Geography.distance(35.1730990d, 136.883466d, 35.1855732d, 136.899092d), 0.0001);
        assertEquals(1987.3842d, Geography.distance(35.1855732d, 136.899092d, 35.1730990d, 136.883466d), 0.0001);

        assertEquals(1.9874d, Geography.distance(35.1730990d, 136.883466d, 35.1855732d, 136.899092d, 4));
        assertEquals(1.9874d, Geography.distance(35.1855732d, 136.899092d, 35.1730990d, 136.883466d, 4));
    }
}
