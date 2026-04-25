package com.example.approvalsystem.entity;

import com.example.approvalsystem.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalForm implements Serializable {
    private static final long serialVersionUID = 1L;

    private String formId;
    private String templateId;
    private String approvalType;
    private String title;
    private String content;
    private BigDecimal amount;
    private String applicantId;
    private ApprovalStatus status;
    private String currentNodeId;
    private List<String> currentApproverIds;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
