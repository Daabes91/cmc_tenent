package com.clinic.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EncryptionUtil.
 */
class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;
    private static final String TEST_ENCRYPTION_KEY = "12345678901234567890123456789012"; // 32 bytes

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil(TEST_ENCRYPTION_KEY);
    }

    @Test
    void testEncryptAndDecrypt() {
        String plaintext = "test-client-secret-12345";

        String encrypted = encryptionUtil.encrypt(plaintext);
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);

        String decrypted = encryptionUtil.decrypt(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testEncryptProducesDifferentOutputEachTime() {
        String plaintext = "test-secret";

        String encrypted1 = encryptionUtil.encrypt(plaintext);
        String encrypted2 = encryptionUtil.encrypt(plaintext);

        // Due to random IV, encrypted values should be different
        assertNotEquals(encrypted1, encrypted2);

        // But both should decrypt to the same plaintext
        assertEquals(plaintext, encryptionUtil.decrypt(encrypted1));
        assertEquals(plaintext, encryptionUtil.decrypt(encrypted2));
    }

    @Test
    void testEncryptEmptyString() {
        String plaintext = "";

        String encrypted = encryptionUtil.encrypt(plaintext);
        assertNotNull(encrypted);

        String decrypted = encryptionUtil.decrypt(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testEncryptLongString() {
        String plaintext = "This is a very long client secret that contains many characters and should still be encrypted and decrypted correctly without any issues whatsoever.";

        String encrypted = encryptionUtil.encrypt(plaintext);
        assertNotNull(encrypted);

        String decrypted = encryptionUtil.decrypt(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testDecryptInvalidData() {
        assertThrows(RuntimeException.class, () -> {
            encryptionUtil.decrypt("invalid-encrypted-data");
        });
    }

    @Test
    void testConstructorWithInvalidKeyLength() {
        assertThrows(IllegalStateException.class, () -> {
            new EncryptionUtil("short-key");
        });
    }

    @Test
    void testConstructorWithEmptyKey() {
        assertThrows(IllegalStateException.class, () -> {
            new EncryptionUtil("");
        });
    }

    @Test
    void testConstructorWithNullKey() {
        assertThrows(IllegalStateException.class, () -> {
            new EncryptionUtil(null);
        });
    }
}
