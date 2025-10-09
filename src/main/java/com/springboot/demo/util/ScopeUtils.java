package com.springboot.demo.util;

import com.springboot.demo.model.enums.Scope;

public final class ScopeUtils {

    private ScopeUtils() {} // prevent instantiation

    public static Scope parseScope(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("scope is null/blank");
        }
        return switch (s.trim().toLowerCase()) {
            case "company"  -> Scope.company;
            case "workspace"-> Scope.workspace;
            case "project"  -> Scope.project;
            default -> throw new IllegalArgumentException("Unknown scope: " + s);
        };
    }
}
