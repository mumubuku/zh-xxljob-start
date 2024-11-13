package com.zh.model;

/**
 * @author mumu
 */

public enum MisfireStrategy {
    DO_NOTHING("DO_NOTHING"),
    RESCHEDULE("RESCHEDULE");

    private final String value;

    MisfireStrategy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MisfireStrategy fromValue(String value) {
        for (MisfireStrategy strategy : MisfireStrategy.values()) {
            if (strategy.getValue().equals(value)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown misfire strategy: " + value);
    }
}

