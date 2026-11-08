package com.houjiang.merchant.common.mapper.coupon;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.coupon.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 优惠券Mapper
 * @author Amos
 */
@Mapper
public interface CouponMapper extends IBaseMapper<Coupon> {


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_merchant_coupon WHERE status = 1 AND condition_type = 1 AND condition_order_money <= (money * 2)")
    List<Coupon> get50PerDiscountCoupon();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_merchant_coupon WHERE status = 1 AND condition_type = 1 AND condition_order_money <= (money * 2)")
    List<Coupon> getNoThresholdCoupon();

}
