package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.*;
import com.nirvanafire.ocadmin.entity.*;
import com.nirvanafire.ocadmin.enums.*;
import com.nirvanafire.ocadmin.repository.*;
import com.nirvanafire.ocadmin.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考勤服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final CompensatoryLeaveRepository compensatoryLeaveRepository;
    private final AttendanceRuleRepository attendanceRuleRepository;
    private final OvertimeBalanceRepository overtimeBalanceRepository;
    private final AnnualLeaveBalanceRepository annualLeaveBalanceRepository;
    private final UserRepository userRepository;

    // ==================== 打卡签到 ====================

    @Override
    @Transactional
    public AttendanceRecordDTO checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        
        // 获取或创建今日考勤记录
        AttendanceRecord record = attendanceRecordRepository.findByUserIdAndRecordDate(userId, today)
                .orElse(AttendanceRecord.builder()
                        .userId(userId)
                        .recordDate(today)
                        .status(AttendanceStatus.NORMAL)
                        .build());
        
        if (record.getCheckInTime() != null) {
            throw new BusinessException("今日已签到");
        }
        
        record.setCheckInTime(now);
        record.setCheckInDevice("PC");
        
        // 计算考勤状态
        updateAttendanceStatus(record);
        
        record = attendanceRecordRepository.save(record);
        return toRecordDTO(record);
    }

    @Override
    @Transactional
    public AttendanceRecordDTO checkOut(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        
        AttendanceRecord record = attendanceRecordRepository.findByUserIdAndRecordDate(userId, today)
                .orElseThrow(() -> new BusinessException("请先签到"));
        
        if (record.getCheckOutTime() != null) {
            throw new BusinessException("今日已签退");
        }
        
        record.setCheckOutTime(now);
        record.setCheckOutDevice("PC");
        
        // 计算考勤状态
        updateAttendanceStatus(record);
        
        record = attendanceRecordRepository.save(record);
        return toRecordDTO(record);
    }

    private void updateAttendanceStatus(AttendanceRecord record) {
        AttendanceRule rule = attendanceRuleRepository.findByIsDefaultTrueAndEnabledTrue()
                .orElse(AttendanceRule.builder()
                        .workStartTime(LocalTime.of(9, 0))
                        .workEndTime(LocalTime.of(18, 0))
                        .lateThresholdMinutes(15)
                        .earlyLeaveThresholdMinutes(15)
                        .build());
        
        // 检查签到状态
        if (record.getCheckInTime() != null) {
            LocalTime checkInTime = record.getCheckInTime().toLocalTime();
            LocalTime workStartTime = rule.getWorkStartTime();
            int flexibleMinutes = rule.getFlexibleMinutes();
            
            if (checkInTime.isAfter(workStartTime.plusMinutes(flexibleMinutes + rule.getLateThresholdMinutes()))) {
                record.setStatus(AttendanceStatus.LATE);
            }
        }
        
        // 检查签退状态
        if (record.getCheckOutTime() != null) {
            LocalTime checkOutTime = record.getCheckOutTime().toLocalTime();
            LocalTime workEndTime = rule.getWorkEndTime();
            int flexibleMinutes = rule.getFlexibleMinutes();
            
            if (checkOutTime.isBefore(workEndTime.minusMinutes(flexibleMinutes + rule.getEarlyLeaveThresholdMinutes()))) {
                record.setStatus(AttendanceStatus.EARLY_LEAVE);
            }
        }
        
        // 如果没有签到或签退
        if (record.getCheckInTime() == null && record.getCheckOutTime() == null) {
            record.setStatus(AttendanceStatus.ABSENT);
        }
    }

    // ==================== 考勤记录 ====================

    @Override
    public Page<AttendanceRecordDTO> getRecords(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<AttendanceRecord> page;
        if (startDate != null && endDate != null) {
            page = attendanceRecordRepository.findByUserIdAndRecordDateBetween(userId, startDate, endDate, pageable);
        } else {
            page = attendanceRecordRepository.findByUserId(userId, pageable);
        }
        
        List<AttendanceRecordDTO> list = page.getContent().stream()
                .map(this::toRecordDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public AttendanceRecordDTO getRecordById(Long id) {
        AttendanceRecord record = attendanceRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("考勤记录不存在"));
        return toRecordDTO(record);
    }

    @Override
    public List<AttendanceRecordDTO> getCalendar(Long userId, int year, int month) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByUserIdAndYearAndMonth(userId, year, month);
        
        // 填充没有记录的日期
        List<AttendanceRecordDTO> result = records.stream()
                .map(this::toRecordDTO)
                .collect(Collectors.toList());
        
        return result;
    }

    // ==================== 请假申请 ====================

    @Override
    @Transactional
    public LeaveRequestDTO createLeave(Long userId, LeaveRequestDTO dto) {
        // 验证日期
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        
        // 计算请假天数
        long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        dto.setTotalDays(BigDecimal.valueOf(days));
        
        // 年假需要检查余额
        if (dto.getLeaveType() == LeaveType.ANNUAL) {
            int year = dto.getStartDate().getYear();
            BalanceDTO balance = getAnnualLeaveBalance(userId, year);
            if (balance.getAvailable().compareTo(BigDecimal.valueOf(days)) < 0) {
                throw new BusinessException("年假余额不足");
            }
        }
        
        LeaveRequest leave = toLeaveEntity(dto);
        leave.setUserId(userId);
        leave.setStatus(RequestStatus.PENDING);
        
        // TODO: 触发审批流程
        
        leave = leaveRequestRepository.save(leave);
        return toLeaveDTO(leave);
    }

    @Override
    public Page<LeaveRequestDTO> getLeaveList(Long userId, Pageable pageable) {
        Page<LeaveRequest> page = leaveRequestRepository.findByUserId(userId, pageable);
        
        List<LeaveRequestDTO> list = page.getContent().stream()
                .map(this::toLeaveDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public LeaveRequestDTO getLeaveById(Long id) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("请假记录不存在"));
        return toLeaveDTO(leave);
    }

    @Override
    @Transactional
    public void cancelLeave(Long userId, Long id) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("请假记录不存在"));
        
        if (!leave.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        if (leave.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("只能取消待审批的申请");
        }
        
        leave.setStatus(RequestStatus.CANCELLED);
        leaveRequestRepository.save(leave);
    }

    // ==================== 加班申请 ====================

    @Override
    @Transactional
    public OvertimeRequestDTO createOvertime(Long userId, OvertimeRequestDTO dto) {
        // 验证时间
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
        
        // 计算加班时长
        long minutes = Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
        
        if (hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("加班时长必须大于0");
        }
        
        dto.setDurationHours(hours);
        
        OvertimeRequest overtime = toOvertimeEntity(dto);
        overtime.setUserId(userId);
        overtime.setStatus(RequestStatus.PENDING);
        
        // TODO: 触发审批流程
        
        overtime = overtimeRequestRepository.save(overtime);
        return toOvertimeDTO(overtime);
    }

    @Override
    public Page<OvertimeRequestDTO> getOvertimeList(Long userId, Pageable pageable) {
        Page<OvertimeRequest> page = overtimeRequestRepository.findByUserId(userId, pageable);
        
        List<OvertimeRequestDTO> list = page.getContent().stream()
                .map(this::toOvertimeDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public OvertimeRequestDTO getOvertimeById(Long id) {
        OvertimeRequest overtime = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("加班记录不存在"));
        return toOvertimeDTO(overtime);
    }

    @Override
    @Transactional
    public void cancelOvertime(Long userId, Long id) {
        OvertimeRequest overtime = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("加班记录不存在"));
        
        if (!overtime.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        if (overtime.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("只能取消待审批的申请");
        }
        
        overtime.setStatus(RequestStatus.CANCELLED);
        overtimeRequestRepository.save(overtime);
    }

    // ==================== 调休申请 ====================

    @Override
    @Transactional
    public CompensatoryLeaveDTO createCompensatoryLeave(Long userId, CompensatoryLeaveDTO dto) {
        // 检查调休余额
        int year = dto.getLeaveDate().getYear();
        BalanceDTO balance = getOvertimeBalance(userId, year);
        
        if (balance.getAvailable().compareTo(dto.getDurationHours()) < 0) {
            throw new BusinessException("调休余额不足");
        }
        
        // 如果指定了加班申请，验证是否可用
        if (dto.getOvertimeRequestId() != null) {
            OvertimeRequest overtime = overtimeRequestRepository.findById(dto.getOvertimeRequestId())
                    .orElseThrow(() -> new BusinessException("加班记录不存在"));
            
            if (!overtime.getUserId().equals(userId)) {
                throw new BusinessException("无权使用该加班记录");
            }
            
            if (overtime.getStatus() != RequestStatus.APPROVED) {
                throw new BusinessException("该加班申请未通过审批");
            }
        }
        
        CompensatoryLeave leave = toCompensatoryEntity(dto);
        leave.setUserId(userId);
        leave.setStatus(RequestStatus.PENDING);
        
        // TODO: 触发审批流程
        
        leave = compensatoryLeaveRepository.save(leave);
        return toCompensatoryDTO(leave);
    }

    @Override
    public Page<CompensatoryLeaveDTO> getCompensatoryList(Long userId, Pageable pageable) {
        Page<CompensatoryLeave> page = compensatoryLeaveRepository.findByUserId(userId, pageable);
        
        List<CompensatoryLeaveDTO> list = page.getContent().stream()
                .map(this::toCompensatoryDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public CompensatoryLeaveDTO getCompensatoryById(Long id) {
        CompensatoryLeave leave = compensatoryLeaveRepository.findById(id)
                .orElseThrow(() -> new BusinessException("调休记录不存在"));
        return toCompensatoryDTO(leave);
    }

    @Override
    @Transactional
    public void cancelCompensatory(Long userId, Long id) {
        CompensatoryLeave leave = compensatoryLeaveRepository.findById(id)
                .orElseThrow(() -> new BusinessException("调休记录不存在"));
        
        if (!leave.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        if (leave.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("只能取消待审批的申请");
        }
        
        leave.setStatus(RequestStatus.CANCELLED);
        compensatoryLeaveRepository.save(leave);
    }

    // ==================== 余额查询 ====================

    @Override
    public BalanceDTO getAnnualLeaveBalance(Long userId, int year) {
        AnnualLeaveBalance balance = annualLeaveBalanceRepository.findByUserIdAndYear(userId, year)
                .orElse(AnnualLeaveBalance.builder()
                        .userId(userId)
                        .year(year)
                        .totalDays(BigDecimal.valueOf(15))
                        .usedDays(BigDecimal.ZERO)
                        .availableDays(BigDecimal.valueOf(15))
                        .build());
        
        return BalanceDTO.builder()
                .userId(userId)
                .year(year)
                .total(balance.getTotalDays())
                .used(balance.getUsedDays())
                .available(balance.getAvailableDays())
                .build();
    }

    @Override
    public BalanceDTO getOvertimeBalance(Long userId, int year) {
        OvertimeBalance balance = overtimeBalanceRepository.findByUserIdAndYear(userId, year)
                .orElse(OvertimeBalance.builder()
                        .userId(userId)
                        .year(year)
                        .totalHours(BigDecimal.ZERO)
                        .usedHours(BigDecimal.ZERO)
                        .availableHours(BigDecimal.ZERO)
                        .build());
        
        return BalanceDTO.builder()
                .userId(userId)
                .year(year)
                .total(balance.getTotalHours())
                .used(balance.getUsedHours())
                .available(balance.getAvailableHours())
                .build();
    }

    // ==================== 考勤统计 ====================

    @Override
    public AttendanceStatisticsDTO getPersonalStatistics(Long userId, int year, int month) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByUserIdAndYearAndMonth(userId, year, month);
        
        // 统计考勤记录
        int normalDays = 0, lateDays = 0, earlyLeaveDays = 0, absentDays = 0;
        for (AttendanceRecord record : records) {
            switch (record.getStatus()) {
                case NORMAL -> normalDays++;
                case LATE -> lateDays++;
                case EARLY_LEAVE -> earlyLeaveDays++;
                case ABSENT -> absentDays++;
            }
        }
        
        // 统计请假
        List<LeaveRequest> leaves = leaveRequestRepository.findByUserIdAndYearAndMonth(userId, year, month);
        BigDecimal annualLeaveDays = BigDecimal.ZERO;
        BigDecimal sickLeaveDays = BigDecimal.ZERO;
        BigDecimal personalLeaveDays = BigDecimal.ZERO;
        BigDecimal otherLeaveDays = BigDecimal.ZERO;
        
        for (LeaveRequest leave : leaves) {
            if (leave.getStatus() == RequestStatus.APPROVED) {
                switch (leave.getLeaveType()) {
                    case ANNUAL -> annualLeaveDays = annualLeaveDays.add(leave.getTotalDays());
                    case SICK -> sickLeaveDays = sickLeaveDays.add(leave.getTotalDays());
                    case PERSONAL -> personalLeaveDays = personalLeaveDays.add(leave.getTotalDays());
                    default -> otherLeaveDays = otherLeaveDays.add(leave.getTotalDays());
                }
            }
        }
        
        // 统计加班
        List<OvertimeRequest> overtimes = overtimeRequestRepository.findByUserIdAndYearAndMonth(userId, year, month);
        BigDecimal overtimeHours = BigDecimal.ZERO;
        for (OvertimeRequest overtime : overtimes) {
            if (overtime.getStatus() == RequestStatus.APPROVED) {
                overtimeHours = overtimeHours.add(overtime.getDurationHours());
            }
        }
        
        // 统计调休
        List<CompensatoryLeave> compensatories = compensatoryLeaveRepository.findByUserIdAndYearAndMonth(userId, year, month);
        BigDecimal compensatoryHours = BigDecimal.ZERO;
        for (CompensatoryLeave cl : compensatories) {
            if (cl.getStatus() == RequestStatus.APPROVED) {
                compensatoryHours = compensatoryHours.add(cl.getDurationHours());
            }
        }
        
        int workDays = LocalDate.of(year, month, 1).lengthOfMonth();
        int actualWorkDays = workDays - absentDays;
        BigDecimal attendanceRate = workDays > 0 
                ? BigDecimal.valueOf(actualWorkDays).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(workDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        SysUser user = userRepository.findById(userId).orElse(null);
        
        return AttendanceStatisticsDTO.builder()
                .userId(userId)
                .username(user != null ? user.getUsername() : null)
                .nickname(user != null ? user.getNickname() : null)
                .year(year)
                .month(month)
                .workDays(workDays)
                .actualWorkDays(actualWorkDays)
                .attendanceRate(attendanceRate)
                .normalDays(normalDays)
                .lateDays(lateDays)
                .earlyLeaveDays(earlyLeaveDays)
                .absentDays(absentDays)
                .annualLeaveDays(annualLeaveDays)
                .sickLeaveDays(sickLeaveDays)
                .personalLeaveDays(personalLeaveDays)
                .otherLeaveDays(otherLeaveDays)
                .overtimeHours(overtimeHours)
                .compensatoryHours(compensatoryHours)
                .build();
    }

    @Override
    public List<AttendanceStatisticsDTO> getDepartmentStatistics(Long deptId, int year, int month) {
        // TODO: 实现部门统计
        return List.of();
    }

    // ==================== 考勤规则 ====================

    @Override
    public AttendanceRuleDTO getAttendanceRule() {
        AttendanceRule rule = attendanceRuleRepository.findByIsDefaultTrueAndEnabledTrue()
                .orElse(AttendanceRule.builder()
                        .ruleName("默认规则")
                        .workStartTime(LocalTime.of(9, 0))
                        .workEndTime(LocalTime.of(18, 0))
                        .flexibleMinutes(0)
                        .lateThresholdMinutes(15)
                        .earlyLeaveThresholdMinutes(15)
                        .minWorkHours(BigDecimal.valueOf(8.0))
                        .isDefault(true)
                        .enabled(true)
                        .build());
        
        return toRuleDTO(rule);
    }

    @Override
    @Transactional
    public AttendanceRuleDTO updateAttendanceRule(AttendanceRuleDTO dto) {
        AttendanceRule rule = attendanceRuleRepository.findByIsDefaultTrue()
                .orElse(null);
        
        if (rule == null) {
            rule = new AttendanceRule();
            rule.setIsDefault(true);
        }
        
        rule.setRuleName(dto.getRuleName());
        rule.setWorkStartTime(dto.getWorkStartTime());
        rule.setWorkEndTime(dto.getWorkEndTime());
        rule.setFlexibleMinutes(dto.getFlexibleMinutes());
        rule.setLateThresholdMinutes(dto.getLateThresholdMinutes());
        rule.setEarlyLeaveThresholdMinutes(dto.getEarlyLeaveThresholdMinutes());
        rule.setMinWorkHours(dto.getMinWorkHours());
        rule.setEnabled(dto.getEnabled());
        rule.setRemark(dto.getRemark());
        
        rule = attendanceRuleRepository.save(rule);
        return toRuleDTO(rule);
    }

    // ==================== 转换方法 ====================

    private AttendanceRecordDTO toRecordDTO(AttendanceRecord record) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setRecordDate(record.getRecordDate());
        dto.setCheckInTime(record.getCheckInTime());
        dto.setCheckOutTime(record.getCheckOutTime());
        dto.setCheckInDevice(record.getCheckInDevice());
        dto.setCheckOutDevice(record.getCheckOutDevice());
        dto.setCheckInLocation(record.getCheckInLocation());
        dto.setCheckOutLocation(record.getCheckOutLocation());
        dto.setStatus(record.getStatus());
        dto.setStatusDesc(record.getStatus().getDescription());
        dto.setRemark(record.getRemark());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }

    private LeaveRequestDTO toLeaveDTO(LeaveRequest leave) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(leave.getId());
        dto.setUserId(leave.getUserId());
        dto.setLeaveType(leave.getLeaveType());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setTotalDays(leave.getTotalDays());
        dto.setReason(leave.getReason());
        dto.setAttachmentUrl(leave.getAttachmentUrl());
        dto.setStatus(leave.getStatus());
        dto.setStatusDesc(leave.getStatus().getDescription());
        dto.setApprovalInstanceId(leave.getApprovalInstanceId());
        dto.setApproverId(leave.getApproverId());
        dto.setApprovedAt(leave.getApprovedAt());
        dto.setRejectReason(leave.getRejectReason());
        dto.setCreatedAt(leave.getCreatedAt());
        return dto;
    }

    private LeaveRequest toLeaveEntity(LeaveRequestDTO dto) {
        return LeaveRequest.builder()
                .leaveType(dto.getLeaveType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .totalDays(dto.getTotalDays())
                .reason(dto.getReason())
                .attachmentUrl(dto.getAttachmentUrl())
                .build();
    }

    private OvertimeRequestDTO toOvertimeDTO(OvertimeRequest overtime) {
        OvertimeRequestDTO dto = new OvertimeRequestDTO();
        dto.setId(overtime.getId());
        dto.setUserId(overtime.getUserId());
        dto.setOvertimeDate(overtime.getOvertimeDate());
        dto.setStartTime(overtime.getStartTime());
        dto.setEndTime(overtime.getEndTime());
        dto.setDurationHours(overtime.getDurationHours());
        dto.setReason(overtime.getReason());
        dto.setStatus(overtime.getStatus());
        dto.setStatusDesc(overtime.getStatus().getDescription());
        dto.setApprovalInstanceId(overtime.getApprovalInstanceId());
        dto.setApproverId(overtime.getApproverId());
        dto.setApprovedAt(overtime.getApprovedAt());
        dto.setRejectReason(overtime.getRejectReason());
        dto.setCreatedAt(overtime.getCreatedAt());
        return dto;
    }

    private OvertimeRequest toOvertimeEntity(OvertimeRequestDTO dto) {
        return OvertimeRequest.builder()
                .overtimeDate(dto.getOvertimeDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .durationHours(dto.getDurationHours())
                .reason(dto.getReason())
                .build();
    }

    private CompensatoryLeaveDTO toCompensatoryDTO(CompensatoryLeave leave) {
        CompensatoryLeaveDTO dto = new CompensatoryLeaveDTO();
        dto.setId(leave.getId());
        dto.setUserId(leave.getUserId());
        dto.setOvertimeRequestId(leave.getOvertimeRequestId());
        dto.setLeaveDate(leave.getLeaveDate());
        dto.setDurationHours(leave.getDurationHours());
        dto.setStatus(leave.getStatus());
        dto.setStatusDesc(leave.getStatus().getDescription());
        dto.setApprovalInstanceId(leave.getApprovalInstanceId());
        dto.setApproverId(leave.getApproverId());
        dto.setApprovedAt(leave.getApprovedAt());
        dto.setRejectReason(leave.getRejectReason());
        dto.setCreatedAt(leave.getCreatedAt());
        return dto;
    }

    private CompensatoryLeave toCompensatoryEntity(CompensatoryLeaveDTO dto) {
        return CompensatoryLeave.builder()
                .overtimeRequestId(dto.getOvertimeRequestId())
                .leaveDate(dto.getLeaveDate())
                .durationHours(dto.getDurationHours())
                .build();
    }

    private AttendanceRuleDTO toRuleDTO(AttendanceRule rule) {
        AttendanceRuleDTO dto = new AttendanceRuleDTO();
        dto.setId(rule.getId());
        dto.setRuleName(rule.getRuleName());
        dto.setWorkStartTime(rule.getWorkStartTime());
        dto.setWorkEndTime(rule.getWorkEndTime());
        dto.setFlexibleMinutes(rule.getFlexibleMinutes());
        dto.setLateThresholdMinutes(rule.getLateThresholdMinutes());
        dto.setEarlyLeaveThresholdMinutes(rule.getEarlyLeaveThresholdMinutes());
        dto.setMinWorkHours(rule.getMinWorkHours());
        dto.setIsDefault(rule.getIsDefault());
        dto.setEnabled(rule.getEnabled());
        dto.setRemark(rule.getRemark());
        return dto;
    }
}
