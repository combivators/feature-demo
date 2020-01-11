package net.tiny.feature.web;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;

import org.junit.jupiter.api.Test;

public class PropertiesEditorTest {

    @Test
    public void testLocale() throws Exception {
        Locale locale = new Locale("en");
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("en", locale.toLanguageTag());
        assertEquals("en", locale.toString());

        locale = new Locale("en", "US");
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("en-US", locale.toLanguageTag());
        assertEquals("en_US", locale.toString());

        locale = Locale.forLanguageTag("en");
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());

        PropertiesEditor.Lang lang = PropertiesEditor.Lang.en;
        assertEquals("en_US", lang.toString());
    }

    @Test
    public void testResource() throws Exception {
        PropertiesEditor editor = new PropertiesEditor();
        editor.setPath("src/test/resources/data");
        String json = editor.treeView("en").json();
        System.out.println(json);
    }

}
