package com.nirvanafire.ocadmin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产DTO
 */
@Data
public class AssetDTO {
    private Long id;
    private String assetCode;
    private String name;
    private String spec;
    private String assetType;
    private LocalDate purchaseDate;
    private BigDecimal value;
    private String status;
    private Long currentUserId;
    private String currentUserName;
    private Long currentDeptId;
    private String currentDeptName;
    private String storageLocation;
    private String remark;
    private String requestType;
    private String reason;
}
