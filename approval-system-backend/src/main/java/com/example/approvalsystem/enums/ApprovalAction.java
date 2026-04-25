package com.example.approvalsystem.enums;

public enum ApprovalAction {
    SUBMIT("提交"),
    APPROVE("通过"),
    REJECT("驳回"),
    WITHDRAW("撤回"),
    RESUBMIT("重新提交");

    private final String description;

    ApprovalAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
