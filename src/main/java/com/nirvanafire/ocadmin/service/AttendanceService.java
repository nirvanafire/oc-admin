package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤服务接口
 */
public interface AttendanceService {
    
    // ==================== 打卡签到 ====================
    
    /**
     * 签到
     */
    AttendanceRecordDTO checkIn(Long userId);
    
    /**
     * 签退
     */
    AttendanceRecordDTO checkOut(Long userId);
    
    // ==================== 考勤记录 ====================
    
    /**
     * 获取考勤记录列表
     */
    Page<AttendanceRecordDTO> getRecords(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * 获取考勤记录详情
     */
    AttendanceRecordDTO getRecordById(Long id);
    
    /**
     * 获取考勤日历
     */
    List<AttendanceRecordDTO> getCalendar(Long userId, int year, int month);
    
    // ==================== 请假申请 ====================
    
    /**
     * 创建请假申请
     */
    LeaveRequestDTO createLeave(Long userId, LeaveRequestDTO dto);
    
    /**
     * 获取请假列表
     */
    Page<LeaveRequestDTO> getLeaveList(Long userId, Pageable pageable);
    
    /**
     * 获取请假详情
     */
    LeaveRequestDTO getLeaveById(Long id);
    
    /**
     * 取消请假申请
     */
    void cancelLeave(Long userId, Long id);
    
    // ==================== 加班申请 ====================
    
    /**
     * 创建加班申请
     */
    OvertimeRequestDTO createOvertime(Long userId, OvertimeRequestDTO dto);
    
    /**
     * 获取加班列表
     */
    Page<OvertimeRequestDTO> getOvertimeList(Long userId, Pageable pageable);
    
    /**
     * 获取加班详情
     */
    OvertimeRequestDTO getOvertimeById(Long id);
    
    /**
     * 取消加班申请
     */
    void cancelOvertime(Long userId, Long id);
    
    // ==================== 调休申请 ====================
    
    /**
     * 创建调休申请
     */
    CompensatoryLeaveDTO createCompensatoryLeave(Long userId, CompensatoryLeaveDTO dto);
    
    /**
     * 获取调休列表
     */
    Page<CompensatoryLeaveDTO> getCompensatoryList(Long userId, Pageable pageable);
    
    /**
     * 获取调休详情
     */
    CompensatoryLeaveDTO getCompensatoryById(Long id);
    
    /**
     * 取消调休申请
     */
    void cancelCompensatory(Long userId, Long id);
    
    // ==================== 余额查询 ====================
    
    /**
     * 获取年假余额
     */
    BalanceDTO getAnnualLeaveBalance(Long userId, int year);
    
    /**
     * 获取调休余额
     */
    BalanceDTO getOvertimeBalance(Long userId, int year);
    
    // ==================== 考勤统计 ====================
    
    /**
     * 获取个人考勤统计
     */
    AttendanceStatisticsDTO getPersonalStatistics(Long userId, int year, int month);
    
    /**
     * 获取部门考勤统计
     */
    List<AttendanceStatisticsDTO> getDepartmentStatistics(Long deptId, int year, int month);
    
    // ==================== 考勤规则 ====================
    
    /**
     * 获取考勤规则
     */
    AttendanceRuleDTO getAttendanceRule();
    
    /**
     * 更新考勤规则
     */
    AttendanceRuleDTO updateAttendanceRule(AttendanceRuleDTO dto);
}
