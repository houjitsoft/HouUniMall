package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterUserAccessEntity;
import com.houjiang.job.vo.DcenterUserAccessVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterUserAccess
 * @author Amos
 */
@Mapper
public interface DcenterUserAccessMapper extends BaseMapper<DcenterUserAccessEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterUserAccessVO>  getDecenterUserAccessVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




