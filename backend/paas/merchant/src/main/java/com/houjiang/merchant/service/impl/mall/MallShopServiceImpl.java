package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallGoods;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.mall.MallShopCategory;
import com.houjiang.merchant.common.enums.AuditEnum;
import com.houjiang.merchant.common.enums.GoodsEnum;
import com.houjiang.merchant.common.enums.ShopEum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallGoodsMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.houjiang.merchant.common.util.*;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.*;
import com.houjiang.merchant.validate.mall.MallShopUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopDetailVo;
import com.houjiang.merchant.vo.mall.MallShopSimplifyDetailVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

/**
 * 商家账号实现类
 * @author Amos
 */
@Service
public class MallShopServiceImpl implements IMallShopService {

    @Resource
    IMallShopCategoryService iMallShopCategoryService;
    @Resource
    MallShopMapper mallShopMapper;
    @Resource
    MallGoodsMapper mallGoodsMapper;

    @Override
    public MallShopDetailVo detail(Integer id) {
        MallShop shop = this.getById(id);
        if (StringUtils.isNull(shop)) {
            return null;
        } else {
            MallShopCategory mallShopCategory = iMallShopCategoryService.getById(shop.getShopCategoryId());
            MallShopDetailVo vo = new MallShopDetailVo();
            BeanUtils.copyProperties(shop, vo);
            vo.setBusinessLicense(UrlUtils.toShopAbsoluteUrl(shop.getBusinessLicense()));
            vo.setLogo(UrlUtils.toShopAbsoluteUrl(shop.getLogo()));
            vo.setCreateTime(TimeUtils.timestampToDate(shop.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(shop.getUpdateTime()));
            JSONArray otherLicenseArr = new JSONArray();
            for (Object item : JSONArray.parseArray(shop.getOtherLicense())) {
                otherLicenseArr.add(UrlUtils.toShopAbsoluteUrl(item.toString()));
            }
            vo.setOtherLicense(otherLicenseArr);
            vo.setMobile(JSONArray.parseArray(shop.getMobile()));
            vo.setShopCategory(StringUtils.isNull(mallShopCategory) ? new JSONObject() : new JSONObject() {{
                put("id", mallShopCategory.getId());
                put("name", mallShopCategory.getName());
            }});
            vo.setTypeText(ShopEum.getTypeDesc(shop.getType()));
            vo.setSaleMoney(new BigDecimal(shop.getSaleMoney()).divide(new BigDecimal(100)));

            JSONObject config = ConfigUtils.getShopWithdrawConfig();

            vo.setWithdrawRatio(config.getBigDecimal("sw_service_ratio"));
            return vo;
        }
    }

    @Override
    public MallShopSimplifyDetailVo simplifyDetail(Integer id) {
        MallShopDetailVo shop = this.detail(id);
        if (StringUtils.isNull(shop)) {
            return null;
        } else {
            MallShopSimplifyDetailVo vo = new MallShopSimplifyDetailVo();
            BeanUtils.copyProperties(shop, vo);
            return vo;
        }
    }

    @Override
    public MallShop getById(Integer id) {
        return mallShopMapper.selectOne(new QueryWrapper<MallShop>().eq("id", id).eq("delete_time", 0));
    }

    @Override
    public void updateOnlineGoods(Integer shopId) {
        Long goodNums = mallGoodsMapper.selectCount(new QueryWrapper<MallGoods>().eq("shop_id", shopId).eq("status", GoodsEnum.GOODS_STATUS_UP.getCode()).eq("audit_status", AuditEnum.PASS.getCode()).eq("delete_time", 0));

        MallShop shop = new MallShop();
        shop.setId(shopId);
        shop.setSaleOnlineGoods(goodNums.intValue());
        mallShopMapper.updateById(shop);
    }

    @Override
    public Boolean decMoney(Integer shopId, BigDecimal money, Boolean checkMoney) {
        MallShop mallShop = this.getById(shopId);
        if (checkMoney.equals(true)) {
            if (mallShop.getMoney().compareTo(money) < 0) {
                return false;
            }
        }
        mallShop.setMoney(mallShop.getMoney().subtract(money));
        mallShop.setUpdateTime(System.currentTimeMillis() / 1000);
        return mallShopMapper.updateById(mallShop) == 1 ? true : false;
    }

    @Override
    public String getNameById(Integer shopId) {
        MallShop shop = this.getById(shopId);
        return StringUtils.isNull(shop) ? "" : shop.getName();
    }

    @Override
    public void edit(MallShopUpdateValidate updateValidate) {
        MallShop model = mallShopMapper.selectOne(
                new QueryWrapper<MallShop>()
                        .eq("id",  updateValidate.getId())
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在!");

        if (model.getId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能编辑其他店铺的岗位信息");
        }

        model.setId(updateValidate.getId());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setName(updateValidate.getName());
        model.setLogo(UrlUtils.toShopRelativeUrl(updateValidate.getLogo()));
      //  model.setShopCategoryId(updateValidate.getShopCategoryId());
        model.setShopStatus(updateValidate.getShopStatus());
        model.setBusinessStatus(updateValidate.getBusinessStatus());
        model.setContact(updateValidate.getContact());
        model.setMobile(updateValidate.getMobile().toJSONString());
        model.setProvinceId(updateValidate.getProvinceId());
        model.setCityId(updateValidate.getCityId());
        model.setDistrictId(updateValidate.getDistrictId());
        model.setAddressInfo(updateValidate.getAddressInfo());
        model.setLng(updateValidate.getLng());
        model.setLat(updateValidate.getLat());
        //model.setHomeRecommend(updateValidate.getHomeRecommend());
        model.setBusinessLicense(UrlUtils.toShopRelativeUrl(updateValidate.getBusinessLicense()));
        if (updateValidate.getOtherLicense().size() > 0) {
            JSONArray otherLicenseArr = new JSONArray();
            for (Object item : updateValidate.getOtherLicense()) {
                otherLicenseArr.add(UrlUtils.toShopRelativeUrl(item.toString()));
            }
            model.setOtherLicense(otherLicenseArr.toJSONString());
        } else {
            model.setOtherLicense("[]");
        }
        model.setDescStr(updateValidate.getDesc());
        model.setReturnContact(updateValidate.getReturnContact());
        model.setReturnAddressInfo(updateValidate.getReturnAddressInfo());
        model.setReturnMobile(updateValidate.getReturnMobile());
        //退款信息
        model.setReturnProvinceId(updateValidate.getReturnProvinceId());
        model.setReturnCityId(updateValidate.getReturnCityId());
        model.setReturnDistrictId(updateValidate.getReturnDistrictId());

        mallShopMapper.updateById(model);
    }

    @Override
    public Boolean decGatherMoney(Integer shopId, Integer gatherPrice) {
        MallShop mallShop = this.getById(shopId);
        if (mallShop.getGatherMoney() < gatherPrice) {
            return false;
        } else {
            mallShop.setGatherMoney(mallShop.getGatherMoney() - gatherPrice);
            mallShop.setUpdateTime(System.currentTimeMillis() / 1000);
            mallShopMapper.updateById(mallShop);
            return true;
        }
    }
}
