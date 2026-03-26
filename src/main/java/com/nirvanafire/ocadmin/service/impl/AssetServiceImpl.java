package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.AssetDTO;
import com.nirvanafire.ocadmin.entity.Asset;
import com.nirvanafire.ocadmin.entity.AssetRequest;
import com.nirvanafire.ocadmin.repository.AssetRepository;
import com.nirvanafire.ocadmin.repository.AssetRequestRepository;
import com.nirvanafire.ocadmin.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 资产服务实现
 */
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetRequestRepository assetRequestRepository;
    private final DeptRepository deptRepository;

    @Override
    @Transactional
    public AssetDTO createAsset(Long userId, AssetDTO dto) {
        Asset asset = Asset.builder()
                .assetCode(generateAssetCode())
                .name(dto.getName())
                .spec(dto.getSpec())
                .assetType(dto.getAssetType())
                .purchaseDate(dto.getPurchaseDate())
                .value(dto.getValue())
                .status("IDLE")
                .storageLocation(dto.getStorageLocation())
                .remark(dto.getRemark())
                .build();
        
        asset = assetRepository.save(asset);
        return toDTO(asset);
    }

    @Override
    @Transactional
    public AssetDTO updateAsset(Long userId, Long assetId, AssetDTO dto) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        
        if (dto.getName() != null) asset.setName(dto.getName());
        if (dto.getSpec() != null) asset.setSpec(dto.getSpec());
        if (dto.getAssetType() != null) asset.setAssetType(dto.getAssetType());
        if (dto.getValue() != null) asset.setValue(dto.getValue());
        
        asset = assetRepository.save(asset);
        return toDTO(asset);
    }

    @Override
    @Transactional
    public void deleteAsset(Long userId, Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        
        if (!"IDLE".equals(asset.getStatus())) {
            throw new BusinessException("只能删除闲置状态的资产");
        }
        
        assetRepository.delete(asset);
    }

    @Override
    public Page<AssetDTO> listAssets(String status, String keyword, Pageable pageable) {
        Page<Asset> page;
        if (keyword != null && !keyword.isEmpty()) {
            page = assetRepository.search(keyword, pageable);
        } else if (status != null && !status.isEmpty()) {
            page = assetRepository.findByStatus(status, pageable);
        } else {
            page = assetRepository.findAll(pageable);
        }
        
        List<AssetDTO> list = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public AssetDTO getAsset(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        return toDTO(asset);
    }

    @Override
    @Transactional
    public AssetDTO applyBorrow(Long userId, String username, Long assetId, String reason) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        
        if (!"IDLE".equals(asset.getStatus())) {
            throw new BusinessException("资产当前不可领用");
        }
        
        // 创建申请记录
        AssetRequest request = AssetRequest.builder()
                .assetId(assetId)
                .requestType("BORROW")
                .userId(userId)
                .userName(username)
                .reason(reason)
                .status("PENDING")
                .build();
        
        assetRequestRepository.save(request);
        
        // 实际项目中应该触发审批流程，审批通过后更新资产状态
        // 这里简化处理，直接更新
        asset.setStatus("BORROWED");
        asset.setCurrentUserId(userId);
        asset.setCurrentUserName(username);
        assetRepository.save(asset);
        
        return toDTO(asset);
    }

    @Override
    @Transactional
    public AssetDTO applyReturn(Long userId, Long assetId, String reason) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        
        if (!asset.getCurrentUserId().equals(userId)) {
            throw new BusinessException("您不是该资产的当前使用人");
        }
        
        AssetRequest request = AssetRequest.builder()
                .assetId(assetId)
                .requestType("RETURN")
                .userId(userId)
                .userName(asset.getCurrentUserName())
                .reason(reason)
                .status("PENDING")
                .build();
        
        assetRequestRepository.save(request);
        
        asset.setStatus("IDLE");
        asset.setCurrentUserId(null);
        asset.setCurrentUserName(null);
        assetRepository.save(asset);
        
        return toDTO(asset);
    }

    @Override
    @Transactional
    public AssetDTO applyTransfer(Long userId, String username, Long assetId, Long targetDeptId, String reason) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        
        var targetDept = deptRepository.findById(targetDeptId).orElse(null);
        
        AssetRequest request = AssetRequest.builder()
                .assetId(assetId)
                .requestType("TRANSFER")
                .userId(userId)
                .userName(username)
                .targetDeptId(targetDeptId)
                .targetDeptName(targetDept != null ? targetDept.getName() : null)
                .reason(reason)
                .status("PENDING")
                .build();
        
        assetRequestRepository.save(request);
        
        return toDTO(asset);
    }

    @Override
    @Transactional
    public AssetDTO applyScrap(Long userId, String username, Long assetId, String reason) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        
        AssetRequest request = AssetRequest.builder()
                .assetId(assetId)
                .requestType("SCRAP")
                .userId(userId)
                .userName(username)
                .reason(reason)
                .status("PENDING")
                .build();
        
        assetRequestRepository.save(request);
        
        return toDTO(asset);
    }

    @Override
    public Page<AssetDTO> getMyRequests(Long userId, Pageable pageable) {
        Page<AssetRequest> page = assetRequestRepository.findByUserId(userId, pageable);
        
        List<AssetDTO> list = page.getContent().stream().map(req -> {
            AssetDTO dto = new AssetDTO();
            dto.setId(req.getId());
            dto.setRequestType(req.getRequestType());
            dto.setReason(req.getReason());
            dto.setStatus(req.getStatus());
            return dto;
        }).collect(Collectors.toList());
        
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    private String generateAssetCode() {
        return "AST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private AssetDTO toDTO(Asset asset) {
        AssetDTO dto = new AssetDTO();
        dto.setId(asset.getId());
        dto.setAssetCode(asset.getAssetCode());
        dto.setName(asset.getName());
        dto.setSpec(asset.getSpec());
        dto.setAssetType(asset.getAssetType());
        dto.setPurchaseDate(asset.getPurchaseDate());
        dto.setValue(asset.getValue());
        dto.setStatus(asset.getStatus());
        dto.setCurrentUserId(asset.getCurrentUserId());
        dto.setCurrentUserName(asset.getCurrentUserName());
        dto.setCurrentDeptId(asset.getCurrentDeptId());
        dto.setCurrentDeptName(asset.getCurrentDeptName());
        dto.setStorageLocation(asset.getStorageLocation());
        dto.setRemark(asset.getRemark());
        return dto;
    }
}
