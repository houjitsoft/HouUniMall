package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.order.entity.OrderDealerInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName OrderDealerInfoMapper
 * @Description  经销单 经销折扣信息
 * @Author Amos
 * @Date 2024-08-01 09:45:07
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderDealerInfoMapper extends BaseMapper<OrderDealerInfoEntity> {


    /**
     * 查询 店铺经销折扣信息
     */
    OrderDealerInfoEntity queryOrderGoodsDealerDiscount(@Param("orderId") String orderId, @Param("supplierGoodsId")String supplierGoodsId);
    /**
     * 查询 店铺经销折扣信息
     */
    OrderDealerInfoEntity queryOrderGoodsDealerDiscountBySubOrderNo(@Param("orderId") String orderId, @Param("supplierGoodsId")String supplierGoodsId);
}
