package com.nirvanafire.ocadmin.dto;

import lombok.Data;
import java.util.Set;

/**
 * 用户导入DTO（用于Excel批量导入）
 */
@Data
public class UserImportDTO {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String deptName;
    private String roleNames; // 逗号分隔的角色名称
    private String remark;
    
    // 导入结果
    private Integer rowNum;
    private Boolean success;
    private String errorMessage;
}
