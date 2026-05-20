package BlogBack.common.exception;

/**
 * 用于处理所有重名异常
 */
public class NameHasBeenUsedException extends BaseException {
    public NameHasBeenUsedException(){}
    public NameHasBeenUsedException(String message) {
        super(message);
    }
}
