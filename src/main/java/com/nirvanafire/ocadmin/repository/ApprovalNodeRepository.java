package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.ApprovalNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalNodeRepository extends JpaRepository<ApprovalNode, Long> {

    List<ApprovalNode> findByProcessDefinitionId(Long processDefinitionId);

    List<ApprovalNode> findByProcessDefinitionIdAndNodeKey(Long processDefinitionId, String nodeKey);

    void deleteByProcessDefinitionId(Long processDefinitionId);
}
