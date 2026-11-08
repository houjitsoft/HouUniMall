package com.houjiang.merchant.common.mapper.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallSettlement;
import com.houjiang.merchant.common.entity.mall.MallSettlementRecord;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;
import org.apache.ibatis.annotations.Mapper;

/**
 * 结算记录Mapper
 * @author Amos
 */
@Mapper
public interface MallSettlementRecordMapper extends IBaseMapper<MallSettlementRecord> {
    /**
     * TODO 补充方法说明
     * @param field TODO
     * @return TODO
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
             * @param QueryWrapper<MallSettlementRecord>( TODO
             */
            MallSettlementRecord snModel = this.selectOne(
                    /**
                     * TODO 补充方法说明
                     * @return TODO
                     */
                    new QueryWrapper<MallSettlementRecord>()
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
