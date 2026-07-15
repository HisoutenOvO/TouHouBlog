package BlogBack.common.exception;

public class CategoryNotExistException extends BaseException {
    public CategoryNotExistException() {}
    public CategoryNotExistException(String message) {
        super(message);
    }
}
