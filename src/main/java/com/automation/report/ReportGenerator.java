package com.automation.report;

import com.automation.rerun.RerunManager;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerator.class);

    private static final JsonReportProcessor JSON_PROCESSOR = new CucumberJsonReportProcessor();
    private static final JsonReportMerger JSON_MERGER = new CucumberJsonReportMerger();
    private static final RerunTaskExecutor RERUN_TASK_EXECUTOR = new GradleRerunTaskExecutor();

    private ReportGenerator() {
    }

    public static void generateReport(List<String> jsonPaths, String outputDir) {
        if (System.getProperty("rerun.execution") != null) {
            return;
        }

        List<String> existingPaths = existingJsonPaths(jsonPaths);
        if (existingPaths.isEmpty()) {
            LOGGER.warn("No Cucumber JSON file(s) found.");
            return;
        }

        File tempFirstRunDir = new File("build/temp-first-run-jsons");
        List<String> firstRunJsonPaths = new ArrayList<String>();

        try {
            List<String> failedScenarios = RerunManager.getFailedScenarios(existingPaths);
            int totalScenarios = RerunManager.getTotalScenariosCount(existingPaths);

            if (failedScenarios.isEmpty()) {
                LOGGER.info("All tests passed. Generating final report from first run.");
                JSON_PROCESSOR.process(existingPaths);
                generateHtmlReport(existingPaths, outputDir);
                return;
            }

            double failureRate = ((double) failedScenarios.size() / totalScenarios) * 100;
            LOGGER.info("Failure Rate: {}% ({} failed out of {})",
                    String.format("%.2f", failureRate), failedScenarios.size(), totalScenarios);

            copyFirstRunJsons(existingPaths, tempFirstRunDir, firstRunJsonPaths);
            JSON_PROCESSOR.process(firstRunJsonPaths);
            generateHtmlReport(firstRunJsonPaths, "build/first-run-report");

            if (failureRate > 20.0d) {
                LOGGER.warn("Failure rate > 20%. Skipping rerun.");
                generateHtmlReport(existingPaths, outputDir);
                cleanupTempFiles(firstRunJsonPaths, tempFirstRunDir);
                return;
            }

            RerunManager.prepareRerunFiles(failedScenarios);
            deleteFiles(existingPaths);

            LOGGER.info("Executing rerun task...");
            int rerunExitCode = RERUN_TASK_EXECUTOR.executeRerunTask();
            LOGGER.info("Rerun task completed with exit code: {}", rerunExitCode);

            List<String> rerunJsonPaths = discoverRerunJsonPaths();
            if (rerunJsonPaths.isEmpty()) {
                LOGGER.error("No rerun JSONs found. Using first run data for final report.");
                generateHtmlReport(firstRunJsonPaths, outputDir);
                cleanupTempFiles(firstRunJsonPaths, tempFirstRunDir);
                return;
            }

            File finalJsonFile = new File(outputDir, "cucumber-final.json");
            new File(outputDir).mkdirs();
            JSON_MERGER.merge(firstRunJsonPaths, rerunJsonPaths, finalJsonFile);

            List<String> finalReportPaths = new ArrayList<String>();
            finalReportPaths.add(finalJsonFile.getAbsolutePath());
            JSON_PROCESSOR.process(finalReportPaths);
            generateHtmlReport(finalReportPaths, outputDir);

            cleanupTempFiles(firstRunJsonPaths, tempFirstRunDir);
        } catch (Exception e) {
            LOGGER.error("Error during report generation", e);
            cleanupTempFiles(firstRunJsonPaths, tempFirstRunDir);
        }
    }

    private static List<String> existingJsonPaths(List<String> jsonPaths) {
        List<String> existingPaths = new ArrayList<String>();
        for (String path : jsonPaths) {
            if (new File(path).exists()) {
                existingPaths.add(path);
            }
        }
        return existingPaths;
    }

    private static void copyFirstRunJsons(List<String> existingPaths, File tempFirstRunDir, List<String> output)
            throws Exception {
        if (!tempFirstRunDir.exists()) {
            tempFirstRunDir.mkdirs();
        }

        for (String path : existingPaths) {
            File original = new File(path);
            File copy = new File(tempFirstRunDir, "first-run-" + original.getName());
            Files.copy(original.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            output.add(copy.getAbsolutePath());
        }
    }

    private static void deleteFiles(List<String> paths) {
        for (String path : paths) {
            new File(path).delete();
        }
    }

    private static List<String> discoverRerunJsonPaths() {
        List<String> rerunJsonPaths = new ArrayList<String>();
        File buildDir = new File("build");
        File[] newFiles = buildDir.listFiles((dir, name) -> name.endsWith(".json") && !name.contains("first-run"));
        if (newFiles != null) {
            for (File file : newFiles) {
                if (file.length() > 0) {
                    rerunJsonPaths.add(file.getAbsolutePath());
                }
            }
        }
        return rerunJsonPaths;
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
}
