package com.zh.model;

/**
 * @author mumu
 */

public enum ScheduleType {
    CRON("CRON"),
    FIX_RATE("FIX_RATE"),
    FIX_DELAY("FIX_DELAY");

    private final String value;

    ScheduleType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ScheduleType fromValue(String value) {
        for (ScheduleType type : ScheduleType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown schedule type: " + value);
    }
}

