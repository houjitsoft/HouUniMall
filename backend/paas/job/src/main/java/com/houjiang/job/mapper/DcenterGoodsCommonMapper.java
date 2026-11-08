package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterGoodsCommonEntity;
import com.houjiang.job.vo.DcenterGoodsCommonVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterGoodsCommon
 * @author Amos
 */
@Mapper
public interface DcenterGoodsCommonMapper extends BaseMapper<DcenterGoodsCommonEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterGoodsCommonVO>  getDecenterGoodsCommonVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




