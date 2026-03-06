package com.automation.utilities;

import java.util.function.Supplier;

public final class RetryUtil {

    private RetryUtil() {
        throw new UnsupportedOperationException("Utility class");
    }


    /* ==========================================================
       GENERIC RETRY
       ========================================================== */

    public static <T> T retry(int attempts,
                              long delayMillis,
                              Supplier<T> action) {

        RuntimeException lastException = null;

        for (int i = 1; i <= attempts; i++) {

            try {

                return action.get();

            } catch (RuntimeException e) {

                lastException = e;

                if (i == attempts) {
                    break;
                }

                sleep(delayMillis);
            }
        }

        throw lastException;
    }


    /* ==========================================================
       VOID RETRY
       ========================================================== */

    public static void retryVoid(int attempts,
                                 long delayMillis,
                                 Runnable action) {

        RuntimeException lastException = null;

        for (int i = 1; i <= attempts; i++) {

            try {

                action.run();
                return;

            } catch (RuntimeException e) {

                lastException = e;

                if (i == attempts) {
                    break;
                }

                sleep(delayMillis);
            }
        }

        throw lastException;
    }


    private static void sleep(long millis) {

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
