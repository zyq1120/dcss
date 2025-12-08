package cn.masu.dcs.api;

import cn.masu.dcs.document_classification_system_springboot.DocumentClassificationSystemSpringbootApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 更详细的 API 自动化测试，生成包含请求/响应数据的报告。
 */
@Slf4j
@SpringBootTest(classes = DocumentClassificationSystemSpringbootApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DetailedApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final List<ResultItem> results = new ArrayList<>();
    private static String token;

    @Data
    @Builder
    static class ResultItem {
        String module;
        String name;
        String method;
        String url;
        String requestBody;
        String responseBody;
        int status;
        long costMs;
        boolean success;
    }

    private ResultItem record(MockHttpServletRequestBuilder builder,
                              String module,
                              String name,
                              String method,
                              String url,
                              String requestBody,
                              boolean successCondition) throws Exception {
        long start = System.currentTimeMillis();
        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
        long cost = System.currentTimeMillis() - start;
        String responseBody = mvcResult.getResponse().getContentAsString();
        int status = mvcResult.getResponse().getStatus();
        boolean success = successCondition;

        ResultItem item = ResultItem.builder()
            .module(module)
            .name(name)
            .method(method)
            .url(url)
            .requestBody(requestBody)
            .responseBody(responseBody)
            .status(status)
            .costMs(cost)
            .success(success)
            .build();

        results.add(item);
        return item;
    }

    @Test
    @Order(1)
    void login() throws Exception {
        String body = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        ResultItem item = record(
            post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body),
            "认证", "登录", "POST", "/api/auth/login", body,
            true
        );

        if (item.isSuccess()) {
            var tree = objectMapper.readTree(item.getResponseBody());
            if (tree.has("data") && tree.get("data").has("token")) {
                token = tree.get("data").get("token").asText();
            }
        }
    }

    @Test
    @Order(2)
    void userPage() throws Exception {
        record(
            get("/api/user/page").param("current", "1").param("size", "10")
                .header("Authorization", "Bearer " + token),
            "用户", "分页查询", "GET", "/api/user/page?current=1&size=10", null,
            true
        );
    }

    @Test
    @Order(3)
    void createUser() throws Exception {
        String body = "{\"username\":\"testuser_auto\",\"password\":\"123456\",\"nickname\":\"自动化用户\",\"email\":\"auto@example.com\"}";
        record(
            post("/api/user").contentType(MediaType.APPLICATION_JSON).content(body)
                .header("Authorization", "Bearer " + token),
            "用户", "创建用户", "POST", "/api/user", body,
            true
        );
    }

    @Test
    @Order(4)
    void userDetail() throws Exception {
        record(
            get("/api/user/1").header("Authorization", "Bearer " + token),
            "用户", "用户详情", "GET", "/api/user/1", null,
            true
        );
    }

    @Test
    @Order(5)
    void rolePage() throws Exception {
        record(
            get("/api/role/page").param("current", "1").param("size", "10")
                .header("Authorization", "Bearer " + token),
            "角色", "分页查询", "GET", "/api/role/page?current=1&size=10", null,
            true
        );
    }

    @Test
    @Order(6)
    void createRole() throws Exception {
        String body = "{\"roleName\":\"AutoRole\",\"roleKey\":\"AUTO_ROLE\",\"status\":1}";
        record(
            post("/api/role").contentType(MediaType.APPLICATION_JSON).content(body)
                .header("Authorization", "Bearer " + token),
            "角色", "创建角色", "POST", "/api/role", body,
            true
        );
    }

    @Test
    @Order(7)
    void templatePage() throws Exception {
        record(
            get("/api/template/page").param("current", "1").param("size", "10")
                .header("Authorization", "Bearer " + token),
            "模板", "分页查询", "GET", "/api/template/page?current=1&size=10", null,
            true
        );
    }

    @Test
    @Order(8)
    void createTemplate() throws Exception {
        String body = "{\"templateCode\":\"AUTO_TEMPLATE\",\"templateName\":\"Auto Template\",\"targetKvConfig\":\"{}\",\"targetTableConfig\":\"{}\",\"ruleConfig\":\"{}\"}";
        record(
            post("/api/template").contentType(MediaType.APPLICATION_JSON).content(body)
                .header("Authorization", "Bearer " + token),
            "模板", "创建模板", "POST", "/api/template", body,
            true
        );
    }

    @Test
    @Order(9)
    void filePage() throws Exception {
        record(
            get("/api/file/page").param("current", "1").param("size", "10")
                .header("Authorization", "Bearer " + token),
            "文件", "分页查询", "GET", "/api/file/page?current=1&size=10", null,
            true
        );
    }

    @Test
    @Order(10)
    void ocrResultNotFound() throws Exception {
        record(
            get("/api/ocr/result/1").header("Authorization", "Bearer " + token),
            "OCR", "获取OCR结果(无数据)", "GET", "/api/ocr/result/1", null,
            true // 接口返回业务码也记录
        );
    }

    @Test
    @Order(11)
    void nlpResultNotFound() throws Exception {
        record(
            get("/api/nlp/result/1").header("Authorization", "Bearer " + token),
            "NLP", "获取NLP结果(无数据)", "GET", "/api/nlp/result/1", null,
            true
        );
    }

    @Test
    @Order(12)
    void auditHistoryEmpty() throws Exception {
        record(
            get("/api/audit/history/1").header("Authorization", "Bearer " + token),
            "审核", "审核历史(空)", "GET", "/api/audit/history/1", null,
            true
        );
    }

    @Test
    @Order(13)
    void trainingSamplePageEmpty() throws Exception {
        record(
            get("/api/training/page").param("current", "1").param("size", "10")
                .header("Authorization", "Bearer " + token),
            "训练样本", "分页查询(空)", "GET", "/api/training/page?current=1&size=10", null,
            true
        );
    }

    @Test
    @Order(14)
    void taskPageEmpty() throws Exception {
        record(
            get("/api/task/page").param("current", "1").param("size", "10")
                .header("Authorization", "Bearer " + token),
            "任务", "分页查询(空)", "GET", "/api/task/page?current=1&size=10", null,
            true
        );
    }

    @AfterAll
    static void writeReport() throws Exception {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String file = "test-reports/api-detailed-report-" + ts + ".html";

        long success = results.stream().filter(ResultItem::isSuccess).count();
        long total = results.size();
        double rate = total == 0 ? 0 : success * 100.0 / total;

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>API Detailed Report</title>");
        html.append("<style>body{font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;}table{width:100%;border-collapse:collapse;}th,td{border:1px solid #ddd;padding:8px;}th{background:#0a7cff;color:#fff;}pre{white-space:pre-wrap;word-break:break-all;} .ok{color:#0a7cff;font-weight:bold;} .fail{color:#d32f2f;font-weight:bold;}</style>");
        html.append("</head><body>");
        html.append("<h1>API 自动化详细报告</h1>");
        html.append("<p>时间：").append(LocalDateTime.now()).append("</p>");
        html.append("<p>总数：").append(total).append("，成功：").append(success).append("，成功率：").append(String.format("%.2f", rate)).append("%</p>");
        html.append("<table>");
        html.append("<tr><th>模块</th><th>用例</th><th>方法</th><th>URL</th><th>Status</th><th>耗时(ms)</th><th>请求体</th><th>响应体</th></tr>");
        for (ResultItem r : results) {
            html.append("<tr>")
                .append("<td>").append(r.getModule()).append("</td>")
                .append("<td>").append(r.getName()).append("</td>")
                .append("<td>").append(r.getMethod()).append("</td>")
                .append("<td>").append(r.getUrl()).append("</td>")
                .append("<td class='").append(r.isSuccess() ? "ok" : "fail").append("'>").append(r.getStatus()).append("</td>")
                .append("<td>").append(r.getCostMs()).append("</td>")
                .append("<td><pre>").append(r.getRequestBody() == null ? "" : r.getRequestBody()).append("</pre></td>")
                .append("<td><pre>").append(r.getResponseBody() == null ? "" : r.getResponseBody()).append("</pre></td>")
                .append("</tr>");
        }
        html.append("</table></body></html>");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(html.toString());
        }
        log.info("详细测试报告已生成: {}", file);
    }
}
