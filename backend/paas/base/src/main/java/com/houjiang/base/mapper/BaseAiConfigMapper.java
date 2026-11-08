package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.base.dto.BaseAiConfigDTO;
import com.houjiang.base.entity.BaseAiConfigEntity;
import com.houjiang.base.vo.BaseAiConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI服务配置Mapper接口
 *
 * @author Amos
 * @since 2026-01-07
 */
@Mapper
public interface BaseAiConfigMapper extends BaseMapper<BaseAiConfigEntity> {

    /**
     * 分页查询AI配置列表
     */
    IPage<BaseAiConfigVO> queryAiConfigList(Page<BaseAiConfigVO> page, @Param("param") BaseAiConfigDTO param);

    /**
     * 查询所有启用的AI配置
     */
    List<BaseAiConfigVO> queryEnabledConfigs();

    /**
     * 查询默认AI配置
     */
    BaseAiConfigVO getDefaultConfig();

    /**
     * 查询所有启用的默认AI配置（每种功能类型各一个默认时可能返回多条）
     */
    List<BaseAiConfigVO> getDefaultConfigList();
}
