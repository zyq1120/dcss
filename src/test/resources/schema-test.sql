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

CREATE TABLE sys_doc_template (
    id BIGINT PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    template_code VARCHAR(100) NOT NULL UNIQUE,
    target_kv_config TEXT,
    target_table_config TEXT,
    rule_config TEXT,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    email VARCHAR(150),
    phone VARCHAR(50),
    avatar VARCHAR(255),
    token_version INT DEFAULT 1,
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    version INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    role_key VARCHAR(100) NOT NULL UNIQUE,
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
);

CREATE TABLE document_file (
    id BIGINT PRIMARY KEY,
    file_name VARCHAR(255),
    file_path VARCHAR(255),
    file_type VARCHAR(50),
    file_size BIGINT,
    upload_user_id BIGINT,
    template_id BIGINT,
    status INT,
    ocr_status INT,
    nlp_status INT,
    audit_status INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE document_ocr_raw (
    id BIGINT PRIMARY KEY,
    file_id BIGINT NOT NULL,
    ocr_result CLOB,
    confidence DOUBLE,
    page_num INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE document_extract_main (
    id BIGINT PRIMARY KEY,
    file_id BIGINT NOT NULL,
    template_id BIGINT,
    extract_result CLOB,
    confidence DOUBLE,
    status INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE document_extract_detail (
    id BIGINT PRIMARY KEY,
    main_id BIGINT NOT NULL,
    field_name VARCHAR(200),
    field_value CLOB,
    field_type VARCHAR(100),
    confidence DOUBLE,
    is_verified INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_training_sample (
    id BIGINT PRIMARY KEY,
    file_id BIGINT NOT NULL,
    template_id BIGINT,
    sample_type INT,
    input_data CLOB,
    output_data CLOB,
    is_used INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_task_log (
    id BIGINT PRIMARY KEY,
    task_name VARCHAR(200),
    task_type VARCHAR(100),
    target_id BIGINT,
    status INT,
    error_message CLOB,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_record (
    id BIGINT PRIMARY KEY,
    file_id BIGINT NOT NULL,
    extract_main_id BIGINT,
    auditor_id BIGINT NOT NULL,
    audit_status INT,
    audit_comment VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
