package com.automation.core.logging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class LogContext {

    private static final ThreadLocal<Map<String, String>> CONTEXT =
            ThreadLocal.withInitial(HashMap::new);

    private LogContext() {
    }

    public static void put(String key, String value) {
        if (key != null && value != null) {
            CONTEXT.get().put(key, value);
        }
    }

    public static String get(String key) {
        return CONTEXT.get().get(key);
    }

    public static Map<String, String> snapshot() {
        return Collections.unmodifiableMap(new HashMap<String, String>(CONTEXT.get()));
    }

    public static void clear() {
        CONTEXT.get().clear();
    }
}
