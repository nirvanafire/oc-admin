package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 任务实体 */
@Entity
@Table(name = "task")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 200) private String title;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "creator_id", nullable = false) private Long creatorId;
    @Column(name = "creator_name", length = 100) private String creatorName;
    @Column(name = "assignee_id") private Long assigneeId;
    @Column(name = "assignee_name", length = 100) private String assigneeName;
    @Column(name = "due_date") private LocalDate dueDate;
    @Column(length = 20) @Builder.Default private String priority = "NORMAL";
    @Column(length = 20) @Builder.Default private String status = "PENDING";
    @Column(name = "board_column", length = 20) @Builder.Default private String boardColumn = "TODO";
    @CreatedDate @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate @Column(name = "updated_at") private LocalDateTime updatedAt;
}
