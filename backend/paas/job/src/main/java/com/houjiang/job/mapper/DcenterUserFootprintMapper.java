package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterUserFootprintEntity;
import com.houjiang.job.vo.DcenterUserFootprintVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterUserFootprint
 * @author Amos
 */
@Mapper
public interface DcenterUserFootprintMapper extends BaseMapper<DcenterUserFootprintEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterUserFootprintVO>  getDecenterUserFootprintVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




