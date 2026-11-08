package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.userms.entity.UserPointsLogEntity;
import com.houjiang.userms.vo.IntegralValueListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @Entity com.houjiang.userms.entity.UserPointsLog
 * @author Amos
 */
@Mapper
public interface UserPointsLogMapper extends BaseMapper<UserPointsLogEntity> {

    /**
     * 分页查询
     *
     * @param page
     * @param params
     * @return
     */
    IPage<IntegralValueListVO> queryIntegralValueListPort(IPage<IntegralValueListVO> page, @Param("param") Map<String, Object> params);

}




