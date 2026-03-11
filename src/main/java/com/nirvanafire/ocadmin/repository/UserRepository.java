package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
