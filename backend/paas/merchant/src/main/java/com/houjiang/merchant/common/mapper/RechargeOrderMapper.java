package com.houjiang.merchant.common.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.RechargeOrder;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;

/**
 * 充值订单Mapper
 */
public interface RechargeOrderMapper extends IBaseMapper<RechargeOrder> {

    /**
     * 生成唯一单号
     *
     * @author Amos
     * @param field 字段名
     * @return String
     */
    default String randMakeOrderSn(String field) {
        /**
         * TODO 补充方法说明
         */
        String date = TimeUtils.timestampToDate(System.currentTimeMillis()/1000, "yyyyMMddHHmmss");
        String sn;
        /**
         * TODO 补充方法说明
         */
        while (true) {
            /**
             * TODO 补充方法说明
             */
            sn = date + ToolUtils.randomInt(4);
            /**
             * TODO 补充方法说明
             * @param QueryWrapper<RechargeOrder>( TODO
             */
            RechargeOrder snModel = this.selectOne(
                    /**
                     * TODO 补充方法说明
                     * @return TODO
                     */
                    new QueryWrapper<RechargeOrder>()
                            .select("id")
                            .eq(field, sn)
                            .last("limit 1"));
            /**
             * TODO 补充方法说明
             * @param null TODO
             */
            if (snModel == null) {
                break;
            }
        }
        return sn;
    }

}
