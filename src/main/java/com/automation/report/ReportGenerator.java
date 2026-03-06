package com.automation.report;

import com.automation.rerun.RerunManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerator.class);

    private ReportGenerator() {
    }

    public static void generateReport(List<String> jsonPaths, String outputDir) {
        if (System.getProperty("rerun.execution") != null) {
            return;
        }

        List<String> existingPaths = new ArrayList<>();
        for (String path : jsonPaths) {
            if (new File(path).exists()) {
                existingPaths.add(path);
            }
        }

        if (existingPaths.isEmpty()) {
            LOGGER.warn("⚠️ No Cucumber JSON file(s) found.");
            return;
        }

        File tempFirstRunDir = new File("build/temp-first-run-jsons");
        List<String> firstRunJsonPaths = new ArrayList<>();

        try {
            // 1. Check for Failures FIRST
            List<String> failedScenarios = RerunManager.getFailedScenarios(existingPaths);
            int totalScenarios = RerunManager.getTotalScenariosCount(existingPaths);
            
            // CASE 1: ALL PASSED
            if (failedScenarios.isEmpty()) {
                LOGGER.info("✅ All tests passed. Generating single Final Report.");
                processJsonFiles(existingPaths);
                generateHtmlReport(existingPaths, outputDir);
                return;
            }

            // CASE 2/3: FAILURES DETECTED
            double failureRate = ((double) failedScenarios.size() / totalScenarios) * 100;
            LOGGER.info("📊 Failure Rate: {}% ({} failed out of {})",
                    String.format("%.2f", failureRate), failedScenarios.size(), totalScenarios);

            // 2. Generate First Run Report (Only if failures exist)
            LOGGER.info("Generating First Run Report (due to failures)...");
            if (!tempFirstRunDir.exists()) {
                tempFirstRunDir.mkdirs();
            }
            
            for (String path : existingPaths) {
                File original = new File(path);
                File copy = new File(tempFirstRunDir, "first-run-" + original.getName());
                Files.copy(original.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
                firstRunJsonPaths.add(copy.getAbsolutePath());
            }

            // Process copies for the report
            processJsonFiles(firstRunJsonPaths);
            String firstRunReportDir = "build/first-run-report";
            generateHtmlReport(firstRunJsonPaths, firstRunReportDir);
            LOGGER.info("✅ First Run Report available at: {}", new File(firstRunReportDir).getAbsolutePath());
            
            // NOTE: We do NOT delete tempFirstRunDir yet, we need it for merging!

            // 3. Check Eligibility for Rerun
            boolean isEligible = failureRate <= 20.0;

            if (!isEligible) {
                LOGGER.warn("⚠️ Failure rate > 20%. Skipping rerun.");
                // Generate final report using the COPIES (since originals might be touched/processed differently)
                // Or just use existingPaths if we haven't deleted them yet.
                generateHtmlReport(existingPaths, outputDir);
                // Cleanup
                cleanupTempFiles(firstRunJsonPaths, tempFirstRunDir);
                return;
            }

            LOGGER.info("🔁 Eligibility met. Proceeding with rerun...");

            // 4. Execute Rerun
            RerunManager.prepareRerunFiles(failedScenarios);
            
            // Delete original JSONs to force fresh generation for rerun
            for (String path : existingPaths) {
                new File(path).delete();
            }

            LOGGER.info("🔁 Executing rerun task...");
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;
            if (os.contains("win")) {
                processBuilder = new ProcessBuilder("cmd", "/c", "gradlew cucumberRerun");
            } else {
                processBuilder = new ProcessBuilder("./gradlew", "cucumberRerun");
            }
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();
            LOGGER.info("✅ Rerun completed.");

            // 5. Merge & Final Report
            List<String> rerunJsonPaths = new ArrayList<>();
            File buildDir = new File("build");
            File[] newFiles = buildDir.listFiles((dir, name) -> name.endsWith(".json") && !name.contains("first-run"));
            if (newFiles != null) {
                for (File f : newFiles) {
                    if (f.length() > 0) {
                        rerunJsonPaths.add(f.getAbsolutePath());
                    }
                }
            }

            if (rerunJsonPaths.isEmpty()) {
                LOGGER.error("❌ No rerun JSONs found! Using first run data for final report.");
                // Fallback: use the preserved first run copies
                generateHtmlReport(firstRunJsonPaths, outputDir);
                cleanupTempFiles(firstRunJsonPaths, tempFirstRunDir);
                return;
            }

            File finalJsonFile = new File(outputDir, "cucumber-final.json");
            new File(outputDir).mkdirs();

            // Merge using PRESERVED first run copies + new rerun JSONs
            mergeJsons(firstRunJsonPaths, rerunJsonPaths, finalJsonFile);

            List<String> finalReportList = new ArrayList<>();
            finalReportList.add(finalJsonFile.getAbsolutePath());
            
            processJsonFiles(finalReportList);
            generateHtmlReport(finalReportList, outputDir);
            LOGGER.info("✅ Final Report available at: {}", new File(outputDir).getAbsolutePath());
            
            // Cleanup
            cleanupTempFiles(firstRunJsonPaths, tempFirstRunDir);

        } catch (Exception e) {
            LOGGER.error("❌ Error during report generation", e);
            // Attempt cleanup
            cleanupTempFiles(firstRunJsonPaths, tempFirstRunDir);
        }
    }
    
    private static void cleanupTempFiles(List<String> files, File dir) {
        if (files != null) {
            for (String path : files) {
                new File(path).delete();
            }
        }
        if (dir != null && dir.exists()) {
            dir.delete();
        }
    }
    
    // ... (rest of methods) ...

    private static void mergeJsons(List<String> firstRunPaths, List<String> rerunPaths, File outputFile)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode finalFeatures = mapper.createArrayNode();
        Map<String, JsonNode> featureMap = new HashMap<>();

        // 1. Load Rerun Data (Run 2)
        for (String path : rerunPaths) {
            JsonNode root = mapper.readTree(new File(path));
            if (root.isArray()) {
                for (JsonNode feature : root) {
                    String uri = feature.get("uri").asText();
                    featureMap.put(uri, feature);
                }
            }
        }

        // 2. Load First Run Data (Run 1) - Add ONLY passed scenarios NOT in Run 2
        for (String path : firstRunPaths) {
            JsonNode root = mapper.readTree(new File(path));
            if (root.isArray()) {
                for (JsonNode feature : root) {
                    String uri = feature.get("uri").asText();
                    
                    if (featureMap.containsKey(uri)) {
                        JsonNode run2Feature = featureMap.get(uri);
                        ArrayNode run2Elements = (ArrayNode) run2Feature.get("elements");
                        List<String> run2Ids = new ArrayList<>();
                        if (run2Elements != null) {
                            for (JsonNode s : run2Elements) {
                                run2Ids.add(getScenarioId(s));
                            }
                        } else {
                            run2Elements = mapper.createArrayNode();
                            ((ObjectNode) run2Feature).set("elements", run2Elements);
                        }

                        JsonNode run1Elements = feature.get("elements");
                        if (run1Elements != null) {
                            for (JsonNode scenario : run1Elements) {
                                if (isScenarioPassed(scenario) && !run2Ids.contains(getScenarioId(scenario))) {
                                    run2Elements.add(scenario);
                                }
                            }
                        }
                    } else {
                        ObjectNode newFeature = feature.deepCopy();
                        ArrayNode newElements = mapper.createArrayNode();
                        JsonNode elements = feature.get("elements");
                        if (elements != null) {
                            for (JsonNode scenario : elements) {
                                if (isScenarioPassed(scenario)) {
                                    newElements.add(scenario);
                                }
                            }
                        }
                        if (newElements.size() > 0) {
                            newFeature.set("elements", newElements);
                            featureMap.put(uri, newFeature);
                        }
                    }
                }
            }
        }

        for (JsonNode feature : featureMap.values()) {
            finalFeatures.add(feature);
        }

        mapper.writeValue(outputFile, finalFeatures);
    }

    private static String getScenarioId(JsonNode scenario) {
        if (scenario.has("id")) {
            return scenario.get("id").asText();
        }
        if (scenario.has("line")) {
            return String.valueOf(scenario.get("line").asInt());
        }
        return scenario.get("name").asText();
    }

    private static void generateHtmlReport(List<String> jsonPaths, String outputDir) {
        File reportOutputDirectory = new File(outputDir);
        if (!reportOutputDirectory.exists()) {
            reportOutputDirectory.mkdirs();
        }
        
        Configuration config = new Configuration(reportOutputDirectory, "Selenium Cucumber Framework");
        config.setBuildNumber("1");
        config.addClassifications("Platform", System.getProperty("os.name", ""));
        config.addClassifications("Browser", System.getProperty("browser", "chrome"));

        ReportBuilder reportBuilder = new ReportBuilder(jsonPaths, config);
        reportBuilder.generateReports();
    }

    private static void processJsonFiles(List<String> jsonPaths) {
        ObjectMapper mapper = new ObjectMapper();
        for (String path : jsonPaths) {
            try {
                File jsonFile = new File(path);
                JsonNode root = mapper.readTree(jsonFile);
                boolean modified = false;
                if (root.isArray()) {
                    for (JsonNode feature : root) {
                        JsonNode elements = feature.get("elements");
                        if (elements != null && elements.isArray()) {
                            deduplicateElements((ArrayNode) elements);
                            for (JsonNode scenario : elements) {
                                removeBeforeStepHooks(scenario);
                                mergeAfterStepHooksToStep(scenario);
                                if (isScenarioPassed(scenario)) {
                                    removeEmbeddings(scenario);
                                }
                            }
                            modified = true;
                        }
                    }
                }
                if (modified) {
                    mapper.writeValue(jsonFile, root);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to process JSON: " + path, e);
            }
        }
    }

    private static void deduplicateElements(ArrayNode elements) {
        Map<String, JsonNode> unique = new LinkedHashMap<>();
        for (JsonNode node : elements) {
            unique.put(getScenarioId(node), node);
        }
        if (unique.size() < elements.size()) {
            elements.removeAll();
            unique.values().forEach(elements::add);
        }
    }

    private static void removeBeforeStepHooks(JsonNode scenario) {
        JsonNode steps = scenario.get("steps");
        if (steps != null && steps.isArray()) {
            for (JsonNode step : steps) {
                JsonNode before = step.get("before");
                if (before != null && before.isArray()) {
                    Iterator<JsonNode> iterator = before.iterator();
                    while (iterator.hasNext()) {
                        JsonNode hook = iterator.next();
                        if (shouldRemoveHook(hook, "com.automation.base.Hooks.beforeStep")) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    private static void mergeAfterStepHooksToStep(JsonNode scenario) {
        JsonNode steps = scenario.get("steps");
        if (steps != null && steps.isArray()) {
            for (JsonNode step : steps) {
                JsonNode after = step.get("after");
                if (after != null && after.isArray()) {
                    Iterator<JsonNode> iterator = after.iterator();
                    while (iterator.hasNext()) {
                        JsonNode hook = iterator.next();
                        if (shouldRemoveHook(hook, "com.automation.base.Hooks.afterStep")) {
                            if (hook.has("embeddings")) {
                                JsonNode hookEmbeddings = hook.get("embeddings");
                                if (hookEmbeddings.isArray() && !hookEmbeddings.isEmpty()) {
                                    ArrayNode stepEmbeddings = (ArrayNode) step.get("embeddings");
                                    if (stepEmbeddings == null) {
                                        stepEmbeddings = ((ObjectNode) step).putArray("embeddings");
                                    }
                                    stepEmbeddings.addAll((ArrayNode) hookEmbeddings);
                                }
                            }
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    private static boolean shouldRemoveHook(JsonNode hook, String hookMethodName) {
        JsonNode match = hook.get("match");
        if (match != null) {
            JsonNode location = match.get("location");
            if (location != null) {
                String loc = location.asText();
                return loc != null && loc.contains(hookMethodName);
            }
        }
        return false;
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
            JsonNode result = step.get("result");
            if (result != null) {
                String status = result.get("status").asText();
                if (!"passed".equals(status)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void removeEmbeddings(JsonNode scenario) {
        JsonNode steps = scenario.get("steps");
        if (steps != null && steps.isArray()) {
            for (JsonNode step : steps) {
                if (step instanceof ObjectNode) {
                    ((ObjectNode) step).remove("embeddings");
                }
                JsonNode after = step.get("after");
                if (after != null && after.isArray()) {
                    for (JsonNode hook : after) {
                        if (hook instanceof ObjectNode) {
                            ((ObjectNode) hook).remove("embeddings");
                        }
                    }
                }
            }
        }
        removeEmbeddingsFromHooks(scenario, "before");
        removeEmbeddingsFromHooks(scenario, "after");
    }

    private static void removeEmbeddingsFromHooks(JsonNode scenario, String hookType) {
        JsonNode hooks = scenario.get(hookType);
        if (hooks != null && hooks.isArray()) {
            for (JsonNode hook : hooks) {
                if (hook instanceof ObjectNode) {
                    ((ObjectNode) hook).remove("embeddings");
                }
            }
        }
    }
}
