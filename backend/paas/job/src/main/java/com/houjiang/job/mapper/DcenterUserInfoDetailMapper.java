package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterUserInfoDetailEntity;
import com.houjiang.job.vo.DcenterUserInfoDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterUserInfoDetail
 * @author Amos
 */
@Mapper
public interface DcenterUserInfoDetailMapper extends BaseMapper<DcenterUserInfoDetailEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterUserInfoDetailVO> getDecenterUserInfoDetailVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




