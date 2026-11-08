package com.houjiang.job.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.job.entity.JobMoneyStaticsEntity;
import com.houjiang.job.mapper.JobMoneyStaticsMapper;
import com.houjiang.job.service.JobMoneyStaticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class JobMoneyStaticsServiceImpl extends ServiceImpl<JobMoneyStaticsMapper, JobMoneyStaticsEntity>
    implements JobMoneyStaticsService {

    /**
     * 插入资金统计数据
     * 该方法用于根据当前的统计数据，创建并插入一个新的资金静态数据实体。
     * 它首先查询当前的资金统计数据，然后计算已充值和未充值的金额，
     * 最后将这些数据封装到一个新的实体中并插入到数据库中。
     *
     * @param mapHash 用于标识统计数据创建时间的哈希值
     */
    @Override
    public void InsertMoneyStatistport(String mapHash) {
        // 查询当前的资金统计数据
        Map<String,Object> result = this.baseMapper.queryMoneyStatist();
        // 获取已充值金额
        BigDecimal rechargedAmount = this.baseMapper.getRechargedAmount();
        // 获取未充值金额
        BigDecimal unrechargeAmount = this.baseMapper.getUnrechargedAmount();

        // 将查询结果映射到JobMoneyStaticsEntity实体中
        JobMoneyStaticsEntity entity= ParseMapUtils.parseMap2Object(result,JobMoneyStaticsEntity.class);

        // 设置已充值和未充值金额
        entity.setRechargedAmount(rechargedAmount);
        entity.setUnrechargeAmount(unrechargeAmount);
        // 设置统计数据的创建时间
        entity.setCreateTime(mapHash);
        // 插入封装后的资金静态数据实体
        this.baseMapper.insert(entity);
    }

}




