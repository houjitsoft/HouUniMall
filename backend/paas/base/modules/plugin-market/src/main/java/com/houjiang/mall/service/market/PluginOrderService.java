package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.commons.dto.payment.MallCashierStatusResponse;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.config.market.PluginMarketPayProperties;
import com.houjiang.mall.config.market.PluginPayConfigStore;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.market.MarketPluginOrder;
import com.houjiang.mall.entity.market.MarketPluginPayNotify;
import com.houjiang.mall.pay.plugin.PayIntent;
import com.houjiang.mall.pay.plugin.PluginPayChannel;
import com.houjiang.mall.pay.plugin.PluginPayChannelRouter;
import com.houjiang.mall.pay.plugin.PluginPaySigner;
import com.houjiang.plugin.market.adapter.outbound.feign.OrderPaasFeign;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginOrderMapper;
import com.houjiang.mall.mapper.market.MarketPluginPayNotifyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 插件市场在线订单：下单 → 支付 → 自动发证。
 *
 * <p><b>与线下链路的关系</b>：原先是「线下收款 → 平台手工 issue 授权码 → 租户拿码激活」。
 * 在线链路把最后一步自动化：支付成功后由本服务调用 {@link PluginLicenseService#issue}，
 * 并<b>直接把授权绑定到下单时的 instanceId</b>——租户不需要再手工激活，授权立即出现在「授权管理」页。
 *
 * <p><b>支付通道</b>：不重复造支付轮子，复用商城既有收银台 {@code /order/mall/payment/create}。
 *
 * <p><b>幂等</b>：结算以订单状态为准，已支付订单重复调用不会再发第二张授权。
 *
 * @author Amos
 */
@Slf4j
@Service
public class PluginOrderService {

    /** 订单支付有效期兜底（分钟），配置未设置时使用 */
    private static final int PAY_WINDOW_MINUTES = 30;

    /** 已上架 */
    private static final int STATUS_PUBLISHED = 2;

    private final MarketPluginOrderMapper orderMapper;
    private final MarketPluginMapper marketPluginMapper;
    private final MarketPluginPayNotifyMapper notifyMapper;
    private final PluginLicenseService licenseService;
    private final OrderPaasFeign orderPaasFeign;
    private final PluginPayConfigStore payStore;
    private final PluginPayChannelRouter channelRouter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 分账能力（可选依赖）：老部署里没有这张表时不能把发证链路带崩，
     * 因此用 {@code required=false}，为空即跳过记账。
     */
    @Autowired(required = false)
    private PluginSettlementService settlementService;

    /**
     * 平台市场客户端（可选依赖，2026-10-30）：发证后异步把授权上报到平台，
     * 平台 acquire 换票按 siteCode 校验付费授权——否则站点买了、装不了。
     * 平台自身 / 未对接平台的老站点不注入或未配地址，客户端内部直接跳过，
     * 不影响发证主链路。
     */
    @Autowired(required = false)
    private PlatformMarketClient platformMarketClient;

    @Autowired
    public PluginOrderService(MarketPluginOrderMapper orderMapper,
                              MarketPluginMapper marketPluginMapper,
                              MarketPluginPayNotifyMapper notifyMapper,
                              PluginLicenseService licenseService,
                              OrderPaasFeign orderPaasFeign,
                              PluginPayConfigStore payStore,
                              PluginPayChannelRouter channelRouter) {
        this.orderMapper = orderMapper;
        this.marketPluginMapper = marketPluginMapper;
        this.notifyMapper = notifyMapper;
        this.licenseService = licenseService;
        this.orderPaasFeign = orderPaasFeign;
        this.payStore = payStore;
        this.channelRouter = channelRouter;
    }

    /**
     * 当前生效配置快照：基线（yml/Nacos/env）叠加 {@code hou_market_pay_config} 覆盖后的最终值。
     * 每次调用都重新取，运营在后台改价 / 切渠道后无需重启。
     */
    private PluginMarketPayProperties cfg() {
        return payStore.current();
    }

    /**
     * 下单：校验插件可售性后生成待支付订单（未调用任何支付渠道）。
     *
     * @param pluginCode  插件编码
     * @param licenseType PAID / TRIAL，为空取插件自身标价类型
     * @param version     目标版本，可为空（表示最新版）
     * @param validDays   授权有效天数（PAID 可指定，如 365；为空或 &lt;=0 表示永久）
     * @param instanceId  站点实例（域名），支付成功后授权绑定到此实例
     * @param buyerId     购买方站点 ID，为空默认 1
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginOrder createOrder(String pluginCode, String licenseType, String version,
                                         Integer validDays, String instanceId, Long buyerId) {
        return createOrder(pluginCode, licenseType, version, validDays, instanceId, buyerId, null);
    }

    /**
     * 下单（完整参数，含价格套餐）。
     *
     * <p><b>定价优先级</b>：配置套餐 {@code pay.plans.<pluginCode>.<planCode>} &gt; 商品表
     * {@code hou_market_plugin.price_cents}。配了套餐就必须传 {@code planCode}——
     * 否则「按商品标价买了年度版」这种资损没人背。
     *
     * @param planCode 价格套餐编码；插件已配套餐时必填
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginOrder createOrder(String pluginCode, String licenseType, String version,
                                         Integer validDays, String instanceId, Long buyerId,
                                         String planCode) {
        // 收费链路开关与配置自检：缺失直接拒绝下单，而不是下出一张永远付不掉的单
        cfg().assertUsable();

        MarketPlugin plugin = requirePublishedPlugin(pluginCode);
        if (!StringUtils.hasText(instanceId)) {
            throw new IllegalArgumentException("缺少站点实例标识，无法确定授权归属站点");
        }

        PluginMarketPayProperties.PlanProperties plan = resolvePlan(plugin, planCode);
        String type = resolveLicenseType(plugin, licenseType, plan);
        Integer days = TYPE_TRIAL.equals(type) ? plugin.getTrialDays() : validDays;
        if (TYPE_TRIAL.equals(type) && (days == null || days <= 0)) {
            throw new IllegalStateException("试用天数无效，拒绝下单: " + pluginCode);
        }
        BigDecimal amount = BigDecimal.ZERO;
        String currency = cfg().getCurrency();

        if (plan != null) {
            amount = plan.getPrice() == null ? BigDecimal.ZERO : plan.getPrice();
            if (StringUtils.hasText(plan.getCurrency())) {
                currency = plan.getCurrency().trim();
            }
            if (plan.getValidDays() != null && plan.getValidDays() > 0) {
                days = plan.getValidDays();
            }
        } else if (TYPE_PAID.equals(type)) {
            Integer priceCents = plugin.getPriceCents();
            if (priceCents == null || priceCents <= 0) {
                throw new IllegalStateException("该插件未配置价格（price_cents 为空且未配置价格套餐），"
                        + "无法在线购买: " + pluginCode
                        + "。请在商品表补价，或配置 houcloud.plugin.market.pay.plans." + pluginCode);
            }
            // 分为整数单位，左移两位即元，精确无损，避免引入舍入模式
            amount = BigDecimal.valueOf(priceCents).movePointLeft(2);
        }

        // 付费插件金额必须 > 0：价格为 0 的 PAID 单会在 pay() 里被当成「试用」直接结单发证，
        // 只要套餐/商品表的价格字段被误填成 0（或配置解析失败），就等于白送正版授权。
        if (TYPE_PAID.equals(type) && amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("付费插件（PAID）订单金额必须大于 0，当前为 " + amount
                    + "。请检查套餐 price 或商品表 price_cents 是否被填成 0；"
                    + "确需免费发放请改用 TRIAL 类型");
        }

        MarketPluginOrder order = new MarketPluginOrder();
        order.setOrderNo(generateOrderNo());
        order.setPluginCode(plugin.getPluginCode());
        order.setVersion(StringUtils.hasText(version) ? version.trim() : null);
        order.setLicenseType(type);
        // 🔴 2026-10-27 修 P2：PAID 单以前把 valid_days 一律写 NULL，
        // 导致「30 天版」和「永久版」拿到完全相同的授权（套餐价差无法解释，也无法对账）。
        // 现在按实际期限落库，发证时再折算成该笔 grant 的 term_days。
        order.setValidDays(days != null && days > 0 ? days : null);
        order.setAmount(amount);
        order.setCurrency(currency);
        order.setBuyerId(buyerId == null ? 1L : buyerId);
        order.setInstanceId(instanceId.trim());
        order.setPlanCode(plan == null ? null : planCode.trim());
        order.setChannel(channelRouter.resolveCode());
        order.setSandbox(cfg().isSandbox() ? 1 : 0);
        order.setStatus(MarketPluginOrder.STATUS_PENDING);
        order.setExpireTime(LocalDateTime.now().plusMinutes(cfg().effectivePayWindowMinutes()));
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(order.getCreateTime());
        orderMapper.insert(order);

        log.info("创建插件订单: order={} plugin={} type={} plan={} amount={} {} instance={} channel={} sandbox={}",
                order.getOrderNo(), pluginCode, type, order.getPlanCode(), amount, currency,
                instanceId, order.getChannel(), order.getSandbox());
        return order;
    }

    /**
     * 解析价格套餐：插件配套餐时 planCode 必填且必须命中一个已启用套餐。
     */
    private PluginMarketPayProperties.PlanProperties resolvePlan(MarketPlugin plugin, String planCode) {
        String code = plugin.getPluginCode();
        if (!cfg().hasPlans(code)) {
            return null;
        }
        if (!StringUtils.hasText(planCode)) {
            throw new IllegalArgumentException("该插件按套餐售卖，下单必须指定 planCode（可选："
                    + channelRouter.plansOf(code).stream()
                    .map(item -> String.valueOf(item.get("code")))
                    .collect(java.util.stream.Collectors.joining("、")) + "）");
        }
        PluginMarketPayProperties.PlanProperties plan = cfg().plan(code, planCode);
        if (plan == null) {
            throw new IllegalArgumentException("价格套餐不存在: " + pluginCode(plugin) + "/" + planCode.trim());
        }
        if (!plan.isEnabled()) {
            throw new IllegalStateException("价格套餐已下架: " + pluginCode(plugin) + "/" + planCode.trim());
        }
        return plan;
    }

    private String pluginCode(MarketPlugin plugin) {
        return plugin.getPluginCode();
    }

    /**
     * 发起支付：调用既有商城收银台拿支付参数。
     *
     * <p>金额为 0（如试用）时不满足收银台「金额 &gt;= 0.01」的下限约束，
     * 因此直接走自动结单，不经收银台。
     *
     * @return 含支付参数（payData）的订单
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginOrder pay(String orderNo, String payMethod, String terminal,
                                 String userId, String notifyUrl, String returnUrl) {
        return pay(orderNo, payMethod, terminal, userId, notifyUrl, returnUrl, null);
    }

    /**
     * 发起支付（带站点归属校验）。
     *
     * @param instanceId 当前登录站点；非空时要求订单属于该站点
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginOrder pay(String orderNo, String payMethod, String terminal,
                                 String userId, String notifyUrl, String returnUrl,
                                 String instanceId) {
        cfg().assertUsable();
        MarketPluginOrder order = requireOrder(orderNo, instanceId);
        assertPayable(order);

        if (order.getAmount() == null || order.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("金额为 0（试用/免费套餐），跳过渠道直接结单: order={}", orderNo);
            return settle(order);
        }

        // 渠道以「下单时锁定的渠道」为准：中途改配置不会影响在途订单，避免对账错位
        String channelCode = StringUtils.hasText(order.getChannel())
                ? order.getChannel().trim().toUpperCase()
                : channelRouter.resolveCode();
        PluginPayChannel channel = channelRouter.require(channelCode);
        assertPayMethodSupported(channelCode, payMethod);
        // 回调地址只认配置：允许请求方自带 notifyUrl，等于把「付完钱通知谁」交给调用方——
        // 攻击者指到自己机器上，就能在验签密钥之外再开一个口子（还能拿到完整回调报文）。
        // 传了就丢弃并记 warn，方便排查「为什么配的地址没生效」。
        if (StringUtils.hasText(notifyUrl)) {
            log.warn("已忽略请求携带的 notifyUrl，回调地址以配置为准: order={} ignored={}",
                    orderNo, notifyUrl);
        }
        String effectiveNotifyUrl = channelRouter.notifyUrl();
        String effectiveReturnUrl = StringUtils.hasText(returnUrl)
                ? returnUrl.trim()
                : (cfg().getReturnUrl() == null || cfg().getReturnUrl().isBlank()
                ? null : cfg().getReturnUrl().trim());

        PayIntent intent;
        try {
            intent = channel.createPayment(order, payMethod, terminal, userId,
                    effectiveNotifyUrl, effectiveReturnUrl);
        } catch (IllegalStateException e) {
            // 渠道不可用 / 参数缺失：落到订单上，前端能直接看到原因，不用翻日志
            order.setFailReason("发起支付失败: " + e.getMessage());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            throw e;
        }

        order.setChannel(channel.code());
        order.setCashierNo(intent.getTradeNo());
        order.setPayOrderId(intent.getPayOrderId());
        order.setPayMethod(payMethod);
        order.setTerminal(StringUtils.hasText(terminal) ? terminal : "PC");
        order.setPayData(toJson(intent.getPayData()));
        order.setFailReason(null);
        if (intent.getExpireTime() != null) {
            order.setExpireTime(intent.getExpireTime());
        }
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单已发起支付: order={} channel={} tradeNo={} method={} notifyUrl={}",
                orderNo, channel.code(), intent.getTradeNo(), payMethod, effectiveNotifyUrl);
        return order;
    }

    /**
     * 查询订单并尝试结算：先查收银台真实支付状态，命中成功则自动发证。
     *
     * <p>支付回调尚未接入时，前端以此轮询；回调接入后二者共用 {@link #settle}，天然幂等。
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginOrder syncAndSettle(String orderNo, String userId) {
        return syncAndSettle(orderNo, userId, null);
    }

    /**
     * 查询并同步结算（带站点归属校验）。
     *
     * @param instanceId 当前登录站点；非空时要求订单属于该站点
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginOrder syncAndSettle(String orderNo, String userId, String instanceId) {
        MarketPluginOrder order = requireOrder(orderNo, instanceId);
        if (Integer.valueOf(MarketPluginOrder.STATUS_PAID).equals(order.getStatus())) {
            return order;
        }
        // 待支付与支付失败都要继续追问收银台：
        // 「第一次支付失败、换支付方式后又成功了」完全可能，只认 PENDING 会把这类订单永久漏掉，
        // 用户只能等渠道回调；回调若也没配好，这笔钱就悬在半空。
        boolean traceable = Integer.valueOf(MarketPluginOrder.STATUS_PENDING).equals(order.getStatus())
                || Integer.valueOf(MarketPluginOrder.STATUS_PAY_FAILED).equals(order.getStatus());
        if (!traceable) {
            return order;
        }
        // 超时未付：先关闭，不再向收银台追问
        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
            return close(orderNo);
        }
        if (!StringUtils.hasText(order.getCashierNo())) {
            return order;
        }

        ResponseResult<MallCashierStatusResponse> result = orderPaasFeign.mallCashierStatus(
                order.getCashierNo(),
                StringUtils.hasText(userId) ? userId : String.valueOf(order.getBuyerId()));
        if (result == null || result.getData() == null) {
            return order;
        }
        MallCashierStatusResponse status = result.getData();
        if ("SUCCESS".equalsIgnoreCase(status.getStatus())
                || Integer.valueOf(1).equals(status.getStatusCode())) {
            // 🔴 2026-10-27 修 P0：轮询结算以前只看状态码，不看金额，也不看这笔收银台单号
            // 到底属于哪个商城订单。只要收银台回 SUCCESS 就给插件发证——低价单、串单都能换到全价授权。
            // 现在两道核对：单号归属 + 实收金额，任一不符即拒结算并留告警，等人工核对。
            if (StringUtils.hasText(status.getMallOrderNo())
                    && !status.getMallOrderNo().trim().equals(order.getOrderNo())) {
                log.error("收银台状态返回的商城订单号与插件订单不一致，拒绝结算: order={} cashierOrder={} cashierNo={}",
                        order.getOrderNo(), status.getMallOrderNo(), order.getCashierNo());
                throw new IllegalStateException("收银台订单号与插件订单不一致，拒绝结算: " + order.getOrderNo());
            }
            try {
                PluginPaySigner.assertAmountMatches(order.getAmount(), status.getAmount());
            } catch (RuntimeException e) {
                log.error("轮询结算金额校验失败，拒绝结算: order={} expect={} actual={}",
                        order.getOrderNo(), order.getAmount(), status.getAmount());
                throw e;
            }
            return settle(order);
        }
        return order;
    }

    /**
     * 结算（幂等）：标记已支付并自动签发授权到本订单的站点实例。
     *
     * <p>这是在线链路取代「线下手工发证」的关键一步：授权签发即绑定 instanceId，
     * 因此租户在「授权管理」页能立刻看到，无需再手工激活。
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginOrder settle(MarketPluginOrder order) {
        if (Integer.valueOf(MarketPluginOrder.STATUS_PAID).equals(order.getStatus())) {
            return order;
        }
        // 超时关闭后才收到支付成功：钱确实到账了，补发授权并告警。
        // 不发证等于收了钱不给货，是更严重的资损；是否退款由运营据这笔告警 decide。
        if (Integer.valueOf(MarketPluginOrder.STATUS_CLOSED).equals(order.getStatus())) {
            log.warn("订单已超时关闭后收到支付成功，补发授权并需人工确认是否退款: order={} plugin={}",
                    order.getOrderNo(), order.getPluginCode());
        }
        // 并发抢锁：渠道重发 + 前端轮询可能同时进来，靠「条件更新成功者才发证」保证只发一张。
        // 先改状态再签发，签发失败由事务整体回滚，不会出现「已支付但没授权」。
        // 已退款的订单不在允许集合里：钱已退回去，不能再被结算成已支付。
        int locked = orderMapper.update(null, new LambdaUpdateWrapper<MarketPluginOrder>()
                .set(MarketPluginOrder::getStatus, MarketPluginOrder.STATUS_PAID)
                .eq(MarketPluginOrder::getId, order.getId())
                .in(MarketPluginOrder::getStatus,
                        MarketPluginOrder.STATUS_PENDING,
                        MarketPluginOrder.STATUS_PAY_FAILED,
                        MarketPluginOrder.STATUS_CLOSED));
        if (locked == 0) {
            log.warn("订单结算抢锁失败（已被并发结算或处于终态），本次不发证: order={} status={}",
                    order.getOrderNo(), order.getStatus());
            return requireOrder(order.getOrderNo());
        }
        boolean sandboxGrant = Integer.valueOf(1).equals(order.getSandbox())
                || "MOCK".equalsIgnoreCase(order.getChannel());
        MarketPluginLicense license = TYPE_PAID.equals(order.getLicenseType())
                ? licenseService.issuePaidOrder(order.getPluginCode(), order.getBuyerId(), order.getInstanceId(),
                order.getOrderNo(), sandboxGrant, order.getValidDays())
                : licenseService.issue(
                order.getPluginCode(),
                order.getBuyerId(),
                order.getInstanceId(),
                order.getLicenseType(),
                order.getValidDays());

        order.setStatus(MarketPluginOrder.STATUS_PAID);
        order.setPaidTime(LocalDateTime.now());
        order.setLicenseId(license.getId());
        order.setLicenseKey(license.getLicenseKey());
        order.setUpdateTime(order.getPaidTime());
        orderMapper.updateById(order);

        log.info("订单支付成功并自动发证: order={} plugin={} licenseKey={} 绑定实例={}",
                order.getOrderNo(), order.getPluginCode(), license.getLicenseKey(), order.getInstanceId());

        // 第三方分账：订单真实收款后按抽成比例记一条流水（沙箱/MOCK/0 元单由服务内部跳过）。
        // 独立事务 + 吞异常：分账表缺失或记账失败绝不能把「钱已付、证已发」的订单退回待支付——
        // 那是比少一条分账流水严重得多的资损。失败留 error 日志，由运营对账补记。
        if (settlementService != null) {
            try {
                settlementService.recordForOrder(order);
            } catch (RuntimeException e) {
                log.error("分账流水记账失败（不影响发证，需对账补记）: order={} plugin={}",
                        order.getOrderNo(), order.getPluginCode(), e);
            }
        }
        // 购买授权上报平台（2026-10-30，异步不阻断）：付费插件的平台换票校验站点授权，
        // 站点发证后必须让平台知道这笔购买，否则「站点已买、平台拒发」闭环断裂。
        // 独立线程执行：上报卡顿/失败不影响结算事务返回；失败客户端记日志，
        // 授权主记录在站点侧，后续购买/重装可重试补齐。
        if (platformMarketClient != null && TYPE_PAID.equals(order.getLicenseType())) {
            // order/license/sandboxGrant 均为 effectively final，lambda 直接引用
            try {
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    Map<String, Object> grant = new LinkedHashMap<>();
                    grant.put("pluginCode", order.getPluginCode());
                    grant.put("orderNo", order.getOrderNo());
                    grant.put("licenseKey", license.getLicenseKey());
                    grant.put("licenseType", order.getLicenseType());
                    grant.put("instanceId", order.getInstanceId());
                    if (order.getValidDays() != null) {
                        grant.put("termDays", order.getValidDays());
                    }
                    grant.put("sandbox", sandboxGrant ? 1 : 0);
                    if (license.getExpireTime() != null) {
                        grant.put("expireTime", license.getExpireTime().toString());
                    }
                    platformMarketClient.reportLicenseGrant(grant);
                });
            } catch (RuntimeException e) {
                log.warn("购买授权上报线程提交失败（不阻断发证）: order={}", order.getOrderNo(), e);
            }
        }
        return order;
    }

    /**
     * 标记支付失败（渠道明确通知失败 / 用户取消）。订单回到可重新发起支付的状态。
     *
     * <p>不做「失败即关闭」：用户只是这次没付成（余额不足、取消），
     * 关掉要重走一遍下单流程，转化率白白掉一截。
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginOrder markPayFailed(String orderNo, String reason) {
        MarketPluginOrder order = requireOrder(orderNo);
        if (Integer.valueOf(MarketPluginOrder.STATUS_PAID).equals(order.getStatus())) {
            // 已支付的订单收到失败通知：只记日志不回退，避免把已发的授权收回（资损更大）
            log.warn("已支付订单收到失败通知，忽略: order={} reason={}", orderNo, reason);
            return order;
        }
        // 终态保护：已关闭 / 已退款的订单不能再被打回 PAY_FAILED——
        // PAY_FAILED 是「可重新发起支付」的中间态，把终态订单放回去等于让它复活，
        // 已退款的钱和已回收的授权会对不上。
        if (Integer.valueOf(MarketPluginOrder.STATUS_CLOSED).equals(order.getStatus())
                || Integer.valueOf(MarketPluginOrder.STATUS_REFUNDED).equals(order.getStatus())) {
            log.warn("终态订单收到失败通知，保持终态不做回退: order={} status={} reason={}",
                    orderNo, statusText(order.getStatus()), reason);
            return order;
        }
        LocalDateTime now = LocalDateTime.now();
        String failReason = reason == null ? null : reason.trim();
        int changed = orderMapper.update(null, new LambdaUpdateWrapper<MarketPluginOrder>()
                .set(MarketPluginOrder::getStatus, MarketPluginOrder.STATUS_PAY_FAILED)
                .set(MarketPluginOrder::getFailReason, failReason)
                .set(MarketPluginOrder::getUpdateTime, now)
                .eq(MarketPluginOrder::getId, order.getId())
                .in(MarketPluginOrder::getStatus,
                        MarketPluginOrder.STATUS_PENDING, MarketPluginOrder.STATUS_PAY_FAILED));
        if (changed == 0) {
            // 当前读避免事务快照再次返回结算前的旧状态。
            return orderMapper.selectOne(new LambdaQueryWrapper<MarketPluginOrder>()
                    .eq(MarketPluginOrder::getId, order.getId()).last("limit 1 for update"));
        }
        order.setStatus(MarketPluginOrder.STATUS_PAY_FAILED);
        order.setFailReason(failReason);
        order.setUpdateTime(now);
        log.info("订单支付失败: order={} reason={}", orderNo, reason);
        return order;
    }

    /**
     * 退款：吊销该订单已签发的授权，并把订单置为已退款。
     *
     * <p><b>只吊销不卸载</b>：租户的插件与数据保留，由授权心跳在下一个周期把它软停，
     * 直接硬卸载会让租户数据跟着不可用，纠纷成本远高于「装着一个已失效的插件」。
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginOrder refund(String orderNo, String reason) {
        MarketPluginOrder order = requireOrder(orderNo);
        if (Integer.valueOf(MarketPluginOrder.STATUS_REFUNDED).equals(order.getStatus())) {
            return order;
        }
        // 只有「已支付」的订单才谈得上退款：待支付/支付失败的订单根本没收到钱，
        // 退款回调落在上面会把一张没成交的单标成已退款，对账时凭空多出一笔退款流水。
        if (!Integer.valueOf(MarketPluginOrder.STATUS_PAID).equals(order.getStatus())) {
            log.warn("未支付订单收到退款通知，忽略: order={} status={}",
                    orderNo, statusText(order.getStatus()));
            return order;
        }
        if(TYPE_PAID.equals(order.getLicenseType())) {
            licenseService.refundPaidOrder(order.getPluginCode(),order.getInstanceId(),order.getOrderNo(),
                    StringUtils.hasText(reason)?reason.trim():"订单退款");
        } else if(StringUtils.hasText(order.getLicenseKey())) {
            licenseService.revoke(order.getLicenseKey(),StringUtils.hasText(reason)?reason.trim():"订单退款");
        }
        order.setStatus(MarketPluginOrder.STATUS_REFUNDED);
        order.setFailReason(StringUtils.hasText(reason) ? reason.trim() : "订单退款");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.warn("插件订单退款完成: order={} plugin={} license={}",
                orderNo, order.getPluginCode(), order.getLicenseKey());
        return order;
    }

    /**
     * 卸载后清理支付凭证与回调流水（避免残留数据）。
     *
     * <p><b>清什么</b>：{@code pay_data}（二维码/表单等支付参数，卸载后无任何用途，
     * 留着等于在库里存一份可被复用的支付链接）与该订单的回调流水。
     * <p><b>不清什么</b>：订单主记录本身必须保留——它是「这个站点买过什么」的唯一财务凭据，
     * 删了就没法对账，也没法回答「租户说买过但查不到」。
     *
     * @param instanceId 站点实例（必填，fail-closed：宁可不清，也不能跨站点清）
     * @param pluginCode 插件编码
     * @return 清理的订单条数
     */
    @Transactional(rollbackFor = Exception.class)
    public int scrubCredentials(String pluginCode, String instanceId) {
        if (!StringUtils.hasText(pluginCode) || !StringUtils.hasText(instanceId)) {
            return 0;
        }
        List<MarketPluginOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<MarketPluginOrder>()
                .eq(MarketPluginOrder::getPluginCode, pluginCode.trim())
                .eq(MarketPluginOrder::getInstanceId, instanceId.trim()));
        if (orders == null || orders.isEmpty()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (MarketPluginOrder order : orders) {
            // 回调流水一律清（含完整报文与渠道单号），与有没有 payData 无关——
            // 早期实现只在 payData 非空时才清，导致「未发起支付但有失败回调」的订单留下流水
            if (notifyMapper != null) {
                notifyMapper.delete(new LambdaQueryWrapper<MarketPluginPayNotify>()
                        .eq(MarketPluginPayNotify::getOrderNo, order.getOrderNo()));
            }
            if (!StringUtils.hasText(order.getPayData())) {
                continue;
            }
            order.setPayData(null);
            order.setUpdateTime(now);
            orderMapper.updateById(order);
            count++;
        }
        if (count > 0) {
            log.info("卸载后清理支付凭证: plugin={} instance={} orders={}", pluginCode, instanceId, count);
        }
        return count;
    }

    /**
     * 扫描超时未支付订单并关闭（供定时任务调用）。
     *
     * @return 本次关闭的订单数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int closeExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<MarketPluginOrder> expired = orderMapper.selectList(new LambdaQueryWrapper<MarketPluginOrder>()
                .in(MarketPluginOrder::getStatus,
                        MarketPluginOrder.STATUS_PENDING, MarketPluginOrder.STATUS_PAY_FAILED)
                .lt(MarketPluginOrder::getExpireTime, now));
        int count = 0;
        for (MarketPluginOrder order : expired) {
            count += orderMapper.update(null, new LambdaUpdateWrapper<MarketPluginOrder>()
                    .set(MarketPluginOrder::getStatus, MarketPluginOrder.STATUS_CLOSED)
                    .set(MarketPluginOrder::getUpdateTime, now)
                    .eq(MarketPluginOrder::getId, order.getId())
                    .in(MarketPluginOrder::getStatus,
                            MarketPluginOrder.STATUS_PENDING, MarketPluginOrder.STATUS_PAY_FAILED)
                    .lt(MarketPluginOrder::getExpireTime, now));
        }
        if (count > 0) {
            log.info("关闭超时未支付插件订单: count={}", count);
        }
        return count;
    }

    /**
     * 主动关闭（取消）订单。
     *
     * <p>待支付与支付失败都可取消：用户点了「不买了」，不该留一张还能被支付的单在列表里。
     * 已支付 / 已退款是终态，取消请求一律忽略（不能把已成交的单关掉）。
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginOrder close(String orderNo) {
        return close(orderNo, null);
    }

    /**
     * 取消待支付订单（带站点归属校验）。
     *
     * @param instanceId 当前登录站点；非空时要求订单属于该站点
     */
    @Transactional(rollbackFor = Exception.class)
    public MarketPluginOrder close(String orderNo, String instanceId) {
        MarketPluginOrder order = requireOrder(orderNo, instanceId);
        boolean closable = Integer.valueOf(MarketPluginOrder.STATUS_PENDING).equals(order.getStatus())
                || Integer.valueOf(MarketPluginOrder.STATUS_PAY_FAILED).equals(order.getStatus());
        if (!closable) {
            return order;
        }
        LocalDateTime now = LocalDateTime.now();
        int changed = orderMapper.update(null, new LambdaUpdateWrapper<MarketPluginOrder>()
                .set(MarketPluginOrder::getStatus, MarketPluginOrder.STATUS_CLOSED)
                .set(MarketPluginOrder::getUpdateTime, now)
                .eq(MarketPluginOrder::getId, order.getId())
                .in(MarketPluginOrder::getStatus,
                        MarketPluginOrder.STATUS_PENDING, MarketPluginOrder.STATUS_PAY_FAILED));
        if (changed == 0) {
            return orderMapper.selectOne(new LambdaQueryWrapper<MarketPluginOrder>()
                    .eq(MarketPluginOrder::getId, order.getId()).last("limit 1 for update"));
        }
        order.setStatus(MarketPluginOrder.STATUS_CLOSED);
        order.setUpdateTime(now);
        return order;
    }

    /**
     * 本站点订单列表（按 instanceId 收敛，隔离其它站点）。
     *
     * @param limit 返回条数上限（防御：一个站点长期积累的订单可能有几千条，全量返回会拖垮列表页）
     */
    public List<MarketPluginOrder> listMine(String instanceId, int limit) {
        if (!StringUtils.hasText(instanceId)) {
            return new ArrayList<>();
        }
        int max = limit > 0 ? Math.min(limit, 500) : 100;
        return orderMapper.selectList(new LambdaQueryWrapper<MarketPluginOrder>()
                .eq(MarketPluginOrder::getInstanceId, instanceId.trim())
                .orderByDesc(MarketPluginOrder::getId)
                .last("limit " + max));
    }

    /**
     * 纯读取订单（不做任何状态同步）。
     *
     * <p>详情接口必须用它：{@code /order/detail} 的语义是「把现在的状态原样返回」，
     * 若复用 {@link #syncAndSettle} 会被动触发结算（甚至发证），
     * 一个查询动作就产生了写副作用。
     */
    public MarketPluginOrder findOrder(String orderNo) {
        return requireOrder(orderNo);
    }

    /**
     * 订单详情（带站点归属校验）。
     *
     * @param instanceId 当前登录站点；非空时要求订单属于该站点
     */
    public MarketPluginOrder findOrder(String orderNo, String instanceId) {
        return requireOrder(orderNo, instanceId);
    }

    /** 订单视图：补出易读的状态文案与有效期 */
    public Map<String, Object> toView(MarketPluginOrder order) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", order.getOrderNo());
        view.put("pluginCode", order.getPluginCode());
        view.put("version", order.getVersion());
        view.put("licenseType", order.getLicenseType());
        view.put("validDays", order.getValidDays());
        view.put("amount", order.getAmount());
        view.put("status", order.getStatus());
        view.put("statusText", statusText(order.getStatus()));
        view.put("instanceId", order.getInstanceId());
        view.put("payMethod", order.getPayMethod());
        view.put("cashierNo", order.getCashierNo());
        view.put("payData", parseJson(order.getPayData()));
        view.put("licenseKey", order.getLicenseKey());
        view.put("planCode", order.getPlanCode());
        view.put("channel", order.getChannel());
        view.put("sandbox", Integer.valueOf(1).equals(order.getSandbox()));
        view.put("failReason", order.getFailReason());
        view.put("notifyCount", order.getNotifyCount());
        view.put("notifyTime", order.getNotifyTime() == null ? null : order.getNotifyTime().toString());
        view.put("paidTime", order.getPaidTime() == null ? null : order.getPaidTime().toString());
        view.put("expireTime", order.getExpireTime() == null ? null : order.getExpireTime().toString());
        view.put("createTime", order.getCreateTime() == null ? null : order.getCreateTime().toString());
        return view;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case MarketPluginOrder.STATUS_PENDING -> "待支付";
            case MarketPluginOrder.STATUS_PAID -> "已支付";
            case MarketPluginOrder.STATUS_CLOSED -> "已关闭";
            case MarketPluginOrder.STATUS_REFUNDED -> "已退款";
            case MarketPluginOrder.STATUS_PAY_FAILED -> "支付失败";
            default -> "未知";
        };
    }

    private MarketPlugin requirePublishedPlugin(String pluginCode) {
        if (!StringUtils.hasText(pluginCode)) {
            throw new IllegalArgumentException("插件编码不能为空");
        }
        MarketPlugin plugin = marketPluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, pluginCode.trim())
                .last("limit 1"));
        if (plugin == null) {
            throw new IllegalArgumentException("插件不存在: " + pluginCode);
        }
        if (!Integer.valueOf(STATUS_PUBLISHED).equals(plugin.getStatus())) {
            throw new IllegalStateException("插件未上架，无法购买: " + pluginCode);
        }
        return plugin;
    }

    /**
     * 解析授权类型——**以商品自身配置为准，不接受客户端降级**。
     *
     * <p>🔴 2026-10-27 修 P0：原实现「客户端传什么就用什么」，任何人对一个 PAID 商品
     * 传 {@code licenseType=TRIAL} 都能下出 0 元订单；配合 {@code trial_days=0} 的种子商品，
     * 结算时 validDays 为空 → 授权 {@code expireTime=null} → 永久有效。等于白拿正版。
     *
     * <p>现在的规则：
     * <ol>
     *   <li>商品 {@code license_type} 是唯一权威（缺省视为 PAID）；</li>
     *   <li>价格套餐若声明了 licenseType，必须与商品一致，否则拒绝；</li>
     *   <li>客户端传入的 licenseType 必须与商品一致，否则拒绝——不允许把 PAID 换成 TRIAL；</li>
     *   <li>TRIAL 必须配置 {@code trial_days > 0}，否则拒绝下单（避免「无限期试用」）。</li>
     * </ol>
     */
    private String resolveLicenseType(MarketPlugin plugin, String licenseType,
                                      PluginMarketPayProperties.PlanProperties plan) {
        String productType = plugin.getLicenseType() == null
                ? TYPE_PAID : plugin.getLicenseType().trim().toUpperCase(Locale.ROOT);
        if (!TYPE_PAID.equals(productType) && !TYPE_TRIAL.equals(productType)) {
            throw new IllegalArgumentException("仅支持购买 PAID / TRIAL 类型的插件: " + productType);
        }

        if (plan != null && StringUtils.hasText(plan.getLicenseType())) {
            String planType = plan.getLicenseType().trim().toUpperCase(Locale.ROOT);
            if (!planType.equals(productType)) {
                throw new IllegalStateException("价格套餐的授权类型（" + planType
                        + "）与商品授权类型（" + productType + "）不一致，拒绝下单: "
                        + plugin.getPluginCode());
            }
        }

        if (StringUtils.hasText(licenseType)) {
            String wanted = licenseType.trim().toUpperCase(Locale.ROOT);
            if (!wanted.equals(productType)) {
                throw new IllegalArgumentException("商品 " + plugin.getPluginCode()
                        + " 的授权类型是 " + productType + "，不接受按 " + wanted
                        + " 下单（付费商品不能改用试用类型规避付款）");
            }
        }

        if (TYPE_TRIAL.equals(productType)) {
            Integer trialDays = plugin.getTrialDays();
            if (trialDays == null || trialDays <= 0) {
                throw new IllegalStateException("试用商品未配置有效的 trial_days，"
                        + "不能让试用授权变成永久授权，拒绝下单: " + plugin.getPluginCode());
            }
        }

        return productType;
    }

    private void assertPayable(MarketPluginOrder order) {
        Integer status = order.getStatus();
        boolean payable = Integer.valueOf(MarketPluginOrder.STATUS_PENDING).equals(status)
                // 支付失败允许重新发起：用户只是换种方式再付一次，不该逼他重新下单
                || Integer.valueOf(MarketPluginOrder.STATUS_PAY_FAILED).equals(status);
        if (!payable) {
            throw new IllegalStateException("订单不是待支付状态，无法发起支付: " + order.getOrderNo()
                    + "（当前状态 " + statusText(status) + "）");
        }
        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("订单已超时，请重新下单: " + order.getOrderNo());
        }
    }

    /**
     * 支付方式是否在该渠道允许的名单内。
     *
     * <p>{@code pay-methods} 留空表示不限制（多数渠道如此）；一旦配了名单就必须命中，
     * 否则「渠道根本不支持这种支付方式」会在用户付到一半时才暴露。
     */
    private void assertPayMethodSupported(String channelCode, String payMethod) {
        if (!StringUtils.hasText(payMethod)) {
            return;
        }
        PluginMarketPayProperties.ChannelProperties cp = cfg().channel(channelCode);
        if (cp == null || !StringUtils.hasText(cp.getPayMethods())) {
            return;
        }
        String wanted = payMethod.trim().toUpperCase(Locale.ROOT);
        for (String one : cp.getPayMethods().split("[,;\\s]+")) {
            if (one.trim().equalsIgnoreCase(wanted)) {
                return;
            }
        }
        throw new IllegalArgumentException("渠道 " + channelCode + " 不支持支付方式 " + payMethod
                + "，当前允许：" + cp.getPayMethods());
    }

    private MarketPluginOrder requireOrder(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        MarketPluginOrder order = orderMapper.selectOne(new LambdaQueryWrapper<MarketPluginOrder>()
                .eq(MarketPluginOrder::getOrderNo, orderNo.trim())
                .last("limit 1"));
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderNo);
        }
        return order;
    }

    /**
     * 按订单号取单并校验站点归属。
     *
     * <p>🔴 2026-10-27 修 P1：pay / sync / detail / cancel 以前只凭 {@code orderNo} 操作，
     * 订单号一旦泄露（日志、分享链接、浏览器历史）就能读别人的订单详情（含支付参数、
     * 授权码）、替别人发起支付、甚至把别人的单取消掉。现在统一按登录站点收敛：
     * 订单有 instanceId 且与当前站点不一致 → 直接拒绝。
     *
     * @param orderNo    订单号
     * @param instanceId 当前登录站点（为空表示调用方是内部链路，不做归属校验）
     */
    private MarketPluginOrder requireOrder(String orderNo, String instanceId) {
        MarketPluginOrder order = requireOrder(orderNo);
        if (StringUtils.hasText(instanceId)
                && StringUtils.hasText(order.getInstanceId())
                && !instanceId.trim().equals(order.getInstanceId().trim())) {
            log.warn("拒绝跨站点访问插件订单: orderNo={} owner={} requester={}",
                    orderNo, order.getInstanceId(), instanceId);
            throw new IllegalArgumentException("订单不属于当前站点，无权访问: " + orderNo);
        }
        return order;
    }

    /**
     * 订单详情视图：剥离支付参数与授权码。
     *
     * <p>详情页只需要状态与金额，{@code payData}（渠道支付参数）与 {@code licenseKey}
     * 属于敏感字段，不该跟着一个只读接口出去。
     */
    public Map<String, Object> toDetailView(MarketPluginOrder order) {
        Map<String, Object> view = toView(order);
        view.remove("payData");
        view.remove("licenseKey");
        return view;
    }

    private String generateOrderNo() {
        return "PMO" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("支付参数序列化失败，退回空值: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static final String TYPE_PAID = "PAID";
    private static final String TYPE_TRIAL = "TRIAL";
}
