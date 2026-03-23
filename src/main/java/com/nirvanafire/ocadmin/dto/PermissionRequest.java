package com.nirvanafire.ocadmin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {
    private Long id;

    @NotBlank private String code;

    @NotBlank private String name;

    @NotBlank private String category;

    private String permissionType = "button";

    private String description;

    private Integer permissionSort = 0;
}
