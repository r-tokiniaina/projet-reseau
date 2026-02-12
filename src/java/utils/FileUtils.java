package utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import logging.Log;

public class FileUtils {

    public static String computeChecksum(java.io.File file) throws IOException {
        // Source: https://stackoverflow.com/a/32032908
        byte[] buffer = new byte[8192];
        int count;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            Log.error("Unable to compute the checksum: " + e.getMessage());
            return null;
        }
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
        }

        byte[] hash = digest.digest();
        return Base64.getEncoder().encodeToString(hash);
    }
}
