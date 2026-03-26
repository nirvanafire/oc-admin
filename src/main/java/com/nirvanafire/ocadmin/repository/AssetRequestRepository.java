package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.AssetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRequestRepository extends JpaRepository<AssetRequest, Long> {
    
    Page<AssetRequest> findByUserId(Long userId, Pageable pageable);
    
    List<AssetRequest> findByAssetId(Long assetId);
    
    Long countByStatus(String status);
}
