package com.zh.model;

/**
 * @author mumu
 */

public enum FailStrategy {
    FAIL_FAST("FAIL_FAST"),
    FAIL_OVER("FAIL_OVER");

    private final String value;

    FailStrategy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FailStrategy fromValue(String value) {
        for (FailStrategy strategy : FailStrategy.values()) {
            if (strategy.getValue().equals(value)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown fail strategy: " + value);
    }
}

