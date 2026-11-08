package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterShopJoinBaseEntity;
import com.houjiang.job.vo.DcenterShopJoinBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterShopJoinBase
 * @author Amos
 */
@Mapper
public interface DcenterShopJoinBaseMapper extends BaseMapper<DcenterShopJoinBaseEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterShopJoinBaseVO>  getDecenterShopJoinBaseVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




