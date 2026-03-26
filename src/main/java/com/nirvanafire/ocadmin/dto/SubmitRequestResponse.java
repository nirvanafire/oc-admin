package com.nirvanafire.ocadmin.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitRequestResponse {

    private Long id;
    private String processInstanceId;
    private String businessKey;
    private String processKey;
    private String title;
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private String currentNode;
    private String currentNodeName;
    private String status;
    private String formData;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime completeTime;

    // 下一个审核任务信息
    private List<PendingTaskInfo> pendingTasks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PendingTaskInfo {
        private String taskId;
        private Long assigneeId;
        private String assigneeName;
        private String assigneeEmail;
    }
}
