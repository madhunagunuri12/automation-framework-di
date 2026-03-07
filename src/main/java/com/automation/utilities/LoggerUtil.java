package com.automation.utilities;

@Deprecated
public final class LoggerUtil {

    private LoggerUtil() {
    }

    public static void info(String message) {
        com.automation.core.logging.LoggerUtil.info(message);
    }

    public static void warn(String message) {
        com.automation.core.logging.LoggerUtil.warn(message);
    }

    public static void debug(String message) {
        com.automation.core.logging.LoggerUtil.debug(message);
    }

    public static void error(String message) {
        com.automation.core.logging.LoggerUtil.error(message);
    }

    public static void error(String message, Throwable throwable) {
        com.automation.core.logging.LoggerUtil.error(message, throwable);
    }
}
