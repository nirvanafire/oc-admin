package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.ApprovalRequestData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalRequestDataRepository extends JpaRepository<ApprovalRequestData, Long> {

    Optional<ApprovalRequestData> findByRequestId(Long requestId);
}
