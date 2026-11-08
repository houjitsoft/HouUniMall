package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.dto.BaseOssConfigDTO;
import com.houjiang.base.entity.BaseOssConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName BaseOssConfigMapper
 * @Description OSS
 * @Author Amos
 * @Date 2023/6/21 16:43
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseOssConfigMapper extends BaseMapper<BaseOssConfigEntity> {

    /**
     * 查询信息
     *
     * @return
     */
    BaseOssConfigEntity selectOSSInfo(String ossType);

    /**
     * 插入OSS（对象存储服务）信息到数据库
     *
     * @return 返回影响的行数，成功插入返回1，否则返回0
     */
    int insertOSSInfo();

    /**
     * 根据ID获取OSS配置信息
     *
     * @param
     * @return 返回影响的行数，找到对应配置返回1，否则返回0
     */
    int getOSSConfigbyId();

    /**
     * 更新OSS配置信息
     *
     * @param baseOssConfigReq 包含更新信息的OSS配置数据传输对象
     */
    void updateOssConfig(@Param("param") BaseOssConfigDTO baseOssConfigReq);
}
