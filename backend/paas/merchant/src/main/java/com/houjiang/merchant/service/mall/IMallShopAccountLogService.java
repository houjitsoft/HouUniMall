package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopAccountLog;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopAccountLogSearchValidate;
import com.houjiang.merchant.vo.mall.MallShopAccountLogListedVo;

/**
 * 商家流水服务接口类
 * @author Amos
 */
public interface IMallShopAccountLogService {

    /**
     * 商家流水列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopAccountLogListedVo>
     */
    PageResult<MallShopAccountLogListedVo> list(PageValidate pageValidate, MallShopAccountLogSearchValidate searchValidate);


    /**
     * @notes
     * @param array $params
     * @return bool
     * @author Amos
     */
    void createAccountLog(MallShopAccountLog log);

}
