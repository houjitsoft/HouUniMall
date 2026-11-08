package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.JobMoneyStaticsEntity;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Entity com.houjiang.job.entity.JobMoneyStaticsEntity
 * @author Amos
 */
@Mapper
public interface JobMoneyStaticsMapper extends BaseMapper<JobMoneyStaticsEntity> {

    /**
     * 查询用户余额和充值卡余额
     * @return
     */
    Map<String,Object> queryMoneyStatist();

    /**
     *查询已充值金额
/**
     * @return
     */
    BigDecimal getRechargedAmount();

    /**
     *查询激活未充值金额
/**
     * @return
     */
    BigDecimal getUnrechargedAmount();
}




