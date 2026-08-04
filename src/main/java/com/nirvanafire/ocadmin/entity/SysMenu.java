package com.nirvanafire.ocadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_menu")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String path;

    @Column(length = 100)
    private String component;

    @Column(name = "menu_type", length = 10)
    @Builder.Default
    private String menuType = "menu";

    @Column(length = 50)
    private String icon;

    @Column(name = "parent_id")
    @Builder.Default
    private Long parentId = 0L;

    @Column(name = "menu_sort")
    @Builder.Default
    private Integer menuSort = 0;

    @Column(name = "visible", length = 10)
    @Builder.Default
    private String visible = "1";

    @Column(name = "keep_alive")
    @Builder.Default
    private Boolean keepAlive = true;

    @Column(name = "always_show")
    @Builder.Default
    private Boolean alwaysShow = false;

    @Column(length = 255)
    private String remark;

    @Column(name = "permission_code", length = 100, unique = true)
    private String permissionCode;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
