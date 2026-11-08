package com.houjiang.plugin.api.host;

import java.util.ArrayList;
import java.util.List;

/**
 * standalone 插件能力调用请求（Core → 独立部署的插件进程）。
 *
 * <p>为什么需要它：定稿 §3.2 要求 {@code Core → plugin-api/PluginHostApi → embedded/standalone}。
 * embedded 插件与宿主同进程，可直接取到实现类；standalone 插件是独立进程，必须有一套
 * 跨进程调用契约，否则 {@code PluginHostApi} 只能服务宿主进程内的调用方（如 mall），
 * 而 order / goods 这类 Core 微服务将永远拿不到插件能力，只能退回硬编码
 * {@code @FeignClient("activity")} 的老路。
 *
 * <p>契约：{@code POST {plugin.host}/api/plugin/capability}，请求体与响应体即本类与
 * {@link CapabilityCallResponse}，JSON 序列化由 Jackson 完成。
 *
 * @param capability 能力接口全限定名，如 {@code com.xxx.SeckillCapability}
 * @param method     方法名
 * @param argTypes   参数类型全限定名，按声明顺序，用于重载方法定位
 * @param args       实参，按声明顺序
 * @author Amos
 */
public record CapabilityCallRequest(String capability,
                                    String method,
                                    List<String> argTypes,
                                    List<Object> args) {

    public CapabilityCallRequest(String capability, String method) {
        this(capability, method, new ArrayList<>(), new ArrayList<>());
    }
}
