package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    
    Optional<Asset> findByAssetCode(String assetCode);
    
    Page<Asset> findByStatus(String status, Pageable pageable);
    
    Page<Asset> findByCurrentUserId(Long userId, Pageable pageable);
    
    @Query("SELECT a FROM Asset a WHERE a.name LIKE %:keyword% OR a.assetCode LIKE %:keyword%")
    Page<Asset> search(@Param("keyword") String keyword, Pageable pageable);
    
    List<Asset> findByCurrentDeptId(Long deptId);
    
    Long countByStatus(String status);
}
