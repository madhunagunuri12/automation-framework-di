package com.automation.rerun;

import com.automation.core.logging.LoggerUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class RerunManager {

    private static final String RERUN_DIR = "build/rerun";
    private static final String MASTER_RERUN = "build/rerun/master.txt";
    private static final String FIRST_RUN_DIR = "build/first-run-report";

    private RerunManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void cleanPreviousRunArtifacts() {
        LoggerUtil.info("RerunManager: Starting cleanup in build/...");
        deleteDirectory(Paths.get(FIRST_RUN_DIR));
        deleteDirectory(Paths.get(RERUN_DIR));
        deleteDirectory(Paths.get("build/cucumber-reports"));
        deleteDirectory(Paths.get("build/first-run"));
        deleteDirectory(Paths.get("build/temp-first-run-jsons"));

        File zip = new File("build/first-run-report.zip");
        if (zip.exists() && !zip.delete()) {
            LoggerUtil.warn("RerunManager: Failed to delete old zip: " + zip.getAbsolutePath());
        }

        LoggerUtil.info("RerunManager: Cleanup completed.");
    }

    private static void deleteDirectory(Path path) {
        if (Files.exists(path)) {
            try {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                LoggerUtil.warn("Failed to delete path during cleanup: " + p);
                            }
                        });
            } catch (IOException e) {
                LoggerUtil.error("Failed to cleanup directory: " + path, e);
            }
        }
    }

    public static int getTotalScenariosCount(List<String> jsonPaths) {
        ObjectMapper mapper = new ObjectMapper();
        int total = 0;
        for (String path : jsonPaths) {
            File jsonFile = new File(path);
            if (!jsonFile.exists()) {
                continue;
            }
            try {
                JsonNode root = mapper.readTree(jsonFile);
                if (root.isArray()) {
                    for (JsonNode feature : root) {
                        JsonNode elements = feature.get("elements");
                        if (elements != null && elements.isArray()) {
                            for (JsonNode element : elements) {
                                if ("scenario".equals(element.get("type").asText())) {
                                    total++;
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                LoggerUtil.error("Failed to parse scenario count from json: " + path, e);
            }
        }
        return total;
    }

    public static List<String> getFailedScenarios(List<String> jsonPaths) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Boolean> scenarioStatus = new HashMap<String, Boolean>();

        for (String path : jsonPaths) {
            File jsonFile = new File(path);
            if (!jsonFile.exists()) {
                continue;
            }

            try {
                JsonNode root = mapper.readTree(jsonFile);
                if (root.isArray()) {
                    for (JsonNode feature : root) {
                        String uri = feature.get("uri").asText();
                        JsonNode elements = feature.get("elements");
                        if (elements != null && elements.isArray()) {
                            for (JsonNode element : elements) {
                                String key = uri + ":" + element.get("line").asInt();
                                boolean passed = isScenarioPassed(element);
                                if (scenarioStatus.getOrDefault(key, false)) {
                                    continue;
                                }
                                scenarioStatus.put(key, passed);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                LoggerUtil.error("Failed to parse failed scenarios from json: " + path, e);
            }
        }

        return scenarioStatus.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private static boolean isScenarioPassed(JsonNode scenario) {
        if (!areStepsPassed(scenario.get("before"))) {
            return false;
        }
        if (!areStepsPassed(scenario.get("steps"))) {
            return false;
        }
        return areStepsPassed(scenario.get("after"));
    }

    private static boolean areStepsPassed(JsonNode steps) {
        if (steps == null || !steps.isArray()) {
            return true;
        }
        for (JsonNode step : steps) {
            if (!isResultPassed(step.get("result"))) {
                return false;
            }
            if (!areStepsPassed(step.get("before"))) {
                return false;
            }
            if (!areStepsPassed(step.get("after"))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isResultPassed(JsonNode result) {
        if (result != null && result.has("status")) {
            return "passed".equals(result.get("status").asText());
        }
        return true;
    }

    public static void prepareRerunFiles(List<String> failedScenarios) throws IOException {
        Files.createDirectories(Paths.get(RERUN_DIR));
        Files.write(Paths.get(MASTER_RERUN), failedScenarios);
    }
}
