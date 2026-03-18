package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.ApprovalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    Page<ApprovalRequest> findByApplicantIdOrderByCreateTimeDesc(Long applicantId, Pageable pageable);

    Optional<ApprovalRequest> findByProcessInstanceId(String processInstanceId);

    Page<ApprovalRequest> findByStatusOrderByCreateTimeDesc(String status, Pageable pageable);

    Page<ApprovalRequest> findByApplicantIdAndStatusOrderByCreateTimeDesc(Long applicantId, String status, Pageable pageable);
}
