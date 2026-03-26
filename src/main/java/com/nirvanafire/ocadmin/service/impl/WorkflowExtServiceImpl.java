package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.WorkflowOperationDTO;
import com.nirvanafire.ocadmin.dto.WorkflowStatisticsDTO;
import com.nirvanafire.ocadmin.entity.*;
import com.nirvanafire.ocadmin.repository.*;
import com.nirvanafire.ocadmin.service.WorkflowExtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流扩展服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExtServiceImpl implements WorkflowExtService {

    private final ApprovalOperationRepository approvalOperationRepository;
    private final ApprovalCcRepository approvalCcRepository;
    private final ApprovalReminderRepository approvalReminderRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalTaskRepository approvalTaskRepository;
    private final UserRepository userRepository;

    // ==================== 审批操作 ====================

    @Override
    @Transactional
    public void executeOperation(Long userId, String username, WorkflowOperationDTO dto) {
        ApprovalTask task = approvalTaskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new BusinessException("任务不存在"));

        if (!task.getAssigneeId().equals(userId)) {
            throw new BusinessException("无权操作此任务");
        }

        if (!"PENDING".equals(task.getTaskStatus())) {
            throw new BusinessException("任务已处理，不能重复操作");
        }

        String operationType = dto.getOperationType();
        
        // 执行操作
        switch (operationType) {
            case "approve":
                task.setAction("APPROVE");
                task.setComment(dto.getComment());
                task.setTaskStatus("COMPLETED");
                task.setCompleteTime(LocalDateTime.now());
                break;
            case "reject":
                task.setAction("REJECT");
                task.setComment(dto.getComment());
                task.setTaskStatus("COMPLETED");
                task.setCompleteTime(LocalDateTime.now());
                break;
            case "transfer":
                // 转交操作
                if (dto.getTargetUserId() == null) {
                    throw new BusinessException("转交目标用户不能为空");
                }
                task.setAction("TRANSFER");
                task.setAssigneeId(dto.getTargetUserId());
                task.setAssigneeName(dto.getTargetUserName());
                task.setComment(dto.getComment());
                break;
            case "delegate":
                // 委派操作
                if (dto.getTargetUserId() == null) {
                    throw new BusinessException("委派目标用户不能为空");
                }
                task.setAction("DELEGATE");
                task.setComment(dto.getComment());
                // 委派创建新任务
                break;
            case "add_sign":
                // 加签操作
                if (dto.getTargetUserId() == null) {
                    throw new BusinessException("加签目标用户不能为空");
                }
                task.setAction("ADD_SIGN");
                task.setComment(dto.getComment());
                break;
            default:
                throw new BusinessException("不支持的操作类型: " + operationType);
        }

        approvalTaskRepository.save(task);

        // 记录操作
        ApprovalOperation operation = ApprovalOperation.builder()
                .taskId(task.getId())
                .requestId(task.getRequestId())
                .operatorId(userId)
                .operatorName(username)
                .operationType(operationType)
                .comment(dto.getComment())
                .targetUserId(dto.getTargetUserId())
                .targetUserName(dto.getTargetUserName())
                .build();

        approvalOperationRepository.save(operation);
    }

    @Override
    public List<Object> getOperationHistory(Long requestId) {
        List<ApprovalOperation> operations = approvalOperationRepository.findByRequestIdOrderByCreateTimeDesc(requestId);
        
        return operations.stream().map(op -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", op.getId());
            map.put("operatorId", op.getOperatorId());
            map.put("operatorName", op.getOperatorName());
            map.put("operationType", op.getOperationType());
            map.put("operationTypeDesc", getOperationTypeDesc(op.getOperationType()));
            map.put("comment", op.getComment());
            map.put("targetUserName", op.getTargetUserName());
            map.put("createTime", op.getCreateTime());
            return (Object) map;
        }).collect(Collectors.toList());
    }

    private String getOperationTypeDesc(String type) {
        return switch (type) {
            case "approve" -> "批准";
            case "reject" -> "驳回";
            case "transfer" -> "转交";
            case "delegate" -> "委派";
            case "add_sign" -> "加签";
            default -> type;
        };
    }

    // ==================== 抄送 ====================

    @Override
    @Transactional
    public void addCc(Long requestId, List<Long> userIds) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("申请不存在"));

        for (Long userId : userIds) {
            SysUser user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;

            ApprovalCc cc = ApprovalCc.builder()
                    .requestId(requestId)
                    .userId(userId)
                    .userName(user.getNickname() != null ? user.getNickname() : user.getUsername())
                    .build();

            approvalCcRepository.save(cc);
        }
    }

    @Override
    public Page<ApprovalCc> getMyCcList(Long userId, Pageable pageable) {
        return approvalCcRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
    }

    @Override
    public Long getUnreadCcCount(Long userId) {
        return approvalCcRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markCcAsRead(Long userId, Long ccId) {
        approvalCcRepository.markAsRead(ccId, userId);
    }

    // ==================== 催办 ====================

    @Override
    @Transactional
    public void remind(Long userId, String username, Long requestId) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("申请不存在"));

        if (!request.getApplicantId().equals(userId)) {
            throw new BusinessException("只能催办自己的申请");
        }

        if ("COMPLETED".equals(request.getStatus()) || "CANCELLED".equals(request.getStatus())) {
            throw new BusinessException("申请已完成，不能催办");
        }

        // 检查催办次数
        Integer maxCount = approvalReminderRepository.getMaxReminderCount(requestId);
        int reminderCount = maxCount != null ? maxCount + 1 : 1;

        if (reminderCount > 3) {
            throw new BusinessException("催办次数已达上限（3次）");
        }

        // 获取当前待审批任务
        List<ApprovalTask> pendingTasks = approvalTaskRepository.findByRequestIdAndTaskStatus(requestId, "PENDING");
        
        ApprovalTask task = pendingTasks.isEmpty() ? null : pendingTasks.get(0);

        ApprovalReminder reminder = ApprovalReminder.builder()
                .requestId(requestId)
                .taskId(task != null ? task.getId() : null)
                .reminderUserId(userId)
                .reminderUserName(username)
                .reminderTime(LocalDateTime.now())
                .reminderCount(reminderCount)
                .build();

        approvalReminderRepository.save(reminder);

        // TODO: 发送催办通知
        log.info("催办申请: requestId={}, user={}, count={}", requestId, username, reminderCount);
    }

    @Override
    public List<Object> getReminderHistory(Long requestId) {
        List<ApprovalReminder> reminders = approvalReminderRepository.findByRequestIdOrderByCreateTimeDesc(requestId);
        
        return reminders.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("reminderUserName", r.getReminderUserName());
            map.put("reminderCount", r.getReminderCount());
            map.put("createTime", r.getCreateTime());
            return (Object) map;
        }).collect(Collectors.toList());
    }

    // ==================== 统计 ====================

    @Override
    public WorkflowStatisticsDTO getOverviewStatistics(Long userId) {
        List<ApprovalRequest> requests = approvalRequestRepository.findByApplicantId(userId);
        
        long total = requests.size();
        long pending = requests.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long approved = requests.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count();
        long rejected = requests.stream().filter(r -> "REJECTED".equals(r.getStatus())).count();

        BigDecimal approvalRate = total > 0 
                ? BigDecimal.valueOf(approved * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal rejectionRate = total > 0 
                ? BigDecimal.valueOf(rejected * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return WorkflowStatisticsDTO.builder()
                .totalRequests(total)
                .pendingRequests(pending)
                .approvedRequests(approved)
                .rejectedRequests(rejected)
                .completedRequests(approved)
                .approvalRate(approvalRate)
                .rejectionRate(rejectionRate)
                .build();
    }

    @Override
    public WorkflowStatisticsDTO getEfficiencyStatistics(Long userId, Integer year, Integer month) {
        // 获取指定月份的审批任务
        LocalDateTime startTime = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endTime = startTime.plusMonths(1);
        
        List<ApprovalTask> tasks = approvalTaskRepository.findByAssigneeIdAndCreateTimeBetween(
                userId, startTime, endTime);

        long total = tasks.size();
        long completed = tasks.stream().filter(t -> "COMPLETED".equals(t.getTaskStatus())).count();
        
        // 计算平均处理时长
        List<ApprovalTask> completedTasks = tasks.stream()
                .filter(t -> t.getCompleteTime() != null)
                .collect(Collectors.toList());
        
        BigDecimal avgProcessTime = BigDecimal.ZERO;
        if (!completedTasks.isEmpty()) {
            double totalHours = completedTasks.stream()
                    .mapToLong(t -> ChronoUnit.MINUTES.between(t.getCreateTime(), t.getCompleteTime()))
                    .sum();
            avgProcessTime = BigDecimal.valueOf(totalHours / 60.0 / completedTasks.size())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        long approved = completedTasks.stream()
                .filter(t -> "APPROVE".equals(t.getAction())).count();
        long rejected = completedTasks.stream()
                .filter(t -> "REJECT".equals(t.getAction())).count();

        BigDecimal approvalRate = completed > 0 
                ? BigDecimal.valueOf(approved * 100.0 / completed).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal rejectionRate = completed > 0 
                ? BigDecimal.valueOf(rejected * 100.0 / completed).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return WorkflowStatisticsDTO.builder()
                .totalRequests(total)
                .completedRequests(completed)
                .avgProcessTime(avgProcessTime)
                .approvalRate(approvalRate)
                .rejectionRate(rejectionRate)
                .build();
    }

    @Override
    public List<WorkflowStatisticsDTO> getDepartmentStatistics(Long deptId, Integer year, Integer month) {
        // TODO: 实现部门统计
        return List.of();
    }
}
