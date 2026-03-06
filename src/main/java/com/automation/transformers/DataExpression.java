package com.automation.transformers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataExpression {
    /**
     * The regex pattern to match inside the {% ... %} block.
     * Example: "cucumber-get-random-num:(.*)"
     */
    String value();
}
