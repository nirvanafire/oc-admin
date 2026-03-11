package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.SysRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<SysRole, Long> {
    Optional<SysRole> findByCode(String code);
    boolean existsByCode(String code);
    Page<SysRole> findByCodeContaining(String code, Pageable pageable);
}
