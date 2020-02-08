package net.tiny.feature.service;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.UUID;
import java.util.function.Supplier;

import javax.persistence.EntityManager;

import net.tiny.dao.BaseService;
import net.tiny.dao.Dao;
import net.tiny.dao.EntityManagerProducer;
import net.tiny.dao.IDao;
import net.tiny.feature.entity.Setting;
import net.tiny.service.ServiceContext;
import net.tiny.ws.auth.JsonWebToken;
import net.tiny.ws.auth.Keys;

public class SettingService implements Supplier<Setting> {

    private static final String DEFAULT_ID = "default";

    private final IDao<Setting, String> dao;

    public SettingService(ServiceContext context) {
        this(context.lookup(EntityManagerProducer.class).getScoped(false));
    }

    public SettingService(BaseService<?> base) {
        this(base.getContext());
    }

    public SettingService(EntityManager em) {
        dao = Dao.getDao(em, String.class, Setting.class);
    }

    @Override
    public Setting get() {
        return dao.find(DEFAULT_ID).get();
    }

    public void put(Setting entity) {
        dao.update(entity);
        dao.flush();
    }

    /**
     * 生成ETag码(UUID)
     * 	id(Long) + modifyTime(Long) + class name
     */
    public String createEntityTag(Setting entity) {
        final String className = entity.getClass().getName();
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES*2 + className.length());
        buffer.putLong(entity.getId().hashCode())
            .putLong(entity.getModified().getLong(ChronoField.MICRO_OF_SECOND))
            .put(className.getBytes());
        byte[] bytes = buffer.array();
        return UUID.nameUUIDFromBytes(bytes).toString();
    }

    /**
     * 生成令牌的密钥对
     *
     */
    public String setTokenKey(String algorithm) {
        Setting setting = get();
        String encodedPublicKey = "";
        String encodedPrivateKey = "";
        if (algorithm.startsWith("HS")) {
            String key = JsonWebToken.generateHMACKey(algorithm);
            encodedPublicKey  = key;
            encodedPrivateKey = key;
        } else {
            final KeyPair keyPair = JsonWebToken.generateKeyPair(algorithm);
            encodedPublicKey = Keys.encodeSSHPublicKey((PublicKey) keyPair.getPublic(), setting.getEmail());
            encodedPrivateKey = Keys.encodeKey(keyPair.getPrivate());
        }
        setting.setJwt(algorithm);
        setting.setPublicKey(encodedPublicKey.getBytes());
        setting.setPrivateKey(encodedPrivateKey.getBytes());
        setting.setUpdater("admin");
        setting.setUpdated("publicKey, privateKey");
        setting.setModified(LocalDateTime.now());
        put(setting);
        return encodedPublicKey;
    }


}
