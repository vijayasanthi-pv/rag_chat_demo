package com.example.ragchat.crypto;

public class NoopCryptoService implements CryptoService {
    @Override public boolean enabled() { return false; }
    @Override public String encryptString(String plaintext) { return plaintext; }
    @Override public String decryptString(String ciphertext) { return ciphertext; }
}
