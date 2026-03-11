package com.nirvanafire.ocadmin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;
    
    @Size(min = 6, max = 100, message = "密码长度6-100位")
    private String password;
    
    @Size(max = 100, message = "昵称最长100位")
    private String nickname;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Size(max = 20, message = "手机号最长20位")
    private String phone;
    
    private String avatar;
    
    private Boolean enabled;
    
    private Set<Long> roleIds;

    private Set<String> roles;

    private Set<String> permissions;
}
