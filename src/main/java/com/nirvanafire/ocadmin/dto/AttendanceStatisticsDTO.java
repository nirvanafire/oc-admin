package com.nirvanafire.ocadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 考勤统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatisticsDTO {
    private Long userId;
    private String username;
    private String nickname;
    private Long deptId;
    private String deptName;
    private Integer year;
    private Integer month;
    
    // 出勤统计
    private Integer workDays;        // 上班天数
    private Integer actualWorkDays;  // 实际出勤天数
    private BigDecimal attendanceRate; // 出勤率
    
    // 考勤状态统计
    private Integer normalDays;      // 正常天数
    private Integer lateDays;       // 迟到天数
    private Integer earlyLeaveDays;  // 早退天数
    private Integer absentDays;      // 缺卡天数
    
    // 请假统计
    private BigDecimal annualLeaveDays;   // 年假天数
    private BigDecimal sickLeaveDays;     // 病假天数
    private BigDecimal personalLeaveDays; // 事假天数
    private BigDecimal otherLeaveDays;    // 其他请假天数
    
    // 加班统计
    private BigDecimal overtimeHours;     // 加班时长
    private BigDecimal compensatoryHours; // 调休时长
}
