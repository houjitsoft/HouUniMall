package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterShopBaseEntity;
import com.houjiang.job.vo.DcenterShopBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterShopBase
 * @author Amos
 */
@Mapper
public interface DcenterShopBaseMapper extends BaseMapper<DcenterShopBaseEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterShopBaseVO> getDecenterShopBaseVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);
}




