package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.*;
import com.nirvanafire.ocadmin.entity.*;
import com.nirvanafire.ocadmin.enums.*;
import com.nirvanafire.ocadmin.repository.*;
import com.nirvanafire.ocadmin.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AttendanceService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;
    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private OvertimeRequestRepository overtimeRequestRepository;
    @Mock
    private CompensatoryLeaveRepository compensatoryLeaveRepository;
    @Mock
    private AttendanceRuleRepository attendanceRuleRepository;
    @Mock
    private OvertimeBalanceRepository overtimeBalanceRepository;
    @Mock
    private AnnualLeaveBalanceRepository annualLeaveBalanceRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private SysUser testUser;
    private AttendanceRule testRule;

    @BeforeEach
    void setUp() {
        testUser = SysUser.builder()
                .id(1L)
                .username("testuser")
                .nickname("测试用户")
                .build();

        testRule = AttendanceRule.builder()
                .id(1L)
                .ruleName("默认规则")
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .flexibleMinutes(0)
                .lateThresholdMinutes(15)
                .earlyLeaveThresholdMinutes(15)
                .isDefault(true)
                .enabled(true)
                .build();
    }

    @Test
    void checkIn_Success() {
        when(attendanceRuleRepository.findByIsDefaultTrueAndEnabledTrue())
                .thenReturn(Optional.of(testRule));
        when(attendanceRecordRepository.findByUserIdAndRecordDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceRecordDTO result = attendanceService.checkIn(1L);

        assertNotNull(result);
        assertNotNull(result.getCheckInTime());
        assertEquals("PC", result.getCheckInDevice());
    }

    @Test
    void checkIn_AlreadyCheckedIn() {
        AttendanceRecord existingRecord = AttendanceRecord.builder()
                .id(1L)
                .userId(1L)
                .recordDate(LocalDate.now())
                .checkInTime(LocalDateTime.now())
                .build();

        when(attendanceRecordRepository.findByUserIdAndRecordDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(existingRecord));

        assertThrows(BusinessException.class, () -> {
            attendanceService.checkIn(1L);
        });
    }

    @Test
    void checkOut_Success() {
        AttendanceRecord existingRecord = AttendanceRecord.builder()
                .id(1L)
                .userId(1L)
                .recordDate(LocalDate.now())
                .checkInTime(LocalDateTime.now().minusHours(8))
                .build();

        when(attendanceRuleRepository.findByIsDefaultTrueAndEnabledTrue())
                .thenReturn(Optional.of(testRule));
        when(attendanceRecordRepository.findByUserIdAndRecordDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(existingRecord));
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceRecordDTO result = attendanceService.checkOut(1L);

        assertNotNull(result);
        assertNotNull(result.getCheckOutTime());
        assertEquals("PC", result.getCheckOutDevice());
    }

    @Test
    void checkOut_NotCheckedIn() {
        when(attendanceRecordRepository.findByUserIdAndRecordDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            attendanceService.checkOut(1L);
        });
    }

    @Test
    void createLeave_Success() {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setLeaveType(LeaveType.PERSONAL);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(1));

        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestDTO result = attendanceService.createLeave(1L, dto);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(2), result.getTotalDays());
        assertEquals(RequestStatus.PENDING, result.getStatus());
    }

    @Test
    void createLeave_InvalidDateRange() {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setLeaveType(LeaveType.PERSONAL);
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now());

        assertThrows(BusinessException.class, () -> {
            attendanceService.createLeave(1L, dto);
        });
    }

    @Test
    void createOvertime_Success() {
        OvertimeRequestDTO dto = new OvertimeRequestDTO();
        dto.setOvertimeDate(LocalDate.now());
        dto.setStartTime(LocalDateTime.now().withHour(18).withMinute(0));
        dto.setEndTime(LocalDateTime.now().withHour(21).withMinute(0));

        when(overtimeRequestRepository.save(any(OvertimeRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OvertimeRequestDTO result = attendanceService.createOvertime(1L, dto);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(3.0), result.getDurationHours());
        assertEquals(RequestStatus.PENDING, result.getStatus());
    }

    @Test
    void createOvertime_InvalidTime() {
        OvertimeRequestDTO dto = new OvertimeRequestDTO();
        dto.setOvertimeDate(LocalDate.now());
        dto.setStartTime(LocalDateTime.now().withHour(21).withMinute(0));
        dto.setEndTime(LocalDateTime.now().withHour(18).withMinute(0));

        assertThrows(BusinessException.class, () -> {
            attendanceService.createOvertime(1L, dto);
        });
    }

    @Test
    void createCompensatoryLeave_Success() {
        CompensatoryLeaveDTO dto = new CompensatoryLeaveDTO();
        dto.setLeaveDate(LocalDate.now());
        dto.setDurationHours(BigDecimal.valueOf(4));

        OvertimeBalance balance = OvertimeBalance.builder()
                .id(1L)
                .userId(1L)
                .year(LocalDate.now().getYear())
                .totalHours(BigDecimal.valueOf(10))
                .usedHours(BigDecimal.ZERO)
                .availableHours(BigDecimal.valueOf(10))
                .build();

        when(overtimeBalanceRepository.findByUserIdAndYear(anyLong(), anyInt()))
                .thenReturn(Optional.of(balance));
        when(compensatoryLeaveRepository.save(any(CompensatoryLeave.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CompensatoryLeaveDTO result = attendanceService.createCompensatoryLeave(1L, dto);

        assertNotNull(result);
        assertEquals(RequestStatus.PENDING, result.getStatus());
    }

    @Test
    void createCompensatoryLeave_InsufficientBalance() {
        CompensatoryLeaveDTO dto = new CompensatoryLeaveDTO();
        dto.setLeaveDate(LocalDate.now());
        dto.setDurationHours(BigDecimal.valueOf(20));

        OvertimeBalance balance = OvertimeBalance.builder()
                .id(1L)
                .userId(1L)
                .year(LocalDate.now().getYear())
                .totalHours(BigDecimal.valueOf(10))
                .usedHours(BigDecimal.ZERO)
                .availableHours(BigDecimal.valueOf(10))
                .build();

        when(overtimeBalanceRepository.findByUserIdAndYear(anyLong(), anyInt()))
                .thenReturn(Optional.of(balance));

        assertThrows(BusinessException.class, () -> {
            attendanceService.createCompensatoryLeave(1L, dto);
        });
    }

    @Test
    void getPersonalStatistics_Success() {
        when(attendanceRecordRepository.findByUserIdAndYearAndMonth(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(leaveRequestRepository.findByUserIdAndYearAndMonth(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(overtimeRequestRepository.findByUserIdAndYearAndMonth(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(compensatoryLeaveRepository.findByUserIdAndYearAndMonth(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(testUser));

        AttendanceStatisticsDTO result = attendanceService.getPersonalStatistics(1L, 2026, 3);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getAttendanceRule_Success() {
        when(attendanceRuleRepository.findByIsDefaultTrueAndEnabledTrue())
                .thenReturn(Optional.of(testRule));

        AttendanceRuleDTO result = attendanceService.getAttendanceRule();

        assertNotNull(result);
        assertEquals("默认规则", result.getRuleName());
        assertEquals(LocalTime.of(9, 0), result.getWorkStartTime());
        assertEquals(LocalTime.of(18, 0), result.getWorkEndTime());
    }

    @Test
    void updateAttendanceRule_Success() {
        AttendanceRuleDTO dto = new AttendanceRuleDTO();
        dto.setRuleName("新规则");
        dto.setWorkStartTime(LocalTime.of(8, 30));
        dto.setWorkEndTime(LocalTime.of(17, 30));
        dto.setFlexibleMinutes(10);
        dto.setLateThresholdMinutes(10);
        dto.setEarlyLeaveThresholdMinutes(10);

        when(attendanceRuleRepository.findByIsDefaultTrue())
                .thenReturn(Optional.of(testRule));
        when(attendanceRuleRepository.save(any(AttendanceRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceRuleDTO result = attendanceService.updateAttendanceRule(dto);

        assertNotNull(result);
        assertEquals("新规则", result.getRuleName());
        assertEquals(LocalTime.of(8, 30), result.getWorkStartTime());
    }
}
