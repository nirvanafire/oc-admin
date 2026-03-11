package com.nirvanafire.ocadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 用户缓存DTO - 仅包含认证所需的最小信息
 * 不包含密码等敏感信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCacheDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 角色编码集合
     */
    private Set<String> roles;

    /**
     * 权限编码集合
     */
    private Set<String> permissions;
}
