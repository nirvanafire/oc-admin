package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/** 消息实体 */
@Entity
@Table(name = "message")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false, length = 200) private String title;
    @Column(columnDefinition = "TEXT") private String content;
    @Column(length = 20) private String type; // APPROVAL, ATTENDANCE, ANNOUNCEMENT, SYSTEM
    @Column @Builder.Default private Boolean isRead = false;
    @Column(name = "read_time") private LocalDateTime readTime;
    @CreatedDate @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
}
