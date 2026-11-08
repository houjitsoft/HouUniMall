package com.houjiang.merchant.service.impl.user;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.merchant.common.validation.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallGoodsDiscount;
import com.houjiang.merchant.common.entity.user.User;
import com.houjiang.merchant.common.entity.user.UserLevel;
import com.houjiang.merchant.common.enums.ClientEnum;
import com.houjiang.merchant.common.enums.GoodsEnum;
import com.houjiang.merchant.common.enums.LogMoneyEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.log.UserAccountLogMapper;
import com.houjiang.merchant.common.mapper.user.UserLevelMapper;
import com.houjiang.merchant.common.mapper.user.UserMapper;
import com.houjiang.merchant.common.util.*;
import com.houjiang.merchant.service.mall.IMallGoodsDiscountService;
import com.houjiang.merchant.service.user.IUserLevelService;
import com.houjiang.merchant.service.user.IUserService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.user.UserSearchValidate;
import com.houjiang.merchant.validate.user.UserUpdateValidate;
import com.houjiang.merchant.validate.user.UserWalletValidate;
import com.houjiang.merchant.vo.mall.MallGoodsDetailVo;
import com.houjiang.merchant.vo.mall.MallGoodsSkuListedVo;
import com.houjiang.merchant.vo.mall.MallGoodsUserLevelDiscountVo;
import com.houjiang.merchant.vo.user.DiscountUserListedVo;
import com.houjiang.merchant.vo.user.UserLevelListedVo;
import com.houjiang.merchant.vo.user.UserListExportVo;
import com.houjiang.merchant.vo.user.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户等级服务实现类
 * @author Amos
 */
@Service
public class UserLevelServiceImpl implements IUserLevelService {
    @Resource
    IMallGoodsDiscountService iMallGoodsDiscountService;
    @Resource
    UserLevelMapper userLevelMapper;
    @Override
    public List<DiscountUserListedVo> listsAForGoodsDiscountDetail(MallGoodsDetailVo detail) {
        List<DiscountUserListedVo> ret = new ArrayList<>();
        List<UserLevelListedVo> lists = this.getList();

        if (lists.size() == 0) {
            return ret;
        }
        // 不参与 获取使用默认的比例 初始化默认信息
        List<MallGoodsUserLevelDiscountVo> discountList = new ArrayList<MallGoodsUserLevelDiscountVo>();
        if (StringUtils.isNotNull(detail.getId())) {
            for (UserLevelListedVo userLevel : lists) {
//                if (userLevel.getDiscountList().size() > 0) { //已经读出不需要再处理
//                    continue;
//                }
                List<MallGoodsUserLevelDiscountVo> goodsList = new ArrayList<>();
                for (MallGoodsSkuListedVo skuVo : detail.getSku()) {
                    MallGoodsUserLevelDiscountVo levelDiscountVo = new MallGoodsUserLevelDiscountVo();
                    levelDiscountVo.setGoodsId(skuVo.getGoodsId());
                    levelDiscountVo.setGoodsSkuId(skuVo.getId());
                    levelDiscountVo.setUserLevelId(userLevel.getId());
                    levelDiscountVo.setAloneDiscount(new BigDecimal(0));
                    levelDiscountVo.setAlonePrice(new BigDecimal(0));
                    levelDiscountVo.setSkuValueArr(skuVo.getSkuValueArr());
                    levelDiscountVo.setPrice(skuVo.getPrice());

                    MallGoodsDiscount discount = iMallGoodsDiscountService.getByGSLid(skuVo.getGoodsId(), skuVo.getId(), userLevel.getId());
                    if (StringUtils.isNotNull(discount)) {
                        levelDiscountVo.setAloneDiscount(discount.getAloneDiscount());
                        levelDiscountVo.setAlonePrice(discount.getAlonePrice());
                    }
                    goodsList.add(levelDiscountVo);
                }
                userLevel.setDiscountList(goodsList);
            }
        }

        for (UserLevelListedVo userLevel : lists) {
            DiscountUserListedVo vo = new DiscountUserListedVo();
            BeanUtils.copyProperties(userLevel, vo);
            for (MallGoodsUserLevelDiscountVo sku : vo.getDiscountList()) {
                //默认
                sku.setDiscountPrice(sku.getPrice());
                if (detail.getDiscountType().equals(GoodsEnum.DISCOUNT_TYPE_DEFAULT.getCode())) {
                    // 会员等级折扣
                    sku.setDiscountPrice(sku.getPrice().multiply(userLevel.getDiscountRatio().divide(new BigDecimal(10))));
                }
                if (detail.getDiscountType().equals(GoodsEnum.DISCOUNT_TYPE_ALONE_DISCOUNT.getCode())) {
                    // 单独折扣
                    sku.setDiscountPrice(sku.getPrice().multiply(sku.getAloneDiscount().divide(new BigDecimal(10))));
                }
                if (detail.getDiscountType().equals(GoodsEnum.DISCOUNT_TYPE_ALONE_PRICE.getCode())) {
                    sku.setDiscountPrice(sku.getAlonePrice());
                }
            }
            vo.setDiscountList(vo.getDiscountList());
            ret.add(vo);
        }
        return ret;
    }

    private List<UserLevelListedVo> getList() {
        List<UserLevelListedVo> ret = new ArrayList<>();
        List<UserLevel> list = userLevelMapper.selectList(new QueryWrapper<UserLevel>().eq("delete_time", 0));
        if (list.size() > 0) {
            for (UserLevel level : list) {
                UserLevelListedVo vo = new UserLevelListedVo();
                BeanUtils.copyProperties(level, vo);
                List<MallGoodsUserLevelDiscountVo> discountList = new ArrayList<>();
                vo.setDiscountList(discountList);
                ret.add(vo);
            }
        }
        return ret;
    }
}
