package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterOrderCancelReasonEntity;
import com.houjiang.job.vo.DcenterOrderCancelReasonVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterOrderCancelReason
 * @author Amos
 */
@Mapper
public interface DcenterOrderCancelReasonMapper extends BaseMapper<DcenterOrderCancelReasonEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterOrderCancelReasonVO> getDecenterOrderCancelReasonVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);
}




