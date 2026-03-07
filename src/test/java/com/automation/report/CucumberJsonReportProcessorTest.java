package com.automation.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CucumberJsonReportProcessorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldRemoveCurrentBeforeHookAndMoveAfterEmbeddings() throws Exception {
        Path tempFile = Files.createTempFile("cucumber-json", ".json");

        String json = "[{\"uri\":\"feature1\",\"elements\":[{\"id\":\"s1\",\"name\":\"scenario\",\"steps\":[{" +
                "\"name\":\"step1\",\"result\":{\"status\":\"failed\"}," +
                "\"before\":[{\"match\":{\"location\":\"com.automation.steps.Hooks.beforeStep\"}}]," +
                "\"after\":[{\"match\":{\"location\":\"com.automation.steps.Hooks.afterStep\"},\"embeddings\":[{\"data\":\"abc\"}]}]" +
                "}]}]}]";

        Files.write(tempFile, json.getBytes(StandardCharsets.UTF_8));

        JsonReportProcessor processor = new CucumberJsonReportProcessor();
        processor.process(Collections.singletonList(tempFile.toString()));

        JsonNode root = objectMapper.readTree(tempFile.toFile());
        JsonNode step = root.get(0).get("elements").get(0).get("steps").get(0);

        Assert.assertEquals(step.get("before").size(), 0);
        Assert.assertEquals(step.get("after").size(), 0);
        Assert.assertNotNull(step.get("embeddings"));
        Assert.assertEquals(step.get("embeddings").size(), 1);

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void shouldDeduplicateScenarioElementsById() throws Exception {
        Path tempFile = Files.createTempFile("cucumber-json-dup", ".json");

        String json = "[{\"uri\":\"feature1\",\"elements\":[" +
                "{\"id\":\"duplicate\",\"name\":\"s1\",\"steps\":[{\"result\":{\"status\":\"passed\"}}]}," +
                "{\"id\":\"duplicate\",\"name\":\"s1-copy\",\"steps\":[{\"result\":{\"status\":\"passed\"}}]}" +
                "]}]";

        Files.write(tempFile, json.getBytes(StandardCharsets.UTF_8));

        JsonReportProcessor processor = new CucumberJsonReportProcessor();
        processor.process(Collections.singletonList(tempFile.toString()));

        JsonNode root = objectMapper.readTree(tempFile.toFile());
        JsonNode elements = root.get(0).get("elements");
        Assert.assertEquals(elements.size(), 1);

        Files.deleteIfExists(tempFile);
    }
}
