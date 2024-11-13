package com.zh.model;

/**
 * @author mumu
 */

public enum ExecutorRouteStrategy {
    FIRST("FIRST"),
    RANDOM("RANDOM"),
    CONSISTENT_HASH("CONSISTENT_HASH"),
    LEAST_FREQUENTLY_USED("LEAST_FREQUENTLY_USED");

    private final String value;

    ExecutorRouteStrategy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExecutorRouteStrategy fromValue(String value) {
        for (ExecutorRouteStrategy strategy : ExecutorRouteStrategy.values()) {
            if (strategy.getValue().equals(value)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown executor route strategy: " + value);
    }
}

