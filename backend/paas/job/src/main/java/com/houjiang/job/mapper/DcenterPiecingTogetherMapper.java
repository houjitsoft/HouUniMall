package com.houjiang.job.mapper;

import com.houjiang.job.entity.DcenterPiecingTogetherEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.vo.DcenterPiecingTogetherVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterPiecingTogether
 * @author Amos
 */
@Mapper
public interface DcenterPiecingTogetherMapper extends BaseMapper<DcenterPiecingTogetherEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterPiecingTogetherVO>  getDecenterPiecingTogetherVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




