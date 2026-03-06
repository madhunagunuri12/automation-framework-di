package com.automation.transformers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTableCellTransform {

    private static final List<ExpressionEntry> REGISTRY = new ArrayList<>();
    // Updated pattern to match {*key*}
    // noinspection all
    private static final Pattern CONTEXT_PATTERN = Pattern.compile("\\{\\*([^{}]+)\\*\\}");

    // Cache to ensure consistent values for the same expression within a single step execution
    private static final ThreadLocal<Map<String, String>> STEP_CACHE = ThreadLocal.withInitial(HashMap::new);

    static {
        registerClass(DataExpressions.class);
    }

    private DataTableCellTransform() {
    }

    public static void registerClass(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(DataExpression.class)) {
                    DataExpression annotation = method.getAnnotation(DataExpression.class);
                    REGISTRY.add(new ExpressionEntry(Pattern.compile(annotation.value()), method, instance));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to register DataExpression class: " + clazz.getName(), e);
        }
    }

    public static void clearCache() {
        STEP_CACHE.get().clear();
    }

    public static String handleDataTableCell(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // 1. Handle quotes if present (common for {string} arguments)
        boolean hasQuotes = value.startsWith("\"") && value.endsWith("\"");
        String contentToProcess = hasQuotes ? value.substring(1, value.length() - 1) : value;

        // 2. Resolve Context Variables {*key*}
        String resolvedValue = resolveContextVariables(contentToProcess);

        // 3. Check for {%...%} wrapper and strip it
        if (resolvedValue.startsWith("{%") && resolvedValue.endsWith("%}")) {
            // Check cache first
            if (STEP_CACHE.get().containsKey(resolvedValue)) {
                String cachedValue = STEP_CACHE.get().get(resolvedValue);
                return hasQuotes ? "\"" + cachedValue + "\"" : cachedValue;
            }

            String innerContent = resolvedValue.substring(2, resolvedValue.length() - 2).trim();
            // 4. Resolve Dynamic Expressions using the inner content
            String transformedValue = resolveDynamicExpressions(innerContent);

            // Cache the result (mapping {%...%} -> TransformedValue)
            STEP_CACHE.get().put(resolvedValue, transformedValue);

            // Restore quotes if they were present
            return hasQuotes ? "\"" + transformedValue + "\"" : transformedValue;
        }

        // If no {%...%} wrapper, return as is (but with context resolved)
        return hasQuotes ? "\"" + resolvedValue + "\"" : resolvedValue;
    }

    private static String resolveContextVariables(String content) {
        Matcher matcher = CONTEXT_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            // No need to check for % prefix as the pattern {*...*} is distinct

            Object value = TestDataStore.get(key);
            if (value != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
            } else {
                String prop = System.getProperty(key);
                if (prop != null) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(prop));
                } else {
                    // Keep original {*key*} if not found
                    matcher.appendReplacement(sb, Matcher.quoteReplacement("{*" + key + "*}"));
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String resolveDynamicExpressions(String content) {
        for (ExpressionEntry entry : REGISTRY) {
            Matcher matcher = entry.pattern.matcher(content);
            if (matcher.matches()) {
                try {
                    int groupCount = matcher.groupCount();
                    Object[] args = new Object[groupCount];
                    for (int i = 0; i < groupCount; i++) {
                        args[i] = matcher.group(i + 1);
                    }

                    Object result;
                    if (entry.method.getParameterCount() == 0) {
                        result = entry.method.invoke(entry.instance);
                    } else {
                        result = entry.method.invoke(entry.instance, args);
                    }
                    return String.valueOf(result);

                } catch (Exception e) {
                    throw new RuntimeException("Error executing DataExpression for: " + content, e);
                }
            }
        }
        // If no match found, return the original content wrapped back in {%...%}
        // so the user knows it failed to transform.
        return "{%" + content + "%}";
    }

    private static class ExpressionEntry {
        Pattern pattern;
        Method method;
        Object instance;

        ExpressionEntry(Pattern pattern, Method method, Object instance) {
            this.pattern = pattern;
            this.method = method;
            this.instance = instance;
        }
    }
}
