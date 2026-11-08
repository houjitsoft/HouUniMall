package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterOrderReturnEntity;
import com.houjiang.job.vo.DcenterOrderReturnVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterOrderReturn
 * @author Amos
 */
@Mapper
public interface DcenterOrderReturnMapper extends BaseMapper<DcenterOrderReturnEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterOrderReturnVO> getDecenterOrderReturnVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);
}




