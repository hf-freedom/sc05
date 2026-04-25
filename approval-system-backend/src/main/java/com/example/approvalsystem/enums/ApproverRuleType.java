package com.example.approvalsystem.enums;

public enum ApproverRuleType {
    DIRECT_MANAGER("直属上级"),
    DEPARTMENT_HEAD("部门主管"),
    FINANCE_ROLE("财务角色"),
    FIXED_USER("固定用户");

    private final String description;

    ApproverRuleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
