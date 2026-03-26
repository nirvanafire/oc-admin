package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.AssetDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 资产服务接口
 */
public interface AssetService {
    
    // ==================== 资产管理 ====================
    
    /**
     * 创建资产
     */
    AssetDTO createAsset(Long userId, AssetDTO dto);
    
    /**
     * 更新资产
     */
    AssetDTO updateAsset(Long userId, Long assetId, AssetDTO dto);
    
    /**
     * 删除资产
     */
    void deleteAsset(Long userId, Long assetId);
    
    /**
     * 获取资产列表
     */
    Page<AssetDTO> listAssets(String status, String keyword, Pageable pageable);
    
    /**
     * 获取资产详情
     */
    AssetDTO getAsset(Long assetId);
    
    // ==================== 资产申请 ====================
    
    /**
     * 申请领用
     */
    AssetDTO applyBorrow(Long userId, String username, Long assetId, String reason);
    
    /**
     * 申请归还
     */
    AssetDTO applyReturn(Long userId, Long assetId, String reason);
    
    /**
     * 申请调拨
     */
    AssetDTO applyTransfer(Long userId, String username, Long assetId, Long targetDeptId, String reason);
    
    /**
     * 申请报废
     */
    AssetDTO applyScrap(Long userId, String username, Long assetId, String reason);
    
    /**
     * 获取我的申请
     */
    Page<AssetDTO> getMyRequests(Long userId, Pageable pageable);
}
