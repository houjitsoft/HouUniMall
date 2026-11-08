package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterGoodsBaseEntity;
import com.houjiang.job.vo.DcenterGoodsBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterGoodsBase
 * @author Amos
 */
@Mapper
public interface DcenterGoodsBaseMapper extends BaseMapper<DcenterGoodsBaseEntity> {


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterGoodsBaseVO> getDecenterGoodsBaseVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);


}




