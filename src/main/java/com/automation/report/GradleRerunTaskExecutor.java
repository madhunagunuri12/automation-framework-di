package com.automation.report;

import java.util.ArrayList;
import java.util.List;

public class GradleRerunTaskExecutor implements RerunTaskExecutor {

    @Override
    public int executeRerunTask() throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase();
        List<String> command = new ArrayList<String>();

        if (os.contains("win")) {
            command.add("cmd");
            command.add("/c");
            command.add("gradlew.bat");
        } else {
            command.add("./gradlew");
        }

        command.add("cucumberRerun");
        appendSystemProp(command, "browser");
        appendSystemProp(command, "suiteFile");
        appendSystemProp(command, "execution");
        appendSystemProp(command, "grid.url");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        return process.waitFor();
    }

    private void appendSystemProp(List<String> command, String propName) {
        String value = System.getProperty(propName);
        if (value != null && !value.trim().isEmpty()) {
            command.add("-D" + propName + "=" + value.trim());
        }
    }
}
