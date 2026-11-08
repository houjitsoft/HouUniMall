package com.houjiang.job.service;

import com.houjiang.commons.vo.order.CommissionSettleVO;

import java.util.List;

/**
 * @ClassName PayFxTaskService
 * @Description
 * @Author Amos
 * @Date 2023/9/5 13:25
 * @Version 1.0
 * @author Amos
 */

public interface PayFxTaskService {

    /**
     * 根据佣金结算列表和佣金类型判断是否可以进行佣金结算。
     *
     * @param commissionSettleList 佣金结算列表，包含需要结算的佣金信息。
     * @param commissionType 佣金类型，用于区分不同类型的佣金。
     * @return 返回一个Boolean值，表示是否可以进行佣金结算。
     *         如果可以结算，返回true；否则返回false。
     */
    Boolean vkOrderCommission(List<CommissionSettleVO> commissionSettleList, Integer commissionType);
}
