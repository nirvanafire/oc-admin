package com.nirvanafire.ocadmin.entity;

import com.nirvanafire.ocadmin.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录实体
 */
@Entity
@Table(name = "attendance_record", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id, record_date"),
    @Index(name = "idx_record_date", columnList = "record_date")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "check_in_device", length = 100)
    private String checkInDevice;

    @Column(name = "check_out_device", length = 100)
    private String checkOutDevice;

    @Column(name = "check_in_location", length = 255)
    private String checkInLocation;

    @Column(name = "check_out_location", length = 255)
    private String checkOutLocation;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.NORMAL;

    @Column(length = 500)
    private String remark;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
