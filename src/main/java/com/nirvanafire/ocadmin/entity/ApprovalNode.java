package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "wf_approval_node")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_definition_id", nullable = false)
    private Long processDefinitionId;

    @Column(name = "node_key", nullable = false, length = 100)
    private String nodeKey;

    @Column(name = "node_name", nullable = false, length = 100)
    private String nodeName;

    @Column(name = "approver_type", length = 20)
    @Builder.Default
    private String approverType = "USER";

    @Column(name = "approver_ids", length = 500)
    private String approverIds;

    @Column(name = "approver_role", length = 50)
    private String approverRole;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
