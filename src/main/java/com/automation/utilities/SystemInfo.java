package com.automation.utilities;

public class SystemInfo {
    public static String getOS() {
        return System.getProperty("os.name");
    }

    public static String getOSVersion() {
        return System.getProperty("os.version");
    }

    public static String getArchitecture() {
        return System.getProperty("os.arch");
    }

    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    public static String getUserName() {
        return System.getProperty("user.name");
    }
}

