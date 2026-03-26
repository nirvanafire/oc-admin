-- 迁移脚本：扩展审批流程模块相关表
-- 执行时间: 2026-03-26

-- 审批操作记录表
CREATE TABLE IF NOT EXISTS wf_approval_operation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    request_id BIGINT NOT NULL COMMENT '请求ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型: approve-批准, reject-驳回, transfer-转交, delegate-委派, add_sign-加签',
    comment VARCHAR(500) COMMENT '审批意见',
    target_user_id BIGINT COMMENT '转交/委派目标用户ID',
    target_user_name VARCHAR(100) COMMENT '目标用户姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_request_id (request_id),
    INDEX idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批操作记录表';

-- 审批抄送表
CREATE TABLE IF NOT EXISTS wf_approval_cc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL COMMENT '审批请求ID',
    user_id BIGINT NOT NULL COMMENT '被抄送人ID',
    user_name VARCHAR(100) COMMENT '被抄送人姓名',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批抄送表';

-- 催办记录表
CREATE TABLE IF NOT EXISTS wf_approval_reminder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL COMMENT '审批请求ID',
    task_id BIGINT COMMENT '任务ID',
    reminder_user_id BIGINT NOT NULL COMMENT '催办人ID',
    reminder_user_name VARCHAR(100) COMMENT '催办人姓名',
    reminder_time DATETIME COMMENT '催办时间',
    reminder_count INT DEFAULT 1 COMMENT '催办次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_request_id (request_id),
    INDEX idx_reminder_user_id (reminder_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催办记录表';
