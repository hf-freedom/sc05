package com.example.approvalsystem.entity;

import com.example.approvalsystem.enums.ApproverRuleType;
import com.example.approvalsystem.enums.SignType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalNode implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String templateId;
    private String nodeName;
    private Integer nodeOrder;
    private SignType signType;
    private ApproverRuleType approverRuleType;
    private List<String> fixedUserIds;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
}
