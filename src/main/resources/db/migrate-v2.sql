-- 一次性迁移脚本：将 sys_permission 合并到 sys_menu 中作为 button 类型节点
-- 执行前请先备份数据库！

-- 新增 permission_code 列
ALTER TABLE sys_menu ADD COLUMN permission_code VARCHAR(100) DEFAULT NULL COMMENT '权限标识符，仅button类型使用' AFTER menu_type;
ALTER TABLE sys_menu ADD UNIQUE INDEX uk_permission_code (permission_code);

-- 将已关联菜单的权限迁移为 button 类型子节点
INSERT INTO sys_menu (name, menu_type, permission_code, parent_id, menu_sort, visible, keep_alive, always_show)
SELECT p.name, 'button', p.code, mp.menu_id, 0, '0', 0, 0
FROM sys_permission p
INNER JOIN sys_menu_permission mp ON mp.permission_id = p.id;

-- 将未关联菜单的权限迁移到第一个目录节点下
INSERT INTO sys_menu (name, menu_type, permission_code, parent_id, menu_sort, visible, keep_alive, always_show)
SELECT p.name, 'button', p.code,
    COALESCE((SELECT id FROM sys_menu WHERE menu_type = 'directory' ORDER BY id LIMIT 1), 0),
    0, '0', 0, 0
FROM sys_permission p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu_permission mp WHERE mp.permission_id = p.id);

-- 将角色-权限关联转换为角色-菜单关联
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT rp.role_id, m.id
FROM sys_role_permission rp
INNER JOIN sys_permission p ON p.id = rp.permission_id
INNER JOIN sys_menu m ON m.permission_code = p.code AND m.menu_type = 'button';

-- 重命名旧表为备份（验证无误后手动 DROP）
RENAME TABLE sys_menu_permission TO _backup_menu_permission;
RENAME TABLE sys_role_permission TO _backup_role_permission;
RENAME TABLE sys_permission TO _backup_permission;
