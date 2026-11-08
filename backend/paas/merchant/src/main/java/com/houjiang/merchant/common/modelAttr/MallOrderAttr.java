package com.houjiang.merchant.common.modelAttr;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallOrderAfterSale;
import com.houjiang.merchant.common.enums.OrderEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.mapper.ConfigMapper;
import com.houjiang.merchant.common.mapper.mall.MallOrderAfterSaleMapper;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.enums.OrderAfterSaleEnum;
import java.math.BigDecimal;

public class MallOrderAttr {

    /**
     * @notes can_apply_refund 可申请售后退款
     * @author Amos
     * @DateTime 2024-12-27T11:25:19+0800
     */
    public static Integer getCanApplyRefundAttr(Integer status, Long expressConfirmTime) {
        int btn = 0;
        if (status.equals(OrderEnum.STATUS_FINISH.getCode())) {
            Integer afterSale = Integer.valueOf(ConfigUtils.get("mall-order-transaction", "after_sale", "0"));
            if (afterSale.equals(YesNoEnum.YES.getCode()) == false) {
                return btn;
            }
            BigDecimal configConfirmTime = new BigDecimal(ConfigUtils.get("mall-order-transaction", "after_sale_day", "0"));
            configConfirmTime = configConfirmTime.multiply(new BigDecimal(24 * 3600));

            BigDecimal currTime = new BigDecimal(System.currentTimeMillis() / 1000);
            BigDecimal expressConfirmTimeD = new BigDecimal(expressConfirmTime);
            if (expressConfirmTimeD.compareTo(currTime.subtract(configConfirmTime)) >= 0) {
                return YesNoEnum.YES.getCode();
            } else {
                return YesNoEnum.NO.getCode();
            }
        }

        // 待收货 可以直接售后
        if(status.equals(OrderEnum.STATUS_WAIT_RECEIVE.getCode())) {
            btn = 1;
        }
        // 待发货 使用 can_apply_refund_all 字段
        if(status.equals(OrderEnum.STATUS_WAIT_SEND.getCode())) {
            btn = 0;
        }
        return btn;
    }

    /**
     * @notes can_apply_refund_all 可整单退款 但可能有审核
     * @return int
     * @author Amos
     * @datetime 2023-05-05 11:58:20
     */
    public static Integer getCanApplyRefundAllAttr(Integer status) {
       if (status.equals(OrderEnum.STATUS_WAIT_SEND.getCode()) == true) {
           return 1;
       } else {
           return 0;
       }
    }

    public static Integer getAutoCancelRemainTimeAttr(Integer isAutoCancel, Long createTime) {
        if (isAutoCancel.equals(YesNoEnum.YES.getCode()) == false) {
            return null;
        } else {
            BigDecimal configTime = new BigDecimal(ConfigUtils.get("mall-order-transaction", "auto_cancel_minute", "0"));
            BigDecimal cancelTime =  BigDecimal.valueOf(createTime).add(configTime.multiply(new BigDecimal(60)) );

            BigDecimal remainTime = cancelTime.subtract(BigDecimal.valueOf(System.currentTimeMillis() / 1000));
            return remainTime.compareTo(BigDecimal.ZERO) > 0 ? remainTime.intValue() : 0;
        }
    }

    /**
     * @notes 售后信息
      * @return
     */
    public static JSONObject getAfterSaleDetailAttr(Integer orderGoodsId) {
        JSONObject ret = new JSONObject();
        MallOrderAfterSaleMapper model = SpringUtils.getBean(MallOrderAfterSaleMapper.class);
        QueryWrapper<MallOrderAfterSale> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_time", 0);
        //queryWrapper.in("status", OrderAfterSaleEnum.getStatusApplyArr());

        // 使用 and 条件组合，检查 order_goods_id 或 order_goods_ids 包含指定ID
        queryWrapper.and(wrapper ->
                ((QueryWrapper<MallOrderAfterSale>) wrapper)
                        .eq("order_goods_id", orderGoodsId)
                        .or()
                        .apply("JSON_CONTAINS(order_goods_ids, JSON_ARRAY({0}))", orderGoodsId)
        );

        queryWrapper.last("LIMIT 1");

        // 执行查询
        MallOrderAfterSale afterSale = model.selectOne(queryWrapper);
        if (afterSale != null) {
            ret.put("id", afterSale.getId());
            ret.put("status", afterSale.getStatus());
            ret.put("status_text", OrderAfterSaleEnum.getStatusTextArr(afterSale.getStatus()));
            return ret;
        } else {
            return null;
        }
    }

}
