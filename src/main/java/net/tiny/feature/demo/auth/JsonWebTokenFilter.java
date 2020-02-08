package net.tiny.feature.demo.auth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import net.tiny.feature.service.SettingService;
import net.tiny.service.Patterns;
import net.tiny.ws.auth.JsonWebToken;

public class JsonWebTokenFilter extends Filter {

    private static Logger LOGGER = Logger.getLogger(JsonWebTokenFilter.class.getName());

    private String pattern = "/api/.*";
    private Patterns patterns;
    private String publicKey;

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String description() {
        return "Json Web Token auth filter";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        final String requestURI = exchange.getRequestURI().toString();
        boolean check = getPatterns().vaild(requestURI);
        LOGGER.info(String.format("[JWT] '%s' on %s is %s", requestURI, pattern, check));
        if (check) {
            // When HTTP path is requested to check JWT
            check = checkJsonWebToken(exchange);
            if (!check) {
                // 401 : Unauthorized
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, -1);
                return;
             }
        }
        if (null != chain) {
            chain.doFilter(exchange);
        }
    }

    private boolean checkJsonWebToken(HttpExchange he) {
        // See HTPP header "Authorization: Bearer {jwt}"
        final String auth = he.getRequestHeaders().getFirst("Authorization");
        if (null == auth) {
           return false;
        }
        String[] bearer  = auth.split(" ");
        if (bearer.length != 2 || !"Bearer".equalsIgnoreCase(bearer[0])) {
           return false;
        }
        final JsonWebToken token = JsonWebToken.valueOf(bearer[1]);
        LOGGER.info(String.format("[JWT] claims '%s'", token.claims()));
        return token.verify(cachedPublicKey(he));
    }

    private Patterns getPatterns() {
        if (patterns == null) {
            patterns = Patterns.valueOf(pattern);
        }
        return patterns;
    }

    private String cachedPublicKey(HttpExchange he) {
        if (publicKey == null) {
            final EntityManager em  = (EntityManager)he.getAttribute(EntityManager.class.getName());
            final SettingService service = new SettingService(em);
            publicKey = new String(service.get().getPublicKey());
        }
        return publicKey;
    }

}
