package com.example.approvalsystem.store;

import com.example.approvalsystem.entity.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryDataStore {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, ApprovalTemplate> templates = new ConcurrentHashMap<>();
    private final Map<String, ApprovalForm> forms = new ConcurrentHashMap<>();
    private final Map<String, ApprovalInstance> instances = new ConcurrentHashMap<>();
    private final Map<String, ApprovalNodeInstance> nodeInstances = new ConcurrentHashMap<>();
    private final Map<String, List<ApprovalLog>> logsByForm = new ConcurrentHashMap<>();

    public String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public User saveUser(User user) {
        users.put(user.getUserId(), user);
        return user;
    }

    public User getUserById(String userId) {
        return users.get(userId);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public void deleteUser(String userId) {
        users.remove(userId);
    }

    public ApprovalTemplate saveTemplate(ApprovalTemplate template) {
        templates.put(template.getTemplateId(), template);
        return template;
    }

    public ApprovalTemplate getTemplateById(String templateId) {
        return templates.get(templateId);
    }

    public List<ApprovalTemplate> getAllTemplates() {
        return new ArrayList<>(templates.values());
    }

    public void deleteTemplate(String templateId) {
        templates.remove(templateId);
    }

    public ApprovalForm saveForm(ApprovalForm form) {
        forms.put(form.getFormId(), form);
        return form;
    }

    public ApprovalForm getFormById(String formId) {
        return forms.get(formId);
    }

    public List<ApprovalForm> getFormsByApplicantId(String applicantId) {
        List<ApprovalForm> result = new ArrayList<>();
        for (ApprovalForm form : forms.values()) {
            if (applicantId.equals(form.getApplicantId())) {
                result.add(form);
            }
        }
        return result;
    }

    public List<ApprovalForm> getFormsByApproverId(String approverId) {
        List<ApprovalForm> result = new ArrayList<>();
        for (ApprovalForm form : forms.values()) {
            if (form.getCurrentApproverIds() != null && form.getCurrentApproverIds().contains(approverId)) {
                result.add(form);
            }
        }
        return result;
    }

    public List<ApprovalForm> getAllForms() {
        return new ArrayList<>(forms.values());
    }

    public ApprovalInstance saveInstance(ApprovalInstance instance) {
        instances.put(instance.getInstanceId(), instance);
        if (instance.getNodes() != null) {
            for (ApprovalNodeInstance node : instance.getNodes()) {
                nodeInstances.put(node.getNodeInstanceId(), node);
            }
        }
        return instance;
    }

    public ApprovalInstance getInstanceById(String instanceId) {
        return instances.get(instanceId);
    }

    public ApprovalInstance getInstanceByFormId(String formId) {
        for (ApprovalInstance instance : instances.values()) {
            if (formId.equals(instance.getFormId())) {
                return instance;
            }
        }
        return null;
    }

    public ApprovalLog saveLog(ApprovalLog log) {
        List<ApprovalLog> logs = logsByForm.computeIfAbsent(log.getFormId(), k -> new ArrayList<>());
        logs.add(log);
        return log;
    }

    public List<ApprovalLog> getLogsByFormId(String formId) {
        List<ApprovalLog> logs = logsByForm.get(formId);
        if (logs == null) {
            return new ArrayList<>();
        }
        List<ApprovalLog> sortedLogs = new ArrayList<>(logs);
        sortedLogs.sort(Comparator.comparing(ApprovalLog::getActionTime));
        return sortedLogs;
    }
}
