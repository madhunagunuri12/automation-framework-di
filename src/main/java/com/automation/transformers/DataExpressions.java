package com.automation.transformers;

import java.time.LocalDate;
import java.util.Random;

/**
 * Contains all the custom data expressions used in Cucumber feature files.
 * These methods are automatically registered by DataTransformer.
 */
public class DataExpressions {

    @SuppressWarnings("unused")
    @DataExpression("cucumber-get-random-num:(.*)")
    public String getRandomNum(String prefix) {
        return prefix + new Random().nextInt(10000);
    }

    @SuppressWarnings("unused")
    @DataExpression("current-date")
    public String getCurrentDate() {
        return LocalDate.now().toString();
    }

    @SuppressWarnings("unused")
    @DataExpression("random-int")
    public Integer getRandomInt() {
        return new Random().nextInt(100);
    }

    @SuppressWarnings("unused")
    @DataExpression("is-true")
    public Boolean getTrue() {
        return true;
    }
}
