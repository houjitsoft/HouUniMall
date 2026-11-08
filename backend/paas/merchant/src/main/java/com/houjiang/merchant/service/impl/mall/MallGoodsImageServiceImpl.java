package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallGoodsImage;
import com.houjiang.merchant.common.mapper.mall.MallGoodsImageMapper;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.service.mall.IMallGoodsImageService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 商家账号实现类
 * @author Amos
 */
@Service
public class MallGoodsImageServiceImpl implements IMallGoodsImageService {

    @Resource
    MallGoodsImageMapper mallGoodsImageMapper;
    @Override
    public List<String> getListByGoodsId(Integer goodsId) {
        List<MallGoodsImage> goodsImages = mallGoodsImageMapper.selectList(new QueryWrapper<MallGoodsImage>().eq("goods_id", goodsId).eq("delete_time", 0));
        List<String> ret = new ArrayList<String>();
        if (goodsImages.size() > 0) {
            for (MallGoodsImage item : goodsImages) {
                ret.add(UrlUtils.toShopAbsoluteUrl(item.getImage()));
            }
        }
        return ret;
    }
}
