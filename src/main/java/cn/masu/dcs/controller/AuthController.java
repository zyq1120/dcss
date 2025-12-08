package cn.masu.dcs.controller;

import cn.masu.dcs.common.config.GlobalExceptionHandler.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.dto.LoginRequest;
import cn.masu.dcs.dto.LoginResponse;
import cn.masu.dcs.dto.UserCreateDTO;
import cn.masu.dcs.service.AuthService;
import cn.masu.dcs.vo.UserInfoVO;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 认证控制器
 * <p>
 * 负责用户认证相关功能：
 * 1. 用户登录
 * 2. 用户注册
 * 3. 用户登出
 * 4. 获取用户信息
 * 5. Token刷新
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param request 登录请求（用户名、密码）
     * @return 登录响应（Token、用户信息）
     */
    @PostMapping("/login")
    public R<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        log.info("收到登录请求: username={}", request.getUsername());
        LoginResponse response = authService.login(request);
        log.info("登录成功: username={}, userId={}", request.getUsername(), response.getUserId());
        return R.ok("登录成功", response);
    }

    /**
     * 用户注册
     *
     * @param request 注册请求（用户信息）
     * @return 注册响应（自动登录后的Token）
     */
    @PostMapping("/register")
    public R<LoginResponse> register(@Validated @RequestBody UserCreateDTO request) {
        log.info("收到注册请求: username={}", request.getUsername());
        LoginResponse response = authService.register(request);
        log.info("注册成功: username={}, userId={}", request.getUsername(), response.getUserId());
        return R.ok("注册成功", response);
    }

    /**
     * 用户登出
     * <p>
     * 说明：
     * 1. 如果不传userId，则登出当前用户
     * 2. 如果传userId，必须与当前用户一致
     * 3. 登出会使Token失效（增加tokenVersion）
     * </p>
     *
     * @param userId 用户ID（可选）
     * @return 登出结果
     */
    @PostMapping("/logout")
    public R<Void> logout(@RequestParam(required = false) Long userId) {
        Long currentUserId = getCurrentUserId();

        // 参数校验：如果传入userId，必须与当前用户一致
        if (userId != null && !Objects.equals(userId, currentUserId)) {
            log.warn("用户尝试登出其他用户: currentUserId={}, targetUserId={}", currentUserId, userId);
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权操作其他用户");
        }

        log.info("用户登出: userId={}", currentUserId);
        authService.logout(currentUserId);

        return R.ok("登出成功");
    }

    /**
     * 获取用户信息
     * <p>
     * 说明：
     * 1. 如果不传userId，则获取当前用户信息
     * 2. 如果传userId，必须与当前用户一致（普通用户不能查看其他用户信息）
     * </p>
     *
     * @param userId 用户ID（可选）
     * @return 用户信息
     */
    @GetMapping("/userinfo")
    public R<UserInfoVO> getUserInfo(@RequestParam(required = false) Long userId) {
        Long currentUserId = getCurrentUserId();

        // 参数校验：如果传入userId，必须与当前用户一致
        if (userId != null && !Objects.equals(userId, currentUserId)) {
            log.warn("用户尝试查看其他用户信息: currentUserId={}, targetUserId={}", currentUserId, userId);
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权查看其他用户信息");
        }

        log.info("获取用户信息: userId={}", currentUserId);
        UserInfoVO vo = authService.getCurrentUserInfo(currentUserId);

        return R.ok(vo);
    }

    /**
     * 刷新Token
     * <p>
     * 在Token即将过期时，可以使用此接口刷新获取新Token
     * </p>
     *
     * @param authorization Authorization请求头（格式：Bearer {token}）
     * @return 新的Token
     */
    @PostMapping("/refresh")
    public R<String> refreshToken(
            @RequestHeader("Authorization") @NotBlank(message = "Token不能为空") String authorization) {

        // 校验Token格式
        if (!authorization.startsWith("Bearer ")) {
            log.warn("Token格式错误: {}", authorization);
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "Token格式错误，应为：Bearer {token}");
        }

        String token = authorization.substring(7); // 移除"Bearer "前缀
        if (token.isEmpty()) {
            log.warn("Token为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "Token不能为空");
        }

        log.info("刷新Token: tokenLength={}", token.length());
        String newToken = authService.refreshToken(token);

        return R.ok("Token刷新成功", newToken);
    }

    /**
     * 获取当前登录用户ID
     * <p>
     * 从Spring Security上下文中获取当前认证用户的ID
     * </p>
     *
     * @return 用户ID
     * @throws BusinessException 未登录或身份验证失败时抛出
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 校验认证对象
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("用户未认证");
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            log.warn("Principal为空");
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 支持Long类型的principal
        if (principal instanceof Long id) {
            return id;
        }

        // 支持String类型的principal（尝试转换为Long）
        if (principal instanceof String stringId) {
            try {
                return Long.parseLong(stringId);
            } catch (NumberFormatException e) {
                log.error("无法将principal转换为Long: principal={}", stringId);
                throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "用户身份格式错误");
            }
        }

        // 不支持的principal类型
        log.error("不支持的principal类型: {}", principal.getClass().getName());
        throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "未获取到用户身份");
    }
}
