/*
 * 数据库初始化脚本（终极加强版）
 * 项目：基于 OCR + NLP 的高校教务材料自动处理系统
 * 数据库版本：MySQL 8.0+
 * 作者：System Architect
 * 时间：2025-05-21 (China Standard Time)
 */
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
-- =================================================================
-- 0. 先按依赖顺序 DROP（有外键也不会报错，因为已关闭检查）
-- =================================================================
DROP TABLE IF EXISTS audit_record;
DROP TABLE IF EXISTS sys_task_log;
DROP TABLE IF EXISTS ai_training_sample;
DROP TABLE IF EXISTS document_extract_detail;
DROP TABLE IF EXISTS document_extract_main;
DROP TABLE IF EXISTS document_ocr_raw;
DROP TABLE IF EXISTS document_file;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_doc_template;
-- =================================================================
-- 1. 系统基础配置与模版表 (定义系统能识别什么类型的文档)
-- =================================================================
CREATE TABLE sys_doc_template (
    id BIGINT UNSIGNED NOT NULL COMMENT '模版ID (雪花算法)',
    template_name VARCHAR(50) NOT NULL COMMENT '模版名称：学生成绩单/请假条/资产采购单',
    template_code VARCHAR(50) NOT NULL UNIQUE COMMENT '编码：TRANSCRIPT, LEAVE_APP, ASSET_LIST',
    -- 示例: [{"key":"student_name", "label":"姓名"}]
    target_kv_config JSON COMMENT 'KV键值对字段提取规则',
    -- 示例: [{"key":"course", "label":"课程"}, {"key":"score", "label":"分数"}]
    target_table_config JSON COMMENT '表格明细提取规则',
    -- 示例: {"logic_check": "total_score == regular * 0.3 + exam * 0.7"}
    rule_config JSON COMMENT '校验与纠错规则配置',
    status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CHECK (status IN (0, 1)),
    CHECK (
        target_kv_config IS NULL
        OR JSON_VALID(target_kv_config)
    ),
    CHECK (
        target_table_config IS NULL
        OR JSON_VALID(target_table_config)
    ),
    CHECK (
        rule_config IS NULL
        OR JSON_VALID(rule_config)
    ),
    PRIMARY KEY(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文档识别模版配置表';
-- =================================================================
-- 2. 用户与权限模块 (采用 RBAC + Token 版本号)
-- =================================================================
CREATE TABLE sys_user (
    id BIGINT UNSIGNED NOT NULL COMMENT '用户ID（雪花算法）',
    username VARCHAR(50) NOT NULL COMMENT '工号/学号',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    nickname VARCHAR(50),
    email VARCHAR(150),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    token_version INT DEFAULT 1 COMMENT 'Token版本号：用于强制旧Token失效',
    status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '乐观锁',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CHECK (status IN (0, 1)),
    CHECK (deleted IN (0, 1)),
    PRIMARY KEY(id),
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone),
    KEY idx_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';
CREATE TABLE sys_role (
    id BIGINT UNSIGNED NOT NULL COMMENT '角色ID（雪花算法）',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(100) NOT NULL COMMENT '角色标识，如 ROLE_ADMIN, ROLE_TEACHER',
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    CHECK (status IN (0, 1)),
    CHECK (deleted IN (0, 1)),
    PRIMARY KEY(id),
    UNIQUE KEY uk_role_key(role_key),
    UNIQUE KEY uk_role_name(role_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';
CREATE TABLE sys_user_role (
    id BIGINT UNSIGNED NOT NULL COMMENT '主键ID（雪花算法）',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY(id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_role (user_id, role_id),
    KEY idx_role_user (role_id, user_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户角色关联表';
-- =================================================================
-- 3. 教务材料主表 (全生命周期管理)
-- =================================================================
CREATE TABLE document_file (
    id BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '上传者',
    -- [业务分类]
    template_id BIGINT UNSIGNED COMMENT '识别模版ID (可由AI自动分类填充)',
    batch_no VARCHAR(50) COMMENT '批次号',
    -- [文件信息]
    file_no VARCHAR(50) UNIQUE COMMENT '业务流水号 DOC20250521001',
    file_name VARCHAR(255),
    file_type VARCHAR(20) COMMENT 'img/pdf',
    file_size BIGINT,
    -- [存储路径]
    minio_bucket VARCHAR(64) NOT NULL,
    minio_object VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) COMMENT '缩略图访问地址',
    -- [状态机] 0:待处理 1:队列中 2:处理中 3:待人工校对 4:已归档 5:处理失败
    process_status TINYINT DEFAULT 0,
    -- [智能调度策略]
    process_mode VARCHAR(20) DEFAULT 'STANDARD' COMMENT 'STANDARD(Paddle+BERT) / LLM_FALLBACK(大模型)',
    retry_count INT DEFAULT 0 COMMENT '已重试次数',
    fail_reason VARCHAR(500) COMMENT '失败原因',
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CHECK (process_status IN (0, 1, 2, 3, 4, 5)),
    CHECK (deleted IN (0, 1)),
    PRIMARY KEY(id),
    KEY idx_user (user_id),
    KEY idx_status (process_status),
    KEY idx_batch (batch_no),
    KEY idx_template (template_id),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_document_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE RESTRICT,
    CONSTRAINT fk_document_template FOREIGN KEY (template_id) REFERENCES sys_doc_template(id) ON DELETE
    SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '教务材料文件记录表';
-- =================================================================
-- 4. AI 原始识别结果表 (存储 PaddleOCR/LLM 原始输出)
-- =================================================================
CREATE TABLE document_ocr_raw (
    id BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    file_id BIGINT UNSIGNED NOT NULL COMMENT '关联文件ID',
    full_text LONGTEXT COMMENT 'OCR识别出的所有纯文本拼接',
    raw_data_json JSON COMMENT '带坐标的原始数据',
    text_count INT GENERATED ALWAYS AS (JSON_LENGTH(raw_data_json)) VIRTUAL COMMENT '文本块数量',
    avg_confidence DECIMAL(5, 4) COMMENT '整页平均置信度',
    is_llm_used TINYINT DEFAULT 0 COMMENT '是否使用了大模型',
    token_usage INT DEFAULT 0 COMMENT 'LLM Token消耗量',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    CHECK (is_llm_used IN (0, 1)),
    CHECK (
        raw_data_json IS NULL
        OR JSON_VALID(raw_data_json)
    ),
    PRIMARY KEY(id),
    UNIQUE KEY uk_file (file_id),
    KEY idx_file (file_id),
    KEY idx_text_len(text_count),
    KEY idx_avg_confidence (avg_confidence),
    CONSTRAINT fk_ocr_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI原始识别结果表';
-- =================================================================
-- 5. 通用抽取结果主表 (KV数据 - 表头信息)
-- =================================================================
CREATE TABLE document_extract_main (
    id BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    file_id BIGINT UNSIGNED NOT NULL COMMENT '文件ID',
    template_id BIGINT UNSIGNED COMMENT '冗余模版ID',
    owner_id VARCHAR(50) COMMENT '通用学号/工号',
    owner_name VARCHAR(50) COMMENT '通用姓名/申请人',
    doc_date DATE COMMENT '文档业务日期',
    kv_data_json JSON COMMENT '所有非表格的提取数据',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CHECK (
        kv_data_json IS NULL
        OR JSON_VALID(kv_data_json)
    ),
    PRIMARY KEY(id),
    UNIQUE KEY uk_file_main (file_id),
    KEY idx_owner_id (owner_id),
    KEY idx_owner_name (owner_name),
    KEY idx_doc_date (doc_date),
    CONSTRAINT fk_extract_main_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE CASCADE,
    CONSTRAINT fk_extract_main_template FOREIGN KEY (template_id) REFERENCES sys_doc_template(id) ON DELETE
    SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文档关键信息提取主表(KV)';
-- =================================================================
-- 6. 通用表格明细表 (List数据 - 表体信息)
-- =================================================================
CREATE TABLE document_extract_detail (
    id BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    file_id BIGINT UNSIGNED NOT NULL COMMENT '文件ID',
    main_id BIGINT UNSIGNED NOT NULL COMMENT '关联提取主表ID',
    row_index INT COMMENT '行号',
    row_data_json JSON NOT NULL COMMENT '该行的结构化数据',
    logic_flag TINYINT DEFAULT 0 COMMENT '0:正常,1:逻辑警告,2:格式警告',
    logic_msg VARCHAR(200) COMMENT '警告信息',
    row_confidence DECIMAL(4, 3) COMMENT '该行数据的平均置信度',
    is_corrected TINYINT DEFAULT 0 COMMENT '是否经过人工修改',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    CHECK (logic_flag IN (0, 1, 2)),
    CHECK (is_corrected IN (0, 1)),
    CHECK (JSON_VALID(row_data_json)),
    PRIMARY KEY(id),
    KEY idx_file (file_id),
    KEY idx_main (main_id),
    KEY idx_logic (logic_flag),
    KEY idx_row_index (row_index),
    CONSTRAINT fk_detail_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_main FOREIGN KEY (main_id) REFERENCES document_extract_main(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文档表格明细提取表';
-- =================================================================
-- 7. 样本回流训练表 (AI 闭环优化)
-- =================================================================
CREATE TABLE ai_training_sample (
    id BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    file_id BIGINT UNSIGNED NOT NULL COMMENT '文件ID',
    image_region_path VARCHAR(255) COMMENT '切片图路径 (MinIO)',
    coordinate JSON COMMENT '坐标 [x1,y1,x2,y2]',
    field_key VARCHAR(50) COMMENT '字段Key: score, course_name',
    ocr_value VARCHAR(255) COMMENT '原始错误识别值',
    corrected_value VARCHAR(255) COMMENT '人工修正后的真值',
    is_trained TINYINT DEFAULT 0 COMMENT '0:未训练 1:已加入训练集',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    CHECK (is_trained IN (0, 1)),
    CHECK (
        coordinate IS NULL
        OR JSON_VALID(coordinate)
    ),
    PRIMARY KEY(id),
    KEY idx_field (field_key),
    KEY idx_trained (is_trained),
    KEY idx_ai_file (file_id),
    CONSTRAINT fk_training_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI负样本/训练数据表';
-- =================================================================
-- 8. 异步任务与审计日志
-- =================================================================
CREATE TABLE sys_task_log (
    id BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    trace_id VARCHAR(64) NOT NULL COMMENT '链路追踪ID',
    file_id BIGINT UNSIGNED COMMENT '关联文件ID',
    stage VARCHAR(50) COMMENT 'UPLOAD, OCR, NLP, RULE',
    status VARCHAR(20) COMMENT 'SUCCESS, FAIL',
    cost_ms BIGINT COMMENT '耗时(毫秒)',
    error_msg TEXT,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    CHECK (stage IN ('UPLOAD', 'OCR', 'NLP', 'RULE')),
    CHECK (status IN ('SUCCESS', 'FAIL')),
    PRIMARY KEY(id),
    KEY idx_file (file_id),
    KEY idx_trace (trace_id),
    CONSTRAINT fk_tasklog_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE
    SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统任务流水日志';
CREATE TABLE audit_record (
    id BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    file_id BIGINT UNSIGNED NOT NULL COMMENT '文件ID',
    auditor_id BIGINT UNSIGNED NOT NULL COMMENT '审核人ID',
    action_type VARCHAR(20) COMMENT 'APPROVE, REJECT, MODIFY',
    comment VARCHAR(500),
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY(id),
    KEY idx_file (file_id),
    KEY idx_auditor (auditor_id),
    CONSTRAINT fk_audit_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_user FOREIGN KEY (auditor_id) REFERENCES sys_user(id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '人工审核/校对记录';
-- =================================================================
-- 9. 初始化数据 (模版示例)
-- =================================================================
INSERT INTO sys_doc_template (
        id,
        template_name,
        template_code,
        target_kv_config,
        target_table_config,
        status
    )
VALUES (
        1001,
        '通用学生成绩单',
        'TRANSCRIPT',
        '[{"key":"student_name", "label":"姓名"}, {"key":"student_id", "label":"学号"}, {"key":"college", "label":"学院"}]',
        '[{"key":"course", "label":"课程名称"}, {"key":"score", "label":"成绩"}, {"key":"credit", "label":"学分"}, {"key":"type", "label":"课程性质"}]',
        1
    );
SET FOREIGN_KEY_CHECKS = 1;