package com.automation.core.driver;

import java.util.Arrays;

public enum ExecutionMode {
    LOCAL("local"),
    REMOTE("remote");

    private final String value;

    ExecutionMode(String value) {
        this.value = value;
    }

    public static ExecutionMode from(String input) {
        if (input == null || input.trim().isEmpty()) {
            return LOCAL;
        }

        return Arrays.stream(values())
                .filter(mode -> mode.value.equalsIgnoreCase(input.trim()))
                .findFirst()
                .orElse(LOCAL);
    }
}
