package com.lucene.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;

public class FileUtil {
    /**
     * Generates the index path for a given source directory.
     * The index will be stored in "LucidSearch/data/[encoded-directory-name]"
     *
     */
    public static Path getIndexPath(String sourceDirectory) {
        try {
            // Create a safe directory name based on the source path
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sourceDirectory.getBytes(StandardCharsets.UTF_8));
            String encodedName = Base64.getUrlEncoder().encodeToString(hash).substring(0, 20); // Take first 20 chars

            // Base directory is the project root/data
            String baseDir = Paths.get(System.getProperty("user.dir"), "data").toString();
            return Paths.get(baseDir, encodedName);
        } catch (Exception e) {
            throw new RuntimeException("Error generating index path", e);
        }
    }

    public static boolean isValidDirectory(String path) {
        return path != null && Files.isDirectory(Paths.get(path));
    }
}
