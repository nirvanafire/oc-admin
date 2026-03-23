package com.nirvanafire.ocadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String taskId;
    private Long requestId;
    private String requestTitle;  // 申请标题
    private String formData;       // 申请说明（JSON格式）
    private Long assigneeId;
    private String assigneeName;
    private String assigneeEmail;
    private String action;
    private String comment;
    private String taskStatus;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
}
