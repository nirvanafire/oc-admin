package com.nirvanafire.ocadmin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 考勤规则DTO
 */
@Data
public class AttendanceRuleDTO {
    private Long id;
    private String ruleName;
    private LocalTime workStartTime;
    private LocalTime workEndTime;
    private Integer flexibleMinutes;
    private Integer lateThresholdMinutes;
    private Integer earlyLeaveThresholdMinutes;
    private BigDecimal minWorkHours;
    private Boolean isDefault;
    private Boolean enabled;
    private String remark;
}
