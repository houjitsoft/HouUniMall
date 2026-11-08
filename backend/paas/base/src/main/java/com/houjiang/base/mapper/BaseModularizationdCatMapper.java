package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.base.dto.ModularizationdCatDTO;
import com.houjiang.base.entity.BaseModularizationdCatEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.houjiang.base.entity.BaseModularizationdCat
 * @author Amos
 */
@Mapper
public interface BaseModularizationdCatMapper extends BaseMapper<BaseModularizationdCatEntity> {

    /**
     * 选择所有模块化的猫信息
     *
     * @param page 分页对象，用于封装分页信息
     * @param param 查询参数，用于条件筛选
     */
    IPage<BaseModularizationdCatEntity> selectAllModularizationdCat(IPage<BaseModularizationdCatEntity> page , @Param("param") ModularizationdCatDTO param);

    /**
     * 添加模块化的猫信息
     *
     * @param modularizationdCatParam 要添加的猫的信息
     */
    void addModularizationdCat(@Param("param") ModularizationdCatDTO modularizationdCatParam);

    /**
     * 更新模块化的猫信息
     *
     * @param modularizationdCatParam 要更新的猫的信息
     */
    void updateModularizationdCat(@Param("param")ModularizationdCatDTO modularizationdCatParam);

}




