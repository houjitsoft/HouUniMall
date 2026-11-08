package com.houjiang.bffweb.config;

import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.commons.errorcode.domain.SystemErrorCode;
import com.houjiang.commons.errorcode.domain.UpstreamErrorCode;
import com.houjiang.commons.errorcode.domain.ValidationErrorCode;
import com.houjiang.commons.errorcode.handler.ErrorCodeHandlerSupport;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;

/**
 * bff-web 全局异常处理器。
 *
 * <p>解决 Feign 调用下游 paas 服务失败时（如下游进程未启动 / 编码失败 / 反序列化失败），
 * bff-web 没有兜底拦截，导致 Spring Boot 直接返回标准 500 错误页 + 不可读的 JSON。
 *
 * <p>关键设计：所有 Feign / 通用异常均返回 HTTP 200 + 业务码，方便前端统一处理；
 *            同时把异常信息打到日志里，便于排查。
 *
 * <p>统一错误码改造说明（v1.2.0）：本类原先返回裸码 5001 / 404 / 400 / 5000，
 * 并且把 {@code simpleCause(e)}（最长 300 字符的异常根因，可能含 SQL、类名、内部路径）
 * 拼进响应体直接出屏。本项目要开源，响应里出现这些内部细节既难堪又有泄露风险，因此本轮：
 * <ol>
 *   <li>裸码全部换成统一新码（{@link ErrorCodeHandlerSupport#render} 产出，码值域 20001-99999）；</li>
 *   <li>响应 message 一律取新码的 {@code userMessage}（中文、可读、脱敏），
 *       {@code simpleCause(e)} 与内部 path <b>只进日志</b>；</li>
 *   <li>原先针对 {@code web/oa/attendance} / {@code web/oa/salary} 的两条超长排障提示
 *       降级为日志提示 —— 那是给开发看的，不该出现在用户屏幕上。</li>
 * </ol>
 *
 * <p>依赖说明：bff-web 依赖 paas-commons，但用 {@code <exclusions>*:</exclusions>} 排除了全部传递依赖
 * （commons 有 60+ 重依赖，会把网关撑爆并与 bff-web 的 security/openfeign 版本冲突）。
 * bff-web 只用到了 commons 里零依赖的 {@code com.houjiang.commons.errorcode} 包。
 * 另注：bff-web 的 {@code @SpringBootApplication} 未声明 scanBasePackages，默认只扫
 * {@code com.houjiang.bffweb}，不会扫到 {@code com.houjiang.commons} 的任何组件。
 *
 * @author Amos
 * @since 2026-05-21
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截 Feign 调用下游服务时抛出的所有异常。
     *
     * @param e FeignException（如连接拒绝、超时、4xx、5xx）
     * @return ResponseResult 业务统一格式
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(FeignException.class)
    @ResponseBody
    public ResponseResult<Object> handleFeignException(FeignException e) {
        int status = e.status();
        // 旧写法把 simpleCause(e) 拼进响应；这里根因只进日志
        log.error("[BFF Feign 调用失败] status={} 根因（仅日志，不出屏）={}", status, simpleCause(e), e);
        // status == -1 表示下游不可达（旧裸码 5001 的两个语义在这里拆开，故用 render 显式指定）
        ErrorCodeHandlerSupport.ErrorRenderResult render = status == -1
                ? ErrorCodeHandlerSupport.of(UpstreamErrorCode.SERVICE_UNREACHABLE)
                : ErrorCodeHandlerSupport.of(UpstreamErrorCode.UPSTREAM_CALL_FAILED);
        return ResponseResult.fail(render.code(), render.message());
    }

    /**
     * Spring Boot 3.4+ 无匹配 Controller 时会走静态资源查找并抛此异常（含误用 GET 打开 /web/oa/* API）。
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseBody
    public ResponseResult<Object> handleNoResourceFound(NoResourceFoundException e) {
        String path = e.getResourcePath() != null ? e.getResourcePath() : "";
        // 内部 path 与排障提示只进日志，绝不回显（旧写法把 "接口不存在: " + path 直接出屏）
        log.warn("[BFF 路由未命中] path={}；排查提示：数据接口请由页面内 POST 调用，"
                + "勿在地址栏直接打开 /web/…；请确认前端路由与后端 Controller 映射同步，"
                + "且 bff-web 已重启到最新版本", path);
        if (path.contains("web/oa/attendance") || path.contains("web/oa/salary")) {
            log.warn("[BFF 路由未命中·OA 专项] 考勤请访问 /oa/attendance/lists，"
                    + "薪资请访问 /oa/salary/payment 等前端路由，数据接口由页面内 POST 调用");
        }
        ErrorCodeHandlerSupport.ErrorRenderResult render =
                ErrorCodeHandlerSupport.of(SystemErrorCode.ROUTE_NOT_FOUND);
        return ResponseResult.fail(render.code(), render.message());
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseResult<Object> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        String supported = e.getSupportedHttpMethods() != null
                ? Arrays.toString(e.getSupportedHttpMethods().toArray())
                : "POST";
        // 允许的方法列表只进日志，不回显
        log.warn("[BFF 请求方法错误] method={} 需要 {}", e.getMethod(), supported);
        ErrorCodeHandlerSupport.ErrorRenderResult render =
                ErrorCodeHandlerSupport.of(ValidationErrorCode.METHOD_NOT_ALLOWED);
        return ResponseResult.fail(render.code(), render.message());
    }

    /**
     * 拦截其它未捕获的运行时异常，避免落到 Spring Boot 默认 500 页。
     *
     * @param e 任意异常
     * @return ResponseResult 业务统一格式
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult<Object> handleException(Exception e) {
        // 旧写法 ResponseResult.fail(5000, "服务端异常：" + simpleCause(e))：异常根因直接出屏
        log.error("[BFF 未捕获异常] {}", e.getMessage(), e);
        log.error("[BFF 未捕获异常·根因（仅日志，不出屏）] {}", simpleCause(e));
        ErrorCodeHandlerSupport.ErrorRenderResult render =
                ErrorCodeHandlerSupport.of(SystemErrorCode.UNKNOWN_ERROR);
        return ResponseResult.fail(render.code(), render.message());
    }

    /**
     * 提取异常根因的简要描述，避免日志/响应过长。
     *
     * <p>本方法的返回值<b>只允许进日志</b>，不得放进响应体。
     *
     * @param e 异常
     * @return 简化描述
     */
    private String simpleCause(Throwable e) {
        Throwable cur = e;
        int depth = 0;
        while (cur.getCause() != null && cur.getCause() != cur && depth < 5) {
            cur = cur.getCause();
            depth++;
        }
        String msg = cur.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = cur.getClass().getSimpleName();
        }
        if (msg.length() > 300) {
            msg = msg.substring(0, 300) + "...";
        }
        return msg;
    }
}
