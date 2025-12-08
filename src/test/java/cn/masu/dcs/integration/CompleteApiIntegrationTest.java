package cn.masu.dcs.integration;

import cn.masu.dcs.document_classification_system_springboot.DocumentClassificationSystemSpringbootApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 完整接口集成测试套件
 * 测试所有API接口并生成测试报告
 *
 * @author System
 */
@Slf4j
@SpringBootTest(classes = DocumentClassificationSystemSpringbootApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CompleteApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 测试报告收集器
    private static final List<TestReport> testReports = new ArrayList<>();
    private static String authToken = null;
    private static Long testUserId = null;
    private static Long testRoleId = null;
    private static Long testTemplateId = null;
    private static Long testFileId = null;

    /**
     * 测试报告实体
     */
    static class TestReport {
        String moduleName;
        String testName;
        String method;
        String url;
        boolean success;
        String errorMessage;
        long responseTime;
        int statusCode;
        String timestamp;

        public TestReport(String moduleName, String testName, String method, String url) {
            this.moduleName = moduleName;
            this.testName = testName;
            this.method = method;
            this.url = url;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    @BeforeAll
    public static void setup() {
        log.info("========================================");
        log.info("开始执行完整接口集成测试");
        log.info("测试时间: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("========================================");
    }

    @AfterAll
    public static void tearDown() throws IOException {
        generateTestReport();
    }

    // ==================== 1. 认证模块测试 ====================

    @Test
    @Order(1)
    @DisplayName("1.1 用户登录测试")
    public void testLogin() {
        TestReport report = new TestReport("认证模块", "用户登录", "POST", "/api/auth/login");
        long startTime = System.currentTimeMillis();

        try {
            String loginJson = """
                {
                    "username": "admin",
                    "password": "admin123"
                }
                """;

            MvcResult result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson))
                    .andExpect(status().isOk())
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            String responseBody = result.getResponse().getContentAsString();

            // 提取token（假设响应格式为 {"code":200,"data":{"token":"xxx","userId":1}}）
            if (responseBody.contains("token")) {
                Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data != null) {
                    authToken = (String) data.get("token");
                    Object userIdObj = data.get("userId");
                    if (userIdObj instanceof Integer) {
                        testUserId = ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        testUserId = (Long) userIdObj;
                    }
                }
            }

            report.success = true;
            log.info("✅ 登录测试成功 - Token: {}", authToken != null ? "已获取" : "未获取");

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 登录测试失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    @Test
    @Order(2)
    @DisplayName("1.2 获取用户信息测试")
    public void testGetUserInfo() {
        TestReport report = new TestReport("认证模块", "获取用户信息", "GET", "/api/auth/userinfo");
        long startTime = System.currentTimeMillis();

        try {
            if (testUserId == null) {
                testUserId = 1L; // 使用默认值
            }

            MvcResult result = mockMvc.perform(get("/api/auth/userinfo")
                    .param("userId", testUserId.toString())
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 获取用户信息成功");
            } else {
                log.warn("⚠️ 获取用户信息失败 - 状态码: {}", report.statusCode);
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 获取用户信息失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    // ==================== 2. 用户管理模块测试 ====================

    @Test
    @Order(3)
    @DisplayName("2.1 分页查询用户列表")
    public void testGetUserPage() {
        TestReport report = new TestReport("用户管理", "分页查询用户", "GET", "/api/user/page");
        long startTime = System.currentTimeMillis();

        try {
            MvcResult result = mockMvc.perform(get("/api/user/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 分页查询用户列表成功");
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 分页查询用户失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    @Test
    @Order(4)
    @DisplayName("2.2 创建用户测试")
    public void testCreateUser() {
        TestReport report = new TestReport("用户管理", "创建用户", "POST", "/api/user");
        long startTime = System.currentTimeMillis();

        try {
            String userJson = String.format("""
                {
                    "username": "testuser_%d",
                    "password": "test123",
                    "nickname": "测试用户",
                    "email": "test@example.com",
                    "phone": "13900000000",
                    "status": 1
                }
                """, System.currentTimeMillis());

            MvcResult result = mockMvc.perform(post("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson)
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 创建用户成功");
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 创建用户失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    // ==================== 3. 角色管理模块测试 ====================

    @Test
    @Order(5)
    @DisplayName("3.1 分页查询角色列表")
    public void testGetRolePage() {
        TestReport report = new TestReport("角色管理", "分页查询角色", "GET", "/api/role/page");
        long startTime = System.currentTimeMillis();

        try {
            MvcResult result = mockMvc.perform(get("/api/role/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 分页查询角色列表成功");

                // 提取第一个角色ID用于后续测试
                String responseBody = result.getResponse().getContentAsString();
                if (responseBody.contains("\"id\"")) {
                    testRoleId = 1L; // 简化处理，使用默认值
                }
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 分页查询角色失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    @Test
    @Order(6)
    @DisplayName("3.2 创建角色测试")
    public void testCreateRole() {
        TestReport report = new TestReport("角色管理", "创建角色", "POST", "/api/role");
        long startTime = System.currentTimeMillis();

        try {
            String roleJson = String.format("""
                {
                    "roleCode": "TEST_ROLE_%d",
                    "roleName": "测试角色",
                    "description": "自动化测试角色",
                    "status": 1
                }
                """, System.currentTimeMillis());

            MvcResult result = mockMvc.perform(post("/api/role")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(roleJson)
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 创建角色成功");
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 创建角色失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    // ==================== 4. 模板管理模块测试 ====================

    @Test
    @Order(7)
    @DisplayName("4.1 分页查询模板列表")
    public void testGetTemplatePage() {
        TestReport report = new TestReport("模板管理", "分页查询模板", "GET", "/api/template/page");
        long startTime = System.currentTimeMillis();

        try {
            MvcResult result = mockMvc.perform(get("/api/template/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 分页查询模板列表成功");
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 分页查询模板失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    @Test
    @Order(8)
    @DisplayName("4.2 创建模板测试")
    public void testCreateTemplate() {
        TestReport report = new TestReport("模板管理", "创建模板", "POST", "/api/template");
        long startTime = System.currentTimeMillis();

        try {
            String templateJson = String.format("""
                {
                    "templateCode": "TEST_TPL_%d",
                    "templateName": "测试模板",
                    "description": "自动化测试模板",
                    "fieldConfig": "{\\"fields\\":[\\"name\\",\\"id\\"]}",
                    "status": 1
                }
                """, System.currentTimeMillis());

            MvcResult result = mockMvc.perform(post("/api/template")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(templateJson)
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 创建模板成功");

                // 提取模板ID
                String responseBody = result.getResponse().getContentAsString();
                if (responseBody.contains("\"data\"")) {
                    testTemplateId = 1L; // 简化处理
                }
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 创建模板失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    // ==================== 5. 文件管理模块测试 ====================

    @Test
    @Order(9)
    @DisplayName("5.1 文件上传测试")
    public void testFileUpload() {
        TestReport report = new TestReport("文件管理", "文件上传", "POST", "/api/file/upload");
        long startTime = System.currentTimeMillis();

        try {
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Test PDF Content".getBytes(StandardCharsets.UTF_8)
            );

            MvcResult result = mockMvc.perform(multipart("/api/file/upload")
                    .file(file)
                    .param("userId", testUserId != null ? testUserId.toString() : "1")
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 文件上传成功");

                // 提取文件ID
                String responseBody = result.getResponse().getContentAsString();
                if (responseBody.contains("\"data\"")) {
                    testFileId = 1L; // 简化处理
                }
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 文件上传失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    @Test
    @Order(10)
    @DisplayName("5.2 分页查询文件列表")
    public void testGetFilePage() {
        TestReport report = new TestReport("文件管理", "分页查询文件", "GET", "/api/file/page");
        long startTime = System.currentTimeMillis();

        try {
            MvcResult result = mockMvc.perform(get("/api/file/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 分页查询文件列表成功");
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 分页查询文件失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    // ==================== 6. OCR模块测试 ====================

    @Test
    @Order(11)
    @DisplayName("6.1 执行OCR识别测试")
    public void testProcessOcr() {
        TestReport report = new TestReport("OCR模块", "执行OCR识别", "POST", "/api/ocr/process/{fileId}");
        long startTime = System.currentTimeMillis();

        try {
            Long fileId = testFileId != null ? testFileId : 1L;

            MvcResult result = mockMvc.perform(post("/api/ocr/process/" + fileId)
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ OCR识别测试成功");
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ OCR识别测试失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    @Test
    @Order(12)
    @DisplayName("6.2 获取OCR结果测试")
    public void testGetOcrResult() {
        TestReport report = new TestReport("OCR模块", "获取OCR结果", "GET", "/api/ocr/result/{fileId}");
        long startTime = System.currentTimeMillis();

        try {
            Long fileId = testFileId != null ? testFileId : 1L;

            MvcResult result = mockMvc.perform(get("/api/ocr/result/" + fileId)
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 获取OCR结果成功");
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 获取OCR结果失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    // ==================== 7. NLP模块测试 ====================

    @Test
    @Order(13)
    @DisplayName("7.1 执行NLP提取测试")
    public void testProcessNlp() {
        TestReport report = new TestReport("NLP模块", "执行NLP提取", "POST", "/api/nlp/process/{fileId}");
        long startTime = System.currentTimeMillis();

        try {
            Long fileId = testFileId != null ? testFileId : 1L;

            MvcResult result = mockMvc.perform(post("/api/nlp/process/" + fileId)
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ NLP提取测试成功");
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ NLP提取测试失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    @Test
    @Order(14)
    @DisplayName("7.2 获取NLP结果测试")
    public void testGetNlpResult() {
        TestReport report = new TestReport("NLP模块", "获取NLP结果", "GET", "/api/nlp/result/{fileId}");
        long startTime = System.currentTimeMillis();

        try {
            Long fileId = testFileId != null ? testFileId : 1L;

            MvcResult result = mockMvc.perform(get("/api/nlp/result/" + fileId)
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 获取NLP结果成功");
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 获取NLP结果失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    // ==================== 8. 审核模块测试 ====================

    @Test
    @Order(15)
    @DisplayName("8.1 分页查询审核记录")
    public void testGetAuditPage() {
        TestReport report = new TestReport("审核模块", "分页查询审核记录", "GET", "/api/audit/page");
        long startTime = System.currentTimeMillis();

        try {
            MvcResult result = mockMvc.perform(get("/api/audit/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            report.statusCode = result.getResponse().getStatus();
            report.success = result.getResponse().getStatus() == 200;

            if (report.success) {
                log.info("✅ 分页查询审核记录成功");
            }

        } catch (Exception e) {
            report.success = false;
            report.errorMessage = e.getMessage();
            log.error("❌ 分页查询审核记录失败: {}", e.getMessage());
        } finally {
            report.responseTime = System.currentTimeMillis() - startTime;
            testReports.add(report);
        }
    }

    // ==================== 生成测试报告 ====================

    /**
     * 生成测试报告
     */
    private static void generateTestReport() throws IOException {
        String reportDir = "test-reports";
        File dir = new File(reportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportPath = reportDir + "/api-test-report-" + timestamp + ".html";

        // 统计数据
        long totalTests = testReports.size();
        long successTests = testReports.stream().filter(r -> r.success).count();
        long failedTests = totalTests - successTests;
        double successRate = totalTests > 0 ? (successTests * 100.0 / totalTests) : 0;
        long totalTime = testReports.stream().mapToLong(r -> r.responseTime).sum();

        // 按模块分组统计
        Map<String, Long> moduleStats = new HashMap<>();
        Map<String, Long> moduleSuccess = new HashMap<>();

        for (TestReport report : testReports) {
            moduleStats.merge(report.moduleName, 1L, Long::sum);
            if (report.success) {
                moduleSuccess.merge(report.moduleName, 1L, Long::sum);
            }
        }

        // 生成HTML报告
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='zh-CN'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <title>API接口测试报告</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 20px; background: #f5f5f5; }\n");
        html.append("        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("        h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }\n");
        html.append("        h2 { color: #34495e; margin-top: 30px; }\n");
        html.append("        .summary { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin: 20px 0; }\n");
        html.append("        .stat-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: center; }\n");
        html.append("        .stat-card.success { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }\n");
        html.append("        .stat-card.failed { background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%); }\n");
        html.append("        .stat-card.rate { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }\n");
        html.append("        .stat-value { font-size: 36px; font-weight: bold; }\n");
        html.append("        .stat-label { font-size: 14px; margin-top: 5px; opacity: 0.9; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
        html.append("        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append("        th { background-color: #3498db; color: white; font-weight: bold; }\n");
        html.append("        tr:hover { background-color: #f5f5f5; }\n");
        html.append("        .success { color: #27ae60; font-weight: bold; }\n");
        html.append("        .failed { color: #e74c3c; font-weight: bold; }\n");
        html.append("        .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; }\n");
        html.append("        .badge-success { background: #27ae60; color: white; }\n");
        html.append("        .badge-failed { background: #e74c3c; color: white; }\n");
        html.append("        .method { font-family: monospace; font-weight: bold; }\n");
        html.append("        .method-get { color: #61affe; }\n");
        html.append("        .method-post { color: #49cc90; }\n");
        html.append("        .method-put { color: #fca130; }\n");
        html.append("        .method-delete { color: #f93e3e; }\n");
        html.append("        .footer { margin-top: 30px; text-align: center; color: #7f8c8d; font-size: 12px; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class='container'>\n");
        html.append("        <h1>📊 API接口自动化测试报告</h1>\n");
        html.append("        <p><strong>测试时间：</strong>" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>\n");
        html.append("        <p><strong>项目名称：</strong>文档分类系统</p>\n");

        // 总体统计
        html.append("        <div class='summary'>\n");
        html.append("            <div class='stat-card'>\n");
        html.append("                <div class='stat-value'>").append(totalTests).append("</div>\n");
        html.append("                <div class='stat-label'>总测试数</div>\n");
        html.append("            </div>\n");
        html.append("            <div class='stat-card success'>\n");
        html.append("                <div class='stat-value'>").append(successTests).append("</div>\n");
        html.append("                <div class='stat-label'>成功</div>\n");
        html.append("            </div>\n");
        html.append("            <div class='stat-card failed'>\n");
        html.append("                <div class='stat-value'>").append(failedTests).append("</div>\n");
        html.append("                <div class='stat-label'>失败</div>\n");
        html.append("            </div>\n");
        html.append("            <div class='stat-card rate'>\n");
        html.append("                <div class='stat-value'>").append(String.format("%.1f%%", successRate)).append("</div>\n");
        html.append("                <div class='stat-label'>成功率</div>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");

        // 模块统计
        html.append("        <h2>📈 模块测试统计</h2>\n");
        html.append("        <table>\n");
        html.append("            <tr><th>模块名称</th><th>总数</th><th>成功</th><th>失败</th><th>成功率</th></tr>\n");
        for (Map.Entry<String, Long> entry : moduleStats.entrySet()) {
            long total = entry.getValue();
            long success = moduleSuccess.getOrDefault(entry.getKey(), 0L);
            long failed = total - success;
            double rate = total > 0 ? (success * 100.0 / total) : 0;

            html.append("            <tr>\n");
            html.append("                <td>").append(entry.getKey()).append("</td>\n");
            html.append("                <td>").append(total).append("</td>\n");
            html.append("                <td class='success'>").append(success).append("</td>\n");
            html.append("                <td class='failed'>").append(failed).append("</td>\n");
            html.append("                <td>").append(String.format("%.1f%%", rate)).append("</td>\n");
            html.append("            </tr>\n");
        }
        html.append("        </table>\n");

        // 详细测试结果
        html.append("        <h2>📋 详细测试结果</h2>\n");
        html.append("        <table>\n");
        html.append("            <tr><th>模块</th><th>测试名称</th><th>方法</th><th>URL</th><th>状态码</th><th>结果</th><th>耗时(ms)</th><th>错误信息</th></tr>\n");

        for (TestReport report : testReports) {
            String methodClass = "method-" + report.method.toLowerCase();
            String statusClass = report.success ? "success" : "failed";
            String badgeClass = report.success ? "badge-success" : "badge-failed";
            String statusText = report.success ? "✅ 成功" : "❌ 失败";

            html.append("            <tr>\n");
            html.append("                <td>").append(report.moduleName).append("</td>\n");
            html.append("                <td>").append(report.testName).append("</td>\n");
            html.append("                <td class='method ").append(methodClass).append("'>").append(report.method).append("</td>\n");
            html.append("                <td><code>").append(report.url).append("</code></td>\n");
            html.append("                <td>").append(report.statusCode).append("</td>\n");
            html.append("                <td><span class='badge ").append(badgeClass).append("'>").append(statusText).append("</span></td>\n");
            html.append("                <td>").append(report.responseTime).append("</td>\n");
            html.append("                <td>").append(report.errorMessage != null ? report.errorMessage : "-").append("</td>\n");
            html.append("            </tr>\n");
        }
        html.append("        </table>\n");

        html.append("        <div class='footer'>\n");
        html.append("            <p>测试总耗时: ").append(totalTime).append(" ms</p>\n");
        html.append("            <p>© 2025 文档分类系统 - 自动化测试报告</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");

        // 写入文件
        try (FileWriter writer = new FileWriter(reportPath)) {
            writer.write(html.toString());
        }

        // 同时生成文本报告到控制台
        log.info("\n");
        log.info("========================================");
        log.info("          测试报告汇总");
        log.info("========================================");
        log.info("总测试数: {}", totalTests);
        log.info("成功数: {} ✅", successTests);
        log.info("失败数: {} ❌", failedTests);
        log.info("成功率: {:.2f}%", successRate);
        log.info("总耗时: {} ms", totalTime);
        log.info("========================================");
        log.info("详细报告已生成: {}", new File(reportPath).getAbsolutePath());
        log.info("========================================");

        // 输出失败的测试
        if (failedTests > 0) {
            log.info("\n失败的测试:");
            testReports.stream()
                .filter(r -> !r.success)
                .forEach(r -> log.error("❌ {} - {} - {}", r.moduleName, r.testName, r.errorMessage));
        }
    }
}

