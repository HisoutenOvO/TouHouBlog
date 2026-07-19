package BlogBack.common.exception;

public class ArticleNotExistException extends RuntimeException {
    public ArticleNotExistException(){}
    public ArticleNotExistException(String message) {
        super(message);
    }
}
