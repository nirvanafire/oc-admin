-- 迁移脚本：创建文档管理模块相关表
-- 执行时间: 2026-03-26

-- 文档文件夹表
CREATE TABLE IF NOT EXISTS document_folder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父文件夹ID，0为根目录',
    name VARCHAR(100) NOT NULL COMMENT '文件夹名称',
    owner_id BIGINT NOT NULL COMMENT '所有者ID',
    owner_name VARCHAR(100) COMMENT '所有者姓名',
    is_public TINYINT(1) DEFAULT 0 COMMENT '是否公开',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档文件夹表';

-- 文档文件表
CREATE TABLE IF NOT EXISTS document_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    folder_id BIGINT NOT NULL COMMENT '文件夹ID',
    file_name VARCHAR(200) NOT NULL COMMENT '文件名称',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_type VARCHAR(50) COMMENT '文件类型',
    file_path VARCHAR(500) COMMENT '存储路径',
    file_url VARCHAR(500) COMMENT '访问URL',
    file_ext VARCHAR(20) COMMENT '文件扩展名',
    owner_id BIGINT NOT NULL COMMENT '上传人ID',
    owner_name VARCHAR(100) COMMENT '上传人姓名',
    version INT DEFAULT 1 COMMENT '版本号',
    is_latest TINYINT(1) DEFAULT 1 COMMENT '是否为最新版本',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    tags VARCHAR(500) COMMENT '标签，逗号分隔',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_folder_id (folder_id),
    INDEX idx_owner_id (owner_id),
    INDEX idx_file_name (file_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档文件表';

-- 文档版本表
CREATE TABLE IF NOT EXISTS document_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id BIGINT NOT NULL COMMENT '文件ID',
    version INT NOT NULL COMMENT '版本号',
    file_size BIGINT COMMENT '文件大小',
    file_path VARCHAR(500) COMMENT '存储路径',
    file_url VARCHAR(500) COMMENT '访问URL',
    comment VARCHAR(500) COMMENT '版本说明',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    creator_name VARCHAR(100) COMMENT '创建人姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_file_version (file_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档版本表';

-- 文档共享表
CREATE TABLE IF NOT EXISTS document_share (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id BIGINT NOT NULL COMMENT '文件ID',
    share_type VARCHAR(20) NOT NULL COMMENT '共享类型: DEPARTMENT-部门, USER-个人, PUBLIC-公开',
    target_id BIGINT COMMENT '目标ID（部门ID或用户ID）',
    can_download TINYINT(1) DEFAULT 1 COMMENT '允许下载',
    can_print TINYINT(1) DEFAULT 0 COMMENT '允许打印',
    expire_time DATETIME COMMENT '过期时间',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_file_id (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档共享表';
