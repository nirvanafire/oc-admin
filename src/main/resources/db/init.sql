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

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(100) COMMENT '路由路径',
    component VARCHAR(100) COMMENT '组件路径',
    menu_type VARCHAR(10) DEFAULT 'menu' COMMENT '菜单类型：directory-目录，menu-菜单，button-按钮',
    permission_code VARCHAR(100) COMMENT '权限标识符，仅button类型使用',
    icon VARCHAR(50) COMMENT '图标',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_sort INT DEFAULT 0 COMMENT '排序',
    visible VARCHAR(10) DEFAULT '1' COMMENT '是否显示',
    keep_alive TINYINT(1) DEFAULT 1 COMMENT '是否缓存',
    always_show TINYINT(1) DEFAULT 0 COMMENT '总是显示',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    UNIQUE INDEX uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 初始化数据

-- 创建默认管理员用户（密码：admin123）
INSERT INTO sys_user (username, password, nickname, email, enabled) VALUES
('admin', '$2a$10$oGvkVz2C/8i/HHf1CBsGUuHGNojLdY6D59Mlk1NQAvd5rV8OPEo9y', '系统管理员', 'admin@example.com', 1);

-- 创建默认角色
INSERT INTO sys_role (code, name, description, enabled) VALUES
('admin', '超级管理员', '拥有所有权限', 1),
('user', '普通用户', '普通用户权限', 1);

-- 创建默认菜单
INSERT INTO sys_menu (name, path, component, menu_type, icon, parent_id, menu_sort, visible) VALUES
('系统管理', '/system', 'Layout', 'directory', 'Setting', 0, 1, '1'),
('用户管理', '/system/users', '/system/users/index', 'menu', 'User', 1, 1, '1'),
('角色管理', '/system/roles', '/system/roles/index', 'menu', 'UserFilled', 1, 2, '1'),
('菜单管理', '/system/menus', '/system/menus/index', 'menu', 'Menu', 1, 3, '1');

-- 创建权限按钮节点（归属对应菜单）
INSERT INTO sys_menu (name, menu_type, permission_code, parent_id, menu_sort, visible) VALUES
('创建用户', 'button', 'user:create', 2, 1, '0'),
('修改用户', 'button', 'user:update', 2, 2, '0'),
('删除用户', 'button', 'user:delete', 2, 3, '0'),
('查看用户', 'button', 'user:view', 2, 4, '0'),
('用户列表', 'button', 'user:list', 2, 5, '0'),
('创建角色', 'button', 'role:create', 3, 1, '0'),
('修改角色', 'button', 'role:update', 3, 2, '0'),
('删除角色', 'button', 'role:delete', 3, 3, '0'),
('查看角色', 'button', 'role:view', 3, 4, '0'),
('角色列表', 'button', 'role:list', 3, 5, '0'),
('创建菜单', 'button', 'menu:create', 4, 1, '0'),
('修改菜单', 'button', 'menu:update', 4, 2, '0'),
('删除菜单', 'button', 'menu:delete', 4, 3, '0'),
('查看菜单', 'button', 'menu:view', 4, 4, '0'),
('菜单列表', 'button', 'menu:list', 4, 5, '0');

-- 关联管理员和超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 关联超级管理员和所有菜单（含权限按钮）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;
