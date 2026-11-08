package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.dto.base.BasePlatformBrankDTO;
import com.houjiang.base.entity.BasePlatformBrankEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName BasePlatformBrankMapper
 * @Description  平台收款银行卡表
 * @Author Amos
 * @Date 2023-07-03 09:56:25
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BasePlatformBrankMapper extends BaseMapper<BasePlatformBrankEntity> {

    /**
     * 获取平台收款银行卡信息
     * @return
     */
    BasePlatformBrankEntity queryBrank();

    /**
     * 新增银行
     *
     * @param basePlatformBrankDto
     */
    void insertBrank(@Param("param") BasePlatformBrankDTO basePlatformBrankDto);

}
