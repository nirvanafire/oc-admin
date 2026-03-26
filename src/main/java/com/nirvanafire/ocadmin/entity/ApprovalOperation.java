package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 审批操作记录实体
 */
@Entity
@Table(name = "wf_approval_operation")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    @Column(name = "operation_type", nullable = false, length = 20)
    private String operationType; // approve-批准, reject-驳回, transfer-转交, delegate-委派, add_sign-加签

    @Column(length = 500)
    private String comment;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "target_user_name", length = 100)
    private String targetUserName;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
