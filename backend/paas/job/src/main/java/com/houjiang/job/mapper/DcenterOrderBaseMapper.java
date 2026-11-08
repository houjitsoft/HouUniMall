package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterOrderBaseEntity;
import com.houjiang.job.vo.DecenterOrderBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterOrderBase
 * @author Amos
 */
@Mapper
public interface DcenterOrderBaseMapper extends BaseMapper<DcenterOrderBaseEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DecenterOrderBaseVO> getDecenterOrderBaseVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);
}




