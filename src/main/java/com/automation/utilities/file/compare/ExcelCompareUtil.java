package com.automation.utilities.file.compare;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Objects;


public final class ExcelCompareUtil {

    private ExcelCompareUtil() {
        throw new UnsupportedOperationException("Utility class");
    }


    /* ==========================================================
       Ignore Sheets & Columns
       ========================================================== */

    public static CompareResult compare(String f1,
                                        String f2,
                                        Set<String> ignoreSheets,
                                        Set<Integer> ignoreCols)
            throws Exception {

        CompareResult result = new CompareResult();

        try (Workbook w1 =
                     new XSSFWorkbook(new FileInputStream(f1));
             Workbook w2 =
                     new XSSFWorkbook(new FileInputStream(f2))) {

            for (int i = 0; i < w1.getNumberOfSheets(); i++) {

                Sheet s1 = w1.getSheetAt(i);
                Sheet s2 = w2.getSheetAt(i);

                if (ignoreSheets != null &&
                        ignoreSheets.contains(s1.getSheetName())) {
                    continue;
                }

                compareSheet(s1, s2, ignoreCols, result);
            }
        }

        return result;
    }


    /* ==========================================================
       Ignore Row Order
       ========================================================== */

    public static CompareResult compareIgnoreRowOrder(String f1,
                                                      String f2,
                                                      Set<String> ignoreSheets,
                                                      Set<Integer> ignoreCols)
            throws Exception {

        CompareResult result = new CompareResult();

        try (Workbook w1 =
                     new XSSFWorkbook(new FileInputStream(f1));
             Workbook w2 =
                     new XSSFWorkbook(new FileInputStream(f2))) {

            for (int i = 0; i < w1.getNumberOfSheets(); i++) {

                Sheet s1 = w1.getSheetAt(i);
                Sheet s2 = w2.getSheetAt(i);

                if (ignoreSheets != null &&
                        ignoreSheets.contains(s1.getSheetName())) {
                    continue;
                }

                List<String> r1 = extractRows(s1, ignoreCols);
                List<String> r2 = extractRows(s2, ignoreCols);

                Collections.sort(r1);
                Collections.sort(r2);

                if (!r1.equals(r2)) {

                    result.addDifference(
                            "Sheet mismatch: "
                                    + s1.getSheetName());
                }
            }
        }

        return result;
    }


    /* ==========================================================
       INTERNAL HELPERS
       ========================================================== */

    private static void compareSheet(Sheet s1,
                                     Sheet s2,
                                     Set<Integer> ignoreCols,
                                     CompareResult result) {

        int maxRow =
                Math.max(s1.getLastRowNum(),
                        s2.getLastRowNum());

        for (int i = 0; i <= maxRow; i++) {

            Row r1 = s1.getRow(i);
            Row r2 = s2.getRow(i);

            if (!compareRow(r1, r2, ignoreCols)) {

                result.addDifference(
                        "Row mismatch at index: " + i);
            }
        }
    }


    private static boolean compareRow(Row r1,
                                      Row r2,
                                      Set<Integer> ignoreCols) {

        if (r1 == null && r2 == null) {
            return true;
        }

        if (r1 == null || r2 == null) {
            return false;
        }

        int maxCell =
                Math.max(r1.getLastCellNum(),
                        r2.getLastCellNum());

        if (maxCell < 0) {
            return true;
        }

        for (int i = 0; i < maxCell; i++) {

            if (ignoreCols != null &&
                    ignoreCols.contains(i)) {
                continue;
            }

            String v1 = getValue(r1.getCell(i));
            String v2 = getValue(r2.getCell(i));

            if (!Objects.equals(v1, v2)) {
                return false;
            }
        }

        return true;
    }


    private static List<String> extractRows(Sheet sheet,
                                            Set<Integer> ignoreCols) {

        List<String> rows = new ArrayList<>();

        int maxRow = sheet.getLastRowNum();

        for (int i = 0; i <= maxRow; i++) {

            Row row = sheet.getRow(i);

            if (row == null) {
                rows.add("");
                continue;
            }

            StringBuilder sb = new StringBuilder();

            int maxCell = row.getLastCellNum();

            if (maxCell < 0) {
                rows.add("");
                continue;
            }

            for (int j = 0; j < maxCell; j++) {

                if (ignoreCols != null &&
                        ignoreCols.contains(j)) {
                    continue;
                }

                sb.append(getValue(row.getCell(j)))
                        .append("|");
            }

            rows.add(sb.toString());
        }

        return rows;
    }


    private static String getValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {

            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                return String.valueOf(
                        cell.getNumericCellValue());

            case BOOLEAN:
                return String.valueOf(
                        cell.getBooleanCellValue());

            case FORMULA:
                return cell.getCellFormula();

            case BLANK:
                return "";

            default:
                return cell.toString().trim();
        }
    }
}
