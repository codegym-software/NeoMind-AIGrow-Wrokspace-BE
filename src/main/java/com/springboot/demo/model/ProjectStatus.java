package com.springboot.demo.model;

public enum ProjectStatus {
    PLANNING("Planning"),
    ACTIVE("Active"),
    ON_HOLD("On Hold"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

