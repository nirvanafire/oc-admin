package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.LeaveRequest;
import com.nirvanafire.ocadmin.enums.LeaveType;
import com.nirvanafire.ocadmin.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    Page<LeaveRequest> findByUserId(Long userId, Pageable pageable);
    
    Page<LeaveRequest> findByUserIdAndStatus(Long userId, RequestStatus status, Pageable pageable);
    
    List<LeaveRequest> findByUserIdAndStatus(Long userId, RequestStatus status);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.userId = :userId AND lr.status = :status AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findByUserIdAndStatusAndDateRange(@Param("userId") Long userId, @Param("status") RequestStatus status, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(lr.totalDays), 0) FROM LeaveRequest lr WHERE lr.userId = :userId AND lr.leaveType = :leaveType AND lr.status = :status AND YEAR(lr.startDate) = :year")
    BigDecimal sumTotalDaysByUserIdAndLeaveTypeAndStatusAndYear(@Param("userId") Long userId, @Param("leaveType") LeaveType leaveType, @Param("status") RequestStatus status, @Param("year") int year);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.userId = :userId AND YEAR(lr.startDate) = :year AND MONTH(lr.startDate) = :month")
    List<LeaveRequest> findByUserIdAndYearAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
}
