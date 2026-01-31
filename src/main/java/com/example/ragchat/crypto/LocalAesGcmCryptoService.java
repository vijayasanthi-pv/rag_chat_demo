package com.example.ragchat.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class LocalAesGcmCryptoService implements CryptoService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SecureRandom RNG = new SecureRandom();

    private static final int IV_LEN = 12;
    private static final int TAG_LEN_BITS = 128;

    private final SecretKey key;

    public LocalAesGcmCryptoService(byte[] keyBytes) {
        if (keyBytes == null || (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32)) {
            throw new IllegalArgumentException("Encryption key must be 16/24/32 bytes");
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public String encryptString(String plaintext) {
        try {
            byte[] iv = new byte[IV_LEN];
            RNG.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);

            return CiphertextFormat.PREFIX + Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    @Override
    public String decryptString(String ciphertext) {
        try {
            if (!ciphertext.startsWith(CiphertextFormat.PREFIX)) {
                return ciphertext;
            }
            String b64 = ciphertext.substring(CiphertextFormat.PREFIX.length());
            byte[] all = Base64.getDecoder().decode(b64);

            ByteBuffer buf = ByteBuffer.wrap(all);
            byte[] iv = new byte[IV_LEN];
            buf.get(iv);
            byte[] enc = new byte[buf.remaining()];
            buf.get(enc);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN_BITS, iv));
            byte[] plain = cipher.doFinal(enc);

            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    @Override
    public JsonNode encryptContextIfEnabled(JsonNode context) {
        if (context == null) return null;
        try {
            String json = MAPPER.writeValueAsString(context);
            String enc = encryptString(json);

            ObjectNode node = MAPPER.createObjectNode();
            node.put("_enc", enc);
            return node;
        } catch (Exception e) {
            throw new IllegalStateException("Context encryption failed", e);
        }
    }

    @Override
    public JsonNode decryptContextIfEncrypted(JsonNode context) {
        if (context == null || !context.isObject() || !context.has("_enc")) return context;
        try {
            String enc = context.get("_enc").asText();
            String json = decryptString(enc);
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Context decryption failed", e);
        }
    }
}
