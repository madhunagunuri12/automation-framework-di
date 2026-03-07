package com.automation.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CucumberJsonReportMerger implements JsonReportMerger {

    @Override
    public void merge(List<String> firstRunPaths, List<String> rerunPaths, File outputFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode finalFeatures = mapper.createArrayNode();
        Map<String, JsonNode> featureMap = new HashMap<String, JsonNode>();

        for (String path : rerunPaths) {
            JsonNode root = mapper.readTree(new File(path));
            if (root.isArray()) {
                for (JsonNode feature : root) {
                    String uri = feature.get("uri").asText();
                    featureMap.put(uri, feature);
                }
            }
        }

        for (String path : firstRunPaths) {
            JsonNode root = mapper.readTree(new File(path));
            if (root.isArray()) {
                for (JsonNode feature : root) {
                    String uri = feature.get("uri").asText();

                    if (featureMap.containsKey(uri)) {
                        JsonNode run2Feature = featureMap.get(uri);
                        ArrayNode run2Elements = (ArrayNode) run2Feature.get("elements");
                        List<String> run2Ids = new ArrayList<String>();
                        if (run2Elements != null) {
                            for (JsonNode scenario : run2Elements) {
                                run2Ids.add(getScenarioId(scenario));
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

    private String getScenarioId(JsonNode scenario) {
        if (scenario.has("id")) {
            return scenario.get("id").asText();
        }
        if (scenario.has("line")) {
            return String.valueOf(scenario.get("line").asInt());
        }
        return scenario.get("name").asText();
    }

    private boolean isScenarioPassed(JsonNode scenario) {
        if (!areStepsPassed(scenario.get("before"))) {
            return false;
        }
        if (!areStepsPassed(scenario.get("steps"))) {
            return false;
        }
        return areStepsPassed(scenario.get("after"));
    }

    private boolean areStepsPassed(JsonNode steps) {
        if (steps == null || !steps.isArray()) {
            return true;
        }
        for (JsonNode step : steps) {
            JsonNode result = step.get("result");
            if (result != null && result.has("status") && !"passed".equals(result.get("status").asText())) {
                return false;
            }
        }
        return true;
    }
}
