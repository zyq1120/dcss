package cn.masu.dcs.api;

import cn.masu.dcs.document_classification_system_springboot.DocumentClassificationSystemSpringbootApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 完整API测试套件
 * 测试所有接口并生成HTML报告
 *
 * @author System
 */
@Slf4j
@SpringBootTest(classes = DocumentClassificationSystemSpringbootApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CompleteApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final List<TestResult> testResults = new ArrayList<>();
    private static String token = null;
    private static Long userId = null;

    @Data
    static class TestResult {
        String module;
        String testName;
        String method;
        String url;
        String requestBody;
        String responseBody;
        boolean success;
        String errorMessage;
        long responseTime;
        int statusCode;
        String timestamp;

        public TestResult(String module, String testName, String method, String url) {
            this.module = module;
            this.testName = testName;
            this.method = method;
            this.url = url;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    @BeforeAll
    public static void setup() {
        log.info("========================================");
        log.info("开始执行完整API测试");
        log.info("测试时间: {}", LocalDateTime.now());
        log.info("========================================");
    }

    @AfterAll
    public static void tearDown() throws IOException {
        generateHtmlReport();
        printSummary();
    }

    private void applyResponse(TestResult result, MvcResult mvcResult, long start, boolean successCondition, String requestBody) throws Exception {
        result.statusCode = mvcResult.getResponse().getStatus();
        result.responseTime = System.currentTimeMillis() - start;
        result.success = successCondition;
        result.requestBody = requestBody;
        result.responseBody = mvcResult.getResponse().getContentAsString();
    }

    // ==================== 1. 认证模块测试 ====================

    @Test
    @Order(1)
    @DisplayName("认证-用户登录")
    public void test01_login() {
        TestResult result = new TestResult("认证模块", "用户登录", "POST", "/api/auth/login");
        long start = System.currentTimeMillis();

        try {
            String loginJson = "{\"username\":\"admin\",\"password\":\"admin123\"}";

            MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;

            String responseBody = mvcResult.getResponse().getContentAsString();
            log.info("登录响应: {}", responseBody);

            if (result.statusCode == 200 && responseBody.contains("token")) {
                // 提取token
                var response = objectMapper.readTree(responseBody);
                if (response.has("data")) {
                    var data = response.get("data");
                    if (data.has("token")) {
                        token = data.get("token").asText();
                        userId = data.get("userId").asLong();
                        log.info("✅ 登录成功，Token已获取");
                    }
                }
                result.success = true;
            } else {
                result.success = false;
                result.errorMessage = "登录失败: " + responseBody;
            }

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
            log.error("❌ 登录测试失败: {}", e.getMessage());
        }

        testResults.add(result);
    }

    @Test
    @Order(2)
    @DisplayName("认证-获取用户信息")
    public void test02_getUserInfo() {
        if (token == null) {
            log.warn("⚠️ 跳过测试：未获取到Token");
            return;
        }

        TestResult result = new TestResult("认证模块", "获取用户信息", "GET", "/api/auth/userinfo");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/auth/userinfo")
                    .param("userId", userId.toString())
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

            if (!result.success) {
                result.errorMessage = mvcResult.getResponse().getContentAsString();
            }

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 2. 用户管理模块测试 ====================

    @Test
    @Order(3)
    @DisplayName("用户-分页查询")
    public void test03_getUserPage() {
        if (token == null) return;

        TestResult result = new TestResult("用户管理", "分页查询", "GET", "/api/user/page");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/user/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 3. 角色管理模块测试 ====================

    @Test
    @Order(4)
    @DisplayName("角色-分页查询")
    public void test04_getRolePage() {
        if (token == null) return;

        TestResult result = new TestResult("角色管理", "分页查询", "GET", "/api/role/page");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/role/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 4. 模板管理模块测试 ====================

    @Test
    @Order(5)
    @DisplayName("模板-分页查询")
    public void test05_getTemplatePage() {
        if (token == null) return;

        TestResult result = new TestResult("模板管理", "分页查询", "GET", "/api/template/page");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/template/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 5. 文件管理模块测试 ====================

    @Test
    @Order(6)
    @DisplayName("文件-分页查询")
    public void test06_getFilePage() {
        if (token == null) return;

        TestResult result = new TestResult("文件管理", "分页查询", "GET", "/api/file/page");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/file/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 6. 用户管理扩展测试 ====================

    @Test
    @Order(7)
    @DisplayName("用户-创建用户")
    public void test07_createUser() {
        if (token == null) return;

        TestResult result = new TestResult("用户管理", "创建用户", "POST", "/api/user");
        long start = System.currentTimeMillis();

        try {
            String userJson = "{\"username\":\"testuser001\",\"password\":\"123456\",\"nickname\":\"测试用户\",\"email\":\"test@example.com\"}";

            MvcResult mvcResult = mockMvc.perform(post("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson)
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    @Test
    @Order(8)
    @DisplayName("用户-获取用户详情")
    public void test08_getUserDetail() {
        if (token == null) return;

        TestResult result = new TestResult("用户管理", "获取详情", "GET", "/api/user/{id}");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/user/" + userId)
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 7. 角色管理扩展测试 ====================

    @Test
    @Order(9)
    @DisplayName("角色-创建角色")
    public void test09_createRole() {
        if (token == null) return;

        TestResult result = new TestResult("角色管理", "创建角色", "POST", "/api/role");
        long start = System.currentTimeMillis();

        try {
            String roleJson = "{\"roleName\":\"Test Role\",\"roleKey\":\"TEST_ROLE\",\"status\":1}";

            MvcResult mvcResult = mockMvc.perform(post("/api/role")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(roleJson)
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 8. 模板管理扩展测试 ====================

    @Test
    @Order(10)
    @DisplayName("模板-创建模板")
    public void test10_createTemplate() {
        if (token == null) return;

        TestResult result = new TestResult("模板管理", "创建模板", "POST", "/api/template");
        long start = System.currentTimeMillis();

        try {
            String templateJson = "{\"templateCode\":\"TEST_TEMPLATE\",\"templateName\":\"Test Template\",\"targetKvConfig\":\"{}\",\"targetTableConfig\":\"{}\",\"ruleConfig\":\"{}\"}";

            MvcResult mvcResult = mockMvc.perform(post("/api/template")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(templateJson)
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 9. OCR模块测试 ====================

    @Test
    @Order(11)
    @DisplayName("OCR-获取OCR结果")
    public void test11_getOcrResult() {
        if (token == null) return;

        TestResult result = new TestResult("OCR模块", "获取OCR结果", "GET", "/api/ocr/result/{fileId}");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/ocr/result/1")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200 || result.statusCode == 404;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 10. NLP模块测试 ====================

    @Test
    @Order(12)
    @DisplayName("NLP-获取提取结果")
    public void test12_getNlpResult() {
        if (token == null) return;

        TestResult result = new TestResult("NLP模块", "获取提取结果", "GET", "/api/nlp/result/{fileId}");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/nlp/result/1")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200 || result.statusCode == 404;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 11. 数据编辑模块测试 ====================

    @Test
    @Order(13)
    @DisplayName("编辑-获取编辑数据")
    public void test13_getEditData() {
        if (token == null) return;

        TestResult result = new TestResult("数据编辑", "获取编辑数据", "GET", "/api/extract/edit/{fileId}");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/extract/edit/1")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200 || result.statusCode == 404;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 12. 审核模块测试 ====================

    @Test
    @Order(14)
    @DisplayName("审核-分页查询审核记录")
    public void test14_getAuditPage() {
        if (token == null) return;

        TestResult result = new TestResult("审核模块", "分页查询", "GET", "/api/audit/page");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/audit/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    @Test
    @Order(15)
    @DisplayName("审核-获取审核历史")
    public void test15_getAuditHistory() {
        if (token == null) return;

        TestResult result = new TestResult("审核模块", "获取审核历史", "GET", "/api/audit/history/{fileId}");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/audit/history/1")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 13. 训练样本模块测试 ====================

    @Test
    @Order(16)
    @DisplayName("训练-分页查询样本")
    public void test16_getTrainingSamplePage() {
        if (token == null) return;

        TestResult result = new TestResult("训练样本", "分页查询", "GET", "/api/training/page");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/training/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 14. 任务日志模块测试 ====================

    @Test
    @Order(17)
    @DisplayName("任务-分页查询日志")
    public void test17_getTaskPage() {
        if (token == null) return;

        TestResult result = new TestResult("任务日志", "分页查询", "GET", "/api/task/page");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/task/page")
                    .param("current", "1")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    @Test
    @Order(18)
    @DisplayName("任务-获取统计信息")
    public void test18_getTaskStatistics() {
        if (token == null) return;

        TestResult result = new TestResult("任务日志", "获取统计", "GET", "/api/task/statistics");
        long start = System.currentTimeMillis();

        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/task/statistics")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();

            result.statusCode = mvcResult.getResponse().getStatus();
            result.responseTime = System.currentTimeMillis() - start;
            result.success = result.statusCode == 200;

        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.responseTime = System.currentTimeMillis() - start;
        }

        testResults.add(result);
    }

    // ==================== 报告生成 ====================

    private static void generateHtmlReport() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "test-reports/api-test-report-" + timestamp + ".html";

        long successCount = testResults.stream().filter(r -> r.success).count();
        long failCount = testResults.size() - successCount;
        double successRate = testResults.isEmpty() ? 0 : (successCount * 100.0 / testResults.size());

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>API测试报告</title>\n");
        html.append("<style>\n");
        html.append("body{font-family:Arial,sans-serif;margin:20px;background:#f5f5f5;}\n");
        html.append(".container{max-width:1200px;margin:0 auto;background:white;padding:20px;border-radius:8px;box-shadow:0 2px 4px rgba(0,0,0,0.1);}\n");
        html.append("h1{color:#333;border-bottom:3px solid #4CAF50;padding-bottom:10px;}\n");
        html.append(".summary{display:flex;gap:20px;margin:20px 0;}\n");
        html.append(".summary-card{flex:1;padding:20px;border-radius:5px;color:white;text-align:center;}\n");
        html.append(".success{background:#4CAF50;}\n");
        html.append(".fail{background:#f44336;}\n");
        html.append(".total{background:#2196F3;}\n");
        html.append("table{width:100%;border-collapse:collapse;margin-top:20px;}\n");
        html.append("th,td{padding:12px;text-align:left;border-bottom:1px solid #ddd;}\n");
        html.append("th{background:#4CAF50;color:white;}\n");
        html.append("tr:hover{background:#f5f5f5;}\n");
        html.append(".status-success{color:#4CAF50;font-weight:bold;}\n");
        html.append(".status-fail{color:#f44336;font-weight:bold;}\n");
        html.append(".error{color:#f44336;font-size:12px;}\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        html.append("<div class='container'>\n");
        html.append("<h1>📊 API接口测试报告</h1>\n");
        html.append("<p>生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>\n");

        // 摘要
        html.append("<div class='summary'>\n");
        html.append("<div class='summary-card total'><h2>").append(testResults.size()).append("</h2><p>总测试数</p></div>\n");
        html.append("<div class='summary-card success'><h2>").append(successCount).append("</h2><p>成功</p></div>\n");
        html.append("<div class='summary-card fail'><h2>").append(failCount).append("</h2><p>失败</p></div>\n");
        html.append("<div class='summary-card total'><h2>").append(String.format("%.2f%%", successRate)).append("</h2><p>成功率</p></div>\n");
        html.append("</div>\n");

        // 测试结果表格
        html.append("<table>\n");
        html.append("<tr><th>模块</th><th>测试名称</th><th>方法</th><th>URL</th><th>状态码</th><th>耗时(ms)</th><th>结果</th><th>错误信息</th></tr>\n");

        for (TestResult result : testResults) {
            html.append("<tr>\n");
            html.append("<td>").append(result.module).append("</td>\n");
            html.append("<td>").append(result.testName).append("</td>\n");
            html.append("<td>").append(result.method).append("</td>\n");
            html.append("<td>").append(result.url).append("</td>\n");
            html.append("<td>").append(result.statusCode).append("</td>\n");
            html.append("<td>").append(result.responseTime).append("</td>\n");
            html.append("<td class='").append(result.success ? "status-success'>✅ 成功" : "status-fail'>❌ 失败").append("</td>\n");
            html.append("<td class='error'>").append(result.errorMessage != null ? result.errorMessage : "").append("</td>\n");
            html.append("</tr>\n");
        }

        html.append("</table>\n");
        html.append("</div>\n</body>\n</html>");

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(html.toString());
        }

        log.info("📄 测试报告已生成: {}", filename);
    }

    private static void printSummary() {
        log.info("========================================");
        log.info("测试完成总结");
        log.info("========================================");
        log.info("总测试数: {}", testResults.size());
        log.info("成功: {}", testResults.stream().filter(r -> r.success).count());
        log.info("失败: {}", testResults.stream().filter(r -> !r.success).count());
        log.info("成功率: {:.2f}%", testResults.isEmpty() ? 0 : (testResults.stream().filter(r -> r.success).count() * 100.0 / testResults.size()));
        log.info("========================================");
    }
}
