package com.automation.base;

import com.automation.core.config.ConfigReader;
import com.automation.core.config.CucumberPlugins;
import com.automation.report.ReportGenerator;
import com.automation.rerun.RerunManager;
import com.automation.retry.RetryListener;
import com.automation.retry.RetryTransformer;
import io.cucumber.testng.CucumberPropertiesProvider;
import io.cucumber.testng.TestNGCucumberRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Listeners({
    RetryListener.class,
    RetryTransformer.class
})
public abstract class BaseRunner {

    private static final String REPORT_JSON_PATH = "build/cucumber.json";
    private static final String REPORT_OUTPUT_DIR = "build/cucumber-reports";

    private TestNGCucumberRunner testNGCucumberRunner;

    private static List<String> discoverJsonPaths() {
        File buildDir = new File("build");
        if (!buildDir.isDirectory()) {
            return new ArrayList<>();
        }
        // Match cucumber.json, cucumber-chrome-test.json, etc.
        // Exclude files that are artifacts of rerun logic (containing "first-run")
        // This prevents picking up stale files from previous runs or the renamed files from the current run logic
        File[] files = buildDir.listFiles((dir, name) ->
                name.matches("cucumber.*\\.json") && !name.contains("first-run")
        );
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }
        return Arrays.stream(files)
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * One JSON per test name so parallel runs don't overwrite. IDE run → cucumber.json.
     */
    private static String jsonFileNameFromTest(XmlTest xmlTest) {
        if (xmlTest == null) {
            return "cucumber.json";
        }
        String name = xmlTest.getName();
        if (name == null || name.isEmpty() || "Command line test".equalsIgnoreCase(name)
                || "Gradle Test".equalsIgnoreCase(name)) {
            // When running from Gradle command line without specific suite, it often defaults to "Gradle Test"
            // We want a consistent name.
            return "cucumber.json";
        }
        // "Chrome Test" → "cucumber-chrome-test.json"
        String sanitized = name.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "");
        return "cucumber-" + sanitized + ".json";
    }

    @BeforeClass(alwaysRun = true)
    public void setUpClass(org.testng.ITestContext context) {
        XmlTest xmlTest = context != null ? context.getCurrentXmlTest() : null;
        CucumberPropertiesProvider provider = key -> {
            // 1. Handle Plugin Logic
            if ("cucumber.plugin".equals(key)) {
                String jsonFileName = jsonFileNameFromTest(xmlTest);
                return CucumberPlugins.fullPluginList(jsonFileName);
            }

            // 2. Check TestNG XML Parameters
            String value = (xmlTest != null) ? xmlTest.getParameter(key) : null;
            if (value != null) {
                return value;
            }

            // 3. Check System Properties (Crucial for Rerun to pick up cucumber.features)
            return System.getProperty(key);
        };
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass(), provider);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (testNGCucumberRunner != null) {
            testNGCucumberRunner.finish();
        }
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "scenarios",
            retryAnalyzer = com.automation.retry.SmartRetryAnalyzer.class)
    public void scenario(io.cucumber.testng.PickleWrapper pickle, io.cucumber.testng.FeatureWrapper feature) {
        testNGCucumberRunner.runScenario(pickle.getPickle());
    }

    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return testNGCucumberRunner == null ? new Object[0][] : testNGCucumberRunner.provideScenarios();
    }

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        System.out.println("🔹 Before Suite - Loading Properties...");
        ConfigReader.loadProperties();

        // Clean up previous run artifacts (first_run folder, rerun folder, and stale JSONs)
        // Only if this is NOT a rerun execution itself
        if (System.getProperty("rerun.execution") == null) {
            System.out.println("🧹 Cleaning up previous run artifacts in build/...");
            RerunManager.cleanPreviousRunArtifacts();

            // Also clean up stale JSON files in build to prevent confusion
            File buildDir = new File("build");
            if (buildDir.exists() && buildDir.isDirectory()) {
                File[] jsonFiles = buildDir.listFiles((dir, name) ->
                        name.startsWith("cucumber") && name.endsWith(".json")
                );
                if (jsonFiles != null) {
                    for (File f : jsonFiles) {
                        f.delete();
                    }
                }
            }
        }
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        // Only generate report if this is NOT a rerun execution
        // The rerun task will handle its own reporting via ReportGenerator called from the main process
        if (System.getProperty("rerun.execution") != null) {
            System.out.println("📝 Rerun Execution - Skipping Report Generation in AfterSuite...");
            return;
        }

        System.out.println("📝 After Suite - Generating Report...");
        List<String> jsonPaths = discoverJsonPaths();
        if (jsonPaths.isEmpty()) {
            jsonPaths.add(REPORT_JSON_PATH);
        }
        ReportGenerator.generateReport(jsonPaths, REPORT_OUTPUT_DIR);
    }
}
