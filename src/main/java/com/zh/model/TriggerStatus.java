package com.zh.model;

/**
 * @author mumu
 */

public enum TriggerStatus {
    TRIGGERED("1", "触发"),
    NOT_TRIGGERED("2", "未触发"),
    RUNNING("3", "正在运行");

    private final String value;
    private final String description;

    TriggerStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static TriggerStatus fromValue(String value) {
        for (TriggerStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid trigger status value: " + value);
    }
}
