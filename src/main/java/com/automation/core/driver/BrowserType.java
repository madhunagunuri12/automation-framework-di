package com.automation.core.driver;

import java.util.Arrays;

public enum BrowserType {
    CHROME("chrome"),
    FIREFOX("firefox"),
    EDGE("edge"),
    HEADLESS_CHROME("headless-chrome"),
    HEADLESS_FIREFOX("headless-firefox");

    private final String value;

    BrowserType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static BrowserType from(String input) {
        if (input == null || input.trim().isEmpty()) {
            return CHROME;
        }

        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(input.trim()))
                .findFirst()
                .orElse(CHROME);
    }

    public boolean isHeadless() {
        return this == HEADLESS_CHROME || this == HEADLESS_FIREFOX;
    }
}
