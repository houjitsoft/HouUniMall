package com.houjiang.merchant.service.finance;

import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.finance.FinanceWalletSearchValidate;
import com.houjiang.merchant.vo.finance.FinanceWalletListVo;
import com.houjiang.merchant.common.core.PageResult;

import java.util.Map;

/**
 * 用户余额记录服务接口类
 */
public interface IFinanceWalletService {

    /**
     * 余额明细列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<FinanceWalletListVo>
     */
    PageResult<FinanceWalletListVo> list(PageValidate pageValidate, FinanceWalletSearchValidate searchValidate);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Map<Integer, String> getUmChangeType();
}
