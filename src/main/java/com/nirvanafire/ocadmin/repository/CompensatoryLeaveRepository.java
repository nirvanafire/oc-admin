package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.CompensatoryLeave;
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
public interface CompensatoryLeaveRepository extends JpaRepository<CompensatoryLeave, Long> {
    
    Page<CompensatoryLeave> findByUserId(Long userId, Pageable pageable);
    
    Page<CompensatoryLeave> findByUserIdAndStatus(Long userId, RequestStatus status, Pageable pageable);
    
    List<CompensatoryLeave> findByUserIdAndStatus(Long userId, RequestStatus status);
    
    @Query("SELECT COALESCE(SUM(cl.durationHours), 0) FROM CompensatoryLeave cl WHERE cl.userId = :userId AND cl.status = :status AND YEAR(cl.leaveDate) = :year")
    BigDecimal sumDurationHoursByUserIdAndStatusAndYear(@Param("userId") Long userId, @Param("status") RequestStatus status, @Param("year") int year);
    
    @Query("SELECT cl FROM CompensatoryLeave cl WHERE cl.userId = :userId AND YEAR(cl.leaveDate) = :year AND MONTH(cl.leaveDate) = :month")
    List<CompensatoryLeave> findByUserIdAndYearAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
    
    @Query("SELECT cl FROM CompensatoryLeave cl WHERE cl.overtimeRequestId = :overtimeRequestId AND cl.status = 'APPROVED'")
    List<CompensatoryLeave> findByOvertimeRequestId(@Param("overtimeRequestId") Long overtimeRequestId);
}
