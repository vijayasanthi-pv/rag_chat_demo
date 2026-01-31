package com.det.ragchat.crypto;

import com.fasterxml.jackson.databind.JsonNode;

public interface CryptoService {
    boolean enabled();

    /** Encrypts plaintext; returns ciphertext with prefix. */
    String encryptString(String plaintext);

    /** Decrypts ciphertext (with prefix) to plaintext. */
    String decryptString(String ciphertext);

    default String encryptIfEnabled(String value) {
        if (!enabled() || value == null) return value;
        return encryptString(value);
    }

    default String decryptIfEncrypted(String value) {
        if (value == null) return null;
        if (!value.startsWith(CiphertextFormat.PREFIX)) return value;
        return decryptString(value);
    }

    /**
     * Context encryption scheme:
     * - If encryption enabled: store JSON object {"_enc":"enc:v1:<base64>"}.
     * - If not enabled: store original JSON.
     */
    default JsonNode encryptContextIfEnabled(JsonNode context) {
        return context;
    }

    default JsonNode decryptContextIfEncrypted(JsonNode context) {
        return context;
    }
}
