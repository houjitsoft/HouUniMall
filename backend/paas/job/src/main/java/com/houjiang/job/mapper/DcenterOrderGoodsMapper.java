package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterOrderGoodsEntity;
import com.houjiang.job.vo.DcenterOrderGoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterOrderGoods
 * @author Amos
 */
@Mapper
public interface DcenterOrderGoodsMapper extends BaseMapper<DcenterOrderGoodsEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterOrderGoodsVO> getDecenterOrderGoodsVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);
}




