package com.houjiang.merchant.common.logic;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.enums.OrderSettingEnum;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.StringUtils;

import java.util.Map;

/**
 * @author Amos
 */
public class CommonConfigLogic {
    public static JSONObject mallOrderSettlementGet() {
        JSONObject ret = new JSONObject();
        Map<String, String> map = ConfigUtils.get("mall-order-settlement");
        // 结算周期 默认为每天
        ret.put("settle_cycle", StringUtils.isNull(map.get("settle_cycle")) ? OrderSettingEnum.SETTLEMENT_CYCLE_DAY : Integer.valueOf(map.get("settle_cycle")));
        // 结算周期 每周第几天
        ret.put("settle_cycle_week_day", StringUtils.isNull(map.get("settle_cycle_week_day")) ? 1 : Integer.valueOf(map.get("settle_cycle_week_day")));
        // 结算周期 每周第几天
        ret.put("settle_cycle_month_day", StringUtils.isNull(map.get("settle_cycle_month_day")) ? 1 : Integer.valueOf(map.get("settle_cycle_month_day")));
        return ret;
    }
}
