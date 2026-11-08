package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterSearchWordEntity;
import com.houjiang.job.vo.DcenterSearchWordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterSearchWord
 * @author Amos
 */
@Mapper
public interface DcenterSearchWordMapper extends BaseMapper<DcenterSearchWordEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterSearchWordVO>  getDecenterSearchWordVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




