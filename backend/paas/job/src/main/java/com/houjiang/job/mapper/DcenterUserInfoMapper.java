package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterUserInfoEntity;
import com.houjiang.job.vo.DcenterUserInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterUserInfo
 * @author Amos
 */
@Mapper
public interface DcenterUserInfoMapper extends BaseMapper<DcenterUserInfoEntity> {


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterUserInfoVO> getDecenterUserInfoVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




