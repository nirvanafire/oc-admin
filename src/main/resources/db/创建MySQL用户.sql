-- =========================
-- 1. 创建数据库（如果不存在）
-- =========================
CREATE DATABASE IF NOT EXISTS oc_admin
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

-- =========================
-- 2. 创建用户（允许远程访问）
-- =========================
CREATE USER IF NOT EXISTS 'ocadmin'@'%' IDENTIFIED BY 'ocadmin';

-- =========================
-- 3. 授权数据库全部权限
-- =========================
GRANT ALL PRIVILEGES ON oc_admin.* TO 'ocadmin'@'%';

-- =========================
-- 4. 刷新权限
-- =========================
FLUSH PRIVILEGES;

-- =========================
-- 5. 查看授权（可选）
-- =========================
SHOW GRANTS FOR 'ocadmin'@'%';