package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterUserResourceEntity;
import com.houjiang.job.vo.DcenterUserResourceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterUserResource
 * @author Amos
 */
@Mapper
public interface DcenterUserResourceMapper extends BaseMapper<DcenterUserResourceEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterUserResourceVO>  getDecenterUserResourceVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);
}




