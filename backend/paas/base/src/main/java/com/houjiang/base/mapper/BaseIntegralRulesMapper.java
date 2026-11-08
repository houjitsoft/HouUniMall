package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.dto.IntegralRulesDTO;
import com.houjiang.base.entity.BaseIntegralRulesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.houjiang.base.entity.BaseIntegralRules
 * @author Amos
 */
@Mapper
public interface BaseIntegralRulesMapper extends BaseMapper<BaseIntegralRulesEntity> {

    /**
     * 新增积分规则
     *
     * @param param
     */
    void integralRulesAdd(@Param("param") IntegralRulesDTO param);

}




