package com.example.approvalsystem.entity;

import com.example.approvalsystem.enums.ApprovalStatus;
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
public class ApprovalInstance implements Serializable {
    private static final long serialVersionUID = 1L;

    private String instanceId;
    private String formId;
    private String templateId;
    private List<ApprovalNodeInstance> nodes;
    private Integer currentNodeIndex;
    private ApprovalStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
