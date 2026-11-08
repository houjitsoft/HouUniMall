package com.houjiang.merchant.service.finance;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.finance.FinanceWalletSearchValidate;
import com.houjiang.merchant.vo.finance.FinanceIndexVo;
import com.houjiang.merchant.vo.finance.FinanceWalletListVo;

import java.util.Map;

/**
 *  财务中心接口类
 * @author Amos
 */
public interface IFinanceService {

    /**
     * 财务中心
     * @return
     */
    FinanceIndexVo index(Integer shopId);
}
