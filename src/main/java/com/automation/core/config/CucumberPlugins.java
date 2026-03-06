package com.automation.core.config;

public final class CucumberPlugins {
    // Prevent instantiation
    private CucumberPlugins() {
        throw new IllegalStateException("Utility class");
    }

    public static final String PRETTY = "pretty";
    public static final String HTML_REPORT = "html:build/cucumber-html-report";

    public static String fullPluginList(String jsonFileName) {

        System.out.println("DEBUG: rerun : status :  " + System.getProperty("rerun.execution"));
        boolean isRerun =
                System.getProperty("rerun.execution") != null;

        if (isRerun) {
            // Rerun output
            return String.join(",",
                    PRETTY,
                    HTML_REPORT,
                    "json:build/cucumber-rerun.json",
                    "rerun:build/rerun/cucumber-failures.txt" // Separate file for raw cucumber output
            );
        }

        // First run output
        return String.join(",",
                PRETTY,
                HTML_REPORT,
                "json:build/" + jsonFileName,
                "rerun:build/rerun/cucumber-failures.txt" // Separate file for raw cucumber output
        );
    }
}
