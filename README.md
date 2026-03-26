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
| Flowable | 7.0.0 | 工作流引擎 |
| Spring Mail | - | 邮件服务 |

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
│   │   ├── RedisConfig.java             # Redis配置
│   │   ├── FlowableConfig.java          # Flowable配置
│   │   └── CorsConfig.java              # CORS配置
│   ├── controller/                      # 控制器层
│   │   ├── WorkflowController.java      # 工作流控制器
│   │   ├── AuthController.java          # 认证控制器
│   │   ├── UserController.java          # 用户控制器
│   │   ├── RoleController.java          # 角色控制器
│   │   ├── MenuController.java          # 菜单控制器
│   │   └── DeptController.java          # 部门控制器
│   ├── service/                         # 业务层
│   │   ├── WorkflowService.java         # 工作流服务
│   │   ├── EmailService.java            # 邮件服务
│   │   └── impl/                        # 服务实现
│   ├── repository/                      # 数据访问层
│   ├── entity/                          # 实体类
│   │   ├── ProcessDefinition.java       # 流程定义
│   │   ├── ApprovalRequest.java         # 审核申请
│   │   ├── ApprovalRequestData.java     # 审核申请数据（表单数据，独立表存储）
│   │   ├── ApprovalTask.java            # 审核任务
│   │   ├── ApprovalNode.java            # 审核节点配置
│   │   ├── SysUser.java                # 系统用户
│   │   ├── SysRole.java                # 角色
│   │   ├── SysPermission.java           # 权限
│   │   ├── SysMenu.java                # 菜单
│   │   ├── SysDept.java                # 部门（含level字段）
│   │   ├── SysDeptRole.java             # 部门角色关联
│   │   └── SysDeptRoleId.java           # 部门角色关联复合主键
│   ├── dto/                             # 数据传输对象
│   │   ├── ApprovalRequestDTO.java      # 申请详情DTO
│   │   ├── SubmitRequestResponse.java   # 提交申请响应（含待审核任务信息）
│   │   └── TaskDTO.java                 # 任务DTO
│   └── security/                        # 安全相关
│       ├── JwtTokenProvider.java        # JWT工具
│       ├── JwtAuthenticationFilter.java # JWT过滤器
│       └── CustomUserDetailsService.java
├── src/main/resources
│   ├── application.yml                  # 配置文件
│   └── db/
│       ├── init.sql                    # 初始化SQL
│       └── workflow.sql                # 工作流表SQL
└── pom.xml
```

## 数据库设计

### RBAC 模型

> **设计原则**：表关联不使用外键约束，由应用层负责 JOIN 查询。

- **sys_user** - 系统用户（密码 BCrypt 加密）
- **sys_role** - 角色定义（ADMIN、USER）
- **sys_permission** - 权限点（user:list、workflow:approve 等）
- **sys_menu** - 菜单项（用于动态路由）
- **sys_dept** - 部门表（树形结构，支持 level 字段标识层级）
- **sys_config** - 系统配置表

关联表（无外键）：
- sys_user_role - 用户角色关联
- sys_role_permission - 角色权限关联
- sys_role_menu - 角色菜单关联
- sys_menu_permission - 菜单权限关联
- sys_user_dept - 用户部门关联（多对多）
- sys_dept_role - 部门角色关联

### 部门层级

部门表支持多级树形结构：

| 级别 | 说明 | 示例 |
|------|------|------|
| 1 | 公司/总公司 | HQ |
| 2 | 事业部 | 技术中心 |
| 3 | 部门 | 研发部 |
| 4 | 小组 | 前端组 |

### 工作流模型

> **设计原则**：表关联不使用外键约束，由应用层负责 JOIN 查询。

- **wf_process_definition** - 流程定义表（存储 BPMN XML）
- **wf_approval_request** - 审核申请记录表（不含表单数据）
- **wf_approval_request_data** - 审核申请数据表（表单数据 JSON，独立存储）
- **wf_approval_node** - 审核节点配置表（支持多种审批人类型）
- **wf_approval_task** - 审核任务记录表

### 审批人类型

| 类型 | 说明 | 配置字段 |
|------|------|----------|
| USER | 指定用户 | approver_ids |
| ROLE | 指定角色 | approver_role |
| DEPT | 部门所有人 | approver_dept_id |
| DEPT_MANAGER | 部门领导 | approver_dept_id（可选，不填则用申请人主部门） |

**DEPT_MANAGER 说明**：
- 可选择特定部门的领导进行审批（如财务部审批报销）
- 不选择时，默认使用申请人主部门的领导
- 支持跨部门审批场景

## 快速开始

### 1. 环境准备

- Java 17+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.9+

### 2. 初始化数据库

**MySQL：**
```bash
mysql -u root -p < src/main/resources/db/init.sql
```

**PostgreSQL：**
```bash
psql -U postgres -d oc_admin -f src/main/resources/db/init.sql
```

### 3. 修改配置

配置文件位于 `src/main/resources/`，采用 Spring Profiles 机制切换数据库。

**激活 MySQL（默认）：**
```bash
mvn spring-boot:run
# 或设置环境变量
SPRING_PROFILES_ACTIVE=mysql mvn spring-boot:run
```

**激活 PostgreSQL：**
```bash
SPRING_PROFILES_ACTIVE=postgres mvn spring-boot:run
```

**各配置文件说明：**
- `application.yml` - 公共配置（Redis、邮件、JWT、Flowable 等）
- `application-mysql.yml` - MySQL 数据源和 JPA 配置
- `application-postgres.yml` - PostgreSQL 数据源和 JPA 配置

> 如需修改数据库连接，直接编辑对应的 profile 文件即可。

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

  # 邮件配置（可选，邮件功能预留接口）
  mail:
    host: smtp.example.com
    port: 587
    username: noreply@example.com
    password: your_password

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

- 基础地址: `http://localhost:8090`
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

### 部门管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/depts | GET | 部门列表（分页+搜索） | dept:list |
| /api/depts/all | GET | 全部部门 | dept:list |
| /api/depts/tree | GET | 部门树形结构 | dept:list |
| /api/depts/{id} | GET | 部门详情 | dept:view |
| /api/depts | POST | 创建部门 | dept:create |
| /api/depts/{id} | PUT | 更新部门 | dept:update |
| /api/depts/{id} | DELETE | 删除部门 | dept:delete |
| /api/depts/{id}/users | GET | 部门用户列表 | dept:list |
| /api/depts/{id}/roles | GET | 部门角色列表 | dept:list |
| /api/depts/{id}/users/{userId} | POST | 添加用户到部门 | dept:update |
| /api/depts/{id}/users/{userId} | DELETE | 从部门移除用户 | dept:update |
| /api/depts/{id}/roles/{roleId} | POST | 为部门分配角色 | dept:update |
| /api/depts/{id}/roles/{roleId} | DELETE | 取消部门角色 | dept:update |

### 工作流管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/workflow/deploy | POST | 部署流程 | workflow:deploy |
| /api/workflow/definitions | GET | 流程定义列表 | workflow:list |
| /api/workflow/definitions/{id} | PUT | 更新流程定义 | workflow:deploy |
| /api/workflow/definitions/{id} | DELETE | 删除流程定义 | workflow:delete |
| /api/workflow/definitions/save | POST | 保存流程定义(新建/更新) | workflow:deploy |
| /api/workflow/definitions/{id}/deployed | GET | 检查流程是否已部署 | - |
| /api/workflow/definitions/{id}/deploy | POST | 部署指定流程定义 | workflow:deploy |
| /api/workflow/definitions/batch-deploy | POST | 批量部署流程定义 | workflow:deploy |
| /api/workflow/definitions/deployed/all | GET | 获取所有已部署流程 | - |
| /api/workflow/requests | POST | 提交审核申请 | workflow:request |
| /api/workflow/requests/my | GET | 获取我的申请 | workflow:request |
| /api/workflow/requests/{id} | GET | 获取申请详情 | workflow:request |
| /api/workflow/requests/{id}/tasks | GET | 获取申请关联的任务列表 | - |
| /api/workflow/requests/{id}/cancel | POST | 撤销申请 | workflow:request |
| /api/workflow/requests/{id}/withdraw | POST | 撤回申请 | workflow:request |
| /api/workflow/tasks/my | GET | 获取我的待审核任务 | workflow:approve |
| /api/workflow/tasks/{taskId} | GET | 获取任务详情 | workflow:approve |
| /api/workflow/tasks/{taskId}/complete | POST | 完成审核任务 | workflow:approve |

## 工作流功能

### 流程设计

管理员可以通过 bpmn-js 在线设计审核流程：

1. **开始事件** - 流程起始节点（每个流程必须有且只有一个开始事件）
2. **用户任务** - 审核节点，可配置审核人类型：
   - 指定用户 - 多选用户
   - 指定角色 - 角色编码
   - 部门领导 - 可选择审批部门（默认申请人主部门）
3. **排他网关** - 条件分支，支持条件表达式如 `${amount > 10000}`
4. **结束事件** - 流程结束节点

### 审核流程

1. 用户提交审核申请
2. 系统根据流程配置自动分配审核任务
3. 提交申请后返回待审核任务信息
4. 审核人收到邮件通知（邮件服务预留接口）
5. 审核人通过/拒绝申请
6. 审核结果邮件通知申请人

### 审批人类型详解

**USER（指定用户）**：
- 多选用户，所有选中用户都可审批
- 先抢先审批

**ROLE（指定角色）**：
- 所有拥有该角色的用户都可见任务
- 先抢先审批

**DEPT（部门所有人）**：
- 指定部门下所有用户都可见任务
- 先抢先审批

**DEPT_MANAGER（部门领导）**：
- 可选择特定部门的领导进行审批
- 不选择时，默认使用申请人主部门的领导
- 支持跨部门审批场景（如财务部审批报销）

### 权限说明

- `workflow:list` - 查看流程列表
- `workflow:deploy` - 部署/编辑流程
- `workflow:delete` - 删除流程定义
- `workflow:request` - 提交审核申请
- `workflow:approve` - 审核任务
- `dept:list` - 部门列表
- `dept:create` - 创建部门
- `dept:update` - 更新部门
- `dept:delete` - 删除部门
- `dept:view` - 查看部门

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
- ✅ Flowable 工作流引擎
- ✅ 在线流程设计（bpmn-js）
- ✅ 多种审批人类型（用户/角色/部门/部门领导）
- ✅ 跨部门审批
- ✅ 邮件通知（预留接口）

## 安全特性

- 密码加密存储（BCrypt）
- JWT Token 签名验证
- Token 过期自动失效
- SQL 注入防护（JPA预编译）
- XSS 防护（前端渲染）
- CORS 跨域配置

## 测试

项目包含完整的单元测试和集成测试，覆盖各功能模块。

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=UserControllerTest

# 跳过测试
mvn clean package -DskipTests
```

## Docker 部署

项目提供完整的 Docker Compose 部署配置，一键启动 MySQL、Redis 和后端应用。

### 快速部署

```bash
# 方式一：使用部署脚本（推荐）
chmod +x deploy.sh
./deploy.sh

# 方式二：手动部署
cp .env.example .env
mvn clean package -DskipTests
docker-compose up -d
```

详细部署文档请参考 [DOCKER_DEPLOY.md](DOCKER_DEPLOY.md)

## 许可证

MIT License
