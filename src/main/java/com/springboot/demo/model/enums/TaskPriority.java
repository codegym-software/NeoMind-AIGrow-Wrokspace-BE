package com.springboot.demo.model.enums;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum TaskPriority {
    low("Low", 1),
    medium("Medium", 2),
    high("High", 3);

    private final String displayName;
    private final int level;

    TaskPriority(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

}