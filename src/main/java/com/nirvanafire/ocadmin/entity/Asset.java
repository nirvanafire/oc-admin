package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产实体
 */
@Entity
@Table(name = "asset")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_code", nullable = false, unique = true, length = 50)
    private String assetCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String spec;

    @Column(name = "asset_type", length = 50)
    private String assetType;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal value;

    @Column(length = 20)
    @Builder.Default
    private String status = "IDLE"; // IDLE-闲置, IN_USE-使用中, BORROWED-已借出, MAINTENANCE-维修中, SCRAP-已报废

    @Column(name = "current_user_id")
    private Long currentUserId;

    @Column(name = "current_user_name", length = 100)
    private String currentUserName;

    @Column(name = "current_dept_id")
    private Long currentDeptId;

    @Column(name = "current_dept_name", length = 100)
    private String currentDeptName;

    @Column(name = "storage_location", length = 200)
    private String storageLocation;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
