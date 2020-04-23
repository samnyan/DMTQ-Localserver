package icu.samnya.dmtq_server.server.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
public class HashUtil {

    public static String getMd5(byte[] data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data);
            byte[] digiest = messageDigest.digest();
            return ByteUtil.bytesToHex(digiest);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
