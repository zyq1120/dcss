# 📡 API接口完整列表（前端开发参考）

> **基础URL**: `http://localhost:8080`  
> **Token格式**: `Authorization: Bearer {token}`  
> **时间格式**: `yyyy-MM-dd HH:mm:ss`

---

## 📋 通用说明

### 成功响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { }
}
```

### 失败响应格式
```json
{
  "code": 400,
  "message": "错误描述",
  "data": null
}
```

### 分页响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  }
}
```

### 错误码
- `200`: 成功
- `400`: 参数错误
- `401`: 未登录/Token过期
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器错误

---

## 1. 🔐 认证模块 (Auth)

| 方法 | 接口 | 说明 | 是否需要Token |
|-----|------|------|--------------|
| POST | /api/auth/login | 用户登录 | ❌ |
| POST | /api/auth/register | 用户注册 | ❌ |
| POST | /api/auth/logout | 用户登出 | ✅ |
| GET | /api/auth/me | 获取当前用户信息 | ✅ |
| GET | /api/auth/userinfo | 获取用户信息 | ✅ |
| POST | /api/auth/refresh | 刷新Token | ✅ |

## 2. 用户管理 (User)

| 方法 | 接口 | 说明 | 是否需要Token |
|-----|------|------|--------------|
| POST | /api/user | 创建用户 | ✅ |
| GET | /api/user/page | 用户列表（分页） | ✅ |
| GET | /api/user/{id} | 获取用户详情 | ✅ |
| PUT | /api/user | 更新用户信息 | ✅ |
| DELETE | /api/user/{id} | 删除用户 | ✅ |
| POST | /api/user/assign-role | 分配角色给用户 | ✅ |

## 3. 角色管理 (Role)

| 方法 | 接口 | 说明 | 是否需要Token |
|-----|------|------|--------------|
| POST | /api/role | 创建角色 | ✅ |
| GET | /api/role/page | 角色列表（分页） | ✅ |
| GET | /api/role/{id} | 获取角色详情 | ✅ |
| PUT | /api/role | 更新角色 | ✅ |
| DELETE | /api/role/{id} | 删除角色 | ✅ |

## 4. 模板管理 (Template)

| 方法 | 接口 | 说明 | 是否需要Token |
|-----|------|------|--------------|
| POST | /api/template | 创建模板 | ✅ |
| GET | /api/template/page | 模板列表（分页） | ✅ |
| GET | /api/template/{id} | 获取模板详情 | ✅ |
| GET | /api/template/code/{code} | 根据编码获取模板 | ✅ |
| PUT | /api/template | 更新模板 | ✅ |
| DELETE | /api/template/{id} | 删除模板 | ✅ |
| PUT | /api/template/{id}/status | 启用/禁用模板 | ✅ |

## 5. 文件管理 (File)

| 方法 | 接口 | 说明 | 是否需要Token |
|-----|------|------|--------------|
| POST | /api/file/upload | 上传文件 | ✅ |
| GET | /api/file/download/{id} | 下载文件 | ✅ |
| GET | /api/file/{id} | 获取文件详情 | ✅ |
| GET | /api/file/page | 文件列表（分页） | ✅ |
| PUT | /api/file/status | 更新文件状态 | ✅ |
| DELETE | /api/file/{id} | 删除文件 | ✅ |

## 6. AI处理 (AI Process)

| 方法 | 接口 | 说明 | 是否需要Token |
|-----|------|------|--------------|
| POST | /api/v1/ai/process | AI智能处理 | ✅ |
| POST | /api/ai/upload-and-process | 上传并处理 | ✅ |
| POST | /api/ai/process-text | 纯文本处理 | ✅ |
| POST | /api/ai/reprocess/{fileId} | 重新处理文件 | ✅ |
| POST | /api/ai/save-verified | 保存校对后的数据 | ✅ |

## 7. 审核管理 (Audit)

| 方法 | 接口 | 说明 | 是否需要Token |
|-----|------|------|--------------|
| POST | /api/audit/submit | 提交审核 | ✅ |
| GET | /api/audit/history/{fileId} | 获取审核历史 | ✅ |
| GET | /api/audit/page | 审核记录列表（分页） | ✅ |
| GET | /api/audit/preview/{fileId} | 获取文件预览URL | ✅ |
| GET | /api/audit/result/{fileId} | 获取AI处理结果 | ✅ |
| POST | /api/audit/modify/{fileId} | 修改字段并保存 | ✅ |

## 8. 校对工作台 (Review)

| 方法 | 接口 | 说明 | 是否需要Token |
|-----|------|------|--------------|
| GET | /api/review/pending | 获取待审核任务列表 | ✅ |
| GET | /api/review/{fileId}/detail | 获取任务详情 | ✅ |
| POST | /api/review/save-draft | 保存字段修改（草稿） | ✅ |
| POST | /api/review/complete | 完成校对 | ✅ |
| GET | /api/review/{fileId}/history | 获取审核历史 | ✅ |
| GET | /api/review/{fileId}/preview-url | 获取文件预览URL | ✅ |
| POST | /api/review/batch-complete | 批量完成校对 | ✅ |

## 9. 数据驾驶舱 (Dashboard)

| 方法 | 接口 | 说明 | 是否需要Token |
|-----|------|------|--------------|
| GET | /api/dashboard/overview | 获取概览统计数据 | ✅ |
| GET | /api/dashboard/stats | 获取统计数据（别名） | ✅ |
| GET | /api/dashboard/trend | 获取趋势数据 | ✅ |
| GET | /api/dashboard/efficiency | 获取效率分析数据 | ✅ |
| GET | /api/dashboard/confidence-distribution | 获取置信度分布 | ✅ |
| GET | /api/dashboard/file-type-distribution | 获取文件类型分布 | ✅ |
| GET | /api/dashboard/status-distribution | 获取处理状态分布 | ✅ |

## 10. 数据导出 (Export)

| 方法 | 接口 | 说明 | 是否需要Token |
|-----|------|------|--------------|
| GET | /api/export/files | 导出文件列表 | ✅ |
| GET | /api/export/audits | 导出审核记录 | ✅ |
| GET | /api/export/stats | 导出统计报表 | ✅ |

## 11. 健康检查 (Health)

| 方法 | 接口 | 说明 | 是否需要Token |
|-----|------|------|--------------|
| GET | /health | 系统健康检查 | ❌ |

## 请求示例

### 1. 用户登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 123456789,
    "username": "admin",
    "nickname": "管理员"
  }
}
```

### 2. 创建模板
```bash
curl -X POST http://localhost:8080/api/template \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "templateName": "学生成绩单",
    "templateCode": "TRANSCRIPT",
    "targetKvConfig": [
      {"key":"student_name","label":"姓名"},
      {"key":"student_id","label":"学号"}
    ],
    "targetTableConfig": [
      {"key":"course","label":"课程名称"},
      {"key":"score","label":"成绩"}
    ]
  }'
```

### 3. 获取用户列表
```bash
curl -X GET "http://localhost:8080/api/user/page?current=1&size=10&keyword=admin" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. AI文档处理
```bash
curl -X POST http://localhost:8080/api/v1/ai/process \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "fileId": 123456,
    "options": {
      "use_llm": true,
      "enhance_image": true
    },
    "template_config": {
      "fields": ["name", "id", "school"]
    }
  }'
```

### 5. 获取数据面板统计
```bash
curl -X GET http://localhost:8080/api/dashboard/stats \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权/未登录 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 通用响应格式

成功响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

失败响应：
```json
{
  "code": 400,
  "message": "参数错误: xxx",
  "data": null
}
```

分页响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [...],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  }
}
```

## 注意事项

1. **Token格式**：所有需要认证的接口，请在请求头中添加：`Authorization: Bearer <token>`
2. **Token有效期**：默认24小时，过期后需要重新登录
3. **分页参数**：`current`（当前页，从1开始），`size`（每页数量）
4. **时间格式**：统一使用 `yyyy-MM-dd HH:mm:ss` 格式
5. **文件上传**：使用 `multipart/form-data` 格式
6. **JSON配置**：模板的KV和表格配置需要是合法的JSON数组格式

