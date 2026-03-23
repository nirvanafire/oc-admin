-- 创建数据库
CREATE DATABASE IF NOT EXISTS oc_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oc_admin;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(100) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    avatar VARCHAR(255) COMMENT '头像',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    account_non_expired TINYINT(1) DEFAULT 1 COMMENT '账户是否过期',
    account_non_locked TINYINT(1) DEFAULT 1 COMMENT '账户是否锁定',
    credentials_non_expired TINYINT(1) DEFAULT 1 COMMENT '凭证是否过期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '描述',
    role_sort INT DEFAULT 0 COMMENT '排序',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_type VARCHAR(20) DEFAULT 'button' COMMENT '权限类型：menu-菜单，button-按钮',
    description VARCHAR(255) COMMENT '描述',
    category VARCHAR(50) DEFAULT NULL COMMENT '权限分类：user, role, menu, workflow, permission',
    permission_sort INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(100) COMMENT '路由路径',
    component VARCHAR(100) COMMENT '组件路径',
    menu_type VARCHAR(10) DEFAULT 'menu' COMMENT '菜单类型：directory-目录，menu-菜单，button-按钮',
    icon VARCHAR(50) COMMENT '图标',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_sort INT DEFAULT 0 COMMENT '排序',
    visible VARCHAR(10) DEFAULT '1' COMMENT '是否显示',
    keep_alive TINYINT(1) DEFAULT 1 COMMENT '是否缓存',
    always_show TINYINT(1) DEFAULT 0 COMMENT '总是显示',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 菜单权限关联表
CREATE TABLE IF NOT EXISTS sys_menu_permission (
    menu_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (menu_id, permission_id),
    FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限关联表';

-- 初始化数据

-- 创建默认管理员用户（密码：admin123）
INSERT INTO sys_user (username, password, nickname, email, enabled) VALUES
('admin', '$2a$10$oGvkVz2C/8i/HHf1CBsGUuHGNojLdY6D59Mlk1NQAvd5rV8OPEo9y', '系统管理员', 'admin@example.com', 1);

-- 创建默认角色
INSERT INTO sys_role (code, name, description, enabled) VALUES
('admin', '超级管理员', '拥有所有权限', 1),
('user', '普通用户', '普通用户权限', 1);

-- 创建默认权限
INSERT INTO sys_permission (code, name, permission_type, category, permission_sort) VALUES
('user:create', '创建用户', 'button', 'user', 1),
('user:update', '修改用户', 'button', 'user', 2),
('user:delete', '删除用户', 'button', 'user', 3),
('user:view', '查看用户', 'button', 'user', 4),
('user:list', '用户列表', 'button', 'user', 5),
('role:create', '创建角色', 'button', 'role', 1),
('role:update', '修改角色', 'button', 'role', 2),
('role:delete', '删除角色', 'button', 'role', 3),
('role:view', '查看角色', 'button', 'role', 4),
('role:list', '角色列表', 'button', 'role', 5),
('menu:create', '创建菜单', 'button', 'menu', 1),
('menu:update', '修改菜单', 'button', 'menu', 2),
('menu:delete', '删除菜单', 'button', 'menu', 3),
('menu:view', '查看菜单', 'button', 'menu', 4),
('menu:list', '菜单列表', 'button', 'menu', 5),
('workflow:list', '流程列表', 'button', 'workflow', 1),
('workflow:deploy', '部署流程', 'button', 'workflow', 2),
('workflow:delete', '删除流程', 'button', 'workflow', 3),
('workflow:request', '提交申请', 'button', 'workflow', 4),
('workflow:approve', '审核任务', 'button', 'workflow', 5),
('permission:list', '权限列表', 'button', 'permission', 1),
('permission:create', '创建权限', 'button', 'permission', 2),
('permission:update', '修改权限', 'button', 'permission', 3),
('permission:delete', '删除权限', 'button', 'permission', 4);

-- 创建默认菜单
INSERT INTO sys_menu (name, path, component, menu_type, icon, parent_id, menu_sort, visible) VALUES
('系统管理', '/system', 'Layout', 'directory', 'Setting', 0, 1, '1'),
('用户管理', '/system/users', '/system/users/index', 'menu', 'User', 1, 1, '1'),
('角色管理', '/system/roles', '/system/roles/index', 'menu', 'UserFilled', 1, 2, '1'),
('菜单管理', '/system/menus', '/system/menus/index', 'menu', 'Menu', 1, 3, '1'),
('权限管理', '/system/permissions', '/system/permissions/index', 'menu', 'Lock', 1, 4, '1');

-- 设置工作流目录的父菜单ID变量
SET @workflow_parent_id = (SELECT LAST_INSERT_ID());

-- 添加工作流管理目录
INSERT INTO sys_menu (name, path, component, menu_type, icon, parent_id, menu_sort, visible) VALUES
('工作流管理', '/workflow', 'Layout', 'directory', 'DocumentCopy', 0, 2, '1');

SET @workflow_parent_id = LAST_INSERT_ID();

-- 添加工作流子菜单
INSERT INTO sys_menu (name, path, component, menu_type, icon, parent_id, menu_sort, visible) VALUES
('流程管理', '/workflow/processes', '/workflow/processes/index', 'menu', 'Document', @workflow_parent_id, 1, '1'),
('我的申请', '/workflow/requests', '/workflow/requests/index', 'menu', 'List', @workflow_parent_id, 2, '1'),
('待审核任务', '/workflow/tasks', '/workflow/tasks/index', 'menu', 'Check', @workflow_parent_id, 3, '1');

-- 关联管理员和超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 关联超级管理员和所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 关联超级管理员和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- ============================================
-- 迁移脚本：已有数据库执行以下语句
-- ============================================
-- ALTER TABLE sys_permission ADD COLUMN category VARCHAR(50) DEFAULT NULL COMMENT '权限分类';
-- ALTER TABLE sys_permission ADD COLUMN permission_sort INT DEFAULT 0 COMMENT '排序';
--
-- -- 更新现有数据分类
-- UPDATE sys_permission SET category = 'user', permission_sort = 1 WHERE code LIKE 'user:%';
-- UPDATE sys_permission SET category = 'role', permission_sort = 2 WHERE code LIKE 'role:%';
-- UPDATE sys_permission SET category = 'menu', permission_sort = 3 WHERE code LIKE 'menu:%';
-- UPDATE sys_permission SET category = 'workflow', permission_sort = 4 WHERE code LIKE 'workflow:%';
-- UPDATE sys_permission SET category = 'permission', permission_sort = 5 WHERE code LIKE 'permission:%';
--
-- -- 添加新的权限管理相关权限（如果不存在）
-- INSERT INTO sys_permission (code, name, permission_type, category, permission_sort)
-- SELECT 'permission:create', '创建权限', 'button', 'permission', 2 WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'permission:create');
-- INSERT INTO sys_permission (code, name, permission_type, category, permission_sort)
-- SELECT 'permission:update', '修改权限', 'button', 'permission', 3 WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'permission:update');
-- INSERT INTO sys_permission (code, name, permission_type, category, permission_sort)
-- SELECT 'permission:delete', '删除权限', 'button', 'permission', 4 WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'permission:delete');
--
-- -- 添加权限管理菜单（如果不存在）
-- INSERT INTO sys_menu (name, path, component, menu_type, icon, parent_id, menu_sort, visible)
-- SELECT '权限管理', '/system/permissions', '/system/permissions/index', 'menu', 'Lock', 1, 4, '1' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/system/permissions');
--
-- -- 将新权限关联给admin角色
-- INSERT INTO sys_role_permission (role_id, permission_id)
-- SELECT 1, id FROM sys_permission WHERE code IN ('permission:create', 'permission:update', 'permission:delete')
-- AND NOT EXISTS (SELECT 1 FROM sys_role_permission WHERE role_id = 1 AND permission_id = (SELECT id FROM sys_permission WHERE code = 'permission:create'));
--
-- -- 将权限管理菜单关联给admin角色
-- INSERT INTO sys_role_menu (role_id, menu_id)
-- SELECT 1, id FROM sys_menu WHERE path = '/system/permissions'
-- AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = (SELECT id FROM sys_menu WHERE path = '/system/permissions'));
