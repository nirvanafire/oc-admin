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
public class MenuDTO {
    private Long id;
    private String name;
    private String path;
    private String component;
    private String menuType;
    private String icon;
    private Long parentId;
    private Integer menuSort;
    private String visible;
    private Boolean keepAlive;
    private Boolean alwaysShow;
    private String remark;
    private String permissionCode;
    private List<MenuDTO> children;
}
