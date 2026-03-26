package com.nirvanafire.ocadmin.dto;

import lombok.Data;

/**
 * 审批任务DTO
 */
@Data
public class WorkflowTaskDTO {
    private Long id;
    private String taskId;
    private Long requestId;
    private String requestTitle;
    private Long assigneeId;
    private String assigneeName;
    private String assigneeEmail;
    private String action;
    private String comment;
    private String taskStatus;
    private String taskStatusDesc;
    private String createTime;
    private String completeTime;
}
