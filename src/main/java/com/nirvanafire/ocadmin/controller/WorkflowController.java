package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.common.Result;
import com.nirvanafire.ocadmin.dto.ApprovalRequestDTO;
import com.nirvanafire.ocadmin.dto.SubmitRequestResponse;
import com.nirvanafire.ocadmin.dto.TaskDTO;
import com.nirvanafire.ocadmin.entity.ApprovalRequest;
import com.nirvanafire.ocadmin.entity.ApprovalTask;
import com.nirvanafire.ocadmin.entity.ProcessDefinition;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.security.SecurityUtils;
import com.nirvanafire.ocadmin.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final UserRepository userRepository;

    @PostMapping("/deploy")
    @PreAuthorize("hasAuthority('workflow:deploy')")
    public Result<ProcessDefinition> deployProcess(@RequestBody Map<String, String> request) {
        String processName = request.get("processName");
        String processKey = request.get("processKey");
        String bpmnXml = request.get("bpmnXml");
        String description = request.get("description");

        ProcessDefinition result = workflowService.deployProcess(processName, processKey, bpmnXml, description);
        return Result.success(result);
    }

    @GetMapping("/definitions")
    @PreAuthorize("hasAuthority('workflow:list')")
    public Result<List<ProcessDefinition>> getDefinitions() {
        return Result.success(workflowService.getProcessDefinitions());
    }

    @DeleteMapping("/definitions/{id}")
    @PreAuthorize("hasAuthority('workflow:delete')")
    public Result<Void> deleteDefinition(@PathVariable Long id) {
        workflowService.deleteProcessDefinition(id);
        return Result.success();
    }

    @PutMapping("/definitions/{id}")
    @PreAuthorize("hasAuthority('workflow:deploy')")
    public Result<ProcessDefinition> updateDefinition(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String processName = request.get("processName");
        String processKey = request.get("processKey");
        String description = request.get("description");

        ProcessDefinition result = workflowService.updateProcessDefinition(id, processName, processKey, description);
        return Result.success(result);
    }

    @PostMapping("/definitions/save")
    @PreAuthorize("hasAuthority('workflow:deploy')")
    public Result<ProcessDefinition> saveDefinition(@RequestBody Map<String, Object> request) {
        Long id = request.get("id") != null ? Long.valueOf(request.get("id").toString()) : null;
        String processName = (String) request.get("processName");
        String processKey = (String) request.get("processKey");
        String bpmnXml = (String) request.get("bpmnXml");
        String description = (String) request.get("description");

        ProcessDefinition result = workflowService.saveProcessDefinition(id, processName, processKey, bpmnXml, description);
        return Result.success(result);
    }

    @GetMapping("/definitions/{id}/deployed")
    @PreAuthorize("hasAuthority('workflow:list')")
    public Result<Boolean> isDeployed(@PathVariable Long id) {
        return Result.success(workflowService.isProcessDeployed(id));
    }

    @PostMapping("/definitions/{id}/deploy")
    @PreAuthorize("hasAuthority('workflow:deploy')")
    public Result<ProcessDefinition> deployDefinition(@PathVariable Long id) {
        ProcessDefinition result = workflowService.deployDefinition(id);
        return Result.success(result);
    }

    @PostMapping("/definitions/batch-deploy")
    @PreAuthorize("hasAuthority('workflow:deploy')")
    public Result<List<ProcessDefinition>> batchDeployDefinition(@RequestBody List<Long> ids) {
        List<ProcessDefinition> results = workflowService.batchDeployDefinition(ids);
        return Result.success(results);
    }

    @GetMapping("/definitions/deployed/all")
    public Result<List<Map<String, Object>>> getAllDeployedProcesses() {
        return Result.success(workflowService.getAllDeployedProcesses());
    }

    @PostMapping("/requests")
    @PreAuthorize("hasAuthority('workflow:request')")
    public Result<SubmitRequestResponse> submitRequest(@RequestBody Map<String, Object> request) {
        String username = SecurityUtils.getCurrentUsername();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String title = (String) request.get("title");
        String processKey = (String) request.get("processKey");
        @SuppressWarnings("unchecked")
        Map<String, Object> formData = (Map<String, Object>) request.get("formData");

        SubmitRequestResponse result = workflowService.submitRequest(
                user.getId(),
                user.getNickname() != null ? user.getNickname() : user.getUsername(),
                user.getEmail(),
                title,
                processKey,
                formData
        );
        return Result.success(result);
    }

    @GetMapping("/requests/my")
    @PreAuthorize("hasAuthority('workflow:request')")
    public Result<List<ApprovalRequest>> getMyRequests() {
        String username = SecurityUtils.getCurrentUsername();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return Result.success(workflowService.getMyRequests(user.getId()));
    }

    @GetMapping("/requests/{id}")
    @PreAuthorize("hasAuthority('workflow:request')")
    public Result<ApprovalRequestDTO> getRequest(@PathVariable Long id) {
        return Result.success(workflowService.getRequest(id));
    }

    @GetMapping("/requests/{id}/tasks")
    @PreAuthorize("hasAuthority('workflow:request')")
    public Result<List<TaskDTO>> getRequestTasks(@PathVariable Long id) {
        return Result.success(workflowService.getRequestTasks(id));
    }

    @PostMapping("/requests/{id}/cancel")
    @PreAuthorize("hasAuthority('workflow:request')")
    public Result<ApprovalRequest> cancelRequest(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return Result.success(workflowService.cancelRequest(id, user.getId()));
    }

    @PostMapping("/requests/{id}/withdraw")
    @PreAuthorize("hasAuthority('workflow:request')")
    public Result<ApprovalRequest> withdrawRequest(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return Result.success(workflowService.withdrawRequest(id, user.getId()));
    }

    @GetMapping("/tasks/my")
    @PreAuthorize("hasAuthority('workflow:approve')")
    public Result<List<TaskDTO>> getMyTasks(@RequestParam(required = false) String taskStatus) {
        String username = SecurityUtils.getCurrentUsername();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return Result.success(workflowService.getMyTasks(user.getId(), taskStatus));
    }

    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("hasAuthority('workflow:approve')")
    public Result<TaskDTO> getTask(@PathVariable String taskId) {
        return Result.success(workflowService.getTask(taskId));
    }

    @PostMapping("/tasks/{taskId}/complete")
    @PreAuthorize("hasAuthority('workflow:approve')")
    public Result<ApprovalTask> completeTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        String username = SecurityUtils.getCurrentUsername();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String action = request.get("action");
        String comment = request.get("comment");

        ApprovalTask result = workflowService.completeTask(
                taskId,
                user.getId(),
                user.getNickname() != null ? user.getNickname() : user.getUsername(),
                action,
                comment
        );
        return Result.success(result);
    }
}
