package com.houjiang.plugin.artifact.authorization;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.market.MarketPluginLicenseMapper;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.service.market.PluginLicenseService;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 下载授权门控（方案 §6.3 授权矩阵）。
 *
 * <p>判定顺序（成本从低到高，且「版本态」先于「授权态」——
 * 版本已下架时，有没有授权都无意义，直接 3005）：
 * <ol>
 *   <li>版本行不存在 → 3009；</li>
 *   <li>版本 offline(5) → 3005；版本非 published(1) → 3006；</li>
 *   <li>FREE → 登录即放行；商品或授权类型缺失则拒绝；</li>
 *   <li>PAID/TRIAL：站点实例为空 → 3002（fail-closed，不允许「不限实例」的授权查询）；</li>
 *   <li>有有效授权 → 放行；</li>
 *   <li>无有效授权 → 查同一 (code, instanceId) 的最新一条留痕再定性：
 *       已吊销 → 3004；已过期 → 3003；都没有 → 3002。</li>
 * </ol>
 *
 * <p><b>为什么 3003/3004 要再查一次库</b>：{@link PluginLicenseService#findValid} 只返回有效授权，
 * 无法区分「没买」与「买了但过期/被吊销」，而这两种情况给用户的动作完全不同
 * （续费 vs 申诉）。本服务<b>不改动</b> PluginLicenseService，改用
 * {@link MarketPluginLicenseMapper} 直接查一次历史留痕做定性。
 *
 * @author Amos
 * @since M2
 */
@Slf4j
@Service
public class PluginDownloadAuthorizationService {

    private final MarketPluginVersionMapper versionMapper;

    private final MarketPluginMapper pluginMapper;

    private final PluginLicenseService licenseService;

    private final MarketPluginLicenseMapper licenseMapper;

    /**
     * 构造授权门控。
     *
     * @param versionMapper 版本 Mapper
     * @param pluginMapper  商品 Mapper
     * @param licenseService 授权服务（复用，不修改）
     * @param licenseMapper 授权 Mapper（用于过期/吊销定性）
     */
    public PluginDownloadAuthorizationService(MarketPluginVersionMapper versionMapper,
                                              MarketPluginMapper pluginMapper,
                                              PluginLicenseService licenseService,
                                              MarketPluginLicenseMapper licenseMapper) {
        this.versionMapper = Objects.requireNonNull(versionMapper, "MarketPluginVersionMapper 不能为空");
        this.pluginMapper = Objects.requireNonNull(pluginMapper, "MarketPluginMapper 不能为空");
        this.licenseService = Objects.requireNonNull(licenseService, "PluginLicenseService 不能为空");
        this.licenseMapper = Objects.requireNonNull(licenseMapper, "MarketPluginLicenseMapper 不能为空");
    }

    /**
     * 判断某站点实例/用户能否下载指定插件版本。
     *
     * @param pluginCode 插件编码
     * @param version    版本
     * @param identity   调用方身份（服务端解析）
     * @return 判定结果
     */
    public DownloadDecision authorize(String pluginCode, String version, PluginIdentity identity) {
        if (!StringUtils.hasText(pluginCode) || !StringUtils.hasText(version)) {
            return DownloadDecision.deny(PluginDistErrorCode.ARTIFACT_NOT_FOUND, "插件编码与版本不能为空");
        }
        String code = pluginCode.trim();
        String ver = version.trim();

        MarketPluginVersion versionRow = versionMapper.selectOne(new LambdaQueryWrapper<MarketPluginVersion>()
                .eq(MarketPluginVersion::getPluginCode, code)
                .eq(MarketPluginVersion::getVersion, ver)
                .last("limit 1"));
        if (versionRow == null) {
            return DownloadDecision.deny(PluginDistErrorCode.ARTIFACT_NOT_FOUND,
                    "插件版本不存在：" + code + " " + ver);
        }
        Integer status = versionRow.getStatus();
        if (status == null) {
            return DownloadDecision.deny(PluginDistErrorCode.VERSION_NOT_PUBLISHED,
                    "插件版本状态缺失，不可下载：" + code + " " + ver, versionRow);
        }
        if (status == 5) {
            return DownloadDecision.deny(PluginDistErrorCode.PLUGIN_OR_VERSION_OFFLINE,
                    "该版本已下架：" + code + " " + ver, versionRow);
        }
        if (status != 1) {
            return DownloadDecision.deny(PluginDistErrorCode.VERSION_NOT_PUBLISHED,
                    "该版本未发布（status=" + status + "），不可下载：" + code + " " + ver, versionRow);
        }

        MarketPlugin plugin = pluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, code)
                .last("limit 1"));
        if (plugin == null) {
            return DownloadDecision.deny(PluginDistErrorCode.NOT_PURCHASED,
                    "插件商品未登记，禁止分发：" + code, versionRow);
        }
        String licenseType = plugin.getLicenseType();
        if ("FREE".equals(licenseType)) {
            return DownloadDecision.allow(versionRow, plugin);
        }
        if (!PluginLicenseService.requiresLicense(licenseType)) {
            return DownloadDecision.deny(PluginDistErrorCode.NOT_PURCHASED,
                    "插件授权类型未配置或无法识别，禁止分发：" + code, versionRow);
        }

        String instanceId = identity == null ? "" : identity.getInstanceId();
        if (!StringUtils.hasText(instanceId)) {
            // fail-closed：付费插件必须能定位站点，否则退化成「不限实例」的越权查询
            log.warn("付费插件下载缺少站点实例标识，拒绝: plugin={} version={}", code, ver);
            return DownloadDecision.deny(PluginDistErrorCode.NOT_PURCHASED,
                    "未识别站点实例，无法确认授权归属：" + code, versionRow);
        }
        // 🔴 2026-10-27：制品下载必须持「生产购买」授权（沙箱 / MOCK 单一律不认），
        // 与源码包、独立服务镜像的口径保持一致，避免沙箱授权把生产 JAR 拉走。
        if (licenseService.findValidProduction(code, instanceId.trim()) != null) {
            return DownloadDecision.allow(versionRow, plugin);
        }
        return classifyNoValidLicense(code, ver, instanceId.trim(), versionRow);
    }

    /**
     * 无有效授权时定性：吊销 / 过期 / 未购买。
     *
     * @param code       插件编码
     * @param ver        版本
     * @param instanceId 站点实例
     * @param versionRow 命中的版本行
     * @return 拒绝判定
     */
    private DownloadDecision classifyNoValidLicense(String code,
                                                    String ver,
                                                    String instanceId,
                                                    MarketPluginVersion versionRow) {
        MarketPluginLicense latest = licenseMapper.selectOne(new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getPluginCode, code)
                .eq(MarketPluginLicense::getInstanceId, instanceId)
                .orderByDesc(MarketPluginLicense::getId)
                .last("limit 1"));
        if (latest == null) {
            return DownloadDecision.deny(PluginDistErrorCode.NOT_PURCHASED,
                    "当前站点未购买该插件：" + code, versionRow);
        }
        if (!Integer.valueOf(1).equals(latest.getStatus())) {
            return DownloadDecision.deny(PluginDistErrorCode.LICENSE_REVOKED,
                    "该插件授权已被吊销：" + code, versionRow);
        }
        if (Integer.valueOf(1).equals(latest.getPurchaseSandbox())) {
            return DownloadDecision.deny(PluginDistErrorCode.NOT_PURCHASED,
                    "该授权来自沙箱购买，只能用于沙箱验证，不能下载生产制品：" + code, versionRow);
        }
        LocalDateTime expireTime = latest.getExpireTime();
        if (expireTime != null && expireTime.isBefore(LocalDateTime.now())) {
            return DownloadDecision.deny(PluginDistErrorCode.LICENSE_EXPIRED,
                    "该插件授权已过期：" + code, versionRow);
        }
        // 授权存在且未过期，但 findValid 没命中：多半是实例绑定不一致，按未购买处理
        log.warn("插件授权校验未命中: plugin={} version={} instance={} licenseId={}",
                code, ver, instanceId, latest.getId());
        return DownloadDecision.deny(PluginDistErrorCode.NOT_PURCHASED,
                "当前站点未持有该插件的有效授权：" + code, versionRow);
    }
}
