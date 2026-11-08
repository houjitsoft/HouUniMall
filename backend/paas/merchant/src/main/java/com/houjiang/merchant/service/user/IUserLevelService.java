package com.houjiang.merchant.service.user;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.user.User;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.user.UserSearchValidate;
import com.houjiang.merchant.validate.user.UserUpdateValidate;
import com.houjiang.merchant.validate.user.UserWalletValidate;
import com.houjiang.merchant.vo.mall.MallGoodsDetailVo;
import com.houjiang.merchant.vo.user.DiscountUserListedVo;
import com.houjiang.merchant.vo.user.UserVo;

import java.util.List;

/**
 * 用户等级服务接口类
 */
public interface IUserLevelService {

    /**
     * @notes
     * 优惠会员信息
     * @param $goods
     * @return array
     * @author Amos
     */
    List<DiscountUserListedVo> listsAForGoodsDiscountDetail(MallGoodsDetailVo detail);
}
