package com.nirvanafire.ocadmin.dto;

import com.nirvanafire.ocadmin.entity.ApprovalRequest;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequestDTO {

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

    public static ApprovalRequestDTO fromEntity(ApprovalRequest request, String formData) {
        if (request == null) return null;
        return ApprovalRequestDTO.builder()
                .id(request.getId())
                .processInstanceId(request.getProcessInstanceId())
                .businessKey(request.getBusinessKey())
                .processKey(request.getProcessKey())
                .title(request.getTitle())
                .applicantId(request.getApplicantId())
                .applicantName(request.getApplicantName())
                .applicantEmail(request.getApplicantEmail())
                .currentNode(request.getCurrentNode())
                .currentNodeName(request.getCurrentNodeName())
                .status(request.getStatus())
                .formData(formData)
                .createTime(request.getCreateTime())
                .updateTime(request.getUpdateTime())
                .completeTime(request.getCompleteTime())
                .build();
    }
}
