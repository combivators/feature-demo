package net.tiny.feature.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.NoResultException;

import net.tiny.dao.BaseService;
import net.tiny.dao.Dao;
import net.tiny.dao.IDao;
import net.tiny.feature.entity.Admin;
import net.tiny.feature.entity.Role;
import net.tiny.feature.entity.Setting;
import net.tiny.feature.entity.Token;
import net.tiny.service.ServiceContext;
import net.tiny.ws.auth.Codec;
import net.tiny.ws.auth.JsonWebToken;

public class AdminService extends BaseService<Admin> {

    public AdminService(ServiceContext c) {
        super(c, Admin.class);
    }
    public AdminService(BaseService<?> base) {
        super(base, Admin.class);
    }
    /**
     * 判断用户名是否存在
     *
     * @param username
     *            用户名
     * @return 用户名是否存在
     */
    public boolean exists(String username) {
        return (null != findByUsername(username));
    }

    /**
     * 根据用户名查找管理员
     *
     * @param username
     *            用户名
     * @return 管理员若不存在则返回null
     */
    public Optional<Admin> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        try {
            Admin admin = dao()
                    .getNamedQuery("Admin.findByUsername")
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(admin);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * 根据管理户ID查找角色
     *
     * @param id
     *            用户ID
     * @return 用户角色
     */
    public List<String> findAuthorities(Long id) {
        List<String> authorities = new ArrayList<String>();
        Optional<Admin> entity = get(id);
        if (entity.isPresent()) {
            for (Role role : entity.get().getRoles()) {
                authorities.addAll(role.getAuthorities());
            }
        }
        return authorities;
    }

    /**
     * 创建管理账号令牌
     *
     * @param id
     *            用户ID
     */
    public Optional<String> updateToken(Long id) {
        Optional<Admin> entity = get(id);
        if (!entity.isPresent())
            return Optional.empty();
        Admin admin = entity.get();
        String secret = JsonWebToken.randomString(8, 16);
        final String paylod = String.format("{\"id\":%d,\"admin\":\"%s\",\"sec\",\"%s\"}",
                admin.getId(), admin.getUsername(), secret);
        final String tokenId = Codec.encodeString(paylod.getBytes());
        final Token token = new Token();
        token.setId(tokenId);
        token.setType(Token.Type.admin);
        token.setAdmin(admin);
        admin.setToken(token);
        super.put(admin);
        return Optional.of(tokenId);
    }

    /**
     * 根据令牌查找管理账号
     *
     * @param token
     *            令牌
     * @return 管理账号
     */
    public Optional<Admin> findByToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        IDao<Token, String> tokenDao = new Dao<>(String.class, Token.class);
        tokenDao.setEntityManager(dao().getEntityManager());
        Optional<Token> entity = tokenDao.find(token);
        if (!entity.isPresent())
            return Optional.empty();
        Token t = entity.get();
        if (!Token.Type.admin.equals(t.getType())) {
            return Optional.empty();
        }
        return Optional.of(t.getAdmin());
    }

    /**
     * 根据令牌生成API的JWT
     *
     * @param token
     *            令牌
     * @return JWT
     */
    public Optional<JsonWebToken> jsonWebToken(String token) {
        Optional<Admin> entity = findByToken(token);
        if (!entity.isPresent()) {
            return Optional.empty();
        }
        // Get account by user token
        Admin admin = entity.get();
        final Setting setting = service(SettingService.class).get();
        // Create a JWT
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("id", admin.getId());
        payload.put("user", admin.getUsername());
        payload.put("site", setting.getSiteName());
        payload.put("scope", "admin");
        JsonWebToken jwt = new JsonWebToken.Builder()
                .signer(setting.getJwt(), new String(setting.getPrivateKey()))
                .notBefore(new Date())
                .subject("oauth")
                .issuer(setting.getSiteUrl())
                .audience(admin.getUsername())
                .jti(true)
                .build(payload);
        return Optional.of(jwt);
    }

    /**
     * 创建管理员账号
     */
    public Optional<Admin> create(String username, String password, String mail, String ip) {
        if (findByUsername(username).isPresent()) {
            return Optional.empty();
        }
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(Codec.md5(password));
        admin.setEmail(mail);
        admin.setLoginIp(ip);
        post(admin);
        return Optional.of(admin);
    }
}
