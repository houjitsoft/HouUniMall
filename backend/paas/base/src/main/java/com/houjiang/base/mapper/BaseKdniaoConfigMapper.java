package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.dto.BasekdiniaoConfigDTO;
import com.houjiang.base.entity.BaseKdniaoConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.houjiang.base.entity.BaseKdniaoConfig
 * @author Amos
 */
@Mapper
public interface BaseKdniaoConfigMapper extends BaseMapper<BaseKdniaoConfigEntity> {

    /**
     * 修改配置
     *
     * @param basekdiniaoConfigReq
     */
    void updateKdiniaoConfig(@Param("param") BasekdiniaoConfigDTO basekdiniaoConfigReq);
}




