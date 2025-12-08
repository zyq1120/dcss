-- ========================================
-- 智能文档处理系统 - 数据库索引优化脚本
-- 用途: 优化查询性能，提升系统响应速度
-- 执行时机: 在现有数据库上执行（dcs.sql执行后）
-- 作者: zyq
-- 日期: 2025-12-07
-- 注意: 此脚本会检查并添加dcs.sql中未定义的索引
-- ========================================

USE dcs;

-- ========================================
-- 1. document_file 表索引优化
-- dcs.sql中已有: idx_user, idx_status, idx_batch, idx_template, idx_create_time
-- 需要新增的索引:
-- ========================================

-- 复合索引：状态+删除标记+创建时间（用于待审核列表查询）
-- 这个索引比单独的idx_status更高效
CREATE INDEX IF NOT EXISTS idx_process_status_deleted_time
ON document_file(process_status, deleted, create_time DESC);

-- 复合索引：用户+删除标记+创建时间（用于用户文件查询）
CREATE INDEX IF NOT EXISTS idx_user_deleted_time
ON document_file(user_id, deleted, create_time DESC);

-- 文件名搜索索引（用于模糊搜索）
CREATE INDEX IF NOT EXISTS idx_file_name ON document_file(file_name);

-- 删除标记索引（用于过滤已删除记录）
CREATE INDEX IF NOT EXISTS idx_deleted ON document_file(deleted);

-- ========================================
-- 2. document_extract_main 表索引优化
-- dcs.sql中已有: uk_file_main(UNIQUE), idx_owner_id, idx_owner_name, idx_status
-- 需要新增的索引:
-- ========================================

-- 置信度范围查询索引（用于筛选低置信度数据）
CREATE INDEX IF NOT EXISTS idx_confidence ON document_extract_main(confidence);

-- 创建时间查询索引（用于按时间排序）
CREATE INDEX IF NOT EXISTS idx_extract_create_time ON document_extract_main(create_time DESC);

-- 复合索引：文件ID+创建时间（用于查询最新的提取结果）
-- 注意：dcs.sql中已有uk_file_main唯一索引，此索引用于排序优化
CREATE INDEX IF NOT EXISTS idx_file_create
ON document_extract_main(file_id, create_time DESC);

-- 模板ID索引（用于按模板查询）
CREATE INDEX IF NOT EXISTS idx_extract_template ON document_extract_main(template_id);

-- ========================================
-- 3. document_extract_detail 表索引优化
-- dcs.sql中已有: idx_file, idx_main, idx_row_index, idx_verified
-- 需要新增的索引:
-- ========================================

-- 复合索引：文件ID+主表ID+行索引（用于精确查询）
CREATE INDEX IF NOT EXISTS idx_file_main_row
ON document_extract_detail(file_id, main_id, row_index);

-- 字段名索引（用于字段级查询）
CREATE INDEX IF NOT EXISTS idx_field_name ON document_extract_detail(field_name);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_detail_create_time ON document_extract_detail(create_time DESC);

-- ========================================
-- 4. audit_record 表索引优化
-- dcs.sql中已有: idx_file, idx_auditor
-- 需要新增的索引:
-- ========================================

-- 复合索引：文件ID+创建时间（用于查询审核历史）
CREATE INDEX IF NOT EXISTS idx_audit_file_time
ON audit_record(file_id, create_time DESC);

-- 复合索引：审核人+创建时间（用于查询审核人的操作记录）
CREATE INDEX IF NOT EXISTS idx_auditor_time
ON audit_record(auditor_id, create_time DESC);

-- 操作类型索引（用于按操作类型筛选）
CREATE INDEX IF NOT EXISTS idx_action_type ON audit_record(action_type);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_audit_create_time ON audit_record(create_time DESC);

-- ========================================
-- 5. sys_user 表索引优化
-- dcs.sql中已有: uk_username(UNIQUE), idx_phone, idx_email
-- 需要新增的索引:
-- ========================================

-- 复合索引：状态+删除标记（用于查询有效用户）
CREATE INDEX IF NOT EXISTS idx_user_status_deleted ON sys_user(status, deleted);

-- Token版本索引（用于Token失效检查）
CREATE INDEX IF NOT EXISTS idx_token_version ON sys_user(token_version);

-- ========================================
-- 6. sys_user_role 表索引优化
-- dcs.sql中已有: uk_user_role(UNIQUE), idx_user_role, idx_role_user
-- 此表索引已经足够，无需新增
-- ========================================

-- ========================================
-- 7. sys_doc_template 表索引优化
-- dcs.sql中已有: 无索引（除了主键和UNIQUE KEY uk_template_code）
-- 需要新增的索引:
-- ========================================

-- 模板名称索引（用于搜索）
CREATE INDEX IF NOT EXISTS idx_template_name ON sys_doc_template(template_name);

-- 状态索引（用于查询启用的模板）
CREATE INDEX IF NOT EXISTS idx_template_status ON sys_doc_template(status);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_template_create_time ON sys_doc_template(create_time DESC);

-- ========================================
-- 8. sys_task_log 表索引优化
-- dcs.sql中已有: idx_file, idx_trace
-- 需要新增的索引:
-- ========================================

-- 阶段索引（用于按阶段查询）
CREATE INDEX IF NOT EXISTS idx_stage ON sys_task_log(stage);

-- 状态索引（用于查询失败任务）
CREATE INDEX IF NOT EXISTS idx_log_status ON sys_task_log(status);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_log_create_time ON sys_task_log(create_time DESC);

-- 复合索引：阶段+状态+创建时间（用于综合查询）
CREATE INDEX IF NOT EXISTS idx_stage_status_time
ON sys_task_log(stage, status, create_time DESC);

-- ========================================
-- 9. document_ocr_raw 表索引优化
-- dcs.sql中已有: uk_file(UNIQUE), idx_file, idx_text_len, idx_avg_confidence
-- 需要新增的索引:
-- ========================================

-- LLM使用标记索引（用于统计LLM使用情况）
CREATE INDEX IF NOT EXISTS idx_llm_used ON document_ocr_raw(is_llm_used);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_ocr_create_time ON document_ocr_raw(create_time DESC);

-- ========================================
-- 10. ai_training_sample 表索引优化
-- dcs.sql中已有: idx_field, idx_trained, idx_ai_file
-- 需要新增的索引:
-- ========================================

-- 复合索引：字段+训练状态（用于查询待训练样本）
CREATE INDEX IF NOT EXISTS idx_field_trained
ON ai_training_sample(field_key, is_trained);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_sample_create_time ON ai_training_sample(create_time DESC);

-- ========================================
-- 索引创建完成提示
-- ========================================

SELECT 'Database indexes optimization completed successfully!' AS status;
SELECT 'Total new indexes added: approximately 30+' AS info;

-- ========================================
-- 索引使用说明
-- ========================================

-- 1. 待审核任务列表查询优化
-- SELECT * FROM document_file
-- WHERE process_status = 3 AND deleted = 0
-- ORDER BY create_time DESC;
-- 使用索引: idx_process_status_deleted_time

-- 2. 文件详情查询优化（获取最新提取结果）
-- SELECT * FROM document_extract_main
-- WHERE file_id = ?
-- ORDER BY create_time DESC LIMIT 1;
-- 使用索引: idx_file_create (如果file_id有多条记录) 或 uk_file_main (UNIQUE索引)

-- 3. 审核历史查询优化
-- SELECT * FROM audit_record
-- WHERE file_id = ?
-- ORDER BY create_time DESC;
-- 使用索引: idx_audit_file_time

-- 4. 用户登录查询优化
-- SELECT * FROM sys_user
-- WHERE username = ? AND deleted = 0;
-- 使用索引: uk_username (UNIQUE索引)

-- 5. 查询低置信度文件（需要人工审核）
-- SELECT f.*, e.confidence
-- FROM document_file f
-- JOIN document_extract_main e ON f.id = e.file_id
-- WHERE e.confidence <= 0.85 AND f.process_status = 3;
-- 使用索引: idx_confidence, idx_process_status_deleted_time

-- 6. 查询用户的所有文件
-- SELECT * FROM document_file
-- WHERE user_id = ? AND deleted = 0
-- ORDER BY create_time DESC;
-- 使用索引: idx_user_deleted_time

-- 7. 按模板查询文件
-- SELECT * FROM document_file
-- WHERE template_id = ?;
-- 使用索引: idx_template (来自dcs.sql)

-- 8. 查询失败的任务日志
-- SELECT * FROM sys_task_log
-- WHERE status = 'FAIL' AND stage = 'OCR'
-- ORDER BY create_time DESC;
-- 使用索引: idx_stage_status_time

-- ========================================
-- 性能监控建议
-- ========================================

-- 查看所有表的索引使用情况
SHOW INDEX FROM document_file;
SHOW INDEX FROM document_extract_main;
SHOW INDEX FROM document_extract_detail;
SHOW INDEX FROM audit_record;
SHOW INDEX FROM sys_user;
SHOW INDEX FROM sys_doc_template;
SHOW INDEX FROM sys_task_log;

-- 分析查询性能（使用EXPLAIN）
EXPLAIN SELECT * FROM document_file WHERE process_status = 3 AND deleted = 0;
EXPLAIN SELECT * FROM document_extract_main WHERE file_id = 1 ORDER BY create_time DESC;
EXPLAIN SELECT * FROM audit_record WHERE file_id = 1 ORDER BY create_time DESC;

-- 查看表状态和数据分布
SHOW TABLE STATUS LIKE 'document_file';
SHOW TABLE STATUS LIKE 'document_extract_main';

-- 查看索引统计信息
SELECT
    TABLE_NAME,
    INDEX_NAME,
    SEQ_IN_INDEX,
    COLUMN_NAME,
    CARDINALITY,
    INDEX_TYPE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- ========================================
-- 注意事项
-- ========================================

-- 1. 索引会占用额外的存储空间（约10-20%）
-- 2. 索引会略微降低INSERT/UPDATE/DELETE的性能（通常可忽略）
-- 3. 定期使用ANALYZE TABLE更新索引统计信息
-- 4. 监控慢查询日志，持续优化索引策略
-- 5. 避免创建重复或冗余的索引
-- 6. 对于选择性低的字段（如status只有几个值），索引效果有限

-- ========================================
-- 维护命令
-- ========================================

-- 优化表（重建索引，回收空间）
OPTIMIZE TABLE document_file;
OPTIMIZE TABLE document_extract_main;
OPTIMIZE TABLE document_extract_detail;
OPTIMIZE TABLE audit_record;
OPTIMIZE TABLE sys_user;
OPTIMIZE TABLE sys_task_log;

-- 分析表（更新统计信息，帮助优化器选择最佳执行计划）
ANALYZE TABLE document_file;
ANALYZE TABLE document_extract_main;
ANALYZE TABLE document_extract_detail;
ANALYZE TABLE audit_record;
ANALYZE TABLE sys_user;
ANALYZE TABLE sys_task_log;

-- 检查表（检查表是否有错误）
CHECK TABLE document_file;
CHECK TABLE document_extract_main;

-- 修复表（如果发现错误）
REPAIR TABLE document_file;

-- 查看慢查询日志配置
SHOW VARIABLES LIKE 'slow_query%';
SHOW VARIABLES LIKE 'long_query_time';

-- 启用慢查询日志（生产环境建议）
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;  -- 记录超过1秒的查询

-- ========================================
-- 索引删除命令（如需回滚）
-- ========================================

-- 如果某个索引不再需要，可以使用以下命令删除
-- DROP INDEX idx_process_status_deleted_time ON document_file;
-- DROP INDEX idx_user_deleted_time ON document_file;
-- 等等...

-- ========================================
-- 执行完成
-- ========================================

