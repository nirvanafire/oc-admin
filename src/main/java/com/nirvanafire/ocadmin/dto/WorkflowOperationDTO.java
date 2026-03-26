package com.nirvanafire.ocadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批操作DTO
 */
@Data
public class WorkflowOperationDTO {
    
    @NotNull(message = "任务ID不能为空")
    private Long taskId;
    
    @NotBlank(message = "操作类型不能为空")
    private String operationType; // approve, reject, transfer, delegate, add_sign
    
    private String comment;
    
    private Long targetUserId;
    
    private String targetUserName;
}
