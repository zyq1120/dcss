-- 添加文档提取扩展字段
-- V2: 支持Python AI返回的完整文档信息存储
use dcs;
-- 添加文档类型字段
ALTER TABLE document_extract_main
ADD COLUMN document_type VARCHAR(50) COMMENT '文档类型（如：transcript, certificate, leave_application等）' AFTER status;

-- 添加文档URL字段
ALTER TABLE document_extract_main
ADD COLUMN document_url VARCHAR(500) COMMENT '文档访问URL（MinIO预签名URL）' AFTER document_type;

-- 添加原始文本字段
ALTER TABLE document_extract_main
ADD COLUMN raw_text LONGTEXT COMMENT 'OCR识别的原始文本内容' AFTER document_url;

-- 添加基本信息JSON字段
ALTER TABLE document_extract_main
ADD COLUMN basic_info_json JSON COMMENT '基本信息JSON（姓名、学号、学校等）' AFTER raw_text;

-- 添加学业信息JSON字段
ALTER TABLE document_extract_main
ADD COLUMN academic_info_json JSON COMMENT '学业信息JSON（学位、入学日期等）' AFTER basic_info_json;

-- 添加证书信息JSON字段
ALTER TABLE document_extract_main
ADD COLUMN certificate_info_json JSON COMMENT '证书信息JSON（证书编号、颁发日期等）' AFTER academic_info_json;

-- 添加财务信息JSON字段
ALTER TABLE document_extract_main
ADD COLUMN financial_info_json JSON COMMENT '财务信息JSON（银行账户、贷款金额等）' AFTER certificate_info_json;

-- 添加请假信息JSON字段
ALTER TABLE document_extract_main
ADD COLUMN leave_info_json JSON COMMENT '请假信息JSON（请假原因、天数等）' AFTER financial_info_json;

-- 添加摘要字段
ALTER TABLE document_extract_main
ADD COLUMN summary TEXT COMMENT '摘要信息' AFTER leave_info_json;

-- 添加表格数据JSON字段
ALTER TABLE document_extract_main
ADD COLUMN tables_json JSON COMMENT '表格数据JSON（课程成绩等表格）' AFTER summary;

-- 添加索引优化查询
CREATE INDEX idx_document_type ON document_extract_main(document_type);
