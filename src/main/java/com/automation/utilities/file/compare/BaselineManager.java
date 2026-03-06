package com.automation.utilities.file.compare;

import com.automation.utilities.file.FileManager;

import java.io.File;

public final class BaselineManager {

    private static final String BASE_DIR =
            "baselines";

    private BaselineManager() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static void createBaseline(String actual,
                                      String name)
            throws Exception {

        File src = new File(actual);

        File dest =
                new File(BASE_DIR, name);

        if (!dest.exists()) {
            dest.mkdirs();
        }

        FileManager.copy(
                src.getAbsolutePath(),
                new File(dest,
                        src.getName()).getPath());
    }


    public static String getBaseline(String name,
                                     String file) {

        return BASE_DIR + "/" + name + "/" + file;
    }
}
