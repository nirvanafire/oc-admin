package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.ApprovalCc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalCcRepository extends JpaRepository<ApprovalCc, Long> {
    
    Page<ApprovalCc> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
    
    Long countByUserIdAndIsReadFalse(Long userId);
    
    @Modifying
    @Query("UPDATE ApprovalCc SET isRead = true, readTime = CURRENT_TIMESTAMP WHERE id = :id AND userId = :userId")
    void markAsRead(@Param("id") Long id, @Param("userId") Long userId);
}
