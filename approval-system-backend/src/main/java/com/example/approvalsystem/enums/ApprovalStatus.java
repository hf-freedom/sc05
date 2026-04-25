package com.example.approvalsystem.enums;

public enum ApprovalStatus {
    DRAFT("草稿"),
    IN_PROGRESS("审批中"),
    APPROVED("审批通过"),
    REJECTED("审批驳回"),
    WITHDRAWN("已撤回");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
