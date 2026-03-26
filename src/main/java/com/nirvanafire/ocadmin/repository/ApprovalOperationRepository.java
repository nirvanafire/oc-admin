package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.ApprovalOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalOperationRepository extends JpaRepository<ApprovalOperation, Long> {
    
    List<ApprovalOperation> findByRequestIdOrderByCreateTimeDesc(Long requestId);
    
    Page<ApprovalOperation> findByOperatorId(Long operatorId, Pageable pageable);
    
    @Query("SELECT ao FROM ApprovalOperation ao WHERE ao.requestId = :requestId AND ao.operatorId = :operatorId ORDER BY ao.createTime DESC")
    List<ApprovalOperation> findByRequestIdAndOperatorId(@Param("requestId") Long requestId, @Param("operatorId") Long operatorId);
    
    Long countByTaskId(Long taskId);
}
