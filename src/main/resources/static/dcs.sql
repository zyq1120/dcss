/*
 * Database initialization script for the document classification system.
 * MySQL 8.0+
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Drop in dependency order
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

-- Templates
CREATE TABLE sys_doc_template (
    id BIGINT UNSIGNED NOT NULL COMMENT 'template id (snowflake)',
    template_name VARCHAR(50) NOT NULL COMMENT 'template name',
    template_code VARCHAR(50) NOT NULL UNIQUE COMMENT 'code e.g. TRANSCRIPT',
    target_kv_config JSON COMMENT 'KV extraction rules',
    target_table_config JSON COMMENT 'table extraction rules',
    rule_config JSON COMMENT 'validation rules',
    status TINYINT DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CHECK (status IN (0, 1)),
    CHECK (target_kv_config IS NULL OR JSON_VALID(target_kv_config)),
    CHECK (target_table_config IS NULL OR JSON_VALID(target_table_config)),
    CHECK (rule_config IS NULL OR JSON_VALID(rule_config)),
    PRIMARY KEY(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Document template config';

-- Users and roles
CREATE TABLE sys_user (
    id BIGINT UNSIGNED NOT NULL COMMENT 'user id (snowflake)',
    username VARCHAR(50) NOT NULL COMMENT 'login name',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt password',
    nickname VARCHAR(50),
    email VARCHAR(150),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    token_version INT DEFAULT 1 COMMENT 'token version',
    status TINYINT DEFAULT 1 COMMENT '1 enabled 0 disabled',
    deleted TINYINT DEFAULT 0 COMMENT 'logical delete',
    version INT DEFAULT 0 COMMENT 'optimistic lock',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    update_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CHECK (status IN (0, 1)),
    CHECK (deleted IN (0, 1)),
    PRIMARY KEY(id),
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone),
    KEY idx_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Users';

CREATE TABLE sys_role (
    id BIGINT UNSIGNED NOT NULL COMMENT 'role id (snowflake)',
    role_name VARCHAR(50) NOT NULL COMMENT 'role name',
    role_key VARCHAR(100) NOT NULL COMMENT 'role key e.g. ROLE_ADMIN',
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    CHECK (status IN (0, 1)),
    CHECK (deleted IN (0, 1)),
    PRIMARY KEY(id),
    UNIQUE KEY uk_role_key(role_key),
    UNIQUE KEY uk_role_name(role_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Roles';

CREATE TABLE sys_user_role (
    id BIGINT UNSIGNED NOT NULL COMMENT 'pk (snowflake)',
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'user id',
    role_id BIGINT UNSIGNED NOT NULL COMMENT 'role id',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY(id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_role (user_id, role_id),
    KEY idx_role_user (role_id, user_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'User-role mapping';

-- Files
CREATE TABLE document_file (
    id BIGINT UNSIGNED NOT NULL COMMENT 'pk',
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'uploader id',
    template_id BIGINT UNSIGNED COMMENT 'template id',
    batch_no VARCHAR(50) COMMENT 'batch no',
    file_no VARCHAR(50) UNIQUE COMMENT 'business number',
    file_name VARCHAR(255),
    file_type VARCHAR(20) COMMENT 'img/pdf',
    file_size BIGINT,
    minio_bucket VARCHAR(64) NOT NULL,
    minio_object VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255),
    process_status TINYINT DEFAULT 0 COMMENT '0 pending 1 queued 2 processing 3 manual 4 archived 5 failed',
    process_mode VARCHAR(20) DEFAULT 'STANDARD' COMMENT 'STANDARD / LLM_FALLBACK',
    retry_count INT DEFAULT 0 COMMENT 'retry times',
    fail_reason VARCHAR(500),
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
    CONSTRAINT fk_document_template FOREIGN KEY (template_id) REFERENCES sys_doc_template(id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Document files';

-- OCR raw
CREATE TABLE document_ocr_raw (
    id BIGINT UNSIGNED NOT NULL COMMENT 'pk',
    file_id BIGINT UNSIGNED NOT NULL COMMENT 'file id',
    full_text LONGTEXT COMMENT 'plain text result',
    raw_data_json JSON COMMENT 'ocr result with coordinates',
    text_count INT GENERATED ALWAYS AS (JSON_LENGTH(raw_data_json)) VIRTUAL COMMENT 'block count',
    avg_confidence DECIMAL(5, 4) COMMENT 'average confidence',
    is_llm_used TINYINT DEFAULT 0 COMMENT '1 if LLM used',
    token_usage INT DEFAULT 0 COMMENT 'LLM token usage',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    CHECK (is_llm_used IN (0, 1)),
    CHECK (raw_data_json IS NULL OR JSON_VALID(raw_data_json)),
    PRIMARY KEY(id),
    UNIQUE KEY uk_file (file_id),
    KEY idx_file (file_id),
    KEY idx_text_len(text_count),
    KEY idx_avg_confidence (avg_confidence),
    CONSTRAINT fk_ocr_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'OCR raw result';

-- Extract main (KV)
CREATE TABLE document_extract_main (
    id BIGINT UNSIGNED NOT NULL COMMENT 'pk',
    file_id BIGINT UNSIGNED NOT NULL COMMENT 'file id',
    template_id BIGINT UNSIGNED COMMENT 'template id',
    owner_id VARCHAR(50) COMMENT 'owner id (student_id/employee_id)',
    owner_name VARCHAR(50) COMMENT 'owner name',
    kv_data_json JSON COMMENT 'kv data (key-value pairs)',
    extract_result TEXT COMMENT 'complete extraction result JSON',
    confidence DECIMAL(5, 4) COMMENT 'extraction confidence 0-1',
    status TINYINT DEFAULT 0 COMMENT '0-pending review, 1-reviewed, 2-confirmed',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (kv_data_json IS NULL OR JSON_VALID(kv_data_json)),
    CHECK (status IN (0, 1, 2)),
    PRIMARY KEY(id),
    UNIQUE KEY uk_file_main (file_id),
    KEY idx_owner_id (owner_id),
    KEY idx_owner_name (owner_name),
    KEY idx_status (status),
    CONSTRAINT fk_extract_main_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE CASCADE,
    CONSTRAINT fk_extract_main_template FOREIGN KEY (template_id) REFERENCES sys_doc_template(id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Extracted KV data';

-- Extract detail (table rows)
CREATE TABLE document_extract_detail (
    id BIGINT UNSIGNED NOT NULL COMMENT 'pk',
    file_id BIGINT UNSIGNED NOT NULL COMMENT 'file id',
    main_id BIGINT UNSIGNED NOT NULL COMMENT 'extract main id',
    row_index INT COMMENT 'row index (0-based)',
    row_data_json JSON NOT NULL COMMENT 'structured row data (complete row)',
    field_name VARCHAR(100) COMMENT 'field name (for single field extraction)',
    field_value TEXT COMMENT 'field value (for single field extraction)',
    row_confidence DECIMAL(5, 4) COMMENT 'row confidence 0-1',
    is_verified TINYINT DEFAULT 0 COMMENT '0-not verified, 1-verified by human',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (is_verified IN (0, 1)),
    CHECK (JSON_VALID(row_data_json)),
    PRIMARY KEY(id),
    KEY idx_file (file_id),
    KEY idx_main (main_id),
    KEY idx_row_index (row_index),
    KEY idx_verified (is_verified),
    CONSTRAINT fk_detail_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_main FOREIGN KEY (main_id) REFERENCES document_extract_main(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Extracted table detail';

-- Training samples
CREATE TABLE ai_training_sample (
    id BIGINT UNSIGNED NOT NULL COMMENT 'pk',
    file_id BIGINT UNSIGNED NOT NULL COMMENT 'file id',
    image_region_path VARCHAR(255) COMMENT 'cropped image path',
    coordinate JSON COMMENT 'bounding box [x1,y1,x2,y2]',
    field_key VARCHAR(50) COMMENT 'field key',
    ocr_value VARCHAR(255) COMMENT 'original value',
    corrected_value VARCHAR(255) COMMENT 'corrected value',
    is_trained TINYINT DEFAULT 0 COMMENT '0 not trained, 1 trained',
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    CHECK (is_trained IN (0, 1)),
    CHECK (coordinate IS NULL OR JSON_VALID(coordinate)),
    PRIMARY KEY(id),
    KEY idx_field (field_key),
    KEY idx_trained (is_trained),
    KEY idx_ai_file (file_id),
    CONSTRAINT fk_training_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI training samples';

-- Task log
CREATE TABLE sys_task_log (
    id BIGINT UNSIGNED NOT NULL COMMENT 'pk',
    trace_id VARCHAR(64) NOT NULL COMMENT 'trace id',
    file_id BIGINT UNSIGNED COMMENT 'file id',
    stage VARCHAR(50) COMMENT 'UPLOAD, OCR, NLP, RULE',
    status VARCHAR(20) COMMENT 'SUCCESS, FAIL',
    cost_ms BIGINT COMMENT 'cost in ms',
    error_msg TEXT,
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    CHECK (stage IN ('UPLOAD', 'OCR', 'NLP', 'RULE')),
    CHECK (status IN ('SUCCESS', 'FAIL')),
    PRIMARY KEY(id),
    KEY idx_file (file_id),
    KEY idx_trace (trace_id),
    CONSTRAINT fk_tasklog_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Task log';

-- Audit record
CREATE TABLE audit_record (
    id BIGINT UNSIGNED NOT NULL COMMENT 'pk',
    file_id BIGINT UNSIGNED NOT NULL COMMENT 'file id',
    auditor_id BIGINT UNSIGNED NOT NULL COMMENT 'auditor user id',
    action_type VARCHAR(20) COMMENT 'APPROVE, REJECT, MODIFY',
    comment VARCHAR(500),
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY(id),
    KEY idx_file (file_id),
    KEY idx_auditor (auditor_id),
    CONSTRAINT fk_audit_file FOREIGN KEY (file_id) REFERENCES document_file(id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_user FOREIGN KEY (auditor_id) REFERENCES sys_user(id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Audit records';

-- Seed data
INSERT INTO sys_doc_template (
    id,
    template_name,
    template_code,
    target_kv_config,
    target_table_config,
    status
) VALUES (
    1001,
    'Student Transcript',
    'TRANSCRIPT',
    '[{"key":"student_name","label":"姓名"},{"key":"student_id","label":"学号"},{"key":"college","label":"学院"}]',
    '[{"key":"course","label":"课程名称"},{"key":"score","label":"成绩"},{"key":"credit","label":"学分"},{"key":"type","label":"课程性质"}]',
    1
);

SET FOREIGN_KEY_CHECKS = 1;
