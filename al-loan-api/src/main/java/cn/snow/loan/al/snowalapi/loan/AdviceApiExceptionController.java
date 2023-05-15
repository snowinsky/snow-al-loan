package cn.snow.loan.al.snowalapi.loan;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cn.snow.loan.al.snowalapi.loan.entity.ResponseData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@ResponseBody
public class AdviceApiExceptionController {

    // 使用form data方式调用接口，校验异常抛出 BindException
    // 使用 json 请求体调用接口，校验异常抛出 MethodArgumentNotValidException
    // 单个参数校验异常抛出ConstraintViolationException
    // 处理 json 请求体调用接口校验失败抛出的异常
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseData<List<String>> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> collect = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        log.warn("####<==Json parameter fail:{}", e.getMessage());
        return ResponseData.argumentfail(collect);
    }

    // 使用form data方式调用接口，校验异常抛出 BindException
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ResponseData<List<String>> bindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> collect = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        log.warn("####<==form data parameter fail:{}", e.getMessage());
        return ResponseData.argumentfail(collect);
    }


    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = HttpMessageConversionException.class)
    public ResponseData<String> httpMessageConversionExceptionHandler(final HttpMessageConversionException e) {
        log.warn("####<==Invalid http status:{}", e.getMessage());
        return ResponseData.argumentfail("Input Http Status Error:" + e.getMessage());
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseData<String> illegalArgumentExceptionHandler(final IllegalArgumentException e) {
        log.warn("####<==Invalid argument:{}", e.getMessage());
        return ResponseData.argumentfail("Input Argument Error:" + e.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseData<String> illegalStateExceptionHandler(final IllegalStateException e) {
        log.warn("####<==Invalid State:{}", e.getMessage());
        return ResponseData.bizfail("Execute Method Error:" + e.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = RuntimeException.class)
    public ResponseData<String> runtimeException(final RuntimeException e) {
        log.warn("####<==channel pay API return failure message:{}", e.getMessage());
        return ResponseData.bizfail("Internal server error:" + e.getMessage());
    }
}
