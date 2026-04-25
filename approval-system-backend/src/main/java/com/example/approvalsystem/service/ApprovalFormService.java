package com.example.approvalsystem.service;

import com.example.approvalsystem.entity.*;
import com.example.approvalsystem.enums.ApprovalAction;
import com.example.approvalsystem.enums.ApprovalStatus;
import com.example.approvalsystem.store.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ApprovalFormService {

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private ApprovalTemplateService templateService;

    @Autowired
    private ApprovalFlowService flowService;

    @Autowired
    private UserService userService;

    public ApprovalForm saveDraft(ApprovalForm form) {
        form.setFormId(dataStore.generateId());
        form.setStatus(ApprovalStatus.DRAFT);
        form.setVersion(1);
        form.setCreateTime(LocalDateTime.now());
        form.setUpdateTime(LocalDateTime.now());
        return dataStore.saveForm(form);
    }

    public ApprovalForm submitForm(String formId, String applicantId) {
        ApprovalForm form = dataStore.getFormById(formId);
        if (form == null) {
            return null;
        }

        if (form.getStatus() != ApprovalStatus.DRAFT && form.getStatus() != ApprovalStatus.REJECTED) {
            return null;
        }

        ApprovalTemplate template = templateService.getTemplateById(form.getTemplateId());
        if (template == null) {
            return null;
        }

        form.setApplicantId(applicantId);
        form.setApprovalType(template.getApprovalType());
        form.setStatus(ApprovalStatus.IN_PROGRESS);
        form.setUpdateTime(LocalDateTime.now());

        ApprovalInstance instance = flowService.createApprovalInstance(form, template);

        if (instance.getNodes() != null && !instance.getNodes().isEmpty()) {
            ApprovalNodeInstance firstNode = instance.getNodes().get(0);
            form.setCurrentNodeId(firstNode.getNodeInstanceId());
            form.setCurrentApproverIds(new ArrayList<>(firstNode.getApproverIds()));
        } else {
            form.setStatus(ApprovalStatus.APPROVED);
            instance.setStatus(ApprovalStatus.APPROVED);
            dataStore.saveInstance(instance);
        }

        User applicant = userService.getUserById(applicantId);
        String applicantName = applicant != null ? applicant.getName() : applicantId;
        flowService.recordApprovalLog(form, instance, null, applicantId, applicantName, ApprovalAction.SUBMIT, "提交审批单");

        return dataStore.saveForm(form);
    }

    public ApprovalForm submitFormDirectly(ApprovalForm form, String applicantId) {
        ApprovalTemplate template = templateService.getTemplateById(form.getTemplateId());
        if (template == null) {
            return null;
        }

        form.setFormId(dataStore.generateId());
        form.setApplicantId(applicantId);
        form.setApprovalType(template.getApprovalType());
        form.setStatus(ApprovalStatus.IN_PROGRESS);
        form.setVersion(1);
        form.setCreateTime(LocalDateTime.now());
        form.setUpdateTime(LocalDateTime.now());

        ApprovalInstance instance = flowService.createApprovalInstance(form, template);

        if (instance.getNodes() != null && !instance.getNodes().isEmpty()) {
            ApprovalNodeInstance firstNode = instance.getNodes().get(0);
            form.setCurrentNodeId(firstNode.getNodeInstanceId());
            form.setCurrentApproverIds(new ArrayList<>(firstNode.getApproverIds()));
        } else {
            form.setStatus(ApprovalStatus.APPROVED);
            instance.setStatus(ApprovalStatus.APPROVED);
            dataStore.saveInstance(instance);
        }

        User applicant = userService.getUserById(applicantId);
        String applicantName = applicant != null ? applicant.getName() : applicantId;
        flowService.recordApprovalLog(form, instance, null, applicantId, applicantName, ApprovalAction.SUBMIT, "提交审批单");

        return dataStore.saveForm(form);
    }

    public Map<String, Object> approve(String formId, String approverId, String comment) {
        Map<String, Object> result = new HashMap<>();

        if (!flowService.isCurrentApprover(formId, approverId)) {
            result.put("success", false);
            result.put("message", "您不是当前审批人，无法审批此单据");
            return result;
        }

        ApprovalForm form = dataStore.getFormById(formId);
        if (form == null) {
            result.put("success", false);
            result.put("message", "审批单不存在");
            return result;
        }

        if (form.getStatus() != ApprovalStatus.IN_PROGRESS) {
            result.put("success", false);
            result.put("message", "审批单不在审批中状态");
            return result;
        }

        ApprovalInstance instance = dataStore.getInstanceByFormId(formId);
        if (instance == null) {
            result.put("success", false);
            result.put("message", "审批实例不存在");
            return result;
        }

        flowService.processApproval(form, instance, approverId, true, comment);
        dataStore.saveForm(form);

        result.put("success", true);
        result.put("data", form);
        return result;
    }

    public Map<String, Object> reject(String formId, String approverId, String comment) {
        Map<String, Object> result = new HashMap<>();

        if (!flowService.isCurrentApprover(formId, approverId)) {
            result.put("success", false);
            result.put("message", "您不是当前审批人，无法审批此单据");
            return result;
        }

        ApprovalForm form = dataStore.getFormById(formId);
        if (form == null) {
            result.put("success", false);
            result.put("message", "审批单不存在");
            return result;
        }

        if (form.getStatus() != ApprovalStatus.IN_PROGRESS) {
            result.put("success", false);
            result.put("message", "审批单不在审批中状态");
            return result;
        }

        ApprovalInstance instance = dataStore.getInstanceByFormId(formId);
        if (instance == null) {
            result.put("success", false);
            result.put("message", "审批实例不存在");
            return result;
        }

        flowService.processApproval(form, instance, approverId, false, comment);
        dataStore.saveForm(form);

        result.put("success", true);
        result.put("data", form);
        return result;
    }

    public Map<String, Object> withdraw(String formId, String applicantId) {
        Map<String, Object> result = new HashMap<>();

        ApprovalForm form = dataStore.getFormById(formId);
        if (form == null) {
            result.put("success", false);
            result.put("message", "审批单不存在");
            return result;
        }

        if (!applicantId.equals(form.getApplicantId())) {
            result.put("success", false);
            result.put("message", "只有申请人才能撤回审批单");
            return result;
        }

        if (form.getStatus() != ApprovalStatus.IN_PROGRESS) {
            result.put("success", false);
            result.put("message", "只能撤回审批中的单据");
            return result;
        }

        form.setStatus(ApprovalStatus.WITHDRAWN);
        form.setCurrentApproverIds(new ArrayList<>());
        form.setCurrentNodeId(null);
        form.setUpdateTime(LocalDateTime.now());

        ApprovalInstance instance = dataStore.getInstanceByFormId(formId);
        if (instance != null) {
            instance.setStatus(ApprovalStatus.WITHDRAWN);
            instance.setUpdateTime(LocalDateTime.now());
            dataStore.saveInstance(instance);

            User applicant = userService.getUserById(applicantId);
            String applicantName = applicant != null ? applicant.getName() : applicantId;
            flowService.recordApprovalLog(form, instance, null, applicantId, applicantName, ApprovalAction.WITHDRAW, "撤回审批单");
        }

        dataStore.saveForm(form);
        result.put("success", true);
        result.put("data", form);
        return result;
    }

    public ApprovalForm resubmit(String formId, String applicantId, ApprovalForm updatedForm) {
        ApprovalForm existingForm = dataStore.getFormById(formId);
        if (existingForm == null) {
            return null;
        }

        if (!applicantId.equals(existingForm.getApplicantId())) {
            return null;
        }

        if (existingForm.getStatus() != ApprovalStatus.REJECTED) {
            return null;
        }

        ApprovalForm newForm = new ApprovalForm();
        newForm.setFormId(dataStore.generateId());
        newForm.setTemplateId(existingForm.getTemplateId());
        newForm.setApprovalType(existingForm.getApprovalType());

        if (updatedForm.getTitle() != null) {
            newForm.setTitle(updatedForm.getTitle());
        } else {
            newForm.setTitle(existingForm.getTitle());
        }

        if (updatedForm.getContent() != null) {
            newForm.setContent(updatedForm.getContent());
        } else {
            newForm.setContent(existingForm.getContent());
        }

        if (updatedForm.getAmount() != null) {
            newForm.setAmount(updatedForm.getAmount());
        } else {
            newForm.setAmount(existingForm.getAmount());
        }

        newForm.setApplicantId(applicantId);
        newForm.setStatus(ApprovalStatus.IN_PROGRESS);
        newForm.setVersion(existingForm.getVersion() + 1);
        newForm.setCreateTime(LocalDateTime.now());
        newForm.setUpdateTime(LocalDateTime.now());

        ApprovalTemplate template = templateService.getTemplateById(newForm.getTemplateId());
        if (template == null) {
            return null;
        }

        ApprovalInstance instance = flowService.createApprovalInstance(newForm, template);

        if (instance.getNodes() != null && !instance.getNodes().isEmpty()) {
            ApprovalNodeInstance firstNode = instance.getNodes().get(0);
            newForm.setCurrentNodeId(firstNode.getNodeInstanceId());
            newForm.setCurrentApproverIds(new ArrayList<>(firstNode.getApproverIds()));
        } else {
            newForm.setStatus(ApprovalStatus.APPROVED);
            instance.setStatus(ApprovalStatus.APPROVED);
            dataStore.saveInstance(instance);
        }

        User applicant = userService.getUserById(applicantId);
        String applicantName = applicant != null ? applicant.getName() : applicantId;
        flowService.recordApprovalLog(newForm, instance, null, applicantId, applicantName, ApprovalAction.RESUBMIT, "重新提交审批单");

        return dataStore.saveForm(newForm);
    }

    public ApprovalForm getFormById(String formId) {
        return dataStore.getFormById(formId);
    }

    public List<ApprovalForm> getFormsByApplicantId(String applicantId) {
        return dataStore.getFormsByApplicantId(applicantId);
    }

    public List<ApprovalForm> getFormsByApproverId(String approverId) {
        return dataStore.getFormsByApproverId(approverId);
    }

    public List<ApprovalForm> getAllForms() {
        return dataStore.getAllForms();
    }

    public ApprovalInstance getInstanceByFormId(String formId) {
        return dataStore.getInstanceByFormId(formId);
    }

    public List<ApprovalLog> getLogsByFormId(String formId) {
        return dataStore.getLogsByFormId(formId);
    }
}
