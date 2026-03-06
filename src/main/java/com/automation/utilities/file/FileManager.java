package com.automation.utilities.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public final class FileManager {

    private FileManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void createDir(String path)
            throws IOException {

        Files.createDirectories(Paths.get(path));
    }

    public static void delete(String path)
            throws IOException {

        Files.deleteIfExists(Paths.get(path));
    }

    public static boolean exists(String path) {

        return Files.exists(Paths.get(path));
    }

    public static File get(String path) {

        return new File(path);
    }

    public static void copy(String src,
                            String dest)
            throws IOException {

        Files.copy(Paths.get(src),
                Paths.get(dest),
                StandardCopyOption.REPLACE_EXISTING);
    }
}
