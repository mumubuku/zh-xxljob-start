package com.zh.model;

/**
 * @author mumu
 */

public enum ExecutorBlockStrategy {
    SERIAL_EXECUTION("SERIAL_EXECUTION"),
    CONCURRENT_EXECUTION("CONCURRENT_EXECUTION");

    private final String value;

    ExecutorBlockStrategy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExecutorBlockStrategy fromValue(String value) {
        for (ExecutorBlockStrategy strategy : ExecutorBlockStrategy.values()) {
            if (strategy.getValue().equals(value)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown executor block strategy: " + value);
    }
}
