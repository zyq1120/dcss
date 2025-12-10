# Audit API 说明

## 认证说明
所有接口都位于 `/api/audit` 前缀下，默认需要携带认证后的 Token。统一响应结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

`code` 为 200 表示成功，其他为失败；`message` 是提示信息；`data` 为具体数据。

---

## 1. 提交审核
- **URL**：`POST /api/audit/submit`
- **请求体**：
```json
{
  "fileId": 123456789012345678,
  "auditStatus": 1,
  "auditComment": "可选的审核备注"
}
```
  - `fileId`(Long, 必填)：文件 ID。
  - `auditStatus`(Integer, 必填)：审核状态，业务自定义数值（例如：1=通过，2=驳回）。
  - `auditComment`(String, 可选)：审核意见。
- **响应**：
```json
{
  "code": 200,
  "message": "审核提交成功",
  "data": true
}
```
`data` 为 `true/false` 表示提交是否成功。

---

## 2. 获取审核历史
- **URL**：`GET /api/audit/history/{fileId}`
- **路径参数**：`fileId`(Long, 必填)
- **响应数据结构**（`data` 为数组，每条为 `AuditRecordVO`）：
```json
[
  {
    "id": "785079546327072768",
    "fileId": "785079546327072700",
    "fileName": "毕业证书.pdf",
    "extractMainId": "785079546327072710",
    "auditorId": "785079546327072720",
    "auditorName": "审核员A",
    "auditStatus": 1,
    "auditStatusName": "已通过",
    "auditComment": "符合规范",
    "createTime": "2025-12-06 17:45:50"
  }
]
```
> Long 类型字段均以字符串形式返回，避免前端精度丢失。

---

## 3. 分页查询审核记录
- **URL**：`GET /api/audit/page`
- **查询参数**：
  - `current`(Long, 默认 1)：当前页码。
  - `size`(Long, 默认 10)：每页数量。
  - `auditStatus`(Integer, 可选)：按审核状态筛选。
  - `auditorId`(Long, 可选)：按审核人筛选。
- **响应数据结构**（`PageResult<AuditRecordVO>`）：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 50,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": "785079546327072768",
        "fileId": "785079546327072700",
        "fileName": "毕业证书.pdf",
        "auditStatus": 1,
        "auditStatusName": "已通过",
        "createTime": "2025-12-06 17:45:50"
      }
    ],
    "pages": 5
  }
}
```

---

## 4. 获取文件预览链接
- **URL**：`GET /api/audit/preview/{fileId}`
- **路径参数**：`fileId`(Long, 必填)
- **响应**：
```json
{
  "code": 200,
  "message": "获取预览链接成功",
  "data": "https://minio-domain/document-files/..."
}
```
`data` 为可直接访问的预签名 URL。

---

## 5. 获取 AI 处理结果
- **URL**：`GET /api/audit/result/{fileId}`
- **路径参数**：`fileId`(Long, 必填)
- **响应**：`data` 为 Python AI 服务返回的原始 JSON，结构随模型输出而定，例如：
```json
{
  "code": 200,
  "message": "获取处理结果成功",
  "data": {
    "document_type": "毕业证书/学历证书",
    "fields": { "name": "张三" },
    "confidence_overall": 0.95,
    "fileId": "785079546327072700"
  }
}
```

---

## 6. 审核人员修改字段
- **URL**：`POST /api/audit/modify/{fileId}`
- **路径参数**：`fileId`(Long, 必填)
- **请求体**：任意键值对的 JSON，对应需要覆盖的字段。例如：
```json
{
  "document_type": "成绩单",
  "fields": {
    "name": "李四",
    "major": "软件工程"
  }
}
```
- **响应**：
```json
{
  "code": 200,
  "message": "字段修改成功",
  "data": true
}
```

---

## 统一错误格式
失败时返回：
```json
{
  "code": 500,
  "message": "系统繁忙，请稍后重试"
}
```
或业务自定义错误码与信息（例如权限不足、参数校验失败等）。

