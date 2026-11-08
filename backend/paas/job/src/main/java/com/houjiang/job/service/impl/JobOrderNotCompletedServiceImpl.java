package com.houjiang.job.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.entity.JobOrderNotCompletedEntity;
import com.houjiang.job.mapper.JobOrderNotCompletedMapper;
import com.houjiang.job.service.JobOrderNotCompletedService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class JobOrderNotCompletedServiceImpl extends ServiceImpl<JobOrderNotCompletedMapper, JobOrderNotCompletedEntity>
    implements JobOrderNotCompletedService {

    /**
     * 插入未完成任务的实现方法。
     *
     * 本方法用于处理将查询到的未完成任务信息插入到数据库中的逻辑。首先，通过调用baseMapper的queryNotCompletedparam方法，
     * 查询出所有未完成的任务信息。然后，将这些信息转换为JobOrderNotCompletedEntity实体的列表，
     * 并批量保存到数据库中。如果查询结果为空，则不执行保存操作。
     *
     * @param finishTime 查询未完成任务的依据，通常是指定的完成时间。
     * @return 返回操作结果，如果操作成功，则返回一个成功的ResponseResult对象。
     */
    @Override
    public ResponseResult insertNotCompletedJobimplements(String finishTime) {
        // 通过查询语句获取未完成任务的信息
        List<Map<String,Object>> result = this.baseMapper.queryNotCompletedparam(finishTime);

        // 初始化一个空列表，用于存储转换后的实体对象
        List<JobOrderNotCompletedEntity> entities=new ArrayList<>();
        // 判断查询结果是否为空，如果不为空，则进行后续处理
        if (!CollectionUtils.isEmpty(result)){
            // 遍历查询结果，将每个Map对象转换为JobOrderNotCompletedEntity实体，并添加到列表中
            for (Map<String,Object> map:result) {
                JobOrderNotCompletedEntity entity= ParseMapUtils.parseMap2Object(map,JobOrderNotCompletedEntity.class);
                entities.add(entity);
            }
            // 批量保存转换后的实体到数据库中
            this.saveBatch(entities);
        }

        // 返回操作成功的响应结果
        return ResponseResult.success();
    }
}




