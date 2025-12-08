-- ========================================
-- 智能文档处理系统 - 索引验证脚本
-- 用途: 验证所有索引是否正确创建
-- 执行时机: 执行add_indexes.sql之后
-- 作者: zyq
-- 日期: 2025-12-07
-- ========================================

USE dcs;

SELECT '========================================' AS '';
SELECT '开始验证索引...' AS '';
SELECT '========================================' AS '';

-- ========================================
-- 1. 验证document_file表的索引
-- ========================================
SELECT '1. 检查 document_file 表索引' AS '';
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
  AND TABLE_NAME = 'document_file'
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY INDEX_NAME;

-- ========================================
-- 2. 验证document_extract_main表的索引
-- ========================================
SELECT '2. 检查 document_extract_main 表索引' AS '';
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
  AND TABLE_NAME = 'document_extract_main'
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY INDEX_NAME;

-- ========================================
-- 3. 验证document_extract_detail表的索引
-- ========================================
SELECT '3. 检查 document_extract_detail 表索引' AS '';
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
  AND TABLE_NAME = 'document_extract_detail'
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY INDEX_NAME;

-- ========================================
-- 4. 验证audit_record表的索引
-- ========================================
SELECT '4. 检查 audit_record 表索引' AS '';
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
  AND TABLE_NAME = 'audit_record'
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY INDEX_NAME;

-- ========================================
-- 5. 验证sys_user表的索引
-- ========================================
SELECT '5. 检查 sys_user 表索引' AS '';
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
  AND TABLE_NAME = 'sys_user'
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY INDEX_NAME;

-- ========================================
-- 6. 验证sys_doc_template表的索引
-- ========================================
SELECT '6. 检查 sys_doc_template 表索引' AS '';
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
  AND TABLE_NAME = 'sys_doc_template'
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY INDEX_NAME;

-- ========================================
-- 7. 验证sys_task_log表的索引
-- ========================================
SELECT '7. 检查 sys_task_log 表索引' AS '';
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
  AND TABLE_NAME = 'sys_task_log'
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY INDEX_NAME;

-- ========================================
-- 8. 验证document_ocr_raw表的索引
-- ========================================
SELECT '8. 检查 document_ocr_raw 表索引' AS '';
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
  AND TABLE_NAME = 'document_ocr_raw'
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY INDEX_NAME;

-- ========================================
-- 9. 验证ai_training_sample表的索引
-- ========================================
SELECT '9. 检查 ai_training_sample 表索引' AS '';
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
  AND TABLE_NAME = 'ai_training_sample'
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY INDEX_NAME;

-- ========================================
-- 10. 汇总统计
-- ========================================
SELECT '========================================' AS '';
SELECT '索引统计汇总' AS '';
SELECT '========================================' AS '';

SELECT
    TABLE_NAME AS '表名',
    COUNT(DISTINCT INDEX_NAME) AS '索引数量'
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dcs'
GROUP BY TABLE_NAME
ORDER BY TABLE_NAME;

-- ========================================
-- 11. 检查是否有重复索引
-- ========================================
SELECT '========================================' AS '';
SELECT '检查潜在的重复索引' AS '';
SELECT '========================================' AS '';

SELECT
    a.TABLE_NAME,
    a.INDEX_NAME AS '索引1',
    b.INDEX_NAME AS '索引2',
    GROUP_CONCAT(a.COLUMN_NAME ORDER BY a.SEQ_IN_INDEX) AS '列'
FROM information_schema.STATISTICS a
JOIN information_schema.STATISTICS b
    ON a.TABLE_SCHEMA = b.TABLE_SCHEMA
    AND a.TABLE_NAME = b.TABLE_NAME
    AND a.COLUMN_NAME = b.COLUMN_NAME
    AND a.INDEX_NAME < b.INDEX_NAME
WHERE a.TABLE_SCHEMA = 'dcs'
GROUP BY a.TABLE_NAME, a.INDEX_NAME, b.INDEX_NAME
HAVING COUNT(*) > 0;

-- ========================================
-- 12. 验证完成
-- ========================================
SELECT '========================================' AS '';
SELECT '索引验证完成！' AS '';
SELECT '请检查上述输出，确保所有索引都已正确创建。' AS '';
SELECT '========================================' AS '';

