package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserPlusEquityTempEntity;
import com.houjiang.userms.mapper.UserPlusEquityTempMapper;
import com.houjiang.userms.service.UserPlusEquityTempService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户权益临时表 服务实现类
 * </p>
 *
 * @author Amos
 * @since 2022-02-28
 */
@Service
public class UserPlusEquityTempServiceImpl extends ServiceImpl<UserPlusEquityTempMapper, UserPlusEquityTempEntity>
    implements UserPlusEquityTempService{

    /**
     * 根据权益ID列表查询权益状态为1的用户权益临时记录
     * @param id 权益ID列表
     * @return 包含符合条件的用户权益临时记录的ResponseResult对象
     */
    @Override
    public ResponseResult selectPlusEquityTemps(List<Integer> id) {
        // 使用LambdaQueryWrapper构造查询条件，查询权益状态为1且权益ID在指定列表中的记录
        List<UserPlusEquityTempEntity> list=this.baseMapper.selectList(new LambdaQueryWrapper<UserPlusEquityTempEntity>()
                .eq(UserPlusEquityTempEntity::getEquityStatus,1)
                .in(UserPlusEquityTempEntity::getEquityId,id));
        // 返回查询结果，包装在ResponseResult对象中
        return ResponseResult.success(list);
    }
}
