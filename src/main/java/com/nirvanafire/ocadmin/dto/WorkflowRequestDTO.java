package com.nirvanafire.ocadmin.dto;

import lombok.Data;

/**
 * 审批请求DTO
 */
@Data
public class WorkflowRequestDTO {
    private Long id;
    private String processInstanceId;
    private String businessKey;
    private String processKey;
    private String title;
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private Long applicantDeptId;
    private String applicantDeptName;
    private String currentNode;
    private String currentNodeName;
    private String status;
    private String statusDesc;
    private String businessType; // 业务类型: leave-请假, overtime-加班, etc.
    private Long businessId; // 关联的业务ID
    private Object businessData; // 业务数据
    private String createTime;
    private String completeTime;
}
