package com.automation.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CucumberJsonReportProcessor implements JsonReportProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CucumberJsonReportProcessor.class);
    private static final String LEGACY_BEFORE_HOOK = "com.automation.base.Hooks.beforeStep";
    private static final String LEGACY_AFTER_HOOK = "com.automation.base.Hooks.afterStep";
    private static final String CURRENT_BEFORE_HOOK = "com.automation.steps.Hooks.beforeStep";
    private static final String CURRENT_AFTER_HOOK = "com.automation.steps.Hooks.afterStep";

    @Override
    public void process(List<String> jsonPaths) {
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

    private void deduplicateElements(ArrayNode elements) {
        Map<String, JsonNode> unique = new LinkedHashMap<String, JsonNode>();
        for (JsonNode node : elements) {
            unique.put(getScenarioId(node), node);
        }
        if (unique.size() < elements.size()) {
            elements.removeAll();
            unique.values().forEach(elements::add);
        }
    }

    private void removeBeforeStepHooks(JsonNode scenario) {
        JsonNode steps = scenario.get("steps");
        if (steps != null && steps.isArray()) {
            for (JsonNode step : steps) {
                JsonNode before = step.get("before");
                if (before != null && before.isArray()) {
                    Iterator<JsonNode> iterator = before.iterator();
                    while (iterator.hasNext()) {
                        JsonNode hook = iterator.next();
                        if (shouldRemoveHook(hook, LEGACY_BEFORE_HOOK) || shouldRemoveHook(hook, CURRENT_BEFORE_HOOK)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    private void mergeAfterStepHooksToStep(JsonNode scenario) {
        JsonNode steps = scenario.get("steps");
        if (steps != null && steps.isArray()) {
            for (JsonNode step : steps) {
                JsonNode after = step.get("after");
                if (after != null && after.isArray()) {
                    Iterator<JsonNode> iterator = after.iterator();
                    while (iterator.hasNext()) {
                        JsonNode hook = iterator.next();
                        if (shouldRemoveHook(hook, LEGACY_AFTER_HOOK) || shouldRemoveHook(hook, CURRENT_AFTER_HOOK)) {
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

    private boolean shouldRemoveHook(JsonNode hook, String hookMethodName) {
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
            if (result != null) {
                String status = result.get("status").asText();
                if (!"passed".equals(status)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void removeEmbeddings(JsonNode scenario) {
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

    private void removeEmbeddingsFromHooks(JsonNode scenario, String hookType) {
        JsonNode hooks = scenario.get(hookType);
        if (hooks != null && hooks.isArray()) {
            for (JsonNode hook : hooks) {
                if (hook instanceof ObjectNode) {
                    ((ObjectNode) hook).remove("embeddings");
                }
            }
        }
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
}
