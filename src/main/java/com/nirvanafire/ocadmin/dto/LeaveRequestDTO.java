package com.nirvanafire.ocadmin.dto;

import com.nirvanafire.ocadmin.enums.LeaveType;
import com.nirvanafire.ocadmin.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请DTO
 */
@Data
public class LeaveRequestDTO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    
    @NotNull(message = "请假类型不能为空")
    private LeaveType leaveType;
    
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    
    private BigDecimal totalDays;
    private String reason;
    private String attachmentUrl;
    private RequestStatus status;
    private String statusDesc;
    private String approvalInstanceId;
    private Long approverId;
    private String approverName;
    private LocalDateTime approvedAt;
    private String rejectReason;
    private LocalDateTime createdAt;
}
