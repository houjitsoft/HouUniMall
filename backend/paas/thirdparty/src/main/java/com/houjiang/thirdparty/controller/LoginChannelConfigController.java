package com.houjiang.thirdparty.controller;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.service.ThirdPartyConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一登录渠道凭据读取接口（UniLogin 专用，内部调用）
 *
 * <p><b>为什么需要这个接口：</b>登录页处于未登录态，请求里没有组织上下文，
 * 走设置页那套 {@code /api/setting/third-party/**}（依赖登录态 + 权限）读不到凭据，
 * 就会出现「设置页填了凭据、登录页却提示未开通」。因此这里提供一个
 * <b>只读、只覆盖登录类平台、且不依赖登录态</b>的最小通道，供 auth 服务在登录流程中调用。</p>
 *
 * <p><b>暴露面控制（重要）：</b></p>
 * <ul>
 *   <li>只允许 {@link ThirdPartyConfigServiceImpl#LOGIN_CHANNEL_PLATFORMS} 里的平台，
 *       电子签/语雀等非登录类第三方凭据一律拒绝（返回 403 语义的失败码）；</li>
 *   <li>只读：不提供任何写入、测试、删除入口；</li>
 *   <li>只返回该平台自己的那一段配置（entityToDto 只装配匹配的子对象），不做批量返回；</li>
 *   <li>整体被 {@code JwtAuthenticationTokenFilter} 放行，仅在服务内网可达
 *       （BFF 只暴露 /web/**，不转发本路径）。</li>
 * </ul>
 *
 * <p>若后续需要更强的隔离，可在两端的配置里加一个内部调用令牌并在此校验（失败即拒绝）。
 * 当前实现按「服务内网可信」处理，风险仅限于同主机上的其他进程。</p>
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/login-channel")
public class LoginChannelConfigController {

    @Autowired
    private ThirdPartyConfigService thirdPartyConfigService;

    /**
     * 读取某登录渠道的凭据
     *
     * @param platform 平台键：wechat-login / wechat-work / dingtalk / feishu / qq-login
     * @return 该平台的配置；未配置时 data 为 null
     */
    @GetMapping("/{platform}")
    public ResponseResult<ThirdPartyConfigDTO> getLoginChannelConfig(@PathVariable("platform") String platform) {
        if (platform == null || !ThirdPartyConfigService.LOGIN_CHANNEL_PLATFORMS.contains(platform.trim().toLowerCase())) {
            log.warn("拒绝读取非登录类平台凭据: {}", platform);
            return ResponseResult.fail(403, "该平台不属于登录渠道，不允许读取。");
        }
        try {
            ThirdPartyConfigDTO config = thirdPartyConfigService.getLoginChannelConfig(platform);
            return ResponseResult.success(config);
        } catch (Exception e) {
            log.error("读取登录渠道配置失败: platform={}, error={}", platform, e.getMessage(), e);
            return ResponseResult.fail(500, "读取渠道配置失败");
        }
    }
}
