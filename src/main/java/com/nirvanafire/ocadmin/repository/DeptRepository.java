package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.SysDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeptRepository extends JpaRepository<SysDept, Long> {

    Optional<SysDept> findByDeptCode(String deptCode);

    boolean existsByDeptCode(String deptCode);

    List<SysDept> findByParentId(Long parentId);

    List<SysDept> findByParentIdOrderBySortOrderAsc(Long parentId);

    @Query("SELECT d FROM SysDept d WHERE d.parentId = 0 ORDER BY d.sortOrder ASC")
    List<SysDept> findRootDepts();

    boolean existsByParentId(Long parentId);
}
