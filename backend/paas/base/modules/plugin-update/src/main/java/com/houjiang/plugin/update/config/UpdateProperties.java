package com.houjiang.plugin.update.config;

import com.houjiang.mall.service.market.PluginArtifactDownloader;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 插件安装/升级编排配置（{@code houcloud.update.*}）。
 *
 * <p>四个配置的取值原则统一为「<b>不配就按最保守的方式跑</b>」：
 * 验签默认强制（缺签名 = 拒绝安装）、体积上限默认 64MB、
 * 框架版本默认 1.0.0（意味着声明更高要求的插件一律被 3014 拦住，
 * 而不是放行后在运行期炸掉）。
 *
 * @author Amos
 * @since M3
 */
@Component
@ConfigurationProperties(prefix = "houcloud.update")
public class UpdateProperties {

    /**
     * 宿主框架版本（与 {@code hou_market_plugin.min_framework_version} 比较）。
     *
     * <p>为什么是配置项而不是代码常量：框架版本属于宿主能力声明，
     * 由部署方（升级宿主后）维护最合适，写死在代码里会导致「宿主已升级但代码没改」
     * 的长期漂移。默认值 1.0.0 与现有插件包内声明一致。
     */
    private String frameworkVersion = "1.0.0";

    /**
     * 制品体积上限（字节），与 {@link PluginArtifactDownloader#DEFAULT_MAX_BYTES} 对齐。
     */
    private long maxArtifactBytes = PluginArtifactDownloader.DEFAULT_MAX_BYTES;

    /**
     * 是否强制要求制品签名（fail-closed，<b>默认开启</b>）。
     *
     * <p>🔴 2026-10-27 改默认值：上传侧一直在用 Ed25519 签名并写进
     * {@code hou_market_plugin_version.jar_signature}，但安装侧默认不验签 ——
     * 等于签名链路只做了上半截，谁把 jar 换掉都不会被发现。
     *
     * <p>开启后未签名制品会被拒绝安装，错误信息里带「怎么配公钥」的指引；
     * 部署侧需要一次性生成密钥并注入 {@code houcloud.artifact.sign.public-keys}
     * （见 {@code scripts/plugin/gen-sign-keys.sh}）。确需临时放行（仅限内部验证）
     * 可显式设 {@code HOU_PLUGIN_REQUIRE_SIGNATURE=false}。
     */
    private boolean requireSignature = true;

    /**
     * mall 自身对外地址（如 {@code http://127.0.0.1:8220}）。
     *
     * <p>留空表示<b>进程内取制品</b>：直接用 M2 的令牌调 {@code PluginArtifactDownloadService}
     * 开流读取，不经过 HTTP。容器里服务自己的对外地址往往不可知（在 nginx 之后、
     * 端口可能映射），自环 HTTP 反而多一个故障点，因此默认留空。
     * 需要端到端验证 raw 端点链路时才配置它。
     */
    private String publicBaseUrl = "";

    /**
     * 升级排空窗口（毫秒）：撤掉路由后给在途请求的缓冲时间。
     *
     * <p>为什么是 5 秒而不是 0：撤路由是瞬时的，但在途请求不会瞬时结束——
     * 一个正在跑的报表查询可能还要几百毫秒。窗口内请求统一返回 503 + Retry-After，
     * 前端据此提示「功能升级中」；窗口太短会把在途请求打成 404/500，
     * 太长又拖长升级体感。5 秒是「覆盖绝大多数接口耗时」与「升级不迟钝」的折中。
     */
    private long drainWindowMs = 5000L;

    /**
     * Windows jar 句柄释放重试次数（含首次尝试，即总共 attempts 次）。
     *
     * <p>Windows 上 ClassLoader 卸载是异步的：{@code unloadPlugin} 返回后 jar 文件句柄
     * 可能仍被占用几百毫秒，此时删/移 jar 会抛 {@code AccessDeniedException}。
     * 三次指数退避（200ms → 400ms）足以覆盖绝大多数情况；仍失败就放弃升级并恢复流量，
     * 绝不留「旧版本已停、新版本没起」的半生效状态。
     */
    private int handleRetryAttempts = 3;

    /**
     * 句柄释放重试的首次退避（毫秒），之后按 2 倍递增。
     */
    private long handleRetryBackoffMs = 200L;

    /**
     * 版本目录保留数量（<b>不含</b>当前版本与回滚锚点）。
     *
     * <p>保留 N 个是「磁盘占用」与「可回滚范围」的权衡：只留当前版本就没有退路，
     * 全留则每个插件都堆着十几个 64MB 的 jar。3 个足够覆盖「连续回滚两次」的故障场景。
     */
    private int retainVersions = 3;

    /**
     * 备份点保留数量（{@code .backup-{ts}}）。
     */
    private int retainBackups = 3;

    /**
     * 孤儿任务判定阈值（分钟）：启动时仍是非终态且已开始超过该时长的任务，
     * 视为「进程被杀留下的残骸」。
     *
     * <p>30 分钟远大于任何一次真实升级耗时（下载 + 迁移通常在 1 分钟内），
     * 因此不会误伤正在跑的任务；同时足够短，不会让残留任务长期占着互斥锁
     * （非终态任务会阻塞同插件的下一次升级 / 回滚）。
     */
    private long orphanTimeoutMinutes = 30L;

    /**
     * @return 宿主框架版本
     */
    public String getFrameworkVersion() {
        return frameworkVersion;
    }

    /**
     * @param frameworkVersion 宿主框架版本
     */
    public void setFrameworkVersion(String frameworkVersion) {
        this.frameworkVersion = frameworkVersion;
    }

    /**
     * @return 制品体积上限（字节）
     */
    public long getMaxArtifactBytes() {
        return maxArtifactBytes;
    }

    /**
     * @param maxArtifactBytes 制品体积上限（字节）
     */
    public void setMaxArtifactBytes(long maxArtifactBytes) {
        this.maxArtifactBytes = maxArtifactBytes > 0 ? maxArtifactBytes : PluginArtifactDownloader.DEFAULT_MAX_BYTES;
    }

    /**
     * @return 是否强制要求制品签名
     */
    public boolean isRequireSignature() {
        return requireSignature;
    }

    /**
     * @param requireSignature 是否强制要求制品签名
     */
    public void setRequireSignature(boolean requireSignature) {
        this.requireSignature = requireSignature;
    }

    /**
     * @return mall 对外地址，留空表示进程内取制品
     */
    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    /**
     * @param publicBaseUrl mall 对外地址
     */
    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();
    }

    /**
     * @return 升级排空窗口（毫秒）
     */
    public long getDrainWindowMs() {
        return drainWindowMs;
    }

    /**
     * @param drainWindowMs 升级排空窗口（毫秒），负数按 0 处理
     */
    public void setDrainWindowMs(long drainWindowMs) {
        this.drainWindowMs = Math.max(0L, drainWindowMs);
    }

    /**
     * @return 句柄释放重试次数
     */
    public int getHandleRetryAttempts() {
        return handleRetryAttempts;
    }

    /**
     * @param handleRetryAttempts 重试次数，小于 1 时按 1 处理（至少要试一次）
     */
    public void setHandleRetryAttempts(int handleRetryAttempts) {
        this.handleRetryAttempts = Math.max(1, handleRetryAttempts);
    }

    /**
     * @return 首次退避毫秒数
     */
    public long getHandleRetryBackoffMs() {
        return handleRetryBackoffMs;
    }

    /**
     * @param handleRetryBackoffMs 首次退避毫秒数，负数按 0 处理
     */
    public void setHandleRetryBackoffMs(long handleRetryBackoffMs) {
        this.handleRetryBackoffMs = Math.max(0L, handleRetryBackoffMs);
    }

    /**
     * @return 版本目录保留数量
     */
    public int getRetainVersions() {
        return retainVersions;
    }

    /**
     * @param retainVersions 保留数量，小于 1 时按 1 处理（至少保留当前版本）
     */
    public void setRetainVersions(int retainVersions) {
        this.retainVersions = Math.max(1, retainVersions);
    }

    /**
     * @return 备份点保留数量
     */
    public int getRetainBackups() {
        return retainBackups;
    }

    /**
     * @param retainBackups 备份点保留数量，小于 1 时按 1 处理
     */
    public void setRetainBackups(int retainBackups) {
        this.retainBackups = Math.max(1, retainBackups);
    }

    /**
     * @return 孤儿任务判定阈值（分钟）
     */
    public long getOrphanTimeoutMinutes() {
        return orphanTimeoutMinutes;
    }

    /**
     * @param orphanTimeoutMinutes 阈值（分钟），小于 1 时按 1 处理
     */
    public void setOrphanTimeoutMinutes(long orphanTimeoutMinutes) {
        this.orphanTimeoutMinutes = Math.max(1L, orphanTimeoutMinutes);
    }
}
