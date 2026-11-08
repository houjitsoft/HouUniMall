package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.entity.BaseMerchantPlusRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * plus会员规则表(BaseMerchantPlusRule)
 *
 * @since 2023-05-15 15:00:26
 * @author Amos
 */
@Mapper
public interface BaseMerchantPlusRuleMapper extends BaseMapper<BaseMerchantPlusRuleEntity> {

    /**
     * 查询plus 会员 是否开启
     * @return
     */
    Integer queryMerchantPlusRule();

    /**
     * plus 用户协议查询
     * @return
     */
    String selectplusrule1();
    /**
     * 获取商家plus规则
     * @return
     */
    String getBusinessRule();
    /**
     * 修改plus会员规则
     * @return
     */
    Integer updateplusrule(@Param("param")Map<String, Object> map);

    /**
     *新增会员规则
/**
     * @param plusRule
     * @return
     */
    Integer insertplusrule(@Param("param") Map<String, Object> plusRule);

    /**
     *  查询plus规则
     * @return
     */
    Integer selectplus();


}

