package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallOrderLog;
import com.houjiang.merchant.common.entity.mall.MallRefundLog;
import com.houjiang.merchant.common.enums.OrderLogEnum;
import com.houjiang.merchant.common.enums.RefundEnum;
import com.houjiang.merchant.common.mapper.mall.MallOrderLogMapper;
import com.houjiang.merchant.common.mapper.mall.MallRefundLogMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.service.mall.IMallOrderLogService;
import com.houjiang.merchant.service.mall.IMallRefundLogService;
import com.houjiang.merchant.vo.mall.MallOrderLogListedVo;
import com.houjiang.merchant.vo.mall.MallRefundLogListedVo;
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
public class MallRefundLogServiceImpl implements IMallRefundLogService {

    @Resource
    IMallOrderLogService iMallOrderLogService;
    @Resource
    MallRefundLogMapper mallRefundLogMapper;
    @Override
    public List<MallRefundLogListedVo> getListByRefundId(Integer refundId) {
        List<MallRefundLog> list = mallRefundLogMapper.selectList(new QueryWrapper<MallRefundLog>().eq("refund_id", refundId).eq("delete_time", 0).orderByDesc("id"));
        List<MallRefundLogListedVo> retList = new ArrayList<MallRefundLogListedVo>();
        if (list.size() > 0) {
            for (MallRefundLog item : list) {
                MallRefundLogListedVo vo = new MallRefundLogListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
                vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
                vo.setRefundStatusText(this.getRefundStatusTextAttr(item.getRefundStatus()));
                vo.setOperator(iMallOrderLogService.getOperatorAttr(item.getOperatorType(), item.getOperatorId()));
                retList.add(vo);
            }
        }
        return retList;
    }

    @Override
    public void createLog(MallRefundLog log) {
        log.setCreateTime(System.currentTimeMillis() / 1000);
        log.setUpdateTime(System.currentTimeMillis() / 1000);
        log.setSn(mallRefundLogMapper.randMakeOrderSn("sn"));
        mallRefundLogMapper.insert(log);
    }

    private String getRefundStatusTextAttr(Integer refundStatus) {
        return RefundEnum.getRefundStatusMsg(refundStatus);
    }

}
