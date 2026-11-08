package com.houjiang.plugin.engine.host;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.plugin.api.host.CapabilityCallRequest;
import com.houjiang.plugin.api.host.CapabilityCallResponse;
import com.houjiang.plugin.api.host.PluginCapabilityCallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * standalone 插件能力客户端：把能力接口调用跨进程转发到插件自己的服务。
 *
 * <p><b>为什么必须跨进程</b>：activity 下每个活动都是独立插件（秒杀 / 限时折扣 /
 * 满减满折 / 优惠券…）。它们既可以 embedded 跑在宿主进程，也可以 standalone 独立部署。
 * 对 standalone 插件，Core 微服务（order / goods 等）与插件不在同一进程，
 * 只能走 HTTP；没有这一层，{@code PluginHostApi} 就只能在宿主内部用，
 * Core 想要插件能力就只剩硬编码 {@code @FeignClient("activity")} 一条路。
 *
 * <p><b>实现方式</b>：JDK 动态代理把接口方法转成
 * {@code POST {host}/api/plugin/capability}（契约见 {@link CapabilityCallRequest}），
 * 返回值按方法声明类型反序列化。这样调用方拿到的仍是一个普通 Java 接口，
 * 与 embedded 插件的用法完全一致——插件从 embedded 切到 standalone，Core 代码不用改。
 *
 * <p><b>超时</b>：连接与读取各 5 秒。插件不可用时应快速失败让 Core 走降级，
 * 不能把 Core 线程拖死——这正是闭坑清单 P0「Core 失去独立运行」要防的。
 *
 * @author HouCloud
 */
public class StandaloneCapabilityClient {

    /** 插件侧需暴露的能力调用端点。 */
    public static final String CAPABILITY_PATH = "/api/plugin/capability";

    private static final Logger log = LoggerFactory.getLogger(StandaloneCapabilityClient.class);

    private static final int TIMEOUT_MILLIS = 5_000;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public StandaloneCapabilityClient() {
        this(new RestTemplate(defaultRequestFactory()), new ObjectMapper());
    }

    public StandaloneCapabilityClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成能力接口的远程代理。
     *
     * @param host 可信服务发现解析的地址；禁止直接使用 plugin.yaml 中的 host URL
     * @param type 能力接口类型
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(String host, Class<T> type) {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            return call(host, type, method, args);
        };
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    Object call(String host, Class<?> capabilityType, Method method, Object[] args) {
        List<String> argTypes = new ArrayList<>();
        for (Class<?> parameterType : method.getParameterTypes()) {
            argTypes.add(parameterType.getName());
        }
        CapabilityCallRequest request = new CapabilityCallRequest(
                capabilityType.getName(),
                method.getName(),
                argTypes,
                args == null ? List.of() : List.of(args));

        String url = host + CAPABILITY_PATH;
        try {
            ResponseEntity<CapabilityCallResponse> response =
                    restTemplate.postForEntity(url, request, CapabilityCallResponse.class);
            CapabilityCallResponse body = response.getBody();
            if (body == null) {
                throw new PluginCapabilityCallException("插件返回空响应: " + url);
            }
            if (!body.ok()) {
                throw new PluginCapabilityCallException(
                        "插件能力调用失败: " + method.getName() + ", reason=" + body.error());
            }
            return convert(body.result(), method.getReturnType());
        } catch (PluginCapabilityCallException e) {
            log.warn("[plugin-engine] standalone 能力调用失败: url={}, method={}, reason={}",
                    url, method.getName(), e.getMessage());
            throw e;
        } catch (RestClientException e) {
            log.warn("[plugin-engine] standalone 插件不可达: url={}, method={}", url, method.getName());
            throw new PluginCapabilityCallException("插件不可达: " + url, e);
        }
    }

    private Object convert(Object raw, Class<?> returnType) {
        if (raw == null || returnType == null || returnType == void.class || returnType == Void.class) {
            return null;
        }
        return objectMapper.convertValue(raw, returnType);
    }

    private static SimpleClientHttpRequestFactory defaultRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MILLIS);
        factory.setReadTimeout(TIMEOUT_MILLIS);
        return factory;
    }
}
