package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.config.market.PluginMarketPayProperties;
import com.houjiang.mall.config.market.PluginPayConfigStore;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.market.MarketPluginOrder;
import com.houjiang.mall.mapper.market.MarketPluginOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 安装 / 卸载两端的商业侧清算：把「钱 — 单 — 授权 — 凭证」这条链在生命周期节点上收口。
 *
 * <p><b>为什么单独成类</b>：安装编排在 {@code PluginMarketService}、卸载编排在
 * {@code PluginCenterService}，两边都要碰授权与订单，但谁都不该互相依赖对方的事务边界。
 * 清算动作放在这里，两端只调一个方法。
 *
 * <p><b>安装侧</b>（{@link #onInstalled}）：核对「确实付过钱」+ 拦截沙箱授权流入生产。
 * <p><b>卸载侧</b>（{@link #onUninstalled}）：授权回收（释放实例占用）+ 订单支付凭证与回调流水清理，
 * 但<b>保留订单主记录</b>——它是财务凭据，删了无法对账。
 *
 * @author Amos
 */
@Slf4j
@Service
public class PluginLifecycleSettlementService {

    private final MarketPluginOrderMapper orderMapper;
    private final PluginLicenseService licenseService;
    private final PluginOrderService orderService;
    private final PluginPayConfigStore payStore;

    @Autowired
    public PluginLifecycleSettlementService(MarketPluginOrderMapper orderMapper,
                                            PluginLicenseService licenseService,
                                            PluginOrderService orderService,
                                            PluginPayConfigStore payStore) {
        this.orderMapper = orderMapper;
        this.licenseService = licenseService;
        this.orderService = orderService;
        this.payStore = payStore;
    }

    /** 当前生效配置快照（DB 覆盖后的最终值），每次重新取以支持热更新。 */
    private PluginMarketPayProperties cfg() {
        return payStore.current();
    }

    /**
     * 安装成功后的商业侧收尾。
     *
     * <p>授权门控（有没有买）已在 {@code PluginMarketService#installFromMarket} 由
     * {@code assertLicensed} 拦过，这里补的是<b>买的这一笔是否干净</b>：
     * <ul>
     *   <li>订单必须处于已支付（或退款前的已支付）——防止「订单被手工改成已支付」绕过支付；</li>
     *   <li>沙箱（模拟支付）订单换来的授权，在生产环境（{@code sandbox=false}）不允许安装。</li>
     * </ul>
     *
     * <p><b>失败即抛</b>：安装编排整体回滚，插件不会以「已安装」状态留下。
     *
     * @param pluginCode 插件编码
     * @param instanceId 站点实例（为空时 fail-closed 直接抛错，不做跨站点判断）
     */
    @Transactional(rollbackFor = Exception.class)
    public void onInstalled(String pluginCode, String instanceId) {
        if (!StringUtils.hasText(pluginCode)) {
            return;
        }
        if (!StringUtils.hasText(instanceId)) {
            throw new IllegalStateException(
                    "安装付费插件必须携带站点实例标识，无法核对购买订单: " + pluginCode);
        }
        MarketPluginOrder paid = latestPaidOrder(pluginCode, instanceId.trim());
        if (paid == null) {
            // 线下发证 / 平台自营插件没有在线订单属正常，不阻断安装，只留痕
            log.info("安装前未找到在线已支付订单（可能为线下发证或平台自营）: plugin={} instance={}",
                    pluginCode, instanceId);
            return;
        }
        if (!cfg().isSandbox() && Integer.valueOf(1).equals(paid.getSandbox())) {
            throw new IllegalStateException(
                    "该授权来自沙箱（模拟支付）订单，不允许在生产环境安装: order=" + paid.getOrderNo()
                            + "。请在生产环境重新购买，或关闭沙箱配置后重试");
        }
        log.info("安装前商业校验通过: plugin={} instance={} order={} amount={} channel={}",
                pluginCode, instanceId.trim(), paid.getOrderNo(), paid.getAmount(), paid.getChannel());
    }

    /**
     * 卸载后的清算：授权回收 + 凭证清理。
     *
     * <p><b>授权回收为什么不吊销</b>：卸载≠退款。吊销会把「租户花钱买的授权」作废，
     * 重装就得重新买；这里只做 {@code unbind}（释放实例占用、状态落 UNBOUND），
     * 授权本身仍有效，重装时可重新激活。首次安装时间保留，试用期不会被重置。
     *
     * <p><b>清算失败不阻断卸载</b>：卸载是租户的显式意图，因清算异常导致「卸不掉」
     * 比「清理不干净」严重得多；失败一律记 error 日志等人工补偿。
     *
     * @param pluginCode 插件编码
     * @param instanceId 站点实例（必填，fail-closed）
     * @return 清算报告
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> onUninstalled(String pluginCode, String instanceId) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("pluginCode", pluginCode);
        report.put("instanceId", instanceId);
        if (!StringUtils.hasText(pluginCode) || !StringUtils.hasText(instanceId)) {
            // fail-closed：没有站点身份就不动任何授权，宁可留着也不能解绑别人的
            report.put("licenseReclaimed", false);
            report.put("reason", "缺少站点实例标识，跳过授权回收");
            log.warn("卸载清算跳过：缺少站点实例标识 plugin={}", pluginCode);
            return report;
        }
        String code = pluginCode.trim();
        String instance = instanceId.trim();

        // 1. 授权回收
        boolean reclaimed = false;
        try {
            MarketPluginLicense license = licenseService.findAny(code, instance);
            if (license != null && Integer.valueOf(1).equals(license.getStatus())
                    && StringUtils.hasText(license.getLicenseKey())) {
                licenseService.unbind(license.getLicenseKey(), instance);
                reclaimed = true;
                report.put("licenseKey", license.getLicenseKey());
            }
        } catch (RuntimeException e) {
            log.error("卸载后授权回收失败（需人工补偿）: plugin={} instance={} err={}",
                    code, instance, e.getMessage());
            report.put("licenseError", e.getMessage());
        }
        report.put("licenseReclaimed", reclaimed);

        // 2. 支付凭证与回调流水清理（订单主记录保留，作为财务凭据）
        int scrubbed = 0;
        try {
            scrubbed = orderService.scrubCredentials(code, instance);
        } catch (RuntimeException e) {
            log.error("卸载后支付凭证清理失败（需人工补偿）: plugin={} instance={} err={}",
                    code, instance, e.getMessage());
            report.put("scrubError", e.getMessage());
        }
        report.put("ordersScrubbed", scrubbed);

        log.info("卸载清算完成: plugin={} instance={} licenseReclaimed={} ordersScrubbed={}",
                code, instance, reclaimed, scrubbed);
        return report;
    }

    /** 该站点该插件最近一笔已支付订单。 */
    private MarketPluginOrder latestPaidOrder(String pluginCode, String instanceId) {
        List<MarketPluginOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<MarketPluginOrder>()
                .eq(MarketPluginOrder::getPluginCode, pluginCode)
                .eq(MarketPluginOrder::getInstanceId, instanceId)
                .eq(MarketPluginOrder::getStatus, MarketPluginOrder.STATUS_PAID)
                .orderByDesc(MarketPluginOrder::getId)
                .last("limit 1"));
        return orders == null || orders.isEmpty() ? null : orders.get(0);
    }
}
