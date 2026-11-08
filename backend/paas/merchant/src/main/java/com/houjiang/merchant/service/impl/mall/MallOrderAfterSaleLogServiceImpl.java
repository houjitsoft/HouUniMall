package com.houjiang.merchant.service.impl.mall;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallOrderAfterSale;
import com.houjiang.merchant.common.entity.mall.MallOrderAfterSaleLog;
import com.houjiang.merchant.common.mapper.mall.MallOrderAfterSaleLogMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.service.mall.IMallOrderAfteSaleLogService;
import com.houjiang.merchant.service.mall.IMallOrderLogService;
import com.houjiang.merchant.validate.mallorder.OrderAfterSaleLogCreateValidate;
import com.houjiang.merchant.vo.mall.MallOrderAfterSaleLogListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 退货日志实现类
 * @author Amos
 */
@Service
public class MallOrderAfterSaleLogServiceImpl implements IMallOrderAfteSaleLogService {
    @Resource
    MallOrderAfterSaleLogMapper mallOrderAfterSaleLogMapper;
    @Resource
    IMallOrderLogService iMallOrderLogService;

    @Override
    public void add(OrderAfterSaleLogCreateValidate validate) {
        MallOrderAfterSaleLog log = new MallOrderAfterSaleLog();
        BeanUtils.copyProperties(validate, log);
        log.setCreateTime(System.currentTimeMillis() / 1000);
        log.setUpdateTime(System.currentTimeMillis() / 1000);
        mallOrderAfterSaleLogMapper.insert(log);
    }

    @Override
    public List<MallOrderAfterSaleLogListedVo> getListByOId(Integer orderId) {
        List<MallOrderAfterSaleLog> list = mallOrderAfterSaleLogMapper.selectList(new QueryWrapper<MallOrderAfterSaleLog>().eq("after_sale_id", orderId).eq("delete_time", 0).orderByDesc("id"));
        List<MallOrderAfterSaleLogListedVo> listVo = new ArrayList<MallOrderAfterSaleLogListedVo>();
        if (list.size() > 0) {
            for (MallOrderAfterSaleLog item : list) {
                MallOrderAfterSaleLogListedVo vo = new MallOrderAfterSaleLogListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setOperator(iMallOrderLogService.getOperatorAttr(item.getOperatorType(), item.getOperatorId()));
                vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
                listVo.add(vo);
            }
        }
        return listVo;
    }
}
