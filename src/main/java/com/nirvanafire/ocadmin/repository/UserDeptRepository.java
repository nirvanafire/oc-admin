package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.SysUserDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDeptRepository extends JpaRepository<SysUserDept, Long> {

    @Query("SELECT ud.userId FROM SysUserDept ud WHERE ud.deptId = ?1")
    List<Long> findUserIdsByDeptId(Long deptId);

    void deleteByUserIdAndDeptId(Long userId, Long deptId);

    boolean existsByUserIdAndDeptId(Long userId, Long deptId);

    List<SysUserDept> findByUserId(Long userId);
}
