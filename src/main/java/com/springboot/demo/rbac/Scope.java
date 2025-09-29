package com.springboot.demo.rbac;

public enum Scope {
    company, workspace, project;

    public static Scope parse(String s) {
        if (s == null) throw new IllegalArgumentException("scope is null");
        return Scope.valueOf(s.trim().toLowerCase());
    }
}
