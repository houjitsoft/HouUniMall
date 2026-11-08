package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.houjiang.merchant.common.entity.mall.MallGoods;
import com.houjiang.merchant.common.entity.mall.MallGoodsSku;
import com.houjiang.merchant.common.entity.mall.MallOrder;
import com.houjiang.merchant.common.entity.mall.MallOrderGoods;
import com.houjiang.merchant.common.mapper.mall.MallGoodsMapper;
import com.houjiang.merchant.common.mapper.mall.MallGoodsSkuMapper;
import com.houjiang.merchant.common.mapper.mall.MallOrderGoodsMapper;
import com.houjiang.merchant.common.modelAttr.MallOrderAttr;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.service.mall.IMallOrderGoodsService;
import com.houjiang.merchant.service.mall.IMallOrderService;
import com.houjiang.merchant.validate.mall.MallOrderGoodsSearchValidate;
import com.houjiang.merchant.vo.mall.MallOrderGoodsListedVo;
import com.houjiang.merchant.vo.mallorder.MallOrderSimpleGoodsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 商家账号实现类
 * @author Amos
 */
@Service
public class MallOrderGoodsServiceImpl implements IMallOrderGoodsService {

    @Resource
    MallOrderGoodsMapper mallOrderGoodsMapper;
    @Resource
    MallGoodsSkuMapper mallGoodsSkuMapper;
    @Resource
    MallGoodsMapper mallGoodsMapper;
    @Override
    public List<MallOrderGoodsListedVo> getListByOrderId(MallOrderGoodsSearchValidate searchValidate) {
        QueryWrapper queryWrapper = new QueryWrapper<MallOrderGoods>()
                .eq("order_id", searchValidate.getOrderId())
                .eq("shop_id", searchValidate.getShopId())
                .eq("delete_time", 0);
        if (StringUtils.isNotNull(searchValidate.getAfterSaleId())) {
            queryWrapper.eq("after_sale_id", searchValidate.getAfterSaleId());
        }
        List<MallOrderGoods> list = mallOrderGoodsMapper.selectList(queryWrapper);
        List<MallOrderGoodsListedVo> ret = new ArrayList<>();
        if (list.size() > 0) {
            for (MallOrderGoods item : list) {
                MallOrderGoodsListedVo vo = new MallOrderGoodsListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setGoodsImage(UrlUtils.toShopAbsoluteUrl(item.getGoodsImage()));
                vo.setGoodsSkuValue(JSONArray.parseArray(item.getGoodsSkuValue()));
                vo.setDecMoney(item.getDiscountMoney().add(item.getCouponMoney()));
                vo.setAfterSaleDetail(MallOrderAttr.getAfterSaleDetailAttr(item.getId()));
                ret.add(vo);
            }
        }
        return ret;
    }

    @Override
    public void updateAfterSaleBase(List<Integer> ids, Integer afterSale) {
        QueryWrapper queryWrapper = new QueryWrapper<MallOrderGoods>();
        queryWrapper.in("id", ids);
        MallOrderGoods mallOrderGoods = new MallOrderGoods();
        mallOrderGoods.setAfterSale(afterSale);
        mallOrderGoodsMapper.update(mallOrderGoods, queryWrapper);
    }

    @Override
    public void orderCancelIncStock(Integer orderId) {
        List<MallOrderGoods> orderGoodsList = mallOrderGoodsMapper.selectList(new QueryWrapper<MallOrderGoods>()
                .eq("order_id", orderId).eq("delete_time", 0));

        for (MallOrderGoods orderGoods : orderGoodsList) {
            // 退回商品规格库存
            mallGoodsSkuMapper.update(null, new UpdateWrapper<MallGoodsSku>()
                    .eq("id", orderGoods.getGoodsSkuId())
                    .setSql("stock = stock + " + orderGoods.getGoodsNum()));
            // 退回商品总库存
            mallGoodsMapper.update(null, new UpdateWrapper<MallGoods>()
                    .eq("id", orderGoods.getGoodsId())
                    .setSql("total_stock = total_stock+" + orderGoods.getGoodsNum()));
        }
    }

    @Override
    public MallOrderGoods getById(Integer id) {
        return mallOrderGoodsMapper.selectById(id);
    }

    @Override
    public MallOrderSimpleGoodsVo getSimpleById(Integer id) {
        MallOrderGoods goods = this.getById(id);
        if (StringUtils.isNull(goods)) {
            return null;
        } else {
            MallOrderSimpleGoodsVo vo = new MallOrderSimpleGoodsVo();
            BeanUtils.copyProperties(goods, vo);
            vo.setGoodsImage(UrlUtils.toShopAbsoluteUrl(goods.getGoodsImage()));
            return vo;
        }
    }
}
