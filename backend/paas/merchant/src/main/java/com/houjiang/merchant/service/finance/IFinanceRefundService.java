package com.houjiang.merchant.service.finance;

import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.finance.FinanceRefundSearchValidate;
import com.houjiang.merchant.vo.finance.FinanceRefundListVo;
import com.houjiang.merchant.vo.finance.FinanceRefundLogVo;
import com.houjiang.merchant.common.core.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 退款记录服务接口类
 */
public interface IFinanceRefundService {

    /**
     * 退款记录列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<FinanceRechargeListVo>
     */
    PageResult<FinanceRefundListVo> list(PageValidate pageValidate, FinanceRefundSearchValidate searchValidate);

    /**
     * 退款日志
     *
     * @param recordId 退款日志ID
     * @return  List<FinanceRefundLogVo>
     */
    List<FinanceRefundLogVo> log(Integer recordId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Map<String, Object> stat();
}
