package com.clinic.security;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Locale;

@Component
public class TotpService {

    private static final long TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final Base32 BASE32 = new Base32();

    public boolean verifyCode(String secretBase32, String code) {
        if (secretBase32 == null || secretBase32.isBlank() || code == null) {
            return false;
        }

        String normalizedCode = code.replace(" ", "").trim();
        if (normalizedCode.length() != CODE_DIGITS) {
            return false;
        }

        long currentBucket = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        try {
            byte[] secret = BASE32.decode(secretBase32.toUpperCase(Locale.ROOT));
            for (long offset = -1; offset <= 1; offset++) {
                String expected = generateCode(secret, currentBucket + offset);
                if (expected.equals(normalizedCode)) {
                    return true;
                }
            }
            return false;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    private String generateCode(byte[] secret, long counter) throws GeneralSecurityException {
        byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret, "HmacSHA1"));
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0x0F;
        int binary =
                ((hash[offset] & 0x7F) << 24) |
                        ((hash[offset + 1] & 0xFF) << 16) |
                        ((hash[offset + 2] & 0xFF) << 8) |
                        (hash[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, CODE_DIGITS);
        return String.format("%0" + CODE_DIGITS + "d", otp);
    }
}
