package com.houjiang.merchant.service.coupon;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.coupon.Coupon;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.coupon.CouponSearchValidate;
import com.houjiang.merchant.vo.coupon.CouponDetailVo;
import com.houjiang.merchant.vo.coupon.CouponGooodsVo;
import com.houjiang.merchant.vo.coupon.CouponListedVo;

import java.util.List;

/**
 * 优惠券商品接口类
 * @author Amos
 */
public interface ICouponGoodsService {

    /**
     * 根据优惠券获取列表
     * @param id
     * @return
     */
    List<CouponGooodsVo> getListByCid(Integer id);
}
