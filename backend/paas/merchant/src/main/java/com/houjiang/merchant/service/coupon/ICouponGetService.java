package com.houjiang.merchant.service.coupon;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.validate.commons.IdsValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.coupon.CouponGetSearchValidate;
import com.houjiang.merchant.vo.coupon.CouponGetListedVo;
import com.houjiang.merchant.vo.coupon.CouponGooodsVo;
import com.houjiang.merchant.vo.coupon.CouponListedVo;

import java.util.List;

/**
 * 优惠券领取接口类
 */
public interface ICouponGetService {

    /**
     * 根据优惠券ID返回发放数量
     * @param couponId
     * @return
     */
    Integer getCountByCouponId(Integer couponId);

    /**
     * 根据优惠券ID返回发放数量
     * @param couponId
     * @return
     */
    Integer getTodayCountByUid(Integer userId);

    /**
     * @notes 获取优惠券领取列表
     * @return array
     * @author Amos
     */
    PageResult<CouponGetListedVo> list(PageValidate pageValidate, CouponGetSearchValidate searchValidate);

    /**
     * 作废
     * @param idsValidate
     */
    void invalid(IdsValidate idsValidate);
}
