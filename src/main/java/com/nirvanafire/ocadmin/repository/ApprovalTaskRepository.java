package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.ApprovalTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalTaskRepository extends JpaRepository<ApprovalTask, Long> {

    Page<ApprovalTask> findByAssigneeIdAndTaskStatusOrderByCreateTimeDesc(Long assigneeId, String taskStatus, Pageable pageable);

    List<ApprovalTask> findByRequestId(Long requestId);

    Optional<ApprovalTask> findByTaskId(String taskId);

    Page<ApprovalTask> findByTaskStatusOrderByCreateTimeDesc(String taskStatus, Pageable pageable);
}
