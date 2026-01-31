package com.example.ragchat.crypto;

import com.example.ragchat.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class CryptoConfig {

    @Bean
    public CryptoService cryptoService(AppProperties props) {
        if (!props.getEncryption().isEnabled() || props.getEncryption().getProvider() == AppProperties.EncryptionProvider.NONE) {
            return new NoopCryptoService();
        }

        // LOCAL encryption (AES-GCM). In production, prefer envelope encryption with KMS.
        if (props.getEncryption().getProvider() == AppProperties.EncryptionProvider.LOCAL) {
            String b64 = props.getEncryption().getLocalKeyBase64();
            if (b64 == null || b64.isBlank()) {
                throw new IllegalStateException("APP_ENCRYPTION_ENABLED=true requires APP_ENCRYPTION_LOCAL_KEY_BASE64 for LOCAL provider");
            }
            byte[] key = Base64.getDecoder().decode(b64.trim());
            return new LocalAesGcmCryptoService(key);
        }

        // AWS_KMS placeholder: keep the code compile-safe, but recommend implementing envelope encryption using AWS KMS SDK.
        throw new IllegalStateException("AWS_KMS provider is not fully implemented in this case study. Use LOCAL or NONE.");
    }
}
