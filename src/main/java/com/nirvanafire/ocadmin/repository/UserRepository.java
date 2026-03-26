package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
    boolean existsByUsername(String username);
    Page<SysUser> findByUsernameContaining(String username, Pageable pageable);

    @Query("SELECT u FROM SysUser u JOIN SysUserDept ud ON u.id = ud.userId WHERE ud.deptId = :deptId")
    List<SysUser> findByDeptId(@Param("deptId") Long deptId);

    @Query("SELECT u FROM SysUser u JOIN SysUserDept ud ON u.id = ud.userId WHERE ud.deptId = :deptId")
    Page<SysUser> findByDeptId(@Param("deptId") Long deptId, Pageable pageable);

    @Query("SELECT ud.deptId FROM SysUserDept ud WHERE ud.userId = :userId")
    List<Long> findDeptIdsByUserId(@Param("userId") Long userId);
    
    boolean existsByPhone(String phone);
    
    boolean existsByPhoneAndIdNot(String phone, Long id);
    
    boolean existsByEmail(String email);
}
