package com.houjiang.merchant.common.mapper.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.log.UserAccountLog;
import com.houjiang.merchant.common.entity.mall.MallOrderAfterSale;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单售后Mapper
 * @author Amos
 */
@Mapper
public interface MallOrderAfterSaleMapper extends IBaseMapper<MallOrderAfterSale> {
    /**
     * 生成唯一流水号
     *
     * @author Amos
     * @return String
     */
    default String randMakeOrderSn() {
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
             * @param QueryWrapper<MallOrderAfterSale>( TODO
             */
            MallOrderAfterSale snModel = this.selectOne(
                    /**
                     * TODO 补充方法说明
                     * @return TODO
                     */
                    new QueryWrapper<MallOrderAfterSale>()
                            .select("id")
                            .eq("sn", sn)
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
