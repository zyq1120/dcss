# 数据库优化脚本使用说明

## 📁 文件说明

### 1. `add_indexes.sql` - 索引优化脚本
**用途**: 为系统添加性能优化索引  
**执行时机**: 在执行完`dcs.sql`后执行  
**索引数量**: 约30+个优化索引

### 2. `verify_indexes.sql` - 索引验证脚本
**用途**: 验证索引是否正确创建  
**执行时机**: 在执行完`add_indexes.sql`后执行  

---

## 🚀 快速开始

### 方式一：使用命令行

```bash
# 1. 初始化数据库（如果还没执行）
mysql -u dcs_user -p dcs < ../../static/dcs.sql

# 2. 添加优化索引
mysql -u dcs_user -p dcs < add_indexes.sql

# 3. 验证索引
mysql -u dcs_user -p dcs < verify_indexes.sql
```

### 方式二：使用MySQL客户端

```sql
-- 1. 连接数据库
USE dcs;

-- 2. 执行索引优化脚本
SOURCE /path/to/add_indexes.sql;

-- 3. 验证索引
SOURCE /path/to/verify_indexes.sql;
```

---

## 📊 索引详细说明

### document_file 表（6个索引）

**来自dcs.sql的索引**:
- `idx_user` - 用户ID索引
- `idx_status` - 状态索引
- `idx_batch` - 批次号索引
- `idx_template` - 模板ID索引
- `idx_create_time` - 创建时间索引

**新增的优化索引**:
- `idx_process_status_deleted_time` - 复合索引（状态+删除+时间）
- `idx_user_deleted_time` - 复合索引（用户+删除+时间）
- `idx_file_name` - 文件名搜索索引
- `idx_deleted` - 删除标记索引

**优化场景**:
```sql
-- 待审核列表查询（使用 idx_process_status_deleted_time）
SELECT * FROM document_file 
WHERE process_status = 3 AND deleted = 0 
ORDER BY create_time DESC;

-- 用户文件查询（使用 idx_user_deleted_time）
SELECT * FROM document_file 
WHERE user_id = ? AND deleted = 0 
ORDER BY create_time DESC;

-- 文件名搜索（使用 idx_file_name）
SELECT * FROM document_file 
WHERE file_name LIKE '%成绩单%';
```

---

### document_extract_main 表（4个索引）

**来自dcs.sql的索引**:
- `uk_file_main` - 唯一索引（文件ID）
- `idx_owner_id` - 拥有者ID索引
- `idx_owner_name` - 拥有者姓名索引
- `idx_status` - 状态索引

**新增的优化索引**:
- `idx_confidence` - 置信度索引
- `idx_extract_create_time` - 创建时间索引
- `idx_file_create` - 复合索引（文件ID+时间）
- `idx_extract_template` - 模板ID索引

**优化场景**:
```sql
-- 查询低置信度文件（使用 idx_confidence）
SELECT * FROM document_extract_main 
WHERE confidence <= 0.85;

-- 获取最新提取结果（使用 idx_file_create）
SELECT * FROM document_extract_main 
WHERE file_id = ? 
ORDER BY create_time DESC LIMIT 1;
```

---

### document_extract_detail 表（3个索引）

**来自dcs.sql的索引**:
- `idx_file` - 文件ID索引
- `idx_main` - 主表ID索引
- `idx_row_index` - 行索引
- `idx_verified` - 验证标记索引

**新增的优化索引**:
- `idx_file_main_row` - 复合索引（文件+主表+行）
- `idx_field_name` - 字段名索引
- `idx_detail_create_time` - 创建时间索引

**优化场景**:
```sql
-- 精确查询某行数据（使用 idx_file_main_row）
SELECT * FROM document_extract_detail 
WHERE file_id = ? AND main_id = ? AND row_index = ?;

-- 查询特定字段（使用 idx_field_name）
SELECT * FROM document_extract_detail 
WHERE field_name = 'student_name';
```

---

### audit_record 表（4个索引）

**来自dcs.sql的索引**:
- `idx_file` - 文件ID索引
- `idx_auditor` - 审核人索引

**新增的优化索引**:
- `idx_audit_file_time` - 复合索引（文件ID+时间）
- `idx_auditor_time` - 复合索引（审核人+时间）
- `idx_action_type` - 操作类型索引
- `idx_audit_create_time` - 创建时间索引

**优化场景**:
```sql
-- 查询审核历史（使用 idx_audit_file_time）
SELECT * FROM audit_record 
WHERE file_id = ? 
ORDER BY create_time DESC;

-- 查询审核人操作记录（使用 idx_auditor_time）
SELECT * FROM audit_record 
WHERE auditor_id = ? 
ORDER BY create_time DESC;
```

---

### sys_user 表（2个索引）

**来自dcs.sql的索引**:
- `uk_username` - 唯一索引（用户名）
- `idx_phone` - 手机号索引
- `idx_email` - 邮箱索引

**新增的优化索引**:
- `idx_user_status_deleted` - 复合索引（状态+删除）
- `idx_token_version` - Token版本索引

**优化场景**:
```sql
-- 查询有效用户（使用 idx_user_status_deleted）
SELECT * FROM sys_user 
WHERE status = 1 AND deleted = 0;

-- Token版本检查（使用 idx_token_version）
SELECT * FROM sys_user 
WHERE id = ? AND token_version = ?;
```

---

### sys_doc_template 表（3个索引）

**来自dcs.sql的索引**:
- `uk_template_code` - 唯一索引（模板编码）

**新增的优化索引**:
- `idx_template_name` - 模板名称索引
- `idx_template_status` - 状态索引
- `idx_template_create_time` - 创建时间索引

---

### sys_task_log 表（4个索引）

**来自dcs.sql的索引**:
- `idx_file` - 文件ID索引
- `idx_trace` - 追踪ID索引

**新增的优化索引**:
- `idx_stage` - 阶段索引
- `idx_log_status` - 状态索引
- `idx_log_create_time` - 创建时间索引
- `idx_stage_status_time` - 复合索引（阶段+状态+时间）

**优化场景**:
```sql
-- 查询失败的OCR任务（使用 idx_stage_status_time）
SELECT * FROM sys_task_log 
WHERE stage = 'OCR' AND status = 'FAIL' 
ORDER BY create_time DESC;
```

---

### document_ocr_raw 表（2个索引）

**来自dcs.sql的索引**:
- `uk_file` - 唯一索引（文件ID）
- `idx_file` - 文件ID索引
- `idx_text_len` - 文本长度索引
- `idx_avg_confidence` - 平均置信度索引

**新增的优化索引**:
- `idx_llm_used` - LLM使用标记索引
- `idx_ocr_create_time` - 创建时间索引

---

### ai_training_sample 表（2个索引）

**来自dcs.sql的索引**:
- `idx_field` - 字段索引
- `idx_trained` - 训练标记索引
- `idx_ai_file` - 文件ID索引

**新增的优化索引**:
- `idx_field_trained` - 复合索引（字段+训练状态）
- `idx_sample_create_time` - 创建时间索引

---

## ⚡ 性能提升预期

| 操作类型 | 优化前 | 优化后 | 提升 |
|---------|--------|--------|------|
| 待审核列表查询 | ~1000ms | ~100ms | 10倍 |
| 文件详情查询 | ~800ms | ~80ms | 10倍 |
| 审核历史查询 | ~500ms | ~50ms | 10倍 |
| 用户文件列表 | ~600ms | ~60ms | 10倍 |

---

## 🔍 性能监控

### 查看索引使用情况

```sql
-- 查看某个表的所有索引
SHOW INDEX FROM document_file;

-- 查看索引统计信息
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    CARDINALITY
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
  AND TABLE_NAME = 'document_file';
```

### 分析查询性能

```sql
-- 使用EXPLAIN分析查询
EXPLAIN SELECT * FROM document_file 
WHERE process_status = 3 AND deleted = 0;

-- 查看是否使用了索引
-- type列显示的值越靠前越好：
-- system > const > eq_ref > ref > range > index > ALL
```

### 启用慢查询日志

```sql
-- 查看慢查询配置
SHOW VARIABLES LIKE 'slow_query%';

-- 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;  -- 记录超过1秒的查询

-- 查看慢查询日志文件位置
SHOW VARIABLES LIKE 'slow_query_log_file';
```

---

## 🛠️ 维护建议

### 定期维护（每月）

```sql
-- 优化表（重建索引，回收空间）
OPTIMIZE TABLE document_file;
OPTIMIZE TABLE document_extract_main;
OPTIMIZE TABLE document_extract_detail;

-- 分析表（更新统计信息）
ANALYZE TABLE document_file;
ANALYZE TABLE document_extract_main;
ANALYZE TABLE document_extract_detail;
```

### 检查表健康状态

```sql
-- 检查表是否有错误
CHECK TABLE document_file;
CHECK TABLE document_extract_main;

-- 如果发现错误，执行修复
REPAIR TABLE document_file;
```

---

## ⚠️ 注意事项

1. **索引空间占用**: 索引会占用约10-20%的额外存储空间
2. **写入性能**: 索引会略微降低INSERT/UPDATE/DELETE性能（通常可忽略）
3. **索引维护**: 定期执行ANALYZE TABLE更新统计信息
4. **避免冗余**: 不要创建重复或冗余的索引
5. **监控慢查询**: 持续监控慢查询日志，优化索引策略

---

## 🔄 索引回滚

如果需要删除某个索引：

```sql
-- 删除单个索引
DROP INDEX idx_process_status_deleted_time ON document_file;

-- 删除所有新增索引（谨慎操作！）
DROP INDEX idx_process_status_deleted_time ON document_file;
DROP INDEX idx_user_deleted_time ON document_file;
DROP INDEX idx_file_name ON document_file;
DROP INDEX idx_deleted ON document_file;
-- ... 依此类推
```

---

## 📞 技术支持

如有问题，请参考：
- [部署指南.md](../../../部署指南.md)
- [API文档.md](../../../API文档.md)

---

**创建时间**: 2025-12-07  
**作者**: zyq  
**版本**: v1.0

