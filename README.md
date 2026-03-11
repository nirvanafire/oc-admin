# OC Admin - RBAC权限管理系统

基于 Spring Boot 3 + Spring Security 6 + JWT 的现代化权限管理后台系统。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 核心框架 |
| Spring Security | 6.x | 安全认证 |
| Spring Data JPA | 3.x | 数据持久层 |
| JWT | 0.12.x | Token认证 |
| Redis | 7.x | 缓存、Token黑名单 |
| MySQL | 8.x | 数据库 |
| Java | 17+ | JDK版本 |
| Maven | 3.9+ | 构建工具 |

## 项目结构

```
oc-admin
├── src/main/java/com/nirvanafire/ocadmin
│   ├── OcAdminApplication.java          # 启动类
│   ├── common/                          # 通用组件
│   │   ├── Result.java                  # 统一响应体
│   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   └── exception/                   # 自定义异常
│   ├── config/                          # 配置类
│   │   ├── SecurityConfig.java          # Security配置
│   │   └── RedisConfig.java             # Redis配置
│   ├── controller/                      # 控制器层
│   ├── service/                         # 业务层
│   ├── repository/                      # 数据访问层
│   ├── entity/                          # 实体类
│   ├── dto/                             # 数据传输对象
│   └── security/                        # 安全相关
│       ├── JwtTokenProvider.java        # JWT工具
│       ├── JwtAuthenticationFilter.java # JWT过滤器
│       └── CustomUserDetailsService.java
├── src/main/resources
│   ├── application.yml                  # 配置文件
│   └── db/init.sql                      # 初始化SQL
└── pom.xml
```

## 数据库设计

采用 RBAC（Role-Based Access Control）模型：

- **用户 (sys_user)** - 系统用户
- **角色 (sys_role)** - 角色定义
- **权限 (sys_permission)** - 权限点
- **菜单 (sys_menu)** - 系统菜单

关联表：
- sys_user_role - 用户角色关联
- sys_role_permission - 角色权限关联
- sys_role_menu - 角色菜单关联

## 快速开始

### 1. 环境准备

- Java 17+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.9+

### 2. 初始化数据库

```bash
mysql -u root -p < src/main/resources/db/init.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/oc_admin?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    password: your_password  # 如无密码留空

jwt:
  secret: your-secret-key-here  # 至少32位
  expiration: 7200000  # 2小时，单位毫秒
```

### 4. 运行项目

```bash
# 开发模式
mvn spring-boot:run

# 打包
mvn clean package

# 运行jar
java -jar target/oc-admin-1.0.0.jar
```

### 5. 访问接口

- 基础地址: `http://localhost:8080`
- 登录接口: `POST /api/auth/login`

### 默认账号

- 用户名: `admin`
- 密码: `admin123`

## API 接口

### 认证相关

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 用户登录 |
| /api/auth/logout | POST | 退出登录 |
| /api/auth/current | GET | 获取当前用户 |

### 用户管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/users | GET | 用户列表 | user:list |
| /api/users | POST | 创建用户 | user:create |
| /api/users/{id} | PUT | 更新用户 | user:update |
| /api/users/{id} | DELETE | 删除用户 | user:delete |

### 角色管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/roles | GET | 角色列表 | role:list |
| /api/roles/all | GET | 所有角色 | - |
| /api/roles | POST | 创建角色 | role:create |
| /api/roles/{id} | PUT | 更新角色 | role:update |
| /api/roles/{id} | DELETE | 删除角色 | role:delete |

### 菜单管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/menus/tree | GET | 菜单树 | - |
| /api/menus/user | GET | 用户菜单 | - |
| /api/menus | POST | 创建菜单 | menu:create |
| /api/menus/{id} | PUT | 更新菜单 | menu:update |
| /api/menus/{id} | DELETE | 删除菜单 | menu:delete |

## 前端项目

配套前端项目使用 Vue3 开发：

👉 [oc-admin-vue3](https://github.com/nirvanafire/oc-admin-vue3)

## 核心功能

- ✅ JWT Token 认证
- ✅ Token 黑名单（登出失效）
- ✅ RBAC 权限控制
- ✅ 菜单动态路由
- ✅ 接口权限校验
- ✅ 全局异常处理
- ✅ Redis 缓存

## 安全特性

- 密码加密存储（BCrypt）
- JWT Token 签名验证
- Token 过期自动失效
- SQL 注入防护（JPA预编译）
- XSS 防护（前端渲染）
- CORS 跨域配置

## 许可证

MIT License
