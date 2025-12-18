package cn.masu.dcs.common.result;

import lombok.Getter;

/**
 * 错误码枚举
 * <p>
 * 定义系统中所有的错误码和错误信息
 * 错误码分类：
 * - 2xx: 成功
 * - 400: 参数错误
 * - 401: 未授权
 * - 403: 无权限
 * - 404: 资源不存在
 * - 500: 系统错误
 * - 1xxx: 用户相关业务错误
 * - 2xxx: 文件相关业务错误
 * - 3xxx: 模板相关业务错误
 * - 4xxx: AI处理相关业务错误
 * - 5xxx: 审计相关业务错误
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Getter
public enum ErrorCode {


    /** 操作成功 */
    SUCCESS(200, "操作成功"),

    /** 参数错误 */
    PARAM_ERROR(400, "参数错误"),

    /** 未授权 */
    UNAUTHORIZED(401, "未授权"),

    /** 无权限 */
    FORBIDDEN(403, "无权限"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 系统错误 */
    SYSTEM_ERROR(500, "系统错误"),

    // ==================== 用户相关错误码 (1xxx) ====================

    /** 用户不存在 */
    USER_NOT_FOUND(1001, "用户不存在"),

    /** 用户已存在 */
    USER_ALREADY_EXISTS(1002, "用户已存在"),

    /** 密码错误 */
    PASSWORD_ERROR(1003, "密码错误"),

    /** Token已过期 */
    TOKEN_EXPIRED(1004, "Token已过期"),

    /** Token无效 */
    TOKEN_INVALID(1005, "Token无效"),

    // ==================== 文件相关错误码 (2xxx) ====================

    /** 文件上传失败 */
    FILE_UPLOAD_ERROR(2001, "文件上传失败"),

    /** 文件不存在 */
    FILE_NOT_FOUND(2002, "文件不存在"),

    /** 文件类型不支持 */
    FILE_TYPE_ERROR(2003, "文件类型不支持"),

    /** 文件大小超限 */
    FILE_SIZE_ERROR(2004, "文件大小超限"),

    // ==================== 模板相关错误码 (3xxx) ====================

    /** 模板不存在 */
    TEMPLATE_NOT_FOUND(3001, "模板不存在"),

    /** 模板已禁用 */
    TEMPLATE_DISABLED(3002, "模板已禁用"),

    // ==================== AI处理相关错误码 (4xxx) ====================

    /** OCR识别失败 */
    OCR_PROCESS_ERROR(4001, "OCR识别失败"),

    /** NLP提取失败 */
    NLP_PROCESS_ERROR(4002, "NLP提取失败"),

    /** 数据提取失败 */
    EXTRACT_PROCESS_ERROR(4003, "数据提取失败"),

    // ==================== 审计相关错误码 (5xxx) ====================

    /** 审核操作失败 */
    AUDIT_ERROR(5001, "审核操作失败");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误信息
     */
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

