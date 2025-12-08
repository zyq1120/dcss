package cn.masu.dcs.common.filter;

import cn.masu.dcs.common.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * <p>
 * 拦截所有请求，验证JWT Token并设置认证信息到Spring Security上下文
 * </p>
 * <p>
 * 工作流程：
 * 1. 从请求头中提取Token
 * 2. 验证Token有效性
 * 3. 提取用户信息
 * 4. 设置到Security上下文
 * 5. 继续过滤器链
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    /**
     * 白名单路径前缀
     */
    private static final String[] WHITE_LIST_PATHS = {
        "/api/auth/login",
        "/api/auth/register",
        "/swagger-ui/",
        "/v3/api-docs/",
        "/swagger-resources/",
        "/webjars/"
    };

    /**
     * Authorization请求头名称
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Bearer前缀
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Bearer前缀长度
     */
    private static final int BEARER_PREFIX_LENGTH = 7;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 📝 调试日志：打印请求信息
            if (log.isDebugEnabled()) {
                log.debug("=== JWT Filter Debug ===");
                log.debug("URI: {}", request.getRequestURI());
                log.debug("Method: {}", request.getMethod());

                // 打印所有请求头
                java.util.Enumeration<String> headerNames = request.getHeaderNames();
                StringBuilder headers = new StringBuilder();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    String headerValue = request.getHeader(headerName);
                    headers.append(headerName).append(": ").append(headerValue).append("; ");
                }
                log.debug("Headers: {}", headers.toString());
            }

            // 1. 从请求头中获取Token
            String token = extractTokenFromRequest(request);

            // 📝 调试日志：Token提取结果
            if (token != null) {
                log.debug("Token已提取: length={}, prefix={}...", token.length(),
                    token.length() > 20 ? token.substring(0, 20) : token);
            } else {
                log.debug("未找到Token，检查Authorization请求头");
            }

            // 2. 验证Token
            if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
                // 3. 从Token中提取用户信息
                Long userId = jwtUtils.getUserIdFromToken(token);
                String username = jwtUtils.getUsernameFromToken(token);

                if (userId != null && username != null) {
                    log.debug("JWT验证成功: userId={}, username={}", userId, username);

                    // 4. 创建认证对象并设置到Security上下文
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userId,  // principal - 主体（用户ID）
                            null,    // credentials - 凭证（密码，已验证后不需要）
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // authorities - 权限
                        );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 可选：将用户信息存入请求属性，方便Controller使用
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);
                } else {
                    log.warn("从Token中提取用户信息失败: userId={}, username={}", userId, username);
                }
            } else {
                log.debug("请求未携带有效Token: uri={}", request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("JWT认证失败: uri={}, error={}", request.getRequestURI(), e.getMessage());
            // 认证失败，清除上下文
            SecurityContextHolder.clearContext();
        }

        // 5. 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取Token
     * <p>
     * 支持两种格式:
     * 1. Authorization: Bearer <token>
     * 2. Authorization: <token>
     * </p>
     *
     * @param request HTTP请求
     * @return JWT Token字符串，未找到返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken)) {
            // 标准格式: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            if (bearerToken.startsWith(BEARER_PREFIX)) {
                return bearerToken.substring(BEARER_PREFIX_LENGTH);
            }
            // 简化格式: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            return bearerToken;
        }

        return null;
    }

    /**
     * 判断是否应跳过此过滤器
     * <p>
     * 对于登录、注册等公开接口，不需要JWT验证
     * </p>
     *
     * @param request HTTP请求
     * @return true-跳过此过滤器，false-执行此过滤器
     * @throws ServletException Servlet异常
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 检查是否在白名单中
        for (String whiteListPath : WHITE_LIST_PATHS) {
            if (path.startsWith(whiteListPath)) {
                log.debug("白名单路径，跳过JWT验证: {}", path);
                return true;
            }
        }

        return false;
    }
}

