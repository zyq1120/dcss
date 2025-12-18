# 智能文档分类系统 (DCSS) 接口文档

本文档详细描述了智能文档分类系统 (DCSS) 的后端 API 接口。

## 1. 认证模块 (Auth)

### 1.1 用户登录
- **接口地址**: `/api/auth/login`
- **请求方式**: `POST`
- **描述**: 用户登录接口，返回 Token 和用户信息。
- **请求参数**:
  ```json
  {
    "username": "admin",
    "password": "password123"
  }
  ```
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "userId": 1,
      "username": "admin",
      "nickname": "管理员",
      "roles": ["ADMIN"]
    }
  }
  ```

### 1.2 用户注册
- **接口地址**: `/api/auth/register`
- **请求方式**: `POST`
- **描述**: 用户注册接口。
- **请求参数**:
  ```json
  {
    "username": "newuser",
    "password": "password123",
    "nickname": "新用户",
    "email": "newuser@example.com"
  }
  ```
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "userId": 2,
      "username": "newuser"
    }
  }
  ```

### 1.3 用户登出
- **接口地址**: `/api/auth/logout`
- **请求方式**: `POST`
- **描述**: 用户登出接口，使 Token 失效。
- **请求参数**: `userId` (可选，Query Param)
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "登出成功",
    "data": null
  }
  ```

## 2. AI 智能处理模块 (AI)

### 2.1 统一 AI 处理
- **接口地址**: `/api/v1/ai/process`
- **请求方式**: `POST`
- **描述**: 统一的 AI 文档处理接口，支持 OCR、NLP、LLM 等多种模式。
- **请求参数**:
  ```json
  {
    "fileId": "123456", // 可选，已上传的文件ID
    "fileContent": "base64...", // 可选，文件Base64内容
    "text": "纯文本内容...", // 可选，纯文本处理
    "options": {
      "use_llm": true,
      "ocr_engine": "paddle"
    },
    "templateConfig": {} // 可选，模板配置
  }
  ```
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "aiResult": { ... },
      "processing_time": 1.23
    }
  }
  ```

### 2.2 文件上传并处理
- **接口地址**: `/api/v1/ai/process-file`
- **请求方式**: `POST`
- **描述**: 上传文件并直接进行 AI 处理。
- **请求参数**:
  - `file`: 文件 (MultipartFile)
  - `options`: 处理选项 JSON 字符串 (可选)
  - `template_config`: 模板配置 JSON 字符串 (可选)
- **响应示例**: 同上。

### 2.3 获取 AI 服务状态
- **接口地址**: `/api/v1/ai/status`
- **请求方式**: `GET`
- **��述**: 获取 AI 服务的健康状态和配置信息。

## 3. 文件管理模块 (File)

### 3.1 上传文件
- **接口地址**: `/api/file/upload`
- **请求方式**: `POST`
- **描述**: 上传文件到系统。
- **请求参数**:
  - `file`: 文件 (MultipartFile)
  - `templateId`: 模板ID (可选)
  - `userId`: 用户ID (可选)
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "上传成功",
    "data": 123456 // 文件ID
  }
  ```

### 3.2 下载文件
- **接口地址**: `/api/file/download/{id}`
- **请求方式**: `GET`
- **描述**: 下载指定 ID 的文件。
- **响应**: 文件流 (application/octet-stream)。

### 3.3 获取文件详情
- **接口地址**: `/api/file/{id}`
- **请求方式**: `GET`
- **描述**: 获取文件的详细信息。

### 3.4 删除文件
- **接口地址**: `/api/file/{id}`
- **请求方式**: `DELETE`
- **描述**: 删除指定 ID 的文件。

### 3.5 更新文件状态
- **接口地址**: `/api/file/status`
- **请求方式**: `PUT`
- **请求参数**:
  ```json
  {
    "id": 123456,
    "status": 1 // 状态码
  }
  ```

## 4. 文档分类模块 (Classification)

### 4.1 获取文档类型统计
- **接口地址**: `/api/classification/types`
- **请求方式**: `GET`
- **描述**: 获取所有文档类型的统计信息。
- **响应示例**:
  ```json
  {
    "code": 200,
    "data": [
      { "documentType": "发票", "count": 10 },
      { "documentType": "合同", "count": 5 }
    ]
  }
  ```

### 4.2 按类型查询文档列表
- **接口地址**: `/api/classification/list`
- **请求方式**: `GET`
- **请求参数**:
  - `documentType`: 文档类型
  - `current`: 当前页 (默认1)
  - `size`: 每页数量 (默认10)
  - `keyword`: 搜索关键词 (可选)

### 4.3 全局搜索文档
- **接口地址**: `/api/classification/search`
- **请求方式**: `GET`
- **请求参数**:
  - `keyword`: 搜索关键词
  - `current`: 当前页
  - `size`: 每页数量

### 4.4 获取文档详情
- **接口地址**: `/api/classification/detail/{fileId}`
- **请求方式**: `GET`
- **描述**: 获取文档的详细信息，包括 AI 识别结果。

## 5. 审核与校对模块 (Audit & Review)

### 5.1 提交审核
- **接口地址**: `/api/audit/submit`
- **请求方式**: `POST`
- **请求参数**:
  ```json
  {
    "fileId": 123456,
    "status": 1, // 1: 通过, 2: 驳回
    "comment": "审核意见"
  }
  ```

### 5.2 获取审核历史
- **接口地址**: `/api/audit/history/{fileId}`
- **请求方式**: `GET`

### 5.3 获取待审核任务 (校对工作台)
- **接口地址**: `/api/review/pending`
- **请求方式**: `GET`
- **描述**: 获取待人工校对的任务列表。

### 5.4 获取校对任务详情
- **接口地址**: `/api/review/{fileId}/detail`
- **请求方式**: `GET`

### 5.5 保存校对草稿
- **接口地址**: `/api/review/save-draft`
- **请求方式**: `POST`
- **描述**: 保存校对过程中的中间状态。

## 6. 数据驾驶舱模块 (Dashboard)

### 6.1 获取概览数据
- **接口地址**: `/api/dashboard/overview`
- **请求方式**: `GET`
- **描述**: 获取系统的整体统计数据。

### 6.2 获取趋势数据
- **接口地址**: `/api/dashboard/trend`
- **请求方式**: `GET`
- **请求参数**: `days` (默认7)

### 6.3 获取效率分析
- **接口地址**: `/api/dashboard/efficiency`
- **请求方式**: `GET`

### 6.4 获取置信度分布
- **接口地址**: `/api/dashboard/confidence-distribution`
- **请求方式**: `GET`

## 7. 系统管理模块 (User, Role, Template)

### 7.1 用户管理
- `POST /api/user`: 创建用户
- `PUT /api/user`: 更新用户
- `DELETE /api/user/{id}`: 删除用户
- `GET /api/user/{id}`: 获取用户详情
- `GET /api/user/page`: 分页查询用户
- `POST /api/user/assign-roles`: 分配角色

### 7.2 角色管理
- `POST /api/role`: 创建角色
- `PUT /api/role`: 更新角色
- `DELETE /api/role/{id}`: 删除角色
- `GET /api/role/page`: 分页查询角色

### 7.3 模板管理
- `POST /api/template`: 创建模板
- `PUT /api/template`: 更新模板
- `DELETE /api/template/{id}`: 删除模板
- `GET /api/template/page`: 分页查询模板
- `PUT /api/template/{id}/status`: 启用/禁用模板

## 8. 导出模块 (Export)

### 8.1 导出文件列表
- **接口地址**: `/api/export/files`
- **请求方式**: `GET`
- **响应**: Excel 文件流。

### 8.2 导出审核记录
- **接口地址**: `/api/export/audit-records`
- **请求方式**: `GET`
- **响应**: Excel 文件流。

## 9. 健康检查 (Health)

### 9.1 系统健康检查
- **接口地址**: `/health`
- **请求方式**: `GET`
- **响应**: `{"code": 200, "data": {"status": "healthy"}}`

