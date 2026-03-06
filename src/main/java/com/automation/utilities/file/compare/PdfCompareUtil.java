package com.automation.utilities.file.compare;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public final class PdfCompareUtil {

    private PdfCompareUtil() {
        throw new UnsupportedOperationException("Utility class");
    }


    /* Page Order Sensitive */

    public static CompareResult compare(String p1,
                                        String p2,
                                        Set<String> ignore)
            throws Exception {

        CompareResult result = new CompareResult();

        try (PDDocument d1 =
                     PDDocument.load(new File(p1));
             PDDocument d2 =
                     PDDocument.load(new File(p2))) {

            PDFTextStripper s =
                    new PDFTextStripper();

            if (d1.getNumberOfPages()
                    != d2.getNumberOfPages()) {

                result.addDifference("Page count mismatch");
                return result;
            }

            for (int i = 1;
                 i <= d1.getNumberOfPages();
                 i++) {

                s.setStartPage(i);
                s.setEndPage(i);

                String t1 = normalize(
                        s.getText(d1), ignore);

                String t2 = normalize(
                        s.getText(d2), ignore);

                if (!t1.equals(t2)) {

                    result.addDifference(
                            "Mismatch at page " + i);
                }
            }
        }

        return result;
    }


    /* Ignore Page Order */

    public static CompareResult compareIgnorePageOrder(
            String p1,
            String p2,
            Set<String> ignore)
            throws Exception {

        CompareResult result = new CompareResult();

        List<String> l1 = extractPages(p1, ignore);
        List<String> l2 = extractPages(p2, ignore);

        Collections.sort(l1);
        Collections.sort(l2);

        if (!l1.equals(l2)) {

            result.addDifference(
                    "PDF mismatch (order ignored)");
        }

        return result;
    }


    /* ========================= */

    private static List<String> extractPages(
            String pdf,
            Set<String> ignore)
            throws Exception {

        List<String> pages = new ArrayList<>();

        try (PDDocument d =
                     PDDocument.load(new File(pdf))) {

            PDFTextStripper s =
                    new PDFTextStripper();

            for (int i = 1;
                 i <= d.getNumberOfPages();
                 i++) {

                s.setStartPage(i);
                s.setEndPage(i);

                pages.add(
                        normalize(
                                s.getText(d), ignore));
            }
        }

        return pages;
    }


    private static String normalize(String text,
                                    Set<String> ignore) {
        if (text == null) {
            return "";
        }

        String t = text
                .replaceAll("\\s+", " ")
                .trim();

        if (ignore != null) {

            for (String s : ignore) {
                t = t.replace(s, "");
            }
        }

        return t;
    }
}
