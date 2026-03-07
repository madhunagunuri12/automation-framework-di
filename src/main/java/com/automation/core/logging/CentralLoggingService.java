package com.automation.core.logging;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CentralLoggingService {

    private static final CentralLoggingService INSTANCE = new CentralLoggingService();
    private static final Logger LOGGER = LoggerFactory.getLogger("AutomationFramework");

    private CentralLoggingService() {
    }

    public static CentralLoggingService getInstance() {
        return INSTANCE;
    }

    public void log(LogLevel level, String message) {
        log(level, message, null);
    }

    public void log(LogLevel level, String message, Throwable throwable) {
        String formattedMessage = formatWithContext(message);

        switch (level) {
            case DEBUG:
                if (throwable == null) {
                    LOGGER.debug(formattedMessage);
                } else {
                    LOGGER.debug(formattedMessage, throwable);
                }
                break;
            case WARN:
                if (throwable == null) {
                    LOGGER.warn(formattedMessage);
                } else {
                    LOGGER.warn(formattedMessage, throwable);
                }
                break;
            case ERROR:
                if (throwable == null) {
                    LOGGER.error(formattedMessage);
                } else {
                    LOGGER.error(formattedMessage, throwable);
                }
                break;
            case INFO:
            default:
                if (throwable == null) {
                    LOGGER.info(formattedMessage);
                } else {
                    LOGGER.info(formattedMessage, throwable);
                }
                break;
        }

        appendStepBuffer(level, formattedMessage, throwable);
    }

    private String formatWithContext(String message) {
        String safeMessage = message == null ? "" : message;
        Map<String, String> context = LogContext.snapshot();
        String scenario = context.get("scenario");
        if (scenario == null || scenario.trim().isEmpty()) {
            return safeMessage;
        }
        return "[scenario=" + scenario + "] " + safeMessage;
    }

    private void appendStepBuffer(LogLevel level, String formattedMessage, Throwable throwable) {
        String suffix = throwable != null && throwable.getMessage() != null
                ? " - " + throwable.getMessage()
                : "";
        StepLogBuffer.append(level.name() + ": " + formattedMessage + suffix);
    }
}
