package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterLogActionEntity;
import com.houjiang.job.vo.DcenterLogActionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterLogAction
 * @author Amos
 */
@Mapper
public interface DcenterLogActionMapper extends BaseMapper<DcenterLogActionEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterLogActionVO> getDecenterLogActionVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);
}




