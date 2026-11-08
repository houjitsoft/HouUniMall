package com.houjiang.plugin.artifact.delivery;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.artifact.PluginArtifact;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.artifact.PluginArtifactMapper;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.service.market.PluginLicenseService;
import com.houjiang.plugin.artifact.token.DownloadTokenService;
import com.houjiang.plugin.artifact.token.IssueTokenCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 平台侧：站点级插件制品分发（2026-10-30 补齐 10-29 架构口径的最后一环）。
 *
 * <p><b>为什么要有它</b>：10-29 起市场列表以平台目录为唯一权威——配了
 * {@code houcloud.platform.url} 的站点，市场页实时显示平台的全部在架插件；
 * 但安装链路当时仍查<b>站点本地</b> {@code hou_market_plugin_version}，
 * 而开源包的本地版本表恒为空（本地没有插件商品快照是口径而非缺陷），
 * 结果「列表能看到 39 个、点安装必死在『插件版本不存在或未通过审核』」。
 * 本服务把「插件商品 + 版本 + 制品」三样权威数据的判定收回平台侧，
 * 站点用 {@code X-Platform-Token} 换票后经现有 raw 端点拉包。</p>
 *
 * <p><b>放在 plugin-artifact 的原因</b>：换票要复用 {@link DownloadTokenService}
 * （令牌签发）与 {@link PluginArtifactMapper}（制品定位），而 plugin-artifact
 * 已依赖 plugin-market；反向放 plugin-market 会造成 Maven 循环依赖。</p>
 *
 * <p><b>付费门控</b>（2026-10-30 打通）：PAID / TRIAL 插件校验「该站点是否已持有
 * 有效授权」——站点在线购买后由 PluginOrderService.settle 异步上报授权到此
 * （{@link #reportSiteLicenseGrant}），平台落 {@code hou_market_plugin_license}
 * 副本；acquire 按 (pluginCode, siteCode) 查有效授权，有则放行、无则明确拒绝。
 * 此前是无条件拒绝（授权同步未开通），导致站点侧完成了购买拿到授权、
 * 平台派发却不认账，付费变现闭环断在最后一环。授权主记录在站点侧，
 * 平台这份是「派发凭证」，与协议回传（PlatformConsentReporter）同模型。</p>
 *
 * @author Amos
 * @since 2026-10-30
 */
@Slf4j
@Service
public class PlatformPluginDistService {

    /** 市场插件上架状态：2 = 已上架（与 PluginMarketService.STATUS_PUBLISHED 一致）。 */
    private static final int STATUS_PUBLISHED = 2;

    /** 版本审核状态：1 = 已通过（与 PluginMarketService.VERSION_APPROVED 一致）。 */
    private static final int VERSION_APPROVED = 1;

    /** 制品上传完成状态：1 = 可下载。 */
    private static final int UPLOAD_STATUS_DONE = 1;

    /** 站点拉包走的 BFF 相对路径（站点拼 platformUrl 后为绝对地址）。 */
    private static final String SITE_RAW_ENDPOINT = "/web/plugin-center/artifact/raw?token=";

    private final MarketPluginMapper marketPluginMapper;
    private final MarketPluginVersionMapper marketPluginVersionMapper;
    private final PluginArtifactMapper artifactMapper;
    private final DownloadTokenService tokenService;
    /** 授权服务（2026-10-30）：付费插件 acquire 校验站点授权 + 上报落库都走它。 */
    private final PluginLicenseService licenseService;

    public PlatformPluginDistService(MarketPluginMapper marketPluginMapper,
                                     MarketPluginVersionMapper marketPluginVersionMapper,
                                     PluginArtifactMapper artifactMapper,
                                     DownloadTokenService tokenService,
                                     PluginLicenseService licenseService) {
        this.marketPluginMapper = marketPluginMapper;
        this.marketPluginVersionMapper = marketPluginVersionMapper;
        this.artifactMapper = artifactMapper;
        this.tokenService = tokenService;
        this.licenseService = licenseService;
    }

    /**
     * 站点换票：校验插件商品 / 版本 / 制品并签发限时下载令牌。
     *
     * <p>令牌复用现有下载令牌机制（10 分钟 + 限次），instanceId 绑定为站点标识
     * {@code siteCode}——raw 端点校验令牌即可，无需站点登录态。</p>
     *
     * @param pluginCode 插件编码（必填）
     * @param version    版本号；空 = 取最新已审核版本
     * @param siteCode   站点标识（必填，令牌绑定）
     * @param siteName   站点名（留痕）
     * @param instanceId 站点实例标识（可选，留痕）
     * @param clientIp   调用方 IP（留痕溯源）
     * @return 换票结果（downloadUrl 为 BFF 相对路径，站点拼平台地址）
     * @throws IllegalArgumentException 参数缺失
     * @throws IllegalStateException     插件不存在 / 未上架 / 付费拒绝 / 版本未审核 / 制品缺失
     */
    public Map<String, Object> acquireForSite(String pluginCode, String version,
                                               String siteCode, String siteName,
                                               String instanceId, String clientIp) {
        String code = pluginCode == null ? "" : pluginCode.trim();
        if (code.isEmpty()) {
            throw new IllegalArgumentException("缺少插件编码 pluginCode");
        }
        if (!StringUtils.hasText(siteCode)) {
            throw new IllegalArgumentException("缺少站点标识 siteCode");
        }
        MarketPlugin plugin = marketPluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, code)
                .last("limit 1"));
        if (plugin == null) {
            throw new IllegalStateException("平台不存在该插件商品: " + code);
        }
        if (!Integer.valueOf(STATUS_PUBLISHED).equals(plugin.getStatus())) {
            throw new IllegalStateException("该插件商品未在平台上架: " + code);
        }
        if (requiresLicense(plugin)) {
            // 付费插件：站点已购买（授权副本已同步到平台）则放行，否则诚实拒绝。
            // 授权副本由 PluginOrderService.settle 发证后异步上报（见 reportSiteLicenseGrant）；
            // 未上报/未购买一律拒——平台是制品派发的唯一关口，不设这道门就等于免费送付费插件。
            MarketPluginLicense grant = licenseService.findValid(code, siteCode);
            if (grant == null) {
                throw new IllegalStateException("付费插件需先在站点市场完成购买授权后才能安装: "
                        + code + "（站点标识: " + siteCode + "）");
            }
            log.info("付费插件换票放行: plugin={} site={} order={}",
                    code, siteCode, grant.getSourceOrderNo());
        }
        String ver = version == null ? "" : version.trim();
        LambdaQueryWrapper<MarketPluginVersion> wrapper = new LambdaQueryWrapper<MarketPluginVersion>()
                .eq(MarketPluginVersion::getPluginCode, code)
                .eq(MarketPluginVersion::getStatus, VERSION_APPROVED);
        if (StringUtils.hasText(ver)) {
            wrapper.eq(MarketPluginVersion::getVersion, ver);
        } else {
            wrapper.orderByDesc(MarketPluginVersion::getPublishTime)
                    .orderByDesc(MarketPluginVersion::getId);
        }
        MarketPluginVersion target = marketPluginVersionMapper.selectOne(wrapper.last("limit 1"));
        if (target == null) {
            throw new IllegalStateException("插件版本不存在或未通过审核: " + code
                    + "@" + (ver.isEmpty() ? "latest" : ver));
        }
        PluginArtifact artifact = artifactMapper.selectOne(new LambdaQueryWrapper<PluginArtifact>()
                .eq(PluginArtifact::getPluginCode, code)
                .eq(PluginArtifact::getVersion, target.getVersion())
                .eq(PluginArtifact::getUploadStatus, UPLOAD_STATUS_DONE)
                .eq(PluginArtifact::getArtifactKind, "jar")
                .orderByDesc(PluginArtifact::getId)
                .last("limit 1"));
        if (artifact == null) {
            throw new IllegalStateException("该版本制品不存在或尚未上传完成: " + code + "@" + target.getVersion());
        }
        DownloadTokenService.IssuedToken issued = tokenService.issue(new IssueTokenCommand(
                artifact.getArtifactId(), code, target.getVersion(),
                siteCode.trim(), null, clientIp, "site-install:" + (StringUtils.hasText(siteName) ? siteName : siteCode)));
        if (log.isInfoEnabled()) {
            log.info("站点换票安装包: plugin={}, version={}, site={}, instance={}, ip={}",
                    code, target.getVersion(), siteCode, instanceId, clientIp);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pluginCode", code);
        result.put("version", target.getVersion());
        result.put("jarMd5", artifact.getMd5());
        result.put("sha256", artifact.getSha256());
        result.put("sizeBytes", artifact.getSizeBytes() == null ? 0L : artifact.getSizeBytes());
        // pluginYaml 站点装载 PF4J 时要用（manifest 声明）；md5/pluginYaml 只发给
        // 通过 X-Platform-Token 校验的站点，比公开目录（刻意不含指纹）门槛更高
        result.put("pluginYaml", target.getPluginYaml());
        // 优先用制品表 host_service；历史上传若漏写，从 plugin.yaml 回落，避免站点默认装到 base。
        String hostService = artifact.getHostService();
        if (!StringUtils.hasText(hostService) && StringUtils.hasText(target.getPluginYaml())) {
            for (String line : target.getPluginYaml().split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.startsWith("hostService:")) {
                    hostService = trimmed.substring("hostService:".length()).trim();
                    break;
                }
            }
        }
        result.put("hostService", hostService);
        result.put("token", issued.getToken());
        result.put("downloadUrl", SITE_RAW_ENDPOINT
                + URLEncoder.encode(issued.getToken(), StandardCharsets.UTF_8));
        result.put("expiresAt", LocalDateTime.now().plusMinutes(10).toString());
        return result;
    }

    /**
     * 站点购买授权上报落库（2026-10-30）：站点在线购买付费插件发证后，把授权
     * 副本上报到平台——平台 acquire 换票按 (pluginCode, siteCode) 校验授权副本，
     * 这是从「站点已买、平台拒发」断链到「购买即派发」闭环的关键一步。
     *
     * <p><b>幂等与归属冲突</b>：复用 {@link PluginLicenseService#issuePaidOrder}——
     * 按 sourceOrderNo 幂等（同订单重复上报返回既有授权行），订单归属冲突
     * （同 orderNo 不同 plugin/site）直接拒绝，防伪造篡改他人授权。</p>
     *
     * <p><b>沙箱标记透传</b>：站点沙箱购买（purchase_sandbox=1）如实落标记——
     * 平台不歧视沙箱站（评估/演示环境要能走完整闭环），但 findValidPaid /
     * 维护期等生产口径会按既有规则排除沙箱单，语义不混淆。</p>
     *
     * @param params {pluginCode, siteCode, orderNo, licenseKey?, licenseType?,
     *               termDays?, sandbox?(0/1), expireTime?(ISO-8601), instanceId?}
     * @return 受理结果摘要（orderNo / licenseKey / 幂等信息）
     * @throws IllegalArgumentException 参数缺失
     * @throws IllegalStateException     订单授权归属冲突 / 商品不在架
     */
    public Map<String, Object> reportSiteLicenseGrant(Map<String, Object> params) {
        String code = text(params, "pluginCode");
        String siteCode = text(params, "siteCode");
        String orderNo = text(params, "orderNo");
        if (!StringUtils.hasText(code) || !StringUtils.hasText(siteCode) || !StringUtils.hasText(orderNo)) {
            throw new IllegalArgumentException("缺少 pluginCode / siteCode / orderNo");
        }
        MarketPlugin plugin = marketPluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, code)
                .last("limit 1"));
        if (plugin == null) {
            throw new IllegalStateException("平台不存在该插件商品，授权上报被拒: " + code);
        }
        boolean sandbox = "1".equals(text(params, "sandbox")) || "true".equalsIgnoreCase(text(params, "sandbox"));
        Integer termDays = null;
        String termText = text(params, "termDays");
        if (StringUtils.hasText(termText)) {
            try {
                termDays = Integer.valueOf(termText);
            } catch (NumberFormatException ignored) {
                // 非数字按未传处理：期限是锦上添花字段，不让它挡住授权副本落库
            }
        }
        // instanceId 即站点标识：授权副本绑定 siteCode，acquire 校验同口径。
        MarketPluginLicense license = licenseService.issuePaidOrder(
                code, null, siteCode, orderNo, sandbox, termDays);
        log.info("站点购买授权上报落库: plugin={} site={} order={} sandbox={} licenseId={}",
                code, siteCode, orderNo, sandbox, license.getId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderNo", orderNo);
        result.put("licenseKey", license.getLicenseKey());
        result.put("licenseId", license.getId());
        result.put("pluginCode", code);
        result.put("siteCode", siteCode);
        return result;
    }

    private static String text(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    /** PAID / TRIAL 需要授权（与 PluginLicenseService.requiresLicense 同口径，静态复判避免跨模块依赖）。 */
    private static boolean requiresLicense(MarketPlugin plugin) {
        String type = plugin.getLicenseType() == null ? "" : plugin.getLicenseType().trim().toUpperCase();
        return "PAID".equals(type) || "TRIAL".equals(type);
    }
}
