package com.automation.utilities.file.compare;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public final class JsonCompareUtil {

    private static final ObjectMapper mapper =
            new ObjectMapper();

    private JsonCompareUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static CompareResult compare(String f1,
                                        String f2)
            throws Exception {

        CompareResult result = new CompareResult();

        JsonNode n1 = mapper.readTree(new File(f1));
        JsonNode n2 = mapper.readTree(new File(f2));

        compareNodes("", n1, n2, result);

        return result;
    }


    private static void compareNodes(String path,
                                     JsonNode n1,
                                     JsonNode n2,
                                     CompareResult result) {

        if (n1 == null && n2 == null) {
            return;
        }

        if (n1 == null || n2 == null) {
            result.addDifference(path + " : null mismatch");
            return;
        }

        if (!n1.getNodeType().equals(n2.getNodeType())) {
            result.addDifference(path + " : type mismatch");
            return;
        }

        if (n1.isValueNode()) {

            if (!n1.equals(n2)) {

                result.addDifference(
                        path + " : " + n1 + " vs " + n2);
            }
            return;
        }

        if (n1.isObject()) {

            n1.fieldNames().forEachRemaining(f ->

                    compareNodes(
                            path + "/" + f,
                            n1.get(f),
                            n2.get(f),
                            result));
        }

        if (n1.isArray()) {

            if (n1.size() != n2.size()) {

                result.addDifference(
                        path + " : array size mismatch");
                return;
            }

            for (int i = 0; i < n1.size(); i++) {

                compareNodes(
                        path + "[" + i + "]",
                        n1.get(i),
                        n2.get(i),
                        result);
            }
        }
    }
}
