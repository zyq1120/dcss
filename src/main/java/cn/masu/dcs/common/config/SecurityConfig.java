package cn.masu.dcs.common.config;

import cn.masu.dcs.common.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security配置
 * <p>
 * 配置内容：
 * 1. 密码加密器（BCrypt）
 * 2. 安全过滤器链
 * 3. JWT认证过滤器
 * 4. 无状态Session管理
 * 5. 请求授权规则
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码加密器
     * <p>
     * 使用BCrypt算法，安全性高，每次加密结果不同（内置随机盐）
     * </p>
     *
     * @return BCrypt密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS配置
     * <p>
     * 允许跨域请求，方便前端开发和调试
     * 生产环境建议配置具体的允许域名
     * </p>
     *
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许所有来源（开发环境）
        // 生产环境应改为具体域名，如：configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com"))
        configuration.setAllowedOriginPatterns(List.of("*"));

        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 允许的请求头
        configuration.setAllowedHeaders(List.of("*"));

        // 允许携带认证信息（cookies）
        configuration.setAllowCredentials(true);

        // 预检请求的有效期（秒）
        configuration.setMaxAge(3600L);

        // 允许暴露的响应头
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 安全过滤器链配置
     * <p>
     * 配置说明：
     * 1. 禁用CSRF - 因为使用JWT，不需要CSRF保护
     * 2. 无状态Session - JWT是无状态的，不使用Session
     * 3. 授权规则 - 配置哪些路径需要认证，哪些公开访问
     * 4. JWT过滤器 - 在UsernamePasswordAuthenticationFilter之前执行
     * </p>
     *
     * @param http HttpSecurity对象
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 启用CORS配置
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 禁用CSRF保护（使用JWT不需要CSRF）
            .csrf(AbstractHttpConfigurer::disable)

            // Session管理：无状态（STATELESS）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 授权规则配置
            .authorizeHttpRequests(auth -> auth
                // 认证相关接口：允许所有人访问
                .requestMatchers("/api/auth/**").permitAll()

                // AI处理接口：允许所有人访问
                .requestMatchers("/api/v1/ai/**").permitAll()

                // 健康检查接口：允许所有人访问
                .requestMatchers("/health").permitAll()

                // Swagger文档：允许所有人访问（生产环境建议关闭）
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // 错误页面：允许所有人访问
                .requestMatchers("/error").permitAll()

                // 其他所有请求：需要认证
                .anyRequest().authenticated()
            )

            // 添加JWT认证过滤器（在UsernamePasswordAuthenticationFilter之前）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

