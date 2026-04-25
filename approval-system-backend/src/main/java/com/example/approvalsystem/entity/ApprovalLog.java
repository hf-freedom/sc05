package com.example.approvalsystem.entity;

import com.example.approvalsystem.enums.ApprovalAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private String logId;
    private String formId;
    private String instanceId;
    private String nodeInstanceId;
    private String nodeName;
    private String approverId;
    private String approverName;
    private ApprovalAction action;
    private String comment;
    private LocalDateTime actionTime;
}
