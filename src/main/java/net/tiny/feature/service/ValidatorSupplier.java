package net.tiny.feature.service;

import net.tiny.service.Patterns;
import net.tiny.ws.auth.JsonWebTokenValidator;

public class ValidatorSupplier extends JsonWebTokenValidator {

    private SettingService setting;

    @Override
    protected Patterns getPatterns() {
        return Patterns.valueOf(setting.get().getAuthPattern());
    }

    @Override
    protected String getPublicKey() {
        return new String(setting.get().getPublicKey());
    }
}
