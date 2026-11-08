package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.ModularizationdDTO;
import com.houjiang.base.entity.BaseShopModularizationdEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface BaseShopModularizationdService extends IService<BaseShopModularizationdEntity> {

    /**
     *底部导航栏配置
/**
     * @param param
     * @return
     */
    ResponseResult bottomNavigation(ModularizationdDTO param);


    /**
     *获取导航列表
/**
     * @param param
     * @return
     */
    ResponseResult querybottomNavigation(Map<String,Object> param);

    /**
     * 移动端首页获取导航
     * @param param
     * @return
     */
    ResponseResult indexQueryBottomNavigation(Map<String,Object> param);

    /**
     * 查询店铺模板
     * @param map
     * @return
     */
    ResponseResult modularizationdUpdateIndex(Map<String,Object> map);


    /**
     * 添加或修改 组件化页面
     * @param param
     * @return
     */
     ResponseResult ModularizationdAddOrUPdate(ModularizationdDTO param);


    /*
/**
     * 查询组件化页面 列表
     */
     ResponseResult queryModularizationdAll(ModularizationdDTO param);

    /*
/**
     * 查询组件化页面 id
     */
     ResponseResult queryModularizationdShopById(ModularizationdDTO param);


    /**
     * 删除   组件化页面
     * @param param
     * @return
     */
    ResponseResult modularizationdDelete(ModularizationdDTO param);


    /**
     * 修改页面 是否使用
     */
    ResponseResult modularizationdUpdateStart(ModularizationdDTO param);
}
