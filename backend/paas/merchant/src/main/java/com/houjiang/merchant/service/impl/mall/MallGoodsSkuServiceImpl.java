package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallGoodsSku;
import com.houjiang.merchant.common.entity.mall.MallGoodsSkuName;
import com.houjiang.merchant.common.entity.mall.MallGoodsSkuValue;
import com.houjiang.merchant.common.mapper.mall.MallGoodsSkuMapper;
import com.houjiang.merchant.common.mapper.mall.MallGoodsSkuNameMapper;
import com.houjiang.merchant.common.mapper.mall.MallGoodsSkuValueMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.service.mall.IMallGoodsSkuService;
import com.houjiang.merchant.vo.mall.MallGoodsSkuDetailVo;
import com.houjiang.merchant.vo.mall.MallGoodsSkuListedVo;
import com.houjiang.merchant.vo.mall.SkuNameListVo;
import com.houjiang.merchant.vo.mall.SpecListVo;
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
public class MallGoodsSkuServiceImpl implements IMallGoodsSkuService {

    @Resource
    MallGoodsSkuMapper mallGoodsSkuMapper;
    @Resource
    MallGoodsSkuNameMapper goodsSkuNameMapper;
    @Resource
    MallGoodsSkuValueMapper goodsSkuValueMapper;
    @Override
    public List<MallGoodsSkuListedVo> getListByGoods(Integer goodsId) {
        List<MallGoodsSku> list = mallGoodsSkuMapper.selectList(new QueryWrapper<MallGoodsSku>().eq("goods_id", goodsId).eq("delete_time", 0));
        List<MallGoodsSkuListedVo> retlist = new ArrayList<>();
        if (list.size() > 0) {
            for (MallGoodsSku item : list) {
                MallGoodsSkuListedVo vo = new MallGoodsSkuListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setImage(UrlUtils.toShopAbsoluteUrl(item.getImage()));
                vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
                vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
                vo.setSkuValueArr(JSONArray.parseArray(item.getSkuValueArr()));
                vo.setSkuValueIds(JSONArray.parseArray(item.getSkuValueIds()));
                retlist.add(vo);
            }
        }
        return retlist;
    }

    @Override
    public List<SkuNameListVo> getSkuNameList(Integer goodsId) {
        // 商品SKU
        List<MallGoodsSkuName> goodsSkuNameList = goodsSkuNameMapper.selectList(new QueryWrapper<MallGoodsSkuName>().eq("goods_id", goodsId).eq("delete_time", 0));
        List<MallGoodsSkuValue> goodsSkuValueList = goodsSkuValueMapper.selectList(new QueryWrapper<MallGoodsSkuValue>().eq("goods_id", goodsId).eq("delete_time", 0));
        List<SkuNameListVo> specValue = new ArrayList<>();
        for (MallGoodsSkuName goodsSkuName : goodsSkuNameList) {
            SkuNameListVo specValueVo = new SkuNameListVo();
            BeanUtils.copyProperties(goodsSkuName,specValueVo);
            List<SpecListVo> specListVos = new ArrayList<>();
            for (MallGoodsSkuValue goodsSkuValue : goodsSkuValueList) {
                if(goodsSkuValue.getSkuNameId().equals(goodsSkuName.getId())){
                    SpecListVo specListVo = new SpecListVo();
                    BeanUtils.copyProperties(goodsSkuValue,specListVo);
                    specListVo.setImage(specListVo.getImage() != null ? specListVo.getImage() : "");
                    specListVos.add(specListVo);
                }
            }
            specValueVo.setValue(specListVos);
            specValue.add(specValueVo);
        }
        return specValue;
    }

    @Override
    public MallGoodsSkuDetailVo detail(Integer id) {
        MallGoodsSku model = this.getById(id);
        if (StringUtils.isNull(model)) {
            return null;
        } else {
            MallGoodsSkuDetailVo vo = new MallGoodsSkuDetailVo();
            BeanUtils.copyProperties(model, vo);
            vo.setSkuValueArr(JSONArray.parseArray(model.getSkuValueArr()));
            vo.setSkuValueIds(JSONArray.parseArray(model.getSkuValueIds()));
            return vo;
        }
    }

    @Override
    public MallGoodsSku getById(Integer id) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("id", id);
        wrapper.eq("delete_time", 0);
        return mallGoodsSkuMapper.selectOne(wrapper);
    }
}
