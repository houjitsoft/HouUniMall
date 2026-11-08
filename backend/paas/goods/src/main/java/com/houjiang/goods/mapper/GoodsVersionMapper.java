package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.goods.dto.GoodsVersionDTO;
import com.houjiang.goods.entity.GoodsVersionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品版本(GoodsVersion)表数据库访问层
 *
 * @author Amos
 * @since 2025-04-29 13:25:21
 */
@Mapper
public interface GoodsVersionMapper extends BaseMapper<GoodsVersionEntity> {

    /**
     * 查询商品版本列表
     *
     * @param dto 商品版本查询条件DTO，包含分页信息和查询条件
     * @return 包含商品版本实体的分页工具类
     */
    IPage<GoodsVersionEntity> queryGoodsVersionList(Page<GoodsVersionEntity> page,@Param("param") GoodsVersionDTO dto);
}
