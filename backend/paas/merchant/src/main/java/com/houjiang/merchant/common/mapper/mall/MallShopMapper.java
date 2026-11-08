package com.houjiang.merchant.common.mapper.mall;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallShop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商家店铺Mapper
 * @author Amos
 */
@Mapper
public interface MallShopMapper extends IBaseMapper<MallShop> {

    @Select({"<script>",
            "SELECT id " +
                    "FROM hou_merchant_mall_shop " +
                    "WHERE name like concat('%', #{name}, '%')" +
            "</script>"})
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<Integer> getShopIdsByName(@Param("name") String name);

    /**
     * 仅查库表已有列，避免实体多出的 owner_user_id/banner 等缺列导致 selectById 失败。
     */
    @Select("SELECT id, create_time, update_time, delete_time, name, logo, `desc` AS descStr, "
            + "shop_category_id, type, shop_status, business_status, contact, mobile, "
            + "province_id, city_id, district_id, address_info, lng, lat, "
            + "return_contact, return_mobile, return_province_id, return_city_id, return_district_id, return_address_info, "
            + "home_recommend, goods_audit, business_license, other_license, service_ratio, score, star, "
            + "visit_num, money, sale_money, sale_orders, sale_online_goods, gather_money "
            + "FROM hou_merchant_mall_shop WHERE id = #{id} LIMIT 1")
    MallShop selectLiteById(@Param("id") Integer id);

}
