package com.automation.transformers;

import com.automation.core.logging.LoggerUtil;
import java.util.HashMap;
import java.util.Map;

public final class TestDataStore {
    private static final ThreadLocal<Map<String, Object>> DATA = ThreadLocal.withInitial(HashMap::new);

    private TestDataStore() {
    }

    public static void put(String key, Object value) {
        LoggerUtil.info("TestDataStore: PUT " + key + " = " + value);
        DATA.get().put(key, value);
    }

    public static Object get(String key) {
        Object value = DATA.get().get(key);
        LoggerUtil.info("TestDataStore: GET " + key + " = " + value);
        return value;
    }

    public static void clear() {
        DATA.get().clear();
    }

    public static void clearAndRemove() {
        DATA.remove();
    }
}
