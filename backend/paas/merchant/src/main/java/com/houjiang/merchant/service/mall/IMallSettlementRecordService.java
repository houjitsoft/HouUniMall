package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallSettlementRecordSearchValidate;
import com.houjiang.merchant.vo.mall.MallSettlementRecordListedVo;

/**
 *  结算详情表接口类
 * @author Amos
 */
public interface IMallSettlementRecordService {
    /**
     * @notes 获取结算详情列表
     *
     */
    PageResult<MallSettlementRecordListedVo> list(PageValidate pageValidate, MallSettlementRecordSearchValidate searchValidate);
}
