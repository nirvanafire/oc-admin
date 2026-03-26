-- 迁移脚本：为sys_user表添加个人中心扩展字段
-- 执行时间: 2026-03-26

-- 添加个人资料扩展字段
ALTER TABLE sys_user 
ADD COLUMN IF NOT EXISTS signature VARCHAR(255) COMMENT '个人签名' AFTER avatar;

-- 添加消息通知设置字段
ALTER TABLE sys_user 
ADD COLUMN IF NOT EXISTS notify_approval TINYINT(1) DEFAULT 1 COMMENT '审批通知' AFTER signature;

ALTER TABLE sys_user 
ADD COLUMN IF NOT EXISTS notify_attendance TINYINT(1) DEFAULT 1 COMMENT '考勤通知' AFTER notify_approval;

ALTER TABLE sys_user 
ADD COLUMN IF NOT EXISTS notify_announcement TINYINT(1) DEFAULT 1 COMMENT '公告通知' AFTER notify_attendance;

ALTER TABLE sys_user 
ADD COLUMN IF NOT EXISTS notify_email TINYINT(1) DEFAULT 0 COMMENT '邮件通知' AFTER notify_announcement;

ALTER TABLE sys_user 
ADD COLUMN IF NOT EXISTS notify_sms TINYINT(1) DEFAULT 0 COMMENT '短信通知' AFTER notify_email;

-- 添加部门ID字段
ALTER TABLE sys_user 
ADD COLUMN IF NOT EXISTS dept_id BIGINT COMMENT '所属部门ID' AFTER notify_sms;

-- 添加索引
ALTER TABLE sys_user 
ADD INDEX IF NOT EXISTS idx_dept_id (dept_id);

-- 更新默认管理员的通知设置
UPDATE sys_user SET notify_approval = 1, notify_attendance = 1, notify_announcement = 1, notify_email = 0, notify_sms = 0 WHERE username = 'admin';
