package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<SysPermission, Long> {
    Optional<SysPermission> findByCode(String code);
}
