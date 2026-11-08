package com.houjiang.plugin.update.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.entity.update.PluginUpdateTask;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.mapper.update.PluginUpdateTaskMapper;
import com.houjiang.mall.service.market.PluginArtifactDownloader;
import com.houjiang.mall.service.plugin.AddonPluginSyncService;
import com.houjiang.mall.service.plugin.SchemaMigrationRunner;
import com.houjiang.plugin.artifact.authorization.DownloadDecision;
import com.houjiang.plugin.artifact.authorization.PluginDownloadAuthorizationService;
import com.houjiang.plugin.artifact.download.DownloadUrlResult;
import com.houjiang.plugin.artifact.download.PluginArtifactDownloadService;
import com.houjiang.plugin.artifact.download.RawArtifact;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import com.houjiang.plugin.artifact.model.ArtifactStream;
import com.houjiang.plugin.engine.artifact.PluginArtifactStore;
import com.houjiang.plugin.engine.core.PluginDrainGate;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import com.houjiang.plugin.sdk.model.PluginManifest;
import com.houjiang.plugin.signature.ArtifactSignaturePayload;
import com.houjiang.plugin.signature.ArtifactSigner;
import com.houjiang.plugin.update.UpdateOpType;
import com.houjiang.plugin.update.UpdateStep;
import com.houjiang.plugin.update.UpdateTaskState;
import com.houjiang.plugin.update.config.UpdateProperties;
import com.houjiang.plugin.update.exception.UpdateRejectedException;
import com.houjiang.plugin.update.model.InstallResult;
import com.houjiang.plugin.update.model.TaskStatus;
import com.houjiang.plugin.update.model.UpdateOutcome;
import com.houjiang.plugin.update.precheck.PluginCompatibilityPrecheckService;
import com.houjiang.plugin.update.precheck.PrecheckResult;
import com.houjiang.plugin.update.util.FileSystemHandleRetry;
import com.houjiang.commons.version.VersionComparison;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * 插件安装编排（M3，方案 §5.2 的「下载 → 校验 → schema → 安装」后半段）。
 *
 * <p><b>顺序是强制的：先文件系统与引擎，最后写 DB。</b>
 * 理由是这三层里只有 DB 能被事务回滚——MySQL 的 DDL 隐式提交，
 * 已建的插件表在 {@code @Transactional} 里也撤不回来；文件系统与 PF4J 装载更不受事务约束。
 * 因此本类<b>刻意不使用 {@code @Transactional}</b>：加了只会给人「失败能整体回滚」的错觉，
 * 真正的兜底是每一步失败后的<b>尽力补偿</b>（删新版本目录 / 卸载引擎 / 恢复旧版本）。
 * 这也与既有 {@code PluginMarketService} 的补偿范式一致。
 *
 * <p><b>诚实边界（做不到 100% 原子，写清楚别吹）</b>：
 * <ul>
 *   <li>schema 失败时只能「跑包内 rollback.sql + 记 FAILED」，已建的表不会被自动删除——
 *       {@code rollback.sql} 是插件作者提供的补偿脚本，没提供就只能留着；</li>
 *   <li>引擎装载失败前有「旧版本已卸载、新版本未生效」的窗口，本类会<b>尽力重装旧版本</b>
 *       （旧 jar 仍在版本目录里），但重装失败就无能为力，只能置 FAILED 交给人工；</li>
 *   <li>并发互斥依赖 {@code uk_active} 唯一键（生成列 active_key，仅非终态参与）
 *       + 主动查询双保险，不依赖应用层锁。</li>
 * </ul>
 *
 * <p><b>为什么校验放在落盘之前</b>：md5 / sha256 / 验签全部在内存里对下载到的字节做，
 * 任何一项不过就直接抛异常，磁盘上一个字节都不会写。
 * 「删临时文件」这个动作因此根本不需要——没有临时文件，旧版本天然零影响。
 *
 * @author Amos
 * @since M3
 */
@Slf4j
@Service
public class PluginUpdateService {

    /**
     * 版本已发布。
     */
    private static final int VERSION_PUBLISHED = 1;

    /**
     * 版本已下架。
     */
    private static final int VERSION_OFFLINE = 5;

    /**
     * 非插件分发类失败的统一 HTTP 码。
     */
    private static final int INTERNAL_ERROR = 500;

    /**
     * 错误描述入库截断长度（列宽 varchar(1024)）。
     */
    private static final int ERROR_MSG_MAX = 1000;

    /**
     * 读取缓冲区大小。
     */
    private static final int READ_BUFFER = 8192;

    /**
     * 安装态 {@code update_status}：空闲。
     */
    private static final int UPDATE_STATUS_IDLE = 0;

    /**
     * 安装态 {@code update_status}：升级中。
     */
    private static final int UPDATE_STATUS_UPGRADING = 2;

    /**
     * 安装态 {@code update_status}：回滚中。
     */
    private static final int UPDATE_STATUS_ROLLING_BACK = 3;

    /**
     * 安装态 {@code update_status}：上次失败。
     */
    private static final int UPDATE_STATUS_FAILED = 4;

    /**
     * 安装态 {@code update_status}：卸载中。
     *
     * <p>取值 5 是新增档位（原范围为 0-4），与 {@link #requireInstallUnlocked} 的判活口径
     * 「非 0 且非 4 即为忙」天然兼容，无需改那段逻辑。
     */
    private static final int UPDATE_STATUS_UNINSTALLING = 5;

    /**
     * 孤儿任务错误码（与 DDL 第六段收敛历史非终态任务时用的码保持一致）。
     */
    private static final String ORPHAN_TIMEOUT = "ORPHAN_TIMEOUT";

    /**
     * 句柄未释放错误码（Windows jar 被占用，三次重试后仍失败）。
     */
    private static final String HANDLE_LOCKED = "HANDLE_LOCKED";

    private final PluginCompatibilityPrecheckService precheckService;

    private final PluginDownloadAuthorizationService authorizationService;

    private final PluginArtifactDownloadService downloadService;

    private final PluginArtifactDownloader artifactDownloader;

    private final ArtifactSigner artifactSigner;

    private final PluginArtifactStore artifactStore;

    private final PluginLifecycleManager lifecycleManager;

    private final SchemaMigrationRunner schemaMigrationRunner;

    private final MallPluginInstallMapper installMapper;

    private final MarketPluginVersionMapper versionMapper;

    private final PluginUpdateTaskMapper taskMapper;

    private final AddonPluginSyncService addonPluginSyncService;

    private final UpdateProperties properties;

    /**
     * 升级排空窗口登记（M4）：撤路由后让请求统一拿到 503 + Retry-After，而不是 404/500。
     *
     * <p>宿主未接入引擎自动配置时为 {@code null}，此时跳过排空（不改变 M3 的行为）。
     */
    private final PluginDrainGate drainGate;

    /**
     * 版本目录与回滚锚点的文件系统管理（M4）。
     */
    private final PluginVersionBackupStore backupStore;

    /**
     * 构造安装编排服务。
     *
     * @param precheckService        兼容性预检
     * @param authorizationService   下载授权门控（二次校验用）
     * @param downloadService        M2 制品下载服务（换票 + 开流）
     * @param artifactDownloader     下载器（配置了对外地址时走真实 HTTP）
     * @param artifactSigner         制品验签器
     * @param artifactStore          制品落盘仓库
     * @param lifecycleManager       引擎生命周期管理器，可为 null（无引擎场景只跑落盘链路）
     * @param schemaMigrationRunner  schema 迁移执行器，可为 null
     * @param installMapper          安装态 Mapper
     * @param versionMapper          市场版本 Mapper
     * @param taskMapper             任务 Mapper
     * @param addonPluginSyncService 站点目录同步，可为 null
     * @param properties             编排配置
     * @param drainGate              升级排空窗口登记，可为 null（未接入引擎时跳过排空）
     */
    public PluginUpdateService(PluginCompatibilityPrecheckService precheckService,
                               PluginDownloadAuthorizationService authorizationService,
                               PluginArtifactDownloadService downloadService,
                               PluginArtifactDownloader artifactDownloader,
                               ArtifactSigner artifactSigner,
                               PluginArtifactStore artifactStore,
                               PluginLifecycleManager lifecycleManager,
                               SchemaMigrationRunner schemaMigrationRunner,
                               MallPluginInstallMapper installMapper,
                               MarketPluginVersionMapper versionMapper,
                               PluginUpdateTaskMapper taskMapper,
                               AddonPluginSyncService addonPluginSyncService,
                               UpdateProperties properties,
                               @Nullable PluginDrainGate drainGate) {
        this.precheckService = Objects.requireNonNull(precheckService, "兼容性预检服务不能为空");
        this.authorizationService = Objects.requireNonNull(authorizationService, "授权门控不能为空");
        this.downloadService = Objects.requireNonNull(downloadService, "制品下载服务不能为空");
        this.artifactDownloader = Objects.requireNonNull(artifactDownloader, "制品下载器不能为空");
        this.artifactSigner = artifactSigner;
        this.artifactStore = Objects.requireNonNull(artifactStore, "制品落盘仓库不能为空");
        this.lifecycleManager = lifecycleManager;
        this.schemaMigrationRunner = schemaMigrationRunner;
        this.installMapper = Objects.requireNonNull(installMapper, "MallPluginInstallMapper 不能为空");
        this.versionMapper = Objects.requireNonNull(versionMapper, "MarketPluginVersionMapper 不能为空");
        this.taskMapper = Objects.requireNonNull(taskMapper, "PluginUpdateTaskMapper 不能为空");
        this.addonPluginSyncService = addonPluginSyncService;
        this.properties = Objects.requireNonNull(properties, "UpdateProperties 不能为空");
        this.drainGate = drainGate;
        this.backupStore = new PluginVersionBackupStore(artifactStore);
    }

    // ==================================================================
    // 对外能力
    // ==================================================================

    /**
     * 安装插件（幂等 + 互斥 + 预检 + 二次授权 + 下载 + 三重校验 + 落盘 + schema + 装载）。
     *
     * @param identity   服务端解析的调用方身份（含站点实例）
     * @param pluginCode 插件编码
     * @param version    版本号，为空表示最新已发布版本
     * @param clientIp   客户端 IP（下载留痕）
     * @param userAgent  客户端 UA（下载留痕）
     * @return 安装结果
     * @throws UpdateRejectedException 任何一步被拒绝时抛出（携带 3001-3018 错误码）
     */
    public InstallResult install(PluginIdentity identity,
                                 String pluginCode,
                                 String version,
                                 String clientIp,
                                 String userAgent) {
        String code = pluginCode == null ? "" : pluginCode.trim();
        if (java.util.Set.of("crm","cms","erp","finance","geo","im","logging","mes","oa","orderfood","pm","rider","supplier","webpos").contains(code)) {
            throw new UpdateRejectedException(PluginDistErrorCode.MANIFEST_INVALID,
                    "服务型产品必须由独立安装代理启动容器：" + code);
        }
        if (code.isEmpty()) {
            throw new UpdateRejectedException(PluginDistErrorCode.ARTIFACT_NOT_FOUND, "插件编码不能为空");
        }
        String instanceId = identity == null || identity.getInstanceId() == null
                ? ""
                : identity.getInstanceId().trim();
        if (instanceId.isEmpty()) {
            throw new UpdateRejectedException(PluginDistErrorCode.NOT_LOGIN,
                    "未登录或无法识别站点实例，无法安装插件：" + code);
        }
        MarketPluginVersion target = resolveTargetVersion(code, version);
        String toVersion = target.getVersion();

        // ① 幂等 / ② 互斥：同一组合的非终态任务只能有一条
        PluginUpdateTask active = taskMapper.selectActive(instanceId, code,
                UpdateOpType.INSTALL.getCode(), toVersion);
        if (active != null) {
            throw new UpdateRejectedException(PluginDistErrorCode.TASK_IN_PROGRESS,
                    "该插件已有安装任务在进行中（taskId=" + active.getTaskId()
                            + "，状态 " + active.getState() + "），请勿重复点击");
        }
        PluginUpdateTask task = createTask(instanceId, code, toVersion,
                identity == null ? null : identity.getUserId());
        try {
            return runInstall(task, identity, target, clientIp, userAgent);
        } catch (UpdateRejectedException e) {
            // 各步骤已自行落 FAILED，这里只负责原样透传错误码
            throw e;
        } catch (RuntimeException e) {
            log.error("安装插件异常: plugin={} version={} instance={}", code, toVersion, instanceId, e);
            UpdateStep step = stepOf(task);
            failTask(task, "ENGINE_ERROR", "安装失败：" + e.getMessage(), step);
            throw new UpdateRejectedException(INTERNAL_ERROR, "安装失败：" + e.getMessage(), step);
        }
    }

    // ==================================================================
    // 升级与回滚（M4，方案 §5.3）
    // ==================================================================

    /**
     * 升级插件。
     *
     * <p>顺序严格按 §5.3：版本号校验 → 互斥 → 授权 + 预检 → 下载 + 三重校验 →
     * 备份 → 撤流量 + 排空 → 卸载（句柄重试）→ 落盘新版 → schema → 装载 → 健康检查 →
     * 写库 → 清理。<b>任何一步失败都不会让系统停在「旧版本已停、新版本没起」</b>：
     * 备份之后的失败统一走 {@link #rollbackAfterFailure}，不留下半生效状态。
     *
     * @param identity      服务端解析的调用方身份
     * @param pluginCode    插件编码
     * @param targetVersion 目标版本，为空表示最新已发布版本
     * @param clientIp      客户端 IP（下载留痕）
     * @param userAgent     客户端 UA（下载留痕）
     * @return 升级结果（含是否已自动回滚）
     * @throws UpdateRejectedException 任何一步被拒绝时抛出（携带 3001-3018 错误码）
     */
    public UpdateOutcome upgrade(PluginIdentity identity,
                                 String pluginCode,
                                 String targetVersion,
                                 String clientIp,
                                 String userAgent) {
        String code = requireCode(pluginCode);
        if (java.util.Set.of("crm","cms","erp","finance","geo","im","logging","mes","oa","orderfood","pm","rider","supplier","webpos").contains(code))
            throw new UpdateRejectedException(PluginDistErrorCode.MANIFEST_INVALID,"独立服务生命周期必须通过安装代理管理："+code);
        String instanceId = requireInstanceId(identity, code);
        MallPluginInstall current = findInstall(code);
        if (current == null || !Integer.valueOf(1).equals(current.getInstalled())) {
            throw new UpdateRejectedException(PluginDistErrorCode.ARTIFACT_NOT_FOUND,
                    "插件尚未安装，无法升级：" + code);
        }
        String fromVersion = current.getVersion();
        if (!StringUtils.hasText(fromVersion)) {
            throw new UpdateRejectedException(PluginDistErrorCode.ARTIFACT_NOT_FOUND,
                    "安装记录缺少当前版本，无法判断升级方向：" + code);
        }
        MarketPluginVersion target = resolveTargetVersion(code, targetVersion);
        String toVersion = target.getVersion();
        // 版本方向必须在下载之前判：下载一个注定要被拒的包既浪费带宽，
        // 也让「低版本覆盖高版本」这种危险操作在磁盘上留下痕迹。
        if (VersionComparison.compare(toVersion, fromVersion) <= 0) {
            throw new UpdateRejectedException(PluginDistErrorCode.VERSION_CONFLICT,
                    "目标版本必须高于当前版本（当前 " + fromVersion + "，目标 " + toVersion + "），拒绝升级：" + code);
        }
        requireNoActiveTask(instanceId, code, UpdateOpType.UPGRADE, toVersion);
        requireInstallUnlocked(current, code);
        lockInstall(current, UPDATE_STATUS_UPGRADING);

        PluginUpdateTask task = createTask(instanceId, code, UpdateOpType.UPGRADE, fromVersion, toVersion,
                identity == null ? null : identity.getUserId());
        try {
            return runUpgrade(task, identity, target, current, fromVersion, clientIp, userAgent);
        } catch (UpdateRejectedException e) {
            // 被拒分支可能是内部直接抛出（未解锁），也可能内部已自行落定。
            // 只在仍持锁时补解锁，避免覆盖内部已完成的回滚结果。
            unlockIfStillLocked(code, "升级被拒：" + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("升级插件异常: plugin={} {}->{} instance={}", code, fromVersion, toVersion, instanceId, e);
            UpdateStep step = stepOf(task);
            String message = "升级失败：" + e.getMessage();
            failTask(task, "ENGINE_ERROR", message, step);
            unlockInstall(code, UPDATE_STATUS_FAILED, message);
            throw new UpdateRejectedException(INTERNAL_ERROR, message, step);
        }
    }

    /**
     * 回滚插件（<b>豁免授权与下架状态</b>）。
     *
     * <p>为什么豁免：回滚是<b>履约动作</b>而不是新交易。租户买的是「能用某个版本」，
     * 升级升坏了却因为「授权刚好过期 / 该版本刚好被下架」而不让退回去，
     * 等于把平台自己的发布事故转嫁成租户的损失——这是所有结果里最坏的一种。
     * 因此这里<b>刻意不调用</b> {@code authorizationService.authorize}，
     * 也不校验目标版本的市场发布状态，只校验「本地确实有这个版本可用」。
     *
     * @param pluginCode    插件编码
     * @param targetVersion 回滚目标版本，为空时取安装记录登记的 {@code previous_version}
     * @param instanceId    站点实例（执行主体）
     * @param operatorId    操作人，可为 null
     * @return 回滚结果
     * @throws UpdateRejectedException 回滚点缺失（3018）或并发冲突（3016）等
     */
    public UpdateOutcome rollback(String pluginCode, String targetVersion,
                                  String instanceId, Long operatorId) {
        String code = requireCode(pluginCode);
        if (java.util.Set.of("crm","cms","erp","finance","geo","im","logging","mes","oa","orderfood","pm","rider","supplier","webpos").contains(code))
            throw new UpdateRejectedException(PluginDistErrorCode.MANIFEST_INVALID,"独立服务生命周期必须通过安装代理管理："+code);
        String instance = instanceId == null || instanceId.isBlank() ? "" : instanceId.trim();
        if (instance.isEmpty()) {
            throw new UpdateRejectedException(PluginDistErrorCode.NOT_LOGIN,
                    "无法识别站点实例，无法回滚插件：" + code);
        }
        MallPluginInstall current = findInstall(code);
        if (current == null) {
            throw new UpdateRejectedException(PluginDistErrorCode.ARTIFACT_NOT_FOUND,
                    "插件尚未安装，无法回滚：" + code);
        }
        String fromVersion = current.getVersion();
        String toVersion = StringUtils.hasText(targetVersion)
                ? targetVersion.trim()
                : current.getPreviousVersion();
        if (!StringUtils.hasText(toVersion)) {
            throw new UpdateRejectedException(PluginDistErrorCode.ROLLBACK_POINT_MISSING,
                    "未登记回滚锚点（previous_version 为空），无法回滚：" + code);
        }
        if (toVersion.equals(fromVersion)) {
            throw new UpdateRejectedException(PluginDistErrorCode.VERSION_CONFLICT,
                    "当前已处于目标版本 " + toVersion + "，无需回滚：" + code);
        }
        // 回滚点 = 磁盘上的版本目录，缺了就从备份恢复；两者都没有才是真没退路。
        Path targetJar = artifactStore.resolve(code, toVersion);
        if (!Files.isRegularFile(targetJar) && !backupStore.hasBackup(code, toVersion)) {
            throw new UpdateRejectedException(PluginDistErrorCode.ROLLBACK_POINT_MISSING,
                    "回滚目标版本不可用（磁盘与备份均无 " + code + "@" + toVersion + "）");
        }
        requireNoActiveTask(instance, code, UpdateOpType.ROLLBACK, toVersion);
        requireInstallUnlocked(current, code);
        lockInstall(current, UPDATE_STATUS_ROLLING_BACK);

        PluginUpdateTask task = createTask(instance, code, UpdateOpType.ROLLBACK, fromVersion, toVersion,
                operatorId);
        try {
            return runRollback(task, current, fromVersion, toVersion, null);
        } catch (UpdateRejectedException e) {
            unlockIfStillLocked(code, "回滚被拒：" + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("回滚插件异常: plugin={} {}->{} instance={}", code, fromVersion, toVersion, instance, e);
            UpdateStep step = stepOf(task);
            String message = "回滚失败：" + e.getMessage();
            failTask(task, "ROLLBACK_FAILED", message, step);
            unlockInstall(code, UPDATE_STATUS_FAILED, message);
            throw new UpdateRejectedException(INTERNAL_ERROR, message, step);
        }
    }

    /**
     * 卸载插件（带任务流水与补偿的编排版）。
     *
     * <p>与旧链路 {@code PluginCenterService.uninstall} 的分工：那个是「写状态位 + 删菜单」的
     * 轻量动作，本方法是<b>完整的卸载编排</b>——排空窗口、释放 ClassLoader、关闭插件子上下文、
     * 回收全部版本目录与备份、清除迁移审计、保留正式发布目录，全过程落
     * {@code hou_plugin_update_task}，失败可在「升级历史」里查到具体阶段与原因。
     *
     * <p><b>为什么卸载也要走排空</b>：卸载第一步就是撤路由，此时若有在途请求打进来，
     * 会拿到 404（路由已撤）而不是「功能已下线」。走 {@code PluginDrainGate} 让它们统一
     * 拿到 503 + Retry-After，前端可据此提示「功能维护中」而不是报未知错误。
     *
     * <p><b>为什么必须清迁移审计</b>：迁移按 {@code (plugin_code, version, script)} 幂等，
     * 审计行留着会让重装时判定「已迁移」直接跳过建表 —— 表现是插件装上了但一查数据报
     * 「表不存在」，现象离根因极远。见 {@code SchemaMigrationRunner#forgetMigrations}。
     *
     * <p><b>业务表默认不删</b>：{@code purgeData} 默认 false，只归档不删除都不做；
     * 显式传 true 时按 {@code plugin.yaml} 的 {@code tables.prefix} 把业务表
     * {@code RENAME} 成 {@code _bak_<时间戳>}（不是 DROP，留退路）。
     * 删表不可逆，且插件表可能被别的模块引用，因此绝不能默认执行。
     *
     * @param identity  服务端解析的调用方身份（含站点实例）
     * @param pluginCode 插件编码
     * @param purgeData 是否回收业务数据（归档业务表）
     * @return 卸载结果
     * @throws UpdateRejectedException 未安装（3010）、并发冲突（3016）等
     */
    public UpdateOutcome uninstall(PluginIdentity identity, String pluginCode, boolean purgeData) {
        String code = requireCode(pluginCode);
        if (java.util.Set.of("crm","cms","erp","finance","geo","im","logging","mes","oa","orderfood","pm","rider","supplier","webpos").contains(code))
            throw new UpdateRejectedException(PluginDistErrorCode.MANIFEST_INVALID,"独立服务生命周期必须通过安装代理管理："+code);
        String instanceId = requireInstanceId(identity, code);
        MallPluginInstall current = findInstall(code);
        if (current == null || !Integer.valueOf(1).equals(current.getInstalled())) {
            throw new UpdateRejectedException(PluginDistErrorCode.ARTIFACT_NOT_FOUND,
                    "插件尚未安装，无需卸载：" + code);
        }
        String fromVersion = current.getVersion();
        // UNINSTALL 没有「目标版本」，但任务表 to_version 是业务主键的一部分（用于并发互斥），
        // 这里回填被卸载的版本，保证互斥键非空且语义可解释：「从 v 卸载」。
        String toVersion = StringUtils.hasText(fromVersion) ? fromVersion : "";
        requireNoActiveTask(instanceId, code, UpdateOpType.UNINSTALL, toVersion);
        requireInstallUnlocked(current, code);
        lockInstall(current, UPDATE_STATUS_UNINSTALLING);

        PluginUpdateTask task = createTask(instanceId, code, UpdateOpType.UNINSTALL, fromVersion, toVersion,
                identity == null ? null : identity.getUserId());
        try {
            return runUninstall(task, current, fromVersion, purgeData);
        } catch (UpdateRejectedException e) {
            unlockIfStillLocked(code, "卸载被拒：" + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("卸载插件异常: plugin={} instance={}", code, instanceId, e);
            UpdateStep step = stepOf(task);
            String message = "卸载失败：" + e.getMessage();
            failTask(task, "ENGINE_ERROR", message, step);
            unlockInstall(code, UPDATE_STATUS_FAILED, message);
            throw new UpdateRejectedException(INTERNAL_ERROR, message, step);
        }
    }

    /**
     * 卸载流水线：排空 → 停插件 → 引擎卸载 → 清审计 → 回收版本目录 → 可选归档业务表 → 写状态。
     *
     * <p><b>失败策略</b>：卸载的每一步都是「尽力而为」——卸载的目标是让插件不再生效，
     * 只要引擎卸载成功就应当判定卸载成功。清理动作（删目录、归档表）失败只告警不回滚，
     * 否则一次磁盘占用就会让插件永远退不掉，那是把可恢复问题变成不可恢复问题。
     */
    private UpdateOutcome runUninstall(PluginUpdateTask task, MallPluginInstall install,
                                       String fromVersion, boolean purgeData) {
        String code = task.getPluginCode();
        updateStep(task, UpdateStep.STOP);
        beginDrain(code);
        boolean disabled = false;
        try {
            if (lifecycleManager != null) {
                lifecycleManager.disable(code);
                disabled = true;
            }
            sleepDrainWindow();
            if (lifecycleManager != null) {
                FileSystemHandleRetry.execute("卸载插件 " + code,
                        properties.getHandleRetryAttempts(),
                        properties.getHandleRetryBackoffMs(),
                        () -> lifecycleManager.uninstall(code));
            }
        } catch (Exception e) {
            restoreTraffic(code, disabled);
            endDrain(code);
            String message = "停止插件失败（jar 句柄未释放）：" + e.getMessage();
            failTask(task, HANDLE_LOCKED, message, UpdateStep.STOP);
            unlockInstall(code, UPDATE_STATUS_FAILED, message);
            throw new UpdateRejectedException(INTERNAL_ERROR, message, UpdateStep.STOP);
        }
        try {
            int archivedTables = 0;
            if (purgeData) {
                archivedTables = archivePluginTables(code, fromVersion);
            }
            if (schemaMigrationRunner != null) {
                schemaMigrationRunner.forgetMigrations(code);
            }
            int removedDirs = removeAllVersionDirs(code);
            markUninstalled(install, code, fromVersion);
            finishTask(task, UpdateTaskState.SUCCESS, null, null, UpdateStep.STOP);
            // hou_addon_plugin 是正式发布目录，卸载不能软删目录行；安装态只由
            // hou_addon_plugin_install 表达，未安装商品仍需留在插件中心。
            log.info("插件已卸载: plugin={} version={} 回收版本目录={} 归档业务表={}",
                    code, fromVersion, removedDirs, archivedTables);
            return new UpdateOutcome(task.getTaskId(), code, task.getOpType(), fromVersion, "",
                    "", UpdateTaskState.SUCCESS.getCode(), UpdateStep.STOP.getCode(),
                    true, null, null);
        } catch (RuntimeException e) {
            String message = "卸载后清理失败（插件已卸载）：" + e.getMessage();
            log.error("卸载清理失败: plugin={} : {}", code, e.toString(), e);
            failTask(task, "CLEANUP_FAILED", message, UpdateStep.STOP);
            unlockInstall(code, UPDATE_STATUS_FAILED, message);
            throw new UpdateRejectedException(INTERNAL_ERROR, message, UpdateStep.STOP);
        } finally {
            endDrain(code);
        }
    }

    /**
     * 回收插件的全部版本目录与备份目录。
     *
     * @return 成功删除的版本目录数
     */
    private int removeAllVersionDirs(String code) {
        int removed = 0;
        for (String version : artifactStore.listVersions(code)) {
            try {
                if (artifactStore.deleteVersion(code, version)) {
                    removed++;
                }
            } catch (IOException | RuntimeException e) {
                log.warn("删除版本目录失败 {}/{}：{}", code, version, e.getMessage());
            }
        }
        for (String backup : artifactStore.listBackups(code)) {
            try {
                backupStore.deleteBackup(code, artifactStore.pluginsRoot().resolve(code).resolve(backup));
            } catch (RuntimeException e) {
                log.warn("删除备份目录失败 {}/{}：{}", code, backup, e.getMessage());
            }
        }
        try {
            // 兜底删整目录：版本目录之外还可能有 frontend/ 等附属产物，
            // 逐个删版本清不干净。此处插件已被彻底卸载，不存在需要保留的回滚锚点。
            artifactStore.delete(code);
        } catch (IOException | RuntimeException e) {
            log.warn("删除插件目录失败 plugin={}：{}", code, e.getMessage());
        }
        return removed;
    }

    /**
     * 归档插件业务表（按 {@code plugin.yaml} 的 {@code tables.prefix}）。
     *
     * <p>取不到前缀直接返回 0：<b>宁可不归档，也绝不按猜测的表名去 RENAME</b>。
     *
     * @return 归档的表数量
     */
    private int archivePluginTables(String code, String version) {
        if (schemaMigrationRunner == null) {
            return 0;
        }
        String prefix = resolveTablePrefix(code, version);
        if (!StringUtils.hasText(prefix)) {
            log.warn("未取到插件表名前缀，跳过业务表归档: plugin={} version={}", code, version);
            return 0;
        }
        try {
            return schemaMigrationRunner.archiveTables(prefix);
        } catch (RuntimeException e) {
            log.error("归档插件业务表失败: plugin={} prefix={} : {}", code, prefix, e.getMessage());
            return 0;
        }
    }

    /**
     * 从插件包内 {@code plugin.yaml} 解析 {@code tables.prefix}。
     *
     * @return 表名前缀；解析失败返回 null
     */
    private String resolveTablePrefix(String code, String version) {
        try {
            Path jar = artifactStore.resolve(code, version == null ? "" : version);
            if (!Files.isRegularFile(jar)) {
                return null;
            }
            try (JarFile jarFile = new JarFile(jar.toFile())) {
                JarEntry entry = jarFile.getJarEntry("plugin.yaml");
                if (entry == null) {
                    return null;
                }
                try (InputStream in = jarFile.getInputStream(entry)) {
                    Map<String, Object> declared = new Yaml().load(in);
                    return extractTablePrefix(declared);
                }
            }
        } catch (RuntimeException | IOException e) {
            log.warn("解析插件表名前缀失败: plugin={} : {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * 从已解析的 plugin.yaml 里取 {@code tables.prefix}。
     *
     * <p>单独抽出来是因为 YAML 结构是 {@code Map<String, Object>} 的层层嵌套，
     * 内联写会让类型判断与取值逻辑混在一起，且 {@code tables} 可能是缺失、null 或非 Map。
     */
    @SuppressWarnings("unchecked")
    private static String extractTablePrefix(Map<String, Object> declared) {
        if (declared == null || declared.isEmpty()) {
            return null;
        }
        Object tables = declared.get("tables");
        if (!(tables instanceof Map<?, ?> tableMap)) {
            return null;
        }
        Object prefix = ((Map<String, Object>) tableMap).get("prefix");
        if (prefix == null) {
            return null;
        }
        String text = prefix.toString().trim();
        return text.isEmpty() ? null : text;
    }

    /**
     * 落定卸载后的安装态：{@code installed=0 / enabled=0}，版本与回滚锚点清空。
     *
     * <p><b>为什么清 {@code previous_version}</b>：卸载后没有「上一个版本」的概念了，
     * 留着会让下次安装时误以为存在回滚锚点，回滚接口会指向一个已被删掉的目录。
     */
    private void markUninstalled(MallPluginInstall install, String code, String fromVersion) {
        install.setInstalled(0);
        install.setEnabled(0);
        install.setPreviousVersion(null);
        install.setUpgradableVersion(null);
        install.setUpdateStatus(UPDATE_STATUS_IDLE);
        install.setLastError(null);
        install.setUpdateTime(LocalDateTime.now());
        updateInstallRow(install);
        log.info("安装态已置为未安装: plugin={} 原版本={}", code, fromVersion);
    }

    /**
     * 孤儿任务扫描（M4，方案 §5.3 关键约定 4）：把「进程重启后仍是非终态」的任务收敛掉。
     *
     * <p>为什么必须有它：升级到一半进程被杀（发布、OOM、断电），任务会永远停在 RUNNING，
     * 而 {@code uk_active} 只对非终态生效——于是这台站点的这个插件<b>再也升不了级</b>，
     * 前端只会一直转圈。没有任何自动机制能解开这个锁，只能靠重启时扫一次。
     *
     * <p>判定口径：{@code start_time} 早于「当前时间 - 阈值」（默认 30 分钟）。
     * 阈值远大于一次真实升级耗时，因此不会误伤正在跑的任务。
     *
     * @return 处理的任务条数
     */
    public int scanOrphanTasks() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(properties.getOrphanTimeoutMinutes());
        List<PluginUpdateTask> orphans = taskMapper.selectOrphans(threshold);
        if (orphans == null || orphans.isEmpty()) {
            return 0;
        }
        int handled = 0;
        for (PluginUpdateTask orphan : orphans) {
            handled++;
            try {
                handleOrphanTask(orphan);
            } catch (RuntimeException e) {
                log.error("孤儿任务处理失败: taskId={} : {}", orphan.getTaskId(), e.toString(), e);
            }
        }
        log.info("孤儿任务扫描完成：共 {} 条", handled);
        return handled;
    }

    /**
     * 处理单条孤儿任务：能回滚就先回滚，再把原任务置 FAILED。
     *
     * <p>顺序不能反：先回滚是为了把系统恢复到可用状态；原任务无论回滚成功与否都要置终态
     * ——它代表的那次操作确实没有完成，把它继续留在 RUNNING 就失去了扫描的意义。
     */
    private void handleOrphanTask(PluginUpdateTask orphan) {
        String code = orphan.getPluginCode();
        String opType = orphan.getOpType();
        boolean rollbackable = UpdateOpType.UPGRADE.getCode().equals(opType)
                || UpdateOpType.ROLLBACK.getCode().equals(opType);
        String anchor = orphan.getFromVersion();
        if (rollbackable && StringUtils.hasText(anchor)) {
            // 孤儿任务自己占着 update_status 锁，先把这把陈旧的锁解掉，否则回滚会被 3016 拦住
            unlockInstall(code, UPDATE_STATUS_IDLE, null);
            log.warn("发现孤儿任务，尝试自动回滚: taskId={} plugin={} {}->{}",
                    orphan.getTaskId(), code, orphan.getFromVersion(), orphan.getToVersion());
            try {
                rollback(code, anchor, orphan.getInstanceId(), orphan.getOperatorId());
            } catch (RuntimeException e) {
                log.error("孤儿任务自动回滚失败，仅置 FAILED: taskId={} : {}", orphan.getTaskId(), e.getMessage());
                unlockInstall(code, UPDATE_STATUS_FAILED, "孤儿任务自动回滚失败：" + e.getMessage());
            }
        }
        failTask(orphan, ORPHAN_TIMEOUT,
                "进程重启时任务仍未结束，已判定为孤儿任务并置失败", stepOf(orphan));
    }

    /**
     * 执行升级流水线（建单之后的部分）。
     */
    private UpdateOutcome runUpgrade(PluginUpdateTask task,
                                     PluginIdentity identity,
                                     MarketPluginVersion target,
                                     MallPluginInstall current,
                                     String fromVersion,
                                     String clientIp,
                                     String userAgent) {
        String code = task.getPluginCode();
        String toVersion = task.getToVersion();

        // ④ 授权二次校验 + 预检：升级不是「已经买过就不用管」——
        //    新版本可能需要新的授权范围，也可能引入新依赖，两项都得重新判。
        updateStep(task, UpdateStep.COMPAT);
        DownloadDecision decision = authorizationService.authorize(code, toVersion, identity);
        if (!decision.isAllowed()) {
            failTask(task, String.valueOf(decision.getCode()), decision.getMessage(), UpdateStep.COMPAT);
            throw new UpdateRejectedException(decision.getCode(), decision.getMessage(), UpdateStep.COMPAT);
        }
        PrecheckResult pre = precheckService.precheck(code, toVersion);
        if (!pre.isPass()) {
            failTask(task, String.valueOf(pre.getReason()), pre.getDetail(), UpdateStep.COMPAT);
            throw new UpdateRejectedException(pre.getReason(), pre.getDetail(), UpdateStep.COMPAT);
        }

        // ⑤ 换票 + 下载；⑥ 三重校验。失败即止：这一步之前没动过磁盘，也没撤过流量，
        //    因此旧版本零影响，直接落 FAILED 返回即可。
        byte[] bytes;
        String md5;
        String sha256;
        try {
            updateStep(task, UpdateStep.DOWNLOAD);
            DownloadUrlResult ticket = downloadService.issueDownloadUrl(code, toVersion, identity, clientIp, userAgent);
            bytes = fetchBytes(ticket);
            updateStep(task, UpdateStep.VERIFY);
            md5 = ArtifactSignaturePayload.md5Hex(bytes);
            sha256 = ArtifactSignaturePayload.sha256Hex(bytes);
            verifyChecksum(md5, firstNonBlank(ticket.getMd5(), target.getJarMd5()), "MD5");
            verifyChecksum(sha256, firstNonBlank(ticket.getSha256(), target.getJarSha256()), "SHA-256");
            verifySize(bytes.length, ticket.getSizeBytes());
            verifySignature(target, code, toVersion, md5, sha256, bytes.length);
        } catch (UpdateRejectedException e) {
            failTask(task, String.valueOf(e.getCode()), e.getMessage(), e.getStep());
            throw e;
        }

        // ⑦ 备份当前版本目录（回滚锚点）
        updateStep(task, UpdateStep.BACKUP);
        Path backupDir = backupCurrentVersion(code, fromVersion);

        // ⑧ 撤流量 → 排空 → 卸载。从这里开始旧版本已经不提供服务，失败必须恢复流量。
        updateStep(task, UpdateStep.STOP);
        beginDrain(code);
        boolean disabled = false;
        try {
            if (lifecycleManager != null) {
                lifecycleManager.disable(code);
                disabled = true;
            }
            sleepDrainWindow();
            if (lifecycleManager != null) {
                FileSystemHandleRetry.execute("卸载插件 " + code,
                        properties.getHandleRetryAttempts(),
                        properties.getHandleRetryBackoffMs(),
                        () -> lifecycleManager.uninstall(code));
            }
        } catch (Exception e) {
            // 句柄释放不了就不升级：宁可本次失败，也不留「路由已撤、新版本没起」的半生效状态
            restoreTraffic(code, disabled);
            endDrain(code);
            String message = "停止旧版本失败（jar 句柄未释放）：" + e.getMessage();
            failTask(task, HANDLE_LOCKED, message, UpdateStep.STOP);
            unlockInstall(code, UPDATE_STATUS_FAILED, message);
            throw new UpdateRejectedException(INTERNAL_ERROR, message, UpdateStep.STOP);
        }

        // ⑨ 落盘新版 → ⑩ schema → ⑪ 装载 → ⑫ 健康检查
        Path jarPath = null;
        try {
            jarPath = storeJar(code, toVersion, bytes);
            task.setBackupPath(backupDir == null ? null : backupDir.toString());
            updateStep(task, UpdateStep.SCHEMA);
            if (schemaMigrationRunner != null) {
                try {
                    schemaMigrationRunner.migrateUpgrade(code, fromVersion, toVersion);
                } catch (RuntimeException e) {
                    deleteVersionDir(code, toVersion);
                    throw new UpgradeFailedException("数据迁移失败：" + e.getMessage(), UpdateStep.SCHEMA);
                }
            }
            updateStep(task, UpdateStep.START);
            if (lifecycleManager != null) {
                try {
                    lifecycleManager.install(code, jarPath, manifestOf(target));
                } catch (RuntimeException e) {
                    deleteVersionDir(code, toVersion);
                    throw new UpgradeFailedException("新版本装载失败：" + e.getMessage(), UpdateStep.START);
                }
            }
            updateStep(task, UpdateStep.HEALTH);
            if (lifecycleManager != null && !lifecycleManager.isHealthy(code)) {
                try {
                    lifecycleManager.uninstall(code);
                } catch (RuntimeException ignored) {
                    log.warn("健康检查失败后卸载新版本报错，已继续回滚: {}", code);
                }
                deleteVersionDir(code, toVersion);
                throw new UpgradeFailedException("新版本启动后健康检查未通过", UpdateStep.HEALTH);
            }
        } catch (UpgradeFailedException e) {
            return rollbackAfterFailure(task, current, fromVersion, toVersion, backupDir, e.getMessage());
        }

        // ⑬ 写库（最后一步）+ 清理 + 解锁
        try {
            applyUpgrade(current, fromVersion, toVersion, jarPath, md5, target);
            finishTask(task, UpdateTaskState.SUCCESS, null, null, UpdateStep.HEALTH);
        } catch (RuntimeException e) {
            return rollbackAfterFailure(task, current, fromVersion, toVersion, backupDir,
                    "安装登记失败：" + e.getMessage());
        }
        cleanupVersions(code, current);
        endDrain(code);
        return new UpdateOutcome(task.getTaskId(), code, UpdateOpType.UPGRADE.getCode(), fromVersion,
                toVersion, toVersion, UpdateTaskState.SUCCESS.getCode(), UpdateStep.HEALTH.getCode(),
                false, null, null);
    }

    /**
     * 执行回滚流水线（独立的 {@code /rollback} 接口与升级失败自动回滚共用）。
     *
     * @param task         任务（op_type 决定它是主动回滚还是自动回滚）
     * @param install      安装态记录
     * @param fromVersion  当前（要离开的）版本
     * @param toVersion    回滚目标版本
     * @param backupDir    升级前备份目录，主动回滚时为 null
     * @return 回滚结果
     */
    private UpdateOutcome runRollback(PluginUpdateTask task,
                                      MallPluginInstall install,
                                      String fromVersion,
                                      String toVersion,
                                      Path backupDir) {
        String code = task.getPluginCode();
        updateStep(task, UpdateStep.STOP);
        beginDrain(code);
        boolean disabled = false;
        try {
            if (lifecycleManager != null) {
                lifecycleManager.disable(code);
                disabled = true;
            }
            sleepDrainWindow();
            if (lifecycleManager != null) {
                FileSystemHandleRetry.execute("卸载插件 " + code,
                        properties.getHandleRetryAttempts(),
                        properties.getHandleRetryBackoffMs(),
                        () -> lifecycleManager.uninstall(code));
            }
        } catch (Exception e) {
            restoreTraffic(code, disabled);
            endDrain(code);
            String message = "停止当前版本失败（jar 句柄未释放）：" + e.getMessage();
            failTask(task, HANDLE_LOCKED, message, UpdateStep.STOP);
            unlockInstall(code, UPDATE_STATUS_FAILED, message);
            throw new UpdateRejectedException(INTERNAL_ERROR, message, UpdateStep.STOP);
        }
        try {
            restoreVersionIfMissing(code, toVersion);
            if (schemaMigrationRunner != null) {
                // 被回滚掉的是 fromVersion（它的 DDL 要撤销），审计记在 toVersion 名下
                schemaMigrationRunner.migrateRollback(code, fromVersion, toVersion, fromVersion, null);
            }
            Path targetJar = artifactStore.resolve(code, toVersion);
            if (lifecycleManager != null) {
                lifecycleManager.install(code, targetJar, plainManifest(code, toVersion));
                if (!lifecycleManager.isHealthy(code)) {
                    try {
                        lifecycleManager.uninstall(code);
                    } catch (RuntimeException ignored) {
                        log.warn("回滚后健康检查失败，卸载报错已忽略: {}", code);
                    }
                    throw new IllegalStateException("回滚后版本 " + toVersion + " 健康检查未通过");
                }
            }
            applyRollback(install, toVersion, fromVersion, targetJar);
            finishTask(task, UpdateTaskState.ROLLED_BACK, null, null, UpdateStep.STOP);
            if (backupDir != null) {
                backupStore.deleteBackup(code, backupDir);
            }
            return new UpdateOutcome(task.getTaskId(), code, task.getOpType(), fromVersion, toVersion,
                    toVersion, UpdateTaskState.ROLLED_BACK.getCode(), UpdateStep.STOP.getCode(),
                    true, null, null);
        } catch (RuntimeException e) {
            String message = "回滚失败，需人工介入：" + e.getMessage();
            log.error("回滚失败: plugin={} {}->{} : {}", code, fromVersion, toVersion, e.toString(), e);
            failTask(task, "ROLLBACK_FAILED", message, UpdateStep.STOP);
            unlockInstall(code, UPDATE_STATUS_FAILED, message);
            throw new UpdateRejectedException(INTERNAL_ERROR, message, UpdateStep.STOP);
        } finally {
            endDrain(code);
        }
    }

    /**
     * 升级失败的统一回滚路径（§5.3 红框部分，schema 失败与装载失败共用）。
     *
     * <p>与 {@link #runRollback} 的区别：它不抛异常——升级已经失败过一次，
     * 回滚再抛只会让调用方看到「第二次失败」而丢失第一次的真实原因。
     * 回滚成功就是 ROLLED_BACK，回滚失败才是 FAILED（此时必须人工介入）。
     */
    private UpdateOutcome rollbackAfterFailure(PluginUpdateTask task,
                                               MallPluginInstall install,
                                               String targetVersion,
                                               String failedVersion,
                                               Path backupDir,
                                               String reason) {
        String code = task.getPluginCode();
        updateStep(task, UpdateStep.STOP);
        try {
            if (lifecycleManager != null) {
                lifecycleManager.uninstall(code);
            }
            restoreVersionIfMissing(code, targetVersion);
            if (schemaMigrationRunner != null) {
                schemaMigrationRunner.migrateRollback(code, failedVersion, targetVersion, failedVersion, null);
            }
            Path targetJar = artifactStore.resolve(code, targetVersion);
            if (lifecycleManager != null) {
                lifecycleManager.install(code, targetJar, plainManifest(code, targetVersion));
                if (!lifecycleManager.isHealthy(code)) {
                    try {
                        lifecycleManager.uninstall(code);
                    } catch (RuntimeException ignored) {
                        log.warn("升级回滚后健康检查失败，卸载报错已忽略: {}", code);
                    }
                    throw new IllegalStateException("回滚后版本 " + targetVersion + " 健康检查未通过");
                }
            }
            applyRollback(install, targetVersion, failedVersion, targetJar);
            finishTask(task, UpdateTaskState.ROLLED_BACK, null, reason, UpdateStep.STOP);
            log.warn("升级失败已自动回滚: plugin={} {}->{} 原因={}", code, targetVersion, failedVersion, reason);
            return new UpdateOutcome(task.getTaskId(), code, task.getOpType(), targetVersion, failedVersion,
                    targetVersion, UpdateTaskState.ROLLED_BACK.getCode(), UpdateStep.STOP.getCode(),
                    true, null, reason);
        } catch (RuntimeException e) {
            String message = "升级失败且自动回滚失败，需人工介入：" + e.getMessage();
            log.error("自动回滚失败: plugin={} target={} failed={} : {}", code, targetVersion, failedVersion,
                    e.toString(), e);
            failTask(task, "ROLLBACK_FAILED", message, UpdateStep.STOP);
            unlockInstall(code, UPDATE_STATUS_FAILED, message);
            return new UpdateOutcome(task.getTaskId(), code, task.getOpType(), targetVersion, failedVersion,
                    targetVersion, UpdateTaskState.FAILED.getCode(), UpdateStep.STOP.getCode(),
                    false, "ROLLBACK_FAILED", message);
        } finally {
            endDrain(code);
        }
    }

    /**
     * 兼容性预检（独立接口，同时被安装流程内部强制调用）。
     *
     * @param pluginCode 插件编码
     * @param version    版本号，可为空
     * @return 预检结果
     */
    public PrecheckResult precheck(String pluginCode, String version) {
        return precheckService.precheck(pluginCode, version);
    }

    /**
     * 查询任务状态：优先按 taskId，其次按「站点 + 插件」取最近一条。
     *
     * @param taskId      任务号，可为空
     * @param pluginCode  插件编码，可为空
     * @param instanceId  站点实例，可为空
     * @return 任务状态，不存在返回 null
     */
    public TaskStatus taskStatus(String taskId, String pluginCode, String instanceId) {
        if (StringUtils.hasText(taskId)) {
            PluginUpdateTask task = taskMapper.selectByTaskId(taskId.trim());
            return task == null ? null : new TaskStatus(task);
        }
        if (!StringUtils.hasText(pluginCode) || !StringUtils.hasText(instanceId)) {
            return null;
        }
        PluginUpdateTask task = taskMapper.selectOne(new LambdaQueryWrapper<PluginUpdateTask>()
                .eq(PluginUpdateTask::getInstanceId, instanceId.trim())
                .eq(PluginUpdateTask::getPluginCode, pluginCode.trim())
                .orderByDesc(PluginUpdateTask::getId)
                .last("limit 1"));
        return task == null ? null : new TaskStatus(task);
    }

    /**
     * 任务历史（分页）。
     *
     * @param pluginCode 插件编码，可为空
     * @param instanceId 站点实例，可为空
     * @param page       页码，从 1 开始
     * @param pageSize   每页条数，上限 100
     * @return 分页结果
     */
    public Map<String, Object> history(String pluginCode, String instanceId, int page, int pageSize) {
        int pageNo = page <= 0 ? 1 : page;
        int size = pageSize <= 0 ? 20 : Math.min(pageSize, 100);
        LambdaQueryWrapper<PluginUpdateTask> wrapper = new LambdaQueryWrapper<PluginUpdateTask>()
                .eq(StringUtils.hasText(instanceId), PluginUpdateTask::getInstanceId,
                        instanceId == null ? null : instanceId.trim())
                .eq(StringUtils.hasText(pluginCode), PluginUpdateTask::getPluginCode,
                        pluginCode == null ? null : pluginCode.trim())
                .orderByDesc(PluginUpdateTask::getId);
        IPage<PluginUpdateTask> paged = taskMapper.selectPage(new Page<>(pageNo, size), wrapper);
        List<TaskStatus> items = paged == null || paged.getRecords() == null
                ? List.of()
                : paged.getRecords().stream().map(TaskStatus::new).toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", items);
        result.put("page", pageNo);
        result.put("pageSize", size);
        result.put("total", paged == null ? items.size() : paged.getTotal());
        result.put("pages", paged == null ? (items.isEmpty() ? 0 : 1) : paged.getPages());
        return result;
    }

    // ==================================================================
    // 安装主流程
    // ==================================================================

    /**
     * 执行安装流水线。每一步失败都要先落任务终态再抛，保证「失败必有留痕」。
     *
     * @param task       已建单的任务
     * @param identity   调用方身份
     * @param target     目标版本
     * @param clientIp   客户端 IP
     * @param userAgent  客户端 UA
     * @return 安装结果
     */
    private InstallResult runInstall(PluginUpdateTask task,
                                     PluginIdentity identity,
                                     MarketPluginVersion target,
                                     String clientIp,
                                     String userAgent) {
        String code = task.getPluginCode();
        String toVersion = task.getToVersion();

        // ③ 兼容性预检（不通过绝不下载、绝不改安装态）
        updateStep(task, UpdateStep.COMPAT);
        PrecheckResult pre = precheckService.precheck(code, toVersion);
        if (!pre.isPass()) {
            failTask(task, String.valueOf(pre.getReason()), pre.getDetail(), UpdateStep.COMPAT);
            throw new UpdateRejectedException(pre.getReason(), pre.getDetail(), UpdateStep.COMPAT);
        }

        // ④ 二次授权校验：防止「签发下载链接后、安装前」这个窗口里 license 被吊销
        DownloadDecision decision = authorizationService.authorize(code, toVersion, identity);
        if (!decision.isAllowed()) {
            failTask(task, String.valueOf(decision.getCode()), decision.getMessage(), UpdateStep.COMPAT);
            throw new UpdateRejectedException(decision.getCode(), decision.getMessage(), UpdateStep.COMPAT);
        }

        // ⑤ 换票 + 下载
        // ⑥ 三重校验（全部在内存完成，失败时磁盘上没有任何新产物）
        // 这两步统一收口：校验失败与下载失败一样，都要把任务落 FAILED——
        // 「制品被篡改」是最需要留痕的失败原因，不能因为它是「校验」就只抛异常不记账。
        byte[] bytes;
        String md5;
        String sha256;
        try {
            updateStep(task, UpdateStep.DOWNLOAD);
            DownloadUrlResult ticket = downloadService.issueDownloadUrl(code, toVersion, identity, clientIp, userAgent);
            bytes = fetchBytes(ticket);
            updateStep(task, UpdateStep.VERIFY);
            md5 = ArtifactSignaturePayload.md5Hex(bytes);
            sha256 = ArtifactSignaturePayload.sha256Hex(bytes);
            verifyChecksum(md5, firstNonBlank(ticket.getMd5(), target.getJarMd5()), "MD5");
            verifyChecksum(sha256, firstNonBlank(ticket.getSha256(), target.getJarSha256()), "SHA-256");
            verifySize(bytes.length, ticket.getSizeBytes());
            verifySignature(target, code, toVersion, md5, sha256, bytes.length);
        } catch (UpdateRejectedException e) {
            failTask(task, String.valueOf(e.getCode()), e.getMessage(), e.getStep());
            throw e;
        }

        // ⑦ 已装同 code 必须先卸载：引擎 install 内部直接 loadPlugin，
        //    同 code 二次装载必然失败（没有 upgrade API，热替换不存在）
        MallPluginInstall existing = findInstall(code);
        String previousVersion = existing == null ? null : existing.getVersion();
        if (existing != null && Integer.valueOf(1).equals(existing.getInstalled()) && lifecycleManager != null) {
            updateStep(task, UpdateStep.STOP);
            lifecycleManager.uninstall(code);
        }

        // ⑧ 落盘（先写文件：schema 与装载都依赖它）
        Path jarPath = storeJar(code, toVersion, bytes);

        // ⑨ 执行包内 schema（真实执行源是 jar 内的 plugin.yaml tables.migrate，不是库里的列）
        //
        //    ⚠️ 这里会「重复执行」一次：引擎 PluginLifecycleManager.install() 内部也会回调
        //    SchemaMigrationRunner.migrate()。之所以保留这一次显式调用，是因为
        //    「schema 失败必须能把任务落到 FAILED 并删掉已落盘的版本目录」——
        //    交给引擎的话，失败点落在 install() 内部，本类拿不到可编排的补偿时机。
        //    重复执行是安全的：SchemaMigrationRunner 以 hou_addon_plugin_migration 的
        //    countSuccess(code, version) > 0 为幂等判据，第二次进来直接跳过；
        //    真正执行 SQL 的脚本本身也必须写成幂等（CREATE TABLE IF NOT EXISTS 等），
        //    这是插件作者的契约，不在这里兜底。
        //    代价：多一次 jar 读取与一次 count 查询，可忽略。
        updateStep(task, UpdateStep.SCHEMA);
        if (schemaMigrationRunner != null) {
            try {
                schemaMigrationRunner.migrate(code, toVersion);
            } catch (RuntimeException e) {
                deleteVersionDir(code, toVersion);
                String message = "数据迁移失败：" + e.getMessage();
                failTask(task, "SCHEMA_FAILED", message, UpdateStep.SCHEMA);
                throw new UpdateRejectedException(INTERNAL_ERROR, message, UpdateStep.SCHEMA);
            }
        }

        // ⑩ 装载启动
        updateStep(task, UpdateStep.START);
        if (lifecycleManager != null) {
            try {
                lifecycleManager.install(code, jarPath, manifestOf(target));
            } catch (RuntimeException e) {
                deleteVersionDir(code, toVersion);
                restoreBestEffort(code, previousVersion);
                String message = "插件装载失败：" + e.getMessage();
                failTask(task, "ENGINE_INSTALL_FAILED", message, UpdateStep.START);
                throw new UpdateRejectedException(INTERNAL_ERROR, message, UpdateStep.START);
            }
        }

        // ⑪ 健康检查：装载成功 ≠ 可用，注册表与引擎不同步时必须判失败
        updateStep(task, UpdateStep.HEALTH);
        if (lifecycleManager != null && !lifecycleManager.isHealthy(code)) {
            lifecycleManager.uninstall(code);
            deleteVersionDir(code, toVersion);
            restoreBestEffort(code, previousVersion);
            String message = "插件启动后健康检查未通过，已回退";
            failTask(task, "HEALTH_CHECK_FAILED", message, UpdateStep.HEALTH);
            throw new UpdateRejectedException(INTERNAL_ERROR, message, UpdateStep.HEALTH);
        }

        // ⑫ 最后写库：库写失败要手工回退前面已经成功的文件系统与引擎动作
        try {
            upsertInstall(code, toVersion, jarPath.toString(), md5);
            if (addonPluginSyncService != null) {
                addonPluginSyncService.syncFromPluginYaml(target.getPluginYaml());
            }
            finishTask(task, UpdateTaskState.SUCCESS, null, null, UpdateStep.HEALTH);
        } catch (RuntimeException e) {
            if (lifecycleManager != null) {
                lifecycleManager.uninstall(code);
            }
            deleteVersionDir(code, toVersion);
            String message = "安装登记失败，已回滚：" + e.getMessage();
            failTask(task, "PERSIST_FAILED", message, UpdateStep.HEALTH);
            throw new UpdateRejectedException(INTERNAL_ERROR, message, UpdateStep.HEALTH);
        }
        return new InstallResult(task.getTaskId(), code, toVersion, task.getState(), task.getStep(),
                jarPath.toString(), bytes.length, md5);
    }

    // ==================================================================
    // 下载与校验
    // ==================================================================

    /**
     * 取制品字节。配置了 {@code houcloud.update.public-base-url} 时走真实 HTTP（
     * 复用 {@link PluginArtifactDownloader} 的 64MB 上限与边读边计数），
     * 否则进程内直接用令牌开流（不经过网络，容器里自环地址往往不可知）。
     *
     * @param ticket 换票结果
     * @return 制品字节
     */
    private byte[] fetchBytes(DownloadUrlResult ticket) {
        try {
            // 进程内安装优先走对象存储预签名，减轻 base 回源；安装代理仍只用相对 raw。
            if (StringUtils.hasText(ticket.getCloudUrl())) {
                PluginArtifactDownloader.Artifact artifact =
                        artifactDownloader.download(ticket.getCloudUrl());
                return artifact.getContent();
            }
            String base = properties.getPublicBaseUrl();
            if (StringUtils.hasText(base)) {
                PluginArtifactDownloader.Artifact artifact =
                        artifactDownloader.download(base + ticket.getUrl());
                return artifact.getContent();
            }
            RawArtifact raw = downloadService.openByToken(ticket.getToken(), null);
            try (ArtifactStream stream = raw.getStream()) {
                return readCapped(stream.getStream(), properties.getMaxArtifactBytes(),
                        ticket.getPluginCode(), ticket.getVersion());
            }
        } catch (IOException e) {
            String message = "下载制品失败：" + e.getMessage();
            if (message.contains("超出体积上限")) {
                throw new UpdateRejectedException(PluginDistErrorCode.SIZE_EXCEEDED, message, UpdateStep.DOWNLOAD);
            }
            throw new UpdateRejectedException(INTERNAL_ERROR, message, UpdateStep.DOWNLOAD);
        }
    }

    /**
     * 边读边计数，超过上限立即中断（恶意大包不能打满内存）。
     *
     * @param in       输入流
     * @param maxBytes 体积上限
     * @param code     插件编码（错误文案用）
     * @param version  版本（错误文案用）
     * @return 读到的字节
     * @throws IOException 读取失败
     */
    private static byte[] readCapped(InputStream in, long maxBytes, String code, String version) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[READ_BUFFER];
        long total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new UpdateRejectedException(PluginDistErrorCode.SIZE_EXCEEDED,
                        "制品体积超过上限（>" + maxBytes + " 字节），拒绝安装：" + code + " " + version,
                        UpdateStep.DOWNLOAD);
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    /**
     * 摘要比对。期望值为空按失败处理——「没登记摘要」与「摘要对不上」同样不可安装，
     * 放行等于放弃防篡改。
     *
     * @param actual   实际摘要
     * @param expected 登记摘要
     * @param algorithm 算法名（错误文案用）
     */
    private void verifyChecksum(String actual, String expected, String algorithm) {
        if (expected == null || expected.isBlank()) {
            throw new UpdateRejectedException(PluginDistErrorCode.CHECKSUM_MISMATCH,
                    "制品未登记 " + algorithm + "，无法校验完整性，拒绝安装", UpdateStep.VERIFY);
        }
        if (!expected.trim().equalsIgnoreCase(actual)) {
            throw new UpdateRejectedException(PluginDistErrorCode.CHECKSUM_MISMATCH,
                    "制品 " + algorithm + " 校验失败（期望 " + expected.trim() + "，实际 " + actual
                            + "），已保留原版本不做任何改动", UpdateStep.VERIFY);
        }
    }

    /**
     * 体积对账：制品记录登记了大小就必须一致，防止「声明 1KB 实际 60MB」的绕过。
     *
     * @param actual   实际字节数
     * @param declared 登记字节数
     */
    private void verifySize(long actual, long declared) {
        if (declared > 0 && actual != declared) {
            throw new UpdateRejectedException(PluginDistErrorCode.CHECKSUM_MISMATCH,
                    "制品体积与登记值不一致（登记 " + declared + "，实际 " + actual + "）", UpdateStep.VERIFY);
        }
    }

    /**
     * Ed25519 验签。签名串把 sha256 / code / version / md5 / size 全部纳入，
     * 因此「把旧版本的合法签名贴到新版本 jar 上」也会失败。
     *
     * @param target  版本行（提供 kid 与签名）
     * @param code    插件编码
     * @param version 版本
     * @param md5     实际 md5
     * @param sha256  实际 sha256
     * @param size    实际字节数
     */
    private void verifySignature(MarketPluginVersion target, String code, String version,
                                 String md5, String sha256, long size) {
        String kid = target.getSignatureKid();
        String signature = target.getJarSignature();
        if (!StringUtils.hasText(kid) || !StringUtils.hasText(signature) || artifactSigner == null) {
            if (!properties.isRequireSignature()) {
                log.warn("制品未签名且已关闭强制验签，放行安装: {} {}（kid={}）", code, version, kid);
                return;
            }
            throw new UpdateRejectedException(PluginDistErrorCode.SIGNATURE_INVALID,
                    "制品缺少可用签名（kid 或签名为空，或验签器未装配），拒绝安装："
                            + code + " " + version, UpdateStep.VERIFY);
        }
        String payload = ArtifactSignaturePayload.build(sha256, code, version, md5, size);
        if (!artifactSigner.verify(kid, ArtifactSignaturePayload.toSignBytes(payload), signature)) {
            throw new UpdateRejectedException(PluginDistErrorCode.SIGNATURE_INVALID,
                    "制品签名校验失败（kid=" + kid + "），已保留原版本不做任何改动", UpdateStep.VERIFY);
        }
    }

    // ==================================================================
    // 文件与引擎动作
    // ==================================================================

    /**
     * 落盘安装包。
     *
     * @param code    插件编码
     * @param version 版本
     * @param bytes   制品字节
     * @return 落盘路径
     */
    private Path storeJar(String code, String version, byte[] bytes) {
        try {
            return artifactStore.store(code, version, bytes);
        } catch (IOException e) {
            throw new UpdateRejectedException(INTERNAL_ERROR,
                    "制品落盘失败：" + e.getMessage(), UpdateStep.DOWNLOAD);
        }
    }

    /**
     * 只删本次版本目录，<b>绝不删整个插件目录</b>——历史版本是回滚锚点，删了就无法恢复。
     *
     * @param code    插件编码
     * @param version 版本
     */
    private void deleteVersionDir(String code, String version) {
        try {
            Path jar = artifactStore.resolve(code, version);
            Files.deleteIfExists(jar);
            Path versionDir = jar.getParent();
            if (versionDir != null) {
                Files.deleteIfExists(versionDir);
            }
        } catch (IOException e) {
            log.warn("清理版本目录失败 {}/{}：{}", code, version, e.getMessage());
        }
    }

    /**
     * 尽力恢复上一版本：新版本装载失败后，旧 jar 还在版本目录里，重装它比「插件整体消失」好得多。
     *
     * @param code            插件编码
     * @param previousVersion 上一版本，为空表示全新安装无需恢复
     */
    private void restoreBestEffort(String code, String previousVersion) {
        if (lifecycleManager == null || !StringUtils.hasText(previousVersion)) {
            return;
        }
        try {
            Path oldJar = artifactStore.resolve(code, previousVersion);
            if (!Files.isRegularFile(oldJar)) {
                log.warn("无法恢复上一版本，安装包不存在: {}/{}", code, previousVersion);
                return;
            }
            PluginManifest manifest = new PluginManifest();
            manifest.setCode(code);
            manifest.setVersion(previousVersion);
            manifest.setOwner("official");
            lifecycleManager.install(code, oldJar, manifest);
            log.info("新版本装载失败，已尽力恢复上一版本 {}/{}", code, previousVersion);
        } catch (RuntimeException e) {
            log.error("恢复上一版本失败，需人工介入: {}/{}", code, previousVersion, e);
        }
    }

    /**
     * 由版本行构建引擎清单（owner / runtime 取自包内 plugin.yaml，与引擎校验同一事实来源）。
     *
     * @param target 版本行
     * @return 插件清单
     */
    private PluginManifest manifestOf(MarketPluginVersion target) {
        PluginManifest manifest = new PluginManifest();
        manifest.setCode(target.getPluginCode());
        manifest.setVersion(target.getVersion());
        Map<String, Object> declaration = parseDeclaration(target.getPluginYaml());
        Object owner = declaration.get("owner");
        String ownerText = owner == null ? null : String.valueOf(owner).trim();
        manifest.setOwner(StringUtils.hasText(ownerText) ? ownerText : "official");
        Object runtime = declaration.get("runtime");
        if (runtime != null && !(runtime instanceof Map<?, ?>)) {
            manifest.setRuntime(String.valueOf(runtime).trim());
        }
        Object minFramework = declaration.get("minFrameworkVersion");
        if (minFramework != null) {
            manifest.setMinFrameworkVersion(String.valueOf(minFramework).trim());
        }
        // 引擎 resolveManifest 会用 PluginServiceOwnership.resolve(supplied) 校验市场侧清单：
        // 这里不回填 hostService/host 的话，supplied 的 hostService=null 会让
        // requireService 直接抛 "Unknown hostService: null"，安装永远走不到装载。
        Object hostService = declaration.get("hostService");
        if (hostService != null) {
            manifest.setHostService(String.valueOf(hostService).trim());
        }
        Object host = declaration.get("host");
        if (host != null) {
            manifest.setHost(String.valueOf(host).trim());
        }
        return manifest;
    }

    /**
     * 解析 plugin.yaml；缺文件或解析失败一律返回空 Map（清单细节由引擎从包内再读一次为准）。
     *
     * @param yaml 清单文本
     * @return 键值映射
     */
    private Map<String, Object> parseDeclaration(String yaml) {
        if (!StringUtils.hasText(yaml)) {
            return Map.of();
        }
        try {
            Object loaded = new Yaml().load(yaml);
            if (loaded instanceof Map<?, ?> raw) {
                Map<String, Object> result = new LinkedHashMap<>();
                raw.forEach((key, value) -> result.put(String.valueOf(key), value));
                return result;
            }
        } catch (RuntimeException e) {
            log.warn("plugin.yaml 解析失败，按默认清单处理: {}", e.getMessage());
        }
        return Map.of();
    }

    // ==================================================================
    // 持久化
    // ==================================================================

    /**
     * 定位目标版本：指定版本精确匹配，未指定取最新已发布版本。
     *
     * @param pluginCode 插件编码
     * @param version    版本号，可为空
     * @return 版本行
     */
    private MarketPluginVersion resolveTargetVersion(String pluginCode, String version) {
        LambdaQueryWrapper<MarketPluginVersion> wrapper = new LambdaQueryWrapper<MarketPluginVersion>()
                .eq(MarketPluginVersion::getPluginCode, pluginCode);
        if (StringUtils.hasText(version)) {
            wrapper.eq(MarketPluginVersion::getVersion, version.trim());
        } else {
            wrapper.eq(MarketPluginVersion::getStatus, VERSION_PUBLISHED)
                    .orderByDesc(MarketPluginVersion::getPublishTime)
                    .orderByDesc(MarketPluginVersion::getId);
        }
        MarketPluginVersion target = versionMapper.selectOne(wrapper.last("limit 1"));
        if (target == null) {
            throw new UpdateRejectedException(PluginDistErrorCode.ARTIFACT_NOT_FOUND,
                    "插件版本不存在或未发布：" + pluginCode + "@" + (StringUtils.hasText(version) ? version.trim() : "latest"));
        }
        Integer status = target.getStatus();
        if (status != null && status == VERSION_OFFLINE) {
            throw new UpdateRejectedException(PluginDistErrorCode.PLUGIN_OR_VERSION_OFFLINE,
                    "该版本已下架，无法安装：" + pluginCode + " " + target.getVersion());
        }
        if (status == null || status != VERSION_PUBLISHED) {
            throw new UpdateRejectedException(PluginDistErrorCode.VERSION_NOT_PUBLISHED,
                    "该版本未发布（status=" + status + "），无法安装：" + pluginCode + " " + target.getVersion());
        }
        return target;
    }

    /**
     * 建单（安装）：先 PENDING 落库（撞 {@code uk_active} 即并发冲突），再推进到 RUNNING。
     *
     * @param instanceId 站点实例
     * @param code       插件编码
     * @param toVersion  目标版本
     * @param operatorId 操作人
     * @return 已落库的任务
     */
    private PluginUpdateTask createTask(String instanceId, String code, String toVersion, Long operatorId) {
        return createTask(instanceId, code, UpdateOpType.INSTALL, null, toVersion, operatorId);
    }

    /**
     * 建单：先 PENDING 落库（撞 {@code uk_active} 即并发冲突），再推进到 RUNNING。
     *
     * <p>为什么先建 PENDING 再推 RUNNING 而不是直接插 RUNNING：两条并发请求的 INSERT
     * 都会去争 {@code uk_active}，谁先插进去谁赢；若直接插 RUNNING，
     * 「已建单但未开始执行」这个状态就无从表达，排障时看不出任务是卡在调度还是卡在执行。
     *
     * @param instanceId 站点实例
     * @param code       插件编码
     * @param opType     操作类型
     * @param fromVersion 来源版本（INSTALL 时为 null）
     * @param toVersion  目标版本
     * @param operatorId 操作人
     * @return 已落库的任务
     */
    private PluginUpdateTask createTask(String instanceId, String code, UpdateOpType opType,
                                        String fromVersion, String toVersion, Long operatorId) {
        PluginUpdateTask task = new PluginUpdateTask();
        task.setTaskId("upd-" + UUID.randomUUID().toString().replace("-", ""));
        task.setInstanceId(instanceId);
        task.setPluginCode(code);
        task.setOpType(opType.getCode());
        task.setFromVersion(fromVersion);
        task.setToVersion(toVersion);
        task.setState(UpdateTaskState.PENDING.getCode());
        task.setStep(UpdateStep.DOWNLOAD.getCode());
        task.setOperatorId(operatorId);
        task.setStartTime(LocalDateTime.now());
        try {
            taskMapper.insert(task);
        } catch (RuntimeException e) {
            if (isDuplicateKey(e)) {
                throw new UpdateRejectedException(PluginDistErrorCode.TASK_IN_PROGRESS,
                        "该插件已有任务在进行中，请勿重复点击（" + code + " " + toVersion + "）");
            }
            throw e;
        }
        updateStep(task, UpdateStep.DOWNLOAD);
        return task;
    }

    // ==================================================================
    // 升级 / 回滚 支撑
    // ==================================================================

    /**
     * 校验插件编码非空。
     *
     * @param pluginCode 原始编码
     * @return 去空格后的编码
     */
    private static String requireCode(String pluginCode) {
        String code = pluginCode == null ? "" : pluginCode.trim();
        if (code.isEmpty()) {
            throw new UpdateRejectedException(PluginDistErrorCode.ARTIFACT_NOT_FOUND, "插件编码不能为空");
        }
        return code;
    }

    /**
     * 校验并从身份中取站点实例。
     *
     * @param identity 调用方身份
     * @param code     插件编码（错误文案用）
     * @return 站点实例
     */
    private static String requireInstanceId(PluginIdentity identity, String code) {
        String instanceId = identity == null || identity.getInstanceId() == null
                ? ""
                : identity.getInstanceId().trim();
        if (instanceId.isEmpty()) {
            throw new UpdateRejectedException(PluginDistErrorCode.NOT_LOGIN,
                    "未登录或无法识别站点实例，无法操作插件：" + code);
        }
        return instanceId;
    }

    /**
     * 并发互斥：同组合已有非终态任务直接 3016。
     *
     * @param instanceId 站点实例
     * @param code       插件编码
     * @param opType     操作类型
     * @param toVersion  目标版本
     */
    private void requireNoActiveTask(String instanceId, String code, UpdateOpType opType, String toVersion) {
        PluginUpdateTask active = taskMapper.selectActive(instanceId, code, opType.getCode(), toVersion);
        if (active != null) {
            throw new UpdateRejectedException(PluginDistErrorCode.TASK_IN_PROGRESS,
                    "该插件已有" + opType.getDescription() + "任务在进行中（taskId=" + active.getTaskId()
                            + "，状态 " + active.getState() + "），请勿重复点击");
        }
    }

    /**
     * 并发互斥的第二道闸：安装态上的 {@code update_status} 锁。
     *
     * <p>任务表拦的是「同组合」，这里拦的是「同插件」——
     * 一个插件同时只能有一个更新动作在跑，与操作类型、目标版本无关。
     *
     * <p><b>只有 1/2/3 是「正在动」</b>：4（上次失败）是<b>终态记录</b>而不是活锁。
     * 若把它也当成锁，一次失败的升级会把这台站点的这个插件永久锁死——
     * 失败之后唯一合理的动作就是重试，而重试被 3016 拦住，只能人工改库解锁。
     * 那正是「为了防并发而制造了更大的故障」，必须排除。
     *
     * @param install 安装态记录
     * @param code    插件编码
     */
    private static void requireInstallUnlocked(MallPluginInstall install, String code) {
        Integer status = install.getUpdateStatus();
        boolean active = status != null
                && status != UPDATE_STATUS_IDLE
                && status != UPDATE_STATUS_FAILED;
        if (active) {
            throw new UpdateRejectedException(PluginDistErrorCode.TASK_IN_PROGRESS,
                    "该插件正在处理另一个更新动作（update_status=" + status + "），请稍后再试：" + code);
        }
    }

    /**
     * 上更新锁（写 {@code update_status} 与 {@code update_started_at}）。
     *
     * @param install 安装态记录
     * @param status  锁状态（2=升级中 3=回滚中）
     */
    private void lockInstall(MallPluginInstall install, int status) {
        install.setUpdateStatus(status);
        install.setUpdateStartedAt(LocalDateTime.now());
        install.setUpdateTime(LocalDateTime.now());
        updateInstallRow(install);
    }

    /**
     * 解更新锁。
     *
     * @param code       插件编码
     * @param status     落定的状态（0=空闲 4=上次失败）
     * @param lastError  失败原因，成功传 null
     */
    private void unlockInstall(String code, int status, String lastError) {
        MallPluginInstall install = findInstall(code);
        if (install == null) {
            return;
        }
        install.setUpdateStatus(status);
        install.setLastError(lastError);
        install.setUpdateTime(LocalDateTime.now());
        updateInstallRow(install);
    }

    /**
     * 只在<b>仍持有更新锁</b>时解锁（升级中/回滚中/卸载中）。
     *
     * <p>为什么要有这个条件判断：流水线内部的失败分支大多已经自行 {@link #unlockInstall}
     * 落过 FAILED，甚至回滚成功后落了 IDLE。外层 catch 若无条件再写一次，
     * 会把「已成功回滚」覆盖成「失败」，把可恢复状态写成不可恢复。
     * 反之若内部是<b>直接抛出</b>被拒异常（如预检不过、制品验签失败），
     * 锁就一直停在中间态，该插件从此再也升不了、卸不了——孤儿任务扫描也救不了
     * （任务已是终态，不会再被扫到）。
     *
     * @param code    插件编码
     * @param message 失败原因
     */
    private void unlockIfStillLocked(String code, String message) {
        MallPluginInstall install = findInstall(code);
        if (install == null) {
            return;
        }
        Integer status = install.getUpdateStatus();
        int current = status == null ? UPDATE_STATUS_IDLE : status;
        if (current != UPDATE_STATUS_UPGRADING
                && current != UPDATE_STATUS_ROLLING_BACK
                && current != UPDATE_STATUS_UNINSTALLING) {
            return;
        }
        install.setUpdateStatus(UPDATE_STATUS_FAILED);
        install.setLastError(message);
        install.setUpdateTime(LocalDateTime.now());
        updateInstallRow(install);
    }

    /**
     * 写回安装态行（写失败只告警：锁没解开属于可人工修复的问题，
     * 不该把一次成功的升级判成失败）。
     *
     * @param install 安装态记录
     */
    private void updateInstallRow(MallPluginInstall install) {
        try {
            installMapper.updateById(install);
        } catch (RuntimeException e) {
            log.error("更新安装态失败: plugin={} : {}", install.getPluginCode(), e.toString(), e);
        }
    }

    /**
     * 备份当前版本目录作为回滚锚点。
     *
     * <p>备份失败按失败处理（返回 null 而不是抛）：备份是保险，不是升级的必要条件——
     * 旧版本目录本身还在原地（备份是复制不是移动），没有备份只是少了一层兜底。
     *
     * @param code    插件编码
     * @param version 当前版本
     * @return 备份目录，失败返回 null
     */
    private Path backupCurrentVersion(String code, String version) {
        try {
            return backupStore.backup(code, version);
        } catch (IOException e) {
            log.warn("备份版本目录失败，已继续升级（旧版本目录仍在原位）: {}/{} : {}", code, version, e.getMessage());
            return null;
        }
    }

    /**
     * 目标版本在磁盘上缺失时，从最近的备份恢复。
     *
     * @param code    插件编码
     * @param version 版本
     */
    private void restoreVersionIfMissing(String code, String version) {
        Path jar = artifactStore.resolve(code, version);
        if (Files.isRegularFile(jar)) {
            return;
        }
        if (backupStore.restoreLatest(code, version)) {
            return;
        }
        throw new UpdateRejectedException(PluginDistErrorCode.ROLLBACK_POINT_MISSING,
                "回滚点缺失：磁盘与备份中均无 " + code + "@" + version);
    }

    /**
     * 升级成功后的登记：版本推进 + 回滚锚点前移。
     *
     * @param install    安装态记录
     * @param fromVersion 升级前版本（记为回滚锚点）
     * @param toVersion  升级后版本
     * @param jarPath    新版本落盘路径
     * @param md5        新版本摘要
     * @param target     市场版本行（同步站点目录用）
     */
    private void applyUpgrade(MallPluginInstall install, String fromVersion, String toVersion,
                              Path jarPath, String md5, MarketPluginVersion target) {
        install.setInstalled(1);
        install.setEnabled(1);
        install.setVersion(toVersion);
        install.setPreviousVersion(fromVersion);
        install.setJarPath(jarPath == null ? null : jarPath.toString());
        install.setChecksum(md5);
        install.setUpdateStatus(UPDATE_STATUS_IDLE);
        install.setLastError(null);
        install.setUpdateTime(LocalDateTime.now());
        installMapper.updateById(install);
        if (addonPluginSyncService != null && target != null) {
            addonPluginSyncService.syncFromPluginYaml(target.getPluginYaml());
        }
    }

    /**
     * 回滚完成后的登记：版本退回，锚点记为「刚刚离开的版本」。
     *
     * <p>为什么回滚后 {@code previous_version} 指向被回滚掉的版本而不是清空：
     * 「上一版本」是事实记录，清空它会让运维看不出这台站点刚刚发生过什么；
     * 而版本目录清理又依赖它做锚点保护，保留它等于保护了「再退回去」的可能性。
     *
     * @param install  安装态记录
     * @param version  回滚到的版本
     * @param left     被回滚掉的版本
     * @param targetJar 回滚后实际装载的 jar
     */
    private void applyRollback(MallPluginInstall install, String version, String left, Path targetJar) {
        install.setInstalled(1);
        install.setEnabled(1);
        install.setVersion(version);
        install.setPreviousVersion(left);
        install.setJarPath(targetJar == null ? null : targetJar.toString());
        install.setChecksum(checksumOf(targetJar));
        install.setUpdateStatus(UPDATE_STATUS_IDLE);
        install.setLastError(null);
        install.setUpdateTime(LocalDateTime.now());
        installMapper.updateById(install);
    }

    /**
     * 计算落盘文件的 md5（回滚后用于刷新安装态的完整性摘要）。
     *
     * @param jarPath 文件路径，可为 null
     * @return md5，读取失败返回 null
     */
    private static String checksumOf(Path jarPath) {
        if (jarPath == null || !Files.isRegularFile(jarPath)) {
            return null;
        }
        try {
            return ArtifactSignaturePayload.md5Hex(Files.readAllBytes(jarPath));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 版本目录清理：保留当前版本 + 回滚锚点 + 最近 N 个版本。
     *
     * @param code    插件编码
     * @param install 安装态记录（提供当前版本与锚点）
     */
    private void cleanupVersions(String code, MallPluginInstall install) {
        Set<String> anchored = new LinkedHashSet<>();
        if (install != null) {
            if (StringUtils.hasText(install.getVersion())) {
                anchored.add(install.getVersion().trim());
            }
            if (StringUtils.hasText(install.getPreviousVersion())) {
                anchored.add(install.getPreviousVersion().trim());
            }
        }
        for (MallPluginInstall row : findInstallsByCode(code)) {
            if (row != null && StringUtils.hasText(row.getVersion())) {
                anchored.add(row.getVersion().trim());
            }
        }
        try {
            backupStore.cleanup(code, anchored, properties.getRetainVersions(), properties.getRetainBackups());
        } catch (RuntimeException e) {
            log.warn("版本目录清理失败（不影响升级结果）: {} : {}", code, e.getMessage());
        }
    }

    /**
     * 查询同一编码的全部安装记录（用于锚点收集；正常只有一条，多租户/历史数据可能有多条）。
     *
     * @param code 插件编码
     * @return 安装记录列表
     */
    private List<MallPluginInstall> findInstallsByCode(String code) {
        try {
            List<MallPluginInstall> rows = installMapper.selectList(new LambdaQueryWrapper<MallPluginInstall>()
                    .eq(MallPluginInstall::getPluginCode, code));
            return rows == null ? List.of() : rows;
        } catch (RuntimeException e) {
            log.warn("查询安装记录失败（锚点收集不完整，按已有锚点清理）: {} : {}", code, e.getMessage());
            return List.of();
        }
    }

    /**
     * 开始排空窗口（未装配排空闸门时静默跳过）。
     *
     * @param code 插件编码
     */
    private void beginDrain(String code) {
        if (drainGate != null) {
            drainGate.begin(code, properties.getDrainWindowMs());
        }
    }

    /**
     * 结束排空窗口。
     *
     * @param code 插件编码
     */
    private void endDrain(String code) {
        if (drainGate != null) {
            drainGate.end(code);
        }
    }

    /**
     * 排空等待：给在途请求留出跑完的时间。
     */
    private void sleepDrainWindow() {
        long window = properties.getDrainWindowMs();
        if (window <= 0) {
            return;
        }
        try {
            Thread.sleep(window);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 恢复流量：升级/回滚中途失败且旧版本仍装载时，把路由与扩展点重新注册回去。
     *
     * <p>只在确实 {@code disable} 过之后才 {@code enable}——对没被停过的插件调 enable
     * 是多余的，而且可能把「本来就没启用的插件」意外拉起来。
     *
     * @param code     插件编码
     * @param disabled 之前是否真的撤过流量
     */
    private void restoreTraffic(String code, boolean disabled) {
        if (!disabled || lifecycleManager == null) {
            return;
        }
        try {
            lifecycleManager.enable(code);
            log.info("升级中止，已恢复插件流量: {}", code);
        } catch (RuntimeException e) {
            log.error("恢复插件流量失败，需人工检查插件状态: {} : {}", code, e.toString(), e);
        }
    }

    /**
     * 构建最小清单（回滚场景：没有市场版本行可依据，只有编码与版本是确定的事实）。
     *
     * @param code    插件编码
     * @param version 版本
     * @return 插件清单
     */
    private PluginManifest plainManifest(String code, String version) {
        PluginManifest manifest = new PluginManifest();
        manifest.setCode(code);
        manifest.setVersion(version);
        manifest.setOwner("official");
        return manifest;
    }

    /**
     * 升级流水线内部信号：表示「已撤流量之后的阶段失败，必须走回滚路径」。
     *
     * <p>用异常而不是返回值，是因为失败点散落在落盘 / schema / 装载 / 健康检查四处，
     * 用返回值层层透传会让每个嵌套块都要写一遍判断，反而更容易漏掉某个分支。
     */
    private static final class UpgradeFailedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final UpdateStep step;

        UpgradeFailedException(String message, UpdateStep step) {
            super(message);
            this.step = step;
        }

        UpdateStep getStep() {
            return step;
        }
    }

    /**
     * 推进阶段（顺带把状态钉在 RUNNING）。
     *
     * @param task 任务
     * @param step 阶段
     */
    private void updateStep(PluginUpdateTask task, UpdateStep step) {
        task.setState(UpdateTaskState.RUNNING.getCode());
        task.setStep(step.getCode());
        taskMapper.updateById(task);
    }

    /**
     * 落终态——<b>正常路径就是直接 updateById，不需要任何兜底</b>。
     *
     * <p>唯一键改造（{@code plugin_artifact_tables.sql 第六段}）后，{@code uk_active} 只对
     * 非终态（PENDING / RUNNING）生效，终态行的 {@code active_key} 是 NULL，
     * 而 MySQL 唯一索引中 NULL 互不冲突——因此「同一版本安装三次」会留下三条 SUCCESS/FAILED
     * 历史，不再撞键。{@link #mergeIntoExistingTerminalRow} 保留为兼容兜底：
     * 仅当目标库<b>尚未执行第六段</b>（旧 {@code uk_idem} 仍在）时才会被触发。
     *
     * @param task      任务
     * @param state     终态
     * @param errorCode 错误码，成功为 null
     * @param errorMsg  错误描述，成功为 null
     * @param step      停留阶段
     */
    private void finishTask(PluginUpdateTask task, UpdateTaskState state, String errorCode,
                            String errorMsg, UpdateStep step) {
        task.setState(state.getCode());
        task.setStep(step == null ? null : step.getCode());
        task.setErrorCode(errorCode);
        task.setErrorMsg(truncate(errorMsg));
        task.setEndTime(LocalDateTime.now());
        try {
            taskMapper.updateById(task);
            return;
        } catch (RuntimeException e) {
            if (!isDuplicateKey(e)) {
                throw e;
            }
        }
        mergeIntoExistingTerminalRow(task);
    }

    /**
     * 终态撞键的兜底（<b>仅旧库需要</b>）：删掉本次活动行，把结果合并进已存在的同状态历史行。
     *
     * <p>新库（已建 {@code uk_active}）永远不会走到这里——终态不再互斥。
     * 保留它的唯一理由是「DDL 还没执行的存量环境」：旧 {@code uk_idem} 仍会让第二次
     * SUCCESS 撞键，没有这段兜底任务就会永久停在 RUNNING，前端一直转圈。
     * DDL 推全之后本方法可以随 M4 一起删掉。
     *
     * @param task 本次任务
     */
    private void mergeIntoExistingTerminalRow(PluginUpdateTask task) {
        PluginUpdateTask existing = taskMapper.selectOne(new LambdaQueryWrapper<PluginUpdateTask>()
                .eq(PluginUpdateTask::getInstanceId, task.getInstanceId())
                .eq(PluginUpdateTask::getPluginCode, task.getPluginCode())
                .eq(PluginUpdateTask::getOpType, task.getOpType())
                .eq(PluginUpdateTask::getToVersion, task.getToVersion())
                .eq(PluginUpdateTask::getState, task.getState())
                .last("limit 1"));
        if (existing == null) {
            log.error("任务终态撞键但未找到历史行，任务停留在 {}: taskId={}", task.getState(), task.getTaskId());
            return;
        }
        try {
            taskMapper.deleteById(task.getId());
        } catch (RuntimeException e) {
            log.warn("合并任务终态时删除活动行失败: taskId={}, err={}", task.getTaskId(), e.getMessage());
            return;
        }
        existing.setTaskId(task.getTaskId());
        existing.setOperatorId(task.getOperatorId());
        existing.setStartTime(task.getStartTime());
        existing.setStep(task.getStep());
        existing.setErrorCode(task.getErrorCode());
        existing.setErrorMsg(task.getErrorMsg());
        existing.setEndTime(task.getEndTime());
        taskMapper.updateById(existing);
        log.info("任务终态撞 uk_idem，已合并进历史行: plugin={} version={} state={}",
                task.getPluginCode(), task.getToVersion(), task.getState());
    }

    /**
     * 落失败终态（错误码用字符串，兼容 3001-3018 与 SCHEMA_FAILED 这类符号名）。
     *
     * @param task      任务
     * @param errorCode 错误码
     * @param errorMsg  错误描述
     * @param step      失败阶段
     */
    private void failTask(PluginUpdateTask task, String errorCode, String errorMsg, UpdateStep step) {
        finishTask(task, UpdateTaskState.FAILED, errorCode, errorMsg, step);
    }

    /**
     * 登记 / 更新本站点安装态。
     *
     * @param code     插件编码
     * @param version  版本
     * @param jarPath  落盘路径
     * @param checksum 制品 md5
     */
    private void upsertInstall(String code, String version, String jarPath, String checksum) {
        MallPluginInstall install = findInstall(code);
        LocalDateTime now = LocalDateTime.now();
        if (install == null) {
            install = new MallPluginInstall();
            install.setPluginCode(code);
            install.setInstalled(1);
            install.setEnabled(1);
            install.setVersion(version);
            install.setJarPath(jarPath);
            install.setChecksum(checksum);
            install.setConfigJson("{}");
            install.setInstallTime(now);
            install.setUpdateTime(now);
            install.setDeleted(0);
            installMapper.insert(install);
            return;
        }
        install.setInstalled(1);
        install.setEnabled(1);
        install.setVersion(version);
        install.setJarPath(jarPath);
        install.setChecksum(checksum);
        install.setUpdateTime(now);
        installMapper.updateById(install);
    }

    /**
     * 查询安装态。
     *
     * @param code 插件编码
     * @return 安装行，未安装返回 null
     */
    private MallPluginInstall findInstall(String code) {
        return installMapper.selectOne(new LambdaQueryWrapper<MallPluginInstall>()
                .eq(MallPluginInstall::getPluginCode, code)
                .last("limit 1"));
    }

    /**
     * 取任务当前阶段。
     *
     * @param task 任务
     * @return 阶段枚举，无法识别时返回 null
     */
    private static UpdateStep stepOf(PluginUpdateTask task) {
        try {
            return task.getStep() == null ? null : UpdateStep.fromCode(task.getStep());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 取第一个非空文本。
     *
     * @param values 候选值
     * @return 第一个非空值，全空返回 null
     */
    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    /**
     * 错误描述截断（varchar(1024)）。
     *
     * @param message 原始描述
     * @return 截断后的描述
     */
    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= ERROR_MSG_MAX ? message : message.substring(0, ERROR_MSG_MAX);
    }

    /**
     * 判断异常是否为唯一键冲突。
     *
     * <p>不同驱动 / 不同框架层抛出的类型不一样（Spring 的 {@link DuplicateKeyException}、
     * JDBC 的 {@link SQLIntegrityConstraintViolationException}、MyBatis 包了一层之后的
     * PersistenceException），因此按类型 + 文案双判据逐层回溯，避免漏判导致并发互斥失效。
     *
     * @param error 异常
     * @return true 表示唯一键冲突
     */
    private static boolean isDuplicateKey(Throwable error) {
        Throwable current = error;
        int depth = 0;
        while (current != null && depth < 10) {
            if (current instanceof DuplicateKeyException
                    || current instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null && (message.contains("Duplicate entry") || message.contains("DuplicateKey"))) {
                return true;
            }
            current = current.getCause();
            depth++;
        }
        return false;
    }
}
