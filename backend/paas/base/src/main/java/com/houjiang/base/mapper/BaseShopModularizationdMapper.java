package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.base.dto.ModularizationdDTO;
import com.houjiang.base.entity.BaseShopModularizationdEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.houjiang.base.entity.BaseShopModularizationd
 * @author Amos
 */
@Mapper
public interface BaseShopModularizationdMapper extends BaseMapper<BaseShopModularizationdEntity> {

    /**
     * 添加模块化数据
     *
     * @param param 模块化数据传输对象
     * @return 添加影响的行数
     */
    Integer add(@Param("param") ModularizationdDTO param);

    /**
     * 更新模块化信息
     *
     * @param param 模块化数据传输对象，包含更新的信息
     * @return 更新影响的行数
     */
    Integer updateInfo(@Param("param") ModularizationdDTO param);

    /**
     * 根据店铺ID选择模块化数据
     *
     * @param shopId 店铺ID
     * @return 模块化数据传输对象，包含查询到的数据
     */
    ModularizationdDTO selectModularizationdByStart(@Param("shopId")Integer shopId);

    /**
     * 查询所有模块化数据
     *
     * @param page 分页对象，用于分页查询
     * @param param 模块化数据传输对象，用于查询条件
     * @return 包含模块化实体的分页对象
     */
    IPage<BaseShopModularizationdEntity> queryModularizationdAll(IPage<BaseShopModularizationdEntity> page , @Param("param") ModularizationdDTO param);

    /**
     * 根据ID查询模块化数据
     *
     * @param param 包含查询参数的模块化数据传输对象
     * @return 返回查询到的模块化数据传输对象
     */
    ModularizationdDTO queryModularizationdById(@Param("param") ModularizationdDTO param);
}




