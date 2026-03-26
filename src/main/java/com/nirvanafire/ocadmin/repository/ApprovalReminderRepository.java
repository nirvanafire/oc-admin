package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.ApprovalReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApprovalReminderRepository extends JpaRepository<ApprovalReminder, Long> {
    
    List<ApprovalReminder> findByRequestIdOrderByCreateTimeDesc(Long requestId);
    
    @Query("SELECT MAX(ar.reminderCount) FROM ApprovalReminder ar WHERE ar.requestId = :requestId")
    Integer getMaxReminderCount(@Param("requestId") Long requestId);
    
    @Query("SELECT ar FROM ApprovalReminder ar WHERE ar.requestId = :requestId AND ar.taskId = :taskId ORDER BY ar.createTime DESC")
    List<ApprovalReminder> findByRequestIdAndTaskId(@Param("requestId") Long requestId, @Param("taskId") Long taskId);
    
    @Query("SELECT ar FROM ApprovalReminder ar WHERE ar.createTime >= :startTime AND ar.createTime <= :endTime")
    List<ApprovalReminder> findByReminderTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
