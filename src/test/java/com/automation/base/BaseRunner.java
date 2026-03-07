package com.automation.base;

import com.automation.core.config.ConfigReader;
import com.automation.core.config.CucumberPlugins;
import com.automation.core.logging.LoggerUtil;
import com.automation.report.ReportGenerator;
import com.automation.rerun.RerunManager;
import com.automation.retry.RetryListener;
import com.automation.retry.RetryTransformer;
import io.cucumber.testng.CucumberPropertiesProvider;
import io.cucumber.testng.TestNGCucumberRunner;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

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
            return new ArrayList<String>();
        }

        File[] files = buildDir.listFiles((dir, name) ->
                name.matches("cucumber.*\\.json") && !name.contains("first-run")
        );
        if (files == null || files.length == 0) {
            return new ArrayList<String>();
        }
        return Arrays.stream(files)
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * One JSON per test name so parallel runs do not overwrite. IDE run uses cucumber.json.
     */
    private static String jsonFileNameFromTest(XmlTest xmlTest) {
        if (xmlTest == null) {
            return "cucumber.json";
        }
        String name = xmlTest.getName();
        if (name == null || name.isEmpty() || "Command line test".equalsIgnoreCase(name)
                || "Gradle Test".equalsIgnoreCase(name)) {
            return "cucumber.json";
        }

        String sanitized = name.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "");
        return "cucumber-" + sanitized + ".json";
    }

    @BeforeClass(alwaysRun = true)
    public void setUpClass(org.testng.ITestContext context) {
        XmlTest xmlTest = context != null ? context.getCurrentXmlTest() : null;
        CucumberPropertiesProvider provider = key -> {
            if ("cucumber.plugin".equals(key)) {
                String jsonFileName = jsonFileNameFromTest(xmlTest);
                return CucumberPlugins.fullPluginList(jsonFileName);
            }

            String value = (xmlTest != null) ? xmlTest.getParameter(key) : null;
            if (value != null) {
                return value;
            }

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
        LoggerUtil.info("Before Suite - Loading Properties...");
        ConfigReader.loadProperties();

        if (System.getProperty("rerun.execution") == null) {
            LoggerUtil.info("Cleaning up previous run artifacts in build/...");
            RerunManager.cleanPreviousRunArtifacts();

            File buildDir = new File("build");
            if (buildDir.exists() && buildDir.isDirectory()) {
                File[] jsonFiles = buildDir.listFiles((dir, name) ->
                        name.startsWith("cucumber") && name.endsWith(".json")
                );
                if (jsonFiles != null) {
                    for (File file : jsonFiles) {
                        if (!file.delete()) {
                            LoggerUtil.warn("Failed to delete stale report json: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        if (System.getProperty("rerun.execution") != null) {
            LoggerUtil.info("Rerun execution detected - skipping report generation in AfterSuite.");
            return;
        }

        LoggerUtil.info("After Suite - Generating Report...");
        List<String> jsonPaths = discoverJsonPaths();
        if (jsonPaths.isEmpty()) {
            jsonPaths.add(REPORT_JSON_PATH);
        }
        ReportGenerator.generateReport(jsonPaths, REPORT_OUTPUT_DIR);
    }
}
