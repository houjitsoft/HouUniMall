package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketPluginLicenseMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 插件授权服务：付费插件（PAID / TRIAL）的签发、激活、校验与吊销。
 *
 * <p><b>职责边界</b>：
 * <ul>
 *   <li><b>平台侧</b>（{@link #issue} / {@link #revoke}）：线下收款后签发授权码、或吊销。
 *       对应 mall 的 {@code /base/plugin-center/market/license/issue|revoke}，<b>不经 BFF 对外暴露</b>；</li>
 *   <li><b>租户侧</b>（{@link #activate} / {@link #statusOf}）：拿到授权码后激活、查询本插件授权状态，
 *       经 BFF {@code /web/plugin-center/market/license/**} 暴露；</li>
 *   <li><b>门控</b>（{@link #assertInstallable}）：由 {@link PluginMarketService#installFromMarket}
 *       在安装前调用，未授权直接拒绝安装。</li>
 * </ul>
 *
 * <p>这张表（{@code hou_market_plugin_license}）的 DDL 早已存在，本服务是它的首个 Java 实现。
 *
 * @author Amos
 */
@Slf4j
@Service
public class PluginLicenseService {

    /** 授权类型：免费（无需授权） */
    public static final String TYPE_FREE = "FREE";

    /** 授权类型：付费 */
    public static final String TYPE_PAID = "PAID";

    /** 授权类型：试用 */
    public static final String TYPE_TRIAL = "TRIAL";

    /** 状态机：已签发（尚未绑定实例） */
    public static final String STATE_ISSUED = "ISSUED";

    /** 状态机：已绑定实例 */
    public static final String STATE_BOUND = "BOUND";

    /** 状态机：已激活（首次安装成功，试用期从此刻起算） */
    public static final String STATE_ACTIVATED = "ACTIVATED";

    /** 状态机：已过期 */
    public static final String STATE_EXPIRED = "EXPIRED";

    /** 状态机：已吊销 */
    public static final String STATE_REVOKED = "REVOKED";

    /** 状态机：已解绑（授权本身仍有效，可在他实例重新激活） */
    public static final String STATE_UNBOUND = "UNBOUND";

    /** 授权有效 */
    private static final int STATUS_VALID = 1;

    /** 授权失效（已吊销） */
    private static final int STATUS_REVOKED = 0;

    /** 默认最大实例数 */
    private static final int DEFAULT_MAX_INSTANCES = 1;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MarketPluginLicenseMapper licenseMapper;

    /**
     * 安装态访问能力；为 null 表示只读构造器场景（单测），此时跳过「解绑前须卸载」守卫。
     */
    private final MallPluginInstallMapper pluginInstallMapper;

    /**
     * 只读构造器：供单测与不关心安装态的场景使用。
     */
    public PluginLicenseService(MarketPluginLicenseMapper licenseMapper) {
        this(licenseMapper, null);
    }

    /**
     * Spring 装配：带安装态依赖的完整构造器。
     */
    @Autowired
    public PluginLicenseService(MarketPluginLicenseMapper licenseMapper,
                                MallPluginInstallMapper pluginInstallMapper) {
        this.licenseMapper = licenseMapper;
        this.pluginInstallMapper = pluginInstallMapper;
    }

    /**
     * 该授权类型是否需要持有 License（PAID / TRIAL 需要，FREE 不需要）。
     */
    public static boolean requiresLicense(String licenseType) {
        String type = licenseType == null ? TYPE_FREE : licenseType.trim().toUpperCase(Locale.ROOT);
        return TYPE_PAID.equals(type) || TYPE_TRIAL.equals(type);
    }

    /**
     * 安装门控：付费/试用插件必须已持有本站点的有效授权，否则抛异常阻止安装。
     *
     * @return 命中的有效授权
     */
    public MarketPluginLicense assertInstallable(String pluginCode) {
        return assertInstallable(pluginCode, null);
    }

    /**
     * 按站点实例校验安装授权。传入 instanceId 时只接受绑定到该实例的有效授权，
     * 避免一个站点复用另一个站点的购买记录。
     */
    public MarketPluginLicense assertInstallable(String pluginCode, String instanceId) {
        MarketPluginLicense license = findValid(pluginCode, instanceId);
        if (license == null) {
            throw new IllegalStateException("该插件为付费插件，需先购买并激活授权后才能安装: " + pluginCode);
        }
        return license;
    }

    /**
     * 查询本站点对某插件的有效授权（未吊销、未过期）。
     */
    public MarketPluginLicense findValid(String pluginCode) {
        return findValid(pluginCode, null);
    }

    /**
     * 查询指定站点实例的有效授权；instanceId 为空时保留平台侧全局查询兼容行为。
     */
    public MarketPluginLicense findValid(String pluginCode, String instanceId) {
        if (!StringUtils.hasText(pluginCode)) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<MarketPluginLicense> wrapper = new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getPluginCode, pluginCode.trim())
                .eq(MarketPluginLicense::getStatus, STATUS_VALID)
                .and(w -> w.eq(MarketPluginLicense::getLicenseType, TYPE_PAID)
                        .or(q -> q.isNull(MarketPluginLicense::getExpireTime)
                                .or().gt(MarketPluginLicense::getExpireTime, now)))
                .orderByDesc(MarketPluginLicense::getId)
                .last("limit 1");
        if (StringUtils.hasText(instanceId)) {
            wrapper.eq(MarketPluginLicense::getInstanceId, instanceId.trim());
        }
        return licenseMapper.selectOne(wrapper);
    }

    /**
     * 查询指定站点实例的授权记录（<b>不校验有效性与过期</b>），用于区分
     * 「没买 / 已过期 / 已吊销」三类拦截原因——{@link #findValid} 三者都返回 null，
     * 前端没法据此分流到「购买」还是「续费」。
     *
     * @param pluginCode 插件编码
     * @param instanceId 站点实例标识（必填，fail-closed）
     * @return 最近一条授权记录；无记录返回 null
     */
    public MarketPluginLicense findValidPaid(String pluginCode, String instanceId) {
        if (!StringUtils.hasText(pluginCode) || !StringUtils.hasText(instanceId)) return null;
        return licenseMapper.selectOne(new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getPluginCode, pluginCode.trim())
                .eq(MarketPluginLicense::getInstanceId, instanceId.trim())
                .eq(MarketPluginLicense::getLicenseType, TYPE_PAID)
                .eq(MarketPluginLicense::getStatus, STATUS_VALID)
                .in(MarketPluginLicense::getState, STATE_BOUND, STATE_ACTIVATED)
                .eq(MarketPluginLicense::getPurchaseSandbox, 0)
                .notExists("SELECT 1 FROM hou_market_plugin_order o WHERE o.order_no=hou_market_plugin_license.source_order_no AND (o.status<>1 OR o.sandbox=1 OR o.channel='MOCK')")
                .orderByDesc(MarketPluginLicense::getMaintenanceUntil)
                .orderByDesc(MarketPluginLicense::getId).last("limit 1"));
    }

    /**
     * 查询可用于「下载/安装生产制品」的有效授权——沙箱购买一律不算。
     *
     * <p>🔴 2026-10-27 修 P1：制品下载（jar）以前走 {@link #findValid}，而 {@code findValid}
     * 不区分 {@code purchase_sandbox}，源码包（source）与独立服务镜像（oci）却走
     * {@link #findValidPaid}（带沙箱过滤）。结果：沙箱环境里 0 元买一张授权，
     * 就能把生产 JAR 拉走。这里把口径统一到「生产购买 + 有效期内」，
     * 供所有制品下载/安装校验复用。
     *
     * @param pluginCode 插件编码
     * @param instanceId 站点实例（必填，fail-closed）
     * @return 有效生产授权；没有返回 null
     */
    public MarketPluginLicense findValidProduction(String pluginCode, String instanceId) {
        if (!StringUtils.hasText(pluginCode) || !StringUtils.hasText(instanceId)) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return licenseMapper.selectOne(new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getPluginCode, pluginCode.trim())
                .eq(MarketPluginLicense::getInstanceId, instanceId.trim())
                .eq(MarketPluginLicense::getStatus, STATUS_VALID)
                .and(w -> w.eq(MarketPluginLicense::getLicenseType, TYPE_PAID)
                        .or(q -> q.isNull(MarketPluginLicense::getExpireTime)
                                .or().gt(MarketPluginLicense::getExpireTime, now)))
                .eq(MarketPluginLicense::getPurchaseSandbox, 0)
                .notExists("SELECT 1 FROM hou_market_plugin_order o WHERE o.order_no=hou_market_plugin_license.source_order_no AND (o.status<>1 OR o.sandbox=1 OR o.channel='MOCK')")
                .orderByDesc(MarketPluginLicense::getId)
                .last("limit 1"));
    }

    public MarketPluginLicense findAny(String pluginCode, String instanceId) {
        if (!StringUtils.hasText(pluginCode) || !StringUtils.hasText(instanceId)) {
            return null;
        }
        return licenseMapper.selectOne(new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getPluginCode, pluginCode.trim())
                .eq(MarketPluginLicense::getInstanceId, instanceId.trim())
                .orderByDesc(MarketPluginLicense::getId)
                .last("limit 1"));
    }

    /**
     * 是否已授权（供列表展示，不抛异常）。
     */
    public boolean licensed(String pluginCode) {
        return findValid(pluginCode) != null;
    }

    /** 指定站点实例是否持有有效授权。 */
    public boolean licensed(String pluginCode, String instanceId) {
        return findValid(pluginCode, instanceId) != null;
    }

    /**
     * 本站点全部有效授权（未吊销、未过期），供市场列表合并展示用（一次查询，避免逐条查库）。
     */
    public List<MarketPluginLicense> listValid() {
        LocalDateTime now = LocalDateTime.now();
        return licenseMapper.selectList(new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getStatus, STATUS_VALID)
                .and(w -> w.eq(MarketPluginLicense::getLicenseType, TYPE_PAID)
                        .or(q -> q.isNull(MarketPluginLicense::getExpireTime)
                                .or().gt(MarketPluginLicense::getExpireTime, now)))
                .orderByDesc(MarketPluginLicense::getId));
    }

    /**
     * 本站点（租户侧）已激活授权列表：<b>只返回绑定了本实例且未吊销、未过期的授权</b>。
     *
     * <p>与 {@link #listValid()} 的区别：listValid 面向平台侧内部合并展示、不限实例；
     * 本方法用于「授权管理」页对租户展示，必须以 instanceId 收敛，否则会跨站点泄露他人授权码。
     *
     * @param instanceId 本站点实例标识（域名/机器码），为空一律返回空列表（拒绝无身份的越权查询）
     */
    public List<MarketPluginLicense> listMine(String instanceId) {
        if (!StringUtils.hasText(instanceId)) {
            return new ArrayList<>();
        }
        LocalDateTime now = LocalDateTime.now();
        return licenseMapper.selectList(new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getStatus, STATUS_VALID)
                .eq(MarketPluginLicense::getInstanceId, instanceId.trim())
                .and(w -> w.eq(MarketPluginLicense::getLicenseType, TYPE_PAID)
                        .or(q -> q.isNull(MarketPluginLicense::getExpireTime)
                                .or().gt(MarketPluginLicense::getExpireTime, now)))
                .orderByDesc(MarketPluginLicense::getId));
    }

    /**
     * ★记录首次成功安装时间并起算试用期（M5，方案 §7.3）。
     *
     * <p>为什么试用期不能从「签发」起算：租户买了没装、或者装了又卸载，
     * 倒计时却一直在跑，是典型投诉源。因此以<b>首次成功安装</b>为起点，
     * {@code TRIAL} 授权签发时 {@code expire_time} 为 NULL（不预先扣时间），
     * 直到这里才回填 {@code first_install_time + trial_days}。
     *
     * <p><b>幂等（关键）</b>：{@code first_install_time} 一旦写入就不再覆盖——
     * 「卸载 → 重装」不能把试用期重置，否则等于无限续期。
     *
     * @param pluginCode 插件编码
     * @param instanceId 站点实例标识（必填，fail-closed）
     * @param trialDays  商品声明的试用天数；null / &lt;=0 表示不按试用回填过期时间
     * @return 更新后的授权；未持有有效授权时抛异常
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginLicense markFirstInstall(String pluginCode, String instanceId, Integer trialDays) {
        if (!StringUtils.hasText(pluginCode)) {
            throw new IllegalArgumentException("插件编码不能为空");
        }
        if (!StringUtils.hasText(instanceId)) {
            throw new IllegalArgumentException("未识别站点实例，无法记录首次安装时间");
        }
        MarketPluginLicense license = findValid(pluginCode, instanceId);
        if (license == null) {
            throw new IllegalStateException("未持有该插件的有效授权，无法记录首次安装时间: " + pluginCode);
        }
        if (license.getFirstInstallTime() != null) {
            // 已起算过：卸载重装不重置试用期
            log.info("首次安装时间已存在，跳过试用期重算: plugin={} instance={} firstInstallTime={}",
                    pluginCode, instanceId, license.getFirstInstallTime());
            return license;
        }
        LocalDateTime now = LocalDateTime.now();
        license.setFirstInstallTime(now);
        if (license.getBindTime() == null) {
            license.setBindTime(now);
        }
        license.setState(STATE_ACTIVATED);
        if (TYPE_TRIAL.equalsIgnoreCase(license.getLicenseType())
                && trialDays != null && trialDays > 0
                && license.getExpireTime() == null) {
            license.setExpireTime(now.plusDays(trialDays));
        }
        licenseMapper.updateById(license);
        log.info("记录插件首次安装并起算试用: plugin={} instance={} type={} firstInstallTime={} expireTime={}",
                license.getPluginCode(), instanceId, license.getLicenseType(),
                license.getFirstInstallTime(), license.getExpireTime());
        return license;
    }

    /**
     * 把「已激活且已过有效期」的授权批量迁移为 EXPIRED（M5，方案 §7.3 到期处理）。
     *
     * <p><b>本次只做状态迁移，不做停用</b>：方案里「自动 disable 但保留数据」属于心跳任务的职责，
     * 它还要通知插件引擎撤路由，不是一次 UPDATE 能完成的动作；
     * 在定时任务接进来之前让状态先落库，避免将来一次性补太多行为难以回退。
     *
     * <p>M5b：状态迁移与「停用」拆成两步——本方法只迁移状态并把行返回给调用方，
     * 停用由心跳在能碰到引擎的一侧编排（授权模块够不着 plugin-engine）。
     *
     * @param now   当前时间（显式传入，便于单测钉死边界）
     * @param limit 单次迁移上限，避免长事务
     * @return 实际迁移条数
     */
    public int migrateExpired(LocalDateTime now, int limit) {
        List<MarketPluginLicense> expired = collectExpired(now, limit);
        for (MarketPluginLicense license : expired) {
            markExpired(license, now);
        }
        if (!expired.isEmpty()) {
            log.info("授权过期迁移完成: count={} now={}", expired.size(), now);
        }
        return expired.size();
    }

    /**
     * 扫出「已激活且已过有效期」的授权（<b>只查不写</b>），供心跳编排使用。
     *
     * <p>为什么要拆出只读版本：心跳拿到行之后还要按 {@code pluginCode} 去停用插件，
     * 只有条数的话根本不知道该停谁。
     *
     * @param now   当前时间
     * @param limit 单次上限
     * @return 待迁移的授权行
     */
    public List<MarketPluginLicense> collectExpired(LocalDateTime now, int limit) {
        if (now == null || limit <= 0) {
            return new ArrayList<>();
        }
        List<MarketPluginLicense> expired = licenseMapper.selectList(
                new LambdaQueryWrapper<MarketPluginLicense>()
                        .eq(MarketPluginLicense::getStatus, STATUS_VALID)
                        .eq(MarketPluginLicense::getState, STATE_ACTIVATED)
                        .ne(MarketPluginLicense::getLicenseType, TYPE_PAID)
                        .isNotNull(MarketPluginLicense::getExpireTime)
                        .lt(MarketPluginLicense::getExpireTime, now)
                        .last("limit " + limit));
        if (expired == null || expired.isEmpty()) {
            return new ArrayList<>();
        }
        List<MarketPluginLicense> result = new ArrayList<>();
        for (MarketPluginLicense license : expired) {
            // 双保险：过期判定在 SQL 里已经做了一次，这里再判一次——
            // 把「未到期」或「未激活」的行误置为 EXPIRED 会直接停用租户正在用的插件，
            // 这类错误的代价远高于两次内存比较，值得冗余一道。
            if (TYPE_PAID.equalsIgnoreCase(license.getLicenseType()) || !STATE_ACTIVATED.equals(license.getState())
                    || license.getExpireTime() == null
                    || !license.getExpireTime().isBefore(now)) {
                continue;
            }
            result.add(license);
        }
        return result;
    }

    /**
     * 把单条授权置为 EXPIRED 并记录心跳时间。
     *
     * @param license 授权行
     * @param now     当前时间
     */
    public void markExpired(MarketPluginLicense license, LocalDateTime now) {
        if (license == null) {
            return;
        }
        license.setState(STATE_EXPIRED);
        license.setLastHeartbeat(now == null ? LocalDateTime.now() : now);
        licenseMapper.updateById(license);
    }

    /**
     * 扫出「曾因过期停用、现在又重新有效」的授权（<b>只查不写</b>）——续费后原地恢复的依据。
     *
     * <p><b>为什么只认「同一行被延期」这一种续费</b>：平台也可能直接发一张新授权码（新行），
     * 那种情况下无法区分「当前停用是我们停的」还是「租户自己停的」，
     * 凭猜测自动启用会把租户手动停用的插件重新打开，是更严重的事故。
     * 因此只有「我们置为 EXPIRED 的这一行重新有效」才自动恢复。
     *
     * @param now   当前时间
     * @param limit 单次上限
     * @return 已续费的授权行
     */
    public List<MarketPluginLicense> collectRenewed(LocalDateTime now, int limit) {
        if (now == null || limit <= 0) {
            return new ArrayList<>();
        }
        List<MarketPluginLicense> renewed = licenseMapper.selectList(
                new LambdaQueryWrapper<MarketPluginLicense>()
                        .eq(MarketPluginLicense::getStatus, STATUS_VALID)
                        .eq(MarketPluginLicense::getState, STATE_EXPIRED)
                        .and(w -> w.isNull(MarketPluginLicense::getExpireTime)
                                .or().gt(MarketPluginLicense::getExpireTime, now))
                        .last("limit " + limit));
        if (renewed == null || renewed.isEmpty()) {
            return new ArrayList<>();
        }
        List<MarketPluginLicense> result = new ArrayList<>();
        for (MarketPluginLicense license : renewed) {
            if (!STATE_EXPIRED.equals(license.getState())
                    || !Integer.valueOf(STATUS_VALID).equals(license.getStatus())) {
                continue;
            }
            if (license.getExpireTime() != null && !license.getExpireTime().isAfter(now)) {
                continue;
            }
            result.add(license);
        }
        return result;
    }

    /**
     * 把单条「已续费」的授权恢复为 ACTIVATED 并记录心跳时间。
     *
     * @param license 授权行
     * @param now     当前时间
     */
    public void markRenewed(MarketPluginLicense license, LocalDateTime now) {
        if (license == null) {
            return;
        }
        license.setState(STATE_ACTIVATED);
        license.setLastHeartbeat(now == null ? LocalDateTime.now() : now);
        licenseMapper.updateById(license);
        log.info("授权续费恢复: plugin={} key={}", license.getPluginCode(), license.getLicenseKey());
    }

    /**
     * 按当前时间迁移过期授权。
     *
     * @param limit 单次迁移上限
     * @return 实际迁移条数
     */
    public int migrateExpired(int limit) {
        return migrateExpired(LocalDateTime.now(), limit);
    }

    /**
     * 解绑（租户侧动作）：释放本实例与该授权码的绑定，解绑后授权码可在其它实例重新激活。
     *
     * <p>与平台侧 {@link #revoke} 的区别：revoke 是吊销（status 置 0，永久作废）；
     * 解绑仅清空 instanceId，授权本身仍然有效。
     *
     * @param licenseKey 授权码
     * @param instanceId 调用方站点实例，必须与授权码当前绑定实例一致，否则拒绝
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginLicense unbind(String licenseKey, String instanceId) {
        if (!StringUtils.hasText(licenseKey)) {
            throw new IllegalArgumentException("授权码不能为空");
        }
        if (!StringUtils.hasText(instanceId)) {
            throw new IllegalArgumentException("未识别站点实例，无法解绑");
        }
        String target = instanceId.trim();
        MarketPluginLicense license = findByKey(licenseKey.trim());
        if (license == null) {
            throw new IllegalArgumentException("授权码不存在: " + licenseKey.trim());
        }
        if (!target.equals(license.getInstanceId())) {
            throw new IllegalStateException("该授权码未绑定到当前站点，无法解绑");
        }
        assertNotInstalled(license.getPluginCode());

        license.setInstanceId(null);
        // 解绑不等于作废：状态机落 UNBOUND，授权仍可在其它实例重新激活
        license.setState(STATE_UNBOUND);
        licenseMapper.updateById(license);
        log.info("解绑插件授权: plugin={} key={} 原实例={}",
                license.getPluginCode(), license.getLicenseKey(), target);
        return license;
    }

    /**
     * 解绑守卫：插件仍处于安装状态时不允许解绑，避免「解绑后继续使用」绕开车控。
     *
     * <p><b>已知限制（非本处可修）</b>：{@code hou_addon_plugin_install} 按
     * {@code plugin_code} 唯一、且<b>没有实例列</b>，因此本守卫无法按站点收敛。
     * 若将来该表补 {@code instance_id} 列，这里必须同步加上实例条件，
     * 否则「别人站点装着同名插件」会挡住本站点的正常解绑。
     */
    private void assertNotInstalled(String pluginCode) {
        if (pluginInstallMapper == null) {
            return;
        }
        MallPluginInstall install = pluginInstallMapper.selectOne(new LambdaQueryWrapper<MallPluginInstall>()
                .eq(MallPluginInstall::getPluginCode, pluginCode)
                .last("limit 1"));
        if (install != null && Integer.valueOf(1).equals(install.getInstalled())) {
            throw new IllegalStateException(
                    "该插件仍处于安装状态，请先在插件中心卸载后再解绑: " + pluginCode);
        }
    }

    /**
     * 签发授权（平台侧动作：线下收款后发证）。
     *
     * @param pluginCode  插件编码
     * @param buyerId     购买方/站点 ID，为空默认 1（平台自营）
     * @param instanceId  绑定实例（域名/机器码），可为空表示不绑定
     * @param licenseType PAID / TRIAL，为空默认 PAID
     * @param validDays   有效天数；为空或 &lt;=0 表示永久有效。
     *                    <b>TRIAL 忽略本参数</b>——试用期由首次安装成功时刻起算（方案 §7.3）。
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginLicense issue(String pluginCode, Long buyerId, String instanceId,
                                     String licenseType, Integer validDays) {
        if (!StringUtils.hasText(pluginCode)) {
            throw new IllegalArgumentException("插件编码不能为空");
        }
        String type = StringUtils.hasText(licenseType)
                ? licenseType.trim().toUpperCase(Locale.ROOT)
                : TYPE_PAID;
        if (TYPE_FREE.equals(type)) {
            throw new IllegalArgumentException("FREE 插件无需签发授权");
        }
        if (!TYPE_PAID.equals(type) && !TYPE_TRIAL.equals(type)) {
            throw new IllegalArgumentException("授权类型仅支持 PAID / TRIAL: " + licenseType);
        }

        MarketPluginLicense license = new MarketPluginLicense();
        license.setPluginCode(pluginCode.trim());
        license.setBuyerId(buyerId == null ? 1L : buyerId);
        license.setInstanceId(StringUtils.hasText(instanceId) ? instanceId.trim() : null);
        license.setLicenseKey(generateKey(pluginCode));
        license.setLicenseType(type);
        license.setMaxInstances(DEFAULT_MAX_INSTANCES);
        // ★试用期不从签发起算：TRIAL 一律不预先扣时间，等首次安装成功再由
        //   markFirstInstall() 回填 expire_time = first_install_time + trial_days。
        //   否则「买了没装却在倒计时」，是典型投诉源（方案 §7.3）。
        boolean trial = TYPE_TRIAL.equals(type);
        license.setExpireTime(null);
        license.setStatus(STATUS_VALID);
        if (!trial) {
            OfficialMaintenancePolicy.firstPurchase(license, LocalDateTime.now());
        }
        license.setPurchaseSandbox(0);
        license.setState(STATE_ISSUED);
        license.setCreateTime(LocalDateTime.now());
        licenseMapper.insert(license);

        log.info("签发插件授权: plugin={} type={} buyer={} instances={} expire={} key={}",
                license.getPluginCode(), license.getLicenseType(), license.getBuyerId(),
                license.getMaxInstances(), license.getExpireTime(), license.getLicenseKey());
        return license;
    }

    /** Lock one entitlement before issuing or recalculating maintenance, including first purchase. */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginLicense issuePaidOrder(String code, Long buyer, String instance, String orderNo) {
        return issuePaidOrder(code,buyer,instance,orderNo,false);
    }

    @Transactional(rollbackFor = Exception.class)
    public MarketPluginLicense issuePaidOrder(String code,Long buyer,String instance,String orderNo,boolean sandbox) {
        return issuePaidOrder(code, buyer, instance, orderNo, sandbox, null);
    }

    /**
     * 按订单签发 PAID 授权（含套餐期限）。
     *
     * @param termDays 该笔购买折算的服务期限（天），来自价格套餐 valid_days；
     *                 为空表示按官方政策每笔 +12 个月。期限落在授权行 {@code term_days}，
     *                 累计维护期由 {@link OfficialMaintenancePolicy#recalculate} 按笔累加。
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginLicense issuePaidOrder(String code,Long buyer,String instance,String orderNo,boolean sandbox,Integer termDays) {
        if (!StringUtils.hasText(code) || !StringUtils.hasText(instance) || !StringUtils.hasText(orderNo))
            throw new IllegalArgumentException("购买授权必须绑定产品、站点和订单");
        licenseMapper.lockEntitlement(code,instance);
        var existing=licenseMapper.selectOne(new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getSourceOrderNo,orderNo).last("limit 1"));
        if(existing!=null) {
            if(!code.equals(existing.getPluginCode()) || !instance.equals(existing.getInstanceId()))
                throw new IllegalStateException("订单授权归属冲突");
            return existing;
        }
        MarketPluginLicense issued=issue(code,buyer,instance,TYPE_PAID,null);
        issued.setSourceOrderNo(orderNo);issued.setPurchaseSandbox(sandbox?1:0);
        issued.setState(STATE_BOUND);issued.setBindTime(LocalDateTime.now());
        if(termDays!=null&&termDays>0)issued.setTermDays(termDays);
        if(sandbox)issued.setMaintenanceUntil(null);
        licenseMapper.updateById(issued);
        if(!sandbox)recalculatePaidMaintenance(code,instance);
        return licenseMapper.selectById(issued.getId());
    }

    /**
     * 该站点在当前插件上的官方维护期截止时间（无有效 PAID 正式授权时返回 null）。
     *
     * <p><b>为什么由本模块提供、由 market 模块判定</b>：维护期的是授权属性，属于本模块；
     * 「哪个版本发布于维护期之后」需要版本表，而版本表在 plugin-market 里，
     * 反向依赖会成环。所以这里只回答「维护期到哪天」，判版本留给调用方。
     *
     * <p>沙箱授权（purchase_sandbox=1）不贡献维护期，直接跳过；这点与
     * {@link #findValidProduction} 的口径一致，避免沙箱单把正式升级额度撑开。
     */
    public LocalDateTime maintenanceUntilOf(String pluginCode, String instanceId) {
        if (!StringUtils.hasText(pluginCode) || !StringUtils.hasText(instanceId)) {
            return null;
        }
        List<MarketPluginLicense> grants = licenseMapper.selectList(new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getPluginCode, pluginCode.trim())
                .eq(MarketPluginLicense::getInstanceId, instanceId.trim())
                .eq(MarketPluginLicense::getLicenseType, TYPE_PAID)
                .eq(MarketPluginLicense::getStatus, STATUS_VALID)
                .eq(MarketPluginLicense::getPurchaseSandbox, 0));
        if (grants == null || grants.isEmpty()) {
            return null;
        }
        LocalDateTime until = null;
        for (MarketPluginLicense grant : grants) {
            LocalDateTime value = grant.getMaintenanceUntil();
            if (value != null && (until == null || until.isBefore(value))) {
                until = value;
            }
        }
        return until;
    }

    /** Rebuild cumulative maintenance from non-refunded grants instead of copying a stale predecessor. */
    @Transactional(rollbackFor = Exception.class)
    public void recalculatePaidMaintenance(String code,String instance) {
        licenseMapper.lockEntitlement(code,instance);
        var grants=licenseMapper.selectList(new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getPluginCode,code).eq(MarketPluginLicense::getInstanceId,instance)
                .eq(MarketPluginLicense::getLicenseType,TYPE_PAID).eq(MarketPluginLicense::getStatus,STATUS_VALID)
                .eq(MarketPluginLicense::getPurchaseSandbox,0)
                .orderByAsc(MarketPluginLicense::getCreateTime).orderByAsc(MarketPluginLicense::getId).last("for update"));
        OfficialMaintenancePolicy.recalculate(grants);
        for(var grant:grants)if(StringUtils.hasText(grant.getSourceOrderNo()))licenseMapper.updateById(grant);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundPaidOrder(String code,String instance,String orderNo,String reason) {
        licenseMapper.lockEntitlement(code,instance);
        var grant=licenseMapper.selectOne(new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getPluginCode,code).eq(MarketPluginLicense::getInstanceId,instance)
                .eq(MarketPluginLicense::getSourceOrderNo,orderNo).last("limit 1"));
        if(grant!=null) {
            grant.setStatus(STATUS_REVOKED);grant.setState(STATE_REVOKED);grant.setRevokeReason(reason);
            licenseMapper.updateById(grant);
        }
        recalculatePaidMaintenance(code,instance);
    }

    /**
     * 激活：校验授权码合法（未吊销、未过期、与插件匹配），并绑定本站点实例。
     *
     * @param pluginCode 插件编码，可为空（为空则不校验插件匹配）
     * @param licenseKey 授权码
     * @param instanceId 本站点实例标识（域名/机器码），可为空
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginLicense activate(String pluginCode, String licenseKey, String instanceId) {
        return activate(pluginCode, licenseKey, instanceId, null);
    }

    /**
     * 激活（带购买方归属校验）。
     *
     * <p><b>为什么只对「订单签发」的授权强制校验买方</b>：平台线下发码（手工 issue、
     * 无 {@code source_order_no}）本来就是「谁拿到码谁用」，强制校验会把客户手里的
     * 授权码全部作废。而在线购买签发的授权有明确买方，若不做校验，别人拿到泄漏的
     * 授权码就能抢先绑定到自己的站点，真正付钱的人反而激活不了（审计 2026-10-27 P2）。
     *
     * @param buyerId 当前登录站点/购买方 ID；为空时不做归属校验（内部链路与平台后台）
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginLicense activate(String pluginCode, String licenseKey, String instanceId, Long buyerId) {
        if (!StringUtils.hasText(licenseKey)) {
            throw new IllegalArgumentException("授权码不能为空");
        }
        MarketPluginLicense license = findByKey(licenseKey.trim());
        if (license == null) {
            throw new IllegalArgumentException("授权码无效: " + licenseKey.trim());
        }
        if (!Integer.valueOf(STATUS_VALID).equals(license.getStatus())) {
            throw new IllegalStateException("授权码已吊销: " + license.getLicenseKey());
        }
        if (!TYPE_PAID.equalsIgnoreCase(license.getLicenseType()) && license.getExpireTime() != null && license.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("授权码已过期: " + license.getLicenseKey());
        }
        if (StringUtils.hasText(pluginCode) && !license.getPluginCode().equals(pluginCode.trim())) {
            throw new IllegalArgumentException(
                    "授权码与插件不匹配：该授权码属于 " + license.getPluginCode());
        }
        // 在线购买签发的授权有其唯一买方：不允许第三方拿泄漏的授权码抢先绑定。
        // 线下手工发码（无来源订单）保持「谁拿到码谁用」的既有语义。
        if (buyerId != null && StringUtils.hasText(license.getSourceOrderNo())
                && license.getBuyerId() != null && !license.getBuyerId().equals(buyerId)) {
            log.warn("拒绝激活：授权码属于其它购买方 key={} plugin={} owner={} requester={}",
                    license.getLicenseKey(), license.getPluginCode(), license.getBuyerId(), buyerId);
            throw new IllegalStateException("该授权码属于其它购买方，无法在当前站点激活");
        }
        if (StringUtils.hasText(instanceId)) {
            String target = instanceId.trim();
            // 此前未绑定：这是「授权码被谁领走」的唯一落点，必须留审计日志——
            // 授权码一旦泄漏，事后只能靠这条记录回答绑定到了哪个实例。
            boolean firstBind = !StringUtils.hasText(license.getInstanceId());
            if (StringUtils.hasText(license.getInstanceId()) && !license.getInstanceId().equals(target)) {
                throw new IllegalStateException("授权码已绑定其它实例: " + license.getInstanceId());
            }
            license.setInstanceId(target);
            if (license.getBindTime() == null) {
                license.setBindTime(LocalDateTime.now());
            }
            // ISSUED → BOUND：绑定实例只是生命周期的一步，ACTIVATED 要等首次安装成功
            if (!STATE_ACTIVATED.equals(license.getState())) {
                license.setState(STATE_BOUND);
            }
            licenseMapper.updateById(license);
            if (firstBind) {
                log.warn("未绑定授权码被领取（首次绑定实例）: plugin={} key={} instance={}",
                        license.getPluginCode(), license.getLicenseKey(), target);
            }
        }

        log.info("激活插件授权: plugin={} key={} instance={}",
                license.getPluginCode(), license.getLicenseKey(), license.getInstanceId());
        return license;
    }

    /**
     * 吊销授权（平台侧动作）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void revoke(String licenseKey) {
        revoke(licenseKey, null);
    }

    /**
     * 带原因吊销（M5b）：紧急下架等场景必须能回答「这份授权为什么作废」，
     * 否则租户来问时只能翻日志。原因落 {@code revoke_reason} 列（方案 §3.3）。
     *
     * @param licenseKey 授权码
     * @param reason     吊销原因（平台 / 退款 / 违规 / 紧急下架），可为空
     */
    @Transactional(rollbackFor = Exception.class)
    public void revoke(String licenseKey, String reason) {
        if (!StringUtils.hasText(licenseKey)) {
            throw new IllegalArgumentException("授权码不能为空");
        }
        MarketPluginLicense license = findByKey(licenseKey.trim());
        if (license == null) {
            throw new IllegalArgumentException("授权码不存在: " + licenseKey.trim());
        }
        license.setStatus(STATUS_REVOKED);
        license.setState(STATE_REVOKED);
        license.setRevokeReason(StringUtils.hasText(reason) ? reason.trim() : null);
        licenseMapper.updateById(license);
        log.warn("吊销插件授权: plugin={} key={} reason={}",
                license.getPluginCode(), license.getLicenseKey(), license.getRevokeReason());
    }

    /**
     * 吊销某插件的<b>全部仍有效</b>授权（M5b，方案 §7.4 紧急下架联动）。
     *
     * <p><b>只吊销不卸载</b>（决策 6）：已安装实例由心跳检测降级停用，
     * 这里绝不动安装态——硬卸载会让租户数据跟着不可用，比「装着一个停用的插件」代价高得多。
     *
     * <p><b>幂等</b>：已吊销（status=0）的行不重复处理；同一插件反复下架只是重复计数为 0。
     *
     * @param pluginCode 插件编码
     * @param reason     吊销原因，写 {@code revoke_reason}
     * @return 实际吊销条数
     */
    @Transactional(rollbackFor = Exception.class)
    public int revokeAll(String pluginCode, String reason) {
        if (!StringUtils.hasText(pluginCode)) {
            throw new IllegalArgumentException("插件编码不能为空");
        }
        List<MarketPluginLicense> licenses = listByPlugin(pluginCode.trim());
        if (licenses == null || licenses.isEmpty()) {
            return 0;
        }
        int revoked = 0;
        for (MarketPluginLicense license : licenses) {
            if (!Integer.valueOf(STATUS_VALID).equals(license.getStatus())) {
                continue;
            }
            license.setStatus(STATUS_REVOKED);
            license.setState(STATE_REVOKED);
            license.setRevokeReason(StringUtils.hasText(reason) ? reason.trim() : null);
            licenseMapper.updateById(license);
            revoked++;
        }
        log.warn("批量吊销插件授权（下架联动）: plugin={} count={} reason={}", pluginCode.trim(), revoked, reason);
        return revoked;
    }

    /**
     * 某插件的全部授权记录（含已吊销/已过期），平台侧排障用。
     */
    public List<MarketPluginLicense> listByPlugin(String pluginCode) {
        LambdaQueryWrapper<MarketPluginLicense> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(pluginCode)) {
            wrapper.eq(MarketPluginLicense::getPluginCode, pluginCode.trim());
        }
        wrapper.orderByDesc(MarketPluginLicense::getId);
        return licenseMapper.selectList(wrapper);
    }

    /**
     * 本站点授权状态视图（租户侧展示）：不抛异常，未授权时返回结构化结果。
     */
    public Map<String, Object> statusOf(String pluginCode) {
        return statusOf(pluginCode, null);
    }

    /** 返回指定站点实例的授权状态；instanceId 为空时保持平台侧兼容查询。 */
    public Map<String, Object> statusOf(String pluginCode, String instanceId) {
        MarketPluginLicense license = findValid(pluginCode, instanceId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pluginCode", pluginCode);
        result.put("licensed", license != null);
        if (license != null) {
            result.put("licenseType", license.getLicenseType());
            result.put("licenseKey", license.getLicenseKey());
            result.put("instanceId", license.getInstanceId());
            result.put("expireTime", license.getExpireTime() == null
                    ? null : license.getExpireTime().format(TIME_FORMATTER));
            result.put("permanent", license.getExpireTime() == null);
            result.put("maintenanceUntil", license.getMaintenanceUntil() == null
                    ? null : license.getMaintenanceUntil().format(TIME_FORMATTER));
            result.put("maintenanceActive", OfficialMaintenancePolicy.active(license, LocalDateTime.now()));
            result.put("state", license.getState());
            result.put("firstInstallTime", license.getFirstInstallTime() == null
                    ? null : license.getFirstInstallTime().format(TIME_FORMATTER));
        }
        return result;
    }

    private MarketPluginLicense findByKey(String licenseKey) {
        return licenseMapper.selectOne(new LambdaQueryWrapper<MarketPluginLicense>()
                .eq(MarketPluginLicense::getLicenseKey, licenseKey)
                .last("limit 1"));
    }

    /**
     * 授权码格式：{@code HOU-<插件编码>-<16 位随机>}。
     */
    private String generateKey(String pluginCode) {
        String code = pluginCode.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (code.length() > 16) {
            code = code.substring(0, 16);
        }
        String random = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
        return "HOU-" + code + "-" + random.substring(0, 16);
    }
}
