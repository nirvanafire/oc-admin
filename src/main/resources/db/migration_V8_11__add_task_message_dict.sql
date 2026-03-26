-- 迁移脚本：创建任务、消息、字典模块相关表
-- 执行时间: 2026-03-26

-- 任务表
CREATE TABLE IF NOT EXISTS task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务描述',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    creator_name VARCHAR(100) COMMENT '创建人姓名',
    assignee_id BIGINT COMMENT '负责人ID',
    assignee_name VARCHAR(100) COMMENT '负责人姓名',
    due_date DATE COMMENT '截止日期',
    priority VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级: LOW, NORMAL, HIGH, URGENT',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, IN_PROGRESS, COMPLETED',
    board_column VARCHAR(20) DEFAULT 'TODO' COMMENT '看板列: TODO, IN_PROGRESS, DONE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_creator_id (creator_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 消息表
CREATE TABLE IF NOT EXISTS message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    type VARCHAR(20) COMMENT '类型: APPROVAL, ATTENDANCE, ANNOUNCEMENT, SYSTEM',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    read_time DATETIME COMMENT '阅读时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 字典表
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
    dict_key VARCHAR(50) NOT NULL COMMENT '字典键',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典值',
    label VARCHAR(100) COMMENT '显示标签',
    sort INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type_key (dict_type, dict_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

-- 初始化字典数据
INSERT INTO sys_dict (dict_type, dict_key, dict_value, label, sort) VALUES
('gender', 'male', '男', '男', 1),
('gender', 'female', '女', '女', 2),
('yes_no', 'yes', '1', '是', 1),
('yes_no', 'no', '0', '否', 2);
