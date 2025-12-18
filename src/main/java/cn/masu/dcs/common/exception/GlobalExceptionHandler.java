package cn.masu.dcs.common.exception;

import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理系统中的各类异常，返回标准的错误响应格式
 * </p>
 * <p>
 * 支持的异常类型：
 * 1. BusinessException - 业务异常
 * 2. MethodArgumentNotValidException - 参数校验异常
 * 3. BindException - 参数绑定异常
 * 4. IllegalArgumentException - 非法参数异常
 * 5. AuthenticationException - 认证异常
 * 6. AccessDeniedException - 授权异常
 * 7. Exception - 系统异常
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     * <p>
     * 处理自定义的业务异常，返回对应的错误码和错误信息
     * </p>
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理
     * <p>
     * 处理@Valid或@Validated注解的参数校验失败异常
     * </p>
     *
     * @param e 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.error("参数校验失败: {}", message);
        return R.fail(ErrorCode.PARAM_ERROR.getCode(), "参数校验失败: " + message);
    }

    /**
     * 参数绑定异常处理
     * <p>
     * 处理表单参数绑定失败异常
     * </p>
     *
     * @param e 绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public R<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.error("参数绑定失败: {}", message);
        return R.fail(ErrorCode.PARAM_ERROR.getCode(), "参数绑定失败: " + message);
    }

    /**
     * 非法参数异常处理
     * <p>
     * 处理IllegalArgumentException异常
     * </p>
     *
     * @param e 非法参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public R<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数: {}", e.getMessage());
        return R.fail(ErrorCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 认证异常处理
     * <p>
     * 处理Spring Security的认证异常（未登录或Token无效）
     * </p>
     *
     * @param e 认证异常
     * @return 错误响应
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleAuthenticationException(AuthenticationException e) {
        log.error("认证失败: {}", e.getMessage());
        return R.fail(ErrorCode.UNAUTHORIZED.getCode(), "认证失败，请重新登录");
    }

    /**
     * 授权异常处理
     * <p>
     * 处理Spring Security的授权异常（无权限访问）
     * </p>
     *
     * @param e 授权异常
     * @return 错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("授权失败: {}", e.getMessage());
        return R.fail(ErrorCode.FORBIDDEN.getCode(), "无权访问该资源");
    }

    /**
     * 系统异常处理
     * <p>
     * 处理所有未被捕获的异常，作为兜底处理
     * </p>
     *
     * @param e 系统异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return R.fail(ErrorCode.SYSTEM_ERROR.getCode(), "系统繁忙，请稍后重试");
    }
}
