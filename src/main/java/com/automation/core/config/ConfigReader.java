package com.automation.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public final class ConfigReader {
    private static final String PROPERTY_FILE = "config.properties";
    private static volatile Properties properties;

    static {
        loadProperties();
    }

    private ConfigReader() {
    }

    public static synchronized void loadProperties() {
        Properties loadedProperties = new Properties();
        try (InputStream resourceStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(PROPERTY_FILE)) {
            if (resourceStream == null) {
                throw new IllegalStateException("Properties file not found on classpath: " + PROPERTY_FILE);
            }
            loadedProperties.load(resourceStream);
            loadedProperties.putAll(System.getProperties());
            properties = loadedProperties;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load properties file: " + PROPERTY_FILE, e);
        }
    }

    public static String getProperty(String key) {
        return getOptionalProperty(key).orElse(null);
    }

    public static String getRequiredProperty(String key) {
        return getOptionalProperty(key)
                .orElseThrow(new java.util.function.Supplier<IllegalStateException>() {
                    @Override
                    public IllegalStateException get() {
                        return new IllegalStateException("Missing required property: " + key);
                    }
                });
    }

    public static String getPropertyOrDefault(String key, String defaultValue) {
        return getOptionalProperty(key).orElse(defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue) {
        return getOptionalProperty(key)
                .map(Integer::parseInt)
                .orElse(defaultValue);
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        return getOptionalProperty(key)
                .map(Boolean::parseBoolean)
                .orElse(defaultValue);
    }

    public static Optional<String> getOptionalProperty(String key) {
        if (properties == null) {
            loadProperties();
        }
        return Optional.ofNullable(properties.getProperty(key));
    }
}
