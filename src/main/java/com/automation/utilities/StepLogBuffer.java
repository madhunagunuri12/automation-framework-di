package com.automation.utilities;

public final class StepLogBuffer {

    private static final ThreadLocal<StringBuilder> BUFFER = ThreadLocal.withInitial(StringBuilder::new);

    private StepLogBuffer() {
    }

    public static void append(String message) {
        BUFFER.get().append(message).append(System.lineSeparator());
    }

    public static String getLogs() {
        return BUFFER.get().toString();
    }

    public static void clear() {
        BUFFER.get().setLength(0);
    }
}
