package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterUserCartEntity;
import com.houjiang.job.vo.DcenterUserCartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterUserCart
 * @author Amos
 */
@Mapper
public interface DcenterUserCartMapper extends BaseMapper<DcenterUserCartEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterUserCartVO>  getDecenterUserCartVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




