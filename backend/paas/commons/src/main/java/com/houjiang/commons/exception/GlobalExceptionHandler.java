package com.houjiang.commons.exception;


import com.alibaba.fastjson2.JSON;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.domain.AuthErrorCode;
import com.houjiang.commons.errorcode.domain.SystemErrorCode;
import com.houjiang.commons.errorcode.domain.ValidationErrorCode;
import com.houjiang.commons.errorcode.handler.ErrorCodeHandlerSupport;
import com.houjiang.commons.util.ResponseResult;
import feign.FeignException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.security.sasl.AuthenticationException;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 全局的的异常拦截器（拦截所有的控制器）
 * @author Amos
 *
 * <p>统一错误码改造说明（v1.2.0）：本类原先大量使用裸数字（404 / 11009 / 11003 / 10000 / 10001）
 * 以及语义不明的通用失败码，并且把 {@code exception.getCause().getMessage()} 直接放进响应体 ——
 * 那里面可能是 SQL 片段、内部类名、堆栈原文。本项目要开源，任何人拿到一个错误码都要能去
 * 《public/docs/错误码体系/码表.md》查到「这是什么、怎么解决」，所以本轮做了两件事：
 *
 * <ol>
 *   <li>每个分支直接用 {@link ErrorCodeHandlerSupport#of(ErrorCodeDef)} 出统一新码
 *       （码值域 20001-99999，与存量旧码最大 18040 完全隔离）。
 *       v1.3.0 第二阶段重构后，业务异常自带的码值<b>本身就是统一码</b>
 *       （{@code BizCodeEnume} / {@code RiderCodeEnume} / {@code GlobalExceptionEnum}
 *       已改为从统一码取值），因此运行时不再做任何「旧码 → 新码」翻译，
 *       原 {@code render(旧码, 新码)} 与 {@code legacy-passthrough} 回退开关一并删除。</li>
 *   <li>响应 message 一律取新码的 {@code userMessage}（中文、可读、脱敏）；
 *       异常根因、SQL 原文、堆栈<b>只进日志</b>，绝不进响应。</li>
 * </ol>
 *
 * <p>业务自带码值的异常（{@link LoginToException} / {@link BizCoreException} / {@link DefinedException}）
 * <b>码值原样透传</b>（架构 §4.2 不换码），但文案一律过
 * {@link ErrorCodeHandlerSupport#safeMessage} 脱敏出口，防止业务异常里带上 SQL 或内部细节出屏。
 *
 * <p>线上回退：v1.3.0 起不再有码值开关。若需回退，回滚本次发版即可；
 * 文案脱敏是独立的修复项，回滚码值不会恢复「把 SQL 原文出屏」的旧行为。
 */
@RestControllerAdvice
@Order(-1)
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 所有异常信息
     *
     * @param exception
     * @return
     * @throws Exception
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public ResponseResult<?> exceptionHandler(Exception exception) {
        log.error("exceptionHandle [{}]", exception.toString());
        if (exception instanceof LoginToException le) {
            // 业务自带码值原样透传；文案过脱敏出口，异常原文只进日志
            log.error("exceptionHandler 登录异常根因（仅日志，不出屏）", exception);
            return ResponseResult.fail(le.getMessageCode(),
                    ErrorCodeHandlerSupport.safeMessage(exception.getMessage(), AuthErrorCode.TOKEN_EXPIRED));
        }
        log.error("exceptionHandler 根因（仅日志，不出屏）", exception);
        if (null != exception.getCause() && null != exception.getCause().getMessage()) {
            String causeMessage = exception.getCause().getMessage();
            if (causeMessage.contains("Error querying database")) {
                ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(SystemErrorCode.DB_ACCESS_FAILED);
                return ResponseResult.fail(render.code(), render.message());
            }
            // 旧写法把 causeMessage 直接放进响应；这里只进日志，出屏用统一码的中文文案
            log.error("exceptionHandler 未分类根因（仅日志，不出屏）：{}", causeMessage);
            ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(SystemErrorCode.UNKNOWN_ERROR);
            return ResponseResult.fail(render.code(), render.message());
        }
        ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(SystemErrorCode.UNKNOWN_ERROR);
        return ResponseResult.fail(render.code(), render.message());
    }

    /**
     * 处理资源未找到异常
     *
     * @param e SQL语法错误异常
     * @return 返回错误响应结果
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseResult<?> badSqlGrammarException(NoResourceFoundException e) {
        log.error("badSqlGrammarException 路由未命中，根因见日志", e);
        // 旧裸码 404 在 commons 与 bff-web 两个 handler 里各自定义过，统一收口到 SYSTEM 域 20001
        ErrorCodeHandlerSupport.ErrorRenderResult render =
                ErrorCodeHandlerSupport.of(SystemErrorCode.ROUTE_NOT_FOUND);
        return ResponseResult.fail(render.code(), render.message());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseResult<?> handleDuplicateKeyException(DuplicateKeyException e) {
        // 重复键详情（表名、索引名、冲突值）只进日志
        log.error("handleDuplicateKeyException 唯一键冲突，详情只进日志", e);
        ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(SystemErrorCode.SYSTEM_DUPLICATE_KEY);
        return ResponseResult.fail(render.code(), render.message());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseResult<?> authenticationExceptionException(DuplicateKeyException e) {
        log.error("authenticationExceptionException 凭证校验失败，根因见日志", e);
        // 旧文案 BizCodeEnume.TOKEN_IS_INVALID =「token失效,请重新登陆」含技术术语，改为用户可理解文案
        ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(AuthErrorCode.TOKEN_ILLEGAL);
        return ResponseResult.fail(render.code(), render.message());
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = DefinedException.class)
    public ResponseResult<String> baseExceptionHandler(DefinedException e) {
        log.error("发生业务异常！根因见日志：[{}]", e.getMessage(), e);
        // 码值透传业务自带码；文案脱敏，异常原文只进日志
        return ResponseResult.fail(e.getMessageCode(),
                ErrorCodeHandlerSupport.safeMessage(e.getMessage(), SystemErrorCode.UNKNOWN_ERROR));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseResult<?> expiredJwtException(DuplicateKeyException e) {
        log.error("expiredJwtException 凭证已过期，根因见日志", e);
        ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(AuthErrorCode.TOKEN_EXPIRED);
        return ResponseResult.fail(render.code(), render.message());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult methodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 获取所有字段验证错误
        List<org.springframework.validation.FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        
        if (fieldErrors.isEmpty()) {
            log.error("methodArgumentNotValidException [{}]", e);
            // 旧写法用 CommonStatusEnum.FAIL(0)：0 是通用失败码，语义完全不明，前端无法按码分支
            ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(ValidationErrorCode.PARAM_INVALID);
            return ResponseResult.fail(render.code(), render.message());
        }
        
        // 构建详细的错误信息
        Map<String, List<String>> errorMap = new HashMap<>();
        StringBuilder errorMessage = new StringBuilder();
        
        for (org.springframework.validation.FieldError fieldError : fieldErrors) {
            String field = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            
            // 收集每个字段的错误消息
            errorMap.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
            
            // 构建错误消息字符串（用于向后兼容）
            if (errorMessage.length() > 0) {
                errorMessage.append("; ");
            }
            errorMessage.append(field).append(": ").append(message);
        }
        
        log.error("methodArgumentNotValidException [{}], 错误详情: {}", e, errorMap);
        
        // 返回第一个字段的错误消息（保持向后兼容）
        String firstMessage = fieldErrors.get(0).getDefaultMessage();
        
        // 将详细的错误信息放到data字段中；码值取 VALIDATION 域 21002，message 仍取字段级文案并过脱敏出口
        ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(ValidationErrorCode.PARAM_INVALID);
        return ResponseResult.fail(render.code(), ErrorCodeHandlerSupport.safeMessage(firstMessage, ValidationErrorCode.PARAM_INVALID), errorMap);
    }

    /** 自定义校验报错 */
    @ExceptionHandler(ValidationException.class)
    public ResponseResult<?> validationException(ValidationException e) {
        log.error("validationException 参数校验失败，根因见日志", e);
        ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(ValidationErrorCode.PARAM_INVALID);
        return ResponseResult.fail(render.code(), render.message());
    }

    /**
     * 自定义异常
     */
    @ExceptionHandler(value = BizCoreException.class)
    public ResponseResult<Void> bizCoreException(BizCoreException e) {
        log.error("自定义异常！根因见日志：[{}]", e.getMessage(), e);
        // 码值透传业务自带码；文案脱敏
        return ResponseResult.fail(e.getCode(),
                ErrorCodeHandlerSupport.safeMessage(e.getMessage(), SystemErrorCode.UNKNOWN_ERROR));
    }

    /**
     * 处理运行异常
     *
     * @param e 安全异常
     * @return 返回错误响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<?> runtimeException(RuntimeException e) {
        // 旧写法 e.printStackTrace()：开源项目满屏堆栈打到 stdout，改为只进日志
        log.error("runtimeException 根因（仅日志，不出屏）", e);
        if (e instanceof DataAccessException) {
            // 旧裸码 11009 在 BizCodeEnume 里其实是「账号尚未审核通过」，handler 复用它是历史错误
            ErrorCodeHandlerSupport.ErrorRenderResult render =
                    ErrorCodeHandlerSupport.of(SystemErrorCode.DB_ACCESS_FAILED);
            return ResponseResult.fail(render.code(), render.message());
        }
        if ("try to proceed invocation error".equals(e.getMessage())) {
            // 这里可以记录日志或进行其他处理
            log.error("runtimeException 调用链根因（仅日志，不出屏）", e);
            // 如果e.getCause()是BadSqlGrammarException, 重新抛出
            if (e.getCause() instanceof BadSqlGrammarException || e.getCause() instanceof SQLSyntaxErrorException
                    || e.getCause() instanceof SQLException) {
                // 旧写法返回 "syntax error"（英文，开源者看不懂）；SQL 原文只进日志
                log.error("SQL 语法异常根因（仅日志，不出屏）：{}", e.getCause().getMessage());
                ErrorCodeHandlerSupport.ErrorRenderResult render =
                        ErrorCodeHandlerSupport.of(SystemErrorCode.SQL_GRAMMAR_ERROR);
                return ResponseResult.fail(render.code(), render.message());
            }
            if (e.getCause() instanceof LoginToException le) {
                // 处理登录相关异常：码值透传，文案脱敏
                return ResponseResult.fail(le.getMessageCode(),
                        ErrorCodeHandlerSupport.safeMessage(le.getMessage(), AuthErrorCode.TOKEN_EXPIRED));
            }
            if (e.getCause() instanceof FeignException fe) {
                String body = fe.getMessage();
                int startIndex = body == null ? -1 : body.indexOf("{\"code");
                if (startIndex >= 0) {
                    int endIndex = body.indexOf("}", startIndex) + 1;
                    if (endIndex > startIndex) {
                        // 上游已返回标准响应体，原样透传
                        return JSON.parseObject(body.substring(startIndex, endIndex), ResponseResult.class);
                    }
                }
                // 上游响应体不是标准结构：按上游调用失败出统一码，绝不回显原始报文
                log.error("Feign 响应体非标准结构，原始报文只进日志：{}", body);
                ErrorCodeHandlerSupport.ErrorRenderResult render =
                        ErrorCodeHandlerSupport.of(SystemErrorCode.SYSTEM_UPSTREAM_CALL_FAILED);
                return ResponseResult.fail(render.code(), render.message());
            }
            if (e.getCause() instanceof BindException be) {
                ObjectError objectError = be.getBindingResult().getAllErrors().get(0);
                // 参数绑定失败：码值取 VALIDATION 域 21002，字段级文案保留并过脱敏出口
                ErrorCodeHandlerSupport.ErrorRenderResult render =
                        ErrorCodeHandlerSupport.of(ValidationErrorCode.PARAM_INVALID);
                return ResponseResult.fail(render.code(), ErrorCodeHandlerSupport.safeMessage(objectError.getDefaultMessage(), ValidationErrorCode.PARAM_INVALID));
            }
            if (e.getCause() instanceof DuplicateKeyException) {
                // 旧写法 ResponseResult.fail(String) 走的是泛型重载：码值其实是 0，文案被塞进了 data 字段
                ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(SystemErrorCode.SYSTEM_DUPLICATE_KEY);
                return ResponseResult.fail(render.code(), render.message());
            }
            if (e.getCause() instanceof TooManyResultsException) {
                // 处理查询结果过多异常：旧裸码 10000 在 BizCodeEnume 里是「系统未知异常」，语义不明
                ErrorCodeHandlerSupport.ErrorRenderResult render =
                        ErrorCodeHandlerSupport.of(SystemErrorCode.TOO_MANY_RESULTS);
                return ResponseResult.fail(render.code(), render.message());
            }
            if (e.getCause() instanceof MyBatisSystemException) {
                // 处理其他MyBatis异常：旧裸码 10001 在 BizCodeEnume 里是「参数格式异常」，属于错用
                ErrorCodeHandlerSupport.ErrorRenderResult render =
                        ErrorCodeHandlerSupport.of(SystemErrorCode.MYBATIS_ERROR);
                return ResponseResult.fail(render.code(), render.message());
            }
            if (e.getCause() instanceof DefinedException de) {
                // 处理自定义异常：码值透传，文案脱敏
                return ResponseResult.fail(de.getMessageCode(),
                        ErrorCodeHandlerSupport.safeMessage(de.getMessage(), SystemErrorCode.UNKNOWN_ERROR), null);
            }
        }
        // 兜底：根因只进日志并做空值保护（旧写法 e.getCause().getMessage() 在 cause 为 null 时会 NPE）
        log.error("runtimeException 兜底根因（仅日志，不出屏）：{}", rootCauseText(e));
        ErrorCodeHandlerSupport.ErrorRenderResult render =
                ErrorCodeHandlerSupport.of(SystemErrorCode.UNKNOWN_ERROR);
        return ResponseResult.fail(render.code(), render.message());
    }

    /**
     * 提取可读的根因文本（只用于日志），对 null 做全路径空值保护。
     *
     * @param e 异常
     * @return 根因描述，绝不返回 null
     */
    private static String rootCauseText(Throwable e) {
        if (e == null) {
            return "";
        }
        String message = e.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }
        Throwable cause = e.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return cause.getMessage();
        }
        return e.getClass().getName();
    }

}
