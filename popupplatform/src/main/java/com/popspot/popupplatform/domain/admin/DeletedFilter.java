package com.popspot.popupplatform.domain.admin;

public enum DeletedFilter {

    ACTIVE("active"),
    DELETED("deleted"),
    ALL("all");

    private final String value;

    DeletedFilter(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DeletedFilter from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        for (DeletedFilter filter : values()) {
            if (filter.value.equalsIgnoreCase(value)) {
                return filter;
            }
        }
        return ALL;
    }
}

