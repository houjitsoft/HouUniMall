package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.IntegralRulesDTO;
import com.houjiang.base.entity.BaseIntegralRulesEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * @ClassName BaseIntegralRulesService
 * @Description
 * @Author Amos
 * @Date 2023-05-22 16:51:47
 * @Version 1.0
 * @author Amos
 */
public interface BaseIntegralRulesService extends IService<BaseIntegralRulesEntity> {

    /**
     * 查询积分规则
     * @return
     */
    ResponseResult queryIntegralRules();


    /**
     * 添加 或 修改积分规则
     * @param param
     * @return
     */
    ResponseResult integralRulesAddOrUpdate(IntegralRulesDTO param);
}
