package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.WorkflowOperationDTO;
import com.nirvanafire.ocadmin.dto.WorkflowStatisticsDTO;
import com.nirvanafire.ocadmin.entity.ApprovalCc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 工作流扩展服务接口
 */
public interface WorkflowExtService {
    
    // ==================== 审批操作 ====================
    
    /**
     * 执行审批操作（批准/驳回/转交/委派/加签）
     */
    void executeOperation(Long userId, String username, WorkflowOperationDTO dto);
    
    /**
     * 获取申请的操作记录
     */
    List<Object> getOperationHistory(Long requestId);
    
    // ==================== 抄送 ====================
    
    /**
     * 添加抄送
     */
    void addCc(Long requestId, List<Long> userIds);
    
    /**
     * 获取我的抄送列表
     */
    Page<ApprovalCc> getMyCcList(Long userId, Pageable pageable);
    
    /**
     * 获取未读抄送数量
     */
    Long getUnreadCcCount(Long userId);
    
    /**
     * 标记抄送为已读
     */
    void markCcAsRead(Long userId, Long ccId);
    
    // ==================== 催办 ====================
    
    /**
     * 手动催办
     */
    void remind(Long userId, String username, Long requestId);
    
    /**
     * 获取催办记录
     */
    List<Object> getReminderHistory(Long requestId);
    
    // ==================== 统计 ====================
    
    /**
     * 获取审批概览统计
     */
    WorkflowStatisticsDTO getOverviewStatistics(Long userId);
    
    /**
     * 获取审批效率统计
     */
    WorkflowStatisticsDTO getEfficiencyStatistics(Long userId, Integer year, Integer month);
    
    /**
     * 获取部门审批统计
     */
    List<WorkflowStatisticsDTO> getDepartmentStatistics(Long deptId, Integer year, Integer month);
}
