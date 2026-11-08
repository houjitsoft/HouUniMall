package com.houjiang.merchant.service.impl.pt;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.ms.MsGoods;
import com.houjiang.merchant.common.entity.pt.PtGoods;
import com.houjiang.merchant.common.mapper.ms.MsGoodsMapper;
import com.houjiang.merchant.common.mapper.pt.PtGoodsMapper;
import com.houjiang.merchant.service.mall.IMallGoodsSkuService;
import com.houjiang.merchant.service.ms.IMsGoodsService;
import com.houjiang.merchant.service.pt.IPtGoodsService;
import com.houjiang.merchant.vo.ms.MsSkuListedVo;
import com.houjiang.merchant.vo.pt.PtSkuListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 拼团活动商品实现类
 * @author Amos
 */
@Service
public class PtGoodsServiceImpl implements IPtGoodsService {
    @Resource
    IMallGoodsSkuService iMallGoodsSkuService;
    @Resource
    PtGoodsMapper ptGoodsMapper;
    @Override
    public PtGoods getMinPriceByPtId(Integer ptId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("pt_id", ptId);
        queryWrapper.eq("delete_time", 0);
        queryWrapper.orderByAsc("price");
        queryWrapper.last("limit 1");
        PtGoods msGoods = ptGoodsMapper.selectOne(queryWrapper);
        return msGoods;
    }

    @Override
    public PtGoods getMaxPriceByPtId(Integer ptId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("pt_id", ptId);
        queryWrapper.eq("delete_time", 0);
        queryWrapper.orderByDesc("price");
        queryWrapper.last("limit 1");
        PtGoods msGoods = ptGoodsMapper.selectOne(queryWrapper);
        return msGoods;
    }

    @Override
    public List<PtSkuListedVo> getMsSkuList(Integer id) {
        List<PtGoods> ptGoodsList = this.getListByMsId(id);
        List<PtSkuListedVo> voList = new ArrayList<>();
        if (ptGoodsList.size() > 0) {
            for (PtGoods item : ptGoodsList) {
                PtSkuListedVo vo = new PtSkuListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setSkuInfo(iMallGoodsSkuService.detail(item.getGoodsSkuId()));
                voList.add(vo);
            }
        }
        return voList;
    }

    @Override
    public List<PtGoods> getListByMsId(Integer ptId) {
        return ptGoodsMapper.selectList(new QueryWrapper<PtGoods>().eq("pt_id", ptId).eq("delete_time", 0));
    }

    @Override
    public void destroyItemByPtId(Integer ptId) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("pt_id", ptId);
        ptGoodsMapper.delete(wrapper);
    }
}
