package com.houjiang.merchant.service.coupon;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.coupon.Coupon;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.coupon.CouponCreateValidate;
import com.houjiang.merchant.validate.coupon.CouponRenameValidate;
import com.houjiang.merchant.validate.coupon.CouponSearchValidate;
import com.houjiang.merchant.validate.coupon.CouponSysSendValidate;
import com.houjiang.merchant.validate.coupon.CouponUpdateValidate;
import com.houjiang.merchant.vo.coupon.CouponDetailVo;
import com.houjiang.merchant.vo.coupon.CouponListedVo;
import com.houjiang.merchant.vo.coupon.CouponShortDetailVo;

import java.util.List;

/**
 * 营销优惠券服务接口类
 */
public interface ICouponService {

    /**
     * 优惠券选择
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MarketingCouponSelectVo>
     */
//    PageResult<MarketingCouponSelectVo> select(PageValidate pageValidate, MarketingCouponSearchValidate searchValidate);

    /**
     * 优惠券列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MarketingCouponListedVo>
     */
    PageResult<CouponListedVo> list(PageValidate pageValidate, CouponSearchValidate searchValidate);

    /**
     * @notes 获取优惠券详情
     * @param $params
     * @return array
     * @author Amos
     */
    CouponDetailVo detail(Integer id);

    /**
     * 根据ID返回
     * @param id
     * @return
     */
    Coupon getById(Integer id);

    /**
     * @notes 编辑优惠券
     * @author Amos
     */
    void edit(CouponUpdateValidate updateValidate);

    /**
     * 重命名优惠券 / 调整发放数量（进行中也可改名）
     */
    void rename(CouponRenameValidate renameValidate);

    /**
     * 关闭优惠券
     * @param id
     */
    void end(Integer id);

    /**
     * 开始优惠券
     * @param id
     */
    void start(Integer id);

    /**
     * @notes 删除优惠券
     * @param array $params
     * @return bool
     * @author Amos
     */
    void del(Integer id);

    /**
     * @notes 添加优惠券
     * @param array $params
     * @return bool
     * @author Amos
     */
    void add(CouponCreateValidate createValidate);

    /**
     * @notes 领取优惠券
     * @param array $user_ids
     * @param $coupon_id
     * @param $get_type
     * @param int $nums
     * @return bool
     * @author Amos
     */
    void userGetCoupon(CouponSysSendValidate sysSendValidate);

    /**
     * 获取短优惠券信息
      * @param couponId
     * @return
     */
    CouponShortDetailVo getShortById(Integer couponId);
}
