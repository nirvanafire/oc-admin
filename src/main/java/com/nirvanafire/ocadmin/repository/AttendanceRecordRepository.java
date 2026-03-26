package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    
    Optional<AttendanceRecord> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);
    
    List<AttendanceRecord> findByUserIdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    Page<AttendanceRecord> findByUserId(Long userId, Pageable pageable);
    
    Page<AttendanceRecord> findByUserIdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    @Query("SELECT a FROM AttendanceRecord a WHERE a.userId = :userId AND YEAR(a.recordDate) = :year AND MONTH(a.recordDate) = :month")
    List<AttendanceRecord> findByUserIdAndYearAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
    
    @Query("SELECT a FROM AttendanceRecord a WHERE a.userId IN :userIds AND a.recordDate BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findByUserIdsAndDateBetween(@Param("userIds") List<Long> userIds, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
