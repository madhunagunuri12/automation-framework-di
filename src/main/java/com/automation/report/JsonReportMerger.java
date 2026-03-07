package com.automation.report;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface JsonReportMerger {
    void merge(List<String> firstRunPaths, List<String> rerunPaths, File outputFile) throws IOException;
}
