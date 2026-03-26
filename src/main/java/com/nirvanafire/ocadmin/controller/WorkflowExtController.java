package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.WorkflowOperationDTO;
import com.nirvanafire.ocadmin.dto.WorkflowStatisticsDTO;
import com.nirvanafire.ocadmin.entity.ApprovalCc;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.WorkflowExtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流扩展控制器
 */
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowExtController {

    private final WorkflowExtService workflowExtService;
    private final UserRepository userRepository;

    // ==================== 审批操作 ====================

    /**
     * 批准
     */
    @PostMapping("/tasks/{id}/approve")
    public ResponseEntity<Void> approve(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        
        WorkflowOperationDTO dto = new WorkflowOperationDTO();
        dto.setTaskId(id);
        dto.setOperationType("approve");
        dto.setComment(body != null ? body.get("comment") : null);
        
        workflowExtService.executeOperation(userId, username, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * 驳回
     */
    @PostMapping("/tasks/{id}/reject")
    public ResponseEntity<Void> reject(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        
        WorkflowOperationDTO dto = new WorkflowOperationDTO();
        dto.setTaskId(id);
        dto.setOperationType("reject");
        dto.setComment(body.get("comment"));
        
        workflowExtService.executeOperation(userId, username, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * 转交
     */
    @PostMapping("/tasks/{id}/transfer")
    public ResponseEntity<Void> transfer(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        
        WorkflowOperationDTO dto = new WorkflowOperationDTO();
        dto.setTaskId(id);
        dto.setOperationType("transfer");
        dto.setComment((String) body.get("comment"));
        
        Long targetUserId = Long.valueOf(body.get("targetUserId").toString());
        dto.setTargetUserId(targetUserId);
        
        SysUser targetUser = userRepository.findById(targetUserId).orElse(null);
        if (targetUser != null) {
            dto.setTargetUserName(targetUser.getNickname() != null ? targetUser.getNickname() : targetUser.getUsername());
        }
        
        workflowExtService.executeOperation(userId, username, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * 委派
     */
    @PostMapping("/tasks/{id}/delegate")
    public ResponseEntity<Void> delegate(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        
        WorkflowOperationDTO dto = new WorkflowOperationDTO();
        dto.setTaskId(id);
        dto.setOperationType("delegate");
        dto.setComment((String) body.get("comment"));
        
        Long targetUserId = Long.valueOf(body.get("targetUserId").toString());
        dto.setTargetUserId(targetUserId);
        
        SysUser targetUser = userRepository.findById(targetUserId).orElse(null);
        if (targetUser != null) {
            dto.setTargetUserName(targetUser.getNickname() != null ? targetUser.getNickname() : targetUser.getUsername());
        }
        
        workflowExtService.executeOperation(userId, username, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * 加签
     */
    @PostMapping("/tasks/{id}/add-sign")
    public ResponseEntity<Void> addSign(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        
        WorkflowOperationDTO dto = new WorkflowOperationDTO();
        dto.setTaskId(id);
        dto.setOperationType("add_sign");
        dto.setComment((String) body.get("comment"));
        
        Long targetUserId = Long.valueOf(body.get("targetUserId").toString());
        dto.setTargetUserId(targetUserId);
        
        SysUser targetUser = userRepository.findById(targetUserId).orElse(null);
        if (targetUser != null) {
            dto.setTargetUserName(targetUser.getNickname() != null ? targetUser.getNickname() : targetUser.getUsername());
        }
        
        workflowExtService.executeOperation(userId, username, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取操作历史
     */
    @GetMapping("/requests/{id}/operations")
    public ResponseEntity<List<Object>> getOperationHistory(@PathVariable Long id) {
        return ResponseEntity.ok(workflowExtService.getOperationHistory(id));
    }

    // ==================== 抄送 ====================

    /**
     * 获取我的抄送列表
     */
    @GetMapping("/cc")
    public ResponseEntity<Page<ApprovalCc>> getMyCcList(
            Authentication authentication,
            Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(workflowExtService.getMyCcList(userId, pageable));
    }

    /**
     * 获取未读抄送数量
     */
    @GetMapping("/cc/unread-count")
    public ResponseEntity<Long> getUnreadCcCount(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(workflowExtService.getUnreadCcCount(userId));
    }

    /**
     * 标记抄送为已读
     */
    @PutMapping("/cc/{id}/read")
    public ResponseEntity<Void> markCcAsRead(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        workflowExtService.markCcAsRead(userId, id);
        return ResponseEntity.ok().build();
    }

    // ==================== 催办 ====================

    /**
     * 手动催办
     */
    @PostMapping("/requests/{id}/remind")
    public ResponseEntity<Void> remind(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        workflowExtService.remind(userId, username, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取催办记录
     */
    @GetMapping("/requests/{id}/reminders")
    public ResponseEntity<List<Object>> getReminderHistory(@PathVariable Long id) {
        return ResponseEntity.ok(workflowExtService.getReminderHistory(id));
    }

    // ==================== 统计 ====================

    /**
     * 获取审批概览统计
     */
    @GetMapping("/statistics/overview")
    public ResponseEntity<WorkflowStatisticsDTO> getOverviewStatistics(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(workflowExtService.getOverviewStatistics(userId));
    }

    /**
     * 获取审批效率统计
     */
    @GetMapping("/statistics/efficiency")
    public ResponseEntity<WorkflowStatisticsDTO> getEfficiencyStatistics(
            Authentication authentication,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") Integer year,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") Integer month) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(workflowExtService.getEfficiencyStatistics(userId, year, month));
    }

    /**
     * 获取部门审批统计
     */
    @GetMapping("/statistics/department")
    public ResponseEntity<List<WorkflowStatisticsDTO>> getDepartmentStatistics(
            @RequestParam Long deptId,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") Integer year,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") Integer month) {
        return ResponseEntity.ok(workflowExtService.getDepartmentStatistics(deptId, year, month));
    }

    // ==================== 辅助方法 ====================

    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
}
