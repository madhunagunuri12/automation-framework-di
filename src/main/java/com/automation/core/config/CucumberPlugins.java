package com.automation.core.config;

import com.automation.core.logging.LoggerUtil;

public final class CucumberPlugins {
    private CucumberPlugins() {
        throw new IllegalStateException("Utility class");
    }

    public static final String PRETTY = "pretty";
    public static final String HTML_REPORT = "html:build/cucumber-html-report";
    public static final String RERUN_OUTPUT = "rerun:build/rerun/cucumber-failures.txt";

    public static String fullPluginList(String jsonFileName) {
        boolean isRerun = System.getProperty("rerun.execution") != null;
        LoggerUtil.info("Cucumber plugin mode: " + (isRerun ? "rerun" : "first-run"));

        if (isRerun) {
            return String.join(",",
                    PRETTY,
                    HTML_REPORT,
                    "json:build/cucumber-rerun.json",
                    RERUN_OUTPUT
            );
        }

        return String.join(",",
                PRETTY,
                HTML_REPORT,
                "json:build/" + jsonFileName,
                RERUN_OUTPUT
        );
    }
}
