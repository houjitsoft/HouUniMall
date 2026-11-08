package com.houjiang.merchant.service.impl.mall;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallSettlement;
import com.houjiang.merchant.common.mapper.mall.MallSettlementMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.service.mall.IMallOrderService;
import com.houjiang.merchant.service.mall.IMallSettlementService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.mall.MallSettlementListedVo;
import com.houjiang.merchant.vo.mall.MallSettlementCountSettlementVo;
import com.houjiang.merchant.vo.mall.MallSettlementCountVo;
import com.houjiang.merchant.vo.mall.MallSettlementCountWaitVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 商家账号实现类
 * @author Amos
 */
@Service
public class MallSettlementServiceImpl implements IMallSettlementService {

    @Resource
    IMallOrderService iMallOrderService;
    @Resource
    MallSettlementMapper mallSettlementMapper;
    @Override
    public Integer getSettlementOrders(Integer shopId) {
        BigDecimal sum = mallSettlementMapper.sum("order_num", new QueryWrapper<MallSettlement>().eq("shop_id", shopId).eq("delete_time", 0));
        return sum.intValue();
    }

    @Override
    public BigDecimal getSettlementOrderMoney(Integer shopId) {
        return mallSettlementMapper.sum("order_money", new QueryWrapper<MallSettlement>().eq("shop_id", shopId).eq("delete_time", 0));
    }

    @Override
    public BigDecimal getSettlementMoney(Integer shopId) {
        return mallSettlementMapper.sum("money", new QueryWrapper<MallSettlement>().eq("shop_id", shopId).eq("delete_time", 0));
    }

    @Override
    public BigDecimal getSettlementServiceMoney(Integer shopId) {
        return mallSettlementMapper.sum("service_money", new QueryWrapper<MallSettlement>().eq("shop_id", shopId).eq("delete_time", 0));
    }

    @Override
    public MallSettlementCountVo count(Integer shopId) {
        MallSettlementCountVo vo = new MallSettlementCountVo();
        MallSettlementCountSettlementVo settlementVo = new MallSettlementCountSettlementVo();
        settlementVo.setAllOrderNum(Long.valueOf(this.getSettlementOrders(shopId)));
        settlementVo.setAllMoney(this.getSettlementMoney(shopId));
        settlementVo.setAllOrderMoney(this.getSettlementOrderMoney(shopId));
        settlementVo.setAllServiceMoney(this.getSettlementServiceMoney(shopId));

        MallSettlementCountWaitVo waitVo = new MallSettlementCountWaitVo();
        waitVo.setAllOrderMoney(iMallOrderService.getWaitSettlementAllOrderMoeny(shopId));
        waitVo.setAllOrderNum(Long.valueOf(iMallOrderService.getWaitSettlementAllOrderNum(shopId)));
        vo.setSettlement(settlementVo);
        vo.setWait(waitVo);
        return vo;
    }

    @Override
    public PageResult<MallSettlementListedVo> list(PageValidate pageValidate, Integer shopId) {
        Integer pageNo = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();
        IPage<MallSettlement> iPage = mallSettlementMapper.selectPage(new Page<>(pageNo, pageSize), new QueryWrapper<MallSettlement>().eq("shop_id", shopId).eq("delete_time", 0).orderByDesc("id"));
        List<MallSettlementListedVo> voList = new ArrayList<>();
        if (iPage.getRecords().size() > 0) {
            for (MallSettlement item : iPage.getRecords()) {
                MallSettlementListedVo vo = new MallSettlementListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
                vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
                voList.add(vo);
            }
        }
        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), voList, new HashMap<>());
    }
}
