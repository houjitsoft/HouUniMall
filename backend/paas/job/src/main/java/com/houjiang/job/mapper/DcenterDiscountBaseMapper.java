package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterDiscountBaseEntity;
import com.houjiang.job.vo.DcenterDiscountBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterDiscountBase
 * @author Amos
 */
@Mapper

public interface DcenterDiscountBaseMapper extends BaseMapper<DcenterDiscountBaseEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterDiscountBaseVO>  getDecenterDiscountBaseVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




