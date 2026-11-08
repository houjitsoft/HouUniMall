package com.houjiang.job.service;

import com.houjiang.commons.vo.order.CommissionSettleVO;
import com.houjiang.commons.vo.payment.PayFxRecordVO;

import java.util.List;

/**
 * @ClassName PayFxService
 * @Description
 * @Author Amos
 * @Date 2023/9/5 13:40
 * @Version 1.0
 * @author Amos
 */

public interface PayFxService {

    /**
     * 封装分销佣金结算记录
     * @param commissionSettle
     * @return
     */
    List<PayFxRecordVO> buildFxRecord(CommissionSettleVO commissionSettle);

}
