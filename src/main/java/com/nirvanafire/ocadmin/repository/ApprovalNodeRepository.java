package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.ApprovalNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalNodeRepository extends JpaRepository<ApprovalNode, Long> {

    List<ApprovalNode> findByProcessDefinitionId(Long processDefinitionId);

    List<ApprovalNode> findByProcessDefinitionIdAndNodeKey(Long processDefinitionId, String nodeKey);

    Optional<ApprovalNode> findTopByProcessDefinitionIdAndNodeKey(Long processDefinitionId, String nodeKey);

    void deleteByProcessDefinitionId(Long processDefinitionId);
}
