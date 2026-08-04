-- 一次性迁移脚本：将 sys_permission 合并到 sys_menu 中作为 button 类型节点

-- Step 1: Add permission_code column to sys_menu
ALTER TABLE sys_menu ADD COLUMN permission_code VARCHAR(100) DEFAULT NULL COMMENT '权限标识符，仅button类型使用' AFTER menu_type;
ALTER TABLE sys_menu ADD UNIQUE INDEX uk_permission_code (permission_code);

-- Step 2: Migrate sys_permission data into sys_menu as button-type nodes
-- For each permission linked to a menu via sys_menu_permission, insert as child of that menu
INSERT INTO sys_menu (name, menu_type, permission_code, parent_id, menu_sort, visible, keep_alive, always_show)
SELECT p.name, 'button', p.code, mp.menu_id, 0, '0', 0, 0
FROM sys_permission p
INNER JOIN sys_menu_permission mp ON mp.permission_id = p.id;

-- For permissions not linked to any menu, insert as children of the first directory
INSERT INTO sys_menu (name, menu_type, permission_code, parent_id, menu_sort, visible, keep_alive, always_show)
SELECT p.name, 'button', p.code,
    (SELECT id FROM sys_menu WHERE menu_type = 'directory' ORDER BY id LIMIT 1),
    0, '0', 0, 0
FROM sys_permission p
WHERE p.id NOT IN (SELECT permission_id FROM sys_menu_permission);

-- Step 3: Migrate sys_role_permission to sys_role_menu using new button node ids
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT rp.role_id, m.id
FROM sys_role_permission rp
INNER JOIN sys_permission p ON p.id = rp.permission_id
INNER JOIN sys_menu m ON m.permission_code = p.code AND m.menu_type = 'button';

-- Step 4: Drop legacy tables
DROP TABLE IF EXISTS sys_menu_permission;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_permission;
