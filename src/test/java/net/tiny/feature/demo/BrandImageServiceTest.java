package net.tiny.feature.demo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class BrandImageServiceTest {

    @Test
    public void testGenerateSvgImage() throws Exception {
        BrandImage brandImage = new BrandImage();
        brandImage.setPath("src/main/resources/home/fontawesome-free-5.11.2-web");
        String svg = new String(brandImage.generate("fa", "fa-recycle", "red", "gray"));
        System.out.println(svg);
        assertTrue(svg.contains("viewBox=\"0 0 512 512\""));
        assertTrue(svg.contains("fill:#dc3545"));
        assertTrue(svg.contains("background-color:#6c757d"));

        svg = new String(brandImage.generate("fas", "fa-user-circle", "blue", null));
        System.out.println(svg);
        assertTrue(svg.contains("viewBox=\"0 0 496 512\""));
        assertTrue(svg.contains("fill:#007bff"));
    }
}
