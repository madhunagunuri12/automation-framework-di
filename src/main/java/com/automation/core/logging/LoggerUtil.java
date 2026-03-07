package com.automation.core.logging;

public final class LoggerUtil {

    private static final CentralLoggingService LOGGING_SERVICE = CentralLoggingService.getInstance();

    private LoggerUtil() {
    }

    public static void info(String message) {
        LOGGING_SERVICE.log(LogLevel.INFO, message);
    }

    public static void warn(String message) {
        LOGGING_SERVICE.log(LogLevel.WARN, message);
    }

    public static void debug(String message) {
        LOGGING_SERVICE.log(LogLevel.DEBUG, message);
    }

    public static void error(String message) {
        LOGGING_SERVICE.log(LogLevel.ERROR, message);
    }

    public static void error(String message, Throwable throwable) {
        LOGGING_SERVICE.log(LogLevel.ERROR, message, throwable);
    }
}
