package com.automation.utilities.file.compare;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.Diff;


import java.io.File;

public final class XmlCompareUtil {

    private XmlCompareUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static CompareResult compare(String f1,
                                        String f2) {

        CompareResult result = new CompareResult();

        Diff diff =
                DiffBuilder.compare(new File(f1))
                        .withTest(new File(f2))
                        .ignoreWhitespace()
                        .checkForSimilar()
                        .build();

        for (Difference d : diff.getDifferences()) {

            result.addDifference(d.toString());
        }

        return result;
    }
}
