package com.houjiang.bffweb.controller.plugin;

import java.io.IOException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * BFF 转发 mall 用的 {@link RestTemplate} 工厂。
 *
 * <p><b>为什么必须集中在这里创建</b>：RestTemplate 出厂默认装的是
 * {@code DefaultResponseErrorHandler}，它对 <b>4xx / 5xx 一律抛 {@code HttpStatusCodeException}</b>。
 * 后果是「上游返回了业务错误码」与「网关根本连不上上游」在调用方眼里完全一样——
 * 都变成异常、都只能回 502。
 * 这坑在生产上已经炸过一次：M2 的六态授权矩阵（3002 未购买 / 3003 授权过期 / 3004 已吊销 /
 * 3007 令牌过期 / 3008 租户不符 / 3009 制品缺失）在公网上全部表现为 502 + 空 body，
 * 前端无从区分，等于白做。
 *
 * <p>正确做法是让转发层<b>永不把上游状态码当异常</b>：状态码与响应体由调用方的
 * {@code ResponseExtractor} 原样透传给客户端；只有真正的 IO 故障
 * （连接拒绝 / 超时，表现为 {@code ResourceAccessException}）才算网关故障、才回 502。
 *
 * <p>本工厂把这条规则固化在一处，避免四个转发控制器各写一遍、再各漏一次。
 *
 * @author Amos
 * @since M3（P0 缺陷修复）
 */
public final class ProxyRestTemplateFactory {

    /**
     * 私有构造，工具类不允许实例化。
     */
    private ProxyRestTemplateFactory() {
    }

    /**
     * 创建「不把上游错误码当异常」的转发客户端。
     *
     * @param connectTimeoutMs 连接超时（毫秒）
     * @param readTimeoutMs    读取超时（毫秒）
     * @return 装配好的 RestTemplate
     */
    public static RestTemplate create(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(new PassThroughResponseErrorHandler());
        return restTemplate;
    }

    /**
     * 透传错误处理器：对任何状态码都不抛异常。
     *
     * <p>{@code hasError} 恒为 false 之后，4xx/5xx 会像 2xx 一样进入调用方的
     * {@code ResponseExtractor}，由转发控制器决定怎么写给客户端（原样透传状态与响应体）。
     */
    private static final class PassThroughResponseErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            // 上游的任何状态码都不是「本层异常」：那是业务结果，交给调用方透传。
            return false;
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // 刻意留空：状态码与响应体由 ResponseExtractor 处理，这里不做任何转换。
        }
    }
}
