package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health endpoint aligned with contract.
 * @author zyq
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public R<Map<String, String>> health() {
        return R.ok("success", Map.of("status", "healthy"));
    }
}
