package com.houjiang.merchant.service.impl.coupon;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.coupon.CouponGoods;
import com.houjiang.merchant.common.mapper.coupon.CouponGoodsMapper;
import com.houjiang.merchant.service.coupon.ICouponGoodsService;
import com.houjiang.merchant.service.mall.IMallGoodsService;
import com.houjiang.merchant.vo.coupon.CouponGooodsVo;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 优惠券商品实现类
 * @author Amos
 */
@Service
public class CouponGoodsServiceImpl implements ICouponGoodsService {

    @Resource
    CouponGoodsMapper couponGoodsMapper;

    @Resource
    IMallGoodsService iMallGoodsService;
    @Override
    public List<CouponGooodsVo> getListByCid(Integer id) {
        List<CouponGoods> list = couponGoodsMapper.selectList(new QueryWrapper<CouponGoods>().eq("coupon_id", id).eq("delete_time", 0));
        List<CouponGooodsVo> ret = new ArrayList<>();
        if (list.size() > 0) {
            for (CouponGoods item : list) {
                CouponGooodsVo vo = new CouponGooodsVo();
                vo.setGoodsId(item.getGoodsId());
                vo.setGoodsDetail(iMallGoodsService.getCouponGoods(item.getGoodsId()));
                ret.add(vo);
            }
        }
        return ret;
    }
}
