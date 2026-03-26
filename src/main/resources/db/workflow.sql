-- 流程定义表
CREATE TABLE IF NOT EXISTS wf_process_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_key VARCHAR(100) NOT NULL,
    process_name VARCHAR(200) NOT NULL,
    description TEXT,
    flowable_definition_id VARCHAR(100),
    version INT DEFAULT 1,
    status TINYINT DEFAULT 1 COMMENT '1: 激活, 0: 禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_process_key_version (process_key, version)
);

-- 审核申请记录表
CREATE TABLE IF NOT EXISTS wf_approval_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_instance_id VARCHAR(100),
    business_key VARCHAR(200),
    process_key VARCHAR(100),
    title VARCHAR(200) NOT NULL,
    applicant_id BIGINT NOT NULL,
    applicant_name VARCHAR(100) NOT NULL,
    applicant_email VARCHAR(100),
    current_node VARCHAR(100),
    current_node_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING: 待审核, APPROVED: 已通过, REJECTED: 已拒绝, CANCELLED: 已撤销',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    complete_time DATETIME,
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status),
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_process_key (process_key)
);

-- 审核申请数据表（存储表单数据，独立出来优化查询性能）
CREATE TABLE IF NOT EXISTS wf_approval_request_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    form_data JSON,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_request_id (request_id)
);

-- 审核节点配置表
CREATE TABLE IF NOT EXISTS wf_approval_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_definition_id BIGINT NOT NULL,
    node_key VARCHAR(100) NOT NULL,
    node_name VARCHAR(100) NOT NULL,
    approver_type VARCHAR(20) DEFAULT 'USER' COMMENT 'USER: 指定用户, ROLE: 指定角色',
    approver_ids VARCHAR(500) COMMENT '当approver_type=USER时，存储用户ID列表，逗号分隔',
    approver_role VARCHAR(50) COMMENT '当approver_type=ROLE时，存储角色代码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_process_definition_id (process_definition_id)
);

-- 审核任务记录表
CREATE TABLE IF NOT EXISTS wf_approval_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(100),
    request_id BIGINT NOT NULL,
    assignee_id BIGINT,
    assignee_name VARCHAR(100),
    assignee_email VARCHAR(100),
    action VARCHAR(20) COMMENT 'APPROVE: 通过, REJECT: 拒绝',
    comment TEXT,
    task_status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING: 待处理, COMPLETED: 已处理, REJECTED: 已拒绝, CANCELLED: 已撤销',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    complete_time DATETIME,
    INDEX idx_request_id (request_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_task_status (task_status)
);
