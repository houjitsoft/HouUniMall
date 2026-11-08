package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterSeckillBaseEntity;
import com.houjiang.job.vo.DcenterSeckillBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterSeckillBase
 * @author Amos
 */
@Mapper
public interface DcenterSeckillBaseMapper extends BaseMapper<DcenterSeckillBaseEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterSeckillBaseVO>  getDecenterSeckillBaseVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




