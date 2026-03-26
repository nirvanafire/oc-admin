package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.*;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤管理控制器
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    // ==================== 打卡签到 ====================

    /**
     * 签到
     */
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceRecordDTO> checkIn(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.checkIn(userId));
    }

    /**
     * 签退
     */
    @PostMapping("/check-out")
    public ResponseEntity<AttendanceRecordDTO> checkOut(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.checkOut(userId));
    }

    // ==================== 考勤记录 ====================

    /**
     * 获取考勤记录列表
     */
    @GetMapping("/records")
    public ResponseEntity<Page<AttendanceRecordDTO>> getRecords(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.getRecords(userId, startDate, endDate, pageable));
    }

    /**
     * 获取考勤记录详情
     */
    @GetMapping("/records/{id}")
    public ResponseEntity<AttendanceRecordDTO> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getRecordById(id));
    }

    /**
     * 获取考勤日历
     */
    @GetMapping("/calendar")
    public ResponseEntity<List<AttendanceRecordDTO>> getCalendar(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.getCalendar(userId, year, month));
    }

    // ==================== 请假申请 ====================

    /**
     * 创建请假申请
     */
    @PostMapping("/leave")
    public ResponseEntity<LeaveRequestDTO> createLeave(
            Authentication authentication,
            @Valid @RequestBody LeaveRequestDTO dto) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.createLeave(userId, dto));
    }

    /**
     * 获取请假列表
     */
    @GetMapping("/leave")
    public ResponseEntity<Page<LeaveRequestDTO>> getLeaveList(
            Authentication authentication,
            Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.getLeaveList(userId, pageable));
    }

    /**
     * 获取请假详情
     */
    @GetMapping("/leave/{id}")
    public ResponseEntity<LeaveRequestDTO> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getLeaveById(id));
    }

    /**
     * 取消请假申请
     */
    @DeleteMapping("/leave/{id}")
    public ResponseEntity<Void> cancelLeave(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        attendanceService.cancelLeave(userId, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取年假余额
     */
    @GetMapping("/leave/balance")
    public ResponseEntity<BalanceDTO> getAnnualLeaveBalance(
            Authentication authentication,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.getAnnualLeaveBalance(userId, year));
    }

    // ==================== 加班申请 ====================

    /**
     * 创建加班申请
     */
    @PostMapping("/overtime")
    public ResponseEntity<OvertimeRequestDTO> createOvertime(
            Authentication authentication,
            @Valid @RequestBody OvertimeRequestDTO dto) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.createOvertime(userId, dto));
    }

    /**
     * 获取加班列表
     */
    @GetMapping("/overtime")
    public ResponseEntity<Page<OvertimeRequestDTO>> getOvertimeList(
            Authentication authentication,
            Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.getOvertimeList(userId, pageable));
    }

    /**
     * 获取加班详情
     */
    @GetMapping("/overtime/{id}")
    public ResponseEntity<OvertimeRequestDTO> getOvertimeById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getOvertimeById(id));
    }

    /**
     * 取消加班申请
     */
    @DeleteMapping("/overtime/{id}")
    public ResponseEntity<Void> cancelOvertime(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        attendanceService.cancelOvertime(userId, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取调休余额
     */
    @GetMapping("/overtime/balance")
    public ResponseEntity<BalanceDTO> getOvertimeBalance(
            Authentication authentication,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.getOvertimeBalance(userId, year));
    }

    // ==================== 调休申请 ====================

    /**
     * 创建调休申请
     */
    @PostMapping("/compensatory")
    public ResponseEntity<CompensatoryLeaveDTO> createCompensatory(
            Authentication authentication,
            @Valid @RequestBody CompensatoryLeaveDTO dto) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.createCompensatoryLeave(userId, dto));
    }

    /**
     * 获取调休列表
     */
    @GetMapping("/compensatory")
    public ResponseEntity<Page<CompensatoryLeaveDTO>> getCompensatoryList(
            Authentication authentication,
            Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.getCompensatoryList(userId, pageable));
    }

    /**
     * 获取调休详情
     */
    @GetMapping("/compensatory/{id}")
    public ResponseEntity<CompensatoryLeaveDTO> getCompensatoryById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getCompensatoryById(id));
    }

    /**
     * 取消调休申请
     */
    @DeleteMapping("/compensatory/{id}")
    public ResponseEntity<Void> cancelCompensatory(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        attendanceService.cancelCompensatory(userId, id);
        return ResponseEntity.ok().build();
    }

    // ==================== 考勤统计 ====================

    /**
     * 获取个人考勤统计
     */
    @GetMapping("/statistics/personal")
    public ResponseEntity<AttendanceStatisticsDTO> getPersonalStatistics(
            Authentication authentication,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int month) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(attendanceService.getPersonalStatistics(userId, year, month));
    }

    /**
     * 获取部门考勤统计
     */
    @GetMapping("/statistics/department")
    public ResponseEntity<List<AttendanceStatisticsDTO>> getDepartmentStatistics(
            @RequestParam Long deptId,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int month) {
        return ResponseEntity.ok(attendanceService.getDepartmentStatistics(deptId, year, month));
    }

    // ==================== 考勤规则 ====================

    /**
     * 获取考勤规则
     */
    @GetMapping("/rule")
    public ResponseEntity<AttendanceRuleDTO> getAttendanceRule() {
        return ResponseEntity.ok(attendanceService.getAttendanceRule());
    }

    /**
     * 更新考勤规则
     */
    @PutMapping("/rule")
    public ResponseEntity<AttendanceRuleDTO> updateAttendanceRule(
            @RequestBody AttendanceRuleDTO dto) {
        return ResponseEntity.ok(attendanceService.updateAttendanceRule(dto));
    }

    // ==================== 辅助方法 ====================

    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
}
