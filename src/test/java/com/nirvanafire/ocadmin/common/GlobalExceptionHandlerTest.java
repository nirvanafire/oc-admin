package com.nirvanafire.ocadmin.common;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testBusinessException() throws Exception {
        // 测试业务异常处理（通过访问一个不存在的用户）
        // 需要构造一个会产生 BusinessException 的请求
        // 这里使用无效的用户ID测试
        mockMvc.perform(get("/api/users/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void testValidationException() throws Exception {
        // 发送一个验证失败的请求
        String requestBody = "{\"username\":\"\",\"password\":\"123\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAuthenticationException() throws Exception {
        // 访问受保护资源，不带token
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSuccessResponse() throws Exception {
        // 成功的响应结构
        String requestBody = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").exists());
    }
}
