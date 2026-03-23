package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "wf_approval_request")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_instance_id", length = 100)
    private String processInstanceId;

    @Column(name = "business_key", length = 200)
    private String businessKey;

    @Column(name = "process_key", length = 100)
    private String processKey;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "applicant_name", nullable = false, length = 100)
    private String applicantName;

    @Column(name = "applicant_email", length = 100)
    private String applicantEmail;

    @Column(name = "current_node", length = 100)
    private String currentNode;

    @Column(name = "current_node_name", length = 100)
    private String currentNodeName;

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "form_data", columnDefinition = "JSON")
    private String formData;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;
}
