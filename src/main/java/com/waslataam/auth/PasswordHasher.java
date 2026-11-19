package com.waslataam.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * PBKDF2-HMAC-SHA256 password hashing with a per-account random salt.
 *
 * <p>Passwords are never stored or logged in plain text. The iteration count is
 * deliberately high enough to make offline guessing expensive while staying
 * responsive on a laptop.</p>
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public record HashedPassword(String hash, String salt) {
    }

    public static HashedPassword hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return new HashedPassword(derive(password, salt), Base64.getEncoder().encodeToString(salt));
    }

    public static boolean verify(String password, String expectedHash, String saltBase64) {
        if (password == null || expectedHash == null || saltBase64 == null) {
            return false;
        }
        byte[] salt;
        try {
            salt = Base64.getDecoder().decode(saltBase64);
        } catch (IllegalArgumentException e) {
            return false;
        }
        String actual = derive(password, salt);
        return MessageDigest.isEqual(
                actual.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                expectedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    private static String derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            byte[] key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(key);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 password hashing is unavailable", e);
        } finally {
            spec.clearPassword();
        }
    }
}
