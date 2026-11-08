package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.entity.mall.MallRefundLog;
import com.houjiang.merchant.vo.mall.MallOrderLogListedVo;
import com.houjiang.merchant.vo.mall.MallRefundLogListedVo;

import java.util.List;

/**
 *  退款日志接口类
 * @author Amos
 */
public interface IMallRefundLogService {

    /**
     * 退款日志列表
     * @param shopId
     * @return
     */
    List<MallRefundLogListedVo> getListByRefundId(Integer refundId);

    /**
     * 保存日志
     * @param log
     */
    void createLog(MallRefundLog log);
}
