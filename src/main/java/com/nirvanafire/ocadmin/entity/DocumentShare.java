package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 文档共享实体
 */
@Entity
@Table(name = "document_share")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "share_type", nullable = false, length = 20)
    private String shareType; // DEPARTMENT, USER, PUBLIC

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "can_download")
    @Builder.Default
    private Boolean canDownload = true;

    @Column(name = "can_print")
    @Builder.Default
    private Boolean canPrint = false;

    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
