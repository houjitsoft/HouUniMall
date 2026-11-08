package com.houjiang.merchant.service.ms;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.ms.Ms;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.ms.MsSearchValidate;
import com.houjiang.merchant.vo.ms.GoodsMsVo;
import com.houjiang.merchant.vo.ms.MsListedVo;
import com.houjiang.merchant.vo.ms.MsOrderCountVo;

/**
 *  秒杀活动接口类
 * @author Amos
 */
public interface IMsOrderService {
    /**
     * 获取秒杀统计信息
     * @param record
     * @return
     */
    MsOrderCountVo getOrderCount(Ms record);
}
