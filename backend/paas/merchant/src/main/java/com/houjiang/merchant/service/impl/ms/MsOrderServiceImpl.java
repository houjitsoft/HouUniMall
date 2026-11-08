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
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.mapper.mall.MallOrderMapper;
import com.houjiang.merchant.common.mapper.ms.MsMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.service.mall.IMallGoodsService;
import com.houjiang.merchant.service.ms.IMsGoodsService;
import com.houjiang.merchant.service.ms.IMsOrderService;
import com.houjiang.merchant.service.ms.IMsService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.ms.MsSearchValidate;
import com.houjiang.merchant.vo.ms.GoodsMsVo;
import com.houjiang.merchant.vo.ms.MsListedVo;
import com.houjiang.merchant.vo.ms.MsOrderCountVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 拼团活动实现类
 * @author Amos
 */
@Service
public class MsOrderServiceImpl implements IMsOrderService {
    @Resource
    MallOrderMapper mallOrderMapper;


    @Override
    public MsOrderCountVo getOrderCount(Ms record) {
        MsOrderCountVo vo = new MsOrderCountVo();
        QueryWrapper wrapperCount = new QueryWrapper<Ms>();
        wrapperCount.eq("ms_id", record.getId());
        wrapperCount.eq("delete_time", 0);
        wrapperCount.eq("pay_is", YesNoEnum.YES.getCode());
        Long count = mallOrderMapper.selectCount(wrapperCount);

        QueryWrapper wrapperSaleMoney = new QueryWrapper<Ms>();
        wrapperSaleMoney.eq("ms_id", record.getId());
        wrapperSaleMoney.eq("delete_time", 0);
        wrapperSaleMoney.eq("pay_is", YesNoEnum.YES.getCode());
        BigDecimal saleMoney = mallOrderMapper.sum("pay_money", wrapperSaleMoney);

        QueryWrapper wrapperSaleNum = new QueryWrapper<Ms>();
        wrapperSaleNum.eq("ms_id", record.getId());
        wrapperSaleNum.eq("delete_time", 0);
        wrapperSaleNum.eq("pay_is", YesNoEnum.YES.getCode());
        BigDecimal saleNum = mallOrderMapper.sum("total_goods_num", wrapperSaleNum);

        vo.setCount(count);
        vo.setMsId(record.getId());
        vo.setSaleMoney(saleMoney);
        vo.setSaleNum(saleNum.longValue());
        return vo;
    }
}
