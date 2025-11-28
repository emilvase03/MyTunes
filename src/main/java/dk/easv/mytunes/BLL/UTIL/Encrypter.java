package dk.easv.mytunes.BLL.UTIL;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Encrypter {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH = 16;        // bytes
    private static final int ITERATIONS = 150000;     // upgraded for better security
    private static final int KEY_LENGTH = 256;        // bits

    public static String hashPassword(String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] salt = generateSalt();
        char[] pwChars = password.toCharArray();

        try {
            byte[] hash = pbkdf2(pwChars, salt, ITERATIONS, KEY_LENGTH);

            return Base64.getEncoder().encodeToString(salt)
                    + ":" +
                    Base64.getEncoder().encodeToString(hash);

        } finally {
            Arrays.fill(pwChars, '\0');
        }
    }

    public static boolean verifyPassword(String password, String stored)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        String[] parts = stored.split(":");
        if (parts.length != 2)
            return false;

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] storedHash = Base64.getDecoder().decode(parts[1]);

        char[] pwChars = password.toCharArray();

        try {
            byte[] testHash = pbkdf2(pwChars, salt, ITERATIONS, KEY_LENGTH);

            return slowEquals(storedHash, testHash);

        } finally {
            Arrays.fill(pwChars, '\0');
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
