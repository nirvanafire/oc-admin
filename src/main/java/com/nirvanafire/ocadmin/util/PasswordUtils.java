package com.nirvanafire.ocadmin.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码工具类
 */
public class PasswordUtils {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 要验证的密码
        String rawPassword = "admin123";

        // 生成一个新哈希
        String newHash = encoder.encode(rawPassword);
        System.out.println("admin123 的哈希: " + newHash);

        // 验证
        System.out.println("验证结果: " + encoder.matches(rawPassword, newHash));
    }
}
