package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterReductionPolicyBaseEntity;
import com.houjiang.job.vo.DcenterReductionPolicyBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterReductionPolicyBase
 * @author Amos
 */
@Mapper
public interface DcenterReductionPolicyBaseMapper extends BaseMapper<DcenterReductionPolicyBaseEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterReductionPolicyBaseVO>  getDecenterReductionPolicyBaseVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




