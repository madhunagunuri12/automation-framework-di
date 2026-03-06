package com.automation.utilities.file.compare;

import java.util.ArrayList;
import java.util.List;

public class CompareResult {

    private boolean matched = true;

    private final List<String> differences = new ArrayList<>();


    public void addDifference(String diff) {
        matched = false;
        differences.add(diff);
    }

    public boolean isMatched() {
        return matched;
    }

    public List<String> getDifferences() {
        return differences;
    }

    @Override
    public String toString() {

        if (matched) {
            return "Files Matched Successfully";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Comparison Failed:\n");

        differences.forEach(d ->
                sb.append(" - ").append(d).append("\n"));

        return sb.toString();
    }
}
