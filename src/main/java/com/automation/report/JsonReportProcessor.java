package com.automation.report;

import java.util.List;

public interface JsonReportProcessor {
    void process(List<String> jsonPaths);
}
