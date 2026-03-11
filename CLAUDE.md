# CLAUDE.md

此文件为 Claude Code (claude.ai/code) 在本项目中工作提供指导。

## 项目概述

OC Admin 是一个基于 Spring Boot 3 + Spring Security 6 的 RBAC（基于角色的访问控制）权限管理系统，采用 JWT 进行身份认证。

## 常用命令

```bash
# 运行应用程序
mvn spring-boot:run

# 构建项目
mvn clean package

# 构建但不运行测试
mvn clean package -DskipTests

# 运行所有测试
mvn test

# 运行指定的测试类
mvn test -Dtest=UserControllerTest

# 运行指定包下的测试
mvn test -Dtest="com.nirvanafire.ocadmin.service.*"

# 生成测试覆盖率报告
mvn test jacoco:report
```

## 架构

这是一个标准的 Spring Boot 分层架构：

- **Controller 层** (`src/main/java/.../controller/`) - REST API 端点，处理 HTTP 请求/响应
- **Service 层** (`src/main/java/.../service/`) - 业务逻辑，事务管理
- **Repository 层** (`src/main/java/.../repository/`) - 使用 Spring Data JPA 进行数据访问
- **Entity 层** (`src/main/java/.../entity/`) - JPA 实体/数据库表

## 核心组件

### 安全
- **JwtTokenProvider** - 生成和验证 JWT 令牌
- **JwtAuthenticationFilter** - 拦截请求，验证 JWT，设置安全上下文
- **CustomUserDetailsService** - 从数据库加载用户信息用于身份认证
- **SecurityConfig** - Spring Security 配置（无状态会话、CORS、授权规则）

### 认证流程
1. 用户向 `/api/auth/login` 发送用户名/密码的 POST 请求
2. Spring Security 验证凭证
3. JwtTokenProvider 生成 JWT 令牌
4. 登出时令牌存储到 Redis 黑名单

### 授权
- 通过 `@PreAuthorize` 注解实现方法级安全（如 `@PreAuthorize("hasAuthority('user:list')")`）
- 权限存储在 `sys_permission` 表
- 角色通过 `sys_role_permission` 表分配权限

### 数据库实体
- **SysUser** - 系统用户，密码使用 BCrypt 加密存储
- **SysRole** - 角色（如 ADMIN、USER）
- **SysPermission** - 权限代码（如 user:list、user:create）
- **SysMenu** - 菜单项，用于动态路由

## 配置

配置文件位于 `src/main/resources/application.yml`：
- 数据库：MySQL 8.x
- 缓存：Redis 7.x
- JWT 密钥和过期时间（默认：2小时）
- CORS 允许的来源：`http://localhost:5173`、`http://localhost:3000`

## API 端点

- `POST /api/auth/login` - 登录（公开）
- `POST /api/auth/logout` - 登出（需认证）
- `GET /api/auth/current` - 获取当前用户（需认证）
- `GET /api/users` - 用户列表（需要 user:list 权限）
- `GET /api/roles` - 角色列表（需要 role:list 权限）
- `GET /api/roles/all` - 获取所有角色（公开）
- `GET /api/menus/tree` - 获取菜单树
- `GET /api/menus/user` - 获取当前用户的菜单

## 测试

测试使用 `@SpringBootTest` 配合 `@AutoConfigureMockMvc` 进行集成测试。测试类位于 `src/test/java/`。测试使用 `@Transactional` 保证数据隔离。
