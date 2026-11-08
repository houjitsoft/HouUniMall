package com.houjiang.merchant.common.exception;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.houjiang.commons.errorcode.domain.SystemErrorCode;
import com.houjiang.commons.errorcode.domain.ValidationErrorCode;
import com.houjiang.commons.errorcode.handler.ErrorCodeHandlerSupport;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.exception.GlobalExceptionEnum;
import com.houjiang.commons.exception.UserDefinedException;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.enums.ErrorEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.util.ArrayList;

/**
 * 全局异常处理
 * @author Amos
 *
 * <p>统一错误码改造说明（v1.2.0）：本类的码值本来就取自 {@link ErrorEnum} 常量，没有裸码，
 * 但有三处把 Spring 的英文内部原文（如 {@code Required String parameter 'x' is not present}）
 * 或请求路径直接放进响应体出屏。本项目要开源，这些既看不懂也有信息泄露风险，
 * 本轮统一改为「响应出统一码的中文 userMessage，原文只进日志」。
 */
@Slf4j
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalException {

    /**
     * 处理所有不可知异常
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public AjaxResult<Object> handleException(Exception e) {
        log.error("系统异常", e);
        return AjaxResult.failed(ErrorEnum.SYSTEM_ERROR.getCode(), ErrorEnum.SYSTEM_ERROR.getMsg());
    }

    /**
     * 拦截404异常
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public AjaxResult<Object> handleNoHandlerFoundException(NoHandlerFoundException e){
        // 请求路径只进日志，出屏用统一码的中文文案
        log.warn("路由未命中，路径只进日志：{}", e.getRequestURL());
        ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(SystemErrorCode.ROUTE_NOT_FOUND);
        return AjaxResult.failed(render.code(), render.message());
    }

    /**
     * 拦截自定义抛出异常
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public AjaxResult<Object> handleException(BaseException e) {
        int code = e.getCode();
        String msg = e.getMsg();
        int show = e.getShow();
        //return AjaxResult.failed(code, msg);
        return AjaxResult.failed(code, msg, new ArrayList<>(), show);
    }

    /**
     * 拦截表单参数校验FROM
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public AjaxResult<Object> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        // 字段级文案保留（DTO 注解里写死），但过脱敏出口；顺带消除 Objects.requireNonNull 的 NPE 风险
        ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(ValidationErrorCode.PARAM_INVALID);
        String msg = ErrorCodeHandlerSupport.safeMessage(
                bindingResult.getFieldError() == null ? null : bindingResult.getFieldError().getDefaultMessage(),
                ValidationErrorCode.PARAM_INVALID);
        return AjaxResult.failed(render.code(), msg);
    }

    /**
     * 拦截路径参数校验PATH
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public AjaxResult<Object> handlePathException(MissingServletRequestParameterException e) {
        // 旧写法把 Spring 的英文原文（Required String parameter 'x' is not present）直接出屏，改为只进日志
        log.warn("缺少请求参数，原文只进日志：{}", e.getMessage());
        ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(ValidationErrorCode.PARAM_INVALID);
        return AjaxResult.failed(render.code(), render.message());
    }

    /**
     * 拦截JSON参数校验
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public AjaxResult<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        // 字段级文案保留（DTO 注解里写死），但过脱敏出口；顺带消除 Objects.requireNonNull 的 NPE 风险
        ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(ValidationErrorCode.PARAM_INVALID);
        String msg = ErrorCodeHandlerSupport.safeMessage(
                bindingResult.getFieldError() == null ? null : bindingResult.getFieldError().getDefaultMessage(),
                ValidationErrorCode.PARAM_INVALID);
        return AjaxResult.failed(render.code(), msg);
    }

    /**
     * 拦截参数类型不正确
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public AjaxResult<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Integer code = ErrorEnum.PARAMS_TYPE_ERROR.getCode();
        return AjaxResult.failed(code, ErrorEnum.PARAMS_TYPE_ERROR.getMsg());
    }

    /**
     * 拦截请求方法
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public AjaxResult<Object> handleRequestMethodException(HttpRequestMethodNotSupportedException e) {
        // 旧写法把 Spring 的英文原文直接出屏，改为只进日志
        log.warn("请求方法不支持，原文只进日志：{}", e.getMessage());
        ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(ValidationErrorCode.METHOD_NOT_ALLOWED);
        return AjaxResult.failed(render.code(), render.message());
    }

    /**
     * 拦截断言异常
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public AjaxResult<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        Integer code = ErrorEnum.ASSERT_ARGUMENT_ERROR.getCode();
        return AjaxResult.failed(code, ErrorEnum.ASSERT_ARGUMENT_ERROR.getMsg());
    }

    /**
     * 拦截基础设施层抛出的 {@link DefinedException}（JWT 过滤器最典型）。
     *
     * <p>此前该异常没有专属分支，只会落到 {@link #handleException(Exception)} 被包装成
     * 「系统错误」（code=0）：未登录 / 登录过期都会被前端当成未知错误弹红条。
     * 这里按异常自带的统一码原样出屏，前端才能按 30802 / 30803 做静默登录引导。
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(DefinedException.class)
    @ResponseBody
    public AjaxResult<Object> handleDefinedException(DefinedException e) {
        log.warn("业务异常 DefinedException：code={}, msg={}", e.getMessageCode(), e.getDetailMessage());
        return AjaxResult.failed(e.getMessageCode(), e.getDetailMessage());
    }

    /**
     * 拦截 {@link UserDefinedException}（码值取自 {@link GlobalExceptionEnum}）。
     *
     * <p>同上：没有该分支时入驻链路的「请先登录后再操作」会被兜底成「系统错误」。
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(UserDefinedException.class)
    @ResponseBody
    public AjaxResult<Object> handleUserDefinedException(UserDefinedException e) {
        GlobalExceptionEnum errorCode = e.getErrorCode();
        if (errorCode == null) {
            log.error("业务异常 UserDefinedException 缺少错误码", e);
            return AjaxResult.failed(ErrorEnum.SYSTEM_ERROR.getCode(), ErrorEnum.SYSTEM_ERROR.getMsg());
        }
        log.warn("业务异常 UserDefinedException：code={}, msg={}", errorCode.getErrorCode(), errorCode.getErrorMsg());
        return AjaxResult.failed(errorCode.getErrorCode(), errorCode.getErrorMsg());
    }

    /**
     * 拦截MybatisPlus异常
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MybatisPlusException.class)
    @ResponseBody
    public AjaxResult<Object> handleMybatisPlusException(MybatisPlusException e) {
        log.error("系统异常", e);
        return AjaxResult.failed(ErrorEnum.SYSTEM_ERROR.getCode(), ErrorEnum.SYSTEM_ERROR.getMsg());
    }

}
