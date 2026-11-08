package com.houjiang.merchant.common.mapper.mall;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallOrderGoods;
import com.houjiang.merchant.common.vo.MallOrderAfterSaleGoodsListsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单商品Mapper
 * @author Amos
 */
@Mapper
public interface MallOrderGoodsMapper extends IBaseMapper<MallOrderGoods> {
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("select `og`.`id`,og.id as order_goods_id,`og`.`order_id`,`og`.`user_id`,`og`.`goods_name`,`og`.`goods_image`,`og`.`goods_sku_value`,`og`.`goods_price`,`og`.`goods_num`,`og`.`money`,`og`.`goods_money`,`og`.`express_money`,`og`.`need_pay_money`,`og`.`pay_money`,o.status as order_status FROM hou_merchant_mall_order_goods og INNER JOIN hou_merchant_mall_order o ON og.order_id = o.id WHERE (og.after_sale = 0 AND o.status = 2 AND o.user_id = #{userId}) AND og.delete_time = 0 UNION ALL (SELECT `og`.`id`, og.id    as order_goods_id, `og`.`order_id`, `og`.`user_id`, `og`.`goods_name`, `og`.`goods_image`,`og`.`goods_sku_value`, `og`.`goods_price`, `og`.`goods_num`, `og`.`money`, `og`.`goods_money`,`og`.`express_money`, `og`.`need_pay_money`, `og`.`pay_money`, o.status as order_status FROM `hou_merchant_mall_order_goods` `og` INNER JOIN `hou_merchant_mall_order` `o` ON `og`.`order_id` = `o`.`id` WHERE (`og`.`after_sale` = 0 AND `o`.`status` = 3 AND `o`.`express_confirm_time` >= #{expressConfirmTime} AND `o`.`user_id` = #{userId}) AND `og`.`delete_time` = '0') limit #{offset} , #{limit}")
    List<MallOrderAfterSaleGoodsListsVo> selectMallOrderAfterSaleGoodsLists(@Param("userId")Integer userId, @Param("expressConfirmTime") Integer expressConfirmTime, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("select count(og.id) as count FROM hou_merchant_mall_order_goods og INNER JOIN hou_merchant_mall_order o ON og.order_id = o.id WHERE (og.after_sale = 0 AND o.status = 2 AND o.user_id = #{userId}) AND og.delete_time = 0 UNION ALL (SELECT count(og.id) as count FROM `hou_merchant_mall_order_goods` `og` INNER JOIN `hou_merchant_mall_order` `o` ON `og`.`order_id` = `o`.`id` WHERE (`og`.`after_sale` = 0 AND `o`.`status` = 3 AND `o`.`express_confirm_time` >= #{expressConfirmTime} AND `o`.`user_id` = #{userId}) AND `og`.`delete_time` = '0')")
    List<Long> countMallOrderAfterSaleGoods(@Param("userId")Integer userId, @Param("expressConfirmTime") Integer expressConfirmTime);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("select `og`.`id`,og.id as order_goods_id,`og`.`order_id`,`og`.`user_id`,`og`.`goods_name`,`og`.`goods_image`,`og`.`goods_sku_value`,`og`.`goods_price`,`og`.`goods_num`,`og`.`money`,`og`.`goods_money`,`og`.`express_money`,`og`.`need_pay_money`,`og`.`pay_money`,o.status as order_status FROM hou_merchant_mall_order_goods og INNER JOIN hou_merchant_mall_order o ON og.order_id = o.id WHERE (og.after_sale = 0 AND o.status = 2 AND o.user_id = #{userId}) AND og.delete_time = 0 limit #{offset} , #{limit}")
    List<MallOrderAfterSaleGoodsListsVo> selectMallOrderAfterSaleGoodsListsNoAfterSale(@Param("userId")Integer userId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("select count(og.id) as count FROM hou_merchant_mall_order_goods og INNER JOIN hou_merchant_mall_order o ON og.order_id = o.id WHERE (og.after_sale = 0 AND o.status = 2 AND o.user_id = #{userId}) AND og.delete_time = 0")
    List<Long> countMallOrderAfterSaleGoodsNoAfterSale(@Param("userId")Integer userId);
}
