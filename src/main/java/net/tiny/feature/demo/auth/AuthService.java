package net.tiny.feature.demo.auth;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.tiny.feature.dao.AdminDao;
import net.tiny.feature.entity.Admin;
import net.tiny.service.ServiceContext;
import net.tiny.ws.auth.Codec;
import net.tiny.ws.auth.JsonWebToken;
import net.tiny.ws.rs.Model;

@Path("/")
public class AuthService {

    static final String PUBLIC_KEY="-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuA9BNNJckSivg1J1tRotBVPFclNBfx13wIEMEGifOuLEcQpOoHLSwgN+zB5ZL78x9zYoqVxUodwt4QFPv/mgYwy5ZpU40/NeJSnMa2ZYoKba4PdlwD6VWNrH2MtoL9jFSOE8jDYEI8Zb7bwIatfQKxF2APS/ZyKhQmenXyCxY/E1qv6BzgjQZohLMraYXZJ7nAyvSW2bmNgWBkf6LyGwVFM4KzM222Pz7tXTT0iFP385nMisZ3Po43xUP5y8v/klF73M6El+fcE0U6O+V8eq9U2a5DgsiEL6IyoGI1+vy51SDvb9bUqGE8Yma3sTg1YUTtqjapPhXq0XpLW7tf9+2QIDAQAB\n-----END PUBLIC KEY-----";

    @Resource
    private ServiceContext serviceContext;

    private JsonWebToken.Builder builder;
    private String publicKey = PUBLIC_KEY;//TODO

    private long maxAge  = 86400L; //1 day

    @GET
    @Path("api/v1/auth/key")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Model getPublicKey() {
        Map<String, String> map = new HashMap<>();
        map.put("key", publicKey);
        return new Model(map).cache(maxAge);
    }

    @GET
    @Path("api/v1/auth/token/{token}")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Model token(@PathParam("token")String token) {
        if (null == token || token.isEmpty()) {
            return new Model(HttpURLConnection.HTTP_UNAUTHORIZED); // 401 : Unauthorized
        }
        // Get account by user token
        String account = "user@net.tiny";

        // Create a JWT
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("user", account);
        JsonWebToken jwt = builder
                .audience(account)
                .build(payload);
        return new Model(String.format("{\"token\":\"%s\"}", jwt.token()))
                   .cache(-1L);
    }

    private AdminDao adminDao;

    /**
     * 创建管理员账号
     */
    @POST
    @Path("api/v1/auth/admin")
    //@RolesAllowed(value = "admin:secret")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(value = MediaType.APPLICATION_JSON)
    public Model create(@BeanParam Admin admin) {
        /*
        if (!isValid(admin, Save.class)) {
            return new Model(null).status(HttpURLConnection.HTTP_BAD_REQUEST); // 400 : Bad request
        }
        */
        if (adminDao.exists(admin.getUsername())) {
            return new Model(null).status(HttpURLConnection.HTTP_CONFLICT); // 409 : Conflict
        }
        admin.setPassword(Codec.md5(admin.getPassword()));
        admin.setIsLocked(false);
        admin.setLoginFailureCount(0);
        adminDao.insert(admin);
        return new Model(null).status(HttpURLConnection.HTTP_CREATED); // 201 : Created
    }
}
