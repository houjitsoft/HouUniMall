package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayConfigShopEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @Entity com.houjiang.payment.entity.PayConfigShop
 * @author Amos
 */
@Mapper
public interface PayConfigShopMapper extends BaseMapper<PayConfigShopEntity> {

    /**
     * 更新支付配置
     * @param shopId
     * @return
     */
    PayConfigShopEntity selectpaymentconfig(@Param("shopId") String shopId);
    /**
     * 更新支付配置
     * @param map
     * @return
     */
    Integer updatepaymentconfig(@Param("param") Map<String,Object> map);


    /**
     * 新增支付配置
     * @param map
     * @return
     */
    Integer insertpaymentconfig(@Param("param") Map<String,Object> map);
}




