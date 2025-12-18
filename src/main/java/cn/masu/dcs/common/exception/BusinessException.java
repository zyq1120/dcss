package cn.masu.dcs.common.exception;

import cn.masu.dcs.common.result.ErrorCode;
import lombok.Getter;

/**
 * 自定义业务异常
 * @author zyq
 */
@Getter
public class BusinessException extends RuntimeException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final Integer code;

    private final String message;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ErrorCode errorCode, String errorMessage) {
        super(errorMessage);
        this.code = errorCode.getCode();
        this.message = errorMessage;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ErrorCode errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.code = errorCode.getCode();
        this.message = errorMessage;
    }
}
