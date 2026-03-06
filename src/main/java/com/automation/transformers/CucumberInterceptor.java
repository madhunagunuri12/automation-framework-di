package com.automation.transformers;

import com.automation.core.logging.LoggerUtil;
import com.automation.utilities.ReflectionUtils;
import io.cucumber.cucumberexpressions.Group;
import io.cucumber.java.Scenario;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStep;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CucumberInterceptor {

    private CucumberInterceptor() {
    }

    public static void beforeStep(Scenario scenario) {
        try {
            // Clear cache for the new step to ensure consistency within the step, 
            // but fresh values for new steps (unless we want to cache per scenario? No, per step is safer for randoms)
            DataTableCellTransform.clearCache();
            
            Object testCaseState = ReflectionUtils.getFieldReferenceObject(scenario, "delegate");
            if (testCaseState == null) {
                LoggerUtil.error("DEBUG: testCaseState is null");
                return;
            }

            TestCase testCase = ReflectionUtils.getFieldReferenceObject(testCaseState, "testCase");
            UUID currentTestStepId = ReflectionUtils.getFieldReferenceObject(testCaseState, "currentTestStepId");

            PickleStepTestStep nextStep = getNextStep(currentTestStepId, testCase);

            if (nextStep != null) {
                transformStepArguments(nextStep);
            }

        } catch (Exception e) {
            LoggerUtil.error("CucumberInterceptor Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static PickleStepTestStep getNextStep(UUID currentStepId, TestCase testCase) {
        boolean match = false;
        for (TestStep step : testCase.getTestSteps()) {
            if (step.getId().equals(currentStepId)) {
                match = true;
                continue;
            }
            if (match && step instanceof PickleStepTestStep) {
                return (PickleStepTestStep) step;
            }
        }
        if (currentStepId == null) {
            for (TestStep step : testCase.getTestSteps()) {
                if (step instanceof PickleStepTestStep) {
                    return (PickleStepTestStep) step;
                }
            }
        }
        return null;
    }

    private static void transformStepArguments(PickleStepTestStep step) throws Exception {
        Object definitionMatch = ReflectionUtils.getFieldReferenceObject(step, "definitionMatch");
        List<?> arguments = (List<?>) ReflectionUtils.invokeMethod(definitionMatch, "getArguments");

        Object pickleStep = ReflectionUtils.getFieldReferenceObject(step, "step");
        String originalStepText = (String) ReflectionUtils.invokeMethod(pickleStep, "getText");

        StringBuilder newStepTextBuilder = new StringBuilder(originalStepText);
        int accumulatedOffset = 0;

        for (Object argument : arguments) {
            try {
                // 1. Handle Expression Arguments (Regular Steps)
                Object innerArgument = null;
                try {
                    innerArgument = ReflectionUtils.getFieldReferenceObject(argument, "argument");
                } catch (Exception ignored) {
                    // Ignore
                }

                if (innerArgument != null || argument.getClass().getName().contains("ExpressionArgument")) {
                    Group group = null;
                    try {
                        group = ReflectionUtils.getFieldReferenceObject(
                                innerArgument != null ? innerArgument : argument, "group");
                    } catch (Exception ignored) {
                        // Ignore
                    }

                    if (group != null) {
                        accumulatedOffset = processGroup(group, newStepTextBuilder, accumulatedOffset);
                    }
                }

                // 2. Handle Data Table Arguments
                if (argument.getClass().getName().contains("DataTableArgument")) {
                    transformDataTable(argument, pickleStep);
                }

                // 3. Handle DocString Arguments
                if (argument.getClass().getName().contains("DocStringArgument")) {
                    transformDocString(argument, pickleStep);
                }

            } catch (Exception e) {
                LoggerUtil.warn("Error processing argument: " + e.getMessage());
            }
        }

        String finalStepText = newStepTextBuilder.toString();
        if (!originalStepText.equals(finalStepText)) {
            try {
                Object innerPickleStep = ReflectionUtils.getFieldReferenceObject(pickleStep, "pickleStep");
                if (innerPickleStep != null) {
                    ReflectionUtils.setFieldReferenceObject(innerPickleStep, "text", finalStepText);
                } else {
                    ReflectionUtils.setFieldReferenceObject(pickleStep, "text", finalStepText);
                }
            } catch (Exception e) {
                LoggerUtil.warn("INTERCEPTOR WARNING: Could not set 'text' field. Report might show original text.");
            }
        }
    }

    private static int processGroup(Group group, StringBuilder stepTextBuilder, int currentOffset) throws Exception {
        int originalStart = group.getStart();
        int originalEnd = group.getEnd();
        int newStart = originalStart + currentOffset;
        int newEnd = originalEnd + currentOffset;

        ReflectionUtils.setFieldReferenceObject(group, "start", newStart);
        ReflectionUtils.setFieldReferenceObject(group, "end", newEnd);

        if (group.getChildren() != null) {
            for (Group child : group.getChildren()) {
                currentOffset = processGroup(child, stepTextBuilder, currentOffset);
            }
        }

        String originalValue = group.getValue();
        if (originalValue != null) {
            String newValue = DataTableCellTransform.handleDataTableCell(originalValue);

            if (!originalValue.equals(newValue)) {
                LoggerUtil.info("DEBUG: Transforming '" + originalValue + "' to '" + newValue + "'");
                ReflectionUtils.setFieldReferenceObject(group, "value", newValue);
                
                stepTextBuilder.replace(newStart, newEnd, newValue);
                int lengthDiff = newValue.length() - originalValue.length();
                ReflectionUtils.setFieldReferenceObject(group, "end", newEnd + lengthDiff);
                return currentOffset + lengthDiff;
            }
        }
        return currentOffset;
    }

    private static void transformDataTable(Object argument, Object pickleStep) throws Exception {
        // DataTableArgument has a List<List<String>> argument (raw data)
        List<List<String>> rawData = ReflectionUtils.getFieldReferenceObject(argument, "argument");

        // Transform the raw data
        List<List<String>> transformedData = rawData.stream()
                .map(row -> row.stream()
                        .map(DataTableCellTransform::handleDataTableCell)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        // Update the argument object with transformed data
        ReflectionUtils.setFieldReferenceObject(argument, "argument", transformedData);

        // Also update the PickleStep's DataTable if possible (for report)
        try {
            Object innerPickleStep = ReflectionUtils.getFieldReferenceObject(pickleStep, "pickleStep");
            Object stepArgument = ReflectionUtils.getFieldReferenceObject(
                    innerPickleStep != null ? innerPickleStep : pickleStep, "argument");

            if (stepArgument != null) {
                // PickleStepArgument has 'dataTable' field
                Object dataTable = null;
                try {
                    dataTable = ReflectionUtils.getFieldReferenceObject(stepArgument, "dataTable");
                } catch (Exception e) {
                    // Try direct access if structure differs
                }

                if (dataTable != null) {
                    List<?> rows = ReflectionUtils.getFieldReferenceObject(dataTable, "rows");
                    if (rows != null) {
                        // Iterate rows and update cells
                        for (int i = 0; i < rows.size(); i++) {
                            Object row = rows.get(i);
                            List<?> cells = ReflectionUtils.getFieldReferenceObject(row, "cells");
                            for (int j = 0; j < cells.size(); j++) {
                                Object cell = cells.get(j);
                                String newValue = transformedData.get(i).get(j);
                                
                                // Only update if changed
                                String oldValue = (String) ReflectionUtils.getFieldReferenceObject(cell, "value");
                                if (!oldValue.equals(newValue)) {
                                    // LoggerUtil.info("DEBUG: Updating cell [" + i + "][" + j + "] to " + newValue);
                                    ReflectionUtils.setFieldReferenceObject(cell, "value", newValue);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Failed to update DataTable in report: " + e.getMessage());
        }
    }

    private static void transformDocString(Object argument, Object pickleStep) throws Exception {
        String content = ReflectionUtils.getFieldReferenceObject(argument, "content");
        String newContent = DataTableCellTransform.handleDataTableCell(content);

        if (!content.equals(newContent)) {
            ReflectionUtils.setFieldReferenceObject(argument, "content", newContent);

            // Update PickleStep DocString for report
            try {
                Object innerPickleStep = ReflectionUtils.getFieldReferenceObject(pickleStep, "pickleStep");
                Object stepArgument = ReflectionUtils.getFieldReferenceObject(
                        innerPickleStep != null ? innerPickleStep : pickleStep, "argument");

                if (stepArgument != null) {
                    Object docString = null;
                    try {
                        docString = ReflectionUtils.getFieldReferenceObject(stepArgument, "docString");
                    } catch (Exception e) {
                        // Ignore
                    }
                    
                    if (docString != null) {
                        ReflectionUtils.setFieldReferenceObject(docString, "content", newContent);
                    } else if (stepArgument.getClass().getName().contains("PickleDocString")) {
                        // Fallback for older structures
                        ReflectionUtils.setFieldReferenceObject(stepArgument, "content", newContent);
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
