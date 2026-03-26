package com.nirvanafire.ocadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 审批统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStatisticsDTO {
    // 概览统计
    private Long totalRequests;        // 总申请数
    private Long pendingRequests;      // 待审批数
    private Long approvedRequests;    // 已批准数
    private Long rejectedRequests;    // 已驳回数
    private Long completedRequests;   // 已完成数
    
    // 效率统计
    private BigDecimal avgProcessTime; // 平均处理时长（小时）
    private BigDecimal approvalRate;  // 批准率
    private BigDecimal rejectionRate; // 驳回率
    
    // 分类统计
    private Long leaveRequests;       // 请假申请数
    private Long overtimeRequests;    // 加班申请数
    private Long otherRequests;       // 其他申请数
    
    // 部门统计
    private Long deptId;
    private String deptName;
    private Long deptRequestCount;
    private BigDecimal deptApprovalRate;
}
