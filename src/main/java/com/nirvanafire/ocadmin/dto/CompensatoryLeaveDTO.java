package com.nirvanafire.ocadmin.dto;

import com.nirvanafire.ocadmin.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 调休申请DTO
 */
@Data
public class CompensatoryLeaveDTO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private Long overtimeRequestId;
    
    @NotNull(message = "调休日期不能为空")
    private LocalDate leaveDate;
    
    @NotNull(message = "调休时长不能为空")
    private BigDecimal durationHours;
    
    private RequestStatus status;
    private String statusDesc;
    private String approvalInstanceId;
    private Long approverId;
    private String approverName;
    private LocalDateTime approvedAt;
    private String rejectReason;
    private LocalDateTime createdAt;
}
