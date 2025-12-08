package cn.masu.dcs.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装
 * <p>
 * 用于封装API的统一返回格式
 * </p>
 * <p>
 * 响应结构：
 * - code: 状态码（200-成功，其他-失败）
 * - message: 提示信息
 * - data: 返回数据
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Data
public class R<T> implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * HTTP状态码常量
     */
    private static final int HTTP_OK = 200;
    private static final int HTTP_ERROR = 500;

    /**
     * 默认成功消息
     */
    private static final String DEFAULT_SUCCESS_MESSAGE = "操作成功";

    /**
     * 默认失败消息
     */
    private static final String DEFAULT_FAIL_MESSAGE = "操作失败";

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(HTTP_OK);
        r.setMessage(DEFAULT_SUCCESS_MESSAGE);
        return r;
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> R<T> ok(String message) {
        R<T> r = new R<>();
        r.setCode(HTTP_OK);
        r.setMessage(message);
        return r;
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 返回数据
     * @param <T>  数据类型
     * @return 响应对象
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(HTTP_OK);
        r.setMessage(DEFAULT_SUCCESS_MESSAGE);
        r.setData(data);
        return r;
    }

    /**
     * 成功响应（自定义消息和数据）
     *
     * @param message 提示信息
     * @param data    返回数据
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> R<T> ok(String message, T data) {
        R<T> r = new R<>();
        r.setCode(HTTP_OK);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    /**
     * 失败响应（默认消息）
     *
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> R<T> fail() {
        R<T> r = new R<>();
        r.setCode(HTTP_ERROR);
        r.setMessage(DEFAULT_FAIL_MESSAGE);
        return r;
    }

    /**
     * 失败响应（自定义消息）
     *
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> R<T> fail(String message) {
        R<T> r = new R<>();
        r.setCode(HTTP_ERROR);
        r.setMessage(message);
        return r;
    }

    /**
     * 失败响应（自定义状态码和消息）
     *
     * @param code    状态码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> R<T> fail(Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    /**
     * 失败响应（使用ErrorCode枚举）
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型
     * @return 响应对象
     */
    public static <T> R<T> fail(ErrorCode errorCode) {
        R<T> r = new R<>();
        r.setCode(errorCode.getCode());
        r.setMessage(errorCode.getMessage());
        return r;
    }
}

