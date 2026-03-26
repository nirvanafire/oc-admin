package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产申请实体（领用/归还/调拨/报废）
 */
@Entity
@Table(name = "asset_request")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "request_type", nullable = false, length = 20)
    private String requestType; // BORROW-领用, RETURN-归还, TRANSFER-调拨, SCRAP-报废

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "target_dept_id")
    private Long targetDeptId;

    @Column(name = "target_dept_name", length = 100)
    private String targetDeptName;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "approval_instance_id", length = 64)
    private String approvalInstanceId;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
