package com.houjiang.order.mapper;

import com.houjiang.commons.vo.base.DealerApplyInfoVO;
import com.houjiang.commons.vo.shop.ShopUserMessageResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName OrderSupplierCreateMapper
 * @Description 供应商 创建订单 计算价格
 * @Author Amos
 * @Date 2023/7/14 15:54
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderSupplierCreateMapper {

    /**
     * 经销商品折扣查询
     * @param dealerShopId
     * @param supplierShopList
     * @return
     */
    List<DealerApplyInfoVO> queryDealerApplyListByShop(@Param("dealerShopId") String dealerShopId, @Param("supplierShopList") List<Integer> supplierShopList);

    /**
     * 查询限购商品次数
     * @param userId
     * @param goodsId
     * @param restrictionHours
     * @return
     */
    Integer queryPurchasingGoods(@Param("userId") String userId,@Param("goodsId")Integer goodsId,@Param("restrictionHours")Integer restrictionHours);

    /**
     * 查询待付款是否存在限购商品
     * @param userId
     * @param goodsId
     * @return
     */
    Integer queryPurchasingGoodsStatusNum(@Param("userId") String userId,@Param("goodsId") Integer goodsId);

    /**
     * 根据shopId查询用户id
     * @param shopId
     * @return
     */
    ShopUserMessageResultVO queryUserId(@Param("shopId") Integer shopId);
}
