package com.example.approvalsystem.service;

import com.example.approvalsystem.entity.*;
import com.example.approvalsystem.enums.ApprovalAction;
import com.example.approvalsystem.enums.ApprovalStatus;
import com.example.approvalsystem.enums.ApproverRuleType;
import com.example.approvalsystem.enums.SignType;
import com.example.approvalsystem.store.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ApprovalFlowService {

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private UserService userService;

    public ApprovalInstance createApprovalInstance(ApprovalForm form, ApprovalTemplate template) {
        List<ApprovalNodeInstance> nodeInstances = new ArrayList<>();
        List<ApprovalNode> activeNodes = filterActiveNodes(template.getNodes(), form.getAmount());

        for (int i = 0; i < activeNodes.size(); i++) {
            ApprovalNode node = activeNodes.get(i);
            List<String> approverIds = resolveApprovers(form.getApplicantId(), node);

            if (approverIds.isEmpty()) {
                continue;
            }

            ApprovalNodeInstance nodeInstance = ApprovalNodeInstance.builder()
                    .nodeInstanceId(dataStore.generateId())
                    .nodeName(node.getNodeName())
                    .nodeOrder(i + 1)
                    .signType(node.getSignType())
                    .approverIds(approverIds)
                    .approvedIds(new ArrayList<>())
                    .rejectedIds(new ArrayList<>())
                    .status(ApprovalStatus.IN_PROGRESS)
                    .build();

            nodeInstances.add(nodeInstance);
        }

        ApprovalInstance instance = ApprovalInstance.builder()
                .instanceId(dataStore.generateId())
                .formId(form.getFormId())
                .templateId(template.getTemplateId())
                .nodes(nodeInstances)
                .currentNodeIndex(0)
                .status(ApprovalStatus.IN_PROGRESS)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        for (ApprovalNodeInstance nodeInstance : nodeInstances) {
            nodeInstance.setInstanceId(instance.getInstanceId());
        }

        return dataStore.saveInstance(instance);
    }

    private List<ApprovalNode> filterActiveNodes(List<ApprovalNode> nodes, BigDecimal amount) {
        if (nodes == null || nodes.isEmpty()) {
            return new ArrayList<>();
        }

        List<ApprovalNode> activeNodes = new ArrayList<>();
        for (ApprovalNode node : nodes) {
            if (isNodeApplicable(node, amount)) {
                activeNodes.add(node);
            }
        }
        return activeNodes;
    }

    private boolean isNodeApplicable(ApprovalNode node, BigDecimal amount) {
        if (node.getMinAmount() == null && node.getMaxAmount() == null) {
            return true;
        }
        if (amount == null) {
            return false;
        }
        if (node.getMinAmount() != null && amount.compareTo(node.getMinAmount()) < 0) {
            return false;
        }
        if (node.getMaxAmount() != null && amount.compareTo(node.getMaxAmount()) > 0) {
            return false;
        }
        return true;
    }

    private List<String> resolveApprovers(String applicantId, ApprovalNode node) {
        List<String> approverIds = new ArrayList<>();
        User applicant = userService.getUserById(applicantId);

        if (applicant == null) {
            return approverIds;
        }

        ApproverRuleType ruleType = node.getApproverRuleType();

        if (ruleType == null) {
            return approverIds;
        }

        switch (ruleType) {
            case DIRECT_MANAGER:
                if (applicant.getDirectManagerId() != null && !applicant.getDirectManagerId().isEmpty()) {
                    approverIds.add(applicant.getDirectManagerId());
                }
                break;

            case DEPARTMENT_HEAD:
                List<User> deptHeads = findDepartmentHeads(applicant.getDepartment());
                for (User deptHead : deptHeads) {
                    approverIds.add(deptHead.getUserId());
                }
                break;

            case FINANCE_ROLE:
                List<User> financeUsers = findUsersByRole("财务");
                for (User financeUser : financeUsers) {
                    approverIds.add(financeUser.getUserId());
                }
                break;

            case FIXED_USER:
                if (node.getFixedUserIds() != null) {
                    approverIds.addAll(node.getFixedUserIds());
                }
                break;
        }

        return approverIds;
    }

    private List<User> findDepartmentHeads(String department) {
        List<User> result = new ArrayList<>();
        List<User> allUsers = userService.getAllUsers();
        for (User user : allUsers) {
            if (department != null && department.equals(user.getDepartment())
                    && "部门主管".equals(user.getRole())) {
                result.add(user);
            }
        }
        return result;
    }

    private List<User> findUsersByRole(String role) {
        List<User> result = new ArrayList<>();
        List<User> allUsers = userService.getAllUsers();
        for (User user : allUsers) {
            if (role != null && role.equals(user.getRole())) {
                result.add(user);
            }
        }
        return result;
    }

    public void processApproval(ApprovalForm form, ApprovalInstance instance, String approverId, boolean approved, String comment) {
        int currentIndex = instance.getCurrentNodeIndex();
        List<ApprovalNodeInstance> nodes = instance.getNodes();

        if (currentIndex >= nodes.size()) {
            return;
        }

        ApprovalNodeInstance currentNode = nodes.get(currentIndex);
        User approver = userService.getUserById(approverId);
        String approverName = approver != null ? approver.getName() : approverId;

        if (approved) {
            if (!currentNode.getApprovedIds().contains(approverId)) {
                currentNode.getApprovedIds().add(approverId);
            }
            recordApprovalLog(form, instance, currentNode, approverId, approverName, ApprovalAction.APPROVE, comment);
        } else {
            if (!currentNode.getRejectedIds().contains(approverId)) {
                currentNode.getRejectedIds().add(approverId);
            }
            recordApprovalLog(form, instance, currentNode, approverId, approverName, ApprovalAction.REJECT, comment);
        }

        if (currentNode.getSignType() == SignType.OR_SIGN) {
            if (!currentNode.getRejectedIds().isEmpty()) {
                currentNode.setStatus(ApprovalStatus.REJECTED);
                instance.setStatus(ApprovalStatus.REJECTED);
                form.setStatus(ApprovalStatus.REJECTED);
                form.setCurrentApproverIds(new ArrayList<>());
            } else if (!currentNode.getApprovedIds().isEmpty()) {
                currentNode.setStatus(ApprovalStatus.APPROVED);
                moveToNextNode(form, instance);
            }
        } else {
            if (!currentNode.getRejectedIds().isEmpty()) {
                currentNode.setStatus(ApprovalStatus.REJECTED);
                instance.setStatus(ApprovalStatus.REJECTED);
                form.setStatus(ApprovalStatus.REJECTED);
                form.setCurrentApproverIds(new ArrayList<>());
            } else if (currentNode.getApprovedIds().size() == currentNode.getApproverIds().size()) {
                currentNode.setStatus(ApprovalStatus.APPROVED);
                moveToNextNode(form, instance);
            }
        }

        instance.setUpdateTime(LocalDateTime.now());
        form.setUpdateTime(LocalDateTime.now());
        dataStore.saveInstance(instance);
    }

    private void moveToNextNode(ApprovalForm form, ApprovalInstance instance) {
        int currentIndex = instance.getCurrentNodeIndex();
        List<ApprovalNodeInstance> nodes = instance.getNodes();

        int nextIndex = currentIndex + 1;
        if (nextIndex >= nodes.size()) {
            instance.setStatus(ApprovalStatus.APPROVED);
            form.setStatus(ApprovalStatus.APPROVED);
            form.setCurrentApproverIds(new ArrayList<>());
            form.setCurrentNodeId(null);
        } else {
            instance.setCurrentNodeIndex(nextIndex);
            ApprovalNodeInstance nextNode = nodes.get(nextIndex);
            nextNode.setStatus(ApprovalStatus.IN_PROGRESS);
            form.setCurrentNodeId(nextNode.getNodeInstanceId());
            form.setCurrentApproverIds(new ArrayList<>(nextNode.getApproverIds()));
        }
    }

    public void recordApprovalLog(ApprovalForm form, ApprovalInstance instance, ApprovalNodeInstance nodeInstance,
                                   String approverId, String approverName, ApprovalAction action, String comment) {
        ApprovalLog log = ApprovalLog.builder()
                .logId(dataStore.generateId())
                .formId(form.getFormId())
                .instanceId(instance != null ? instance.getInstanceId() : null)
                .nodeInstanceId(nodeInstance != null ? nodeInstance.getNodeInstanceId() : null)
                .nodeName(nodeInstance != null ? nodeInstance.getNodeName() : null)
                .approverId(approverId)
                .approverName(approverName)
                .action(action)
                .comment(comment)
                .actionTime(LocalDateTime.now())
                .build();
        dataStore.saveLog(log);
    }

    public boolean isCurrentApprover(String formId, String approverId) {
        ApprovalForm form = dataStore.getFormById(formId);
        if (form == null || form.getCurrentApproverIds() == null) {
            return false;
        }
        return form.getCurrentApproverIds().contains(approverId);
    }
}
