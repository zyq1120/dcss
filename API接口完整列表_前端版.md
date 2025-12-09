# 📡 API接口完整列表（前端开发参考）

> **基础URL**: `http://localhost:8080`  
> **Token格式**: `Authorization: Bearer {token}`  
> **时间格式**: `yyyy-MM-dd HH:mm:ss`  
> **文档版本**: v2.0  
> **更新时间**: 2025-12-09

---

## 🎯 核心业务流程（用户视角）

### 流程1：新用户注册与登录
```
1. 用户访问系统 → 进入登录页
2. 点击"注册"按钮 → 填写用户信息
   调用: POST /api/auth/register
3. 注册成功 → 跳转到登录页
4. 输入用户名密码 → 点击登录
   调用: POST /api/auth/login
5. 登录成功 → 保存Token → 进入主页
   调用: GET /api/auth/me (获取用户信息)
```

### 流程2：文档上传与AI识别（核心流程）
```
1. 用户进入"文件管理"页面
2. 点击"上传文件"按钮 → 选择文件
   调用: POST /api/file/upload
   响应: 返回文件ID
3. 上传成功 → 文件列表显示新文件
   调用: GET /api/file/page (刷新列表)
4. 点击文件的"AI识别"按钮
   调用: POST /api/v1/ai/process
   请求: { fileId: "xxx" }
   等待处理（显示加载动画）
5. AI识别完成 → 显示识别结果
   - 文档类型：成绩单
   - 置信度：87%
   - 提取信息：姓名、学号、课程等
6. 用户查看详细结果
   可选: GET /api/classification/detail/{fileId}
```

### 流程3：文档分类查询
```
1. 用户进入"文档分类"页面
2. 查看有哪些文档类型
   调用: GET /api/classification/types
   显示: 成绩单(150)、毕业证书(80)等
3. 选择"成绩单"类型
   调用: GET /api/classification/list?documentType=成绩单
   显示: 所有成绩单列表
4. 可选：搜索特定学生
   输入: 姓名"张三"
   调用: GET /api/classification/list?documentType=成绩单&keyword=张三
5. 点击某条记录查看详情
   调用: GET /api/classification/detail/{fileId}
   显示: 完整的识别信息、课程列表、成绩等
```

### 流程4：文档审核流程
```
1. 审核员登录系统
2. 进入"审核管理"页面
3. 查看待审核文档列表
   调用: GET /api/file/page?status=3
   (status=3 表示待人工审核)
4. 点击某个文档进行审核
   调用: GET /api/audit/result/{fileId} (获取AI识别结果)
   调用: GET /api/audit/preview/{fileId} (获取文件预览URL)
5. 审核员对比原文和识别结果
   - 如果正确 → 点击"通过"
   - 如果有误 → 修改后点击"通过"
   - 如果完全错误 → 点击"拒绝"
6. 提交审核意见
   调用: POST /api/audit/submit
   请求: {
     fileId: "xxx",
     auditStatus: 1,  // 1=通过, 2=拒绝
     auditComment: "审核意见"
   }
7. 审核完成 → 更新文档状态 → 返回列表
```

### 流程5：数据统计查看
```
1. 管理员进入"数据面板"
2. 系统自动加载统计数据
   调用: GET /api/dashboard/stats
   显示: 总文件数、今日上传、处理中等
3. 查看趋势图
   调用: GET /api/dashboard/trend?days=7
   显示: 最近7天的上传趋势
4. 查看文档类型分布
   调用: GET /api/dashboard/file-type-distribution
   显示: 饼图（成绩单50%、证书30%等）
5. 查看置信度分布
   调用: GET /api/dashboard/confidence-distribution
   了解AI识别准确率
```

### 流程6：用户与权限管理
```
1. 超级管理员进入"用户管理"
2. 查看用户列表
   调用: GET /api/user/page
3. 创建新用户
   点击"添加用户" → 填写信息
   调用: POST /api/user
4. 为用户分配角色
   点击"分配角色" → 选择角色（审核员/管理员等）
   调用: POST /api/user/assign-role
   请求: {
     userId: "xxx",
     roleIds: ["111", "222"]
   }
5. 角色管理
   进入"角色管理" → 创建/编辑角色
   调用: POST /api/role (创建)
   调用: PUT /api/role (更新)
```

### 流程7：全局搜索功能
```
1. 用户在任意页面使用搜索框
2. 输入关键词：如"张三"或"2021001"
3. 调用全局搜索接口
   调用: GET /api/classification/search?keyword=张三
4. 系统搜索所有文档
   - 匹配姓名、学号、学校等字段
   - 跨文档类型搜索
5. 显示搜索结果列表
   - 显示文档类型、文件名、匹配信息
6. 点击结果查看详情
   调用: GET /api/classification/detail/{fileId}
```

---

## 🔄 典型页面交互流程

### 页面1：登录页
```
【用户操作】          【前端调用】                【后端响应】
输入用户名密码  →  POST /api/auth/login  →  返回Token和用户信息
保存Token      →  存储到localStorage    →  -
跳转主页       →  GET /api/auth/me      →  获取完整用户信息
```

### 页面2：文件上传页
```
【用户操作】          【前端调用】                    【后端响应】
选择文件       →  创建FormData              →  -
点击上传       →  POST /api/file/upload    →  返回文件ID
显示进度条     →  监听上传进度              →  -
上传完成       →  显示"上传成功"            →  -
自动跳转       →  进入文件详情页            →  -
```

### 页面3：AI识别页
```
【用户操作】          【前端调用】                      【后端响应】
点击AI识别     →  POST /api/v1/ai/process    →  返回识别结果
显示加载中     →  轮询或WebSocket             →  处理进度
识别完成       →  显示结果（表格形式）         →  -
点击字段编辑   →  弹出编辑框                   →  -
保存修改       →  POST /api/audit/modify      →  保存成功
```

### 页面4：文档分类列表页
```
【用户操作】          【前端调用】                              【后端响应】
进入页面       →  GET /api/classification/types      →  返回类型列表
选择类型       →  GET /api/classification/list       →  返回文档列表
搜索关键词     →  GET /api/classification/list?keyword=xxx  →  过滤结果
翻页           →  GET /api/classification/list?current=2    →  第2页数据
导出列表       →  前端生成CSV                        →  -
```

### 页面5：文档详情页
```
【用户操作】          【前端调用】                          【后端响应】
打开详情       →  GET /api/classification/detail/{id}  →  完整信息
预览文件       →  GET /api/audit/preview/{id}         →  返回文件URL
查看原始文本   →  使用data中的text字段                →  -
查看课程表     →  使用data中的courses数组             →  -
下载文件       →  使用fileUrl直接下载                 →  -
```

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

## 1. 🔐 认证模块

### 1.1 用户登录
- **接口**: `POST /api/auth/login`
- **需要Token**: ❌
- **请求参数**:
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- **响应数据**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "123456789",
    "username": "admin",
    "nickname": "管理员"
  }
  ```

### 1.2 用户注册
- **接口**: `POST /api/auth/register`
- **需要Token**: ❌
- **请求参数**:
  ```json
  {
    "username": "newuser",
    "password": "password123",
    "nickname": "新用户",
    "email": "user@example.com"
  }
  ```
- **响应数据**: `true/false`

### 1.3 用户登出
- **接口**: `POST /api/auth/logout`
- **需要Token**: ✅
- **请求参数**: 无
- **响应数据**: `true/false`

### 1.4 获取当前用户信息
- **接口**: `GET /api/auth/me`
- **需要Token**: ✅
- **请求参数**: 无
- **响应数据**:
  ```json
  {
    "id": "123456789",
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@system.com",
    "phone": "13800138000",
    "status": 1,
    "createTime": "2025-12-09 10:00:00"
  }
  ```

### 1.5 刷新Token
- **接口**: `POST /api/auth/refresh`
- **需要Token**: ✅
- **请求参数**: 无
- **响应数据**:
  ```json
  {
    "token": "新的token字符串"
  }
  ```

---

## 2. 👥 用户管理

### 2.1 创建用户
- **接口**: `POST /api/user`
- **需要Token**: ✅
- **请求参数**:
  ```json
  {
    "username": "testuser",
    "password": "password123",
    "nickname": "测试用户",
    "email": "test@example.com",
    "phone": "13900000000"
  }
  ```
- **响应数据**: `"用户ID"` (字符串)

### 2.2 用户列表（分页）
- **接口**: `GET /api/user/page`
- **需要Token**: ✅
- **请求参数**: 
  - `current`: 当前页（默认1）
  - `size`: 每页数量（默认10）
  - `keyword`: 搜索关键词（可选）
- **响应数据**:
  ```json
  {
    "records": [
      {
        "id": "123456789",
        "username": "admin",
        "nickname": "管理员",
        "email": "admin@system.com",
        "phone": "13800138000",
        "status": 1,
        "createTime": "2025-12-09 10:00:00",
        "roleNames": ["超级管理员"],
        "roleIds": ["111", "222"]
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  }
  ```

### 2.3 获取用户详情
- **接口**: `GET /api/user/{id}`
- **需要Token**: ✅
- **请求参数**: `id` (路径参数)
- **响应数据**: 同上用户对象

### 2.4 更新用户
- **接口**: `PUT /api/user`
- **需要Token**: ✅
- **请求参数**:
  ```json
  {
    "id": "123456789",
    "nickname": "新昵称",
    "email": "newemail@example.com",
    "phone": "13900000000"
  }
  ```
- **响应数据**: `true/false`

### 2.5 删除用户
- **接口**: `DELETE /api/user/{id}`
- **需要Token**: ✅
- **请求参数**: `id` (路径参数)
- **响应数据**: `true/false`

### 2.6 分配角色给用户
- **接口**: `POST /api/user/assign-role`
- **需要Token**: ✅
- **请求参数**:
  ```json
  {
    "userId": "123456789",
    "roleIds": ["111", "222"]
  }
  ```
- **响应数据**: `true/false`

---

## 3. 🎭 角色管理

### 3.1 创建角色
- **接口**: `POST /api/role`
- **需要Token**: ✅
- **请求参数**:
  ```json
  {
    "roleName": "管理员",
    "roleKey": "ROLE_ADMIN"
  }
  ```
- **响应数据**: `"角色ID"` (字符串)

### 3.2 角色列表（分页）
- **接口**: `GET /api/role/page`
- **需要Token**: ✅
- **请求参数**: 
  - `current`: 当前页
  - `size`: 每页数量
- **响应数据**:
  ```json
  {
    "records": [
      {
        "id": "111",
        "roleName": "超级管理员",
        "roleKey": "SUPER_ADMIN",
        "status": 1,
        "createTime": "2025-12-09 10:00:00"
      }
    ],
    "total": 10,
    "current": 1,
    "size": 10
  }
  ```

### 3.3 更新角色
- **接口**: `PUT /api/role`
- **需要Token**: ✅
- **请求参数**:
  ```json
  {
    "id": "111",
    "roleName": "新角色名"
  }
  ```
- **响应数据**: `true/false`

### 3.4 删除角色
- **接口**: `DELETE /api/role/{id}`
- **需要Token**: ✅
- **请求参数**: `id` (路径参数)
- **响应数据**: `true/false`

---

## 4. 📋 模板管理

### 4.1 创建模板
- **接口**: `POST /api/template`
- **需要Token**: ✅
- **请求参数**:
  ```json
  {
    "templateName": "学生成绩单",
    "templateCode": "TRANSCRIPT",
    "targetKvConfig": [{"key":"name","label":"姓名"}],
    "targetTableConfig": [{"key":"course","label":"课程"}]
  }
  ```
- **响应数据**: `"模板ID"` (字符串)

### 4.2 模板列表（分页）
- **接口**: `GET /api/template/page`
- **需要Token**: ✅
- **请求参数**: `current`, `size`, `keyword`
- **响应数据**:
  ```json
  {
    "records": [
      {
        "id": "333",
        "templateName": "学生成绩单",
        "templateCode": "TRANSCRIPT",
        "status": 1,
        "createTime": "2025-12-09 10:00:00"
      }
    ],
    "total": 20
  }
  ```

### 4.3 更新模板
- **接口**: `PUT /api/template`
- **需要Token**: ✅
- **请求参数**: 同创建，需包含 `id`
- **响应数据**: `true/false`

### 4.4 删除模板
- **接口**: `DELETE /api/template/{id}`
- **需要Token**: ✅
- **响应数据**: `true/false`

---

## 5. 📁 文件管理

### 5.1 上传文件
- **接口**: `POST /api/file/upload`
- **需要Token**: ✅
- **Content-Type**: `multipart/form-data`
- **表单参数**:
  - `file`: 文件对象
  - `templateId`: 模板ID（可选）
  - `userId`: 用户ID（可选）
- **响应数据**: `"文件ID"` (字符串)

### 5.2 文件列表（分页）
- **接口**: `GET /api/file/page`
- **需要Token**: ✅
- **请求参数**: `current`, `size`, `status`, `fileName`
- **响应数据**:
  ```json
  {
    "records": [
      {
        "id": "555",
        "fileName": "成绩单.jpg",
        "fileType": "image/jpeg",
        "fileSize": 102400,
        "uploadUserName": "张三",
        "status": 1,
        "createTime": "2025-12-09 10:00:00"
      }
    ],
    "total": 50
  }
  ```

### 5.3 文件详情
- **接口**: `GET /api/file/{id}`
- **需要Token**: ✅
- **响应数据**:
  ```json
  {
    "id": "555",
    "fileName": "成绩单.jpg",
    "filePath": "minio路径",
    "fileType": "image/jpeg",
    "fileSize": 102400,
    "uploadUserId": "123",
    "uploadUserName": "张三",
    "templateId": "333",
    "templateName": "成绩单模板",
    "status": 1,
    "ocrStatus": 1,
    "nlpStatus": 1,
    "auditStatus": 0,
    "createTime": "2025-12-09 10:00:00"
  }
  ```

### 5.4 删除文件
- **接口**: `DELETE /api/file/{id}`
- **需要Token**: ✅
- **响应数据**: `true/false`

---

## 6. 🤖 AI处理

### 6.1 AI智能处理
- **接口**: `POST /api/v1/ai/process`
- **需要Token**: ✅
- **请求参数**:
  ```json
  {
    "fileId": "555",
    "options": {
      "use_llm": true,
      "llm_image": false,
      "enhance_image": true,
      "detect_table": true,
      "llm_provider": "Qwen",
      "llm_model": "qwen-max"
    },
    "templateConfig": {
      "fields": []
    }
  }
  ```
- **响应数据**:
  ```json
  {
    "fileId": "555",
    "fileInfo": {
      "bucket": "document-files",
      "object": "2025/12/09/xxx.jpg",
      "url": "http://minio-url/...",
      "fileId": "555"
    },
    "aiResult": {
      "document_type": "成绩单",
      "confidence_overall": 0.87,
      "basic_info": {
        "name": "张三",
        "student_id": "2021001",
        "school_name": "XX大学"
      },
      "academic_info": {},
      "certificate_info": {},
      "financial_info": {},
      "leave_info": {},
      "courses": [
        {
          "course_name": "数据结构",
          "score": 85,
          "credit": 4
        }
      ],
      "summary": "类型 成绩单，姓名 张三，学校 XX大学",
      "text": "OCR识别的原始文本",
      "tables": [],
      "fields": {},
      "document_type_candidates": {
        "成绩单": 0.98,
        "毕业证书": 0.01
      }
    }
  }
  ```

---

## 7. ✅ 审核管理

### 7.1 提交审核
- **接口**: `POST /api/audit/submit`
- **需要Token**: ✅
- **请求参数**:
  ```json
  {
    "fileId": "555",
    "auditStatus": 1,
    "auditComment": "审核通过"
  }
  ```
- **响应数据**: `true/false`

### 7.2 审核历史
- **接口**: `GET /api/audit/history/{fileId}`
- **需要Token**: ✅
- **响应数据**:
  ```json
  [
    {
      "id": "777",
      "fileId": "555",
      "fileName": "成绩单.jpg",
      "auditorName": "审核员",
      "auditStatus": 1,
      "auditStatusName": "通过",
      "auditComment": "审核通过",
      "createTime": "2025-12-09 11:00:00"
    }
  ]
  ```

### 7.3 获取文件预览URL
- **接口**: `GET /api/audit/preview/{fileId}`
- **需要Token**: ✅
- **响应数据**: `"http://minio-url/..."`

### 7.4 获取AI处理结果
- **接口**: `GET /api/audit/result/{fileId}`
- **需要Token**: ✅
- **响应数据**: 同AI处理响应的 `aiResult` 部分

---

## 8. 📊 数据面板

### 8.1 概览统计
- **接口**: `GET /api/dashboard/stats`
- **需要Token**: ✅
- **响应数据**:
  ```json
  {
    "totalFiles": 1000,
    "todayFiles": 50,
    "processingFiles": 20,
    "completedFiles": 900,
    "avgConfidence": 0.85,
    "totalUsers": 100
  }
  ```

### 8.2 趋势数据
- **接口**: `GET /api/dashboard/trend`
- **需要Token**: ✅
- **请求参数**: `days` (默认7)
- **响应数据**:
  ```json
  [
    {
      "date": "2025-12-01",
      "fileCount": 10,
      "avgConfidence": 0.85
    }
  ]
  ```

### 8.3 文件类型分布
- **接口**: `GET /api/dashboard/file-type-distribution`
- **需要Token**: ✅
- **响应数据**:
  ```json
  [
    {
      "documentType": "成绩单",
      "count": 500,
      "percentage": 50.0
    }
  ]
  ```

---

## 9. 📑 文档分类

### 9.1 文档类型统计
- **接口**: `GET /api/classification/types`
- **需要Token**: ✅
- **响应数据**:
  ```json
  [
    {
      "documentType": "成绩单",
      "count": 150
    },
    {
      "documentType": "毕业证书/学历证书",
      "count": 80
    }
  ]
  ```

### 9.2 按类型查询文档
- **接口**: `GET /api/classification/list`
- **需要Token**: ✅
- **请求参数**: 
  - `documentType`: 文档类型（必填）
  - `current`: 当前页
  - `size`: 每页数量
  - `keyword`: 关键词（可选）
- **响应数据**:
  ```json
  {
    "records": [
      {
        "fileId": "555",
        "fileName": "成绩单.jpg",
        "documentType": "成绩单",
        "confidence": 0.87,
        "summary": "类型 成绩单，姓名 张三",
        "name": "张三",
        "idNumber": "2021001",
        "schoolName": null,
        "uploadTime": "2025-12-09 10:00:00",
        "fileUrl": "http://..."
      }
    ],
    "total": 150
  }
  ```

### 9.3 查看文档详情
- **接口**: `GET /api/classification/detail/{fileId}`
- **需要Token**: ✅
- **响应数据**:
  ```json
  {
    "fileId": "555",
    "fileName": "成绩单.jpg",
    "fileUrl": "http://...",
    "documentType": "成绩单",
    "confidenceOverall": 0.87,
    "basicInfo": {
      "name": "张三",
      "student_id": "2021001"
    },
    "academicInfo": {},
    "certificateInfo": {},
    "financialInfo": {},
    "leaveInfo": {},
    "courses": [
      {
        "course_name": "数据结构",
        "score": 85
      }
    ],
    "tables": [],
    "summary": "类型 成绩单，姓名 张三",
    "text": "OCR原始文本",
    "documentTypeCandidates": {
      "成绩单": 0.98
    },
    "fields": {}
  }
  ```

### 9.4 全局搜索
- **接口**: `GET /api/classification/search`
- **需要Token**: ✅
- **请求参数**: 
  - `keyword`: 搜索关键词（必填）
  - `current`: 当前页
  - `size`: 每页数量
- **响应数据**: 同按类型查询

---

## 📌 前端开发注意事项

### 1. Token处理
- 登录成功后保存token到localStorage
- 请求头格式：`Authorization: Bearer {token}`
- Token过期（401）时跳转登录页

### 2. ID字段处理 ⚠️ 重要
- 所有ID字段返回**字符串类型**
- 原因：防止JavaScript精度丢失
- 示例：`"786070547090898944"` 而不是数字

### 3. 分页参数
- `current`: 当前页码，从1开始
- `size`: 每页数量，建议10-50
- `total`: 总记录数
- `pages`: 总页数

### 4. 文件上传
使用 FormData:
```javascript
const formData = new FormData();
formData.append('file', fileObject);
formData.append('templateId', templateId);
```

### 5. 时间格式
- 统一：`yyyy-MM-dd HH:mm:ss`
- 示例：`2025-12-09 10:30:00`

### 6. 错误处理
```javascript
if (response.code === 200) {
  // 成功，使用 response.data
} else {
  // 失败，显示 response.message
}
```

### 7. 状态码说明
- `0`: 待处理/待校对
- `1`: 处理中/已校对
- `2`: 已完成
- `3`: 待人工
- `4`: 已归档
- `5`: 失败

---

## 💡 实际使用场景示例

### 场景1：学生上传成绩单查询
```
【场景描述】
学生小明需要上传自己的成绩单，并查看识别结果

【操作步骤】
1. 小明登录系统
   → POST /api/auth/login
   
2. 进入"文件上传"页面
   → 选择成绩单图片
   → POST /api/file/upload
   → 获得 fileId: "123"
   
3. 点击"开始识别"按钮
   → POST /api/v1/ai/process
   → 请求体: { fileId: "123", options: { use_llm: true } }
   
4. 等待10秒后，识别完成
   → 系统显示：
      - 文档类型：成绩单 (置信度98%)
      - 姓名：小明
      - 学号：2021001
      - 学校：XX大学
      - 课程成绩：数学85分、英语90分...
      
5. 小明下载识别结果
   → 点击"导出PDF"或"导出Excel"
```

### 场景2：教务老师批量审核成绩单
```
【场景描述】
教务老师需要审核100份学生上传的成绩单

【操作步骤】
1. 老师登录系统（审核员角色）
   → POST /api/auth/login
   
2. 进入"审核管理"页面
   → GET /api/file/page?status=3
   → 显示待审核列表（100条）
   
3. 点击第一份成绩单
   → GET /api/classification/detail/{fileId}
   → 左侧显示原图，右侧显示识别结果
   
4. 对比检查：
   - 姓名是否正确 ✓
   - 学号是否正确 ✓
   - 成绩是否正确 ✗ (数学成绩识别错误)
   
5. 修改错误字段
   → 将数学成绩从"83"改为"85"
   
6. 点击"通过"按钮
   → POST /api/audit/submit
   → 请求体: {
        fileId: "xxx",
        auditStatus: 1,
        auditComment: "已修正数学成绩"
      }
      
7. 继续审核下一份
   → 循环步骤3-6
   
8. 审核完成后查看统计
   → GET /api/dashboard/stats
   → 今日审核：100份，通过：95份，拒绝：5份
```

### 场景3：管理员查看系统运营数据
```
【场景描述】
系统管理员需要了解本周系统使用情况

【操作步骤】
1. 管理员登录
   → POST /api/auth/login
   
2. 进入"数据面板"
   → 页面自动加载多个接口：
   
   a) 概览数据
      → GET /api/dashboard/stats
      → 显示：
         - 总文件数：5000
         - 本周新增：500
         - 处理中：20
         - AI平均准确率：87%
         
   b) 趋势图
      → GET /api/dashboard/trend?days=7
      → 绘制折线图显示每日上传量
      
   c) 文档类型分布
      → GET /api/dashboard/file-type-distribution
      → 绘制饼图：
         - 成绩单 45%
         - 毕业证书 30%
         - 其他 25%
         
3. 点击"成绩单"查看详情
   → GET /api/classification/list?documentType=成绩单
   → 显示所有成绩单列表
   
4. 导出周报
   → 前端汇总数据生成Excel报表
```

### 场景4：用户忘记密码找回
```
【场景描述】
用户忘记密码需要重置

【操作步骤】
1. 用户点击"忘记密码"
   → （注：当前API未提供，需要添加）
   → 建议接口: POST /api/auth/forgot-password
   
2. 输入邮箱
   → 系统发送重置链接到邮箱
   
3. 用户点击邮件链接
   → 跳转到重置密码页面
   
4. 输入新密码
   → POST /api/auth/reset-password
   
5. 重置成功，跳转登录页
   → POST /api/auth/login
```

### 场景5：多文件批量上传与识别
```
【场景描述】
用户需要一次性上传20份文档

【操作步骤】
1. 用户选择20个文件
   → 前端遍历文件列表
   
2. 逐个上传（或并发上传）
   → 循环调用: POST /api/file/upload
   → 收集所有fileId: ["1", "2", "3", ...]
   
3. 批量发起AI识别
   → 循环调用: POST /api/v1/ai/process
   → 或使用Promise.all并发请求
   
4. 显示进度条
   → 已完成: 15/20 (75%)
   
5. 全部完成后跳转到文件列表
   → GET /api/file/page
   → 显示刚上传的文件
```

---

## 🎨 前端页面建议

### 推荐页面结构
```
系统首页 (/)
├── 登录页 (/login)
├── 注册页 (/register)
├── 主控制台 (/dashboard)
│   ├── 数据概览
│   ├── 快捷操作
│   └── 最近文档
├── 文件管理 (/files)
│   ├── 文件列表 (/files/list)
│   ├── 上传文件 (/files/upload)
│   └── 文件详情 (/files/:id)
├── AI识别 (/ai)
│   ├── 识别结果 (/ai/result/:id)
│   └── 识别历史 (/ai/history)
├── 文档分类 (/classification)
│   ├── 分类列表 (/classification/types)
│   ├── 文档列表 (/classification/:type)
│   └── 文档详情 (/classification/detail/:id)
├── 审核管理 (/audit)
│   ├── 待审核列表 (/audit/pending)
│   ├── 审核详情 (/audit/:id)
│   └── 审核历史 (/audit/history)
├── 数据统计 (/statistics)
│   ├── 数据面板 (/statistics/dashboard)
│   └── 报表导出 (/statistics/export)
├── 系统管理 (/admin)
│   ├── 用户管理 (/admin/users)
│   ├── 角色管理 (/admin/roles)
│   └── 模板管理 (/admin/templates)
└── 个人中心 (/profile)
    ├── 个人信息 (/profile/info)
    └── 修改密码 (/profile/password)
```

### 各页面需要的接口

**登录页**
- POST /api/auth/login
- POST /api/auth/register

**主控制台**
- GET /api/dashboard/stats
- GET /api/file/page?size=5 (最近文件)
- GET /api/auth/me

**文件列表页**
- GET /api/file/page
- DELETE /api/file/{id}
- POST /api/file/upload

**AI识别页**
- POST /api/v1/ai/process
- GET /api/audit/result/{id}
- GET /api/audit/preview/{id}

**文档分类页**
- GET /api/classification/types
- GET /api/classification/list
- GET /api/classification/detail/{id}
- GET /api/classification/search

**审核管理页**
- GET /api/file/page?status=3
- GET /api/audit/result/{id}
- POST /api/audit/submit
- GET /api/audit/history/{id}

**数据统计页**
- GET /api/dashboard/stats
- GET /api/dashboard/trend
- GET /api/dashboard/file-type-distribution
- GET /api/dashboard/confidence-distribution

**用户管理页**
- GET /api/user/page
- POST /api/user
- PUT /api/user
- DELETE /api/user/{id}
- POST /api/user/assign-role

**角色管理页**
- GET /api/role/page
- POST /api/role
- PUT /api/role
- DELETE /api/role/{id}

**模板管理页**
- GET /api/template/page
- POST /api/template
- PUT /api/template
- DELETE /api/template/{id}

---

## 🔔 常见问题与解决方案

### Q1: Token过期怎么办？
```
【问题】用户操作过程中Token过期，返回401

【解决方案】
1. 拦截器捕获401错误
2. 自动调用刷新Token接口
   → POST /api/auth/refresh
3. 获取新Token并更新localStorage
4. 重新发起原请求
5. 如果刷新失败，跳转登录页
```

### Q2: 文件上传失败怎么处理？
```
【问题】文件太大或网络不稳定导致上传失败

【解决方案】
1. 前端限制文件大小（如10MB）
2. 添加重试机制（最多3次）
3. 使用分片上传（大文件）
4. 显示详细错误信息给用户
5. 提供"取消上传"功能
```

### Q3: AI识别时间过长怎么办？
```
【问题】AI处理需要300秒以上，用户等待焦虑

【解决方案】
1. 显示进度条或加载动画
2. 提示预计等待时间："预计需要300秒"
3. 允许用户后台处理，完成后通知
4. 使用WebSocket推送处理进度
5. 提供"稍后查看结果"选项
```

### Q4: 如何处理分页数据？
```
【问题】需要加载大量数据，如何优化

【解决方案】
1. 使用虚拟滚动（大列表）
2. 懒加载（滚动到底部自动加载）
3. 缓存已加载的页面数据
4. 提供"跳转到指定页"功能
5. 显示总数和当前范围："显示1-10，共500条"
```

### Q5: 搜索结果太多怎么办？
```
【问题】搜索关键词返回几百条结果

【解决方案】
1. 使用分页显示
2. 添加更多筛选条件
   - 文档类型
   - 上传时间范围
   - 置信度范围
3. 支持多关键词搜索
4. 提供排序功能（时间、置信度）
5. 高亮显示匹配的关键词
```


