package by.shakhau.strpingsecurityjwt.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Random;

public final class HashUtils {

    public static String sha256(String text) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return HexFormat.of().formatHex(hash);
    }

    public static String generateSha256() {
        Random r = new Random();
        int len = 64;
        char[] sha256 = new char[len];
        for (int i = 0; i < len; i++) {
            int num = r.nextInt(62);
            char c;
            if (num < 10) {
                c = (char) (num + 48);
            } else if (num < 36) {
                c = (char) (num + 55);
            } else {
                c = (char) (num + 61);
            }
            sha256[i] = c;
        }
        return new String(sha256);
    }
}
