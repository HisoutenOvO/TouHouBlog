package BlogBack.common.exception;

/**
 * 自定义业务异常，使全局异常处理器只捕获业务异常而非系统异常
 */
public class BaseException extends RuntimeException {
    public BaseException(){}
    public BaseException(String message) {
        super(message);
    }
}
