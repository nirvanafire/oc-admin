package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.ProcessDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessDefinitionRepository extends JpaRepository<ProcessDefinition, Long> {

    Optional<ProcessDefinition> findByProcessKeyAndVersion(String processKey, Integer version);

    List<ProcessDefinition> findByProcessKeyOrderByVersionDesc(String processKey);

    Optional<ProcessDefinition> findTopByProcessKeyAndStatusOrderByVersionDesc(String processKey, Integer status);

    List<ProcessDefinition> findByStatus(Integer status);

    boolean existsByProcessKeyAndStatus(String processKey, Integer status);
}
