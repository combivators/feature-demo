package net.tiny.feature.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.persistence.NoResultException;

import net.tiny.dao.AbstractService;
import net.tiny.dao.BaseService;
import net.tiny.dao.Dao;
import net.tiny.dao.IDao;
import net.tiny.feature.entity.Account;
import net.tiny.feature.entity.Group;
import net.tiny.feature.entity.Setting;
import net.tiny.feature.entity.Token;
import net.tiny.feature.entity.secure.Email;
import net.tiny.service.Callback;
import net.tiny.service.ServiceContext;
import net.tiny.ws.auth.Codec;
import net.tiny.ws.auth.JsonWebToken;

public class AccountService extends BaseService<Account> {

    private static final Logger LOGGER  = Logger.getLogger(AccountService.class.getName());

    public AccountService(ServiceContext c) {
        super(c, Account.class);
    }

    public AccountService(BaseService<?> base) {
        super(base, Account.class);
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
     * 根据用户名查找用户
     *
     * @param username
     *            用户名
     * @return 用户若不存在则返回null
     */
    public Optional<Account> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        try {
            Account account = dao()
                    .getNamedQuery("Account.findByUsername")
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(account);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * 根据邮箱查找用户
     *
     * @param mail
     *            邮箱
     * @return 邮箱若不存在则返回null
     */
    public Optional<Account> findByEmail(String mail) {
        if (mail == null) {
            return Optional.empty();
        }
        AbstractService<Email, String> service =
                new AbstractService<Email, String>(this, String.class, Email.class) {};
        Optional<Email> email = service.get(mail);
        if (!email.isPresent())
            return Optional.empty();
        return Optional.of(email.get().getAccount());
    }
    /**
     * 根据用户ID查找用户组
     *
     * @param id
     *            用户ID
     * @return 用户组
     */
    public List<String> findAuthorities(Long id) {
        List<String> authorities = new ArrayList<String>();
        Optional<Account> entity = get(id);
        if (entity.isPresent()) {
            for (Group group : entity.get().getGroups()) {
                authorities.addAll(group.getAuthorities());
            }
        }
        return authorities;
    }

    /**
     * 创建用户账号令牌
     *
     * @param id
     *            用户ID
     */
    public Optional<String> updateToken(Long id) {
        Optional<Account> entity = get(id);
        if (!entity.isPresent())
            return Optional.empty();
        Account account = entity.get();
        String secret = JsonWebToken.randomString(8, 16);
        final String paylod = String.format("{\"id\":%d,\"member\":\"%s\",\"sec\",\"%s\"}", account.getId(), account.getUsername(), secret);
        final String tokenId = Codec.encodeString(paylod.getBytes());
        final Token token = new Token();
        token.setId(tokenId);
        token.setType(Token.Type.member);
        token.setAccount(account);
        account.setToken(token);
        super.put(account);
        return Optional.of(tokenId);
    }

    /**
     * 根据令牌查找用户账号
     *
     * @param token
     *            令牌
     * @return 用户账号
     */
    public Optional<Account> findByToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        IDao<Token, String> tokenDao = new Dao<>(String.class, Token.class);
        tokenDao.setEntityManager(dao().getEntityManager());
        Optional<Token> entity = tokenDao.find(token);
        if (!entity.isPresent())
            return Optional.empty();
        Token t = entity.get();
        if (!Token.Type.member.equals(t.getType())) {
            return Optional.empty();
        }
        return Optional.of(t.getAccount());
    }

    /**
     * 根据令牌生成API的JWT
     *
     * @param token
     *            令牌
     * @return JWT
     */
    public Optional<JsonWebToken> jsonWebToken(String token) {
        Optional<Account> account = findByToken(token);
        if (!account.isPresent()) {
            return Optional.empty();
        }
        // Get account by user token
        Long id = account.get().getId();
        String name = account.get().getName();
        String scope = "member";
        final Setting setting = new SettingService(this).get();
        // Create a JWT
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("id", id);
        payload.put("user", account);
        payload.put("site", setting.getSiteName());
        payload.put("scope", scope);
        JsonWebToken jwt = new JsonWebToken.Builder()
                .signer(setting.getJwt(), new String(setting.getPrivateKey()))
                .notBefore(new Date())
                .subject("oauth")
                .issuer(setting.getSiteUrl())
                .audience(name)
                .jti(true)
                .build(payload);
        return Optional.of(jwt);
    }

    /**
     * 创建用户账号
     */
    public Optional<Account> create(String username, String password, String mail, String mobile, String ip, Consumer<Callback<String>> callback) {
        if (findByUsername(username).isPresent()) {
            if(null != callback) {
                callback.accept(Callback.failed("Existed user name"));
            }
            return Optional.empty();
        }
        if (mail != null && findByEmail(mail).isPresent()) {
            if(null != callback) {
                callback.accept(Callback.failed("Existed mail address"));
            }
            return Optional.empty();
        }
        /*
        if (mobile != null && findByMobile(mobile).isPresent()) {
            return Optional.empty();
        }
        */
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(Codec.md5(password));
        account.setLoginIp(ip);
        post(account);
        if (mail != null) {
            //随机生成4位验证号码
            String code = AccountService.randomNumeric(4);
            // 绑定用户邮箱
            email(account.getId(), mail, code, callback);
        }
        /*
        if (mobile != null) {
            //随机生成4位验证号码
            String code = AccountService.randomNumeric(4);
            // 绑定用户手机
            mobile(account.getId(), mobile, code);
        }
        */
        return Optional.of(account);
    }

    /**
     * 绑定用户邮箱
     *
     * @param id
     *            用户ID
     * @param mail
     *            邮箱
     * @param code
     *            随机4位验证号码
     * @return 邮箱
     */
    public Optional<Email> email(Long id, String mail, String code, Consumer<Callback<String>> callback) {
        Optional<Account> entity = get(id);
        if (!entity.isPresent()) {
            if(null != callback) {
                callback.accept(Callback.failed("Not found mail account - " + id));
            }
            return Optional.empty();
        }
        AbstractService<Email, String> service = new AbstractService<Email, String>(this, String.class, Email.class){};
        Optional<Email> existed = service.get(mail);
        Account account = entity.get();
        if (existed.isPresent()) {
            if (account.getId().equals(existed.get().getAccount().getId())) {
                // Had same mail
                if(null != callback) {
                    callback.accept(Callback.succeed("Using same mail address"));
                }
                return existed;
            } else {
                // Other account using this mail
                if(null != callback) {
                    callback.accept(Callback.failed("Other account using this mail : " + mail));
                }
                return Optional.empty();
            }
        }
        //TODO 邮箱变动
        Email email = new Email();
        email.setAddress(mail);
        //随机生成4位验证号码
        email.setCode(code);
        email.setAccount(account);
        account.setEmail(email);
        service.post(email);

        final Setting setting = new SettingService(this).get();
        String notice = String.format("%sapi/v1/account/activation/%s/%s",
                setting.getSiteUrl(),
                code,
                Codec.encodeString(mail.getBytes()));
        String msg = String.format("非同期发送确认邮件 To:%s 验证号码:[%s] 回调URL:%s", mail, code, notice);
        LOGGER.info(msg);
        //TODO 非同期激活邮件送信
        //new MailService(this).send(mail, "Activation", msg);

        if(null != callback) {
            callback.accept(Callback.succeed(msg));
        }
        return Optional.of(email);
    }

    /**
     * 激活用户邮箱
     *
     * @param code
     *            激活码
     * @return 邮箱
     */
    public Optional<Account> activation(String code, String token, Consumer<Callback<String>> callback) {
        if (token == null || code == null) {
            if(null != callback) {
                callback.accept(Callback.failed("Null code or token"));
            }
            return Optional.empty();
        }
        String mail = new String(Codec.decodeString(token));
        AbstractService<Email, String> service =
                new AbstractService<Email, String>(this, String.class, Email.class) {};
        Optional<Email> entity = service.get(mail);
        if (!entity.isPresent()) {
            if(null != callback) {
                callback.accept(Callback.failed("Not found " + mail));
            }
            return Optional.empty();
        }
        Email email = entity.get();
        if (!code.equals(email.getCode())) {
            if(null != callback) {
                callback.accept(Callback.failed("Invaild code " + code));
            }
            return Optional.empty();
        }
        email.setIsEnabled(true);
        email.setCode(null);
        Account account = email.getAccount();
        if (!account.getIsEnabled()) {
            account.setIsEnabled(true);
        }
        service.put(email);

        //TODO 非同期账户注册邮件送信
        //new MailService(this).send(mail, "Register", msg);
        return Optional.of(account);
    }

    private static final char[] NUMERIC_SYMBOLS = "0123456789".toCharArray();
    /**
     * Generate a random string.
     */
    public static String randomNumeric(int length) {
        Random random = ThreadLocalRandom.current();
        char[] buf = new char[length];
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = NUMERIC_SYMBOLS[random.nextInt(NUMERIC_SYMBOLS.length)];
        return new String(buf);
    }
}
