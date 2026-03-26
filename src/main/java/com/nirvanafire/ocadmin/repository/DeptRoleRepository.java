package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.SysDeptRole;
import com.nirvanafire.ocadmin.entity.SysDeptRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeptRoleRepository extends JpaRepository<SysDeptRole, SysDeptRoleId> {

    @Query("SELECT dr.roleId FROM SysDeptRole dr WHERE dr.deptId = ?1")
    List<Long> findRoleIdsByDeptId(Long deptId);

    void deleteByDeptIdAndRoleId(Long deptId, Long roleId);

    boolean existsByDeptIdAndRoleId(Long deptId, Long roleId);
}
