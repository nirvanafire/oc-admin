package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.OvertimeRequest;
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
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {
    
    Page<OvertimeRequest> findByUserId(Long userId, Pageable pageable);
    
    Page<OvertimeRequest> findByUserIdAndStatus(Long userId, RequestStatus status, Pageable pageable);
    
    List<OvertimeRequest> findByUserIdAndStatus(Long userId, RequestStatus status);
    
    @Query("SELECT COALESCE(SUM(or.durationHours), 0) FROM OvertimeRequest or WHERE or.userId = :userId AND or.status = :status AND YEAR(or.overtimeDate) = :year")
    BigDecimal sumDurationHoursByUserIdAndStatusAndYear(@Param("userId") Long userId, @Param("status") RequestStatus status, @Param("year") int year);
    
    @Query("SELECT or FROM OvertimeRequest or WHERE or.userId = :userId AND YEAR(or.overtimeDate) = :year AND MONTH(or.overtimeDate) = :month")
    List<OvertimeRequest> findByUserIdAndYearAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
    
    @Query("SELECT or FROM OvertimeRequest or WHERE or.userId = :userId AND or.status = 'APPROVED' AND or.id NOT IN (SELECT cl.overtimeRequestId FROM CompensatoryLeave cl WHERE cl.overtimeRequestId IS NOT NULL AND cl.status = 'APPROVED')")
    List<OvertimeRequest> findUnconvertedOvertime(@Param("userId") Long userId);
}
