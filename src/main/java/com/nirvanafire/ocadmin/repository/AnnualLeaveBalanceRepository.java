package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.AnnualLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnualLeaveBalanceRepository extends JpaRepository<AnnualLeaveBalance, Long> {
    
    Optional<AnnualLeaveBalance> findByUserIdAndYear(Long userId, Integer year);
}
