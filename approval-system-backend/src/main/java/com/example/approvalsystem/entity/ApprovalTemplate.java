package com.example.approvalsystem.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    private String templateId;
    private String templateName;
    private String approvalType;
    private String description;
    private List<ApprovalNode> nodes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
