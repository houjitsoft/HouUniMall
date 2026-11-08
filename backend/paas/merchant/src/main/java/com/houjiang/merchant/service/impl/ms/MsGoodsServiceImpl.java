package com.houjiang.merchant.service.impl.ms;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.ms.Ms;
import com.houjiang.merchant.common.entity.ms.MsGoods;
import com.houjiang.merchant.common.enums.MsEnum;
import com.houjiang.merchant.common.mapper.ms.MsGoodsMapper;
import com.houjiang.merchant.common.mapper.ms.MsMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.service.mall.IMallGoodsService;
import com.houjiang.merchant.service.mall.IMallGoodsSkuService;
import com.houjiang.merchant.service.ms.IMsGoodsService;
import com.houjiang.merchant.service.ms.IMsService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.ms.MsSearchValidate;
import com.houjiang.merchant.vo.ms.GoodsMsVo;
import com.houjiang.merchant.vo.ms.MsListedVo;
import com.houjiang.merchant.vo.ms.MsOrderCountVo;
import com.houjiang.merchant.vo.ms.MsSkuListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 拼团活动商品实现类
 * @author Amos
 */
@Service
public class MsGoodsServiceImpl implements IMsGoodsService {
    @Resource
    IMallGoodsSkuService iMallGoodsSkuService;
    @Resource
    MsGoodsMapper msGoodsMapper;
    @Override
    public MsGoods getMinPriceByMsId(Integer msId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("ms_id", msId);
        queryWrapper.eq("delete_time", 0);
        queryWrapper.orderByAsc("price");
        queryWrapper.last("limit 1");
        MsGoods msGoods = msGoodsMapper.selectOne(queryWrapper);
        return msGoods;
    }

    @Override
    public MsGoods getMaxPriceByMsId(Integer msId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("ms_id", msId);
        queryWrapper.eq("delete_time", 0);
        queryWrapper.orderByDesc("price");
        queryWrapper.last("limit 1");
        MsGoods msGoods = msGoodsMapper.selectOne(queryWrapper);
        return msGoods;
    }

    @Override
    public List<MsSkuListedVo> getMsSkuList(Integer id) {
        List<MsGoods> msGoodsList = this.getListByMsId(id);
        List<MsSkuListedVo> voList = new ArrayList<>();
        if (msGoodsList.size() > 0) {
            for (MsGoods item : msGoodsList) {
                MsSkuListedVo vo = new MsSkuListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setSkuInfo(iMallGoodsSkuService.detail(item.getGoodsSkuId()));
                voList.add(vo);
            }
        }
        return voList;
    }

    @Override
    public List<MsGoods> getListByMsId(Integer msId) {
        return msGoodsMapper.selectList(new QueryWrapper<MsGoods>().eq("ms_id", msId).eq("delete_time", 0));
    }

    @Override
    public void destroyItemByMsId(Integer msId) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("ms_id", msId);
        msGoodsMapper.delete(wrapper);
    }
}
