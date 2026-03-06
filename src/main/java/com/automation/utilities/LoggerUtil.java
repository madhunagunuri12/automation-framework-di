package com.automation.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggerUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerUtil.class);

    private LoggerUtil() {
    }

    public static void info(String message) {
        LOGGER.info(message);
        StepLogBuffer.append("INFO: " + message);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
        StepLogBuffer.append("WARN: " + message);
    }

    public static void error(String message) {
        LOGGER.error(message);
        StepLogBuffer.append("ERROR: " + message);
    }

    public static void error(String message, Throwable t) {
        LOGGER.error(message, t);
        StepLogBuffer.append("ERROR: " + message + " - " + t.getMessage());
    }
}
