package com.houjiang.plugin.engine.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 启动装载窗口闸门（与 {@link PluginDrainGate} 同构，但管的是「首次装载」这段窗口）。
 *
 * <p><b>要解决的问题</b>：宿主把插件装载放在 {@code ApplicationRunner} 里（见
 * {@code PluginEngineBootstrap}），而 {@code ApplicationRunner} 跑在 Servlet 容器开始监听
 * <b>之后</b>。于是从「端口可连」到「插件装载完」之间存在一段真实窗口——大插件（如 GEO，
 * 240MB jar、600 条路由）这段窗口实测接近两分钟。窗口内所有 {@code /plugin/{code}/**}
 * 都会走到 {@code routeRegistry.lookup} 未命中 → <b>404</b>。
 *
 * <p>前端无法区分这个 404 与「接口被删了」：用户看到的是一堆凭空出现的报错，运维看到的是
 * 「刚发布完的两分钟内插件全崩了」，而实际上只是还没装完。每次发版重启都会复现一次。
 *
 * <p><b>做法</b>：装载开始前 {@link #begin()}，结束后（无论成功失败）{@link #complete()}。
 * 窗口内 {@link PluginDispatcher} 对未命中路由的请求返回
 * <b>503 + {@code Retry-After}</b>——与升级排空窗口同一个诚实答案：
 * 「暂时不可用、稍后重试」，而不是「资源不存在」。
 *
 * <p><b>默认状态是「已就绪」</b>：只有真正开始装载的宿主才会置为装载中。
 * 未接入本闸门（{@code null}）或 {@code boot-enabled=false} 的宿主行为完全不变，
 * 因此不会给「插件真的没这个接口」制造假 503。
 *
 * <p><b>失效保护</b>：窗口有最长时限（默认 10 分钟），超时自动放行。装载线程被杀、
 * 卡死都不会把插件永久锁在 503 上——最坏情况是窗口自然到期后退回真实的 404。
 *
 * @author HouCloud
 */
public class PluginBootGate {

    /** {@code Retry-After} 缺省秒数：装载窗口按十秒量级重试，不至于把刚起来的进程打垮。 */
    private static final int DEFAULT_RETRY_AFTER_SECONDS = 10;

    /** 装载窗口缺省最长时限：超时后不再拦截，退回真实 404（见类注释「失效保护」）。 */
    private static final long DEFAULT_MAX_WINDOW_MILLIS = 10 * 60 * 1000L;

    private final int retryAfterSeconds;
    private final long maxWindowMillis;

    /**
     * 装载开始时间戳（{@code 0} 表示未在装载窗口内）。
     */
    private final AtomicLong startedAtMillis = new AtomicLong(0L);

    public PluginBootGate() {
        this(DEFAULT_RETRY_AFTER_SECONDS, DEFAULT_MAX_WINDOW_MILLIS);
    }

    public PluginBootGate(int retryAfterSeconds) {
        this(retryAfterSeconds, DEFAULT_MAX_WINDOW_MILLIS);
    }

    /**
     * @param retryAfterSeconds 响应头 {@code Retry-After} 的秒数，小于 1 时按缺省值处理
     * @param maxWindowMillis   装载窗口最长时限，小于等于 0 时按缺省值处理
     */
    public PluginBootGate(int retryAfterSeconds, long maxWindowMillis) {
        this.retryAfterSeconds = retryAfterSeconds >= 1 ? retryAfterSeconds : DEFAULT_RETRY_AFTER_SECONDS;
        this.maxWindowMillis = maxWindowMillis > 0 ? maxWindowMillis : DEFAULT_MAX_WINDOW_MILLIS;
    }

    /** 标记进入装载窗口（幂等：重复调用不刷新开始时间，避免长装载把窗口无限续期）。 */
    public void begin() {
        startedAtMillis.compareAndSet(0L, System.currentTimeMillis());
    }

    /** 结束装载窗口。 */
    public void complete() {
        startedAtMillis.set(0L);
    }

    /**
     * 是否处于装载窗口内（惰性失效：超过最长时限自动放行）。
     *
     * @return true 表示插件还在装载，未命中路由的请求应返回 503
     */
    public boolean isLoading() {
        long started = startedAtMillis.get();
        if (started == 0L) {
            return false;
        }
        if (System.currentTimeMillis() - started >= maxWindowMillis) {
            startedAtMillis.compareAndSet(started, 0L);
            return false;
        }
        return true;
    }

    /** @return 装载窗口响应应携带的 {@code Retry-After} 秒数 */
    public int retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
