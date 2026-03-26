package com.nirvanafire.ocadmin.dto;

import com.nirvanafire.ocadmin.enums.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录DTO
 */
@Data
public class AttendanceRecordDTO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private LocalDate recordDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String checkInDevice;
    private String checkOutDevice;
    private String checkInLocation;
    private String checkOutLocation;
    private AttendanceStatus status;
    private String statusDesc;
    private String remark;
    private LocalDateTime createdAt;
}
