package cn.masu.dcs.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * <p>
 * 用于生成、解析和验证JWT Token
 * 使用JJWT库实现，采用HS256算法签名
 * </p>
 * <p>
 * 主要功能：
 * 1. 生成Token
 * 2. 从Token中提取用户信息
 * 3. 验证Token有效性
 * 4. 检查Token是否过期
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * JWT签名密钥
     * 注意：生产环境必须使用强密码，至少256位
     */
    @Value("${jwt.secret:your-256-bit-secret-your-256-bit-secret-your-256-bit-secret}")
    private String secret;

    /**
     * Token过期时间（毫秒）
     * 默认：86400000ms = 24小时
     */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * 刷新Token的过期时间（毫秒）
     * 默认：604800000ms = 7天
     */
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    /**
     * Claims中的用户ID键
     */
    private static final String CLAIM_KEY_USER_ID = "userId";

    /**
     * Claims中的用户名键
     */
    private static final String CLAIM_KEY_USERNAME = "username";

    /**
     * Claims中的Token版本键
     */
    private static final String CLAIM_KEY_TOKEN_VERSION = "tokenVersion";

    /**
     * 获取签名密钥
     *
     * @return SecretKey对象
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成访问Token
     *
     * @param userId       用户ID
     * @param username     用户名
     * @param tokenVersion Token版本号（用于强制失效）
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username, Integer tokenVersion) {
        Map<String, Object> claims = new HashMap<>(4);
        claims.put(CLAIM_KEY_USER_ID, userId);
        claims.put(CLAIM_KEY_USERNAME, username);
        claims.put(CLAIM_KEY_TOKEN_VERSION, tokenVersion);
        return createToken(claims, username, expiration);
    }

    /**
     * 生成刷新Token
     * <p>
     * 刷新Token的有效期更长，用于获取新的访问Token
     * </p>
     *
     * @param userId       用户ID
     * @param username     用户名
     * @param tokenVersion Token版本号
     * @return 刷新Token字符串
     */
    public String generateRefreshToken(Long userId, String username, Integer tokenVersion) {
        Map<String, Object> claims = new HashMap<>(4);
        claims.put(CLAIM_KEY_USER_ID, userId);
        claims.put(CLAIM_KEY_USERNAME, username);
        claims.put(CLAIM_KEY_TOKEN_VERSION, tokenVersion);
        claims.put("type", "refresh");
        return createToken(claims, username, refreshExpiration);
    }

    /**
     * 创建Token
     *
     * @param claims     自定义声明
     * @param subject    主题（通常是用户名）
     * @param expiration 过期时间（毫秒）
     * @return JWT Token字符串
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get(CLAIM_KEY_USER_ID, Long.class);
        } catch (Exception e) {
            log.error("从Token中获取用户ID失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("从Token中获取用户名失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取Token版本
     *
     * @param token JWT Token
     * @return Token版本号
     */
    public Integer getTokenVersionFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get(CLAIM_KEY_TOKEN_VERSION, Integer.class);
        } catch (Exception e) {
            log.error("从Token中获取版本号失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取所有Claims
     *
     * @param token JWT Token
     * @return Claims对象
     * @throws io.jsonwebtoken.JwtException 如果Token无效或过期
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证Token是否有效
     * <p>
     * 检查Token的签名和格式是否正确
     * </p>
     *
     * @param token JWT Token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.debug("Token已过期: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("不支持的Token: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Token格式错误: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Token签名验证失败: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("Token参数非法: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return false;
        }
    }

    /**
     * 检查Token是否过期
     *
     * @param token JWT Token
     * @return true-已过期，false-未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("检查Token过期状态失败", e);
            return true;
        }
    }

    /**
     * 获取Token的剩余有效时间（毫秒）
     *
     * @param token JWT Token
     * @return 剩余有效时间，如果已过期或无效则返回0
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(remaining, 0);
        } catch (Exception e) {
            log.error("获取Token剩余时间失败", e);
            return 0;
        }
    }

    /**
     * 刷新Token
     * <p>
     * 使用旧Token生成新Token，延长有效期
     * </p>
     *
     * @param token 旧的JWT Token
     * @return 新的JWT Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Long userId = claims.get(CLAIM_KEY_USER_ID, Long.class);
            String username = claims.getSubject();
            Integer tokenVersion = claims.get(CLAIM_KEY_TOKEN_VERSION, Integer.class);

            return generateToken(userId, username, tokenVersion);
        } catch (Exception e) {
            log.error("刷新Token失败", e);
            throw new RuntimeException("Token刷新失败", e);
        }
    }
}

