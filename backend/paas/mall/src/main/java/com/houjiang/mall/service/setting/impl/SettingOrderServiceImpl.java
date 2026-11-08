package com.houjiang.mall.service.setting.impl;

import com.houjiang.mall.service.setting.CommonConfigService;
import com.houjiang.mall.service.setting.SettingOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 订单设置 Service 实现。对应 like-admin SettingOrderServiceImpl。
 * 使用 hou_merchant_config 持久化，分两个分组：
 *   - mall-order-transaction：交易设置（10 字段）
 *   - mall-order-settlement：结算设置（3 字段）
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingOrderServiceImpl implements SettingOrderService {

    /** 交易设置分组 */
    private static final String TYPE_TRANSACTION = "mall-order-transaction";
    /** 结算设置分组 */
    private static final String TYPE_SETTLEMENT = "mall-order-settlement";

    private final CommonConfigService commonConfigService;

    @Override
    public Map<String, Object> getTransaction() {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("autoCancel", parseInt(commonConfigService.getOrDefault(TYPE_TRANSACTION, "auto_cancel", "1"), 1));
        vo.put("autoCancelMinute", parseDecimal(commonConfigService.getOrDefault(TYPE_TRANSACTION, "auto_cancel_minute", "30")));
        vo.put("waitSendRefund", parseInt(commonConfigService.getOrDefault(TYPE_TRANSACTION, "wait_send_refund", "1"), 1));
        vo.put("waitSendCanCancelMinute", parseDecimal(commonConfigService.getOrDefault(TYPE_TRANSACTION, "wait_send_can_cancel_minute", "1")));
        vo.put("autoFinal", parseInt(commonConfigService.getOrDefault(TYPE_TRANSACTION, "auto_final", "1"), 1));
        vo.put("autoFinalDay", parseDecimal(commonConfigService.getOrDefault(TYPE_TRANSACTION, "auto_final_day", "30")));
        vo.put("afterSale", parseInt(commonConfigService.getOrDefault(TYPE_TRANSACTION, "after_sale", "0"), 0));
        vo.put("afterSaleDay", parseDecimal(commonConfigService.getOrDefault(TYPE_TRANSACTION, "after_sale_day", "0")));
        vo.put("orderCreateDecSkuNum", parseInt(commonConfigService.getOrDefault(TYPE_TRANSACTION, "order_create_dec_sku_num", "1"), 1));
        vo.put("orderCancelReturnSkuNum", parseInt(commonConfigService.getOrDefault(TYPE_TRANSACTION, "order_cancel_return_sku_num", "0"), 0));
        return vo;
    }

    @Override
    public void saveTransaction(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 兼容驼峰与下划线两种 key（前端目前用下划线）
        setIfPresent(params, TYPE_TRANSACTION, "auto_cancel", "autoCancel");
        setIfPresent(params, TYPE_TRANSACTION, "auto_cancel_minute", "autoCancelMinute");
        setIfPresent(params, TYPE_TRANSACTION, "wait_send_refund", "waitSendRefund");
        setIfPresent(params, TYPE_TRANSACTION, "wait_send_can_cancel_minute", "waitSendCanCancelMinute");
        setIfPresent(params, TYPE_TRANSACTION, "auto_final", "autoFinal");
        setIfPresent(params, TYPE_TRANSACTION, "auto_final_day", "autoFinalDay");
        setIfPresent(params, TYPE_TRANSACTION, "after_sale", "afterSale");
        setIfPresent(params, TYPE_TRANSACTION, "after_sale_day", "afterSaleDay");
        setIfPresent(params, TYPE_TRANSACTION, "order_create_dec_sku_num", "orderCreateDecSkuNum");
        setIfPresent(params, TYPE_TRANSACTION, "order_cancel_return_sku_num", "orderCancelReturnSkuNum");
    }

    @Override
    public Map<String, Object> getSettlement() {
        Map<String, Object> vo = new LinkedHashMap<>();

        // settlement 子对象（三档）
        Map<String, Object> settlement = new LinkedHashMap<>();
        settlement.put("settle_cycle", parseInt(commonConfigService.getOrDefault(TYPE_SETTLEMENT, "settle_cycle", "0"), 0));
        settlement.put("settle_cycle_week_day", parseInt(commonConfigService.getOrDefault(TYPE_SETTLEMENT, "settle_cycle_week_day", "1"), 1));
        settlement.put("settle_cycle_month_day", parseInt(commonConfigService.getOrDefault(TYPE_SETTLEMENT, "settle_cycle_month_day", "1"), 1));
        vo.put("settlement", settlement);

        // 周枚举：1..7 → 星期一..星期日
        Map<Integer, String> enumWeek = new LinkedHashMap<>();
        enumWeek.put(1, "星期一");
        enumWeek.put(2, "星期二");
        enumWeek.put(3, "星期三");
        enumWeek.put(4, "星期四");
        enumWeek.put(5, "星期五");
        enumWeek.put(6, "星期六");
        enumWeek.put(7, "星期日");
        vo.put("enumWeek", enumWeek);

        // 周期枚举：日结/周结/月结
        List<String> enumCycle = Arrays.asList("日结", "周结", "月结");
        vo.put("enumCycle", enumCycle);

        // 月枚举：1..28
        int[] enumMonth = new int[28];
        for (int i = 0; i < 28; i++) {
            enumMonth[i] = i + 1;
        }
        vo.put("enumMonth", enumMonth);

        return vo;
    }

    @Override
    public void saveSettlement(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        setIfPresent(params, TYPE_SETTLEMENT, "settle_cycle", "settleCycle");
        setIfPresent(params, TYPE_SETTLEMENT, "settle_cycle_week_day", "settleCycleWeekDay");
        setIfPresent(params, TYPE_SETTLEMENT, "settle_cycle_month_day", "settleCycleMonthDay");
    }

    /**
     * 从入参 params 中按下划线/驼峰双 key 兜底取值，存在则写入 hou_merchant_config。
     */
    private void setIfPresent(Map<String, Object> params, String type, String snake, String camel) {
        Object val = params.get(snake);
        if (val == null) {
            val = params.get(camel);
        }
        if (val == null) {
            return;
        }
        commonConfigService.set(type, snake, val.toString());
    }

    private int parseInt(String s, int defaults) {
        if (s == null || s.isEmpty()) {
            return defaults;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaults;
        }
    }

    private BigDecimal parseDecimal(String s) {
        if (s == null || s.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
