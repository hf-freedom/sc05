package com.example.approvalsystem.enums;

public enum SignType {
    AND_SIGN("会签"),
    OR_SIGN("或签");

    private final String description;

    SignType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
