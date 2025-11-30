package com.clinic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for AES-256-GCM encryption and decryption.
 * Used to securely store sensitive data like PayPal client secrets.
 */
@Component
public class EncryptionUtil {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    private final SecretKey secretKey;

    public EncryptionUtil(@Value("${paypal.encryption.key:}") String encryptionKey) {
        if (encryptionKey == null || encryptionKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "PayPal encryption key not configured. Please set PAYPAL_ENCRYPTION_KEY environment variable."
            );
        }

        // Ensure the key is exactly 32 bytes for AES-256
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                "PayPal encryption key must be exactly 32 bytes (256 bits). Current length: " + keyBytes.length
            );
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        logger.info("EncryptionUtil initialized successfully");
    }

    /**
     * Encrypts a plaintext string using AES-256-GCM.
     *
     * @param plaintext The text to encrypt
     * @return Base64-encoded encrypted text with IV prepended
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String plaintext) {
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            // Encode to Base64
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            logger.error("Encryption failed", e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts a Base64-encoded encrypted string using AES-256-GCM.
     *
     * @param encryptedText Base64-encoded encrypted text with IV prepended
     * @return Decrypted plaintext
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String encryptedText) {
        try {
            // Decode from Base64
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decrypt
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (Exception e) {
            logger.error("Decryption failed", e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
}
