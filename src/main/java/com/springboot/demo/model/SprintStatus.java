package com.springboot.demo.model;

public enum SprintStatus {
    PLANNING("Planning"),
    ACTIVE("Active"), 
    COMPLETED("Completed"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    SprintStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
