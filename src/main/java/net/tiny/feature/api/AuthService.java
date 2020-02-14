package net.tiny.feature.api;

import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import net.tiny.feature.entity.Account;
import net.tiny.feature.entity.Admin;
import net.tiny.feature.entity.Setting;
import net.tiny.feature.service.AccountService;
import net.tiny.feature.service.AdminService;
import net.tiny.feature.service.SettingService;
import net.tiny.service.ServiceContext;
import net.tiny.ws.auth.JsonWebToken;
import net.tiny.ws.rs.ApplicationException;
import net.tiny.ws.rs.Response;

@Path("/")
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private static final String KEY_FORMAT   = "{\"key\":\"%s\"}";
    private static final String TOKEN_FORMAT = "{\"token\":\"%s\"}";
    private static final String ADMIN_FORMAT      = "{\"id\":%d,\"name\":\"%s\",\"token\":\"%s\"}";
    private static final String ACCOUNT_FORMAT    = "{\"id\":%d,\"name\":\"%s\",\"mail\":\"%s\",\"msg\":\"%s\"}";
    private static final String ACTIVATION_FORMAT = "{\"id\":%d,\"name\":\"%s\",\"token\":\"%s\"}";

    @Resource
    private ServiceContext serviceContext;

    private long maxAge  = 86400L; //1 day

    /**
     * Open API
     * 获取检验令牌的公钥
     */
    @GET
    @Path("api/v1/auth/key")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response publicKey() {
        final SettingService service = serviceContext.lookup(SettingService.class);
        Setting setting = service.get();
        String key = new String(setting.getPublicKey());
        String etag = service.createEntityTag(setting);
        return Response.ok()
                .entity(String.format(KEY_FORMAT, key))
                .header("ETag", etag)
                .cache(maxAge)
                .build();
    }

    /**
     * Admin API
     * 更新生成令牌的密钥对
     */
    @POST
    @Path("api/v1/auth/keys")
    //@RolesAllowed(value = "admin:secret")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response keys(@BeanParam Map<?,?> param) {
        final SettingService service = serviceContext.lookup(SettingService.class);
        try {
            String key = service.setTokenKey(String.valueOf(param.get("alg")));
            return Response.ok()
                     .entity(String.format("{\"key\":\"%s\"}", key))
                     .build();
        } catch (RuntimeException e) {
            throw new ApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
        }

    }


    /**
     * Open API
     * 使用用户令牌获取API的JWT
     */
    @GET
    @Path("api/v1/auth/token/{token}")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response token(@PathParam("token")String token) {
        if (null == token || token.isEmpty()) {
            return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).build(); // 401 : Unauthorized
        }

        AdminService adminService = new AdminService(serviceContext);
        Optional<JsonWebToken> jwt = adminService.jsonWebToken(token);
        if (!jwt.isPresent()) {
            // Get account by user token
            AccountService accountService = new AccountService(serviceContext);
            jwt = accountService.jsonWebToken(token);
            if (!jwt.isPresent()) {
                return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).build(); // 401 : Unauthorized
            }
        }

        return Response.ok()
                .entity(String.format(TOKEN_FORMAT, jwt.get().token()))
                .cache(-1L)
                .build();
    }

    /**
     * Open API
     * 创建一般账号
     */
    @POST
    @Path("api/v1/account")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response account(@BeanParam Map<Object, Object> param, @Context String client) {
        final AccountService service = new AccountService(serviceContext);
        final String username = (String)param.get("name");
        final String mail = (String)param.get("mail");
        final String mobile = (String)param.get("mobile");
        final String password = (String)param.get("password");
        if (null == username || null == password || (null == mail && null== mobile)) {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build(); // 400 : Bad request
        }

        StringBuilder notice = new StringBuilder();
        Optional<Account> entity = service.create(username, password, mail, mobile, client.toString()
                , callback -> {
                    if(callback.success()) {
                        notice.append(callback.result());
                    } else {
                        Throwable err = callback.cause();
                        notice.append(err.getMessage());
                    }
                });
        if (!entity.isPresent()) {
            LOGGER.warning(String.format("[API] Auth POST '/api/v1/account' cause : %s", notice.toString()));
            return Response.status(HttpURLConnection.HTTP_CONFLICT).build(); // 409 : Conflict
        }
        final Account account = entity.get();
        final String json = String.format(ACCOUNT_FORMAT,
                account.getId(),
                account.getUsername(),
                account.getEmail().getAddress(),
                notice.toString());
        return Response.status(HttpURLConnection.HTTP_CREATED)
                .entity(json)
                .build(); // 201 : Created
    }



    /**
     * Open API
     * 激活用户账号
     */
    @GET
    @Path("api/v1/activation/{code}/{token}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response activation(@PathParam("code")String code, @PathParam("token")String token) {
        AccountService service = new AccountService(serviceContext);
        StringBuilder notice = new StringBuilder();
        Optional<Account> entity = service.activation(code, token
                , callback -> {
                    if(callback.success()) {
                        notice.append(callback.result());
                    } else {
                        Throwable err = callback.cause();
                        notice.append(err.getMessage());
                    }
                });
        if (!entity.isPresent()) {
            LOGGER.warning(String.format("[API] Auth POST '/api/v1/activation/' cause : %s", notice.toString()));
            return Response.status(HttpURLConnection.HTTP_NOT_FOUND).build(); // 404 : Not found
        }
        Account account = entity.get();
        Optional<String> userToken = service.updateToken(account.getId());
        final String json = String.format(ACTIVATION_FORMAT,
                account.getId(),
                account.getUsername(),
                userToken.get());
        return Response.ok()
                .entity(json)
                .build(); // 201 : Created
    }

    /**
     * Member API
     * 更新管理账号令牌
     */
    @PUT
    @Path("api/v1/auth/account/{id}/token")
    //@RolesAllowed(value = "admin:secret")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response accountToken(@PathParam("id")Long id) {
        AccountService service = new AccountService(serviceContext);
        Optional<String> token = service.updateToken(id);
        if (!token.isPresent()) {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build(); // 400 : Bad request
        }
        return Response.ok()
                .entity(String.format("{\"token\":\"%s\"}", token.get()))
                .cache(-1L)
                .build();
    }

    /**
     * Admin API
     * 更新管理账号令牌
     */
    @PUT
    @Path("api/v1/admin/{id}/token")
    //@RolesAllowed(value = "admin:secret")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response adminToken(@PathParam("id")Long id) {
        AdminService service = new AdminService(serviceContext);
        Optional<String> token = service.updateToken(id);
        if (!token.isPresent()) {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build(); // 400 : Bad request
        }
        return Response.ok()
                .entity(String.format("{\"token\":\"%s\"}", token.get()))
                .cache(-1L)
                .build();
    }

    // Admin API
    @PUT
    @Path("api/v1/setting")
    //@RolesAllowed(value = "admin:secret")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response setting(@BeanParam Setting setting) {
        /*
        if (!isValid(admin, Save.class)) {
            return new Model(null).status(HttpURLConnection.HTTP_BAD_REQUEST); // 400 : Bad request
        }
        */
        final SettingService service = serviceContext.lookup(SettingService.class);
        Setting source = service.get();
        source.setPublicKey(setting.getPublicKey());
        source.setUpdater("admin");
        source.setUpdated("publicKey"); //TODO
        source.setModified(LocalDateTime.now());
        service.put(source);
        return Response.ok().build();
    }

    /**
     * Admin API
     * 创建管理员账号
     */
    @POST
    @Path("api/v1/admin")
    //@RolesAllowed(value = "admin:secret")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response admin(@BeanParam Map<Object, Object> param, @Context String client) {
        AdminService service = new AdminService(serviceContext);
        String username = (String)param.get("name");
        String mail = (String)param.get("mail");
        String password = (String)param.get("password");
        if (null == username || null == password || null == mail) {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build(); // 400 : Bad request
        }
        Optional<Admin> entity = service.create(username, password, mail, client.toString());
        if (!entity.isPresent()) {
            return Response.status(HttpURLConnection.HTTP_CONFLICT).build(); // 409 : Conflict
        }
        Admin admin = entity.get();
        Optional<String> adminToken  = service.updateToken(admin.getId());
        final String json = String.format(ADMIN_FORMAT,
                admin.getId(),
                admin.getUsername(),
                adminToken.get());
        return Response.status(HttpURLConnection.HTTP_CREATED)
                .entity(json)
                .build(); // 201 : Created
    }


}
