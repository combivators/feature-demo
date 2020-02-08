package net.tiny.feature.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.LogManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.tiny.ws.AccessLogger;
import net.tiny.ws.EmbeddedServer;
import net.tiny.ws.ResourceHttpHandler;
import net.tiny.ws.WebServiceHandler;
import net.tiny.ws.mvc.HtmlRenderer;
import net.tiny.ws.mvc.ViewRenderer;
import net.tiny.ws.rs.RestApplication;
import net.tiny.ws.rs.RestServiceLocator;
import net.tiny.ws.rs.RestfulHttpHandler;
import net.tiny.ws.rs.client.RestClient;

public class WelcomeServiceTest {

    static int port;
    static EmbeddedServer server;

    @BeforeAll
    public static void setUp() throws Exception {
        LogManager.getLogManager().readConfiguration(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties"));

        RestServiceLocator context = new RestServiceLocator();
        PropertiesEditor editor = new PropertiesEditor();
        editor.setPath("src/test/resources/data");
        context.bind("editor", editor, true);

        final RestApplication application = new RestApplication();
        application.setPattern("net.tiny.message.*, net.tiny.feature.*, !com.sun.*, !org.junit.*,");
        application.setScan(".*/classes/, .*/test-classes/, .*/feature-.*[.]jar, .*/tiny-.*[.]jar, !.*/tiny-dic.*[.]jar");
        context.bind("application", application, true);

        final ViewRenderer renderer = new HtmlRenderer();
        context.bind("renderer", renderer, true);

        final RestfulHttpHandler rest = new RestfulHttpHandler();
        rest.path("/");
        rest.setRenderer(renderer);
        rest.setContext(context);
        rest.setupRestServiceFactory();

        final AccessLogger logger = new AccessLogger();
        final WebServiceHandler restful = rest.filters(Arrays.asList(logger));

        final ResourceHttpHandler resourceHandler = new ResourceHttpHandler();
        resourceHandler.setInternal(true);
        resourceHandler.setPaths(Arrays.asList("/:home"));

        final WebServiceHandler resource = resourceHandler.path("/")
                    .filters(Arrays.asList(logger));


        server = new EmbeddedServer.Builder()
                .random()
                .handlers(Arrays.asList(restful, resource))
                .build();

        port = server.port();
        server.listen(callback -> {
            if(callback.success()) {
                System.out.println("Server listen on port: " + port);
            } else {
                callback.cause().printStackTrace();
            }
        });
    }

    @AfterAll
    public static void tearDown() throws Exception {
        server.close();
    }

    @Test
    public void testGetWelcomePage() throws Exception {
        RestClient client = new RestClient.Builder()
                .userAgent(RestClient.BROWSER_AGENT)
                .build();

        // Test GET
        RestClient.Response response = client.doGet(new URL("http://localhost:" + port +"/ui/index"));
        assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        assertTrue(response.hasEntity());
        String body = response.getEntity();
        assertNotNull(body);
        System.out.println(body);
        assertTrue(body.contains("<link href=\"/css/style.css\" rel=\"stylesheet\" type=\"text/css\" />"));
        assertTrue(body.contains("<meta name=\"author\" content=\"combivators\">"));
        assertTrue(body.contains("<script src=\"/js/script.js\"></script>"));
        assertTrue(body.contains("<h4>Hello, Welcome to feature platform</h4>"));
        response.close();

    }
}
