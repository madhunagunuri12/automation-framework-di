package com.automation.utilities.file.compare;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class CsvCompareUtil {

    private CsvCompareUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean compare(String f1,
                                  String f2)
            throws Exception {

        return readCsv(f1).equals(readCsv(f2));
    }


    /* Ignore Row Order */

    public static CompareResult compareIgnoreRowOrder(String f1,
                                                      String f2)
            throws Exception {

        CompareResult result = new CompareResult();

        List<String> r1 = readCsv(f1);
        List<String> r2 = readCsv(f2);

        Collections.sort(r1);
        Collections.sort(r2);

        if (!r1.equals(r2)) {

            result.addDifference(
                    "CSV mismatch (order ignored)");
        }

        return result;
    }


    private static List<String> readCsv(String file)
            throws Exception {

        List<String> rows = new ArrayList<>();

        try (CSVReader reader =
                     new CSVReader(new FileReader(file))) {

            String[] line;

            while ((line = reader.readNext()) != null) {

                rows.add(String.join("|", line));
            }
        }

        return rows;
    }
}
