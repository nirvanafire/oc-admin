package com.nirvanafire.ocadmin.entity;

import com.nirvanafire.ocadmin.enums.AnnouncementStatus;
import com.nirvanafire.ocadmin.enums.AnnouncementType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 公告实体
 */
@Entity
@Table(name = "announcement", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_announcement_type", columnList = "announcement_type"),
    @Index(name = "idx_published_at", columnList = "published_at"),
    @Index(name = "idx_is_top", columnList = "is_top")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "announcement_type", nullable = false, length = 20)
    private AnnouncementType announcementType;

    @Column(name = "cover_image", length = 255)
    private String coverImage;

    @Column(name = "is_top")
    @Builder.Default
    private Boolean isTop = false;

    @Column(name = "top_expire_time")
    private LocalDateTime topExpireTime;

    @Column(name = "allow_comment")
    @Builder.Default
    private Boolean allowComment = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AnnouncementStatus status = AnnouncementStatus.DRAFT;

    @Column(name = "publisher_id", nullable = false)
    private Long publisherId;

    @Column(name = "publisher_name", length = 100)
    private String publisherName;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
