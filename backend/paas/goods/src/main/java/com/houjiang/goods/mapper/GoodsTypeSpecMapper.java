package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.GoodsTypeSpecEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.goods.entity.GoodsTypeSpec
 * @author Amos
 */
@Mapper
public interface GoodsTypeSpecMapper extends BaseMapper<GoodsTypeSpecEntity> {


    /**
     * 新增类型规格
     *
     * @param typeSpecList
     */
    void insertTypeSpecBatch(@Param("typeSpecList") List<GoodsTypeSpecEntity> typeSpecList);

    /**
     * 更新类型规格
     *
     * @param typeSpecList
     */
    void updateTypeSpecBatch(@Param("typeSpecList")List<GoodsTypeSpecEntity> typeSpecList);



}




