package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.PluginDeveloper;
import com.houjiang.mall.entity.market.PluginSellerAccount;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.PluginDeveloperMapper;
import com.houjiang.mall.mapper.market.PluginSellerAccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 插件开发者身份体系（审计 2026-10-27 缺失能力 ①）。
 *
 * <p><b>为什么需要</b>：此前「发布插件」只认平台管理员角色，没有任何「开发者」概念——
 * 谁发布、能发多少个、被驳回后怎么改，全靠人工台账。本服务把发布主体落库，
 * 提供申请 / 审核 / 冻结 / 配额四条基本能力。
 *
 * <p><b>强制执行强度可配</b>（{@code houcloud.plugin.developer.enforcement}）：
 * <ul>
 *   <li>{@code off}：只记录不校验（自建单租户部署的默认兼容路径）；</li>
 *   <li>{@code warn}：未通过审核只告警，不拦发布（<b>默认</b>，灰度期用）；</li>
 *   <li>{@code strict}：未通过审核 / 超出配额直接拒绝发布。</li>
 * </ul>
 * 默认取 {@code warn} 而不是 {@code strict}，是为了不把现存的平台管理员发布流程
 * 在升级瞬间打断——真要收口时把开关拨到 strict 即可，无需改代码。
 *
 * @author Amos
 * @since 2026-10-27
 */
@Slf4j
@Service
public class PluginDeveloperService {

    private final PluginDeveloperMapper developerMapper;
    private final PluginSellerAccountMapper sellerAccountMapper;
    private final MarketPluginMapper marketPluginMapper;

    /**
     * 发布门禁强度：off / warn / strict。默认 warn。
     */
    @Value("${houcloud.plugin.developer.enforcement:warn}")
    private String enforcement = "warn";

    /** 新注册开发者的默认上架配额 */
    @Value("${houcloud.plugin.developer.default-plugin-quota:5}")
    private int defaultPluginQuota = 5;

    /** 新注册开发者的默认每月上传配额 */
    @Value("${houcloud.plugin.developer.default-version-quota:20}")
    private int defaultVersionQuota = 20;

    @Autowired
    public PluginDeveloperService(PluginDeveloperMapper developerMapper,
                                  PluginSellerAccountMapper sellerAccountMapper,
                                  MarketPluginMapper marketPluginMapper) {
        this.developerMapper = developerMapper;
        this.sellerAccountMapper = sellerAccountMapper;
        this.marketPluginMapper = marketPluginMapper;
    }

    /**
     * 提交（或更新）开发者申请。
     *
     * <p>已有记录时只更新资料、<b>不重置审核状态</b>：一个已通过审核的开发者改个联系电话，
     * 不应该被打回「待审核」导致发布中断。被驳回（status=3）的记录允许改资料后自动回到待审。
     */
    @Transactional(rollbackFor = Exception.class)
    public PluginDeveloper apply(Long userId, Map<String, Object> form) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录，无法提交开发者申请");
        }
        PluginDeveloper developer = findByUser(userId);
        LocalDateTime now = LocalDateTime.now();
        if (developer == null) {
            developer = new PluginDeveloper();
            developer.setUserId(userId);
            developer.setStatus(PluginDeveloper.STATUS_PENDING);
            developer.setQuotaPlugins(defaultPluginQuota);
            developer.setQuotaVersionsMonthly(defaultVersionQuota);
            developer.setCreateTime(now);
        } else if (Integer.valueOf(PluginDeveloper.STATUS_REJECTED).equals(developer.getStatus())) {
            developer.setStatus(PluginDeveloper.STATUS_PENDING);
            developer.setReviewNote("");
        }
        developer.setDisplayName(requireText(form, "displayName", "开发者名称不能为空"));
        developer.setContactEmail(text(form.get("contactEmail")));
        developer.setContactPhone(text(form.get("contactPhone")));
        developer.setCompany(text(form.get("company")));
        developer.setTaxNo(text(form.get("taxNo")));
        developer.setIntro(text(form.get("intro")));
        developer.setUpdateTime(now);
        if (developer.getId() == null) {
            developerMapper.insert(developer);
        } else {
            developerMapper.updateById(developer);
        }
        log.info("开发者资料已提交: userId={} developerId={} status={}", userId, developer.getId(), developer.getStatus());
        return developer;
    }

    /** 按平台用户查开发者身份 */
    public PluginDeveloper findByUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return developerMapper.selectOne(new LambdaQueryWrapper<PluginDeveloper>()
                .eq(PluginDeveloper::getUserId, userId)
                .isNull(PluginDeveloper::getDeleteTime)
                .last("limit 1"));
    }

    /** 列表（平台侧审核用） */
    public List<PluginDeveloper> list(Integer status) {
        LambdaQueryWrapper<PluginDeveloper> wrapper = new LambdaQueryWrapper<PluginDeveloper>()
                .isNull(PluginDeveloper::getDeleteTime)
                .orderByDesc(PluginDeveloper::getId);
        if (status != null) {
            wrapper.eq(PluginDeveloper::getStatus, status);
        }
        return developerMapper.selectList(wrapper);
    }

    /**
     * 审核：通过 → 正常；驳回 → 驳回并留原因（原因必填，否则开发者不知道改什么）。
     */
    @Transactional(rollbackFor = Exception.class)
    public PluginDeveloper review(Long id, boolean approve, String note, Long reviewerId) {
        PluginDeveloper developer = developerMapper.selectById(id);
        if (developer == null) {
            throw new IllegalArgumentException("开发者不存在: " + id);
        }
        if (!approve && !StringUtils.hasText(note)) {
            throw new IllegalArgumentException("驳回必须填写原因，便于开发者修改后重新提交");
        }
        developer.setStatus(approve ? PluginDeveloper.STATUS_ACTIVE : PluginDeveloper.STATUS_REJECTED);
        developer.setReviewNote(StringUtils.hasText(note) ? note.trim() : "");
        developer.setReviewTime(LocalDateTime.now());
        developer.setReviewerId(reviewerId);
        developer.setUpdateTime(LocalDateTime.now());
        developerMapper.updateById(developer);
        log.info("开发者审核完成: id={} approve={} reviewer={} note={}", id, approve, reviewerId, note);
        return developer;
    }

    /** 冻结 / 解冻（冻结后不能再发布新版本，已上架的插件不受影响） */
    @Transactional(rollbackFor = Exception.class)
    public PluginDeveloper freeze(Long id, boolean frozen, String note) {
        PluginDeveloper developer = developerMapper.selectById(id);
        if (developer == null) {
            throw new IllegalArgumentException("开发者不存在: " + id);
        }
        developer.setStatus(frozen ? PluginDeveloper.STATUS_FROZEN : PluginDeveloper.STATUS_ACTIVE);
        developer.setReviewNote(StringUtils.hasText(note) ? note.trim() : developer.getReviewNote());
        developer.setUpdateTime(LocalDateTime.now());
        developerMapper.updateById(developer);
        log.warn("开发者状态变更: id={} frozen={} note={}", id, frozen, note);
        return developer;
    }

    /**
     * 发布门禁：返回当前发布主体（可能为 {@code null}），不通过时按 enforcement 决定抛异常还是只告警。
     *
     * @param userId     平台用户 ID
     * @param pluginCode 目标插件编码（用于配额判定：更新已有插件不占新配额）
     * @return 命中的开发者记录；未登记时为 {@code null}
     */
    public PluginDeveloper assertCanPublish(Long userId, String pluginCode) {
        if ("off".equalsIgnoreCase(enforcement) || userId == null) {
            return null;
        }
        PluginDeveloper developer = findByUser(userId);
        String problem = null;
        if (developer == null) {
            problem = "当前账号尚未登记插件开发者身份，请先在「插件中心 → 开发者中心」提交申请";
        } else if (Integer.valueOf(PluginDeveloper.STATUS_FROZEN).equals(developer.getStatus())) {
            problem = "开发者身份已被冻结，无法发布新版本：" + nullToEmpty(developer.getReviewNote());
        } else if (!Integer.valueOf(PluginDeveloper.STATUS_ACTIVE).equals(developer.getStatus())) {
            problem = "开发者身份尚未通过审核（当前状态 " + statusText(developer.getStatus()) + "），审核通过后才能发布";
        } else {
            problem = quotaProblem(developer, pluginCode);
        }
        if (problem == null) {
            return developer;
        }
        if ("strict".equalsIgnoreCase(enforcement)) {
            throw new IllegalStateException(problem);
        }
        log.warn("开发者门禁未通过（{} 模式放行）: userId={} plugin={} reason={}", enforcement, userId, pluginCode, problem);
        return developer;
    }

    /**
     * 配额判定：只在「上架一个当前不属于自己的插件」时占用新配额。
     *
     * <p>已属于自己的插件发新版本不占新配额——否则改个版本号就被拦，
     * 开发者只会把插件换个编码重发，配额形同虚设。
     */
    private String quotaProblem(PluginDeveloper developer, String pluginCode) {
        int quota = developer.getQuotaPlugins() == null ? defaultPluginQuota : developer.getQuotaPlugins();
        if (quota <= 0) {
            return "开发者配额为 0，请联系平台扩容";
        }
        List<MarketPlugin> mine = marketPluginMapper.selectList(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getAuthorId, developer.getUserId())
                .eq(MarketPlugin::getDeleted, 0));
        boolean alreadyMine = mine.stream().anyMatch(item -> StringUtils.hasText(pluginCode)
                && pluginCode.trim().equals(item.getPluginCode()));
        if (!alreadyMine && mine.size() >= quota) {
            return "已上架 " + mine.size() + " 个插件，达到配额上限 " + quota + "，请联系平台扩容";
        }
        return null;
    }

    /** 保存收款账户（敏感字段只做长度校验；生产环境应改为密文存储） */
    @Transactional(rollbackFor = Exception.class)
    public PluginSellerAccount saveAccount(Long developerId, Map<String, Object> form) {
        if (developerId == null) {
            throw new IllegalArgumentException("请先完成开发者登记");
        }
        String accountNo = requireText(form, "accountNo", "收款账号不能为空");
        PluginSellerAccount account = sellerAccountMapper.selectOne(new LambdaQueryWrapper<PluginSellerAccount>()
                .eq(PluginSellerAccount::getDeveloperId, developerId).last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (account == null) {
            account = new PluginSellerAccount();
            account.setDeveloperId(developerId);
            account.setCreateTime(now);
            account.setStatus(PluginSellerAccount.STATUS_PENDING);
        }
        account.setSettleType(text(form.get("settleType")).toUpperCase(Locale.ROOT));
        if (!StringUtils.hasText(account.getSettleType())) {
            account.setSettleType("BANK");
        }
        account.setAccountName(text(form.get("accountName")));
        account.setAccountNo(accountNo.trim());
        account.setBankName(text(form.get("bankName")));
        account.setUpdateTime(now);
        if (account.getId() == null) {
            sellerAccountMapper.insert(account);
        } else {
            sellerAccountMapper.updateById(account);
        }
        return account;
    }

    /** 查询收款账户（对外回显必须脱敏） */
    public PluginSellerAccount findAccount(Long developerId) {
        if (developerId == null) {
            return null;
        }
        return sellerAccountMapper.selectOne(new LambdaQueryWrapper<PluginSellerAccount>()
                .eq(PluginSellerAccount::getDeveloperId, developerId).last("limit 1"));
    }

    /** 对外视图：账号脱敏，只留后四位 */
    public Map<String, Object> toView(PluginDeveloper developer) {
        if (developer == null) {
            return Map.of();
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", developer.getId());
        view.put("userId", developer.getUserId());
        view.put("displayName", developer.getDisplayName());
        view.put("contactEmail", developer.getContactEmail());
        view.put("contactPhone", developer.getContactPhone());
        view.put("company", developer.getCompany());
        view.put("taxNo", developer.getTaxNo());
        view.put("intro", developer.getIntro());
        view.put("status", developer.getStatus());
        view.put("statusText", statusText(developer.getStatus()));
        view.put("quotaPlugins", developer.getQuotaPlugins());
        view.put("quotaVersionsMonthly", developer.getQuotaVersionsMonthly());
        view.put("reviewNote", developer.getReviewNote());
        view.put("reviewTime", developer.getReviewTime());
        view.put("createTime", developer.getCreateTime());
        return view;
    }

    /** 收款账户视图（脱敏） */
    public Map<String, Object> toAccountView(PluginSellerAccount account) {
        if (account == null) {
            return Map.of();
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", account.getId());
        view.put("settleType", account.getSettleType());
        view.put("accountName", account.getAccountName());
        view.put("accountNoMasked", maskAccount(account.getAccountNo()));
        view.put("bankName", account.getBankName());
        view.put("status", account.getStatus());
        return view;
    }

    /** 只保留后四位：`**** **** 1234` */
    public static String maskAccount(String accountNo) {
        if (!StringUtils.hasText(accountNo)) {
            return "";
        }
        String value = accountNo.trim();
        if (value.length() <= 4) {
            return "****";
        }
        String tail = value.substring(value.length() - 4);
        return "****" + tail;
    }

    private static String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case PluginDeveloper.STATUS_PENDING -> "待审核";
            case PluginDeveloper.STATUS_ACTIVE -> "正常";
            case PluginDeveloper.STATUS_FROZEN -> "已冻结";
            case PluginDeveloper.STATUS_REJECTED -> "已驳回";
            default -> "未知";
        };
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String requireText(Map<String, Object> form, String key, String message) {
        Object value = form == null ? null : form.get(key);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            throw new IllegalArgumentException(message);
        }
        return String.valueOf(value).trim();
    }
}
