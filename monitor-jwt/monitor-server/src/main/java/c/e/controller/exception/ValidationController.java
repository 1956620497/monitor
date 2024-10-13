package c.e.controller.exception;

import c.e.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//处理异常，返回一个自定义的错误信息
@Slf4j       //日志注解
@RestControllerAdvice
public class ValidationController {

    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validateException(ValidationException exception){
        log.warn("Resolve[{}:{}]",exception.getClass().getName(),exception.getMessage());
        return RestBean.failure(400,"请求参数有误");
    }

}
