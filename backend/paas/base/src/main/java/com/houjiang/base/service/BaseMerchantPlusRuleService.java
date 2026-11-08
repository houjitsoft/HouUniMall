package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseMerchantPlusRuleEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * plus会员规则表(BaseMerchantPlusRule)
 *
 * @since 2023-05-15 15:00:26
 * @author Amos
 */
public interface BaseMerchantPlusRuleService extends IService<BaseMerchantPlusRuleEntity> {
    /**
     * 查询plus 会员 是否开启
     * @return
     */
    ResponseResult queryMerchantPlusRule();

    /**
     * plus 用户协议查询
     * @return
     */
    ResponseResult selectPlusUser();
    /**
     * plus 商家须知
     * @return
     */
    ResponseResult getBusinessRule();
    /**
     * 查询plus规则
     * @param map
     * @return
     */
    ResponseResult ruleselect(Map<String, Object> map);

    /**
     *更新plus规则
/**
     * @param map
     * @return
     */
    ResponseResult updateplusrule(Map<String, Object> map);

    /**
     *  查询plus规则
     * @return
     */
    Integer selectplus();
}

