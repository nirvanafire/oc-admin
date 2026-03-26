package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加班调休余额实体
 */
@Entity
@Table(name = "overtime_balance", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_year", columnNames = {"user_id", "year"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "total_hours", precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal totalHours = BigDecimal.ZERO;

    @Column(name = "used_hours", precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal usedHours = BigDecimal.ZERO;

    @Column(name = "available_hours", precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal availableHours = BigDecimal.ZERO;

    @Column(name = "expired_hours", precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal expiredHours = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
