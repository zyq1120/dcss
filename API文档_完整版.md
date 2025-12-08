# 📚 智能文档处理系统 - API接口文档（完整版）

![API](https://img.shields.io/badge/API-50%20endpoints-blue)
![Version](https://img.shields.io/badge/version-v1.0-green)
![Status](https://img.shields.io/badge/status-stable-success)
![Updated](https://img.shields.io/badge/updated-2025--12--07-brightgreen)

**版本**: v1.0  
**更新时间**: 2025-12-07  
**基础URL**: `http://localhost:8080`  
**接口总数**: 50个  
**认证方式**: JWT Token

---

## 📋 目录

- [1. 认证模块 (Auth)](#1-认证模块-auth)
- [2. 用户管理 (User)](#2-用户管理-user)
- [3. 角色管理 (Role)](#3-角色管理-role)
- [4. 文件管理 (File)](#4-文件管理-file)
- [5. AI处理 (AI)](#5-ai处理-ai)
- [6. 校对工作台 (Review)](#6-校对工作台-review)
- [7. 提取数据 (Extract)](#7-提取数据-extract)
- [8. 审核记录 (Audit)](#8-审核记录-audit)
- [9. 模板管理 (Template)](#9-模板管理-template)
- [10. 数据驾驶舱 (Dashboard)](#10-数据驾驶舱-dashboard)
- [11. 导出功能 (Export)](#11-导出功能-export)

---

## 🔐 认证说明

### JWT Token认证

所有需要认证的接口都需要在请求头中携带JWT Token：

```bash
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOjc4NTA3OTU0NjMyNzA3Mjc2OCwidXNlcm5hbWUiOiJhZG1pbiIsInN1YiI6ImFkbWluIiwiaWF0IjoxNzMzNTY1MDAwLCJleHAiOjE3MzM2NTE0MDB9.xxx
```

### Token获取流程

```mermaid
sequenceDiagram
    participant C as 客户端
    participant A as AuthController
    participant S as AuthService
    participant D as 数据库

    C->>A: POST /api/auth/login
    A->>S: login(username, password)
    S->>D: 查询用户
    D-->>S: 用户信息
    S->>S: 验证密码
    S->>S: 生成JWT Token
    S-->>A: LoginResponse
    A-->>C: {token, userId, username}
```

---

## 1. 认证模块 (Auth)

### 1.1 用户登录

**接口**: `POST /api/auth/login`

**描述**: 用户登录，获取JWT Token

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**请求参数**:
```json
{
  "username": "admin",       // 用户名，必填
  "password": "admin123"     // 密码，必填
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOjc4NTA3OTU0NjMyNzA3Mjc2OCwidG9rZW5WZXJzaW9uIjowLCJ1c2VybmFtZSI6ImFkbWluIiwic3ViIjoiYWRtaW4iLCJpYXQiOjE3MzM1NjUwMDAsImV4cCI6MTczMzY1MTQwMH0.xxx",
    "userId": 785079546327072768,
    "username": "admin"
  }
}
```

**流程图**:
```mermaid
graph LR
    A[开始] --> B[输入用户名密码]
    B --> C{验证用户存在?}
    C -->|否| D[返回用户不存在]
    C -->|是| E{验证密码?}
    E -->|否| F[返回密码错误]
    E -->|是| G[生成JWT Token]
    G --> H[返回Token和用户信息]
    H --> I[结束]
```

---

### 1.2 用户注册

**接口**: `POST /api/auth/register`

**描述**: 新用户注册，自动登录并返回Token

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "Pass123!",
    "nickname": "新用户",
    "email": "newuser@example.com",
    "phone": "13800138000"
  }'
```

**请求参数**:
```json
{
  "username": "newuser",           // 用户名，必填，4-20字符
  "password": "Pass123!",          // 密码，必填，6-20字符
  "nickname": "新用户",             // 昵称，可选
  "email": "newuser@example.com",  // 邮箱，可选
  "phone": "13800138000"           // 手机号，可选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "userId": 785123456789012345,
    "username": "newuser"
  }
}
```

---

### 1.3 用户登出

**接口**: `POST /api/auth/logout`

**描述**: 用户登出，使当前Token失效

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

### 1.4 获取当前用户信息

**接口**: `GET /api/auth/me`

**描述**: 获取当前登录用户的详细信息

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 785079546327072768,
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatar": null,
    "status": 1,
    "roles": ["超级管理员"],
    "permissions": ["*:*:*"]
  }
}
```

---

## 2. 用户管理 (User)

### 2.1 创建用户

**接口**: `POST /api/users`

**描述**: 创建新用户（需要管理员权限）

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user001",
    "password": "Pass123!",
    "nickname": "普通用户",
    "email": "user001@example.com",
    "phone": "13900139000"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 785234567890123456
}
```

---

### 2.2 分页查询用户列表

**接口**: `GET /api/users/page`

**描述**: 分页查询用户列表，支持关键词搜索和状态筛选

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/users/page?current=1&size=10&keyword=admin&status=1" \
  -H "Authorization: Bearer {token}"
```

**请求参数**:
- `current`: 当前页码，默认1
- `size`: 每页大小，默认10
- `keyword`: 关键词搜索（用户名/昵称），可选
- `status`: 状态筛选（0=禁用，1=启用），可选

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 5,
    "current": 1,
    "size": 10,
    "pages": 1,
    "records": [
      {
        "id": 785079546327072768,
        "username": "admin",
        "nickname": "管理员",
        "email": "admin@example.com",
        "phone": "13800138000",
        "avatar": null,
        "status": 1,
        "roles": ["超级管理员"],
        "roleNames": ["超级管理员"],
        "roleIds": [1],
        "createTime": "2025-12-06T10:30:00",
        "updateTime": "2025-12-07T09:15:00"
      }
    ]
  }
}
```

---

### 2.3 获取用户详情

**接口**: `GET /api/users/{id}`

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/users/785079546327072768 \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 785079546327072768,
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatar": null,
    "status": 1,
    "roles": ["超级管理员"],
    "roleNames": ["超级管理员"],
    "roleIds": [1],
    "createTime": "2025-12-06T10:30:00",
    "updateTime": "2025-12-07T09:15:00"
  }
}
```

---

### 2.4 更新用户信息

**接口**: `PUT /api/users/{id}`

**请求示例**:
```bash
curl -X PUT http://localhost:8080/api/users/785234567890123456 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "新昵称",
    "email": "newemail@example.com",
    "phone": "13900139999"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": true
}
```

---

### 2.5 删除用户

**接口**: `DELETE /api/users/{id}`

**请求示例**:
```bash
curl -X DELETE http://localhost:8080/api/users/785234567890123456 \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": true
}
```

---

### 2.6 分配角色

**接口**: `POST /api/users/{id}/roles`

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/users/785234567890123456/roles \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "roleIds": [2, 3]
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "角色分配成功",
  "data": true
}
```

---

## 3. 角色管理 (Role)

### 3.1 创建角色

**接口**: `POST /api/roles`

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/roles \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "文档审核员",
    "roleCode": "doc_reviewer",
    "description": "负责审核识别结果"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 3
}
```

---

### 3.2 分页查询角色列表

**接口**: `GET /api/roles/page`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/roles/page?current=1&size=10" \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 3,
    "current": 1,
    "size": 10,
    "pages": 1,
    "records": [
      {
        "id": 1,
        "roleName": "超级管理员",
        "roleCode": "admin",
        "description": "系统管理员",
        "createTime": "2025-12-06T10:00:00"
      },
      {
        "id": 2,
        "roleName": "普通用户",
        "roleCode": "user",
        "description": "普通用户角色",
        "createTime": "2025-12-06T10:00:00"
      }
    ]
  }
}
```

---

## 4. 文件管理 (File)

### 文件处理流程图

```mermaid
stateDiagram-v2
    [*] --> 已上传: 上传文件
    已上传 --> 已入队: 提交AI处理
    已入队 --> 处理中: AI开始处理
    处理中 --> 待审核: 置信度低
    处理中 --> 已归档: 置信度高(>0.85)
    待审核 --> 已归档: 人工校对完成
    处理中 --> 处理失败: AI处理失败
    处理失败 --> 已入队: 重试
    已归档 --> [*]
```

### 状态说明

| 状态值 | 状态名称 | 说明 |
|-------|---------|------|
| 0 | 已上传 | 文件刚上传，未处理 |
| 1 | 已入队 | 等待AI处理 |
| 2 | 处理中 | AI正在处理 |
| 3 | 待审核 | 需要人工校对 |
| 4 | 已归档 | 处理完成并归档 |
| 5 | 处理失败 | AI处理失败 |

---

### 4.1 上传文件

**接口**: `POST /api/file/upload`

**描述**: 上传文件到MinIO，并创建文件记录

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/file/upload \
  -H "Authorization: Bearer {token}" \
  -F "file=@/path/to/document.pdf" \
  -F "userId=785079546327072768" \
  -F "templateId=1"
```

**请求参数**:
- `file`: 文件，必填（支持图片/PDF）
- `userId`: 用户ID，必填
- `templateId`: 模板ID，可选

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": 785345678901234567
}
```

---

### 4.2 分页查询文件列表

**接口**: `GET /api/file/page`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/file/page?current=1&size=10&status=3&keyword=成绩单" \
  -H "Authorization: Bearer {token}"
```

**请求参数**:
- `current`: 当前页码，默认1
- `size`: 每页大小，默认10
- `keyword`: 关键词搜索（文件名），可选
- `status`: 状态筛选，可选
- `userId`: 用户筛选，可选

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 25,
    "current": 1,
    "size": 10,
    "pages": 3,
    "records": [
      {
        "id": 785345678901234567,
        "userId": 785079546327072768,
        "templateId": 1,
        "fileName": "成绩单_2024.pdf",
        "fileType": "pdf",
        "fileSize": 524288,
        "minioBucket": "document-files",
        "minioObject": "2025/12/07/xxx.pdf",
        "processStatus": 3,
        "processMode": "OCR+NLP",
        "retryCount": 0,
        "createTime": "2025-12-07T10:30:00",
        "updateTime": "2025-12-07T10:32:15"
      }
    ]
  }
}
```

---

### 4.3 获取文件详情

**接口**: `GET /api/file/{id}`

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/file/785345678901234567 \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 785345678901234567,
    "userId": 785079546327072768,
    "templateId": 1,
    "fileName": "成绩单_2024.pdf",
    "fileType": "pdf",
    "fileSize": 524288,
    "minioBucket": "document-files",
    "minioObject": "2025/12/07/xxx.pdf",
    "thumbnailUrl": "http://minio:9000/document-files/thumbnails/xxx.jpg",
    "processStatus": 3,
    "processMode": "OCR+NLP",
    "retryCount": 0,
    "failReason": null,
    "createTime": "2025-12-07T10:30:00",
    "updateTime": "2025-12-07T10:32:15",
    "previewUrl": "http://minio:9000/document-files/2025/12/07/xxx.pdf"
  }
}
```

---

### 4.4 下载文件

**接口**: `GET /api/file/download/{id}`

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/file/download/785345678901234567 \
  -H "Authorization: Bearer {token}" \
  -o downloaded_file.pdf
```

**响应**: 二进制文件流

---

### 4.5 删除文件

**接口**: `DELETE /api/file/{id}`

**请求示例**:
```bash
curl -X DELETE http://localhost:8080/api/file/785345678901234567 \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": true
}
```

---

### 4.6 更新文件状态

**接口**: `PUT /api/file/status`

**请求示例**:
```bash
curl -X PUT http://localhost:8080/api/file/status \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "fileId": 785345678901234567,
    "status": 4
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": true
}
```

---

## 5. AI处理 (AI)

### AI处理完整流程

```mermaid
sequenceDiagram
    participant C as 客户端
    participant AC as AiProcessController
    participant AS as AiServiceClient
    participant P as Python AI服务
    participant M as MinIO
    participant DB as 数据库

    C->>AC: POST /api/v1/ai/process
    AC->>AC: 验证请求参数
    AC->>M: 保存原始文件
    AC->>AS: 调用统一AI处理
    AS->>P: HTTP请求（文件/文本）
    P->>P: OCR识别
    P->>P: NLP提取
    P->>P: LLM兜底（如需要）
    P-->>AS: 返回识别结果JSON
    AS-->>AC: AIProcessResponse
    AC->>DB: 保存识别结果
    AC->>DB: 保存提取数据
    AC->>DB: 更新文件状态
    AC-->>C: 返回完整结果
```

---

### 5.1 统一AI处理

**接口**: `POST /api/v1/ai/process`

**描述**: 统一的AI处理接口，支持OCR、NLP、LLM

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/v1/ai/process \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "fileContent": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "fileName": "成绩单.jpg",
    "options": {
      "enhanceImage": true,
      "handwritingMode": false,
      "llmOnly": false,
      "disableAi": false,
      "useLlm": true,
      "llmImage": false,
      "autoInferFields": true,
      "analyzeOcr": true,
      "detectTable": true,
      "tableForce": false,
      "llmProvider": "qwen",
      "llmModel": "qwen-plus"
    },
    "templateConfig": {}
  }'
```

**请求参数详解**:

```json
{
  "fileContent": "base64编码的文件内容",  // 可选，与text二选一
  "fileName": "文件名.jpg",              // 可选
  "text": "直接输入的文本",               // 可选，与fileContent二选一
  "options": {
    "enhanceImage": true,            // 是否图像增强
    "handwritingMode": false,        // 是否手写增强
    "llmOnly": false,                // 是否仅使用LLM
    "disableAi": false,              // 是否禁用AI
    "useLlm": true,                  // 是否允许LLM兜底
    "llmImage": false,               // 是否使用多模态LLM直接读图
    "autoInferFields": true,         // 是否自动推断字段
    "analyzeOcr": true,              // 是否返回OCR诊断
    "detectTable": true,             // 是否检测表格
    "tableForce": false,             // 是否强制表格OCR
    "tableDetectThreshold": 0.002,   // 表格检测阈值
    "llmProvider": "qwen",           // LLM提供商(qwen/gemini/openai)
    "llmModel": "qwen-plus"          // LLM模型名称
  },
  "templateConfig": {}                // 模板配置，可选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "处理成功",
  "data": {
    "fileId": 785456789012345678,
    "fileName": "成绩单.jpg",
    "success": true,
    "confidence": 0.92,
    "processingTime": 3.5,
    "ocrResult": {
      "fullText": "学生姓名：张三\n学号：2021001\n课程：高等数学 95分",
      "confidence": 0.95,
      "source": "paddleocr",
      "llmFallbackUsed": false,
      "textBlocks": [
        {
          "text": "学生姓名：张三",
          "confidence": 0.98,
          "position": {"x": 100, "y": 50, "width": 200, "height": 30}
        }
      ]
    },
    "nlpResult": {
      "extractMain": {
        "studentName": "张三",
        "studentId": "2021001",
        "confidence": 0.92
      },
      "extractDetails": [
        {
          "courseName": "高等数学",
          "score": 95.0,
          "confidence": 0.95
        }
      ]
    },
    "llmUsed": true,
    "errorMessage": null
  }
}
```

---

### 5.2 处理状态说明

**处理模式**:

```mermaid
graph TD
    A[开始] --> B{检查选项}
    B -->|llmOnly=true| C[仅使用LLM]
    B -->|disableAi=true| D[仅OCR]
    B -->|useLlm=true| E[OCR+NLP+LLM兜底]
    B -->|默认| F[OCR+NLP]
    
    C --> G[调用LLM API]
    D --> H[调用PaddleOCR]
    E --> I[调用OCR]
    F --> I
    
    I --> J[调用NLP]
    J --> K{置信度检查}
    K -->|>0.85| L[返回结果]
    K -->|≤0.85| M[LLM兜底]
    M --> L
    G --> L
    H --> L
```

---

## 6. 校对工作台 (Review)

### 校对工作流程

```mermaid
stateDiagram-v2
    [*] --> 待审核列表: 进入工作台
    待审核列表 --> 选择文件: 点击文件
    选择文件 --> 加载详情: 获取识别结果
    加载详情 --> 编辑字段: 开始校对
    编辑字段 --> 保存草稿: 暂存
    保存草稿 --> 编辑字段: 继续修改
    编辑字段 --> 提交审核: 完成校对
    提交审核 --> 更新状态: 标记为已归档
    更新状态 --> [*]
```

---

### 6.1 获取待审核列表

**接口**: `GET /api/review/pending`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/review/pending?current=1&size=10" \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 15,
    "current": 1,
    "size": 10,
    "records": [
      {
        "fileId": 785456789012345678,
        "fileName": "成绩单_2024.pdf",
        "uploadTime": "2025-12-07T10:30:00",
        "confidence": 0.78,
        "needReview": true,
        "status": 3
      }
    ]
  }
}
```

---

### 6.2 获取校对详情

**接口**: `GET /api/review/{fileId}/detail`

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/review/785456789012345678/detail \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "fileInfo": {
      "id": 785456789012345678,
      "fileName": "成绩单_2024.pdf",
      "previewUrl": "http://minio:9000/document-files/xxx.pdf"
    },
    "extractMain": {
      "id": 785567890123456789,
      "studentName": "张三",
      "studentId": "2021001",
      "semester": "2024-2025-1",
      "confidence": 0.78
    },
    "extractDetails": [
      {
        "id": 785678901234567890,
        "courseName": "高等数学",
        "courseCode": "MATH101",
        "credit": 4.0,
        "score": 95.0,
        "gradePoint": 4.5,
        "confidence": 0.92,
        "isVerified": 0
      }
    ],
    "ocrRaw": {
      "fullText": "学生姓名：张三\n学号：2021001...",
      "confidence": 0.88
    }
  }
}
```

---

### 6.3 保存草稿

**接口**: `POST /api/review/{fileId}/draft`

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/review/785456789012345678/draft \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "extractMain": {
      "studentName": "张三",
      "studentId": "2021001",
      "semester": "2024-2025-1"
    },
    "extractDetails": [
      {
        "id": 785678901234567890,
        "courseName": "高等数学",
        "score": 95.0,
        "isVerified": 1
      }
    ]
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "草稿保存成功",
  "data": true
}
```

---

### 6.4 提交审核

**接口**: `POST /api/review/{fileId}/submit`

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/review/785456789012345678/submit \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "extractMain": {
      "studentName": "张三",
      "studentId": "2021001"
    },
    "extractDetails": [
      {
        "id": 785678901234567890,
        "courseName": "高等数学",
        "score": 95.0,
        "isVerified": 1
      }
    ],
    "comment": "已核对无误"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "提交成功",
  "data": true
}
```

---

## 7. 提取数据 (Extract)

### 7.1 获取提取数据列表

**接口**: `GET /api/extract/page`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/extract/page?current=1&size=10&studentName=张三" \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 8,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": 785567890123456789,
        "fileId": 785456789012345678,
        "studentName": "张三",
        "studentId": "2021001",
        "semester": "2024-2025-1",
        "totalCredit": 20.0,
        "avgScore": 88.5,
        "avgGradePoint": 3.8,
        "confidence": 0.92,
        "status": 1,
        "createTime": "2025-12-07T10:32:00"
      }
    ]
  }
}
```

---

### 7.2 获取提取详情

**接口**: `GET /api/extract/{id}`

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/extract/785567890123456789 \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "extractMain": {
      "id": 785567890123456789,
      "fileId": 785456789012345678,
      "studentName": "张三",
      "studentId": "2021001",
      "semester": "2024-2025-1",
      "totalCredit": 20.0,
      "avgScore": 88.5,
      "avgGradePoint": 3.8,
      "confidence": 0.92,
      "status": 1
    },
    "extractDetails": [
      {
        "id": 785678901234567890,
        "mainId": 785567890123456789,
        "courseName": "高等数学",
        "courseCode": "MATH101",
        "credit": 4.0,
        "score": 95.0,
        "gradePoint": 4.5,
        "confidence": 0.95,
        "isVerified": 1
      }
    ]
  }
}
```

---

## 8. 审核记录 (Audit)

### 8.1 分页查询审核记录

**接口**: `GET /api/audit/page`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/audit/page?current=1&size=10&fileId=785456789012345678" \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 3,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": 785789012345678901,
        "fileId": 785456789012345678,
        "fileName": "成绩单_2024.pdf",
        "extractMainId": 785567890123456789,
        "auditorId": 785079546327072768,
        "auditorName": "管理员",
        "auditStatus": 2,
        "auditStatusName": "已通过",
        "auditComment": "已核对无误",
        "createTime": "2025-12-07T11:00:00"
      }
    ]
  }
}
```

---

### 8.2 提交审核

**接口**: `POST /api/audit/submit`

**描述**: 审核员对文件提交审核意见

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/audit/submit \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "fileId": 785456789012345678,
    "auditStatus": 2,
    "auditComment": "已核对无误"
  }'
```

**请求参数**:
```json
{
  "fileId": 785456789012345678,  // 文件ID，必填
  "auditStatus": 2,              // 审核状态：0=待审核, 1=审核中, 2=已通过, 3=已驳回
  "auditComment": "已核对无误"    // 审核意见，可选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "审核提交成功",
  "data": true
}
```

---

### 8.3 获取审核历史

**接口**: `GET /api/audit/history/{fileId}`

**描述**: 获取指定文件的所有审核历史记录

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/audit/history/785456789012345678 \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 785789012345678901,
      "fileId": 785456789012345678,
      "fileName": "成绩单_2024.pdf",
      "extractMainId": 785567890123456789,
      "auditorId": 785079546327072768,
      "auditorName": "管理员",
      "auditStatus": 2,
      "auditStatusName": "已通过",
      "auditComment": "已核对无误",
      "createTime": "2025-12-07T11:00:00"
    }
  ]
}
```

---

### 8.4 获取文件预览URL 🆕

**接口**: `GET /api/audit/preview/{fileId}`

**描述**: 获取MinIO文件的预签名访问URL，用于在审核工作台预览原始文件

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/audit/preview/785456789012345678 \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取预览链接成功",
  "data": "http://localhost:9000/document-files/2025/12/07/a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=..."
}
```

**使用场景**:
- 审核工作台左侧预览原始文件
- URL有效期默认7天
- 支持PDF、图片等多种文件格式

**流程图**:
```mermaid
graph LR
    A[审核员请求预览] --> B[查询文件信息]
    B --> C{文件存在?}
    C -->|否| D[返回文件不存在]
    C -->|是| E{MinIO信息完整?}
    E -->|否| F[返回存储信息不完整]
    E -->|是| G[生成预签名URL]
    G --> H[返回预览链接]
    H --> I[前端iframe展示]
```

---

### 8.5 获取AI处理结果 🆕

**接口**: `GET /api/audit/result/{fileId}`

**描述**: 获取Python AI服务处理后的完整结果，包含KV数据和完整提取结果

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/audit/result/785456789012345678 \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取处理结果成功",
  "data": {
    "fileId": 785456789012345678,
    "extractId": 785567890123456789,
    "confidence": 0.95,
    "status": 0,
    "createTime": "2025-12-07T10:30:00",
    "kvData": {
      "studentName": "张三",
      "studentId": "2021001",
      "major": "计算机科学与技术",
      "grade": "2021级"
    },
    "extractResult": {
      "document_type": "成绩单",
      "confidence_overall": 0.95,
      "fields": {
        "studentName": "张三",
        "studentId": "2021001"
      },
      "courses": [
        {
          "courseName": "高等数学",
          "score": "95",
          "credit": "4"
        }
      ],
      "meta": {
        "processing_time": 2.5,
        "ocr_quality": "good",
        "llm_status": {
          "enabled": true,
          "provider": "qwen"
        }
      }
    }
  }
}
```

**使用场景**:
- 审核工作台右侧展示AI识别结果
- 作为字段编辑的数据源
- 查看置信度和处理质量

**数据结构说明**:
- `kvData`: 键值对形式的提取数据（可编辑）
- `extractResult`: Python返回的完整JSON结果（只读）
- `confidence`: 整体置信度（0-1）
- `status`: 状态（0=待校对, 1=已校对, 2=已确认）

---

### 8.6 修改字段 🆕

**接口**: `POST /api/audit/modify/{fileId}`

**描述**: 审核员修改AI识别的字段值，系统会自动创建审核记录

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/audit/modify/785456789012345678 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "studentName": "张三（已修正）",
    "studentId": "2021001",
    "newField": "新增字段值"
  }'
```

**请求参数**: 任意键值对的JSON对象
```json
{
  "字段名1": "字段值1",
  "字段名2": "字段值2",
  ...
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "字段修改成功",
  "data": true
}
```

**业务逻辑**:
1. 合并现有字段和修改字段
2. 更新`document_extract_main`表的`kv_data_json`
3. 将状态设置为0（待校对）
4. 自动创建审核记录，状态为1（审核中）

**流程图**:
```mermaid
sequenceDiagram
    participant U as 审核员
    participant C as Controller
    participant S as Service
    participant DB as 数据库

    U->>C: POST /api/audit/modify/{fileId}
    C->>S: modifyFields(fileId, fields, auditorId)
    S->>DB: 查询现有数据
    DB-->>S: ExtractMain数据
    S->>S: 解析现有kvData
    S->>S: 合并新字段
    S->>DB: 更新ExtractMain
    S->>DB: 创建AuditRecord
    DB-->>S: 成功
    S-->>C: true
    C-->>U: 修改成功
```

**错误处理**:
- 文件不存在：返回 `FILE_NOT_FOUND`
- 提取数据不存在：返回 `未找到提取数据`
- 字段为空：返回 `修改字段不能为空`

---

### 8.7 审核工作台完整流程 🔥

```mermaid
graph TB
    A[打开审核工作台] --> B[输入文件ID]
    B --> C[加载文件预览]
    B --> D[加载AI处理结果]
    C --> E[左侧iframe显示原文件]
    D --> F[右侧显示识别字段]
    F --> G{需要修改?}
    G -->|是| H[编辑字段]
    H --> I[保存修改]
    I --> J[自动创建审核记录]
    G -->|否| K[提交审核]
    J --> K
    K --> L{审核通过?}
    L -->|是| M[文件状态=已归档]
    L -->|否| N[文件状态=待人工处理]
    M --> O[完成]
    N --> O
```

---

## 9. 模板管理 (Template)

### 9.1 创建模板

**接口**: `POST /api/template`

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/template \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "templateCode": "TRANSCRIPT",
    "templateName": "成绩单模板",
    "docType": "成绩单",
    "targetKvConfig": "{\"studentName\": {\"type\": \"string\", \"required\": true}}",
    "targetTableConfig": "{\"courses\": {\"columns\": [\"courseName\", \"score\"]}}",
    "ruleConfig": "{\"validations\": []}"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 4
}
```

---

### 9.2 分页查询模板列表

**接口**: `GET /api/template/page`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/template/page?current=1&size=10" \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 4,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "templateCode": "TRANSCRIPT",
        "templateName": "成绩单模板",
        "docType": "成绩单",
        "targetKvConfig": "{...}",
        "targetTableConfig": "{...}",
        "ruleConfig": "{...}",
        "createTime": "2025-12-06T10:00:00"
      }
    ]
  }
}
```

---

## 10. 数据驾驶舱 (Dashboard)

### 10.1 获取概览数据

**接口**: `GET /api/dashboard/overview`

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/dashboard/overview \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "fileStats": {
      "total": 1250,
      "uploaded": 50,
      "processing": 12,
      "needReview": 38,
      "archived": 1100,
      "failed": 50
    },
    "taskStats": {
      "todayCount": 45,
      "weekCount": 280,
      "avgAiTime": 3.2,
      "avgHumanTime": 125.5,
      "avgConfidence": 0.87,
      "avgRetry": 0.3
    },
    "statusDistribution": [
      {"status": 0, "count": 50, "percentage": 4.0},
      {"status": 1, "count": 12, "percentage": 1.0},
      {"status": 2, "count": 0, "percentage": 0.0},
      {"status": 3, "count": 38, "percentage": 3.0},
      {"status": 4, "count": 1100, "percentage": 88.0},
      {"status": 5, "count": 50, "percentage": 4.0}
    ]
  }
}
```

---

### 10.2 获取趋势数据

**接口**: `GET /api/dashboard/trend`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/dashboard/trend?days=7" \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dates": ["2025-12-01", "2025-12-02", "2025-12-03", "2025-12-04", "2025-12-05", "2025-12-06", "2025-12-07"],
    "fileCount": [35, 42, 38, 45, 50, 48, 45],
    "avgConfidence": [0.85, 0.86, 0.88, 0.87, 0.89, 0.88, 0.87],
    "successCount": [30, 38, 35, 40, 45, 43, 40],
    "failCount": [2, 1, 1, 2, 1, 2, 2],
    "needReviewCount": [3, 3, 2, 3, 4, 3, 3]
  }
}
```

**趋势图示例**:
```mermaid
xychart-beta
    title "7天文件处理趋势"
    x-axis [12-01, 12-02, 12-03, 12-04, 12-05, 12-06, 12-07]
    y-axis "文件数" 0 --> 50
    line [35, 42, 38, 45, 50, 48, 45]
```

---

### 10.3 获取效率分析

**接口**: `GET /api/dashboard/efficiency`

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/dashboard/efficiency \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "processingSpeed": {
      "avgProcessTime": 2.5,
      "minProcessTime": 0.8,
      "maxProcessTime": 8.5,
      "filesPerHour": 1440.0
    },
    "qualityAnalysis": {
      "highConfidenceRate": 75.5,
      "mediumConfidenceRate": 18.2,
      "lowConfidenceRate": 6.3,
      "autoPassRate": 72.8
    },
    "humanIntervention": {
      "needReviewRate": 27.2,
      "avgReviewTime": 5.5,
      "fieldModificationRate": 15.3,
      "topModifiedFields": [
        {"fieldName": "已通过", "modificationCount": 45},
        {"fieldName": "待审核", "modificationCount": 38},
        {"fieldName": "已驳回", "modificationCount": 12}
      ]
    }
  }
}
```

---

### 10.4 获取置信度分布

**接口**: `GET /api/dashboard/confidence-distribution`

**请求示例**:
```bash
curl -X GET http://localhost:8080/api/dashboard/confidence-distribution \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "distribution": [
      {"range": "0-0.5", "count": 25, "percentage": 2.0},
      {"range": "0.5-0.6", "count": 30, "percentage": 2.4},
      {"range": "0.6-0.7", "count": 48, "percentage": 3.8},
      {"range": "0.7-0.8", "count": 95, "percentage": 7.6},
      {"range": "0.8-0.9", "count": 320, "percentage": 25.6},
      {"range": "0.9-1.0", "count": 732, "percentage": 58.6}
    ]
  }
}
```

---

## 11. 导出功能 (Export)

### 11.1 导出文件列表

**接口**: `POST /api/export/files`

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/export/files \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "status": 4,
    "startDate": "2025-12-01",
    "endDate": "2025-12-07"
  }' \
  -o files_export.xlsx
```

**响应**: Excel文件流

**Excel内容**:
- 文件ID
- 文件名
- 文件类型
- 文件大小
- 处理状态
- 置信度
- 创建时间
- 更新时间

---

### 11.2 导出审核记录

**接口**: `POST /api/export/audit-records`

**请求示例**:
```bash
curl -X POST "http://localhost:8080/api/export/audit-records?startDate=2025-12-01&endDate=2025-12-07" \
  -H "Authorization: Bearer {token}" \
  -o audit_records.xlsx
```

**响应**: Excel文件流

---

### 11.3 导出统计报表

**接口**: `POST /api/export/report`

**请求示例**:
```bash
curl -X POST "http://localhost:8080/api/export/report?type=overview" \
  -H "Authorization: Bearer {token}" \
  -o report.xlsx
```

**请求参数**:
- `type`: 报表类型（overview=概览报表, trend=趋势报表）

**响应**: Excel文件流

---

## 📊 完整业务流程图

### 端到端处理流程

```mermaid
graph TB
    A[用户上传文件] --> B[保存到MinIO]
    B --> C[创建文件记录]
    C --> D[提交AI处理]
    D --> E[Python AI服务]
    
    E --> F{处理模式选择}
    F -->|仅LLM| G[调用LLM API]
    F -->|OCR+NLP| H[PaddleOCR识别]
    F -->|OCR+NLP+LLM| H
    
    H --> I[BERT NLP提取]
    I --> J{置信度检查}
    
    J -->|>0.85| K[高置信度]
    J -->|≤0.85| L[LLM兜底]
    
    G --> M[保存识别结果]
    K --> M
    L --> M
    
    M --> N{置信度最终检查}
    N -->|>0.85| O[自动归档]
    N -->|≤0.85| P[进入待审核队列]
    
    P --> Q[人工校对]
    Q --> R[保存草稿]
    R --> Q
    Q --> S[提交审核]
    S --> T[更新为已归档]
    
    O --> U[数据统计]
    T --> U
    U --> V[报表导出]
```

---

### 错误处理流程

```mermaid
graph TD
    A[API请求] --> B{参数验证}
    B -->|失败| C[400 Bad Request]
    B -->|成功| D{Token验证}
    D -->|失败| E[401 Unauthorized]
    D -->|成功| F{权限检查}
    F -->|失败| G[403 Forbidden]
    F -->|成功| H{业务逻辑}
    H -->|成功| I[200 OK]
    H -->|业务异常| J[400 Business Error]
    H -->|系统异常| K[500 Internal Error]
```

---

## 📝 错误码说明

| 错误码 | 说明 | HTTP状态码 |
|-------|------|-----------|
| 200 | 成功 | 200 |
| 1001 | 参数错误 | 400 |
| 1002 | 用户不存在 | 400 |
| 1003 | 密码错误 | 400 |
| 1004 | Token无效 | 401 |
| 1005 | 权限不足 | 403 |
| 2001 | 文件不存在 | 404 |
| 2002 | 文件上传失败 | 500 |
| 3001 | AI处理失败 | 500 |
| 9999 | 系统错误 | 500 |

---

## 🔧 环境配置

### 开发环境

```yaml
# application.yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dcs?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: dcs_user
    password: your_password

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin123

ai:
  service:
    url: http://localhost:5000
```

### 生产环境

使用环境变量覆盖配置：

```bash
export DB_HOST=production-db.example.com
export DB_PASSWORD=strong_password
export MINIO_ENDPOINT=https://minio.example.com
export AI_SERVICE_URL=https://ai.example.com
```

---

## 📞 技术支持

### 常见问题

**Q: Token过期怎么办？**  
A: 重新登录获取新Token，或实现Token刷新机制。

**Q: 文件上传大小限制？**  
A: 默认50MB，可在配置中调整。

**Q: AI处理超时怎么办？**  
A: 系统会自动重试，最多3次。

**Q: 如何查看API调用日志？**  
A: 查看`logs/app.log`文件。

---

**文档版本**: v1.0  
**最后更新**: 2025-12-07  
**维护者**: zyq

---

**✅ 文档完成！** 🎊

