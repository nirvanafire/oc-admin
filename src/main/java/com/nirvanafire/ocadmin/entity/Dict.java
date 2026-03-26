package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/** 字典实体 */
@Entity
@Table(name = "sys_dict")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Dict {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 50) private String dictType;
    @Column(nullable = false, length = 50) private String dictKey;
    @Column(nullable = false, length = 100) private String dictValue;
    @Column(length = 100) private String label;
    @Column @Builder.Default private Integer sort = 0;
    @CreatedDate @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
}
