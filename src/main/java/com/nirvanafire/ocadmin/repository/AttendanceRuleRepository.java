package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.AttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceRuleRepository extends JpaRepository<AttendanceRule, Long> {
    
    Optional<AttendanceRule> findByIsDefaultTrue();
    
    Optional<AttendanceRule> findByIsDefaultTrueAndEnabledTrue();
}
