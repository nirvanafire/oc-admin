# CLAUDE.md

此文件为 Claude Code (claude.ai/code) 在本项目中工作提供指导。

## 项目概述

OC Admin 是一个基于 Spring Boot 3 + Spring Security 6 的 RBAC（基于角色的访问控制）权限管理系统，采用 JWT 进行身份认证，集成了 Flowable 工作流引擎。

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
- **ProcessDefinition** - 流程定义，存储 BPMN XML
- **ApprovalRequest** - 审核申请记录
- **ApprovalTask** - 审核任务记录
- **ApprovalNode** - 审核节点配置

## 配置

配置文件位于 `src/main/resources/application.yml`：
- 服务端口：8090
- 数据库：MySQL 8.x
- 缓存：Redis 7.x
- JWT 密钥和过期时间（默认：2小时）
- CORS 允许的来源：`http://localhost:5173`、`http://localhost:3000`

## API 端点

### 认证
- `POST /api/auth/login` - 登录（公开）
- `POST /api/auth/logout` - 登出（需认证）
- `GET /api/auth/current` - 获取当前用户（需认证）

### 用户管理
- `GET /api/users` - 用户列表（需要 user:list 权限）
- `POST /api/users` - 创建用户（需要 user:create 权限）
- `PUT /api/users/{id}` - 更新用户（需要 user:update 权限）
- `DELETE /api/users/{id}` - 删除用户（需要 user:delete 权限）

### 角色管理
- `GET /api/roles` - 角色列表（需要 role:list 权限）
- `GET /api/roles/all` - 获取所有角色（公开）
- `POST /api/roles` - 创建角色（需要 role:create 权限）
- `PUT /api/roles/{id}` - 更新角色（需要 role:update 权限）
- `DELETE /api/roles/{id}` - 删除角色（需要 role:delete 权限）

### 菜单管理
- `GET /api/menus/tree` - 获取菜单树
- `GET /api/menus/user` - 获取当前用户的菜单
- `POST /api/menus` - 创建菜单（需要 menu:create 权限）
- `PUT /api/menus/{id}` - 更新菜单（需要 menu:update 权限）
- `DELETE /api/menus/{id}` - 删除菜单（需要 menu:delete 权限）

### 工作流管理
- `POST /api/workflow/deploy` - 部署流程（需要 workflow:deploy 权限）
- `GET /api/workflow/definitions` - 流程定义列表（需要 workflow:list 权限）
- `PUT /api/workflow/definitions/{id}` - 更新流程定义（需要 workflow:deploy 权限）
- `DELETE /api/workflow/definitions/{id}` - 删除流程定义（需要 workflow:delete 权限）
- `POST /api/workflow/definitions/save` - 保存流程定义(新建/更新)（需要 workflow:deploy 权限）
- `POST /api/workflow/requests` - 提交审核申请（需要 workflow:request 权限）
- `GET /api/workflow/requests/my` - 获取我的申请（需要 workflow:request 权限）
- `GET /api/workflow/requests/{id}` - 获取申请详情（需要 workflow:request 权限）
- `GET /api/workflow/tasks/my` - 获取我的待审核任务（需要 workflow:approve 权限）
- `GET /api/workflow/tasks/{taskId}` - 获取任务详情（需要 workflow:approve 权限）
- `POST /api/workflow/tasks/{taskId}/complete` - 完成审核任务（需要 workflow:approve 权限）

## 工作流

### 流程定义
- 使用 Flowable 7.0.0 引擎
- `ProcessDefinition` 实体存储流程元数据和 BPMN XML
- 支持流程版本管理，通过 `processKey` + `version` 唯一标识
- 软删除机制：`status=0` 表示已删除

### 流程部署
- `WorkflowServiceImpl.deployProcess()` - 部署新流程到 Flowable 引擎
- `WorkflowServiceImpl.saveProcessDefinition()` - 保存流程定义，处理新建和更新
- 更新已有流程时自动递增版本号

### 审核流程
- 用户提交申请后创建 `ApprovalRequest` 记录
- 系统自动创建 `ApprovalTask` 任务分配给审核人
- 审核人通过/拒绝后，系统自动处理后续节点或结束流程

## 测试

测试使用 `@SpringBootTest` 配合 `@AutoConfigureMockMvc` 进行集成测试。测试类位于 `src/test/java/`。测试使用 `@Transactional` 保证数据隔离。
