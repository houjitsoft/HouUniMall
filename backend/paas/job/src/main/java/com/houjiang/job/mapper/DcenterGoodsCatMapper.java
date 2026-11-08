package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterGoodsCatEntity;
import com.houjiang.job.vo.DcenterGoodsCatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterGoodsCat
 * @author Amos
 */
@Mapper
public interface DcenterGoodsCatMapper extends BaseMapper<DcenterGoodsCatEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterGoodsCatVO>  getDecenterGoodsCatVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




