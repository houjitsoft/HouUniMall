package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.job.dto.JobDTO;
import com.houjiang.job.entity.JobPlatformEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.houjiang.job.entity.JobPlatform
 * @author Amos
 */
@Mapper
public interface JobPlatformMapper extends BaseMapper<JobPlatformEntity> {

    /**
     * 分页查询所有工作
     *
     * @param page
     * @param job
     * @return
     */
    IPage<JobDTO> selectJobAll(IPage<JobDTO> page, @Param("job") JobDTO job);

    /**
     * 新增工作
     *
     * @param job
     */
    Integer insertJob(JobDTO job);

    /**
     * 修改工作
     *
     * @param job
     */
    Integer editJobById(JobDTO job);

    /**
     * 删除工作
     *
     * @param job
     */
    Integer deleteJobById(JobDTO job);

}




