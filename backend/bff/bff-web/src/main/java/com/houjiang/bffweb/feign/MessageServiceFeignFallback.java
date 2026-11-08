package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * @author Amos
 */
@Component
@Slf4j
public class MessageServiceFeignFallback implements FallbackFactory<MessageServiceFeign> {

    @Override
    public MessageServiceFeign create(Throwable cause) {
        return (MessageServiceFeign) Proxy.newProxyInstance(
                MessageServiceFeign.class.getClassLoader(),
                new Class<?>[]{MessageServiceFeign.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return method.invoke(this, args);
                    }
                    return response(cause, method.getName());
                });
    }

    public ResponseResult response(Throwable cause, String method) {
        ErrorResponse error = classify(cause);
        log.error(
                "MessageServiceFeign 调用失败 method={} code={} failureType={}",
                method, error.code(), failureType(cause));
        return ResponseResult.fail(error.code(), error.message());
    }

    private ErrorResponse classify(Throwable cause) {
        Integer status = null;
        boolean timeout = false;
        Throwable current = cause;
        while (current != null) {
            if (current instanceof SocketTimeoutException || current instanceof TimeoutException) {
                timeout = true;
            }
            if (current instanceof FeignException feignException && feignException.status() > 0) {
                status = feignException.status();
                break;
            }
            current = current.getCause();
        }
        if (timeout) {
            return new ErrorResponse(504, "消息服务响应超时，请稍后重试");
        }
        if (status == null) {
            return new ErrorResponse(503, "消息服务暂时不可用，请稍后重试");
        }
        return switch (status) {
            case 403 -> new ErrorResponse(403, "无权访问消息数据");
            case 404 -> new ErrorResponse(404, "消息数据不存在");
            case 429 -> new ErrorResponse(429, "消息服务请求过于频繁，请稍后重试");
            default -> status >= 500
                    ? new ErrorResponse(503, "消息服务暂时不可用，请稍后重试")
                    : new ErrorResponse(status, "消息服务请求失败");
        };
    }

    private String failureType(Throwable cause) {
        return cause == null ? "unknown" : cause.getClass().getSimpleName();
    }

    private record ErrorResponse(int code, String message) {
    }
}
