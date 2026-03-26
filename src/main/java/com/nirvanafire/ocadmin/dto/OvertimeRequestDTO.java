package com.nirvanafire.ocadmin.dto;

import com.nirvanafire.ocadmin.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 加班申请DTO
 */
@Data
public class OvertimeRequestDTO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    
    @NotNull(message = "加班日期不能为空")
    private LocalDate overtimeDate;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    
    private BigDecimal durationHours;
    private String reason;
    private RequestStatus status;
    private String statusDesc;
    private String approvalInstanceId;
    private Long approverId;
    private String approverName;
    private LocalDateTime approvedAt;
    private String rejectReason;
    private LocalDateTime createdAt;
}
