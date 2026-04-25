package com.example.approvalsystem.controller;

import com.example.approvalsystem.entity.ApprovalForm;
import com.example.approvalsystem.entity.ApprovalInstance;
import com.example.approvalsystem.entity.ApprovalLog;
import com.example.approvalsystem.service.ApprovalFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forms")
public class ApprovalFormController {

    @Autowired
    private ApprovalFormService formService;

    @PostMapping("/draft")
    public ResponseEntity<Map<String, Object>> saveDraft(@RequestBody ApprovalForm form) {
        ApprovalForm saved = formService.saveDraft(form);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", saved);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{formId}/submit")
    public ResponseEntity<Map<String, Object>> submitForm(
            @PathVariable String formId,
            @RequestParam String applicantId) {
        ApprovalForm submitted = formService.submitForm(formId, applicantId);
        Map<String, Object> result = new HashMap<>();
        if (submitted != null) {
            result.put("success", true);
            result.put("data", submitted);
        } else {
            result.put("success", false);
            result.put("message", "提交失败，请检查审批单状态和模板是否存在");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/submit-direct")
    public ResponseEntity<Map<String, Object>> submitFormDirectly(
            @RequestBody ApprovalForm form,
            @RequestParam String applicantId) {
        ApprovalForm submitted = formService.submitFormDirectly(form, applicantId);
        Map<String, Object> result = new HashMap<>();
        if (submitted != null) {
            result.put("success", true);
            result.put("data", submitted);
        } else {
            result.put("success", false);
            result.put("message", "提交失败，模板不存在");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{formId}/approve")
    public ResponseEntity<Map<String, Object>> approve(
            @PathVariable String formId,
            @RequestParam String approverId,
            @RequestParam(required = false) String comment) {
        Map<String, Object> result = formService.approve(formId, approverId, comment);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{formId}/reject")
    public ResponseEntity<Map<String, Object>> reject(
            @PathVariable String formId,
            @RequestParam String approverId,
            @RequestParam(required = false) String comment) {
        Map<String, Object> result = formService.reject(formId, approverId, comment);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{formId}/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(
            @PathVariable String formId,
            @RequestParam String applicantId) {
        Map<String, Object> result = formService.withdraw(formId, applicantId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{formId}/resubmit")
    public ResponseEntity<Map<String, Object>> resubmit(
            @PathVariable String formId,
            @RequestParam String applicantId,
            @RequestBody ApprovalForm updatedForm) {
        ApprovalForm resubmitted = formService.resubmit(formId, applicantId, updatedForm);
        Map<String, Object> result = new HashMap<>();
        if (resubmitted != null) {
            result.put("success", true);
            result.put("data", resubmitted);
        } else {
            result.put("success", false);
            result.put("message", "重新提交失败，请检查审批单状态");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{formId}")
    public ResponseEntity<Map<String, Object>> getFormById(@PathVariable String formId) {
        ApprovalForm form = formService.getFormById(formId);
        Map<String, Object> result = new HashMap<>();
        if (form != null) {
            result.put("success", true);
            result.put("data", form);
        } else {
            result.put("success", false);
            result.put("message", "审批单不存在");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{formId}/instance")
    public ResponseEntity<Map<String, Object>> getInstanceByFormId(@PathVariable String formId) {
        ApprovalInstance instance = formService.getInstanceByFormId(formId);
        Map<String, Object> result = new HashMap<>();
        if (instance != null) {
            result.put("success", true);
            result.put("data", instance);
        } else {
            result.put("success", false);
            result.put("message", "审批实例不存在");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{formId}/logs")
    public ResponseEntity<Map<String, Object>> getLogsByFormId(@PathVariable String formId) {
        List<ApprovalLog> logs = formService.getLogsByFormId(formId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", logs);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/applicant/{applicantId}")
    public ResponseEntity<Map<String, Object>> getFormsByApplicantId(@PathVariable String applicantId) {
        List<ApprovalForm> forms = formService.getFormsByApplicantId(applicantId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", forms);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/approver/{approverId}")
    public ResponseEntity<Map<String, Object>> getFormsByApproverId(@PathVariable String approverId) {
        List<ApprovalForm> forms = formService.getFormsByApproverId(approverId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", forms);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllForms() {
        List<ApprovalForm> forms = formService.getAllForms();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", forms);
        return ResponseEntity.ok(result);
    }
}
