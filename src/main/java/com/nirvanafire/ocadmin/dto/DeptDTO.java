package com.nirvanafire.ocadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeptDTO {

    private Long id;

    private Long parentId;

    private String parentName;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称最长100位")
    private String deptName;

    @Size(max = 50, message = "部门编码最长50位")
    private String deptCode;

    private Long managerId;

    private String managerName;

    private Integer sortOrder;

    private Integer status;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<DeptDTO> children;
}
