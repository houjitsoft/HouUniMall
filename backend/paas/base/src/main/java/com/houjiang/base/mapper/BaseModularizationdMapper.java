package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.base.dto.ModularizationdDTO;
import com.houjiang.base.entity.BaseModularizationdEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName BaseModularizationdMapper
 * @Description  组件化 页面
 * @Author Amos
 * @Date 2023-07-04 17:58:51
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseModularizationdMapper extends BaseMapper<BaseModularizationdEntity> {

    /*
/**
     * 添加组件化页面
     */
    Integer addModularizationd(@Param("param") ModularizationdDTO param);

    /*
/**
     * 修改组件化页面
     */
    Integer updateModularizationd(@Param("param") ModularizationdDTO param);

    /*
/**
     * 查询组件化页面 列表
     */
    IPage<ModularizationdDTO> queryModularizationdAll(IPage<ModularizationdDTO> page,@Param("param") ModularizationdDTO param);

    /*
/**
     * 查询组件化页面 列表
     */
    ModularizationdDTO queryModularizationdById(@Param("param") ModularizationdDTO param);

    /*
/**
     * 删除   组件化页面
     */
    Integer modularizationdDelete(@Param("param") ModularizationdDTO param);

    /*
/**
     * 将页面设置为未使用
     */
    Integer updateStartAll();

    /*
/**
     * 将页面设置为使用
     */
    Integer UpdateStart(@Param("param") ModularizationdDTO param);

    /**
     * 查询商品是否参加活动
     */
    Integer queryCommonIsXian(@Param("goodsId") Integer goodsId);


    /**
     * 根据id 查询系统默认页面
     */
    ModularizationdDTO queryModularizationdDefault(@Param("param")ModularizationdDTO param);

    /**
     * 添加底部导航
     * @param param
     * @return
     */
    int addBottomNavigation(@Param("param")ModularizationdDTO param);

    /**
     * 更新底部导航
     * @param param
     * @return
     */
    int updateBottomNavigation(@Param("param")ModularizationdDTO param);


    /**
     * 添加底部导航
     * @param param
     * @return
     */
    Integer add(@Param("param") ModularizationdDTO param);
}
