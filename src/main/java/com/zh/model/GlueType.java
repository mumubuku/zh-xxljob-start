package com.zh.model;

/**
 * @author mumu
 */

public enum GlueType {
    BEAN("BEAN"),
    GLUE_PYTHON("GLUE_PYTHON"),
    GLUE_SHELL("GLUE_SHELL");

    private final String value;

    GlueType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GlueType fromValue(String value) {
        for (GlueType type : GlueType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown glue type: " + value);
    }
}
