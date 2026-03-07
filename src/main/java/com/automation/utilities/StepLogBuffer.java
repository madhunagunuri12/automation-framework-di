package com.automation.utilities;

@Deprecated
public final class StepLogBuffer {

    private StepLogBuffer() {
    }

    public static void append(String message) {
        com.automation.core.logging.StepLogBuffer.append(message);
    }

    public static String getLogs() {
        return com.automation.core.logging.StepLogBuffer.getLogs();
    }

    public static void clear() {
        com.automation.core.logging.StepLogBuffer.clear();
    }
}
