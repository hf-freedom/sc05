package com.example.approvalsystem.entity;

import com.example.approvalsystem.enums.ApprovalStatus;
import com.example.approvalsystem.enums.SignType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalNodeInstance implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nodeInstanceId;
    private String instanceId;
    private String nodeName;
    private Integer nodeOrder;
    private SignType signType;
    private List<String> approverIds;
    private List<String> approvedIds;
    private List<String> rejectedIds;
    private ApprovalStatus status;
}
