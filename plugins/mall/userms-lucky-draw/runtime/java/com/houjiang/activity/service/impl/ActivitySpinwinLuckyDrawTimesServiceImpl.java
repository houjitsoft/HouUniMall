package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.activity.entity.ActivitySpinwinLuckyDrawTimesEntity;
import com.houjiang.activity.mapper.ActivitySpinwinLuckyDrawTimesMapper;
import com.houjiang.activity.service.ActivitySpinwinLuckyDrawTimesService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Amos
 */
@Service
public class ActivitySpinwinLuckyDrawTimesServiceImpl extends ServiceImpl<ActivitySpinwinLuckyDrawTimesMapper, ActivitySpinwinLuckyDrawTimesEntity>
    implements ActivitySpinwinLuckyDrawTimesService{

    /**
     * 查询抽奖次数列表
     *
     * @param param 查询参数，包含分页信息和过滤条件
     * @return 返回抽奖次数列表的响应结果
     */
    @Override
    public ResponseResult queryLuckyDrawTimesList(Map<String, Object> param) {
        // 根据查询参数进行抽奖次数列表的分页查询
        // 分页查询数据
        IPage<ActivitySpinwinLuckyDrawTimesEntity> page = this.baseMapper.selectLuckyDrawTimesList(
                new Query<ActivitySpinwinLuckyDrawTimesEntity>().getPage(param), param);

        // 将查询结果包装成响应结果返回
        return ResponseResult.success(PageUtils.getPage(page));
    }
}




