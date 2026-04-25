package com.example.approvalsystem.controller;

import com.example.approvalsystem.entity.ApprovalTemplate;
import com.example.approvalsystem.service.ApprovalTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class ApprovalTemplateController {

    @Autowired
    private ApprovalTemplateService templateService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTemplate(@RequestBody ApprovalTemplate template) {
        ApprovalTemplate created = templateService.createTemplate(template);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", created);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<Map<String, Object>> updateTemplate(@PathVariable String templateId, @RequestBody ApprovalTemplate template) {
        ApprovalTemplate updated = templateService.updateTemplate(templateId, template);
        Map<String, Object> result = new HashMap<>();
        if (updated != null) {
            result.put("success", true);
            result.put("data", updated);
        } else {
            result.put("success", false);
            result.put("message", "模板不存在");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<Map<String, Object>> getTemplateById(@PathVariable String templateId) {
        ApprovalTemplate template = templateService.getTemplateById(templateId);
        Map<String, Object> result = new HashMap<>();
        if (template != null) {
            result.put("success", true);
            result.put("data", template);
        } else {
            result.put("success", false);
            result.put("message", "模板不存在");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTemplates() {
        List<ApprovalTemplate> templates = templateService.getAllTemplates();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", templates);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@PathVariable String templateId) {
        boolean deleted = templateService.deleteTemplate(templateId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", deleted);
        if (!deleted) {
            result.put("message", "模板不存在");
        }
        return ResponseEntity.ok(result);
    }
}
