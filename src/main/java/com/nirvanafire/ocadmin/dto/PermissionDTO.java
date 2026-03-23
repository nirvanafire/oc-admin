package com.nirvanafire.ocadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {
    private Long id;
    private String code;
    private String name;
    private String category;
    private String permissionType;
    private String description;
    private Integer permissionSort;
    private List<PermissionDTO> children;  // 树形结构用
}
