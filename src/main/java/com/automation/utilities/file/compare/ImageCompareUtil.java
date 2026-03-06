package com.automation.utilities.file.compare;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public final class ImageCompareUtil {

    private ImageCompareUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static CompareResult compare(String i1,
                                        String i2,
                                        double tolerance)
            throws Exception {

        CompareResult result = new CompareResult();

        BufferedImage b1 =
                ImageIO.read(new File(i1));

        BufferedImage b2 =
                ImageIO.read(new File(i2));

        int diff = 0;

        for (int y = 0; y < b1.getHeight(); y++) {

            for (int x = 0; x < b1.getWidth(); x++) {

                if (b1.getRGB(x, y)
                        != b2.getRGB(x, y)) {

                    diff++;
                }
            }
        }

        double percent =
                100.0 * diff /
                        (b1.getWidth() * b1.getHeight());

        if (percent > tolerance) {

            result.addDifference(
                    "Visual diff: " + percent + "%");
        }

        return result;
    }
}
