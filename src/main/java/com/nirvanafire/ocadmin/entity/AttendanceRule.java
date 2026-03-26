package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 考勤规则实体
 */
@Entity
@Table(name = "attendance_rule")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, length = 50)
    private String ruleName;

    @Column(name = "work_start_time", nullable = false)
    private LocalTime workStartTime;

    @Column(name = "work_end_time", nullable = false)
    private LocalTime workEndTime;

    @Column(name = "flexible_minutes")
    @Builder.Default
    private Integer flexibleMinutes = 0;

    @Column(name = "late_threshold_minutes")
    @Builder.Default
    private Integer lateThresholdMinutes = 15;

    @Column(name = "early_leave_threshold_minutes")
    @Builder.Default
    private Integer earlyLeaveThresholdMinutes = 15;

    @Column(name = "min_work_hours", precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal minWorkHours = BigDecimal.valueOf(8.0);

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column
    @Builder.Default
    private Boolean enabled = true;

    @Column(length = 255)
    private String remark;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
