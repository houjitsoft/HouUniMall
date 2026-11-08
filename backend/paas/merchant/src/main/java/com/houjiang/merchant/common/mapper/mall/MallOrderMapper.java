package com.houjiang.merchant.common.mapper.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallOrder;
import com.houjiang.merchant.common.entity.mall.MallOrderCreate;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单Mapper
 * @author Amos
 */
@Mapper
public interface MallOrderMapper extends IBaseMapper<MallOrder> {


    @Select({
            "<script>",
            "SELECT shop_id, sum(pay_money) as all_order_money, count(*) as all_order_num FROM hou_merchant_mall_order WHERE pay_is = 1 and settlement_is = 0 and shop_id IN",
            "<foreach item='item' index='index' collection='shopIds' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach> group by shop_id",
            "</script>"
    })
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<Map<String, Object>> countShopsWaitSettlementData(@Param("shopIds") List<Integer> shopIds);


    @Select({
            "<script>",
            "SELECT shop_id, sum(pay_money) as all_order_money, count(*) as all_order_num FROM hou_merchant_mall_order WHERE pay_is = 1 and settlement_is = 0 and shop_id = #{shopId}",
            "</script>"
    })
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Map<String, Object> countShopsWaitSettlementDataByShopId(@Param("shopId") Integer shopId);



    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT pay_money FROM hou_merchant_mall_order WHERE pay_is = 1 AND user_id = #{userId} AND delete_time = 0 ORDER BY pay_money DESC limit 1")
    BigDecimal getMaxPayMoney(@Param("userId") Integer userId);

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
             * @param QueryWrapper<MallOrder>( TODO
             */
            MallOrder snModel = this.selectOne(
                    /**
                     * TODO 补充方法说明
                     * @return TODO
                     */
                    new QueryWrapper<MallOrder>()
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
