package com.houjiang.auth.feign;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 登录渠道凭据读取 Feign（统一登录 UniLogin 专用）
 *
 * <p>登录页处于未登录态，请求里没有组织上下文与用户 JWT，因此这里走 thirdparty 服务
 * 专为登录开放的只读通道 {@code /api/login-channel/{platform}}（只覆盖登录类平台）。</p>
 *
 * <p>与其他 client 一样按 URL 直连而非走服务发现：Nacos 里的幽灵实例会让登录这种
 * 高频关键路径出现 Connect/Read timed out。</p>
 *
 * <p>读取失败一律由调用方降级到 Nacos 配置，不影响登录可用性。</p>
 *
 * @author Amos
 */
@FeignClient(name = "thirdparty", url = "${feign.thirdparty.url:http://127.0.0.1:8194}",
        contextId = "loginChannelConfigClient")
public interface LoginChannelConfigClient {

    /**
     * 读取某登录渠道的凭据
     *
     * @param platform 平台键：wechat-login / wechat-work / dingtalk / feishu / qq-login
     * @return 该平台配置，data 为 null 表示未配置
     */
    @GetMapping("/api/login-channel/{platform}")
    ResponseResult<ThirdPartyConfigDTO> getLoginChannelConfig(@PathVariable("platform") String platform);
}
