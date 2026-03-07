package com.automation.core.driver;

import com.automation.core.config.ConfigReader;

public final class DriverConfig {

    private final ExecutionMode executionMode;
    private final BrowserType browserType;
    private final String gridUrl;
    private final boolean maximizeWindow;

    private DriverConfig(Builder builder) {
        this.executionMode = builder.executionMode;
        this.browserType = builder.browserType;
        this.gridUrl = builder.gridUrl;
        this.maximizeWindow = builder.maximizeWindow;
    }

    public static DriverConfig fromSystemProperties() {
        return builder()
                .executionMode(ExecutionMode.from(ConfigReader.getPropertyOrDefault("execution", "local")))
                .browserType(BrowserType.from(ConfigReader.getPropertyOrDefault("browser", "chrome")))
                .gridUrl(ConfigReader.getPropertyOrDefault("grid.url", "http://host.docker.internal:4444/wd/hub"))
                .maximizeWindow(ConfigReader.getBooleanProperty("browser.maximize", true))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public BrowserType getBrowserType() {
        return browserType;
    }

    public String getGridUrl() {
        return gridUrl;
    }

    public boolean isMaximizeWindow() {
        return maximizeWindow;
    }

    public static final class Builder {
        private ExecutionMode executionMode = ExecutionMode.LOCAL;
        private BrowserType browserType = BrowserType.CHROME;
        private String gridUrl = "http://host.docker.internal:4444/wd/hub";
        private boolean maximizeWindow = true;

        private Builder() {
        }

        public Builder executionMode(ExecutionMode executionMode) {
            if (executionMode != null) {
                this.executionMode = executionMode;
            }
            return this;
        }

        public Builder browserType(BrowserType browserType) {
            if (browserType != null) {
                this.browserType = browserType;
            }
            return this;
        }

        public Builder gridUrl(String gridUrl) {
            if (gridUrl != null && !gridUrl.trim().isEmpty()) {
                this.gridUrl = gridUrl;
            }
            return this;
        }

        public Builder maximizeWindow(boolean maximizeWindow) {
            this.maximizeWindow = maximizeWindow;
            return this;
        }

        public DriverConfig build() {
            return new DriverConfig(this);
        }
    }
}
