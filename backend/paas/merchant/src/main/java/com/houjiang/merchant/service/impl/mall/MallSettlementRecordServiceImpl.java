package com.houjiang.merchant.service.impl.mall;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallSettlementRecord;
import com.houjiang.merchant.common.enums.SettlementEnum;
import com.houjiang.merchant.common.mapper.mall.MallSettlementRecordMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.service.mall.IMallSettlementRecordService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallSettlementRecordSearchValidate;
import com.houjiang.merchant.vo.mall.MallSettlementRecordListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 商家账号实现类
 * @author Amos
 */
@Service
public class MallSettlementRecordServiceImpl implements IMallSettlementRecordService {
    @Resource
    MallSettlementRecordMapper mallSettlementRecordMapper;
    @Override
    public PageResult<MallSettlementRecordListedVo> list(PageValidate pageValidate, MallSettlementRecordSearchValidate searchValidate) {
        Integer pageNo = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();
        QueryWrapper wrapper = new QueryWrapper<MallSettlementRecord>().eq("shop_id", searchValidate.getShopId()).eq("delete_time", 0).orderByDesc("id");
        if (StringUtils.isNotNull(searchValidate.getSettlementId())) {
            wrapper.eq("settlement_id", searchValidate.getSettlementId());
        }
        if (StringUtils.isNotNull(searchValidate.getType())) {
            wrapper.eq("type", searchValidate.getType());
        }
        if (StringUtils.isNotEmpty(searchValidate.getFrom_sn())) {
            wrapper.like("from_sn", searchValidate.getFrom_sn());
        }
        IPage<MallSettlementRecord> iPage = mallSettlementRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<MallSettlementRecordListedVo> voList = new ArrayList<>();
        if (iPage.getRecords().size() > 0) {
            for (MallSettlementRecord item : iPage.getRecords()) {
                MallSettlementRecordListedVo vo = new MallSettlementRecordListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
                vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
                vo.setTypeText(SettlementEnum.getMsgByCode(item.getType()));
                voList.add(vo);
            }
        }
        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), voList, new HashMap<>());
    }
}
