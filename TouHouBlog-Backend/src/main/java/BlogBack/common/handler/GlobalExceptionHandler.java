package BlogBack.common.handler;


import BlogBack.common.constant.MessageConstant;
import BlogBack.common.exception.BaseException;
import BlogBack.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

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
    /**
     * 捕获sql异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        //sql中报错重复的字段时会显示 Duplicate entry 'xxx' for key 'xxx'
        String message = ex.getMessage();
        //用“ ”分割字符串，将重复的字段提取出来返回给前端
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String msg = split[2] + MessageConstant.ALREADY_EXISTS;//使用messageConstant中定义好的错误信息常量来返回错误信息
            return Result.error(msg);
        }else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);//否则返回未知错误
        }
    }
}
