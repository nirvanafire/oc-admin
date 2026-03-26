package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.OvertimeBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OvertimeBalanceRepository extends JpaRepository<OvertimeBalance, Long> {
    
    Optional<OvertimeBalance> findByUserIdAndYear(Long userId, Integer year);
}
