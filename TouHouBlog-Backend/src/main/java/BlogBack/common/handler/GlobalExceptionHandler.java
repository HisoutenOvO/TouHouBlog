package BlogBack.common.handler;


import BlogBack.common.exception.BaseException;
import BlogBack.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = ("BlogBack.controller"))
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 全局异常处理
     * @param e
     * @return
     */
    @ExceptionHandler
    public Result globalExceptionHandler(BaseException e){
        log.error("异常信息发生：{}",e.getMessage());
        return Result.error(e.getMessage());
    }

}
