package com.automation.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {
    private static final String PROPERTY_FILE = "config.properties";
    private static Properties properties;

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
            throw new RuntimeException("Failed to load properties file: " + PROPERTY_FILE, e);
        }
    }

    public static String getProperty(String key) {
        if (properties == null) {
            loadProperties();
        }
        return properties.getProperty(key);
    }

    @Deprecated
    public static void loadPropeties() {
        loadProperties();
    }

    @Deprecated
    public static void setProperty(String key, String value) {
        if (properties == null) {
            loadProperties();
        }
        properties.setProperty(key, value);
    }
}
