package com.example.approvalsystem.service;

import com.example.approvalsystem.entity.ApprovalNode;
import com.example.approvalsystem.entity.ApprovalTemplate;
import com.example.approvalsystem.store.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ApprovalTemplateService {

    @Autowired
    private InMemoryDataStore dataStore;

    public ApprovalTemplate createTemplate(ApprovalTemplate template) {
        template.setTemplateId(dataStore.generateId());
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        if (template.getNodes() != null) {
            for (ApprovalNode node : template.getNodes()) {
                node.setNodeId(dataStore.generateId());
                node.setTemplateId(template.getTemplateId());
            }
            template.getNodes().sort(Comparator.comparingInt(ApprovalNode::getNodeOrder));
        }
        return dataStore.saveTemplate(template);
    }

    public ApprovalTemplate updateTemplate(String templateId, ApprovalTemplate template) {
        ApprovalTemplate existing = dataStore.getTemplateById(templateId);
        if (existing == null) {
            return null;
        }
        if (template.getTemplateName() != null) {
            existing.setTemplateName(template.getTemplateName());
        }
        if (template.getApprovalType() != null) {
            existing.setApprovalType(template.getApprovalType());
        }
        if (template.getDescription() != null) {
            existing.setDescription(template.getDescription());
        }
        if (template.getNodes() != null) {
            for (ApprovalNode node : template.getNodes()) {
                if (node.getNodeId() == null) {
                    node.setNodeId(dataStore.generateId());
                }
                node.setTemplateId(existing.getTemplateId());
            }
            template.getNodes().sort(Comparator.comparingInt(ApprovalNode::getNodeOrder));
            existing.setNodes(template.getNodes());
        }
        existing.setUpdateTime(LocalDateTime.now());
        return dataStore.saveTemplate(existing);
    }

    public ApprovalTemplate getTemplateById(String templateId) {
        return dataStore.getTemplateById(templateId);
    }

    public List<ApprovalTemplate> getAllTemplates() {
        return dataStore.getAllTemplates();
    }

    public boolean deleteTemplate(String templateId) {
        ApprovalTemplate template = dataStore.getTemplateById(templateId);
        if (template == null) {
            return false;
        }
        dataStore.deleteTemplate(templateId);
        return true;
    }
}
